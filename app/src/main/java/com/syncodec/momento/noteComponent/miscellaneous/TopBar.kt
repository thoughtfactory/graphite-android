package com.syncodec.momento.noteComponent.miscellaneous

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.syncodec.momento.noteComponent.NoteActivity
import compose.icons.TablerIcons
import compose.icons.tablericons.*

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TopBar(
	isViewer: Boolean,
	onClick: (NoteActivity.Click) -> Unit
) {
	Box(
		modifier = Modifier
			.fillMaxWidth()
			.height(64.dp)
			.background(MaterialTheme.colorScheme.secondaryContainer)
	) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier
				.fillMaxWidth()
				.fillMaxHeight()
				.padding(8.dp)
		) {
			IconButton(
				onClick = { onClick(NoteActivity.Click.TOP_BAR_PRIMARY) },
			) {
				Icon(
					imageVector = if (isViewer) TablerIcons.ArrowLeft else TablerIcons.Check,
					contentDescription = if (isViewer) "Back" else "Save",
					tint = MaterialTheme.colorScheme.onSecondaryContainer,
				)
			}

			Spacer(modifier = Modifier.weight(1f))

			Crossfade(targetState = isViewer) {
				if (it) {
					IconButton(
						onClick = { onClick(NoteActivity.Click.TOP_BAR_QUATERNARY) },
					) {
						Icon(
							imageVector = TablerIcons.Pencil,
							contentDescription = "Edit",
							tint = MaterialTheme.colorScheme.onSecondaryContainer,
						)
					}
				}
			}

			IconButton(
				onClick = { onClick(NoteActivity.Click.TOP_BAR_TERTIARY) },
			) {
				Icon(
					imageVector = TablerIcons.InfoCircle,
					contentDescription = "Metadata",
					tint = MaterialTheme.colorScheme.onSecondaryContainer,
				)
			}

			IconButton(
				onClick = { onClick(NoteActivity.Click.TOP_BAR_SECONDARY) },
			) {
				Icon(
					imageVector = TablerIcons.Dots,
					contentDescription = "Menu",
					tint = MaterialTheme.colorScheme.onSecondaryContainer,
				)
			}
		}
	}
}
