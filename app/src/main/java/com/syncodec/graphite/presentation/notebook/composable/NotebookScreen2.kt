package com.syncodec.graphite.presentation.notebook.composable

import android.content.Intent
import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
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
import com.syncodec.graphite.di.model.ChapterObject
import com.syncodec.graphite.di.model.ChapterObjectLite
import com.syncodec.graphite.di.model.NoteObjectLite
import com.syncodec.graphite.di.model.TagObject
import com.syncodec.graphite.presentation.common.component.chapter.ChapterListCard
import com.syncodec.graphite.presentation.common.component.note.NoteListCard2
import com.syncodec.graphite.presentation.common.dialog.dialog2.DeleteDialog
import com.syncodec.graphite.presentation.common.scaffold.GenericScaffold2
import com.syncodec.graphite.presentation.common.selectionAction.NotebookSelectionActionView
import com.syncodec.graphite.presentation.note2.NoteActivity2
import com.syncodec.graphite.presentation.notebook.composable.bar.BottomBar
import com.syncodec.graphite.presentation.notebook.composable.bar.COLLAPSED_TOP_BAR_HEIGHT
import com.syncodec.graphite.presentation.notebook.composable.bar.CollapsedTopBar
import com.syncodec.graphite.presentation.notebook.composable.bar.EXPANDED_TOP_BAR_HEIGHT
import com.syncodec.graphite.presentation.notebook.composable.bar.ExpandedTopBar
import com.syncodec.graphite.presentation.notebook.composable.bottomSheet.BottomSheet
import com.syncodec.graphite.presentation.notebook.composable.bottomSheet.NotebookBottomSheet
import com.syncodec.graphite.utils.Extra
import com.syncodec.graphite.utils.decodeBase64ToBitmap
import com.syncodec.graphite.utils.getInverseBWColor
import com.syncodec.graphite.utils.timeStampToPrettyFull
import com.syncodec.graphite.utils.xor
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun NotebookScreen2(
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

	val bitmap by remember(chapterObject) { derivedStateOf { chapterObject?.thumbnail?.decodeBase64ToBitmap() } }

	val bottomSheetState = rememberModalBottomSheetState()
	val editChapterBottomSheetState = rememberModalBottomSheetState()
	var isMenuBottomSheetVisible by rememberSaveable { mutableStateOf(false) }
	var isMetadataBottomSheetVisible by rememberSaveable { mutableStateOf(false) }
	var isNewChapterBottomSheetVisible by rememberSaveable { mutableStateOf(false) }
	var isEditChapterBottomSheetVisible by rememberSaveable { mutableStateOf(false) }

	var isDeleteDialogVisible by rememberSaveable { mutableStateOf(false) }

//	null : selecting nothing
//	true : selecting notes
//	false : selecting chapters
	var selectionType by rememberSaveable { mutableStateOf<Boolean?>(null) }
	var selectedIdList: Set<RealmUUID> by rememberSaveable { mutableStateOf(setOf()) }

	fun onSelect(id: RealmUUID) {
		selectedIdList.toMutableSet().apply {
			xor(id)
			selectedIdList = toSet()
		}
	}

	fun onClickNote(id: RealmUUID) {
		when (selectionType) {
			true -> onSelect(id = id)
			false -> Unit
			null -> Intent(context, NoteActivity2::class.java).apply {
				putExtra(Extra.Companion.Extra.IsNew.name, false)
				putExtra(Extra.Companion.Extra.NoteId.name, id.bytes)
				putExtra(Extra.Companion.Extra.Filter.name, Extra.Companion.Filter.SingleRead.name)
				context.startActivity(this)
			}
		}
	}

	fun onClickChapter(id: RealmUUID) {
		when (selectionType) {
			true -> onSelect(id = id)
			false -> Unit
			null -> onLoadChapter(id)
		}
	}

	BackHandler(enabled = chapterPath.size > 1) { onLoadChapter(chapterPath[1].id) }
	BackHandler(enabled = selectionType != null) { selectionType = null; selectedIdList = setOf() }

	val listState = rememberLazyListState()
	val overlapHeightPx = remember { with(density) { EXPANDED_TOP_BAR_HEIGHT.toPx() - COLLAPSED_TOP_BAR_HEIGHT.toPx() } }
	val isCollapsed: Boolean by remember { derivedStateOf { (listState.firstVisibleItemScrollOffset > overlapHeightPx) || listState.firstVisibleItemIndex > 0 } }

//	val firstVisibleItemScrollOffset by remember { derivedStateOf { listState.firstVisibleItemScrollOffset } }

	var noteScrollState by rememberSaveable { mutableStateOf(Pair(0, 0)) }
	var chapterScrollState by rememberSaveable { mutableStateOf(Pair(0, 0)) }
	fun onSelectTab(newTab: Int) {
		if (newTab == selectedTab) return
		else {
			scope.launch {
				if (newTab == 0) {
					chapterScrollState = Pair(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset)
					selectedTab = 0
					listState.scrollToItem(noteScrollState.first, noteScrollState.second)
				} else {
					noteScrollState = Pair(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset)
					selectedTab = 1
					listState.scrollToItem(chapterScrollState.first, chapterScrollState.second)
				}
			}
		}
	}

	GenericScaffold2(
		bottomBar = { BottomBar(onClickMetadata = { isMetadataBottomSheetVisible = true }) },
		isTopBarVisible = selectionType == null,
		isBottomBarVisible = selectionType == null,
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
					Intent(context, NoteActivity2::class.java).apply {
						putExtra(Extra.Companion.Extra.IsNew.name, true)
						putExtra(Extra.Companion.Extra.ParentId.name, chapterObject?.id?.bytes)
						putExtra(Extra.Companion.Extra.Filter.name, Extra.Companion.Filter.SingleRead.name)
						context.startActivity(this)
					}
				}
			)
		},
		isFloatingActionButtonVisible = selectionType == null,
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
			isSelecting = selectionType != null,
			onContainerColor = bitmap?.let { Color.White } ?: chapterObject?.color?.let { Color(it) }?.getInverseBWColor() ?: MaterialTheme.colorScheme.onBackground,
			onClickFavourite = { chapterObject?.let(onClickFavourite) },
			onClickLock = { chapterObject?.let(onClickLock) },
			onClickMenuButton = { isMenuBottomSheetVisible = true },
			onLoadChapter = onLoadChapter,
			onSelectTab = ::onSelectTab
		)

		LazyColumn(
			state = listState,
			modifier = Modifier.fillMaxSize()
		) {
			item {
				ExpandedTopBar(
					chapterTitle = chapterObject?.title,
					bitmap = bitmap,
					containerColor = chapterObject?.color?.let { Color(it) },
					defaultChapterId = defaultChapterId,
					chapterPath = chapterPath,
					selectedTab = selectedTab,
					isSelecting = selectionType != null,
					onLoadChapter = onLoadChapter,
					onSelectTab = ::onSelectTab
				)
			}
			when {
				selectedTab == 0 && noteList.isEmpty() -> Unit
				selectedTab == 0 -> noteList(
					noteList = noteList.toSet(),
					tagList = tagList,
					selectedIdList = selectedIdList,
					onClick = { onClickNote(it.id) },
					onLongClick = { selectionType = true; onSelect(it.id) }
				)

				chapterList.isEmpty() -> Unit
				else -> chapterList(
					chapterList = chapterList,
					chapterNoteItemCount = chapterNoteItemCount,
					chapterChapterItemCount = chapterChapterItemCount,
					selectedIdList = selectedIdList,
					onClick = { onClickChapter(it.id) },
					onLongClick = { selectionType = false; onSelect(it.id) }
				)

			}

			item { Spacer(modifier = Modifier.height(128.dp)) }
		}

		NotebookSelectionActionView(
			modifier = Modifier
				.padding(start = 24.dp, top = 0.dp, end = 24.dp, bottom = 32.dp)
				.align(Alignment.BottomCenter),
			isSelecting = selectionType != null,
			isAllItemFavourite = selectedIdList.isNotEmpty() && noteList.filter { it.id in selectedIdList }.all { it.isFavourite } && chapterList.filter { it.id in selectedIdList }.all { it.isFavourite },
			isAllItemLocked = selectedIdList.isNotEmpty() && noteList.filter { it.id in selectedIdList }.all { it.isLocked } && chapterList.filter { it.id in selectedIdList }.all { it.isLocked },
			selectedItemCount = selectedIdList.size,
			onClickDelete = { isDeleteDialogVisible = true },
			onClickFavourite = { onClickMultiFavourite(selectedIdList) },
			onClickLock = { onClickMultiLock(selectedIdList) }
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
}

