package com.syncodec.momento.notebookComponent.miscellaneous

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.ChipDefaults
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FilterChip
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.syncodec.momento.R
import com.syncodec.momento.database.notebook.NotebookDbEntry
import com.syncodec.momento.miscellaneous.ThemeUtils.Companion.tone
import com.syncodec.momento.notebookComponent.NotebookActivity

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TopBar(
	notebookDbEntry: NotebookDbEntry,
	chapterNamePath: SnapshotStateList<String>,
	isSelected: Boolean,
	selectedItemSize: Int,
	showFavorite: Boolean,
	showArchived: Boolean,
	showLocked: Boolean,
	onAction: (NotebookActivity.Action, Any?) -> Unit
) {
	Column(modifier = Modifier.fillMaxWidth()) {
		Bar(
			title = notebookDbEntry.title,
			isSelected = isSelected,
			selectedItemSize = selectedItemSize
		) { action, data -> onAction(action, data) }

		Filter(
			showFavorite = showFavorite,
			showArchived = showArchived,
			showLocked = showLocked,
		) { action, data -> onAction(action, data) }

		Breadcrumb(chapterNamePath = chapterNamePath) {
			onAction(NotebookActivity.Action.NAVIGATE_CHAPTER, it)
		}

		Spacer(modifier = Modifier.height(4.dp))
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
						style = MaterialTheme.typography.titleMedium,
						color = MaterialTheme.colorScheme.onSurface
					)
				},
				actions = {
					IconButton(
						onClick = { onAction(NotebookActivity.Action.SHOW_DELETE, null) }
					) {
						Icon(
							painter = painterResource(id = R.drawable.ic_trash),
							contentDescription = "Delete items",
							tint = Color(0xFFF05945),
							modifier = Modifier
								.requiredSize(32.dp)
								.padding(4.dp)
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
								.requiredSize(32.dp)
								.padding(4.dp)
						)
					}
				},
				title = {
					Text(
						text = title,
						modifier = Modifier,
						style = MaterialTheme.typography.titleMedium,
						color = MaterialTheme.colorScheme.onSurface
					)
				},
				actions = {
					IconButton(
						onClick = { onAction(NotebookActivity.Action.MENU, null) }
					) {
						Icon(
							painter = painterResource(id = R.drawable.ic_menu),
							contentDescription = "Menu",
							tint = MaterialTheme.colorScheme.onSurface,
							modifier = Modifier
								.requiredSize(32.dp)
								.padding(4.dp)
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

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun Filter(
	showFavorite: Boolean,
	showArchived: Boolean,
	showLocked: Boolean,
	onAction: (NotebookActivity.Action, Any?) -> Unit
) {
	AnimatedVisibility(
		visible = showFavorite || showArchived || showLocked,
		enter = expandVertically(tween(600)) + fadeIn(tween(300)),
		exit = shrinkVertically(tween(600)) + fadeOut(tween(300))
	) {
		Spacer(modifier = Modifier.height(6.dp))

		Row(
			modifier = Modifier
				.fillMaxWidth()
				.horizontalScroll(rememberScrollState())
		) {
			Spacer(modifier = Modifier.width(12.dp))
			FilterChip(
				selected = showFavorite,
				onClick = { onAction(NotebookActivity.Action.TOGGLE_FAVOURITE, null) },
				colors = ChipDefaults.filterChipColors(
					backgroundColor = MaterialTheme.colorScheme.surface.tone(
						isSystemInDarkTheme(),
						1
					),
					selectedBackgroundColor = MaterialTheme.colorScheme.primary,
				),
				leadingIcon = {
					Row {
						Spacer(modifier = Modifier.width(4.dp))
						Icon(
							painter = painterResource(id = R.drawable.ic_favourite),
							contentDescription = null,
							tint = if (showFavorite) MaterialTheme.colorScheme.onPrimary
							else MaterialTheme.colorScheme.onSurface.tone(
								isSystemInDarkTheme(),
								1
							),
							modifier = Modifier.requiredSize(20.dp)
						)
					}
				}
			) {
				Text(
					text = "Favourite",
					style = MaterialTheme.typography.bodyMedium,
					color = if (showFavorite) MaterialTheme.colorScheme.onPrimary
					else MaterialTheme.colorScheme.onSurface.tone(
						isSystemInDarkTheme(),
						1
					)
				)
			}

			Spacer(modifier = Modifier.width(8.dp))

			FilterChip(
				selected = showArchived,
				onClick = { onAction(NotebookActivity.Action.TOGGLE_ARCHIVED, null) },
				colors = ChipDefaults.filterChipColors(
					backgroundColor = MaterialTheme.colorScheme.surface.tone(
						isSystemInDarkTheme(),
						1
					),
					selectedBackgroundColor = MaterialTheme.colorScheme.primary
				),
				leadingIcon = {
					Row {
						Spacer(modifier = Modifier.width(4.dp))
						Icon(
							painter = painterResource(id = R.drawable.ic_archive),
							contentDescription = null,
							tint = if (showArchived) MaterialTheme.colorScheme.onPrimary
							else MaterialTheme.colorScheme.onSurface.tone(
								isSystemInDarkTheme(),
								1
							),
							modifier = Modifier.requiredSize(20.dp)
						)
					}
				}
			) {
				Text(
					text = "Archived",
					style = MaterialTheme.typography.bodyMedium,
					color = if (showArchived) MaterialTheme.colorScheme.onPrimary
					else MaterialTheme.colorScheme.onSurface.tone(
						isSystemInDarkTheme(),
						1
					)
				)
			}

			Spacer(modifier = Modifier.width(8.dp))

			FilterChip(
				selected = showLocked,
				onClick = { onAction(NotebookActivity.Action.LOCKED, null) },
				colors = ChipDefaults.filterChipColors(
					backgroundColor = MaterialTheme.colorScheme.surface.tone(
						isSystemInDarkTheme(),
						1
					),
					selectedBackgroundColor = MaterialTheme.colorScheme.primary
				),
				leadingIcon = {
					Row {
						Spacer(modifier = Modifier.width(4.dp))
						Icon(
							painter = painterResource(id = R.drawable.ic_lock_close),
							contentDescription = null,
							tint = if (showLocked) MaterialTheme.colorScheme.onPrimary
							else MaterialTheme.colorScheme.onSurface.tone(
								isSystemInDarkTheme(),
								1
							),
							modifier = Modifier.requiredSize(20.dp)
						)
					}
				}
			) {
				Text(
					text = "Locked",
					style = MaterialTheme.typography.bodyMedium,
					color = if (showLocked) MaterialTheme.colorScheme.onPrimary
					else MaterialTheme.colorScheme.onSurface.tone(
						isSystemInDarkTheme(),
						1
					)
				)
			}
		}
	}
}
