package com.syncodec.graphite.presentation.note

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.syncodec.graphite.presentation.note.screen.NoteScreen
import com.syncodec.graphite.presentation.note.screen.editorScreen.EditorScreenViewModel
import com.syncodec.graphite.presentation.note.screen.viewerScreen.ViewerScreenViewModel
import com.syncodec.graphite.presentation.ui.BaseContent
import com.syncodec.graphite.utils.Extra
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel


class NoteActivity : ComponentActivity() {

	private val viewModel : NoteViewModel by viewModel()
	private val editorScreenViewModel : EditorScreenViewModel by viewModel()
	private val viewerScreenViewModel : ViewerScreenViewModel by viewModel()

	override fun onCreate(savedInstanceState : Bundle?) {
		super.onCreate(savedInstanceState)

		try {
			val hasFilter = intent.hasExtra(Extra.Companion.Extra.Filter.name)

			if (hasFilter) {
				when (intent.getStringExtra(Extra.Companion.Extra.Filter.name)?.let { Extra.Companion.Filter.valueOf(it) }) {
					Extra.Companion.Filter.SingleRead -> singleRead()
					Extra.Companion.Filter.READ_CHAPTER -> finish()
					null -> finish()
				}
			} else {
				finish()
			}

		} catch (e : Exception) {
			finish()
		}

		setContent {
			BaseContent {
				val systemUiController = rememberSystemUiController()
				systemUiController.setStatusBarColor(MaterialTheme.colorScheme.background)

				val isEditing by viewModel.isEditing.collectAsState()
				Log.i("npr71", "isEditing: $isEditing")

				NoteScreen(
					isEditing = isEditing,
					onClickEditNote = { viewModel.isEditing.tryEmit(true) },
					afterNoteSaved = { noteId ->
						viewerScreenViewModel.loadNote(noteId = noteId)
						lifecycleScope.launch {
							viewerScreenViewModel.isReady.collect { isReady ->
								if (isReady) {
//									editorScreenViewModel.loadNote(noteId = noteId)
									viewModel.isEditing.tryEmit(false)
									cancel()
								}
							}
						}
					},
					onClickBack = {},
				)
			}
		}
	}

	private fun singleRead() {
		val hasIsNew = intent.hasExtra(Extra.Companion.Extra.IsNew.name)

		if (hasIsNew) {
			val isNew = intent.getBooleanExtra(Extra.Companion.Extra.IsNew.name, false)
			if (isNew) {
				Toast.makeText(this, "New note", Toast.LENGTH_SHORT).show()

				val hasParentId = intent.hasExtra(Extra.Companion.Extra.ParentId.name)
				if (hasParentId) {
					val parentId = intent.getByteArrayExtra(Extra.Companion.Extra.ParentId.name)?.let { RealmUUID.from(it) }
					parentId?.let { editorScreenViewModel.initNewNote(parentId = it) } ?:
					Toast.makeText(this, "Parent ID is null", Toast.LENGTH_SHORT).show()

					lifecycleScope.launch {
						editorScreenViewModel.isReady.collect { if (it) viewModel.isEditing.tryEmit(true) }
					}
				} else {
					Toast.makeText(this, "Parent ID is null", Toast.LENGTH_SHORT).show()
				}

			} else {
				val hasNoteId = intent.hasExtra(Extra.Companion.Extra.NoteId.name)
				if (hasNoteId) {
					val noteId = intent.getByteArrayExtra(Extra.Companion.Extra.NoteId.name)?.let { RealmUUID.from(it) }

					noteId?.let {
						Toast.makeText(this, "Note ID is $noteId", Toast.LENGTH_SHORT).show()
						viewerScreenViewModel.loadNote(noteId = it)
						editorScreenViewModel.loadNote(noteId = it)
						lifecycleScope.launch {
							combine(
								viewerScreenViewModel.isReady,
								editorScreenViewModel.isReady
							) { isNoteViewerReady, isNoteEditorReady ->
								isNoteViewerReady && isNoteEditorReady
							}.collect { isReady ->
								if (isReady) {
									viewModel.isEditing.tryEmit(false)
									cancel()
								}
							}
						}
					} ?: run {
						Log.i("npr71", "noteId == null")
						Toast.makeText(this, "Note ID is null", Toast.LENGTH_SHORT).show()
						finish()
					}
				} else {
					Log.i("npr71", "hasNoteId == false")
					Toast.makeText(this, "Note ID is null", Toast.LENGTH_SHORT).show()
					finish()
				}
			}
		} else {
			Log.i("npr71", "hasIsNew == false")
			Toast.makeText(this, "Is new is null", Toast.LENGTH_SHORT).show()
			finish()
		}
	}
}
