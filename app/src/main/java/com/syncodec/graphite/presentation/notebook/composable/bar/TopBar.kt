package com.syncodec.graphite.presentation.notebook.composable.bar

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.syncodec.graphite.di.model.ChapterObjectLite
import com.syncodec.graphite.presentation.common.button.BackButton
import com.syncodec.graphite.presentation.common.button.FavouriteButton
import com.syncodec.graphite.presentation.common.button.FilterButton
import com.syncodec.graphite.presentation.common.button.GenericButtonDefaults
import com.syncodec.graphite.presentation.common.button.LockButton
import com.syncodec.graphite.presentation.common.button.MenuButton
import com.syncodec.graphite.presentation.ui.FavouriteContainer
import com.syncodec.graphite.presentation.ui.LockClosedContainer
import com.syncodec.graphite.utils.getInverseBWColor
import io.realm.kotlin.types.RealmUUID


@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun TopBar(
	chapterTitle: String? = null,
	isFavourite: Boolean = false,
	isLocked: Boolean = false,
	chapterPath: List<ChapterObjectLite> = listOf(),
	defaultChapterId: RealmUUID? = null,
	onClickBack: () -> Unit = {},
	onClickFavourite: () -> Unit = {},
	onClickLock: () -> Unit = {},
	onClickMenuButton: () -> Unit = {},
) {
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollapsedTopBar(
	modifier: Modifier = Modifier,
	isCollapsed: Boolean,
	chapterTitle: String? = null,
	isFavourite: Boolean = false,
	isLocked: Boolean = false,
	chapterColor: Color? = null,
	chapterPath: List<ChapterObjectLite> = listOf(),
	defaultChapterId: RealmUUID? = null,
	onContainerColor: Color = MaterialTheme.colorScheme.onBackground,
	onClickBack: () -> Unit = {},
	onClickFavourite: () -> Unit = {},
	onClickLock: () -> Unit = {},
	onClickMenuButton: () -> Unit = {},
) {
	val containerColor by animateColorAsState(
		targetValue = if (isCollapsed) MaterialTheme.colorScheme.background else chapterColor ?: Color.Transparent,
		label = "containerColor_animation"
	)
	val contentColor by animateColorAsState(
		targetValue = if (isCollapsed) MaterialTheme.colorScheme.onBackground else onContainerColor,
		label = "contentColor_animation"
	)

	Column(
		modifier = modifier.fillMaxWidth()
	) {
		TopAppBar(
			navigationIcon = { BackButton(colors = GenericButtonDefaults.transparentButtonColors(iconColor = contentColor), onClick = onClickBack) },
			title = {
				AnimatedVisibility(
					visible = isCollapsed
				) {
					Text(
						text = chapterTitle ?: "Untitled",
						fontStyle = if (chapterTitle.isNullOrEmpty()) FontStyle.Italic else FontStyle.Normal,
						color = contentColor
					)
				}
			},
			actions = {
				FilterButton(colors = GenericButtonDefaults.transparentButtonColors(iconColor = contentColor))

				LockButton(
					isLocked = isLocked,
					colors = GenericButtonDefaults.transparentButtonColors(
						iconColor = contentColor,
						checkedIconColor = Color.LockClosedContainer
					),
					onClick = onClickLock,
				)

				FavouriteButton(
					isFavourite = isFavourite,
					colors = GenericButtonDefaults.transparentButtonColors(
						iconColor = contentColor,
						checkedIconColor = Color.FavouriteContainer
					),
					onClick = onClickFavourite,
				)

				MenuButton(
					colors = GenericButtonDefaults.transparentButtonColors(iconColor = contentColor),
					onClick = onClickMenuButton
				)
			},
			colors = TopAppBarDefaults.topAppBarColors(
				containerColor = containerColor,
				navigationIconContentColor = contentColor,
				titleContentColor = contentColor,
				actionIconContentColor = contentColor,
			),
		)
//		Navigator(
//			chapterPath = chapterPath.reversed(),
//			defaultChapterId = defaultChapterId,
//			showRoot = false,
//			isVisible = true,
////			onClickNavigatorChapter = onClickNavigatorChapter,
//		)
	}
}

@Composable
fun ExpandedTopBar(
	chapterTitle: String? = null,
	containerColor: Color? = null,
	bitmap: Bitmap? = null,
	firstVisibleItemScrollOffset : Int = 0,
) {
	val context = LocalContext.current

	val animateContainerColor by animateColorAsState(
		targetValue = containerColor ?: MaterialTheme.colorScheme.background,
		label = "animateContainerColor"
	)

	Box(
		modifier = Modifier
			.background(MaterialTheme.colorScheme.background)
			.fillMaxWidth()
			.height(EXPANDED_TOP_BAR_HEIGHT),
		contentAlignment = Alignment.BottomStart
	) {
		SubcomposeAsyncImage(
			model = ImageRequest.Builder(context)
				.data(bitmap)
				.crossfade(470)
				.build(),
			error = {
				Box(
					modifier = Modifier
						.fillMaxSize()
						.background(animateContainerColor)
				)
			},
			contentDescription = null,
			contentScale = ContentScale.Crop,
			modifier = Modifier.fillMaxSize()
		)
		if (bitmap != null) {
			Box(
				modifier = Modifier
					.fillMaxSize()
					.background(Color.Black.copy(alpha = 0.17f))
			)
		}
		Text(
			text = chapterTitle ?: "Untitled",
			style = MaterialTheme.typography.headlineLarge,
			fontStyle = if (chapterTitle.isNullOrEmpty()) FontStyle.Italic else FontStyle.Normal,
			color = containerColor?.getInverseBWColor() ?: Color.White,
			modifier = Modifier.padding(16.dp)
		)
	}
}


@Preview
@Composable
private fun CollapsedTopBarPreview() {
	Column {
		CollapsedTopBar(isCollapsed = true)
		Spacer(Modifier.height(16.dp))
		CollapsedTopBar(isCollapsed = false)
	}
}

@Preview
@Composable
private fun ExpandedTopBarPreview() {
	ExpandedTopBar()
}

val COLLAPSED_TOP_BAR_HEIGHT = 64.dp
val EXPANDED_TOP_BAR_HEIGHT = 256.dp