package com.syncodec.graphite.presentation.explorer.screen.explorerScreen

import android.content.Intent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import com.syncodec.graphite.di.model.NoteObjectLite
import com.syncodec.graphite.presentation.common.scaffold.GenericScaffold
import com.syncodec.graphite.presentation.explorer.ExplorerScreenViewModel
import com.syncodec.graphite.presentation.explorer.screen.explorerScreen.bar.BottomBar
import com.syncodec.graphite.presentation.explorer.screen.explorerScreen.bar.TopBar
import com.syncodec.graphite.presentation.explorer.screen.explorerScreen.bottomSheet.ExplorerBottomSheet
import com.syncodec.graphite.presentation.explorer.screen.explorerScreen.buildingBlock.atlasView.AtlasView
import com.syncodec.graphite.presentation.explorer.screen.explorerScreen.buildingBlock.calendarView.CalendarView
import com.syncodec.graphite.presentation.explorer.screen.explorerScreen.dialog.ExplorerDialog
import com.syncodec.graphite.presentation.explorer.screen.explorerScreen.dialog.ExplorerDialogType
import com.syncodec.graphite.presentation.explorer.screen.searchScreen.SearchScreen
import com.syncodec.graphite.presentation.note.NoteActivity
import com.syncodec.graphite.utils.Extra
import com.syncodec.graphite.utils.LocalIsAuthenticated
import io.realm.kotlin.types.RealmUUID
import org.koin.androidx.compose.koinViewModel


@OptIn(ExperimentalMaterialApi::class)
@Preview
@Composable
fun ExplorerScreen(
	explorerType : Extra.Companion.ExplorerType? = null,
	initSearchInChapterId : RealmUUID? = null,
	searchInDefaultChapter : Boolean = false,
	isStatic : Boolean = false,
	isSelecting : Boolean = false,
	onSelect : (RealmUUID) -> Unit = {},
	selectedIdList : List<RealmUUID> = listOf(),
	onClickCancelSelect : () -> Unit = {},
) {
	val context = LocalContext.current
	val viewModel : ExplorerScreenViewModel = koinViewModel()
	val hapticFeedback = LocalHapticFeedback.current

	val isAuthenticated = LocalIsAuthenticated.current

	val filteredNoteList by viewModel.filteredNoteList.collectAsState()
	val searchInChapter by viewModel.chapterObject.collectAsState()
	val tagList by viewModel.tagList.collectAsState()
	var contextNoteList by remember { mutableStateOf(listOf<NoteObjectLite>()) }

	LaunchedEffect(key1 = null) {
		if (searchInDefaultChapter) viewModel.filterOnDefaultChapter() else viewModel.filterOnChapter(initSearchInChapterId)
	}

	var sheetTitle by remember {
		mutableStateOf(
			when (explorerType) {
				Extra.Companion.ExplorerType.Atlas -> "Zoom into an area"
				Extra.Companion.ExplorerType.Calendar -> "Select a date"
				else -> "Explorer"
			}
		)
	}

	var isWhereDialogVisible by remember { mutableStateOf(false) }
	var isDeleteDialogVisible by remember { mutableStateOf(false) }
	fun openDialog(dialogType : ExplorerDialogType) = when (dialogType) {
		ExplorerDialogType.Where -> isWhereDialogVisible = true
		ExplorerDialogType.Delete -> isDeleteDialogVisible = true
	}

	fun closeDialog(dialogType : ExplorerDialogType) = when (dialogType) {
		ExplorerDialogType.Where -> isWhereDialogVisible = false
		ExplorerDialogType.Delete -> isDeleteDialogVisible = false
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

	fun onLongClickNote(id : RealmUUID) {
		hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
		onSelect(id)
	}

	if (explorerType == Extra.Companion.ExplorerType.Search) {
		SearchScreen()
	} else if (explorerType != null) {
		GenericScaffold(
			topBar = {
				if (! isStatic) TopBar(
					title = when (explorerType) {
						Extra.Companion.ExplorerType.Atlas -> "Atlas"
						Extra.Companion.ExplorerType.Attachment -> "Attachment"
						Extra.Companion.ExplorerType.Calendar -> "Calendar"
						else -> "Explorer"
					},
					isSelecting = isSelecting,
					selectedSize = selectedIdList.size,
					onClickCancelSelect = onClickCancelSelect,
					onClickDelete = { openDialog(ExplorerDialogType.Delete) },
				)
			},
			bottomBar = { if (! isStatic) BottomBar(searchInChapter = searchInChapter) { openDialog(ExplorerDialogType.Where) } },
			dialogContent = {
				ExplorerDialog(
					isWhereDialogVisible = isWhereDialogVisible,
					isDeleteDialogVisible = isDeleteDialogVisible,
					searchInChapter = searchInChapter,
					onSetSearchInChapter = { viewModel.filterOnChapter(it?.id) },
					onDelete = { viewModel.delete(idList = selectedIdList.toList()); onClickCancelSelect() },
					closeDialog = ::closeDialog,
				)
			}
		) {
			val scaffoldState = rememberBottomSheetScaffoldState()
			val sheetBackgroundColor =
				Color(
					ColorUtils.blendARGB(
						MaterialTheme.colorScheme.background.toArgb(),
						MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp).toArgb(),
						if (isSystemInDarkTheme()) 0.71f else 0.17f
					)
				)
			BottomSheetScaffold(
				sheetContent = {
					ExplorerBottomSheet(
						scaffoldState = scaffoldState,
						title = sheetTitle,
						headerBackgroundColor = sheetBackgroundColor,
						noteList = contextNoteList,
						tagList = tagList,
						selectedIdList = selectedIdList,
						onClickNote = { onClickNote(it.id) },
						onLongClickNote = { onLongClickNote(it.id) },
					)
				},
				scaffoldState = scaffoldState,
				sheetElevation = 8.dp,
				sheetBackgroundColor = sheetBackgroundColor,
				sheetContentColor = MaterialTheme.colorScheme.onBackground,
				sheetPeekHeight = 64.dp,
				backgroundColor = MaterialTheme.colorScheme.background
			) { paddingValues ->
				Box(
					modifier = Modifier
						.fillMaxSize()
						.padding(paddingValues)
				) {
					when (explorerType) {
						Extra.Companion.ExplorerType.Atlas -> AtlasView(
							noteList = filteredNoteList.filter { if (it.isLocked) isAuthenticated else true }
						) { title, noteList ->
							contextNoteList = noteList
							sheetTitle = title
						}

						Extra.Companion.ExplorerType.Attachment -> null
						Extra.Companion.ExplorerType.Calendar -> CalendarView(
							noteList = filteredNoteList.filter { if (it.isLocked) isAuthenticated else true }
						) { title, noteList ->
							contextNoteList = noteList
							sheetTitle = title
						}

						else -> null
					}
				}
			}
		}
	}
}
