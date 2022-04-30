package com.syncodec.graphite.noteComponent

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.rememberPagerState
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.syncodec.graphite.R
import com.syncodec.graphite.attachmentComponent.AttachmentActivity
import com.syncodec.graphite.custom.googleMap.rememberMapViewWithLifecycle
import com.syncodec.graphite.custom.richText.RichTextEditor
import com.syncodec.graphite.custom.richText.rememberRichTextEditorWithLifecycle
import com.syncodec.graphite.konstant.Konstant
import com.syncodec.graphite.konstant.Status
import com.syncodec.graphite.miscellaneous.ThemeUtils.Companion.tone
import com.syncodec.graphite.miscellaneous.locationAddressFilter
import com.syncodec.graphite.noteComponent.miscellaneous.NotificationType
import com.syncodec.graphite.noteComponent.miscellaneous.TopBar
import com.syncodec.graphite.noteComponent.modalBottomSheet.BottomSheetType
import com.syncodec.graphite.noteComponent.modalBottomSheet.SheetLayout
import com.syncodec.graphite.noteComponent.screen.NoteEditorScreen
import com.syncodec.graphite.noteComponent.screen.NoteViewerScreen
import com.syncodec.graphite.ui.theme.GraphiteTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.*


class NoteActivity : ComponentActivity() {

	private val viewModel by viewModels<NoteViewModel>()

	@OptIn(
		ExperimentalPagerApi::class,
		ExperimentalMaterialApi::class,
		ExperimentalFoundationApi::class,
		ExperimentalMaterial3Api::class,
		ExperimentalPermissionsApi::class
	)
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		intent.getStringExtra(Konstant.Companion.Konstant.NOTEBOOK_KEY.name).also {
			if (it == null) finish() else viewModel.notebookKey = it
		}
		intent.getStringArrayListExtra(Konstant.Companion.Konstant.CHAPTER_KEY.name).also {
			if (it == null) finish() else viewModel.chapterPath = it.toMutableStateList()
		}

		intent.hasExtra(Konstant.Companion.Konstant.IS_NEW.name).also {
			if (it) {
				intent.getBooleanExtra(Konstant.Companion.Konstant.IS_NEW.name, false).also {
					viewModel.isNew = it
				}
			} else finish()
		}

		viewModel.viewerKey.value = intent.getStringExtra(Konstant.Companion.Konstant.NOTE_KEY.name)
		val title = intent.getStringExtra(Konstant.Companion.Konstant.TITLE.name)
		if (viewModel.viewerKey.value == null) viewModel.createNewNote(title) else viewModel.openNotebook()

