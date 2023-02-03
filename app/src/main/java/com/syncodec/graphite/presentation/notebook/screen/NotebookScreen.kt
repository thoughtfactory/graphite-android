package com.syncodec.graphite.presentation.notebook.screen

import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.tooling.preview.Preview
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.ErrorView
import com.syncodec.graphite.presentation.common.LoadingView
import com.syncodec.graphite.presentation.common.scaffold.GenericButton
import com.syncodec.graphite.presentation.common.scaffold.GenericScaffold
import com.syncodec.graphite.presentation.main.composable.buildingBlock.EmptyView
import com.syncodec.graphite.presentation.note.NoteActivity
import com.syncodec.graphite.presentation.notebook.screen.composable.bar.BottomBar
import com.syncodec.graphite.presentation.notebook.screen.composable.bar.TopBar
import com.syncodec.graphite.presentation.notebook.screen.composable.bottomSheet.NotebookBottomSheetType
import com.syncodec.graphite.presentation.notebook.screen.composable.bottomSheet.SheetLayout
import com.syncodec.graphite.presentation.notebook.screen.composable.dialog.NotebookDialog
import com.syncodec.graphite.presentation.notebook.screen.composable.dialog.NotebookDialogType
import com.syncodec.graphite.presentation.notebook.screen.buildingBlock.Explorer
import com.syncodec.graphite.utils.ContentStatus
import com.syncodec.graphite.utils.Extra
import com.syncodec.graphite.utils.decodeBase64ToBitmap
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel


