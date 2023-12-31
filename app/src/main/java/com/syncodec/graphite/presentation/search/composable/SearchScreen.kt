package com.syncodec.graphite.presentation.search.composable

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.local.ChapterObjectLite
import com.syncodec.graphite.di.model.local.NoteObjectLite
import com.syncodec.graphite.di.model.local.TagObject
import com.syncodec.graphite.di.repository.AttachmentRepository.Companion.getAttachmentCountFromNoteId
import com.syncodec.graphite.presentation.common.component.note.noteList
import com.syncodec.graphite.presentation.common.dialog.dialog2.DeleteDialog
import com.syncodec.graphite.presentation.common.dialog.where.whereChapterDialog2.WhereChapterDialog2
import com.syncodec.graphite.presentation.common.scaffold.GenericScaffold2
import com.syncodec.graphite.presentation.common.selectionAction.SearchSelectionActionView
import com.syncodec.graphite.presentation.note.NoteActivity
import com.syncodec.graphite.presentation.search.SearchViewModel
import com.syncodec.graphite.presentation.search.composable.bar.TopBar
import com.syncodec.graphite.presentation.search.composable.buildingBlock.SearchWhatView
import com.syncodec.graphite.utils.Extra
import com.syncodec.graphite.utils.xor
import io.realm.kotlin.types.RealmUUID


@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun SearchScreen(
	currentChapter : ChapterObjectLite? = null,
	tagList: List<TagObject> = listOf(),
	noteList: List<NoteObjectLite> = listOf(),
	currentFilterList: Set<SearchViewModel.Companion.NoteFilter> = setOf(),
	onExploreChapter: (RealmUUID?) -> Unit = {},
	onAddFilter: (SearchViewModel.Companion.NoteFilter) -> Unit = {},
	onRemoveFilter: (SearchViewModel.Companion.NoteFilter) -> Unit = {},
	onClickMultiFavourite: (Set<RealmUUID>) -> Unit = {},
	onClickMultiLock: (Set<RealmUUID>) -> Unit = {},
	onConfirmDelete: (Set<RealmUUID>) -> Unit = {},
) {
	val context = LocalContext.current

	var isSelecting by remember { mutableStateOf(false) }
	var selectedIdList: Set<RealmUUID> by remember { mutableStateOf(setOf()) }
	fun onSelect(id: RealmUUID) {
		if (!isSelecting) isSelecting = true
		selectedIdList.toMutableSet().apply {
			xor(id)
			selectedIdList = toSet()
		}
	}

	BackHandler(enabled = isSelecting) {
		isSelecting = false
		selectedIdList = setOf()
	}

	var isWhereDialogVisible by rememberSaveable { mutableStateOf(false) }
	var isDeleteDialogVisible by rememberSaveable { mutableStateOf(false) }

	GenericScaffold2(
		topBar = {
			TopBar(
				currentFilterList = currentFilterList,
				onAddFilter = onAddFilter,
				onRemoveFilter = onRemoveFilter,
			)
		},
		bottomBar = {
			com.syncodec.graphite.presentation.explorer.composable.bar.BottomBar(
				currentChapter = currentChapter,
				onClickSearchIn = { isWhereDialogVisible = true },
			)
		},
		isTopBarVisible = !isSelecting,
		isBottomBarVisible = !isSelecting,
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
			WhereChapterDialog2(
				isDialogVisible = isWhereDialogVisible,
				onDismissRequest = { isWhereDialogVisible = false },
				currentSelectedChapter = currentChapter?.id,
				onSelectChapter = onExploreChapter,
			)
		},
	) {
		Crossfade(
			targetState = Pair(currentFilterList, noteList),
			label = "searchWhat_animation"
		) { (filterList, noteList1) ->
			if (filterList.isEmpty()) {
				SearchWhatView(
					tagList = tagList,
					onAddFilter = onAddFilter
				)
			} else {
				LazyColumn(
					modifier = Modifier.fillMaxSize(),
				) {
					item { Spacer(modifier = Modifier.height(8.dp)) }
					noteList(
						noteList = noteList1.filter { if (SearchViewModel.Companion.NoteFilter.WithAttachment in filterList) context.getAttachmentCountFromNoteId(parentId = it.id) > 0 else true },
						tagList = tagList,
						selectedIdList = selectedIdList,
						onClick = {
							if (isSelecting) {
								onSelect(it.id)
							} else {
								Intent(context, NoteActivity::class.java).apply {
									putExtra(Extra.Companion.Extra.IsNew.name, false)
									putExtra(Extra.Companion.Extra.NoteId.name, it.id.bytes)
									putExtra(Extra.Companion.Extra.Filter.name, Extra.Companion.Filter.SingleRead.name)
									context.startActivity(this)
								}
							}
						},
						onLongClick = { onSelect(it.id) },
					)
					item { Spacer(modifier = Modifier.height(128.dp)) }
				}
			}
		}

		SearchSelectionActionView(
			modifier = Modifier
				.align(Alignment.BottomCenter)
				.padding(start = 24.dp, end = 24.dp, bottom = 24.dp),
			isSelecting = isSelecting,
			isAllItemFavourite = selectedIdList.isNotEmpty() && noteList.filter { it.id in selectedIdList }.all { it.isFavourite },
			isAllItemLocked = selectedIdList.isNotEmpty() && noteList.filter { it.id in selectedIdList }.all { it.isLocked },
			selectedItemCount = selectedIdList.size,
			onClickDelete = { isDeleteDialogVisible = true },
			onClickFavourite = { onClickMultiFavourite(selectedIdList) },
			onClickLock = { onClickMultiLock(selectedIdList) }
		)
	}
}
