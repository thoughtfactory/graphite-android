package com.syncodec.graphite.attachmentComponent.miscellaneous

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.attachmentComponent.AttachmentActivity

@OptIn(ExperimentalMaterialApi::class, ExperimentalAnimationApi::class)
@Composable
fun TopBar(
	isSelected: Boolean,
	selectedItemSize: Int,
	isViewer: Boolean,
	isFromNote: Boolean,
	onAction: (AttachmentActivity.Action, Any?) -> Unit
) {
	Column(modifier = Modifier.fillMaxWidth()) {
		Crossfade(targetState = isSelected) {
			if (it) {
				SmallTopAppBar(
					title = {
						Text(
							text = if (selectedItemSize == 0) "Select items" else if (selectedItemSize == 1) "1 item selected" else "$selectedItemSize items selected",
							modifier = Modifier,
							style = MaterialTheme.typography.titleMedium,
							color = MaterialTheme.colorScheme.onSurface
						)
					},
					actions = {
						IconButton(
							onClick = { onAction(AttachmentActivity.Action.MENU, null) }
						) {
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
					colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
						containerColor = MaterialTheme.colorScheme.surface,
					),
				)
			} else {
				SmallTopAppBar(
					navigationIcon = {
						IconButton(
							onClick = { onAction(AttachmentActivity.Action.BACK, null) }
						) {
							Icon(
								painter = painterResource(id = R.drawable.ic_back),
								contentDescription = "Back",
								tint = MaterialTheme.colorScheme.onSurface,
								modifier = Modifier
									.requiredSize(32.dp)
									.padding(4.dp)
							)
						}
					},
					title = {
						Text(
							text = "Attachment",
							modifier = Modifier,
							style = MaterialTheme.typography.titleMedium,
							color = MaterialTheme.colorScheme.onSurface
						)
					},
					actions = {
						AnimatedVisibility(
							visible = isViewer && !isFromNote,
							enter = fadeIn(tween(300)) + scaleIn(tween(300)),
							exit = fadeOut(tween(300)) + scaleOut(tween(300))
						) {
							IconButton(
								onClick = { onAction(AttachmentActivity.Action.OPEN_NOTE, null) }
							) {
								Icon(
									painter = painterResource(id = R.drawable.ic_note),
									contentDescription = "Open note",
									tint = MaterialTheme.colorScheme.onSurface,
									modifier = Modifier
										.requiredSize(32.dp)
										.padding(4.dp)
								)
							}
						}
					},
					colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
						containerColor = MaterialTheme.colorScheme.surface,
					),
				)
			}
		}
	}
}