		setContent {
			GraphiteTheme {
				val systemUiController = rememberSystemUiController()
				systemUiController.setStatusBarColor(
					MaterialTheme.colorScheme.surface.tone(isSystemInDarkTheme(), 1)
				)
				systemUiController.setNavigationBarColor(
					MaterialTheme.colorScheme.surface.tone(isSystemInDarkTheme(), 1)
				)

				viewModel.activityState = rememberActivityState()

				viewModel.activityState.richTextEditor.setOnSaveData(
					object : RichTextEditor.OnSaveDataListener {
						override fun onSaveData(data: String) {
							val dataObject = JSONObject(data)
							val dataJson = dataObject.getJSONObject("dataJson")
							val dataText = dataObject.getString("dataText")

							viewModel.noteContent.value = dataJson
							viewModel.noteDbEntry.value?.contentThumbnail =
								dataText.substring(0, minOf(256, dataText.length))

							viewModel.putNote()
						}
					}
				)

				Screen()
			}
		}
	}

	override fun onBackPressed() {
		if (viewModel.activityState.isSaving.value) {
			Toast.makeText(this, "Please wait while saving data", Toast.LENGTH_SHORT).show()
		} else {
			super.onBackPressed()
		}
	}


	@OptIn(
		ExperimentalPagerApi::class, ExperimentalPermissionsApi::class,
		ExperimentalMaterialApi::class
	)
	private fun onPerformAction(action: Action, data: Any? = null) {
		val scope = viewModel.activityState.coroutineScope
		val activityState = viewModel.activityState
		val note = viewModel.noteDbEntry.value

		when (action) {
			Action.FINISH -> {
				if (activityState.isSaving.value) {
					Toast.makeText(this, "Please wait while saving data", Toast.LENGTH_SHORT).show()
				} else {
					finish()
				}
			}
			Action.SAVE_NOTE -> {
				if (!activityState.isSaving.value) {
					Toast.makeText(this, "Saving data...", Toast.LENGTH_SHORT).show()
					activityState.isSaving.value = true
					activityState.richTextEditor.exec("editor.getData();")
					viewModel.openNotebook()
					viewModel.isNew = true
					activityState.showAddressCard.value = false
				}
			}
			Action.EDITOR_READY -> viewModel.status.value = Status.LOADED
			Action.OPEN_METADATA -> {
				activityState.bottomSheetType.value = BottomSheetType.MetadataBottomSheet
				scope.launch {
					activityState.bottomSheetState.animateTo(ModalBottomSheetValue.Expanded)
				}
			}
			Action.OPEN_MENU -> {
				activityState.bottomSheetType.value = BottomSheetType.MenuBottomSheet
				scope.launch {
					activityState.bottomSheetState.animateTo(ModalBottomSheetValue.Expanded)
				}
			}
			Action.EDIT_NOTE -> {
				if (note != null) {
					viewModel.loadAttachment(note.key)
					activityState.richTextEditor.exec("editor.commands.setContent(${viewModel.noteContent.value!!});")
					viewModel.viewerKey.value = null
				}
			}
			Action.NEXT_PAGE -> {
				data as Int
				scope.launch {
					activityState.pagerState.animateScrollToPage(
						minOf(data - 1, activityState.pagerState.currentPage + 1)
					)
				}
			}
			Action.PREV_PAGE -> scope.launch {
				activityState.pagerState.animateScrollToPage(
					maxOf(0, activityState.pagerState.currentPage - 1)
				)
			}
			Action.SHOW_ADDRESS -> activityState.showAddressCard.value = true
			Action.HIDE_ADDRESS -> activityState.showAddressCard.value = false
			Action.REQUEST_LOCATION_PERMISSION -> activityState.locationPermissionState.launchPermissionRequest()
			Action.SELECT_TIME -> {
				val calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"))
				calendar.timeInMillis = note?.userTimestamp ?: 0
				DatePickerDialog(
					this,
					{ _, year, month, day ->
						calendar.set(Calendar.YEAR, year)
						calendar.set(Calendar.MONTH, month)
						calendar.set(Calendar.DAY_OF_MONTH, day)

						note?.userTimestamp = calendar.timeInMillis
						viewModel.emitNote()

						TimePickerDialog(
							this,
							{ _, hour, minute ->
								calendar.set(Calendar.HOUR_OF_DAY, hour)
								calendar.set(Calendar.MINUTE, minute)

								note?.userTimestamp = calendar.timeInMillis
								viewModel.emitNote()
							},
							calendar.get(Calendar.HOUR_OF_DAY),
							calendar.get(Calendar.MINUTE),
							false
						).show()
					},
					calendar.get(Calendar.YEAR),
					calendar.get(Calendar.MONTH),
					calendar.get(Calendar.DAY_OF_MONTH)
				).show()
			}
			Action.ATTACHMENT_BUTTON -> {
				activityState.bottomSheetType.value = BottomSheetType.AttachmentBottomSheet
				scope.launch {
					activityState.bottomSheetState.animateTo(ModalBottomSheetValue.Expanded)
				}
			}
			Action.TOGGLE_FAVOURITE -> {
				if (note != null) {
					note.isFavourite = !note.isFavourite
					viewModel.emitNote()
				}
			}
			Action.TOGGLE_ARCHIVE -> {
				if (note != null) {
					note.isArchived = !note.isArchived
					viewModel.emitNote()
				}
			}
			Action.TOGGLE_LOCKED -> {
				if (note != null) {
					note.isLocked = !note.isLocked
					viewModel.emitNote()
				}
			}
			Action.INSERT_PICTURE -> {
				data as Uri?
				if (data != null) viewModel.insertAttachment(uri = data)
			}
			Action.INSERT_MEDIA -> {
				data as List<*>
				data.forEach { viewModel.insertAttachment(uri = it as Uri) }
			}
			Action.INSERT_FILE -> {
				data as List<*>
				data.forEach { viewModel.insertAttachment(uri = it as Uri) }
			}
			Action.OPEN_ATTACHMENT -> {
				if (note != null) {
					Intent(this, AttachmentActivity::class.java).apply {
						putExtra(Konstant.Companion.Konstant.IS_NOTE.name, true)
						putExtra(Konstant.Companion.Konstant.NOTE_KEY.name, note.key)
						startActivity(this)
					}
				}
			}
			Action.REMOVE_ATTACHMENT -> viewModel.attachmentMap.remove(data as String)
			Action.TAG_BUTTON -> {
				activityState.bottomSheetType.value = BottomSheetType.TagBottomSheet
				scope.launch {
					activityState.bottomSheetState.animateTo(ModalBottomSheetValue.Expanded)
				}
			}
			Action.TRY_GET_LOCATION -> {
				data as Boolean?
				val locationPermissionState = activityState.locationPermissionState
				if (viewModel.isNew == true || data == true) {
					when {
						locationPermissionState.hasPermission -> {
							activityState.addressState.value = AddressState.PERMISSION_REQUESTED
							viewModel.getLocation()
						}
						locationPermissionState.shouldShowRationale -> {
							activityState.addressState.value = AddressState.SHOW_RATIONALE
						}
						!locationPermissionState.permissionRequested -> {
							activityState.addressState.value = AddressState.REQUEST_PERMISSION
						}
						else -> {
							activityState.addressState.value = AddressState.NO_PERMISSION
						}
					}
				} else {
					if (note?.address != null) {
						activityState.addressState.value = AddressState.SUCCESS
					} else if (note?.address == null && note?.latLng!=null) {
						activityState.addressState.value = AddressState.LOCATION
					} else {
						activityState.addressState.value = AddressState.REMOVED
					}
				}
			}
			Action.REFRESH_LOCATION -> viewModel.getLocation()
			Action.OPEN_MAP_DIALOG -> activityState.showMapLocationDialog.value = true
			Action.REMOVE_LOCATION -> viewModel.removeLocationData()
			Action.DISMISS_MAP_DIALOG -> activityState.showMapLocationDialog.value = false
			Action.REVERSE_GEOCODE -> {
				data as LatLng
				viewModel.reverseGeocode(
					latitude = data.latitude,
					longitude = data.longitude,
					onAddressAvailable = { _address ->
						scope.launch {
							note?.latLng = LatLng(data.latitude, data.longitude)
							note?.address = locationAddressFilter(_address)
							viewModel.emitNote()
						}
					},
					onIoException = {
						Log.i(
							"Diary Activity",
							"Reverse Geocode : IO Exception : Maybe network unavailable"
						)
					},
					onException = {
						Log.e("Diary Activity", "Reverse Geocode : Exception")
					}
				)
			}
			Action.ADD_TAG -> viewModel.addTag(tag = data as String)
			Action.CONNECT_TAG -> viewModel.connectTag(tag = data as String)
		}
	}

	@OptIn(
		ExperimentalMaterialApi::class,
		ExperimentalMaterial3Api::class,
		ExperimentalPagerApi::class
	)
	@Composable
	private fun Screen() {
		val viewerKey by viewModel.viewerKey
		val activityState = viewModel.activityState
		val bottomSheetType by activityState.bottomSheetType
		val note by viewModel.knotDbEntry.collectAsState()
		val tagList by viewModel.tagList.collectAsState(listOf())
		val connectedTag = viewModel.connectedTag
		val attachmentMap = viewModel.attachmentMap
		var addressState by activityState.addressState
		val mapView = activityState.mapView

		LaunchedEffect(key1 = viewerKey) {
			if (viewerKey == null) {
				onPerformAction(Action.TRY_GET_LOCATION, null)
			}
		}

		ModalBottomSheetLayout(
			sheetState = viewModel.activityState.bottomSheetState,
			sheetElevation = 0.dp,
			sheetBackgroundColor = Color.Transparent,
			sheetShape = RoundedCornerShape(16.dp, 16.dp, 0.dp, 0.dp),
			sheetContent = {
				SheetLayout(
					bottomSheetType = bottomSheetType,
					note = note,
					tagList = tagList,
					connectedTag = connectedTag,
					attachmentMap = attachmentMap,
					addressState = addressState,
					mapView = mapView
				) { action, data -> onPerformAction(action = action, data = data) }
			},
		) {
			Scaffold(
				topBar = {
					TopBar(
						isViewer = viewModel.viewerKey.value != null,
						isSaving = viewModel.activityState.isSaving.value
					) { action, data -> onPerformAction(action, data) }
				}
			) {
				Crossfade(
					targetState = viewModel.viewerKey.value == null,
					animationSpec = tween(600)
				) { if (it) EditorScreen() else ViewerScreen() }
			}
		}
	}

	@OptIn(ExperimentalPermissionsApi::class)
	@Composable
	private fun EditorScreen() {
		val scope = rememberCoroutineScope()
		val configuration = LocalConfiguration.current
		val screenWidth = configuration.screenWidthDp.dp

		val activityState = viewModel.activityState
		val noteDbEntry by viewModel.knotDbEntry.collectAsState()
		val addressState by activityState.addressState
		val showAddressCard by activityState.showAddressCard
		val status by viewModel.status

		var isSaved by activityState.isSaved
		val lottieComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.lottie_saved))

		LaunchedEffect(key1 = isSaved) {
			if (isSaved) {
				scope.launch {
					delay(1200)
					isSaved = false
					viewModel.viewerKey.value = viewModel.noteDbEntry.value!!.key
				}
			}
		}

		NoteEditorScreen(
			richTextEditor = activityState.richTextEditor,
			noteDbEntry = noteDbEntry,
			addressState = addressState,
			showAddressCard = showAddressCard,
			status = status,
		) { action, data -> onPerformAction(action = action, data = data) }

		AnimatedVisibility(
			visible = isSaved,
			enter = fadeIn(tween(600)),
			exit = fadeOut(tween(600)),
			modifier = Modifier.fillMaxSize()
		) {
			LottieAnimation(
				composition = lottieComposition,
				isPlaying = isSaved,
				iterations = LottieConstants.IterateForever,
				modifier = Modifier.requiredSize(screenWidth / 3)
			)
		}
	}

	@OptIn(ExperimentalPagerApi::class)
	@Composable
	private fun ViewerScreen() {
		val scope = rememberCoroutineScope()
		val noteKeyList = viewModel.noteKeyList
		val pagerState = viewModel.activityState.pagerState
		val status by viewModel.status

		val noteDbEntry by viewModel.knotDbEntry.collectAsState()
		val noteContent by viewModel.noteContent

		LaunchedEffect(key1 = noteKeyList.hashCode() + pagerState.pageCount.hashCode()) {
			scope.launch {
				val index = noteKeyList.indexOf(viewModel.viewerKey.value)
				if (index != -1 && pagerState.pageCount == noteKeyList.size) pagerState.scrollToPage(
					page = index
				)
			}
		}

		LaunchedEffect(key1 = pagerState.currentPage + noteKeyList.size) {
			snapshotFlow { pagerState.currentPage }.collect {
				if (noteKeyList.size > it) viewModel.loadNote(noteKeyList[it])
			}
		}

		NoteViewerScreen(
			noteKeyList = noteKeyList,
			pagerState = viewModel.activityState.pagerState,
			noteDbEntry = noteDbEntry,
			noteContent = noteContent,
			connectedTag = viewModel.connectedTag,
			status = status
		) { onPerformAction(it, noteKeyList.size) }
	}

	@OptIn(ExperimentalMaterialApi::class)
	inner class ActivityState @OptIn(
		ExperimentalPermissionsApi::class,
		ExperimentalPagerApi::class
	) constructor(
		val coroutineScope: CoroutineScope,
		val richTextEditor: RichTextEditor,
		val bottomSheetState: ModalBottomSheetState,
		val locationPermissionState: PermissionState,
		val mapView: MapView,
		val pagerState: PagerState,
		var bottomSheetType: MutableState<BottomSheetType> = mutableStateOf(BottomSheetType.AttachmentBottomSheet),
		var notificationType: MutableState<NotificationType> = mutableStateOf(NotificationType.UrlSelectionNotification),
		var isNotificationVisible: MutableState<Boolean> = mutableStateOf(false),
		var addressState: MutableState<AddressState> = mutableStateOf(AddressState.INIT),
		var showAddressCard: MutableState<Boolean> = mutableStateOf(false),
		var showMapLocationDialog: MutableState<Boolean> = mutableStateOf(false),
		var isSaving: MutableState<Boolean> = mutableStateOf(false),
		var isSaved: MutableState<Boolean> = mutableStateOf(false)
	)

	@OptIn(
		ExperimentalMaterialApi::class,
		ExperimentalPermissionsApi::class,
		ExperimentalPagerApi::class
	)
	@Composable
	fun rememberActivityState(
		coroutineScope: CoroutineScope = rememberCoroutineScope(),
		richTextEditor: RichTextEditor = rememberRichTextEditorWithLifecycle(),
		bottomSheetState: ModalBottomSheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden),
		locationPermissionState: PermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION),
		mapView: MapView = rememberMapViewWithLifecycle(),
		pagerState: PagerState = rememberPagerState()
	) = remember {
		ActivityState(
			coroutineScope = coroutineScope,
			richTextEditor = richTextEditor,
			bottomSheetState = bottomSheetState,
			locationPermissionState = locationPermissionState,
			mapView = mapView,
			pagerState = pagerState
		)
	}

	enum class AddressState {
		OFF,
		INIT,
		NO_PERMISSION,
		REQUEST_PERMISSION,
		SHOW_RATIONALE,
		PERMISSION_REQUESTED,
		LOCATION_REQUESTED,
		LOCATION,
		SUCCESS,
		ERROR,
		REMOVED
	}

	enum class Action {
		FINISH,
		SAVE_NOTE,
		EDIT_NOTE,
		EDITOR_READY,
		OPEN_METADATA,
		OPEN_MENU,
		NEXT_PAGE,
		PREV_PAGE,
		SHOW_ADDRESS,
		HIDE_ADDRESS,
		REQUEST_LOCATION_PERMISSION,
		SELECT_TIME,
		ATTACHMENT_BUTTON,
		TOGGLE_FAVOURITE,
		TOGGLE_ARCHIVE,
		TOGGLE_LOCKED,
		INSERT_PICTURE,
		INSERT_MEDIA,
		INSERT_FILE,
		OPEN_ATTACHMENT,
		REMOVE_ATTACHMENT,
		TAG_BUTTON,
		TRY_GET_LOCATION,
		REFRESH_LOCATION,
		OPEN_MAP_DIALOG,
		REMOVE_LOCATION,
		DISMISS_MAP_DIALOG,
		REVERSE_GEOCODE,
		ADD_TAG,
		CONNECT_TAG
	}
}
