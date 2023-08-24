package com.syncodec.graphite.presentation.bucketItem.composable.bar

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.with
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.button.GenericButton


@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun TopBar(
	isSaved : Boolean = false,
	isFavourite : Boolean = false,
	isLocked : Boolean = false,
	onClickSave : () -> Unit = {},
	onClickLocalOnly : () -> Unit = {},
	onClickFavourite : () -> Unit = {},
	onClickLock : () -> Unit = {},
	onClickBack : () -> Unit = {}
) {
	TopAppBar(
		navigationIcon = {
			GenericButton(
				icon = R.drawable.ic_back,
				tooltip = "Back",
				onClick = onClickBack
			)
		},
		title = {},
		actions = {
			AnimatedContent(
				targetState = isSaved,
				transitionSpec = { fadeIn(tween(300)) + scaleIn(tween(300), 0.71f) with fadeOut(tween(300)) + fadeOut(tween(300), 0.71f) }, 
				label = "isSaved_animation"
			) {
				when (it) {
					true -> {
						Row(
							modifier = Modifier
						) {
							GenericButton(
								icon = if (isLocked) R.drawable.ic_lock_close else R.drawable.ic_lock_open,
								tooltip = if (isLocked) "Locked" else "Not locked",
								checked = isLocked,
								shape = MaterialTheme.shapes.medium,
								onClick = onClickLock
							)
							GenericButton(
								icon = R.drawable.ic_favourite,
								tooltip = if (isFavourite) "Favourite" else "Not favourite",
								checked = isFavourite,
								shape = MaterialTheme.shapes.medium,
								onClick = onClickFavourite,
							)
						}
					}

					false -> {
						Row(
							modifier = Modifier
						) {
							Button(
								shape = MaterialTheme.shapes.medium,
								onClick = onClickSave,
							) {
								Text(text = "Save")
							}
							Spacer(modifier = Modifier.width(4.dp))
						}
					}
				}
			}
		},
		colors = TopAppBarDefaults.topAppBarColors(
			containerColor = MaterialTheme.colorScheme.background,
			navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
		)
	)
}
