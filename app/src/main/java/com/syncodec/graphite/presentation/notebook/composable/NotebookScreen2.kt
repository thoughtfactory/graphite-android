package com.syncodec.graphite.presentation.notebook.composable

import android.content.Intent
import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import com.syncodec.graphite.presentation.attachment.AttachmentActivity
import com.syncodec.graphite.presentation.common.bottomSheet.genericBottomSheet2.GenericBottomSheetInfo2
import com.syncodec.graphite.presentation.common.bottomSheet.genericBottomSheet2.composable.MetadataBottomSheet
import com.syncodec.graphite.presentation.common.scaffold.GenericScaffold2
import com.syncodec.graphite.presentation.common.selectionAction.NotebookSelectionActionView
import com.syncodec.graphite.presentation.explorer.ExplorerActivity
import com.syncodec.graphite.presentation.main.composable.bottomSheet.ChapterBottomSheet
import com.syncodec.graphite.presentation.note.NoteActivity
import com.syncodec.graphite.presentation.note2.NoteActivity2
import com.syncodec.graphite.presentation.notebook.composable.bar.BottomBar
import com.syncodec.graphite.presentation.notebook.composable.bar.COLLAPSED_TOP_BAR_HEIGHT
import com.syncodec.graphite.presentation.notebook.composable.bar.CollapsedTopBar
import com.syncodec.graphite.presentation.notebook.composable.bar.EXPANDED_TOP_BAR_HEIGHT
import com.syncodec.graphite.presentation.notebook.composable.bar.ExpandedTopBar
import com.syncodec.graphite.presentation.notebook.composable.bottomSheet.MenuBottomSheet
import com.syncodec.graphite.presentation.notebook.screen.buildingBlock.chapterList
import com.syncodec.graphite.presentation.notebook.screen.buildingBlock.noteList
import com.syncodec.graphite.utils.Extra
import com.syncodec.graphite.utils.decodeBase64ToBitmap
import com.syncodec.graphite.utils.getInverseBWColor
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
	chapterPath: List<ChapterObjectLite> = listOf(),
	defaultChapterId: RealmUUID? = null,
	onLoadChapter: (RealmUUID) -> Unit = {},
	onClickFavourite: (ChapterObject) -> Unit = {},
	onClickMultiFavourite: (Set<RealmUUID>) -> Unit = {},
	onClickLock: (ChapterObject) -> Unit = {},
	onClickMultiLock: (Set<RealmUUID>) -> Unit = {},
	putNotebook: (String, String, Color?, Bitmap?) -> Unit = { _, _, _, _ -> },
) {
	val context = LocalContext.current
	val scope = rememberCoroutineScope()

	val density = LocalDensity.current

	val bitmap by remember(chapterObject) { derivedStateOf { chapterObject?.thumbnail?.decodeBase64ToBitmap() } }

	val bottomSheetState = rememberModalBottomSheetState()
	var isMenuBottomSheetVisible by remember { mutableStateOf(false) }
	var isMetadataBottomSheetVisibe by remember { mutableStateOf(false) }
	var isChapterBottomSheetVisible by remember { mutableStateOf(false) }

//	null : selecting nothing
//	true : selecting notes
//	false : selecting chapters
	var selectionType by remember { mutableStateOf<Boolean?>(null) }
	var selectedIdList: Set<RealmUUID> by remember { mutableStateOf(setOf()) }

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
			null -> Intent(context, NoteActivity::class.java).apply {
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

	GenericScaffold2(
		bottomBar = { BottomBar(onClickMetadata = { isMetadataBottomSheetVisibe = true }) },
		isTopBarVisible = selectionType == null,
		isBottomBarVisible = selectionType == null,
		floatingActionButton = {
			FloatingActionButton(
				onClick = { isChapterBottomSheetVisible = true }
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
			isCollapsed = isCollapsed,
			onContainerColor = bitmap?.let { Color.White } ?: chapterObject?.color?.let { Color(it) }?.getInverseBWColor() ?: MaterialTheme.colorScheme.onBackground,
			onClickFavourite = { chapterObject?.let(onClickFavourite) },
			onClickLock = { chapterObject?.let(onClickLock) },
			onClickMenuButton = { isMenuBottomSheetVisible = true },
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
				)
			}
			noteList(
				noteList = noteList,
				tagList = listOf(),
				selectedIdList = selectedIdList,
				isVisible = true,
				toggleVisibility = {},
				onClick = { onClickNote(it.id) },
				onLongClick = { selectionType = true; onSelect(it.id) },
			)
			chapterList(
				chapterList = chapterList,
//				chapterNoteItemCount = chapterNoteItemCount,
//				chapterChapterItemCount = chapterChapterItemCount,
//				tagList = tagList,
				selectedIdList = selectedIdList,
//				isVisible = isChapterListVisible,
//				toggleVisibility = onToggleChapterVisibility,
				onClick = { onClickChapter(it.id) },
				onLongClick = { selectionType = false; onSelect(it.id) }
			)

			item { Spacer(modifier = Modifier.height(32.dp)) }
		}

		NotebookSelectionActionView(
			modifier = Modifier
				.padding(start = 24.dp, top = 0.dp, end = 24.dp, bottom = 32.dp)
				.align(Alignment.BottomCenter),
			isSelecting = selectionType != null,
			isAllItemFavourite = noteList.filter { it.id in selectedIdList }.all { it.isFavourite } && chapterList.filter { it.id in selectedIdList }.all { it.isFavourite },
			isAllItemLocked = noteList.filter { it.id in selectedIdList }.all { it.isLocked } && chapterList.filter { it.id in selectedIdList }.all { it.isLocked },
			selectedItemCount = selectedIdList.size,
			onClickDelete = {},
			onClickFavourite = { onClickMultiFavourite(selectedIdList) },
			onClickLock = { onClickMultiLock(selectedIdList) }
		)
	}

	MenuBottomSheet(
		bottomSheetState = bottomSheetState,
		isBottomSheetVisible = isMenuBottomSheetVisible,
		onDismissRequest = { scope.launch { bottomSheetState.hide(); isMenuBottomSheetVisible = false } },
		isChapterDefault = chapterObject?.id == defaultChapterId,
		onClickAttachments = {
			Intent(context, AttachmentActivity::class.java).apply {
				putExtra(Extra.Companion.Extra.ChapterId.name, chapterObject?.id?.bytes)
				context.startActivity(this)
			}
		},
		onClickAtlas = {
			Intent(context, ExplorerActivity::class.java).apply {
				putExtra(Extra.Companion.Extra.ExplorerType.name, Extra.Companion.ExplorerType.Atlas.name)
				putExtra(Extra.Companion.Extra.ChapterId.name, chapterObject?.id?.bytes)
				context.startActivity(this)
			}
		},
		onClickCalendar = {
			Intent(context, ExplorerActivity::class.java).apply {
				putExtra(Extra.Companion.Extra.ExplorerType.name, Extra.Companion.ExplorerType.Calendar.name)
				putExtra(Extra.Companion.Extra.ChapterId.name, chapterObject?.id?.bytes)
				context.startActivity(this)
			}
		},
		onClickSetAsDefault = {},
		onClickEdit = {},
		onClickDelete = {},
	)

	MetadataBottomSheet(
		bottomSheetState = bottomSheetState,
		isBottomSheetVisible = isMetadataBottomSheetVisibe,
		onDismissRequest = { scope.launch { bottomSheetState.hide(); isMetadataBottomSheetVisibe = false } },
		id = chapterObject?.id,
		createdTimestamp = chapterObject?.createdTimestamp,
		modifiedTimestamp = chapterObject?.modifiedTimestamp,
		extraContent = {
			GenericBottomSheetInfo2(
				key = stringResource(id = R.string.parent_id),
				value = chapterObject?.parentId?.toString() ?: stringResource(id = R.string.root_element),
			)
		}
	)

	ChapterBottomSheet(
		bottomSheetState = bottomSheetState,
		isBottomSheetVisible = isChapterBottomSheetVisible,
		onDismissRequest = { scope.launch { bottomSheetState.hide(); isChapterBottomSheetVisible = false } },
		title = stringResource(id = R.string.new_chapter),
		putNotebook = putNotebook,
	)
}
