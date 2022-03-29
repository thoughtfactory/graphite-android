package com.syncodec.momento.noteComponent

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
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
import androidx.compose.foundation.layout.Box
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
import com.syncodec.momento.BuildConfig
import com.syncodec.momento.R
import com.syncodec.momento.custom.googleMap.rememberMapViewWithLifecycle
import com.syncodec.momento.custom.richText.RichTextEditor
import com.syncodec.momento.custom.richText.rememberRichTextEditorWithLifecycle
import com.syncodec.momento.konstant.Konstant
import com.syncodec.momento.miscellaneous.locationAddressFilter
import com.syncodec.momento.noteComponent.miscellaneous.NotificationType
import com.syncodec.momento.noteComponent.miscellaneous.TopBar
import com.syncodec.momento.noteComponent.modalBottomSheet.BottomSheetType
import com.syncodec.momento.noteComponent.modalBottomSheet.SheetLayout
import com.syncodec.momento.noteComponent.screen.NoteEditorScreen
import com.syncodec.momento.noteComponent.screen.NoteViewerScreen
import com.syncodec.momento.ui.theme.MomentoTheme
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

		viewModel.notebookKey =
			intent.getStringExtra(Konstant.Companion.Konstant.NOTEBOOK_KEY.name)!!
		viewModel.chapterPath =
			intent.getStringArrayListExtra(Konstant.Companion.Konstant.CHAPTER_KEY.name)!!
				.toMutableStateList()
		viewModel.viewerKey.value = intent.getStringExtra(Konstant.Companion.Konstant.NOTE_KEY.name)
		val title = intent.getStringExtra(Konstant.Companion.Konstant.TITLE.name)
		if (viewModel.viewerKey.value == null) viewModel.createNewNote(title)

		setContent {
			MomentoTheme {
				val systemUiController = rememberSystemUiController()
				systemUiController.setStatusBarColor(MaterialTheme.colorScheme.secondaryContainer)
				systemUiController.setNavigationBarColor(MaterialTheme.colorScheme.secondaryContainer)

				viewModel.activityState = rememberActivityState()

				viewModel.activityState.richTextEditor.setOnSaveData(
					object : RichTextEditor.OnSaveDataListener {
						override fun onSaveData(data: String) {
							val dataObject = JSONObject(data)
							val dataJson = dataObject.getString("dataJson")
							val dataText = dataObject.getString("dataText")

							viewModel.note.value?.content = dataJson
							viewModel.noteDbEntry.value?.contentThumbnail =
								dataText.substring(0, minOf(128, dataText.length))

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
	private fun onClick(click: Click, data: Any? = null) {
		val scope = viewModel.activityState.coroutineScope
		val activityState = viewModel.activityState
		val noteDbEntry = viewModel.knotDbEntry.value

		when (click) {
			Click.FINISH -> {
				if (activityState.isSaving.value) {
					Toast.makeText(this, "Please wait while saving data", Toast.LENGTH_SHORT).show()
				} else {
					finish()
				}
			}
			Click.SAVE -> {
				if (!activityState.isSaving.value) {
					Toast.makeText(this, "Saving data...", Toast.LENGTH_SHORT).show()
					activityState.isSaving.value = true
					activityState.richTextEditor.exec("editor.getData();")
				}
			}
			Click.METADATA -> {
				activityState.bottomSheetType.value = BottomSheetType.MetadataBottomSheet
				scope.launch {
					activityState.bottomSheetState.animateTo(ModalBottomSheetValue.Expanded)
				}
			}
			Click.MENU -> {
				activityState.bottomSheetType.value = BottomSheetType.MenuBottomSheet
				scope.launch {
					activityState.bottomSheetState.animateTo(ModalBottomSheetValue.Expanded)
				}
			}
			Click.TOP_BAR_QUATERNARY -> {
				activityState.richTextEditor.exec("editor.commands.setContent(${viewModel.note.value!!.content});")
				viewModel.viewerKey.value = null
			}
			Click.NEXT_PAGE -> {
				data as Int
				scope.launch {
					activityState.pagerState.animateScrollToPage(
						minOf(data - 1, activityState.pagerState.currentPage + 1)
					)
				}
			}
			Click.PREV_PAGE -> scope.launch {
				activityState.pagerState.animateScrollToPage(
					maxOf(0, activityState.pagerState.currentPage - 1)
				)
			}
			Click.SHOW_ADDRESS -> activityState.showAddressCard.value = true
			Click.HIDE_ADDRESS -> activityState.showAddressCard.value = false
			Click.REQUEST_LOCATION_PERMISSION -> activityState.locationPermissionState.launchPermissionRequest()
			Click.SELECT_TIME -> {
				val calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"))
				calendar.timeInMillis = noteDbEntry?.userTimestamp ?: 0
				DatePickerDialog(
					this,
					{ _, year, month, day ->
						calendar.set(Calendar.YEAR, year)
						calendar.set(Calendar.MONTH, month)
						calendar.set(Calendar.DAY_OF_MONTH, day)

						noteDbEntry?.userTimestamp = calendar.timeInMillis
						viewModel.emitNote()

						TimePickerDialog(
							this,
							{ _, hour, minute ->
								calendar.set(Calendar.HOUR_OF_DAY, hour)
								calendar.set(Calendar.MINUTE, minute)

								noteDbEntry?.userTimestamp = calendar.timeInMillis
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
			Click.ATTACHMENT_BUTTON -> {
				activityState.bottomSheetType.value =
					BottomSheetType.AttachmentBottomSheet
				scope.launch {
					activityState.bottomSheetState.animateTo(
						ModalBottomSheetValue.Expanded
					)
				}
			}
			Click.INSERT_PICTURE -> {
				data as Uri?
				if (data != null) {
					viewModel.insertAttachment(uri = data)
				}
			}
			Click.INSERT_MEDIA -> {
				data as List<*>
				data.forEach {
					it as Uri
					viewModel.insertAttachment(uri = it)
				}
			}
			Click.OPEN_ATTACHMENT -> null
			Click.REMOVE_ATTACHMENT -> viewModel.attachmentMap.remove(data as String)
			Click.TAG_BUTTON -> {
				activityState.bottomSheetType.value = BottomSheetType.TagBottomSheet
				scope.launch {
					activityState.bottomSheetState.animateTo(
						ModalBottomSheetValue.Expanded
					)
				}
			}
			Click.ADDRESS_CARD -> {
				val addressState by activityState.addressState
				when (addressState) {
					AddressState.NO_PERMISSION -> {
						Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
							this.data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
							startActivity(this)
						}
					}
					AddressState.REQUEST_PERMISSION -> activityState.locationPermissionState.launchPermissionRequest()
					AddressState.SHOW_RATIONALE -> activityState.locationPermissionState.launchPermissionRequest()
					AddressState.REMOVED -> viewModel.getLocation()
				}
			}
			Click.REFRESH_LOCATION -> viewModel.getLocation()
			Click.OPEN_MAP_DIALOG -> activityState.showMapLocationDialog.value = true
			Click.REMOVE_LOCATION -> viewModel.removeLocationData()
			Click.DISMISS_MAP_DIALOG -> activityState.showMapLocationDialog.value = false
			Click.REVERSE_GEOCODE -> {
				data as LatLng
				viewModel.reverseGeocode(
					latitude = data.latitude,
					longitude = data.longitude,
					onAddressAvailable = { _address ->
						scope.launch {
							noteDbEntry?.location?.latitude = data.latitude
							noteDbEntry?.location?.longitude = data.longitude
							noteDbEntry?.address = locationAddressFilter(_address)
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
			Click.ADD_TAG -> viewModel.addTag(tag = data as String)
			Click.CONNECT_TAG -> viewModel.connectTag(tag = data as String)
		}
	}

	@OptIn(
		ExperimentalMaterialApi::class,
		ExperimentalMaterial3Api::class,
		ExperimentalPagerApi::class
	)
	@Composable
	private fun Screen() {
		ModalBottomSheetLayout(
			sheetState = viewModel.activityState.bottomSheetState,
			sheetElevation = 0.dp,
			sheetBackgroundColor = Color.Transparent,
			sheetShape = RoundedCornerShape(16.dp, 16.dp, 0.dp, 0.dp),
			sheetContent = { SheetLayout { click, data -> onClick(click = click, data = data) } },
		) {
			Scaffold(
				topBar = {
					TopBar(
						isViewer = viewModel.viewerKey.value != null,
						isSaving = viewModel.activityState.isSaving.value
					) { onClick(it) }
				}
			) {
				Crossfade(
					targetState = viewModel.viewerKey.value == null,
					animationSpec = tween(600)
				) { if (it) EditorScreen() else ViewerScreen() }
			}
		}
	}

	@Composable
	private fun EditorScreen() {
		val scope = rememberCoroutineScope()
		val configuration = LocalConfiguration.current
		val screenWidth = configuration.screenWidthDp.dp

		var isSaved by viewModel.activityState.isSaved
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

		Box(modifier = Modifier.fillMaxSize()) {
			NoteEditorScreen { click, data -> onClick(click = click, data = data) }

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
	}

	@OptIn(ExperimentalPagerApi::class)
	@Composable
	private fun ViewerScreen() {
		val scope = rememberCoroutineScope()
		val noteKeyList by viewModel.noteKeyListLiveData.collectAsState(initial = listOf())
		val pagerState = viewModel.activityState.pagerState
		val status by viewModel.status

		val noteDbEntry by viewModel.knotDbEntry.collectAsState()
		val note by viewModel.knot.collectAsState()

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
			note = note,
			attachmentMap = viewModel.attachmentMap,
			connectedTag = viewModel.connectedTag,
			status = status
		) { onClick(it, noteKeyList.size) }
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
		REQUESTED,
		LOCATION,
		SUCCESS,
		ERROR,
		REMOVED
	}

	enum class Click {
		FINISH,
		SAVE,
		METADATA,
		MENU,
		TOP_BAR_QUATERNARY,
		NEXT_PAGE,
		PREV_PAGE,
		SHOW_ADDRESS,
		HIDE_ADDRESS,
		REQUEST_LOCATION_PERMISSION,
		SELECT_TIME,
		ATTACHMENT_BUTTON,
		INSERT_PICTURE,
		INSERT_MEDIA,
		OPEN_ATTACHMENT,
		REMOVE_ATTACHMENT,
		TAG_BUTTON,
		ADDRESS_CARD,
		REFRESH_LOCATION,
		OPEN_MAP_DIALOG,
		REMOVE_LOCATION,
		DISMISS_MAP_DIALOG,
		REVERSE_GEOCODE,
		ADD_TAG,
		CONNECT_TAG
	}
}
