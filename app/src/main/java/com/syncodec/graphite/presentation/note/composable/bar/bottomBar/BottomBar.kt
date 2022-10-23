package com.syncodec.graphite.presentation.note.composable.bar.bottomBar

import androidx.compose.animation.Crossfade
import androidx.compose.runtime.Composable
import com.syncodec.graphite.presentation.note.composable.LocalCompositionIsViewing
import com.syncodec.graphite.presentation.note.composable.bar.bottomBar.editorBottomBar.EditorBottomBar


@Composable
fun BottomBar(
	onClickMetadata : () -> Unit,
	onClickLocation : () -> Unit,
	onClickAttachment : () -> Unit,
	onClickTag : () -> Unit
) {
	val isViewing = LocalCompositionIsViewing.current

	Crossfade(targetState = isViewing) {
		when (it) {
			true -> ViewerBottomBar()
			false -> EditorBottomBar(
				onClickMetadata = onClickMetadata,
				onClickLocation = onClickLocation,
				onClickAttachment = onClickAttachment,
				onClickTag = onClickTag
			)

			null -> Unit
		}
	}
}
