package com.syncodec.momento.noteComponent.screen

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.syncodec.momento.custom.LoadingView
import com.syncodec.momento.custom.richText.RichTextEditor
import com.syncodec.momento.database.note.NoteDbEntry
import com.syncodec.momento.konstant.Status
import com.syncodec.momento.noteComponent.NoteActivity
import com.syncodec.momento.noteComponent.miscellaneous.AddressCard
import com.syncodec.momento.noteComponent.toolbar.EditorToolbar

@OptIn(
	ExperimentalMaterial3Api::class,
	ExperimentalMaterialApi::class,
	ExperimentalPermissionsApi::class
)
@Composable
fun NoteEditorScreen(
	richTextEditor: RichTextEditor,
	noteDbEntry: NoteDbEntry?,
	addressState: NoteActivity.AddressState,
	showAddressCard: Boolean,
	status: Status,
	onAction: (NoteActivity.Action, Any?) -> Unit
) {
	val isReady by richTextEditor.isReady

	LaunchedEffect(key1 = isReady) {
		if (status != Status.LOADED) onAction(NoteActivity.Action.EDITOR_READY, null)
	}

	Crossfade(
		targetState = status,
		animationSpec = tween(600),
		modifier = Modifier.fillMaxSize()
	) {
		when (it) {
			Status.INIT -> LoadingView()
			Status.LOADING -> LoadingView()
			Status.LOADED -> {
				Surface(
					modifier = Modifier.fillMaxSize(),
					color = MaterialTheme.colorScheme.background
				) {
					Column(
						modifier = Modifier.fillMaxSize()
					) {
						AndroidView(
							factory = { richTextEditor },
							update = { viewer -> },
							modifier = Modifier
								.fillMaxWidth()
								.weight(1f)
						)
						AddressCard(
							addressState = addressState,
							showAddressCard = showAddressCard,
							address = noteDbEntry?.address,
							latLng = noteDbEntry?.latLng,
							onAction = onAction
						)
						EditorToolbar(
							richTextEditor = richTextEditor,
							userTimestamp = noteDbEntry?.userTimestamp ?: -1,
							isFavourite = noteDbEntry?.isFavourite == true,
							isArchive = noteDbEntry?.isArchived == true,
							isLocked = noteDbEntry?.isLocked == true
						) { onAction(it, null) }
					}
				}
			}
			Status.ERROR -> null
		}
	}
}
