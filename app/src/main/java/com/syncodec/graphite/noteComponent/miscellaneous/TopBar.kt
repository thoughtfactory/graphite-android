package com.syncodec.graphite.noteComponent.miscellaneous

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.miscellaneous.ThemeUtils.Companion.tone
import com.syncodec.graphite.noteComponent.NoteActivity


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
						)
					} else {
						Icon(
							painter = painterResource(id = R.drawable.ic_close),
							contentDescription = "Discard",
							tint = MaterialTheme.colorScheme.primary,
							modifier = Modifier
						)
					}
				}
			}
		},
		title = {},
		actions = {
			Crossfade(
				targetState = isViewer,
				animationSpec = tween(300)
			) {
				if (it) {
					IconButton(
						onClick = { onAction(NoteActivity.Action.SHOW_DELETE_POPUP, null) }
					) {
						Icon(
							painter = painterResource(id = R.drawable.ic_delete),
							contentDescription = "Menu",
							tint = Color(0xFFF05945),
							modifier = Modifier
						)
					}
				} else {
					IconButton(
						onClick = { onAction(NoteActivity.Action.OPEN_METADATA, null) }
					) {
						Icon(
							painter = painterResource(id = R.drawable.ic_info),
							contentDescription = "Metadata",
							modifier = Modifier
						)
					}
				}
			}
			Spacer(modifier = Modifier.width(4.dp))
		},
		colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
			containerColor = MaterialTheme.colorScheme.surface.tone(isSystemInDarkTheme(), 1),
		),
	)
}
