package com.syncodec.graphite.presentation.explorer.screen.searchScreen

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.syncodec.graphite.presentation.common.ErrorView
import com.syncodec.graphite.presentation.common.LoadingView
import com.syncodec.graphite.presentation.common.scaffold.GenericScaffold
import com.syncodec.graphite.presentation.explorer.screen.searchScreen.bar.BottomBar
import com.syncodec.graphite.presentation.explorer.screen.searchScreen.bar.TopBar
import com.syncodec.graphite.presentation.explorer.screen.searchScreen.buildingBlock.InitView
import com.syncodec.graphite.presentation.explorer.screen.searchScreen.buildingBlock.SearchResultView
import com.syncodec.graphite.presentation.explorer.screen.searchScreen.dialog.SearchDialog
import com.syncodec.graphite.presentation.explorer.screen.searchScreen.dialog.SearchDialogType
import com.syncodec.graphite.presentation.note.NoteActivity
import com.syncodec.graphite.presentation.tags.composable.buildingBlock.EmptyView
import com.syncodec.graphite.utils.Extra
import com.syncodec.graphite.utils.LoaderStatus
import io.realm.kotlin.types.RealmUUID
import org.koin.androidx.compose.koinViewModel


@OptIn(ExperimentalMaterialApi::class, ExperimentalAnimationApi::class)
@Composable
fun SearchScreen() {
	val context = LocalContext.current
	val viewModel : SearchScreenViewModel = koinViewModel()

	val contentStatus by viewModel.loaderStatus.collectAsState()

	val searchFilterType by viewModel.searchFilterType.collectAsState()

	val tagList by viewModel.tagList.collectAsState()
	val noteList by viewModel.filteredNoteList.collectAsState()
	val searchInChapter by viewModel.searchInChapter.collectAsState()

	var isSelecting by remember { mutableStateOf(false) }
	var selectedIdList : List<RealmUUID> by remember { mutableStateOf(listOf()) }
	fun onSelect(id : RealmUUID) {
		if (! isSelecting) isSelecting = true
		selectedIdList.toMutableList().apply {
			if (id in selectedIdList) remove(id) else add(id)
			selectedIdList = this
		}
	}

	fun onClickNote(id : RealmUUID) {
		if (isSelecting) {
			onSelect(id)
		} else {
			Intent(context, NoteActivity::class.java).apply {
				putExtra(Extra.Companion.Extra.IsNew.name, false)
				putExtra(Extra.Companion.Extra.NoteId.name, id.bytes)
				putExtra(Extra.Companion.Extra.Filter.name, Extra.Companion.Filter.SingleRead.name)
				context.startActivity(this)
			}
		}
	}

	var isWhereDialogVisible by remember { mutableStateOf(false) }
	var isDeleteDialogVisible by remember { mutableStateOf(false) }
	fun openDialog(dialogType : SearchDialogType) = when (dialogType) {
		SearchDialogType.Where -> isWhereDialogVisible = true
		SearchDialogType.Delete -> isDeleteDialogVisible = true
	}

	fun closeDialog(dialogType : SearchDialogType) = when (dialogType) {
		SearchDialogType.Where -> isWhereDialogVisible = false
		SearchDialogType.Delete -> isDeleteDialogVisible = false
	}


	BackHandler(enabled = contentStatus != LoaderStatus.Init) {
		viewModel.clearFilter()
	}

	BackHandler(enabled = isSelecting) {
		isSelecting = false
		selectedIdList = listOf()
	}

	GenericScaffold(
		topBar = {
			TopBar(
				searchFilterType = searchFilterType,
				isSelecting = isSelecting,
				selectedSize = selectedIdList.size,
				onClickCancelSelect = {
					isSelecting = false
					selectedIdList = listOf()
				},
				onClickSearch = {
					if (it.isNotEmpty()) viewModel.filterQuery(it)
					else Toast.makeText(context, "Please enter a word to search", Toast.LENGTH_SHORT).show()
				},
				onClickDelete = { openDialog(SearchDialogType.Delete) },
			)
		},
		bottomBar = { BottomBar(searchInChapter = searchInChapter) { openDialog(SearchDialogType.Where) } },
		dialogContent = {
			SearchDialog(
				isWhereDialogVisible = isWhereDialogVisible,
				isDeleteDialogVisible = isDeleteDialogVisible,
				searchInChapter = searchInChapter,
				onSetSearchInChapter = viewModel::setSearchInChapter,
				onDelete = { viewModel.delete(idList = selectedIdList.toList()); isSelecting = false; selectedIdList = listOf() },
				closeDialog = ::closeDialog
			)
		}
	) {
		AnimatedContent(targetState = contentStatus) {
			when (it) {
				LoaderStatus.Init -> InitView(
					tagList = tagList,
					onClickFavorite = viewModel::filterFavourite,
					onClickWithAttachments = viewModel::filterWithAttachment,
					onClickLocked = viewModel::filterLocked,
					onClickTag = viewModel::filterTag,
				)

				LoaderStatus.Error -> ErrorView()
				LoaderStatus.Loading -> LoadingView()
				LoaderStatus.LoadedEmpty -> EmptyView()
				LoaderStatus.Loaded -> SearchResultView(
					noteList = noteList,
					tagList = tagList,
					selectedIdList = selectedIdList,
					onClickNote = ::onClickNote,
					onLongClickNote = ::onSelect,
				)
			}
		}
	}
}
