package com.syncodec.graphite.presentation.notebook.composable.bar

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.button.MenuButton
import com.syncodec.graphite.presentation.notebook.NotebookActivity
import com.syncodec.graphite.presentation.notebook.composable.bottomSheet.NotebookBottomSheetType
import com.syncodec.graphite.presentation.notebook.composable.buildingBlock.navigator.Navigator


@Preview
@Composable
fun TopBar() {

	val defaultChapterId = NotebookActivity.LocalDefaultChapterId.current

	val title = NotebookActivity.LocalTitle.current
	val isFavourite = NotebookActivity.LocalIsFavourite.current
	val isLocked = NotebookActivity.LocalIsLocked.current

	val getChapter = NotebookActivity.LocalGetChapter.current
	val parentChapterObjectList = NotebookActivity.LocalParentChapterObjectList.current

	val onToggleFavourite = NotebookActivity.LocalOnToggleFavourite.current
	val onToggleLock = NotebookActivity.LocalOnToggleLock.current
	val onBackPressed = NotebookActivity.LocalOnBackPressed.current

	val onOpenSheet = NotebookActivity.LocalOpenBottomSheet.current

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
			onClickFilter = { onOpenSheet(NotebookBottomSheetType.FILTER) },
			onBackPressed = onBackPressed
		)

		Navigator(
			defaultChapterId = defaultChapterId,
			chapterObjectLiteList = parentChapterObjectList,
			onClick = getChapter
		)
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
	onBackPressed: () -> Unit
) {
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
		colors = TopAppBarDefaults.smallTopAppBarColors(
			containerColor = MaterialTheme.colorScheme.background,
			navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
			titleContentColor = MaterialTheme.colorScheme.onSurface,
			actionIconContentColor = MaterialTheme.colorScheme.onSurface,
		)
	)
}
