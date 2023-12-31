package com.syncodec.graphite.presentation.notebook.composable

import android.content.Intent
import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
import com.syncodec.graphite.di.repository.group.RealmObjectGroupList
import com.syncodec.graphite.presentation.base.sortOn
import com.syncodec.graphite.presentation.common.component.chapter.chapterList
import com.syncodec.graphite.presentation.common.component.note.noteList
import com.syncodec.graphite.presentation.common.dialog.dialog2.DeleteDialog
import com.syncodec.graphite.presentation.common.scaffold.GenericScaffold2
import com.syncodec.graphite.presentation.common.selectionAction.NotebookSelectionActionView
import com.syncodec.graphite.presentation.main.composable.buildingBlock.EmptyView
import com.syncodec.graphite.presentation.note.NoteActivity
import com.syncodec.graphite.presentation.notebook.composable.bar.BottomBar
import com.syncodec.graphite.presentation.notebook.composable.bar.TopBar
import com.syncodec.graphite.presentation.notebook.composable.bottomSheet.BottomSheet
import com.syncodec.graphite.presentation.notebook.composable.bottomSheet.NotebookBottomSheet
import com.syncodec.graphite.presentation.notebook.composable.buildingBlock.Backdrop
import com.syncodec.graphite.presentation.notebook.composable.buildingBlock.ChapterNavigator
import com.syncodec.graphite.utils.Extra
import com.syncodec.graphite.utils.SortOn
import com.syncodec.graphite.utils.xor
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Preview
@Composable
fun NotebookScreen(
	chapterObject: ChapterObject? = null,
	chapterList: List<ChapterObject> = listOf(),
	noteGroupList: RealmObjectGroupList<NoteObjectLite> = RealmObjectGroupList(),
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

	val bottomSheetState = rememberModalBottomSheetState()
	val editChapterBottomSheetState = rememberModalBottomSheetState()
	var isMenuBottomSheetVisible by rememberSaveable { mutableStateOf(false) }
	var isMetadataBottomSheetVisible by rememberSaveable { mutableStateOf(false) }
	var isNewChapterBottomSheetVisible by rememberSaveable { mutableStateOf(false) }
	var isEditChapterBottomSheetVisible by rememberSaveable { mutableStateOf(false) }

	val sortOn by sortOn()

	var isChapterListVisible by remember { mutableStateOf(true) }

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

	val lazyListState = rememberLazyListState()
	val firstItemIndex by remember { derivedStateOf { lazyListState.firstVisibleItemIndex } }
	val firstItemScrollOffset by remember { derivedStateOf { lazyListState.firstVisibleItemScrollOffset } }
	val firstItemHeight by remember { derivedStateOf { maxOf((lazyListState.layoutInfo.visibleItemsInfo.firstOrNull()?.size ?: 0), 1) } }
	val offset by remember { derivedStateOf { if (firstItemIndex > 0) 1f else (firstItemScrollOffset.toFloat() / firstItemHeight).coerceIn(0f, 1f) } }

	BackHandler(enabled = chapterPath.size > 1) { onLoadChapter(chapterPath[1].id) }
	BackHandler(enabled = isSelecting) { isSelecting = false; selectedIdList = setOf() }

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
		Column {
			Backdrop(
				id = chapterObject?.id,
				chapterTitle = chapterObject?.title,
				chapterDescription = chapterObject?.description,
				thumbnail = chapterObject?.thumbnail,
				chapterColor = chapterObject?.color?.let { Color(it) },
				isSelecting = isSelecting,
				offset = if (firstItemIndex > 0) 1f else offset,
			)
			Spacer(
				modifier = Modifier
					.fillMaxWidth()
					.height(32.dp)
					.background(MaterialTheme.colorScheme.background)
			)
		}

		Column(
			modifier = Modifier.fillMaxSize()
		) {
			TopBar(
				modifier = Modifier.zIndex(2f),
				chapterTitle = chapterObject?.title,
				isFavourite = chapterObject?.isFavourite == true,
				isLocked = chapterObject?.isLocked == true,
				offset = offset,
				chapterColor = chapterObject?.color?.let { Color(it) },
				onClickFavourite = { chapterObject?.let(onClickFavourite) },
				onClickLock = { chapterObject?.let(onClickLock) },
			) { isMenuBottomSheetVisible = true }

			if (noteGroupList.isEmpty() && chapterList.isEmpty()) {
				Spacer(modifier = Modifier.height(196.dp))
				EmptyView(
					image = R.drawable.il_chapter_empty,
					title = stringResource(id = R.string.empty_chapter_message),
					modifier = Modifier
						.fillMaxWidth()
						.weight(1f)
				)
				Spacer(modifier = Modifier.height(32.dp))
			} else {
				LazyColumn(
					state = lazyListState,
					modifier = Modifier.fillMaxSize(),
				) {
					item(
						contentType = { 0 },
					) {
						Spacer(modifier = Modifier.height(196.dp))
					}
					stickyHeader(
						contentType = { 1 },
					) {
						ChapterNavigator(
							defaultChapterId = defaultChapterId,
							chapterPath = chapterPath,
							isChapterListVisible = isChapterListVisible,
							onLoadChapter = onLoadChapter,
							onToggleChapterList = { isChapterListVisible = !isChapterListVisible },
						)
					}

					chapterList(
						chapterList = if (isChapterListVisible) chapterList else listOf(),
						chapterNoteItemCount = chapterNoteItemCount,
						chapterChapterItemCount = chapterChapterItemCount,
						selectedIdList = selectedIdList,
						componentColumnCount = 2,
						onClick = { onClickChapter(it.id) },
						onLongClick = { onSelect(it.id) },
					)

					noteList(
						noteGroupList = noteGroupList,
						tagList = tagList,
						sortOn = sortOn ?: SortOn.Timestamp,
						selectedIdList = selectedIdList,
						onClick = { onClickNote(it.id) },
						onLongClick = { onSelect(it.id) },
					)

					item(
						contentType = { 0 },
					) {
						Spacer(modifier = Modifier.height(194.dp))
					}
				}
			}
		}

		NotebookSelectionActionView(
			modifier = Modifier
				.padding(start = 24.dp, top = 0.dp, end = 24.dp, bottom = 32.dp)
				.align(Alignment.BottomCenter),
			isSelecting = isSelecting,
			isAllItemFavourite = selectedIdList.isNotEmpty() && noteGroupList.filterObject { it.id in selectedIdList }.all { it.isFavourite } && chapterList.filter { it.id in selectedIdList }.all { it.isFavourite },
			isAllItemLocked = selectedIdList.isNotEmpty() && noteGroupList.filterObject { it.id in selectedIdList }.all { it.isLocked } && chapterList.filter { it.id in selectedIdList }.all { it.isLocked },
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
