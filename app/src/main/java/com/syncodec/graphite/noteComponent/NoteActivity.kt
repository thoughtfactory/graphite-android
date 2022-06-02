package com.syncodec.graphite.noteComponent

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.ClipData
import android.content.ClipboardManager
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
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
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
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.rememberPagerState
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.syncodec.graphite.Graphite
import com.syncodec.graphite.attachmentComponent.AttachmentActivity
import com.syncodec.graphite.custom.LoadingView
import com.syncodec.graphite.custom.googleMap.rememberMapViewWithLifecycle
import com.syncodec.graphite.custom.richText.RichTextEditor
import com.syncodec.graphite.custom.richText.rememberRichTextEditorWithLifecycle
import com.syncodec.graphite.konstant.Konstant
import com.syncodec.graphite.konstant.Status
import com.syncodec.graphite.miscellaneous.StringUtils.Companion.encrypt
import com.syncodec.graphite.miscellaneous.ThemeUtils.Companion.tone
import com.syncodec.graphite.miscellaneous.locationAddressFilter
import com.syncodec.graphite.noteComponent.miscellaneous.MapLocationPopup
import com.syncodec.graphite.noteComponent.miscellaneous.NotificationType
import com.syncodec.graphite.noteComponent.miscellaneous.TopBar
import com.syncodec.graphite.noteComponent.modalBottomSheet.BottomSheetType
import com.syncodec.graphite.noteComponent.modalBottomSheet.SheetLayout
import com.syncodec.graphite.noteComponent.screen.NoteEditorScreen
import com.syncodec.graphite.noteComponent.screen.NoteViewerScreen
import com.syncodec.graphite.ui.theme.GraphiteBase
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

		viewModel.showArchived = intent.getBooleanExtra(Konstant.Companion.Konstant.SHOW_ARCHIVED.name, false)
		viewModel.showLocked = intent.getBooleanExtra(Konstant.Companion.Konstant.SHOW_LOCKED.name, false)

		viewModel.viewerKey.value = intent.getStringExtra(Konstant.Companion.Konstant.NOTE_KEY.name)
		val title = intent.getStringExtra(Konstant.Companion.Konstant.TITLE.name)
		if (viewModel.viewerKey.value == null) viewModel.createNewNote(title) else viewModel.openNotebook()

		setContent {
			GraphiteBase {
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
								dataText.substring(0, minOf(256, dataText.length)).encrypt()

							viewModel.putNote()

							viewModel.openNotebook()
							viewModel.isNew = false
							viewModel.activityState.showAddressCard.value = false
						}
					}
				)

				viewModel.activityState.richTextEditor.setOnPrintData(
					object : RichTextEditor.OnPrintDataListener {
						override fun onPrintData(data: String) {
							viewModel.printNote(htmlContent = data)
						}
					}
				)

				viewModel.activityState.richTextEditor.setOnPlainGetText(
					object : RichTextEditor.OnGetTextListener {
						override fun onGetPlainText(data: String) {
							val clipboard: ClipboardManager =
								getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
							val clip = ClipData.newPlainText("note", data)
							clipboard.setPrimaryClip(clip)
							Toast.makeText(
								this@NoteActivity,
								"Text copied...",
								Toast.LENGTH_SHORT
							).show()
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
				val calendar = Calendar.getInstance()
				calendar.timeInMillis = viewModel.userTimestamp.value
				DatePickerDialog(
					this,
					{ _, year, month, day ->
						calendar.set(Calendar.YEAR, year)
						calendar.set(Calendar.MONTH, month)
						calendar.set(Calendar.DAY_OF_MONTH, day)

						viewModel.userTimestamp.value = calendar.timeInMillis

						TimePickerDialog(
							this,
							{ _, hour, minute ->
								calendar.set(Calendar.HOUR_OF_DAY, hour)
								calendar.set(Calendar.MINUTE, minute)

								viewModel.userTimestamp.value = calendar.timeInMillis
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
			Action.TOGGLE_FAVOURITE -> viewModel.isFavourite.value = !viewModel.isFavourite.value
			Action.TOGGLE_ARCHIVE -> viewModel.isArchived.value = !viewModel.isArchived.value
			Action.TOGGLE_LOCKED -> viewModel.isLocked.value = !viewModel.isLocked.value
			Action.INSERT_FILE -> {
				data as Pair<*, *>
				val isPremium = data.second as Boolean

				(data.first as List<*>).apply {
					if (isPremium) {
						this.forEach { uri -> viewModel.insertAttachment(uri as Uri) }
					} else if (viewModel.attachmentMap.size < 4) {
						this.subList(0, minOf(4, this.size, maxOf(0, 4 - viewModel.attachmentMap.size)))
							.forEach { viewModel.insertAttachment(uri = it as Uri) }
					} else {
						Toast.makeText(
							this@NoteActivity,
							"Subscribe to Graphite Premium to add more attachments",
							Toast.LENGTH_SHORT
						).show()
					}
				}
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
			Action.PRINT -> {
				activityState.richTextEditor.exec("editor.printData(${viewModel.noteContent.value?.toString()});")
				activityState.coroutineScope.launch { activityState.bottomSheetState.hide() }
			}
			Action.EXPORT -> {
				if (note != null) {
					(application as Graphite).exportNote(
						noteDbEntry = note,
						noteContent = viewModel.noteContent.value,
						tagList = viewModel.connectedTag
					)
				}
			}
			Action.COPY -> {
				activityState.richTextEditor.exec("editor.getPlainText(${viewModel.noteContent.value?.toString()});")
				activityState.coroutineScope.launch { activityState.bottomSheetState.hide() }
			}
			Action.DELETE -> {
				viewModel.delete()
				activityState.coroutineScope.launch { activityState.bottomSheetState.hide() }
			}
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
					if (viewModel.address.value != null) {
						activityState.addressState.value = AddressState.SUCCESS
					} else if (viewModel.address.value == null && viewModel.latLng.value != null) {
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
			Action.DISMISS_MAP_DIALOG_AND_UPDATE_LOCATION -> {
				data as Pair<*, *>
				activityState.showMapLocationDialog.value = false
				viewModel.latLng.value = data.first as LatLng?
				viewModel.address.value = data.second as String?
				activityState.addressState.value = AddressState.REMOVED
				if (viewModel.address.value != null) {
					activityState.addressState.value = AddressState.SUCCESS
				} else if (viewModel.latLng.value != null) {
					activityState.addressState.value = AddressState.LOCATION
				}
			}
			Action.REVERSE_GEOCODE -> {
				data as LatLng
				viewModel.reverseGeocode(
					latitude = data.latitude,
					longitude = data.longitude,
					onAddressAvailable = { _address ->
						scope.launch {
							viewModel.latLng.value = LatLng(data.latitude, data.longitude)
							viewModel.address.value = locationAddressFilter(_address)
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
	)
	@Composable
	private fun Screen() {
		val viewerKey by viewModel.viewerKey
		val activityState = viewModel.activityState
		val bottomSheetType by activityState.bottomSheetType
		val note by viewModel.noteDbEntry
		val tagList = viewModel.tagList
		val connectedTag = viewModel.connectedTag
		val attachmentMap = viewModel.attachmentMap
		val addressState by activityState.addressState
		val mapView = activityState.mapView

		LaunchedEffect(key1 = viewerKey) {
			if (viewerKey == null) onPerformAction(Action.TRY_GET_LOCATION, null)
		}

		ModalBottomSheetLayout(
			sheetState = viewModel.activityState.bottomSheetState,
			sheetElevation = 0.dp,
			sheetBackgroundColor = Color.Transparent,
			sheetShape = RoundedCornerShape(16.dp, 16.dp, 0.dp, 0.dp),
			sheetContent = {
				SheetLayout(
					bottomSheetType = bottomSheetType,
					createdTimestamp = viewModel.createdTimestamp.value,
					modifiedTimestamp = viewModel.modifiedTimestamp.value,
					latLng = viewModel.latLng.value,
					address = viewModel.address.value,
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
		val noteDbEntry by viewModel.noteDbEntry
		val addressState by activityState.addressState
		val showAddressCard by activityState.showAddressCard
		val status by viewModel.status

		var isSaved by activityState.isSaved

		val userTimestamp by viewModel.userTimestamp
		val latLng by viewModel.latLng
		val address by viewModel.address
		val isFavourite by viewModel.isFavourite
		val isArchived by viewModel.isArchived
		val isLocked by viewModel.isLocked

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
			userTimestamp = userTimestamp,
			latLng = latLng,
			address = address,
			isFavourite = isFavourite,
			isArchived = isArchived,
			isLocked = isLocked,
			addressState = addressState,
			showAddressCard = showAddressCard,
			status = status,
		) { action, data -> onPerformAction(action = action, data = data) }

		AnimatedVisibility(
			visible = isSaved,
			enter = fadeIn(tween(600)),
			exit = fadeOut(tween(600)),
			modifier = Modifier.fillMaxSize()
		) { LoadingView() }

		MapLocationPopup(
			showMapLocationDialog = activityState.showMapLocationDialog.value,
			latLng = viewModel.latLng.value,
			address = viewModel.address.value,
		) { action, data -> onPerformAction(action, data) }
	}

	@OptIn(ExperimentalPagerApi::class)
	@Composable
	private fun ViewerScreen() {
		val scope = rememberCoroutineScope()
		val noteKeyList = viewModel.noteKeyList
		val pagerState = viewModel.activityState.pagerState
		val status by viewModel.status

		val noteDbEntry by viewModel.noteDbEntry
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
		INSERT_FILE,
		OPEN_ATTACHMENT,
		REMOVE_ATTACHMENT,
		PRINT,
		EXPORT,
		COPY,
		DELETE,
		TAG_BUTTON,
		TRY_GET_LOCATION,
		REFRESH_LOCATION,
		OPEN_MAP_DIALOG,
		REMOVE_LOCATION,
		DISMISS_MAP_DIALOG,
		DISMISS_MAP_DIALOG_AND_UPDATE_LOCATION,
		REVERSE_GEOCODE,
		ADD_TAG,
		CONNECT_TAG
	}
}
