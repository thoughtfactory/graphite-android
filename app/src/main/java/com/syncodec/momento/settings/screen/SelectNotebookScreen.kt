package com.syncodec.momento.settings.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.syncodec.momento.custom.notebook.NotebookGridCard
import com.syncodec.momento.database.notebook.NotebookDbEntry
import com.syncodec.momento.settings.SettingsActivity

@Composable
fun SelectNotebookScreen(
	notebookMap: Map<String, Pair<NotebookDbEntry, Int>>,
	onClick: (SettingsActivity.Click, Any?) -> Unit
) {
	LazyVerticalGrid(
		columns = GridCells.Adaptive(144.dp),
		horizontalArrangement = Arrangement.Center,
		modifier = Modifier
			.fillMaxSize()
			.padding(12.dp, 0.dp),
	) {
		notebookMap.forEach { (_, data) ->
			item {
				NotebookGridCard(notebook = data.first, notebookSize = data.second) {
					onClick(SettingsActivity.Click.EXPORT_NOTEBOOK, it)
				}
			}
		}
	}
}
