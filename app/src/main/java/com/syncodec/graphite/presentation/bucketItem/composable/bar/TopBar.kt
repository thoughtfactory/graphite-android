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
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.button.MenuButton


@Composable
fun TopBar(
	title : String?,
	isNew : Boolean,
	isLocked : Boolean,
	isFavourite : Boolean,
	onClickSave : () -> Unit,
	onClickLock : () -> Unit,
	onClickFavourite : () -> Unit,
	onClickNavigationIcon : () -> Unit
) {
	Bar(
		title = title,
		isNew = isNew,
		isLocked = isLocked,
		onClickSave = onClickSave,
		isFavourite = isFavourite,
		onClickLock = onClickLock,
		onClickFavourite = onClickFavourite,
		onClickNavigationIcon = onClickNavigationIcon
	)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
private fun Bar(
	title : String?,
	isNew : Boolean,
	isLocked : Boolean,
	isFavourite : Boolean,
	onClickSave: () -> Unit,
	onClickLock : () -> Unit,
	onClickFavourite : () -> Unit,
	onClickNavigationIcon : () -> Unit
) {
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
