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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.syncodec.momento.custom.LoadingView
import com.syncodec.momento.custom.richText.RichTextEditor
import com.syncodec.momento.database.note.NoteDbEntry
import com.syncodec.momento.konstant.Status
import com.syncodec.momento.miscellaneous.DataStore
import com.syncodec.momento.miscellaneous.logger
import com.syncodec.momento.miscellaneous.toHexString
import com.syncodec.momento.noteComponent.NoteActivity
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
	locationPermissionState: PermissionState,
	status: Status,
	onAction: (NoteActivity.Action, Any?) -> Unit
) {
	val context = LocalContext.current
	val typography by DataStore(context).getTypography.collectAsState(initial = null)

	val textColor = MaterialTheme.colorScheme.onBackground.toHexString()
	val isReady by richTextEditor.isReady

	LaunchedEffect(key1 = isReady && typography != null) {
		if (status != Status.LOADED) {
			when (typography) {
				0 -> richTextEditor.exec("editor.setBaseFontFamily(\"overlock\");")
				1 -> richTextEditor.exec("editor.setBaseFontFamily(\"source_sans_pro\");")
				2 -> richTextEditor.exec("editor.setBaseFontFamily(\"ubuntu\");")
				3 -> richTextEditor.exec("editor.setBaseFontFamily('atwriter');")
				else -> richTextEditor.exec("editor.setBaseFontFamily(\"source_sans_pro\");")
			}
			onAction(NoteActivity.Action.EDITOR_READY, null)
		}
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
				Surface(modifier = Modifier.fillMaxSize()) {
					Column(modifier = Modifier.fillMaxSize()) {
						AndroidView(
							factory = { richTextEditor },
							update = { viewer -> },
							modifier = Modifier
								.fillMaxWidth()
								.weight(1f)
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
