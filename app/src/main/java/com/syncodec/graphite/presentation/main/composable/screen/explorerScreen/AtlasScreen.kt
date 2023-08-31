package com.syncodec.graphite.presentation.main.composable.screen.explorerScreen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.explorer.atlas.composable.AtlasView2
import com.syncodec.graphite.presentation.explorer.composable.ExplorerView
import org.koin.androidx.compose.koinViewModel

@Preview
@Composable
fun AtlasScreen() {
	val explorerViewModel: ExplorerViewModel = koinViewModel()

	val chapterFilteredNoteList by explorerViewModel.chapterFilteredNoteList.collectAsState()
	val locationFilteredNoteList by explorerViewModel.locationFilteredNoteList.collectAsState()

	ExplorerView(
		bottomSheetTitle = stringResource(id = R.string.within_default_chapter),
		noteList = locationFilteredNoteList,
	) {
		AtlasView2(
			modifier = Modifier.fillMaxSize(),
			noteList = chapterFilteredNoteList,
			onClickNote = {},
			onUpdateCameraBound = explorerViewModel::onUpdateCameraBound,
		)
	}
}
