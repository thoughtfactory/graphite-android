package com.syncodec.momento.diaryComponent

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.syncodec.momento.custom.EditorView
import com.syncodec.momento.diaryComponent.miscellaneous.AddressCard
import com.syncodec.momento.diaryComponent.miscellaneous.DiaryEditorTopBar
import com.syncodec.momento.diaryComponent.miscellaneous.EditorToolbar
import com.syncodec.momento.diaryComponent.modalBottomSheet.BottomSheetType
import com.syncodec.momento.diaryComponent.modalBottomSheet.SheetLayout
import com.syncodec.momento.diaryComponent.miscellaneous.NotificationLayout
import com.syncodec.momento.diaryComponent.miscellaneous.NotificationType
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
				val dataHtml = dataJson.getString("html")
				val dataText = dataJson.getString("text")

				viewModel.diary.content = dataHtml
				viewModel.diary.contentThumbnail = dataText
			}
		})

		editorView.setOnSaveCallbackData(object : EditorView.OnSaveDataCallbackListener{
			override fun onSaveDataCallback(data: String) {
				val dataJson = JSONObject(data)
				val dataHtml = dataJson.getString("html")
				val dataText = dataJson.getString("text")

				viewModel.diary.content = dataHtml
				viewModel.diary.contentThumbnail = dataText

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
							editorView.exec("saveData(true);")
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
	class DiaryActivityState(
		val bottomSheetState: ModalBottomSheetState,
	) {
		var bottomSheetType: MutableState<BottomSheetType> = mutableStateOf(BottomSheetType.MediaBottomSheet)
		var notificationType: MutableState<NotificationType> = mutableStateOf(NotificationType.UrlSelectionNotification)
		var isNotificationVisible: MutableState<Boolean> = mutableStateOf(false)
	}

	@OptIn(ExperimentalMaterialApi::class)
	@Composable
	fun rememberDiaryActivityState(
		scaffoldState: ScaffoldState = rememberScaffoldState(),
		bottomSheetState: ModalBottomSheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden),
	) = remember(scaffoldState) {
		DiaryActivityState(bottomSheetState)
	}
}
