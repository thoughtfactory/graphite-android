package com.syncodec.graphite.presentation.notebook.composable.bar

import androidx.annotation.FloatRange
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.util.lerp
import coil.compose.SubcomposeAsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.baec23.ludwig.morpher.model.morpher.VectorSource
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.local.ChapterObjectLite
import com.syncodec.graphite.presentation.base.ANIMATION_DURATION_MILLIS
import com.syncodec.graphite.presentation.base.FavouriteContainer
import com.syncodec.graphite.presentation.base.LocalIsDarkTheme
import com.syncodec.graphite.presentation.base.LockClosedContainer
import com.syncodec.graphite.presentation.common.animation.AnimatedText
import com.syncodec.graphite.presentation.common.button.BackButton
import com.syncodec.graphite.presentation.common.button.FavouriteButton
import com.syncodec.graphite.presentation.common.button.GenericAnimatedMorphButton
import com.syncodec.graphite.presentation.common.button.GenericButtonDefaults
import com.syncodec.graphite.presentation.common.button.LockButton
import com.syncodec.graphite.presentation.common.button.MenuButton
import com.syncodec.graphite.presentation.common.component.chapter.Navigator
import com.syncodec.graphite.presentation.common.component.composable.DotSeparator
import com.syncodec.graphite.utils.decodeBase64ToBitmap
import com.syncodec.graphite.utils.getInverseBWColor
import io.realm.kotlin.types.RealmUUID

val COLLAPSED_TOP_BAR_HEIGHT = 64.dp
val EXPANDED_TOP_BAR_HEIGHT = 260.dp

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun TopBar(
	modifier: Modifier = Modifier,
	chapterTitle: String? = null,
	isFavourite: Boolean = false,
	isLocked: Boolean = false,
	@FloatRange(from = 0.0, to = 1.0) offset: Float = 0f,
	chapterColor: Color? = null,
	onClickFavourite: () -> Unit = {},
	onClickLock: () -> Unit = {},
	onClickMenuButton: () -> Unit = {},
) {
	val isDarkTheme = LocalIsDarkTheme.current

	val contentColor by animateColorAsState(
		targetValue = when {
			isDarkTheme -> MaterialTheme.colorScheme.onBackground
			chapterColor == null -> MaterialTheme.colorScheme.onBackground
			else -> chapterColor.getInverseBWColor()
		},
		label = "contentColor_animation"
	)

	Column(
		modifier = modifier.fillMaxWidth()
	) {
		TopAppBar(
			navigationIcon = { BackButton(colors = GenericButtonDefaults.transparentButtonColors(iconColor = contentColor)) },
			title = {
				AnimatedVisibility(
					visible = offset > 0.5f,
					enter = fadeIn() + slideInVertically { it / 2 },
					exit = fadeOut() + slideOutVertically { it / 2 },
					label = "title_visibility"
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
				containerColor = Color.Transparent,
				navigationIconContentColor = contentColor,
				titleContentColor = contentColor,
				actionIconContentColor = contentColor,
			),
		)
	}
}
