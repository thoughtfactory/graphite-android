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
	onClickBack : () -> Unit = {},
) {
	Crossfade(
		targetState = isEditing,
		animationSpec = tween(300)
	) {
		when (it) {
			true -> EditorScreen(
				afterNoteSaved = afterNoteSaved,
			)
			false -> ViewerScreen(
				onClickMenu = {},
				onClickEditNote = onClickEditNote,
				onClickBack = onClickBack,
			)

			null -> LoadingView()
		}
	}
}
