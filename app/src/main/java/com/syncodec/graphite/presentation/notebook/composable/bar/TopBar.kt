package com.syncodec.graphite.presentation.notebook.composable.bar

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.ChapterObjectLite
import com.syncodec.graphite.presentation.common.button.MenuButton
import com.syncodec.graphite.presentation.notebook.NotebookActivity
import com.syncodec.graphite.presentation.notebook.composable.buildingBlock.Navigator.Navigator
import io.realm.kotlin.types.ObjectId


@Composable
fun TopBar(
	title: String?,
	rootChapterId: ObjectId?,
	color: Color,
	isLocked: Boolean,
	isFavourite: Boolean,
	chapterObjectLiteList: List<ChapterObjectLite>,
	onClickLock: () -> Unit,
	onClickFavourite: () -> Unit,
	onClickMenu: () -> Unit,
	onClickNavigator: (ObjectId) -> Unit,
) {
	Column(
		modifier = Modifier
			.fillMaxWidth()
			.background(MaterialTheme.colorScheme.background)
	) {
		Bar(
			title = title ?: "",
			isLocked = isLocked,
			isFavourite = isFavourite,
			onClickLock = onClickLock,
			onClickFavourite = onClickFavourite
		)

		Navigator(
			chapterObjectLiteList = chapterObjectLiteList,
			rootChapterId = rootChapterId,
			color = color,
			onClick = onClickNavigator
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
) {
	val activity: NotebookActivity = LocalContext.current as NotebookActivity

	TopAppBar(
		navigationIcon = {
			MenuButton(
				icon = R.drawable.ic_back,
				contentDescription = "Back",
				tint = MaterialTheme.colorScheme.onBackground
			) {
				activity.onBackPressed()
			}
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
