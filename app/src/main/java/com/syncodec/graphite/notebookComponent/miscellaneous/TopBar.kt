package com.syncodec.graphite.notebookComponent.miscellaneous

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.custom.ChipData
import com.syncodec.graphite.custom.ChipView
import com.syncodec.graphite.database.notebook.NotebookDbEntry
import com.syncodec.graphite.notebookComponent.NotebookActivity


@Composable
fun TopBar(
	notebookDbEntry: NotebookDbEntry,
	chapterNamePath: SnapshotStateList<String>,
	isSelected: Boolean,
	selectedItemSize: Int,
	showFavorite: Boolean,
	showArchived: Boolean,
	onAction: (NotebookActivity.Action, Any?) -> Unit
) {
	Column(modifier = Modifier.fillMaxWidth()) {
		Bar(
			title = notebookDbEntry.title,
			isSelected = isSelected,
			selectedItemSize = selectedItemSize
		) { action, data -> onAction(action, data) }


		Spacer(modifier = Modifier.height(6.dp))

		Breadcrumb(chapterNamePath = chapterNamePath) {
			onAction(NotebookActivity.Action.NAVIGATE_CHAPTER, it)
		}

		Filter(
			showFavorite = showFavorite,
			showArchived = showArchived,
		) { action, data -> onAction(action, data) }

		Spacer(modifier = Modifier.height(6.dp))
	}
}

@Composable
private fun Bar(
	title: String,
	isSelected: Boolean,
	selectedItemSize: Int,
	onAction: (NotebookActivity.Action, Any?) -> Unit
) {
	Crossfade(targetState = isSelected) {
		if (it) {
			SmallTopAppBar(
				title = {
					Text(
						text = if (selectedItemSize == 0) "Select items to delete" else if (selectedItemSize == 1) "1 item selected" else "$selectedItemSize items selected",
						modifier = Modifier,
						color = MaterialTheme.colorScheme.onSurface
					)
				},
				actions = {
					IconButton(
						onClick = { onAction(NotebookActivity.Action.SHOW_DELETE, null) }
					) {
						Icon(
							painter = painterResource(id = R.drawable.ic_delete),
							contentDescription = "Delete items",
							tint = Color(0xFFF05945),
							modifier = Modifier
						)
					}
				},
				colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
					containerColor = MaterialTheme.colorScheme.surface,
				),
			)
		} else {
			SmallTopAppBar(
				navigationIcon = {
					IconButton(
						onClick = { onAction(NotebookActivity.Action.BACK, null) }
					) {
						Icon(
							painter = painterResource(id = R.drawable.ic_back),
							contentDescription = "Back",
							tint = MaterialTheme.colorScheme.onSurface,
							modifier = Modifier
						)
					}
				},
				title = {
					Text(
						text = title,
						modifier = Modifier,
						color = MaterialTheme.colorScheme.onSurface
					)
				},
				actions = {
					IconButton(
						onClick = { onAction(NotebookActivity.Action.OPEN_METADATA, null) }
					) {
						Icon(
							painter = painterResource(id = R.drawable.ic_info),
							contentDescription = "Metadata",
							tint = MaterialTheme.colorScheme.onSurface,
							modifier = Modifier
						)
					}

					IconButton(
						onClick = { onAction(NotebookActivity.Action.OPEN_MENU, null) }
					) {
						Icon(
							painter = painterResource(id = R.drawable.ic_menu),
							contentDescription = "Menu",
							tint = MaterialTheme.colorScheme.onSurface,
							modifier = Modifier
						)
					}
				},
				colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
					containerColor = MaterialTheme.colorScheme.surface,
				),
			)
		}
	}
}

@Composable
private fun Filter(
	showFavorite: Boolean,
	showArchived: Boolean,
	onAction: (NotebookActivity.Action, Any?) -> Unit
) {
	val chipDataList: List<ChipData> = listOf(
		ChipData(
			title = "Favourite",
			icon = R.drawable.ic_favourite,
			isSelected = showFavorite
		) { onAction(NotebookActivity.Action.TOGGLE_FAVOURITE, null) },
		ChipData(
			title = "Archive",
			icon = R.drawable.ic_archive,
			isSelected = showArchived
		) { onAction(NotebookActivity.Action.TOGGLE_ARCHIVED, null) },
	)

	AnimatedVisibility(
		visible = showFavorite || showArchived,
		enter = expandVertically(tween(600)) + fadeIn(tween(300)),
		exit = shrinkVertically(tween(600)) + fadeOut(tween(300)),
		modifier = Modifier.fillMaxWidth()
	) {
		Column(
			modifier = Modifier.fillMaxWidth()
		) {
			Spacer(modifier = Modifier.height(6.dp))
			ChipView(chipDataList = chipDataList)
		}
	}
}
