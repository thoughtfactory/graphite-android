package com.syncodec.graphite.presentation.explorer.screen.searchScreen

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import com.syncodec.graphite.presentation.note.NoteActivity
import com.syncodec.graphite.presentation.explorer.screen.searchScreen.bar.BottomBar
import com.syncodec.graphite.presentation.explorer.screen.searchScreen.bar.TopBar
import com.syncodec.graphite.presentation.explorer.screen.searchScreen.buildingBlock.InitView
import com.syncodec.graphite.presentation.explorer.screen.searchScreen.buildingBlock.SearchResultView
import com.syncodec.graphite.presentation.explorer.screen.searchScreen.dialog.SearchDialog
import com.syncodec.graphite.presentation.explorer.screen.searchScreen.dialog.SearchDialogType
import com.syncodec.graphite.presentation.tags.composable.buildingBlock.EmptyView
import com.syncodec.graphite.utils.ContentStatus
import com.syncodec.graphite.utils.Extra
import io.realm.kotlin.types.RealmUUID
import org.koin.androidx.compose.koinViewModel


@OptIn(ExperimentalMaterialApi::class, ExperimentalAnimationApi::class)
@Composable
fun SearchScreen() {
	val context = LocalContext.current
	val viewModel : SearchScreenViewModel = koinViewModel()

	val contentStatus by viewModel.contentStatus.collectAsState()

	val searchFilterType by viewModel.searchFilterType.collectAsState()

	val tagList by viewModel.tagList.collectAsState()
	val noteList by viewModel.filteredNoteList.collectAsState()
	val searchInChapter by viewModel.searchInChapter.collectAsState()

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

				activityLauncher.launch(this)
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


	BackHandler(enabled = contentStatus != ContentStatus.Init) {
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
				ContentStatus.Init -> InitView(
					tagList = tagList,
					onClickFavorite = viewModel::filterFavourite,
					onClickWithAttachments = viewModel::filterWithAttachment,
					onClickLocked = viewModel::filterLocked,
					onClickTag = viewModel::filterTag,
				)

				ContentStatus.Error -> ErrorView()
				ContentStatus.Loading -> LoadingView()
				ContentStatus.LoadedEmpty -> EmptyView()
				ContentStatus.Loaded -> SearchResultView(
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
