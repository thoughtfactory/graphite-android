package com.syncodec.graphite.presentation.note.screen

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.syncodec.graphite.presentation.common.LoadingView
import com.syncodec.graphite.presentation.common.richText.RichTextEditor
import com.syncodec.graphite.presentation.note.screen.editorScreen.EditorScreen
import com.syncodec.graphite.presentation.note.screen.viewerScreen.ViewerScreen
import io.realm.kotlin.types.RealmUUID


@Preview
@Composable
fun NoteScreen(
	editor : RichTextEditor,
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
				editor = editor,
				afterNoteSaved = afterNoteSaved,
				onClickBack = {
					if (it) onClickBack()
					else discardChanges()
				},
			)

			false -> ViewerScreen(
				editor = editor,
				onClickEditNote = onClickEditNote,
				onNoteDeleted = onNoteDeleted,
				onClickBack = onClickBack,
			)

			null -> LoadingView()
		}
	}
}
