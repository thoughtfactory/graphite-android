package com.syncodec.graphite.presentation.notebook.screen.composable.bar

import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.ChapterObjectLite
import com.syncodec.graphite.presentation.common.animation.AnimatedText
import com.syncodec.graphite.presentation.common.button.GenericButton
import com.syncodec.graphite.presentation.common.button.GenericButtonDefaults
import io.realm.kotlin.types.RealmUUID


@Preview
@Composable
fun TopBar(
	title : String? = null,
	defaultChapterId : RealmUUID? = null,
	isLocked : Boolean = false,
	isFavourite : Boolean = false,
	chapterPath : List<ChapterObjectLite> = listOf(),
	isSelecting : Boolean = false,
	selectedSize : Int = 0,
	onClickBack : () -> Unit = {},
	onClickCancelSelect : () -> Unit = {},
	onClickLocalOnly : () -> Unit = {},
	onClickLock : () -> Unit = {},
	onClickFavourite : () -> Unit = {},
	onClickFilter : () -> Unit = {},
	onClickDelete : () -> Unit = {},
	onClickNavigatorChapter : (RealmUUID?) -> Unit = {},
) {
	Column(
		modifier = Modifier.fillMaxWidth()
	) {
		Bar(
			title = title,
			isLocked = isLocked,
			isFavourite = isFavourite,
			isSelecting = isSelecting,
			selectedSize = selectedSize,
			onClickBack = onClickBack,
			onClickCancelSelect = onClickCancelSelect,
			onClickLocalOnly = onClickLocalOnly,
			onClickLock = onClickLock,
			onClickFavourite = onClickFavourite,
			onClickFilter = onClickFilter,
			onClickDelete = onClickDelete,
		)

		Navigator(
			chapterPath = chapterPath.reversed(),
			defaultChapterId = defaultChapterId,
			showRoot = false,
			isVisible = ! isSelecting,
			onClickNavigatorChapter = onClickNavigatorChapter,
		)
	}
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
private fun Bar(
	title : String? = null,
	isLocked : Boolean = false,
	isFavourite : Boolean = false,
	isSelecting : Boolean = false,
	selectedSize : Int = 0,
	onClickBack : () -> Unit = {},
	onClickCancelSelect : () -> Unit = {},
	onClickLocalOnly : () -> Unit = {},
	onClickLock : () -> Unit = {},
	onClickFavourite : () -> Unit = {},
	onClickFilter : () -> Unit = {},
	onClickDelete : () -> Unit = {},
) {
	val context = LocalContext.current

	Crossfade(
		targetState = isSelecting,
		animationSpec = tween(300),
		label = "isSelecting_animation"
	) {
		if (it) {
			TopAppBar(
				navigationIcon = {
					GenericButton(
						icon = R.drawable.ic_close,
						onClick = onClickCancelSelect
					)
				},
				title = {
					AnimatedText(
						text = if (selectedSize == 0) "No items selected" else if (selectedSize == 1) "1 item selected" else "$selectedSize items selected",
						color = MaterialTheme.colorScheme.onBackground,
					)
				},
				actions = {
					GenericButton(
						icon = R.drawable.ic_delete,
						colors = GenericButtonDefaults.deleteButtonColors(),
						onClick = onClickDelete
					)
				},
				colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.background)
			)
		} else {
			TopAppBar(
				navigationIcon = {
					GenericButton(
						icon = R.drawable.ic_back,
						onClick = onClickBack
					)
				},
				title = {
					AnimatedText(
						text = title ?: "Untitled",
						fontStyle = if (title.isNullOrEmpty()) FontStyle.Italic else FontStyle.Normal,
					)
				},
				actions = {
					GenericButton(
						icon = R.drawable.ic_filter,
						onClick = onClickFilter
					)

					GenericButton(
						icon = if (isLocked) R.drawable.ic_lock_close else R.drawable.ic_lock_open,
						checked = isLocked,
						onClick = onClickLock
					)

					GenericButton(
						icon = R.drawable.ic_favourite,
						checked = isFavourite,
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