@OptIn(ExperimentalMaterialApi::class)
@Preview
@Composable
fun NotebookScreen() {
	val context = LocalContext.current
	val scope = rememberCoroutineScope()
	val viewModel : NotebookScreenViewModel = koinViewModel()
	val hapticFeedback = LocalHapticFeedback.current
	val backPressedDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

	val defaultChapterId by viewModel.defaultChapterId.collectAsState()

	val chapterId by viewModel.chapterId.collectAsState()
	val createdTimestamp by viewModel.createdTimestamp.collectAsState()
	val modifiedTimestamp by viewModel.modifiedTimestamp.collectAsState()
	val title by viewModel.title.collectAsState()
	val description by viewModel.description.collectAsState()
	val color by viewModel.color.collectAsState()
	val thumbnail by viewModel.thumbnail.collectAsState()
	val isFavourite by viewModel.isFavourite.collectAsState()
	val isLocked by viewModel.isLocked.collectAsState()
	val chapterList by viewModel.chapterList.collectAsState()
	val noteList by viewModel.noteList.collectAsState()
	val parentId by viewModel.parentId.collectAsState()

	val chapterPath by viewModel.chapterPath.collectAsState()

	val tagList by viewModel.tagList.collectAsState()

	val isChapterRefreshing by viewModel.isChapterRefreshing.collectAsState()
	val contentStatus by viewModel.contentStatus.collectAsState()

	var isNoteVisible by remember { mutableStateOf(true) }
	var isChapterVisible by remember { mutableStateOf(true) }

	var isSelecting by remember { mutableStateOf(false) }
	var selectedIdList : List<RealmUUID> by remember { mutableStateOf(listOf()) }
	fun onSelect(id : RealmUUID?) {
		if (! isSelecting) isSelecting = true
		selectedIdList.toMutableList().apply {
			if (id in selectedIdList) remove(id) else id?.let { add(it) }
			selectedIdList = this
		}
		if (selectedIdList.isEmpty()) {
			isNoteVisible = true
			isChapterVisible = true
		}
	}

	val onBackPressedDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

	val activityLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
		try {
			it.data?.let { intent ->
				val hasIntentAction = intent.hasExtra(Extra.Companion.Extra.INTENT_ACTION.name)
				if (hasIntentAction) {
					val intentAction = intent.getStringExtra(Extra.Companion.Extra.INTENT_ACTION.name)?.let { it1 ->
						Extra.Companion.IntentAction.valueOf(it1)
					}
					if (intentAction == Extra.Companion.IntentAction.DELETE) {
						val hasObjectId = intent.hasExtra(Extra.Companion.Extra.OBJECT_ID.name)
						if (hasObjectId) intent.getByteArrayExtra(Extra.Companion.Extra.OBJECT_ID.name)?.let { bytes ->
							try {
								viewModel.delete(listOf(RealmUUID.from(bytes)))
							} catch (e : Exception) {
								null
							}
						}
					}
				}
				Extra.Companion.Extra.INTENT_ACTION.name
				Extra.Companion.Extra.OBJECT_ID.name
			}
		} catch (e : Exception) {
			Toast.makeText(context, "Error performing action", Toast.LENGTH_SHORT).show()
		}
	}

	fun onClickNote(id : RealmUUID) {
		if (isSelecting) {
			isChapterVisible = false
			onSelect(id)
		} else {
			Intent(context, NoteActivity::class.java).apply {
				putExtra(Extra.Companion.Extra.IsNew.name, false)
				putExtra(Extra.Companion.Extra.NoteId.name, id.bytes)
				putExtra(Extra.Companion.Extra.Filter.name, Extra.Companion.Filter.SingleRead.name)

				activityLauncher.launch(this)
			}
		}
	}

	fun onClickChapter(id : RealmUUID?) {
		if (isSelecting) {
			isNoteVisible = false
			onSelect(id)
		} else viewModel.loadChapter(chapterId = id)
	}

	fun onClickNewNote() {
		Intent(context, NoteActivity::class.java).apply {
			putExtra(Extra.Companion.Extra.IsNew.name, true)
			putExtra(Extra.Companion.Extra.ParentId.name, chapterId?.bytes)
			putExtra(Extra.Companion.Extra.Filter.name, Extra.Companion.Filter.SingleRead.name)

			activityLauncher.launch(this)
		}
	}

	val modalBottomSheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
	var sheetType by remember { mutableStateOf(NotebookBottomSheetType.Menu) }
	fun openSheet(type : NotebookBottomSheetType) = scope.launch { sheetType = type; modalBottomSheetState.show() }
	fun closeSheet() = scope.launch { modalBottomSheetState.hide() }

	var showEditChapterDialog by remember { mutableStateOf(false) }
	var showDeleteSelectedDialog by remember { mutableStateOf(false) }
	var showDeleteChapterDialog by remember { mutableStateOf(false) }

	fun openDialog(dialogType : NotebookDialogType) = when (dialogType) {
		NotebookDialogType.EditChapter -> showEditChapterDialog = true
		NotebookDialogType.DeleteSelected -> showDeleteSelectedDialog = true
		NotebookDialogType.DeleteChapter -> showDeleteChapterDialog = true
	}

	fun closeDialog(dialogType : NotebookDialogType) = when (dialogType) {
		NotebookDialogType.EditChapter -> showEditChapterDialog = false
		NotebookDialogType.DeleteSelected -> showDeleteSelectedDialog = false
		NotebookDialogType.DeleteChapter -> showDeleteChapterDialog = false
	}

	Log.i("npr71", "parentId: $parentId")

	BackHandler(enabled = parentId != null) {
		Log.i("npr71", "parentId: $parentId")
		parentId?.let { viewModel.loadChapter(chapterId = RealmUUID.Companion.from(it)) }
	}

	BackHandler(enabled = isSelecting) {
		isSelecting = false
		selectedIdList = listOf()
		isNoteVisible = true
		isChapterVisible = true
	}

	GenericScaffold(
		topBar = {
			TopBar(
				title = title ?: "",
				defaultChapterId = defaultChapterId,
				isLocked = isLocked ?: false,
				isFavourite = isFavourite ?: false,
				chapterPath = chapterPath,
				isSelecting = isSelecting,
				selectedSize = selectedIdList.size,
				onClickBack = { onBackPressedDispatcher?.onBackPressed() },
				onClickCancelSelect = { isSelecting = false; selectedIdList = listOf() },
				onClickLock = { chapterId?.let { viewModel.toggleLock(it) } },
				onClickFavourite = { chapterId?.let { viewModel.toggleFavourite(it) } },
				onClickFilter = { openSheet(NotebookBottomSheetType.Filter) },
				onClickDelete = { openDialog(NotebookDialogType.DeleteSelected) },
				onClickNavigatorChapter = ::onClickChapter,
			)
		},
		bottomBar = { BottomBar(onClickMenu = { openSheet(NotebookBottomSheetType.Menu) }) },
		modalBottomSheetState = modalBottomSheetState,
		sheetContent = {
			SheetLayout(
				bottomSheetType = sheetType,
				chapterId = chapterId,
				createdTimestamp = createdTimestamp,
				modifiedTimestamp = modifiedTimestamp,
				title = title,
				description = description,
				color = color?.let { Color(it) },
				thumbnail = thumbnail?.decodeBase64ToBitmap(),
				parentId = parentId?.let { RealmUUID.Companion.from(it) },
				isDefault = chapterId == defaultChapterId,
				onClickSetDefaultChapter = viewModel::setDefaultChapter,
				onClickEdit = { openDialog(NotebookDialogType.EditChapter) },
				onClickDelete = { openDialog(NotebookDialogType.DeleteChapter) },
				putChapter = { title, description, color, thumbnail ->
					chapterId?.let {
						viewModel.putInnerChapter(parentId = it, title = title, description = description, color = color, thumbnail = thumbnail)
					} ?: Toast.makeText(context, "Error adding chapter", Toast.LENGTH_SHORT).show()
				},
				onCloseSheet = ::closeSheet,
			)
		},
		dialogContent = {
			NotebookDialog(
				title = title ?: "",
				description = description ?: "",
				color = color?.let { Color(it) },
				thumbnail = thumbnail?.decodeBase64ToBitmap(),
				showEditChapterDialog = showEditChapterDialog,
				showDeleteSelectedDialog = showDeleteSelectedDialog,
				showDeleteChapterDialog = showDeleteChapterDialog,
				onUpdateChapter = { title, description, color, thumbnail ->
					chapterId?.let {
						viewModel.updateChapter(it, title, description, color, thumbnail)
					} ?: Toast.makeText(context, "Error updating chapter", Toast.LENGTH_SHORT).show()
				},
				onDeleteSelected = {
					viewModel.delete(selectedIdList.toList())
					isSelecting = false
					selectedIdList = listOf()
					isNoteVisible = true
					isChapterVisible = true
					closeDialog(NotebookDialogType.DeleteSelected)
				},
				onDeleteChapter = {
					if (chapterId == defaultChapterId) {
						Toast.makeText(context, "Cannot delete default chapter", Toast.LENGTH_SHORT).show()
					} else {
						closeDialog(NotebookDialogType.DeleteChapter)
						chapterId?.let { viewModel.deleteCurrentChapter(it) }
					}
				},
				closeDialog = { closeDialog(it) },
			)
		},
		isBottomBarVisible = ! isSelecting,
		primaryButton = GenericButton(
			text = "New Note",
			icon = R.drawable.ic_pencil,
			onClick = ::onClickNewNote,
		),
		secondaryButton = GenericButton(
			text = "New Chapter",
			icon = R.drawable.ic_notebook,
		) { openSheet(NotebookBottomSheetType.Chapter) },
		isButtonVisible = ! isSelecting,
	) {
		Crossfade(
			targetState = contentStatus,
			animationSpec = tween(300)
		) { contentStatus ->
			when (contentStatus) {
				ContentStatus.Init -> LoadingView()
				ContentStatus.Error -> ErrorView()
				ContentStatus.Loading -> LoadingView()
				ContentStatus.LoadedEmpty -> EmptyView(
					image = R.drawable.il_empty_chapter,
					title = "Keep a diary, and perhaps someday it will keep you",
					subTitle = "― Mae West",
				)

				ContentStatus.Loaded -> SwipeRefresh(
					state = rememberSwipeRefreshState(isRefreshing = isChapterRefreshing),
					modifier = Modifier.fillMaxSize(),
					onRefresh = {
						chapterId?.let { viewModel.refresh(it) } ?: Toast.makeText(context, "Error refreshing chapter", Toast.LENGTH_SHORT).show()
					}
				) {
					Explorer(
						noteObjectList = noteList,
						chapterObjectList = chapterList,
						tagList = tagList,
						isNoteListVisible = isNoteVisible,
						isChapterListVisible = isChapterVisible,
						isSelecting = isSelecting,
						selectedIdList = selectedIdList,
						onToggleNoteVisibility = { isNoteVisible = ! isNoteVisible },
						onToggleChapterVisibility = { isChapterVisible = ! isChapterVisible },
						onClickNote = ::onClickNote,
						onLongClickNote = { hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress); isChapterVisible = false; onSelect(it) },
						onClickChapter = ::onClickChapter,
						onLongClickChapter = { hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress); isNoteVisible = false; onSelect(it) },
					)
				}
			}
		}
	}
}
