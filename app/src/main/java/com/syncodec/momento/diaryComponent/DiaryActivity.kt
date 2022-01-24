package com.syncodec.momento.diaryComponent

import android.annotation.SuppressLint
import android.content.Context
import android.location.*
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
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.syncodec.momento.diaryComponent.miscellaneous.AddressCard
import com.syncodec.momento.diaryComponent.miscellaneous.DiaryEditorTopBar
import com.syncodec.momento.diaryComponent.miscellaneous.EditorToolbar
import com.syncodec.momento.diaryComponent.modalBottomSheet.BottomSheetType
import com.syncodec.momento.diaryComponent.modalBottomSheet.SheetLayout
import com.syncodec.momento.ui.theme.MomentoTheme


class DiaryActivity : ComponentActivity() {

	private val viewModel by viewModels<DiaryViewModel>()

	private lateinit var editorView: com.syncodec.momento.custom.EditorView

	@OptIn(
		ExperimentalPagerApi::class, ExperimentalMaterialApi::class,
		ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class
	)
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		editorView = com.syncodec.momento.custom.EditorView(this)

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
					topBar = { DiaryEditorTopBar() }
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
							)
							AddressCard()
						}
						EditorToolbar(editorView)
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
