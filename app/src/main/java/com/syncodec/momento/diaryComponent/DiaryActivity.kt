package com.syncodec.momento.diaryComponent

import android.Manifest
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.syncodec.momento.custom.EditorView
import com.syncodec.momento.diaryComponent.miscellaneous.*
import com.syncodec.momento.diaryComponent.modalBottomSheet.BottomSheetType
import com.syncodec.momento.diaryComponent.modalBottomSheet.SheetLayout
import com.syncodec.momento.konstant.ErrorCode
import com.syncodec.momento.ui.theme.MomentoTheme
import org.json.JSONObject


class DiaryActivity : ComponentActivity() {

	private val viewModel by viewModels<DiaryViewModel>()

	private lateinit var editorView: EditorView

	@OptIn(
		ExperimentalPagerApi::class, ExperimentalMaterialApi::class,
		ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class
	)
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		editorView = EditorView(this)

		editorView.setOnSaveData(object : EditorView.OnSaveDataListener {
			override fun onSaveData(data: String) {
				val dataJson = JSONObject(data)
				val dataHtml = dataJson.getString("dataHtml")
				val dataText = dataJson.getString("dataText")

				viewModel.note.content = dataHtml
				viewModel.note.contentThumbnail = dataText

				viewModel.saveDiary()
			}
		})

		setContent {
			MomentoTheme {
				viewModel.diaryActivityState = rememberDiaryActivityState()
				DiaryEditorScreen()
			}
		}
	}

	@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class, ExperimentalPermissionsApi::class)
	@Composable
	private fun DiaryEditorScreen() {
		val systemUiController = rememberSystemUiController()
		systemUiController.setStatusBarColor(MaterialTheme.colorScheme.primaryContainer)

		val activityState = viewModel.diaryActivityState


		when {
			activityState.locationPermissionState.hasPermission -> {
				Log.i("npr71", "location hasPermission...")
				viewModel.diaryActivityState.addressState.value = AddressState.REQUESTED
				viewModel.getLocation()
			}
			activityState.locationPermissionState.shouldShowRationale -> {
				Log.i("npr71", "address show rationale...")
				viewModel.diaryActivityState.addressState.value = AddressState.SHOW_RATIONALE
			}
			!activityState.locationPermissionState.permissionRequested -> {
				Log.i("npr71", "address request permission...")
				viewModel.diaryActivityState.addressState.value = AddressState.REQUEST_PERMISSION
			}
			else -> {
				Log.i("npr71", "address no permission...")
				viewModel.diaryActivityState.addressState.value = AddressState.NO_PERMISSION
			}
		}

		ModalBottomSheetLayout(
			sheetState = viewModel.diaryActivityState.bottomSheetState,
			sheetElevation = 0.dp,
			sheetBackgroundColor = Color.Transparent,
			sheetShape = RoundedCornerShape(16.dp, 16.dp, 0.dp, 0.dp),
			sheetContent = {
				SheetLayout()
			},
		) {
			Box(
				modifier = Modifier
					.background(Color.Black)
					.scale(1f)
			) {
				Scaffold(
					topBar = {
						DiaryEditorTopBar {
							editorView.exec("editor.getData();")
						}
					}
				) {
					Column {
						Box(
							modifier = Modifier
								.fillMaxWidth()
								.weight(1f)
						) {
							AndroidView(
								factory = { editorView },
								update = { editor ->

								},
								modifier = Modifier
									.fillMaxWidth()
									.fillMaxHeight()
									.background(MaterialTheme.colorScheme.primaryContainer)
							)
							AddressCard()
							NotificationLayout()
							MapLocationPopup()
						}
						EditorToolbar(
							editorView = editorView
						) { errorCode ->
							when(errorCode) {
								ErrorCode.Companion.ErrorCode.URL_RANGE_SELECTION_ERROR -> viewModel.diaryActivityState.notificationType.value = NotificationType.UrlSelectionNotification
							}
							viewModel.diaryActivityState.isNotificationVisible.value = true
						}
					}
				}
			}
		}
	}

	@OptIn(ExperimentalMaterialApi::class)
	class DiaryActivityState @OptIn(ExperimentalPermissionsApi::class) constructor(
		val bottomSheetState: ModalBottomSheetState,
		val locationPermissionState: PermissionState,
		var bottomSheetType: MutableState<BottomSheetType> = mutableStateOf(BottomSheetType.MediaBottomSheet),
		var notificationType: MutableState<NotificationType> = mutableStateOf(NotificationType.UrlSelectionNotification),
		var isNotificationVisible: MutableState<Boolean> = mutableStateOf(false),
		var addressState: MutableState<AddressState> = mutableStateOf(AddressState.INIT),
		var showAddressCard: MutableState<Boolean> = mutableStateOf(false),
		var showMapLocationDialog: MutableState<Boolean> = mutableStateOf(false)
	)

	@OptIn(ExperimentalMaterialApi::class, ExperimentalPermissionsApi::class)
	@Composable
	fun rememberDiaryActivityState(
		bottomSheetState: ModalBottomSheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden),
		locationPermissionState: PermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
	) = remember {
		DiaryActivityState(
			bottomSheetState = bottomSheetState,
			locationPermissionState = locationPermissionState
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
