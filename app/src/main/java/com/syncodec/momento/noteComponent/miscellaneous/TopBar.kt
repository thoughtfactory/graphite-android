package com.syncodec.momento.noteComponent.miscellaneous

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.syncodec.momento.R
import com.syncodec.momento.miscellaneous.ThemeUtils.Companion.tone
import com.syncodec.momento.noteComponent.NoteActivity

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TopBar(
	isViewer: Boolean,
	isSaving: Boolean,
	onAction: (NoteActivity.Action, Any?) -> Unit
) {
	SmallTopAppBar(
		navigationIcon = {
			IconButton(
				onClick = { onAction(NoteActivity.Action.FINISH, null) }
			) {
				Crossfade(targetState = isViewer) {
					if (it) {
						Icon(
							painter = painterResource(id = R.drawable.ic_back),
							contentDescription = "Back",
							tint = MaterialTheme.colorScheme.primary,
							modifier = Modifier
								.requiredSize(32.dp)
								.padding(4.dp)
						)
					} else {
						Icon(
							painter = painterResource(id = R.drawable.ic_close),
							contentDescription = "Discard",
							tint = MaterialTheme.colorScheme.primary,
							modifier = Modifier
								.requiredSize(32.dp)
								.padding(6.dp)
						)
					}
				}
			}
		},
		title = {},
		actions = {
			Button(
				onClick = {
					if (isViewer) onAction(NoteActivity.Action.EDIT_NOTE, null)
					else onAction(NoteActivity.Action.SAVE_NOTE, null)
				},
				colors = ButtonDefaults.filledTonalButtonColors(
					containerColor = MaterialTheme.colorScheme.primary,
					contentColor = MaterialTheme.colorScheme.onPrimary
				)
			) {
				Crossfade(targetState = isViewer) {
					if (it) {
						Text(
							text = "Edit",
							style = MaterialTheme.typography.bodyMedium,
							textAlign = TextAlign.Center,
							modifier = Modifier.width(40.dp)
						)

					} else {
						Text(
							text = "Save",
							style = MaterialTheme.typography.bodyMedium,
							textAlign = TextAlign.Center,
							modifier = Modifier.width(40.dp)
						)
					}
				}
			}
			AnimatedVisibility(
				visible = isViewer
			) {
				IconButton(
					onClick = { onAction(NoteActivity.Action.OPEN_MENU, null) }
				) {
					Icon(
						painter = painterResource(id = R.drawable.ic_menu),
						contentDescription = "Menu",
						tint = MaterialTheme.colorScheme.primary,
						modifier = Modifier
							.requiredSize(32.dp)
							.padding(6.dp)
					)
				}
			}
			Spacer(modifier = Modifier.width(4.dp))
		},
		colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
			containerColor = MaterialTheme.colorScheme.surface.tone(isSystemInDarkTheme(), 1),
		),
	)
}
