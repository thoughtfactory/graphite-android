package com.syncodec.momento.notebookComponent

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.google.accompanist.insets.navigationBarsPadding
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.syncodec.momento.R
import com.syncodec.momento.konstant.Konstant
import com.syncodec.momento.konstant.Status
import com.syncodec.momento.notebookComponent.miscellaneous.TopBar
import com.syncodec.momento.notebookComponent.modalBottomSheet.BottomSheetType
import com.syncodec.momento.notebookComponent.modalBottomSheet.SheetLayout
import com.syncodec.momento.notebookComponent.screen.NotebookScreen
import com.syncodec.momento.ui.theme.MomentoTheme
import compose.icons.TablerIcons
import compose.icons.tablericons.Note
import compose.icons.tablericons.Notebook
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class NotebookActivity : ComponentActivity() {
	private val viewModel by viewModels<ViewModel>()

	@OptIn(
		ExperimentalPagerApi::class, ExperimentalMaterialApi::class,
		ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class
	)
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		viewModel.notebookKey = intent.getStringExtra(Konstant.Companion.Konstant.PRIMARY_KEY.name)!!
		viewModel.observeNotebook()
		viewModel.openNotebook()

		setContent {
			val scope = rememberCoroutineScope()
			viewModel.activityState = rememberNotebookActivityState()

			SideEffect {
				scope.launch {
					delay(ANIMATION_DURATION.toLong())
					viewModel.activityState.showContent.value = true
				}
			}

			MomentoTheme {
				val systemUiController = rememberSystemUiController()
				systemUiController.setStatusBarColor(MaterialTheme.colorScheme.primaryContainer)

				Screen()
			}
		}
	}

	override fun onBackPressed() {
		when {
			viewModel.activityState.isSelected.value -> {
				viewModel.activityState.selectedEntryList.removeAll { true }
				viewModel.activityState.isSelected.value = false
			}
			viewModel.currentRoute.isNotEmpty() -> {
				CoroutineScope(Dispatchers.IO).launch {
//					for animating purpose
					viewModel.activityState.showContent.value = false
					delay(ANIMATION_DURATION.toLong())

					viewModel.currentRoute.removeLast()
					viewModel.currentRouteName.removeLast()

					viewModel.activityState.showContent.value = true
				}
			}
			else -> {
				super.onBackPressed()
			}
		}
	}

	@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class, ExperimentalMaterialApi::class)
	@Composable
	private fun Screen() {

		val scope = rememberCoroutineScope()
		val openSheet: (BottomSheetType) -> Unit = { bottomSheetType ->
			viewModel.activityState.bottomSheetType.value = bottomSheetType
			scope.launch {
				viewModel.activityState.bottomSheetState.show()
			}
		}

		val status by viewModel.status


		ModalBottomSheetLayout(
			sheetState = viewModel.activityState.bottomSheetState,
			sheetElevation = 0.dp,
			sheetBackgroundColor = Color.Transparent,
			sheetContent = {
				SheetLayout()
			},
		) {
			Scaffold(
				topBar = { TopBar() },
			) {
				when (status) {
					Status.INIT -> {
					}
					Status.LOADING -> {
						val lottieComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.lottie_loading))
						Box(
							modifier = Modifier.fillMaxSize(),
							contentAlignment = Alignment.Center
						) {
							LottieAnimation(
								composition = lottieComposition,
								iterations = LottieConstants.IterateForever,
								modifier = Modifier
									.requiredSize(64.dp)
							)
						}
					}
					Status.LOADED -> {
						Box(
							modifier = Modifier
								.fillMaxSize()
						) {
							Crossfade(
								targetState = viewModel.activityState.showContent.value,
								animationSpec = tween(
									durationMillis = ANIMATION_DURATION
								)
							) {
								if (it) {
									NotebookScreen()
								}
							}
							FloatingActionButton(
								onClickAddNote = { openSheet(BottomSheetType.NewNoteBottomSheet) },
								onClickAddChapter = { openSheet(BottomSheetType.NewChapterBottomSheet) }
							)
						}
					}
					Status.ERROR -> {
					}
				}
			}
		}
	}

	@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
	@Composable
	private fun FloatingActionButton(
		onClickAddNote: () -> Unit,
		onClickAddChapter: () -> Unit
	) {
		Column(
			modifier = Modifier
				.fillMaxSize(),
			horizontalAlignment = Alignment.CenterHorizontally
		) {
			Spacer(modifier = Modifier.weight(1f))
			Spacer(modifier = Modifier.width(2.dp))
			Row(
				modifier = Modifier
					.fillMaxWidth()
					.padding(16.dp, 0.dp),
				horizontalArrangement = Arrangement.Center,
				verticalAlignment = Alignment.CenterVertically
			) {
				Box(modifier = Modifier.requiredSize(56.dp))
				Card(
					modifier = Modifier
						.height(56.dp),
					backgroundColor = MaterialTheme.colorScheme.primaryContainer,
					shape = RoundedCornerShape(16.dp),
					elevation = 8.dp,
					onClick = { onClickAddNote() }
				) {
					Row(
						modifier = Modifier
							.fillMaxHeight()
							.padding(16.dp, 0.dp),
						verticalAlignment = Alignment.CenterVertically
					) {
						Icon(
							imageVector = TablerIcons.Note,
							contentDescription = null,
							tint = MaterialTheme.colorScheme.onPrimaryContainer,
							modifier = Modifier
								.requiredSize(24.dp)
						)
						Spacer(modifier = Modifier.width(8.dp))
						androidx.compose.material3.Text(
							text = "Add a new note",
							modifier = Modifier,
							style = MaterialTheme.typography.titleSmall,
							fontWeight = FontWeight.Bold,
							color = MaterialTheme.colorScheme.onPrimaryContainer
						)
					}
				}
				Box(
					modifier = Modifier
						.width(3.dp)
						.height(28.dp)
						.clip(RoundedCornerShape(4.dp))
						.background(MaterialTheme.colorScheme.primary)
				)
				FloatingActionButton(
					onClick = {
						onClickAddChapter()
					},
					modifier = Modifier
						.navigationBarsPadding(),
				) {
					Icon(imageVector = TablerIcons.Notebook, contentDescription = null)
				}
			}

			Spacer(modifier = Modifier.height(16.dp))
		}
	}

	@OptIn(ExperimentalMaterialApi::class)
	inner class ActivityState(
		val bottomSheetState: ModalBottomSheetState,
	) {
		var notebookComponentType: MutableState<ComponentType> = mutableStateOf(ComponentType.ALL)
		var bottomSheetType: MutableState<BottomSheetType> = mutableStateOf(BottomSheetType.NewNoteBottomSheet)
		var selectedEntryList: SnapshotStateList<String> = mutableStateListOf()

		var showContent: MutableState<Boolean> = mutableStateOf(false)

		var isSelected = mutableStateOf(false)
		var showArchived = mutableStateOf(false)
		var showFavourite = mutableStateOf(false)
		var showTrash = mutableStateOf(false)
	}

	@OptIn(ExperimentalMaterialApi::class)
	@Composable
	fun rememberNotebookActivityState(
		bottomSheetState: ModalBottomSheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden),
	) = remember {
		ActivityState(bottomSheetState)
	}

	enum class ComponentType {
		ALL,
		CHAPTER,
		NOTE
	}

	companion object {
		const val ANIMATION_DURATION = 400
	}
}
