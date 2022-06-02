package com.syncodec.graphite.noteComponent.screen

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
import com.google.android.gms.maps.model.LatLng
import com.syncodec.graphite.custom.LoadingView
import com.syncodec.graphite.custom.richText.RichTextEditor
import com.syncodec.graphite.database.note.NoteDbEntry
import com.syncodec.graphite.konstant.Status
import com.syncodec.graphite.noteComponent.NoteActivity
import com.syncodec.graphite.noteComponent.miscellaneous.AddressCard
import com.syncodec.graphite.noteComponent.toolbar.EditorToolbar

@Composable
fun NoteEditorScreen(
	richTextEditor: RichTextEditor,
	userTimestamp: Long,
	latLng: LatLng?,
	address: String?,
	isFavourite: Boolean,
	isArchived: Boolean,
	isLocked: Boolean,
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
							latLng = latLng,
							address = address,
							onAction = onAction
						)
						EditorToolbar(
							richTextEditor = richTextEditor,
							userTimestamp = userTimestamp,
							isFavourite = isFavourite,
							isArchive = isArchived,
							isLocked = isLocked
						) { onAction(it, null) }
					}
				}
			}
			Status.ERROR -> null
		}
	}
}
