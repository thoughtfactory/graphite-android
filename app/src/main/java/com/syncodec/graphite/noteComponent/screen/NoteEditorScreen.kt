package com.syncodec.graphite.noteComponent.screen

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.maps.model.LatLng
import com.syncodec.graphite.custom.ErrorView
import com.syncodec.graphite.custom.LoadingView
import com.syncodec.graphite.konstant.Status
import com.syncodec.graphite.miscellaneous.LocalRichTextEditor
import com.syncodec.graphite.noteComponent.NoteActivity
import com.syncodec.graphite.noteComponent.miscellaneous.AddressCard


@Composable
fun NoteEditorScreen(
	latLng: LatLng?,
	address: String?,
	addressState: NoteActivity.AddressState,
	showAddressCard: Boolean,
	status: Status,
	onAction: (NoteActivity.Action, Any?) -> Unit
) {
	val richTextEditor = LocalRichTextEditor.current
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
					}
				}
			}
			Status.ERROR -> ErrorView()
		}
	}
}
