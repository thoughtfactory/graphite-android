package com.syncodec.graphite.presentation.main.composable.screen.explorerScreen

import android.content.Intent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.explorer.atlas.composable.AtlasView2
import com.syncodec.graphite.presentation.explorer.composable.ExplorerView
import com.syncodec.graphite.presentation.note.NoteActivity
import com.syncodec.graphite.utils.Extra
import io.realm.kotlin.types.RealmUUID
import org.koin.androidx.compose.koinViewModel


@Preview
@Composable
fun AtlasScreen(
	isSelecting: Boolean = false,
	selectedIdList: Set<RealmUUID> = setOf(),
	onSelect: (RealmUUID) -> Unit = {},
	onUnSelectAll: () -> Unit = {},
) {
	val context = LocalContext.current
	val explorerViewModel: ExplorerViewModel = koinViewModel()

	val chapterFilteredNoteList by explorerViewModel.chapterFilteredNoteList.collectAsState()
	val locationFilteredNoteList by explorerViewModel.locationFilteredNoteList.collectAsState()

	fun onClickNote(id: RealmUUID) {
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

	ExplorerView(
		bottomSheetTitle = stringResource(id = R.string.within_default_chapter),
		noteList = locationFilteredNoteList,
		isSelecting = isSelecting,
		selectedIdList = selectedIdList,
		onSelect = onSelect,
		onClickNote = ::onClickNote,
		onClickFavourite = { explorerViewModel.onClickMultiFavourite(idList = selectedIdList) },
		onClickLock = { explorerViewModel.onClickMultiLock(idList = selectedIdList) },
		onUnSelectAll = onUnSelectAll,
		onConfirmDelete = { explorerViewModel.delete(it) },
	) {
		AtlasView2(
			modifier = Modifier.fillMaxSize(),
			noteList = chapterFilteredNoteList,
			onUpdateCameraBound = explorerViewModel::onUpdateCameraBound,
		)
	}
}
