package com.syncodec.graphite.presentation.note.composable.bar.viewer

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.syncodec.graphite.di.model.local.ChapterObjectLite
import com.syncodec.graphite.presentation.common.button.BackButton
import com.syncodec.graphite.presentation.common.button.DeleteButton
import com.syncodec.graphite.presentation.common.button.FavouriteButton
import com.syncodec.graphite.presentation.common.button.LockButton
import com.syncodec.graphite.presentation.common.button.PinButton
import com.syncodec.graphite.presentation.note.composable.bar.buildingBlock.ChapterSelector


@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun ViewerTopBar(
	parentChapter: ChapterObjectLite? = null,
	isFavourite: Boolean = false,
	isLocked: Boolean = false,
	onClickLocalOnly: () -> Unit = {},
	onClickFavourite: () -> Unit = {},
	onClickLock: () -> Unit = {},
	onClickPin: () -> Unit = {},
	onClickDelete: () -> Unit = {},
	onClickBack: () -> Unit = {},
	onClickSelectChapter: () -> Unit = {},
) {
	TopAppBar(
		navigationIcon = { BackButton() },
		title = { ChapterSelector(parentChapter = parentChapter, onClickSelectChapter = onClickSelectChapter) },
		actions = {
			DeleteButton(onClick = onClickDelete)
			PinButton(onClick = onClickPin)
			LockButton(
				isLocked = isLocked,
				onClick = onClickLock,
			)
			FavouriteButton(
				isFavourite = isFavourite,
				onClick = onClickFavourite,
			)
		},
		colors = TopAppBarDefaults.topAppBarColors(
			containerColor = MaterialTheme.colorScheme.background,
			navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
			actionIconContentColor = MaterialTheme.colorScheme.onBackground,
		)
	)
}
