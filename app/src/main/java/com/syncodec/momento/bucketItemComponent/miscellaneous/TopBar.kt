package com.syncodec.momento.bucketItemComponent.miscellaneous

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.syncodec.momento.R
import com.syncodec.momento.bucketItemComponent.BucketItemActivity
import compose.icons.tablericons.*

@OptIn(ExperimentalMaterialApi::class, androidx.compose.animation.ExperimentalAnimationApi::class)
@Composable
fun TopBar(
	isNew: Boolean,
	onAction: (BucketItemActivity.Action, Any?) -> Unit,
) {
	Bar(
		isNew = isNew,
		onAction = onAction
	)
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun Bar(
	isNew: Boolean,
	onAction: (BucketItemActivity.Action, Any?) -> Unit
) {
	SmallTopAppBar(
		navigationIcon = {
			IconButton(onClick = { onAction(BucketItemActivity.Action.TOP_BAR_PRIMARY, null) }) {
				AnimatedContent(
					targetState = isNew,
					transitionSpec = {
						(scaleIn(tween(600), 0f) with scaleOut(tween(600), 1f)).using(SizeTransform(clip = false))
					}
				) {
					if (isNew) {
						Icon(
							painter = painterResource(id = R.drawable.ic_done),
							contentDescription = "Save",
							tint = MaterialTheme.colorScheme.onSurface,
							modifier = Modifier
								.requiredSize(32.dp)
								.padding(4.dp)
						)
					} else {
						Icon(
							painter = painterResource(id = R.drawable.ic_back),
							contentDescription = "Back",
							tint = MaterialTheme.colorScheme.onSurface,
							modifier = Modifier
								.requiredSize(32.dp)
								.padding(4.dp)
						)
					}
				}
			}
		},
		title = {},
		actions = {
			IconButton(onClick = { onAction(BucketItemActivity.Action.MENU, null) }) {
				Icon(
					painter = painterResource(id = R.drawable.ic_menu),
					contentDescription = "Menu",
					tint = MaterialTheme.colorScheme.onSurface,
					modifier = Modifier
						.requiredSize(32.dp)
						.padding(4.dp)
				)
			}
		},
		colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
	)
}
