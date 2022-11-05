package com.syncodec.graphite.presentation.bucketItem2.composable.bar

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
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.bucketItem2.composable.LocalCompositionIsNew
import com.syncodec.graphite.presentation.bucketItem2.composable.LocalCompositionTitle
import com.syncodec.graphite.presentation.bucketItem2.composable.LocalCompositionIsFavourite
import com.syncodec.graphite.presentation.bucketItem2.composable.LocalCompositionIsLocked
import com.syncodec.graphite.presentation.bucketItem2.composable.LocalCompositionOnClickFavourite
import com.syncodec.graphite.presentation.bucketItem2.composable.LocalCompositionOnClickLock
import com.syncodec.graphite.presentation.bucketItem2.composable.LocalCompositionOnClickNavigationIcon
import com.syncodec.graphite.presentation.bucketItem2.composable.LocalCompositionOnClickSave
import com.syncodec.graphite.presentation.common.button.MenuButton


@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
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
				when(it) {
					true -> {
						Row(
							modifier = Modifier
						) {
							Button(onClick = onClickSave) {
								Text(text = "Save")
							}
							Spacer(modifier = Modifier.width(4.dp))
						}
					}
					 false -> {
						 Row(
							 modifier = Modifier
						 ) {
							 MenuButton(
								 icon = if (isLocked == true) R.drawable.ic_lock_close else R.drawable.ic_lock_open,
								 contentDescription = if (isLocked == true) "Locked" else "Not locked",
								 tint = MaterialTheme.colorScheme.onBackground,
								 isChecked = isLocked == true,
								 isEnabled = true,
								 onClick = onClickLock
							 )
							 MenuButton(
								 icon = R.drawable.ic_favourite,
								 contentDescription = "Favourite",
								 tint = MaterialTheme.colorScheme.onBackground,
								 isChecked = isFavourite == true,
								 isEnabled = true,
								 onClick = onClickFavourite,
							 )
						 }
					 }
					null -> null
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
