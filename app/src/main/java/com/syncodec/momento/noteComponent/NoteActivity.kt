package com.syncodec.momento.noteComponent

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.android.libraries.maps.MapView
import com.syncodec.momento.custom.googleMap.rememberMapViewWithLifecycle
import com.syncodec.momento.custom.richText.RichTextEditor
import com.syncodec.momento.custom.richText.rememberRichTextEditorWithLifecycle
import com.syncodec.momento.konstant.Konstant
import com.syncodec.momento.noteComponent.miscellaneous.NotificationType
import com.syncodec.momento.noteComponent.miscellaneous.TopBar
import com.syncodec.momento.noteComponent.modalBottomSheet.BottomSheetType
import com.syncodec.momento.noteComponent.modalBottomSheet.SheetLayout
import com.syncodec.momento.noteComponent.screen.NoteEditorScreen
import com.syncodec.momento.noteComponent.screen.NoteViewerScreen
import com.syncodec.momento.ui.theme.MomentoTheme
import org.json.JSONObject


class NoteActivity : ComponentActivity() {

	private val viewModel by viewModels<NoteViewModel>()

	@OptIn(
		ExperimentalPagerApi::class, ExperimentalMaterialApi::class,
		ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class
	)
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		viewModel.notebookKey.value = intent.getStringExtra(Konstant.Companion.Konstant.NOTEBOOK_KEY.name)!!
		viewModel.chapterPath = intent.getStringArrayListExtra(Konstant.Companion.Konstant.CHAPTER_KEY.name)!!.toMutableStateList()
		viewModel.note.title = intent.getStringExtra(Konstant.Companion.Konstant.TITLE.name)

		setContent {
			MomentoTheme {
				val systemUiController = rememberSystemUiController()
				systemUiController.setStatusBarColor(MaterialTheme.colorScheme.secondaryContainer)
				systemUiController.setNavigationBarColor(MaterialTheme.colorScheme.secondaryContainer)

				viewModel.activityState = rememberActivityState()

				viewModel.activityState.richTextEditor.setOnSaveData(object : RichTextEditor.OnSaveDataListener {
					override fun onSaveData(data: String) {
						val dataObject = JSONObject(data)
						val dataJson = dataObject.getString("dataJson")
						val dataText = dataObject.getString("dataText")

						viewModel.note.content = dataJson
						viewModel.note.contentThumbnail = dataText

						viewModel.putNote()
					}
				})

				Screen()
			}
		}
	}

	@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
	@Composable
	private fun Screen() {

		ModalBottomSheetLayout(
			sheetState = viewModel.activityState.bottomSheetState,
			sheetElevation = 0.dp,
			sheetBackgroundColor = Color.Transparent,
			sheetShape = RoundedCornerShape(16.dp, 16.dp, 0.dp, 0.dp),
			sheetContent = {
				SheetLayout()
			},
		) {
			Scaffold(
				topBar = { TopBar() }
			) {
				if (viewModel.isViewer) {
					NoteEditorScreen()
				} else {
					NoteViewerScreen()
				}
			}
		}
	}

	@OptIn(ExperimentalMaterialApi::class)
	inner class ActivityState @OptIn(ExperimentalPermissionsApi::class) constructor(
		val richTextEditor: RichTextEditor,
		val bottomSheetState: ModalBottomSheetState,
		val locationPermissionState: PermissionState,
		var bottomSheetType: MutableState<BottomSheetType> = mutableStateOf(BottomSheetType.AttachmentBottomSheet),
		var notificationType: MutableState<NotificationType> = mutableStateOf(NotificationType.UrlSelectionNotification),
		var isNotificationVisible: MutableState<Boolean> = mutableStateOf(false),
		var addressState: MutableState<AddressState> = mutableStateOf(AddressState.INIT),
		var showAddressCard: MutableState<Boolean> = mutableStateOf(false),
		var showMapLocationDialog: MutableState<Boolean> = mutableStateOf(false),
		var mapView: MapView,
	)

	@OptIn(ExperimentalMaterialApi::class, ExperimentalPermissionsApi::class)
	@Composable
	fun rememberActivityState(
		richTextEditor: RichTextEditor = rememberRichTextEditorWithLifecycle(),
		bottomSheetState: ModalBottomSheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden),
		locationPermissionState: PermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION),
		mapView: MapView = rememberMapViewWithLifecycle()
	) = remember {
		ActivityState(
			richTextEditor = richTextEditor,
			bottomSheetState = bottomSheetState,
			locationPermissionState = locationPermissionState,
			mapView = mapView
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
}
