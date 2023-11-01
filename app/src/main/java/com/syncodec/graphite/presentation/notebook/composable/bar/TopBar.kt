package com.syncodec.graphite.presentation.notebook.composable.bar

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.local.ChapterObjectLite
import com.syncodec.graphite.presentation.common.button.BackButton
import com.syncodec.graphite.presentation.common.button.FavouriteButton
import com.syncodec.graphite.presentation.common.button.GenericButtonDefaults
import com.syncodec.graphite.presentation.common.button.LockButton
import com.syncodec.graphite.presentation.common.button.MenuButton
import com.syncodec.graphite.presentation.common.tab.GenericTabRow
import com.syncodec.graphite.presentation.common.tab.TabItem
import com.syncodec.graphite.presentation.base.FavouriteContainer
import com.syncodec.graphite.presentation.base.LocalIsDarkTheme
import com.syncodec.graphite.presentation.base.LockClosedContainer
import com.syncodec.graphite.presentation.common.component.chapter.Navigator
import com.syncodec.graphite.utils.getInverseBWColor
import io.realm.kotlin.types.RealmUUID


@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun CollapsedTopBar(
	modifier: Modifier = Modifier,
	chapterTitle: String? = null,
	isFavourite: Boolean = false,
	isLocked: Boolean = false,
	chapterColor: Color? = null,
	chapterPath: List<ChapterObjectLite> = listOf(),
	defaultChapterId: RealmUUID? = null,
	selectedTab: Int = 0,
	isCollapsed: Boolean = false,
	isSelecting: Boolean = false,
	onContainerColor: Color = MaterialTheme.colorScheme.onBackground,
	onClickFavourite: () -> Unit = {},
	onClickLock: () -> Unit = {},
	onClickMenuButton: () -> Unit = {},
	onLoadChapter: (RealmUUID) -> Unit = {},
	onSelectTab: (Int) -> Unit = {}
) {

	val isDarkTheme = LocalIsDarkTheme.current

	@Suppress("KotlinConstantConditions")
	val containerColor = when {
		(chapterColor == null) && isCollapsed && isDarkTheme -> MaterialTheme.colorScheme.background
		(chapterColor == null) && isCollapsed && !isDarkTheme -> MaterialTheme.colorScheme.background
		(chapterColor == null) && !isCollapsed && !isDarkTheme -> Color.Transparent
		(chapterColor == null) && !isCollapsed && isDarkTheme -> Color.Transparent
		(chapterColor != null) && isCollapsed && isDarkTheme -> MaterialTheme.colorScheme.background
		(chapterColor != null) && isCollapsed && !isDarkTheme -> MaterialTheme.colorScheme.background
		(chapterColor != null) && !isCollapsed && !isDarkTheme -> chapterColor
		(chapterColor != null) && !isCollapsed && isDarkTheme -> Color(ColorUtils.blendARGB(MaterialTheme.colorScheme.background.toArgb(), chapterColor.toArgb(), 0.47f))
		else -> Color.Transparent
	}

	val contentColor = when {
		isCollapsed -> MaterialTheme.colorScheme.onBackground
		chapterColor == null -> Color.White
		isDarkTheme -> Color.White
		else -> chapterColor.getInverseBWColor()
	}

	Column(
		modifier = modifier.fillMaxWidth()
	) {
		TopAppBar(
			navigationIcon = { BackButton(colors = GenericButtonDefaults.transparentButtonColors(iconColor = contentColor)) },
			title = {
				AnimatedVisibility(
					visible = isCollapsed
				) {
					Text(
						text = chapterTitle ?: stringResource(id = R.string.untitled),
						fontStyle = if (chapterTitle.isNullOrEmpty()) FontStyle.Italic else FontStyle.Normal,
						color = contentColor
					)
				}
			},
			actions = {
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

		if (isCollapsed) {
			AnimatedVisibility(
				visible = !isSelecting,
				enter = expandVertically(tween(470)),
				exit = shrinkVertically(tween(470))
			) {
				Column(
					modifier = Modifier
						.fillMaxWidth()
						.background(MaterialTheme.colorScheme.background)
				) {
					Navigator(
						chapterPath = chapterPath.reversed(),
						defaultChapterId = defaultChapterId,
						showRoot = false,
						isVisible = true,
						onClickNavigatorChapter = { it?.also(onLoadChapter) },
					)

					GenericTabRow(
						tabItemList = listOf(
							TabItem(text = stringResource(id = R.string.notes), icon = R.drawable.ic_fa_note_duotone) { onSelectTab(0) },
							TabItem(text = stringResource(id = R.string.chapters), icon = R.drawable.ic_fa_notebook_duotone) { onSelectTab(1) },
						),
						selectedTabIndex = selectedTab,
						modifier = Modifier
							.fillMaxWidth()
							.padding(horizontal = 12.dp)
					)

					Spacer(
						modifier = Modifier
							.fillMaxWidth()
							.height(12.dp)
					)
				}
			}
		}
	}
}

@Preview
@Composable
fun ExpandedTopBar(
	chapterTitle: String? = null,
	chapterDescription: String? = null,
	chapterColor: Color? = null,
	bitmap: Bitmap? = null,
	defaultChapterId: RealmUUID? = null,
	chapterPath: List<ChapterObjectLite> = listOf(),
	selectedTab: Int = 0,
	isSelecting: Boolean = false,
	onLoadChapter: (RealmUUID) -> Unit = {},
	onSelectTab: (Int) -> Unit = {}
) {
	val context = LocalContext.current

	val isDarkTheme = LocalIsDarkTheme.current

	val containerColor by animateColorAsState(
		targetValue = (if (isDarkTheme) chapterColor?.copy(alpha = 0.47f) else chapterColor) ?: MaterialTheme.colorScheme.background,
		label = "containerColor_animation"
	)
	val contentColor by animateColorAsState(
		targetValue = when {
			isDarkTheme -> Color.White
			// If thumbnail is available
			chapterColor == null -> Color.White
			else -> chapterColor.getInverseBWColor()
		},
		label = "contentColor_animation"
	)

	Column(
		modifier = Modifier
			.fillMaxWidth()
			.background(MaterialTheme.colorScheme.background)
	) {
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
							.background(containerColor)
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

			Column(
				modifier = Modifier.padding(16.dp)
			) {
				Text(
					text = chapterTitle ?: stringResource(id = R.string.untitled),
					style = MaterialTheme.typography.headlineLarge,
					fontStyle = if (chapterTitle.isNullOrEmpty()) FontStyle.Italic else FontStyle.Normal,
					color = contentColor,
				)

				chapterDescription?.let {
					Spacer(modifier = Modifier.height(4.dp))
					Text(
						text = it,
						style = MaterialTheme.typography.bodySmall,
						color = contentColor,
						modifier = Modifier.padding(start = 2.dp)
					)
				}
			}
		}

		AnimatedVisibility(
			visible = !isSelecting,
			enter = expandVertically(tween(470)),
			exit = shrinkVertically(tween(470))
		) {
			Column(
				modifier = Modifier
					.fillMaxWidth()
					.background(MaterialTheme.colorScheme.background)
			) {

				Navigator(
					chapterPath = chapterPath.reversed(),
					defaultChapterId = defaultChapterId,
					showRoot = false,
					isVisible = true,
					onClickNavigatorChapter = { it?.let(onLoadChapter) },
				)

				GenericTabRow(
					tabItemList = listOf(
						TabItem(text = stringResource(id = R.string.notes), icon = R.drawable.ic_fa_note_duotone) { onSelectTab(0) },
						TabItem(text = stringResource(id = R.string.chapters), icon = R.drawable.ic_fa_notebook_duotone) { onSelectTab(1) },
					),
					selectedTabIndex = selectedTab,
					modifier = Modifier
						.fillMaxWidth()
						.padding(horizontal = 12.dp)
				)

				Spacer(modifier = Modifier.height(4.dp))
			}
		}
	}
}

val COLLAPSED_TOP_BAR_HEIGHT = 64.dp
val EXPANDED_TOP_BAR_HEIGHT = 256.dp