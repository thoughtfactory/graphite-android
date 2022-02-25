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
import com.syncodec.momento.Momento
import com.syncodec.momento.custom.googleMap.rememberMapViewWithLifecycle
import com.syncodec.momento.custom.richText.RichTextEditor
import com.syncodec.momento.custom.richText.rememberRichTextEditorWithLifecycle
import com.syncodec.momento.konstant.Konstant
import com.syncodec.momento.miscellaneous.PREFERENCE_KEY_NOTE_SHOW_LOCATION_PERMISSION
import com.syncodec.momento.miscellaneous.PREFERENCE_KEY_VAULT_KEY
import com.syncodec.momento.miscellaneous.dataStore
import com.syncodec.momento.noteComponent.miscellaneous.NotificationType
import com.syncodec.momento.noteComponent.miscellaneous.TopBar
import com.syncodec.momento.noteComponent.modalBottomSheet.BottomSheetType
import com.syncodec.momento.noteComponent.modalBottomSheet.SheetLayout
import com.syncodec.momento.noteComponent.screen.NoteEditorScreen
import com.syncodec.momento.noteComponent.screen.NoteViewerScreen
import com.syncodec.momento.ui.theme.MomentoTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONObject


class NoteActivity : ComponentActivity() {

	private val viewModel by viewModels<NoteViewModel>()

	@OptIn(
		ExperimentalPagerApi::class, ExperimentalMaterialApi::class,
		ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class, com.google.accompanist.permissions.ExperimentalPermissionsApi::class
	)
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		intent.getIntExtra(Konstant.Companion.Konstant.COMPONENT_TYPE.name, Momento.Companion.ComponentType.DIARY.ordinal).apply {
			viewModel.componentType = Momento.Companion.ComponentType.values()[this]

			if (viewModel.componentType == Momento.Companion.ComponentType.NOTE) {
				viewModel.note.notebookKey = intent.getStringExtra(Konstant.Companion.Konstant.NOTEBOOK_KEY.name)
				viewModel.note.notebookRoute = intent.getStringArrayListExtra(Konstant.Companion.Konstant.CHAPTER_KEY.name)
				viewModel.note.title = intent.getStringExtra(Konstant.Companion.Konstant.TITLE.name)
			} else {
				viewModel.isViewer = intent.getBooleanExtra(Konstant.Companion.Konstant.IS_VIEWER.name, true)
				viewModel.viewerDiaryKey = intent.getStringExtra(Konstant.Companion.Konstant.DIARY_KEY.name)
			}
		}

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

						when (viewModel.componentType) {
							Momento.Companion.ComponentType.DIARY -> viewModel.saveDiary()
							Momento.Companion.ComponentType.NOTE -> viewModel.saveNote()
							else -> viewModel.saveDiary()
						}
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
		val showLocationPermissionKeyFlow: Flow<Boolean?> = dataStore.data.map { preferences -> preferences[PREFERENCE_KEY_NOTE_SHOW_LOCATION_PERMISSION] }
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
