package com.syncodec.graphite.presentation.bucketItem.composable.bar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.button.BackButton
import com.syncodec.graphite.presentation.common.button.DeleteButton
import com.syncodec.graphite.presentation.common.button.FavouriteButton
import com.syncodec.graphite.presentation.common.button.LockButton


@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun TopBar(
	isNew : Boolean = false,
	isFavourite : Boolean = false,
	isLocked : Boolean = false,
	onClickSave : () -> Unit = {},
	onClickDelete : () -> Unit = {},
	onClickFavourite : () -> Unit = {},
	onClickLock : () -> Unit = {},
	onClickBack : () -> Unit = {}
) {
	TopAppBar(
		navigationIcon = { BackButton(onClick = onClickBack) },
		title = {},
		actions = {
			AnimatedVisibility(
				visible = !isNew,
				enter = expandHorizontally(tween(470)),
				exit = shrinkHorizontally(tween(470)),
				label = "isNew_animation"
			) {
				Row {
					DeleteButton(onClick = onClickDelete)

					LockButton(
						isLocked = isLocked,
						onClick = onClickLock,
					)

					FavouriteButton(
						isFavourite = isFavourite,
						onClick = onClickFavourite,
					)
				}
			}

			AnimatedVisibility(
				visible = isNew,
				enter = expandHorizontally(tween(470)),
				exit = shrinkHorizontally(tween(470)),
				label = "isNew_animation"
			) {
				Row {
					Button(
						shape = MaterialTheme.shapes.medium,
						onClick = onClickSave
					) {
						Text(text = stringResource(id = R.string.save))
					}
					Spacer(modifier = Modifier.width(4.dp))
				}
			}
		},
		colors = TopAppBarDefaults.topAppBarColors(
			containerColor = MaterialTheme.colorScheme.background,
			navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
			titleContentColor = MaterialTheme.colorScheme.onSurface,
			actionIconContentColor = MaterialTheme.colorScheme.onSurface,
		)
	)
}
