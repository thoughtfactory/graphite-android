package com.syncodec.graphite.presentation.main.composable.screen.explorerScreen

import android.content.Intent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.dialog.dialog2.DeleteDialog
import com.syncodec.graphite.presentation.explorer.calendar.composable.calendarView.CalendarView
import com.syncodec.graphite.presentation.explorer.composable.ExplorerView
import com.syncodec.graphite.presentation.note2.NoteActivity2
import com.syncodec.graphite.utils.Extra
import io.realm.kotlin.types.RealmUUID
import org.koin.androidx.compose.koinViewModel


@Preview
@Composable
fun CalendarScreen(
	isSelecting: Boolean = false,
	selectedIdList: Set<RealmUUID> = setOf(),
	onSelect: (RealmUUID) -> Unit = {},
	onUnSelectAll: () -> Unit = {},
) {
	val context = LocalContext.current
	val explorerViewModel: ExplorerViewModel = koinViewModel()

	val selectedDate by explorerViewModel.selectedDate.collectAsState()
	val noteListDateCountMap by explorerViewModel.noteListDateCountMap.collectAsState()
	val dateFilteredNoteList by explorerViewModel.dateFilteredNoteList.collectAsState()

	var isDeleteDialogVisible by remember { mutableStateOf(false) }

	fun onClickNote(id: RealmUUID) {
		if (isSelecting) {
			onSelect(id)
		} else {
			Intent(context, NoteActivity2::class.java).apply {
				putExtra(Extra.Companion.Extra.IsNew.name, false)
				putExtra(Extra.Companion.Extra.NoteId.name, id.bytes)
				putExtra(Extra.Companion.Extra.Filter.name, Extra.Companion.Filter.SingleRead.name)
				context.startActivity(this)
			}
		}
	}

	ExplorerView(
		bottomSheetTitle = stringResource(id = R.string.within_default_chapter),
		noteList = dateFilteredNoteList,
		isSelecting = isSelecting,
		selectedIdList = selectedIdList,
		onSelect = onSelect,
		onClickNote = ::onClickNote,
		onClickDelete = { isDeleteDialogVisible = true },
		onClickFavourite = { explorerViewModel.onClickMultiFavourite(idList = selectedIdList) },
		onClickLock = { explorerViewModel.onClickMultiLock(idList = selectedIdList) },
	) {
		CalendarView(
			selectedDate = selectedDate,
			noteListDateCountMap = noteListDateCountMap,
			onSelectDate = explorerViewModel::onSelectDate,
			modifier = Modifier.fillMaxSize()
		)
	}

	DeleteDialog(
		isDialogVisible = isDeleteDialogVisible,
		onDismissRequest = { isDeleteDialogVisible = false },
		title = stringResource(id = R.string.delete_items_multiple),
		contentText = stringResource(id = R.string.are_you_sure_delete_multiple),
		onConfirmDelete = { isDeleteDialogVisible = false; explorerViewModel.delete(selectedIdList); onUnSelectAll() },
	)
}