@OptIn(ExperimentalFoundationApi::class)
fun LazyListScope.noteList(
	noteList: Set<NoteObjectLite> = setOf(),
	tagList: List<TagObject> = listOf(),
	selectedIdList: Set<RealmUUID> = setOf(),
	onClick: (NoteObjectLite) -> Unit = {},
	onLongClick: (NoteObjectLite) -> Unit = {},
) {
	item { Spacer(modifier = Modifier.height(8.dp)) }

	noteList.forEach { note ->
		item(
			key = note.id.toString(),
			contentType = { NoteObjectLite::class }
		) {
			Box(
				modifier = Modifier.animateItemPlacement(tween(470))
			) {
				NoteListCard2(
					id = note.id,
					timestamp = note.userTimestamp.timeStampToPrettyFull(),
					title = note.title,
					isFavourite = note.isFavourite,
					isLocked = note.isLocked,
					contentThumbnail = note.contentThumbnail,
//					thumbnail = note.thumbnail,
					address = note.address,
					latLng = note.latLng,
					tagList = tagList.filter { note.id in it.objectIdList }.map { it.toLite() },
					selected = note.id in selectedIdList,
					onClick = { onClick(note) },
					onLongClick = { onLongClick(note) },
				)
			}
		}
	}
}

@OptIn(ExperimentalFoundationApi::class)
fun LazyListScope.chapterList(
	chapterList: List<ChapterObject> = listOf(),
	chapterNoteItemCount: Map<RealmUUID?, Int> = mapOf(),
	chapterChapterItemCount: Map<RealmUUID?, Int> = mapOf(),
	selectedIdList: Set<RealmUUID> = setOf(),
	onClick: (ChapterObject) -> Unit = {},
	onLongClick: (ChapterObject) -> Unit = {},
) {
	item { Spacer(modifier = Modifier.height(8.dp)) }

	chapterList
		.forEach { chapterObject ->
			item(
				key = chapterObject.id.toString(),
				contentType = { ChapterObject::class }
			) {
				Box(
					modifier = Modifier.animateItemPlacement(tween(470))
				) {
					ChapterListCard(
						id = chapterObject.id,
						createdTimestamp = chapterObject.createdTimestamp,
						modifiedTimestamp = chapterObject.modifiedTimestamp,
						title = chapterObject.title,
						description = chapterObject.description,
						isFavourite = chapterObject.isFavourite,
						isLocked = chapterObject.isLocked,
						color = chapterObject.color?.let { Color(it) },
						thumbnail = chapterObject.thumbnail,
						noteCount = chapterNoteItemCount[chapterObject.id] ?: 0,
						chapterCount = chapterChapterItemCount[chapterObject.id] ?: 0,
						isSelected = chapterObject.id in selectedIdList,
						onClick = { onClick(chapterObject) },
						onLongClick = { onLongClick(chapterObject) },
					)
				}
			}
		}

	item { Spacer(modifier = Modifier.height(96.dp)) }
}


