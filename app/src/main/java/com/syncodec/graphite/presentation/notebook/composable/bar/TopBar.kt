package com.syncodec.graphite.presentation.notebook.composable.bar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.LocalCompositionIsSelected
import com.syncodec.graphite.presentation.common.LocalCompositionOnSelect
import com.syncodec.graphite.presentation.common.LocalCompositionSelectedObjectIdList
import com.syncodec.graphite.presentation.common.button.MenuButton
import com.syncodec.graphite.presentation.notebook.NotebookActivity
import com.syncodec.graphite.presentation.notebook.composable.bottomSheet.NotebookBottomSheetType
import com.syncodec.graphite.presentation.notebook.composable.buildingBlock.navigator.Navigator
import com.syncodec.graphite.presentation.notebook.composable.dialog.NotebookDialogType
import com.syncodec.graphite.presentation.ui.DeleteContainer


@Preview
@Composable
fun TopBar() {

	val defaultChapterId = NotebookActivity.LocalDefaultChapterId.current

	val title = NotebookActivity.LocalTitle.current
	val isFavourite = NotebookActivity.LocalIsFavourite.current
	val isLocked = NotebookActivity.LocalIsLocked.current

	val getChapter = NotebookActivity.LocalGetChapter.current
	val parentChapterObjectList = NotebookActivity.LocalParentChapterObjectList.current

	val isSelected = LocalCompositionIsSelected.current
	val onToggleFavourite = NotebookActivity.LocalOnToggleFavourite.current
	val onToggleLock = NotebookActivity.LocalOnToggleLock.current
	val onBackPressed = NotebookActivity.LocalOnBackPressed.current

	val openSheet = NotebookActivity.LocalOpenBottomSheet.current
	val openDialog = NotebookActivity.LocalOpenDialog.current

	Column(
		modifier = Modifier
			.fillMaxWidth()
			.background(MaterialTheme.colorScheme.background)
	) {
		Bar(
			title = title ?: "",
			isLocked = isLocked ?: false,
			isFavourite = isFavourite ?: false,
			onClickLock = onToggleLock,
			onClickFavourite = onToggleFavourite,
			onClickFilter = { openSheet(NotebookBottomSheetType.FILTER) },
			onClickDelete = { openDialog(NotebookDialogType.DELETE_SELECTED) },
			onBackPressed = onBackPressed
		)

		AnimatedVisibility(
			visible = ! isSelected,
			enter = expandVertically(tween(300)),
			exit = shrinkVertically(tween(300))
		) {
			Navigator(
				defaultChapterId = defaultChapterId,
				chapterObjectLiteList = parentChapterObjectList,
				onClick = getChapter
			)
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Bar(
	title: String,
	isLocked: Boolean,
	isFavourite: Boolean,
	onClickLock: () -> Unit,
	onClickFavourite: () -> Unit,
	onClickFilter: () -> Unit,
	onClickDelete: () -> Unit,
	onBackPressed: () -> Unit
) {
	val isSelected = LocalCompositionIsSelected.current
	val onSelect = LocalCompositionOnSelect.current
	val selectedObjectIdList = LocalCompositionSelectedObjectIdList.current

	Crossfade(
		targetState = isSelected,
		animationSpec = tween(300)
	) {
		if (it) {
			TopAppBar(
				navigationIcon = {
					MenuButton(
						icon = R.drawable.ic_close,
						tint = MaterialTheme.colorScheme.onBackground,
					) {
						onSelect(false)
						selectedObjectIdList.clear()
					}
				},
				title = {
					Text(
						text = if (selectedObjectIdList.isEmpty()) "No items selected" else if (selectedObjectIdList.size == 1) "1 item selected" else "${selectedObjectIdList.size} items selected",
						color = MaterialTheme.colorScheme.onBackground
					)
				},
				actions = {
					IconButton(
						onClick = onClickDelete
					) {
						Icon(
							painter = painterResource(id = R.drawable.ic_delete),
							contentDescription = "Delete items",
							tint = Color.DeleteContainer
						)
					}
				},
				colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.background)
			)
		} else {
			TopAppBar(
				navigationIcon = {
					MenuButton(
						icon = R.drawable.ic_back,
						contentDescription = "Back",
						tint = MaterialTheme.colorScheme.onBackground,
						onClick = onBackPressed
					)
				},
				title = {
					Crossfade(
						targetState = title,
						animationSpec = tween(300)
					) {
						Text(
							text = it,
							color = MaterialTheme.colorScheme.onBackground,
							fontWeight = FontWeight.Bold
						)
					}
				},
				actions = {
					MenuButton(
						icon = R.drawable.ic_filter,
						tint = MaterialTheme.colorScheme.onBackground,
						onClick = onClickFilter
					)

					MenuButton(
						icon = if (isLocked) R.drawable.ic_lock_close else R.drawable.ic_lock_open,
						contentDescription = if (isLocked) "Locked" else "Not locked",
						tint = MaterialTheme.colorScheme.onBackground,
						isChecked = isLocked,
						isEnabled = true,
						onClick = onClickLock
					)

					MenuButton(
						icon = R.drawable.ic_favourite,
						contentDescription = "Favourite",
						tint = MaterialTheme.colorScheme.onBackground,
						isChecked = isFavourite,
						isEnabled = true,
						onClick = onClickFavourite,
					)
				},
				colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
					containerColor = MaterialTheme.colorScheme.background,
					navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
					titleContentColor = MaterialTheme.colorScheme.onBackground,
					actionIconContentColor = MaterialTheme.colorScheme.onBackground,
				)
			)
		}
	}









}
