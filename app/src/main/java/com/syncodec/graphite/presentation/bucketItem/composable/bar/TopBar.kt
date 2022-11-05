package com.syncodec.graphite.presentation.bucketItem.composable.bar

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.with
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.bucketItem.composable.LocalCompositionIsFavourite
import com.syncodec.graphite.presentation.bucketItem.composable.LocalCompositionIsLocked
import com.syncodec.graphite.presentation.bucketItem.composable.LocalCompositionIsNew
import com.syncodec.graphite.presentation.bucketItem.composable.LocalCompositionOnClickFavourite
import com.syncodec.graphite.presentation.bucketItem.composable.LocalCompositionOnClickLock
import com.syncodec.graphite.presentation.bucketItem.composable.LocalCompositionOnClickNavigationIcon
import com.syncodec.graphite.presentation.bucketItem.composable.LocalCompositionOnClickSave
import com.syncodec.graphite.presentation.bucketItem.composable.LocalCompositionTitle
import com.syncodec.graphite.presentation.common.button.MenuButton


@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Preview
@Composable
fun TopBar() {

	val title = LocalCompositionTitle.current
	val onClickNavigationIcon = LocalCompositionOnClickNavigationIcon.current
	val isNew = LocalCompositionIsNew.current
	val isLocked = LocalCompositionIsLocked.current
	val isFavourite = LocalCompositionIsFavourite.current
	val onClickSave = LocalCompositionOnClickSave.current
	val onClickLock = LocalCompositionOnClickLock.current
	val onClickFavourite = LocalCompositionOnClickFavourite.current

	TopAppBar(
		navigationIcon = {
			MenuButton(
				icon = R.drawable.ic_back,
				contentDescription = "Back",
				tint = MaterialTheme.colorScheme.onBackground,
				onClick = onClickNavigationIcon
			)
		},
		title = {
			Crossfade(
				targetState = title,
				animationSpec = tween(300)
			) {
				Text(
					text = it ?: "",
					color = MaterialTheme.colorScheme.onBackground,
					fontWeight = FontWeight.Bold
				)
			}
		},
		actions = {
			AnimatedContent(
				targetState = isNew,
				transitionSpec = { fadeIn(tween(300)) + scaleIn(tween(300), 0.71f) with fadeOut(tween(300)) + fadeOut(tween(300), 0.71f) }
			) {
				if (it) {
					Row(modifier = Modifier) {
						Button(onClick = onClickSave) {
							Text(text = "Save")
						}
						Spacer(modifier = Modifier.width(8.dp))
					}
				} else {
					Row(modifier = Modifier) {
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
					}
				}
			}
		},
		colors = TopAppBarDefaults.smallTopAppBarColors(
			containerColor = MaterialTheme.colorScheme.background,
			navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
			titleContentColor = MaterialTheme.colorScheme.onSurface,
			actionIconContentColor = MaterialTheme.colorScheme.onSurface,
		)
	)
}
