package com.syncodec.momento.notebookComponent

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.insets.navigationBarsPadding
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.syncodec.momento.custom.LoadingView
import com.syncodec.momento.konstant.Konstant
import com.syncodec.momento.konstant.Status
import com.syncodec.momento.notebookComponent.miscellaneous.TopBar
import com.syncodec.momento.notebookComponent.modalBottomSheet.BottomSheetType
import com.syncodec.momento.notebookComponent.modalBottomSheet.SheetLayout
import com.syncodec.momento.notebookComponent.screen.NotebookScreen
import com.syncodec.momento.ui.theme.MomentoTheme
import compose.icons.TablerIcons
import compose.icons.tablericons.Notebook
import compose.icons.tablericons.Notes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotebookActivity : ComponentActivity() {
	private val viewModel by viewModels<NotebookViewModel>()

	@OptIn(
		ExperimentalPagerApi::class, ExperimentalMaterialApi::class,
		ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class
	)
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		viewModel.notebookKey = intent.getStringExtra(Konstant.Companion.Konstant.PRIMARY_KEY.name)!!
		CoroutineScope(Dispatchers.IO).launch { viewModel.initData() }

		setContent {
			viewModel.activityState = rememberNotebookActivityState()

			MomentoTheme {
				val systemUiController = rememberSystemUiController()
				systemUiController.setStatusBarColor(MaterialTheme.colorScheme.secondaryContainer)

				Screen()
			}
		}
	}

	override fun onBackPressed() {
		when {
			viewModel.activityState.isSelected.value -> {
				viewModel.activityState.selectedItemList.removeAll { true }
				viewModel.activityState.isSelected.value = false
			}
			viewModel.chapterPath.isNotEmpty() -> {
				viewModel.chapterPath.removeLast()
				viewModel.chapterNamePath.removeLast()
				viewModel.activityState.showNotes.value = true
				viewModel.activityState.showChapters.value = true
			}
			else -> {
				super.onBackPressed()
			}
		}
	}

	private fun onClick(click: Click, data: Any? = null) {
		when (click) {
			Click.CLICK_NOTE -> {
				data as String
				val selectedItemList = viewModel.activityState.selectedItemList
				if (viewModel.activityState.isSelected.value) {
					if (data in selectedItemList) selectedItemList.remove(data) else selectedItemList.add(data)
				} else {

				}
			}
			Click.LONG_CLICK_NOTE -> {
				data as String
				val selectedItemList = viewModel.activityState.selectedItemList
				viewModel.activityState.isSelected.value = true
				if (data in selectedItemList) selectedItemList.remove(data) else selectedItemList.add(data)
			}
			Click.CLICK_CHAPTER -> {
				data as String
				val selectedItemList = viewModel.activityState.selectedItemList
				if (viewModel.activityState.isSelected.value) {
					if (data in selectedItemList) selectedItemList.remove(data) else selectedItemList.add(data)
				} else {
					viewModel.chapterPath.add(data)
					val chapterName = viewModel.chapterList.find { it.key == data }!!.title
					viewModel.chapterNamePath.add(chapterName)
				}
			}
			Click.LONG_CLICK_CHAPTER -> {
				data as String
				val selectedItemList = viewModel.activityState.selectedItemList
				viewModel.activityState.isSelected.value = true
				if (data in selectedItemList) selectedItemList.remove(data) else selectedItemList.add(data)
			}
			Click.NOTE_HEADER -> viewModel.activityState.showNotes.value = !viewModel.activityState.showNotes.value
			Click.CHAPTER_HEADER -> viewModel.activityState.showChapters.value = !viewModel.activityState.showChapters.value
			Click.BREAD_CRUMB -> {
				data as Int
				Log.i("npr71", "dropper : ${viewModel.chapterPath.size - data}")

				for (i in 0 until viewModel.chapterPath.size - data) {
					viewModel.chapterPath.removeLast()
					viewModel.chapterNamePath.removeLast()
				}
			}
		}
	}

	@OptIn(
		ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class, ExperimentalMaterialApi::class,
		androidx.compose.animation.ExperimentalAnimationApi::class
	)
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
		val noteList = viewModel.noteList
		val chapterList = viewModel.chapterList

		ModalBottomSheetLayout(
			sheetState = viewModel.activityState.bottomSheetState,
			sheetElevation = 0.dp,
			sheetBackgroundColor = Color.Transparent,
			sheetContent = {
				SheetLayout()
			},
		) {
			Scaffold(
				topBar = {
					TopBar(
						status = status,
						title = viewModel.notebookDbEntry.title,
						chapterRoute = viewModel.chapterNamePath
					) { click, index -> onClick(click, index) }
				},
			) {
				AnimatedContent(
					targetState = status
				) {
					when (it) {
						Status.INIT -> LoadingView()
						Status.LOADING -> LoadingView()
						Status.LOADED -> {
							Box(
								modifier = Modifier
									.fillMaxSize()
							) {
								NotebookScreen(
									noteList = noteList.filter { it.chapterPath == viewModel.chapterPath },
									chapterList = chapterList.filter { it.chapterPath == viewModel.chapterPath },
									selectedItemList = viewModel.activityState.selectedItemList,
									showNotes = viewModel.activityState.showNotes.value,
									showChapters = viewModel.activityState.showChapters.value,
									showArchived = viewModel.activityState.showArchived.value,
									showFavourite = viewModel.activityState.showFavourite.value,
									showLocked = viewModel.activityState.showLocked.value
								) { click, data -> onClick(click = click, data = data) }
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
					elevation = 0.dp,
					enabled = true,
					shape = RoundedCornerShape(16.dp),
					onClick = { onClickAddNote() }
				) {
					Row(
						verticalAlignment = Alignment.CenterVertically,
						horizontalArrangement = Arrangement.Center,
						modifier = Modifier
							.fillMaxHeight()
							.padding(16.dp, 0.dp),
					) {
						Icon(
							imageVector = TablerIcons.Notes,
							contentDescription = null,
							tint = MaterialTheme.colorScheme.onPrimaryContainer,
							modifier = Modifier
								.requiredSize(24.dp)
						)
						Spacer(modifier = Modifier.width(8.dp))
						androidx.compose.material3.Text(
							text = "Add new note",
							style = MaterialTheme.typography.bodyLarge,
							color = MaterialTheme.colorScheme.onPrimaryContainer,
							textAlign = TextAlign.Center,
							lineHeight = 0.sp,
							maxLines = 1,
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
		var selectedItemList: SnapshotStateList<String> = mutableStateListOf()

		var isSelected = mutableStateOf(false)
		var showNotes = mutableStateOf(true)
		var showChapters = mutableStateOf(true)
		var showArchived = mutableStateOf(false)
		var showFavourite = mutableStateOf(false)
		var showLocked = mutableStateOf(false)
	}

	@OptIn(ExperimentalMaterialApi::class)
	@Composable
	fun rememberNotebookActivityState(
		bottomSheetState: ModalBottomSheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden),
	) = remember {
		ActivityState(bottomSheetState)
	}

	enum class Click {
		CLICK_NOTE,
		LONG_CLICK_NOTE,
		CLICK_CHAPTER,
		LONG_CLICK_CHAPTER,
		NOTE_HEADER,
		CHAPTER_HEADER,
		BREAD_CRUMB
	}

	enum class ComponentType {
		ALL,
		CHAPTER,
		NOTE
	}

	companion object {
		const val ANIMATION_DURATION = 600
	}
}
