package com.syncodec.graphite.presentation.note.screen

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.syncodec.graphite.presentation.common.LoadingView
import com.syncodec.graphite.presentation.note.screen.editorScreen.EditorScreen
import com.syncodec.graphite.presentation.note.screen.viewerScreen.ViewerScreen
import io.realm.kotlin.types.RealmUUID


@Preview
@Composable
fun NoteScreen(
	isEditing : Boolean? = false,
	onClickEditNote : () -> Unit = {},
	afterNoteSaved : (RealmUUID) -> Unit = {},
	discardChanges : () -> Unit = {},
	onNoteDeleted : () -> Unit = {},
	onClickBack : () -> Unit = {},
) {
	Crossfade(
		targetState = isEditing,
		animationSpec = tween(300)
	) {
		when (it) {
			true -> EditorScreen(
				afterNoteSaved = afterNoteSaved,
				onClickBack = {
					if (it) onClickBack()
					else discardChanges()
				},
			)

			false -> ViewerScreen(
				onClickEditNote = onClickEditNote,
				onNoteDeleted = onNoteDeleted,
				onClickBack = onClickBack,
			)

			null -> LoadingView()
		}
	}
}
