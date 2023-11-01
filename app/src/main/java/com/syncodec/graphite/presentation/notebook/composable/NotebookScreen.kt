package com.syncodec.graphite.presentation.notebook.composable

import android.content.Intent
import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.local.ChapterObject
import com.syncodec.graphite.di.model.local.ChapterObjectLite
import com.syncodec.graphite.di.model.local.NoteObjectLite
import com.syncodec.graphite.di.model.local.TagObject
import com.syncodec.graphite.presentation.common.component.LocalComponentColumnCount
import com.syncodec.graphite.presentation.common.component.chapter.chapterList
import com.syncodec.graphite.presentation.common.component.note.noteList
import com.syncodec.graphite.presentation.common.dialog.dialog2.DeleteDialog
import com.syncodec.graphite.presentation.common.scaffold.GenericScaffold2
import com.syncodec.graphite.presentation.common.selectionAction.NotebookSelectionActionView
import com.syncodec.graphite.presentation.note.NoteActivity
import com.syncodec.graphite.presentation.notebook.composable.bar.BottomBar
import com.syncodec.graphite.presentation.notebook.composable.bar.COLLAPSED_TOP_BAR_HEIGHT
import com.syncodec.graphite.presentation.notebook.composable.bar.CollapsedTopBar
import com.syncodec.graphite.presentation.notebook.composable.bar.EXPANDED_TOP_BAR_HEIGHT
import com.syncodec.graphite.presentation.notebook.composable.bar.ExpandedTopBar
import com.syncodec.graphite.presentation.notebook.composable.bottomSheet.BottomSheet
import com.syncodec.graphite.presentation.notebook.composable.bottomSheet.NotebookBottomSheet
import com.syncodec.graphite.utils.Extra
import com.syncodec.graphite.utils.decodeBase64ToBitmap
import com.syncodec.graphite.utils.xor
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun NotebookScreen(
	chapterObject: ChapterObject? = null,
	chapterList: List<ChapterObject> = listOf(),
	noteList: List<NoteObjectLite> = listOf(),
	tagList: List<TagObject> = listOf(),
	chapterNoteItemCount: Map<RealmUUID?, Int> = mapOf(),
	chapterChapterItemCount: Map<RealmUUID?, Int> = mapOf(),
	chapterPath: List<ChapterObjectLite> = listOf(),
	defaultChapterId: RealmUUID? = null,
	onLoadChapter: (RealmUUID) -> Unit = {},
	onClickFavourite: (ChapterObject) -> Unit = {},
	onClickMultiFavourite: (Set<RealmUUID>) -> Unit = {},
	onClickLock: (ChapterObject) -> Unit = {},
	onClickMultiLock: (Set<RealmUUID>) -> Unit = {},
	onClickSetDefault: (RealmUUID) -> Unit = {},
	putChapter: (RealmUUID?, String, String, Color?, Bitmap?) -> Unit = { _, _, _, _, _ -> },
	onConfirmDelete: (Set<RealmUUID>) -> Unit = {},
) {
	val context = LocalContext.current
	val scope = rememberCoroutineScope()

	val density = LocalDensity.current

	var selectedTab by rememberSaveable { mutableIntStateOf(0) }
	val componentColumnCount = LocalComponentColumnCount.current

	val bitmap by remember(chapterObject?.thumbnail) { derivedStateOf { chapterObject?.thumbnail?.decodeBase64ToBitmap() } }

	val bottomSheetState = rememberModalBottomSheetState()
	val editChapterBottomSheetState = rememberModalBottomSheetState()
	var isMenuBottomSheetVisible by rememberSaveable { mutableStateOf(false) }
	var isMetadataBottomSheetVisible by rememberSaveable { mutableStateOf(false) }
	var isNewChapterBottomSheetVisible by rememberSaveable { mutableStateOf(false) }
	var isEditChapterBottomSheetVisible by rememberSaveable { mutableStateOf(false) }

	var isDeleteDialogVisible by rememberSaveable { mutableStateOf(false) }

	var isSelecting by rememberSaveable { mutableStateOf(false) }
	var selectedIdList: Set<RealmUUID> by rememberSaveable { mutableStateOf(setOf()) }

	fun onSelect(id: RealmUUID) {
		isSelecting = true
		selectedIdList.toMutableSet().apply {
			xor(id)
			selectedIdList = toSet()
		}
	}

	fun onClickNote(id: RealmUUID) {
		if (isSelecting) onSelect(id) else Intent(context, NoteActivity::class.java).apply {
			putExtra(Extra.Companion.Extra.IsNew.name, false)
			putExtra(Extra.Companion.Extra.NoteId.name, id.bytes)
			putExtra(Extra.Companion.Extra.Filter.name, Extra.Companion.Filter.SingleRead.name)
			context.startActivity(this)
		}
	}

	fun onClickChapter(id: RealmUUID) {
		if (isSelecting) onSelect(id) else onLoadChapter(id)
	}

	BackHandler(enabled = chapterPath.size > 1) { onLoadChapter(chapterPath[1].id) }
	BackHandler(enabled = isSelecting) { isSelecting = false; selectedIdList = setOf() }

	val staggeredGridState = rememberLazyStaggeredGridState()
	val overlapHeightPx = remember { with(density) { EXPANDED_TOP_BAR_HEIGHT.toPx() - COLLAPSED_TOP_BAR_HEIGHT.toPx() } }
	val isCollapsed: Boolean by remember { derivedStateOf { (staggeredGridState.firstVisibleItemScrollOffset > overlapHeightPx) || staggeredGridState.firstVisibleItemIndex > 0 } }

//	val firstVisibleItemScrollOffset by remember { derivedStateOf { listState.firstVisibleItemScrollOffset } }

	var noteScrollState by rememberSaveable { mutableStateOf(Pair(0, 0)) }
	var chapterScrollState by rememberSaveable { mutableStateOf(Pair(0, 0)) }
	fun onSelectTab(newTab: Int) {
		if (newTab == selectedTab) return
		else {
			scope.launch {
				if (newTab == 0) {
					chapterScrollState = Pair(staggeredGridState.firstVisibleItemIndex, staggeredGridState.firstVisibleItemScrollOffset)
					selectedTab = 0
					staggeredGridState.scrollToItem(noteScrollState.first, noteScrollState.second)
				} else {
					noteScrollState = Pair(staggeredGridState.firstVisibleItemIndex, staggeredGridState.firstVisibleItemScrollOffset)
					selectedTab = 1
					staggeredGridState.scrollToItem(chapterScrollState.first, chapterScrollState.second)
				}
			}
		}
	}

	GenericScaffold2(
		bottomBar = { BottomBar(onClickMetadata = { isMetadataBottomSheetVisible = true }) },
		isTopBarVisible = !isSelecting,
		isBottomBarVisible = !isSelecting,
		floatingActionButton = {
			FloatingActionButton(
				onClick = { isNewChapterBottomSheetVisible = true }
			) {
				Icon(
					painter = painterResource(id = R.drawable.ic_fa_notebook),
					contentDescription = stringResource(id = R.string.new_chapter),
					modifier = Modifier.requiredSize(16.dp)
				)
			}
			Spacer(modifier = Modifier.height(16.dp))
			ExtendedFloatingActionButton(
				text = { Text(text = stringResource(id = R.string.new_note)) },
				icon = {
					Icon(
						painter = painterResource(id = R.drawable.ic_fa_pen),
						contentDescription = stringResource(id = R.string.new_note),
						modifier = Modifier.requiredSize(16.dp)
					)
				},
				onClick = {
					Intent(context, NoteActivity::class.java).apply {
						putExtra(Extra.Companion.Extra.IsNew.name, true)
						putExtra(Extra.Companion.Extra.ParentId.name, chapterObject?.id?.bytes)
						putExtra(Extra.Companion.Extra.Filter.name, Extra.Companion.Filter.SingleRead.name)
						context.startActivity(this)
					}
				}
			)
		},
		isFloatingActionButtonVisible = !isSelecting,
		dialogContent = {
			DeleteDialog(
				isDialogVisible = isDeleteDialogVisible,
				onDismissRequest = { isDeleteDialogVisible = false },
				title = stringResource(id = R.string.delete_items_multiple),
				contentText = stringResource(id = R.string.are_you_sure_delete_multiple),
				onConfirmDelete = {
					onConfirmDelete(selectedIdList.toSet())
					isDeleteDialogVisible = false
					selectedIdList = setOf()
				},
			)
		},
		modifier = Modifier.fillMaxSize()
	) {
		CollapsedTopBar(
			modifier = Modifier.zIndex(2f),
			chapterTitle = chapterObject?.title,
			isFavourite = chapterObject?.isFavourite == true,
			isLocked = chapterObject?.isLocked == true,
			chapterColor = chapterObject?.color?.let { Color(it) },
			chapterPath = chapterPath,
			defaultChapterId = defaultChapterId,
			selectedTab = selectedTab,
			isCollapsed = isCollapsed,
			isSelecting = isSelecting,
			onClickFavourite = { chapterObject?.let(onClickFavourite) },
			onClickLock = { chapterObject?.let(onClickLock) },
			onClickMenuButton = { isMenuBottomSheetVisible = true },
			onLoadChapter = onLoadChapter,
			onSelectTab = ::onSelectTab
		)

		LazyVerticalStaggeredGrid(
			state = staggeredGridState,
			columns = StaggeredGridCells.Fixed(componentColumnCount),
			modifier = Modifier.fillMaxSize()
		) {
			item(
				key = 0,
				contentType = { 0 },
				span = StaggeredGridItemSpan.FullLine
			) {
				ExpandedTopBar(
					chapterTitle = chapterObject?.title,
					chapterDescription = chapterObject?.description,
					bitmap = bitmap,
					chapterColor = chapterObject?.color?.let { Color(it) },
					defaultChapterId = defaultChapterId,
					chapterPath = chapterPath,
					selectedTab = selectedTab,
					isSelecting = isSelecting,
					onLoadChapter = onLoadChapter,
					onSelectTab = ::onSelectTab
				)
			}

			item(
				key = 1,
				contentType = 1,
				span = StaggeredGridItemSpan.FullLine
			) {
				Spacer(modifier = Modifier.height(8.dp))
			}

			when {
				selectedTab == 0 && noteList.isEmpty() -> Unit
				selectedTab == 0 -> noteList(
					noteList = noteList,
					tagList = tagList,
					selectedIdList = selectedIdList,
					onClick = { onClickNote(it.id) },
					onLongClick = { onSelect(it.id) }
				)

				chapterList.isEmpty() -> Unit
				else -> chapterList(
					chapterList = chapterList,
					chapterNoteItemCount = chapterNoteItemCount,
					chapterChapterItemCount = chapterChapterItemCount,
					selectedIdList = selectedIdList,
					componentColumnCount = componentColumnCount,
					onClick = { onClickChapter(it.id) },
					onLongClick = { onSelect(it.id) }
				)

			}

			item(key = 3, contentType = 1) { Spacer(modifier = Modifier.height(128.dp)) }
		}

		NotebookSelectionActionView(
			modifier = Modifier
				.padding(start = 24.dp, top = 0.dp, end = 24.dp, bottom = 32.dp)
				.align(Alignment.BottomCenter),
			isSelecting = isSelecting,
			isAllItemFavourite = selectedIdList.isNotEmpty() && noteList.filter { it.id in selectedIdList }.all { it.isFavourite } && chapterList.filter { it.id in selectedIdList }.all { it.isFavourite },
			isAllItemLocked = selectedIdList.isNotEmpty() && noteList.filter { it.id in selectedIdList }.all { it.isLocked } && chapterList.filter { it.id in selectedIdList }.all { it.isLocked },
			selectedItemCount = selectedIdList.size,
			onClickDelete = { isDeleteDialogVisible = true },
			onClickFavourite = { onClickMultiFavourite(selectedIdList) },
			onClickLock = { onClickMultiLock(selectedIdList) },
		)
	}

	BottomSheet(
		bottomSheetState = bottomSheetState,
		editChapterBottomSheetState = editChapterBottomSheetState,
		isMenuBottomSheetVisible = isMenuBottomSheetVisible,
		isMetadataBottomSheetVisible = isMetadataBottomSheetVisible,
		isNewChapterBottomSheetVisible = isNewChapterBottomSheetVisible,
		isEditChapterBottomSheetVisible = isEditChapterBottomSheetVisible,
		chapterObject = chapterObject,
		defaultChapterId = defaultChapterId,
		putChapter = putChapter,
		onClickSetDefault = onClickSetDefault,
		onClickEditChapter = { isEditChapterBottomSheetVisible = true },
		onDismissRequest = { notebookBottomSheet ->
			when (notebookBottomSheet) {
				NotebookBottomSheet.Menu -> scope.launch { bottomSheetState.hide(); isMenuBottomSheetVisible = false }
				NotebookBottomSheet.Metadata -> scope.launch { bottomSheetState.hide(); isMetadataBottomSheetVisible = false }
				NotebookBottomSheet.NewChapter -> scope.launch { bottomSheetState.hide(); isNewChapterBottomSheetVisible = false }
				NotebookBottomSheet.EditChapter -> scope.launch { editChapterBottomSheetState.hide(); isEditChapterBottomSheetVisible = false }
			}
		},
	)
}
