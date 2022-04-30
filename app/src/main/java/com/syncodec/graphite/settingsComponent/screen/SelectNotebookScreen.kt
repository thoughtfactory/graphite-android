package com.syncodec.graphite.settingsComponent.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.custom.notebook.NotebookGridCard
import com.syncodec.graphite.database.notebook.NotebookDbEntry
import com.syncodec.graphite.settingsComponent.SettingsActivity

@Composable
fun SelectNotebookScreen(
	notebookList: List<NotebookDbEntry>,
	onClick: (SettingsActivity.Action, Any?) -> Unit
) {
	LazyVerticalGrid(
		columns = GridCells.Adaptive(144.dp),
		horizontalArrangement = Arrangement.Center,
		modifier = Modifier
			.fillMaxSize()
			.padding(12.dp, 0.dp),
	) {
		notebookList.forEach {
			item {
				NotebookGridCard(
					notebook = it,
					isSelected = false,
					onClick = { onClick(SettingsActivity.Action.EXPORT_NOTEBOOK, it) },
					onLongClick = {}
				)
			}
		}
	}
}
