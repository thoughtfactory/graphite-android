package com.syncodec.graphite.presentation.note

import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import com.syncodec.graphite.presentation.common.richText.RichTextEditor
import com.syncodec.graphite.presentation.note.screen.NoteScreen
import com.syncodec.graphite.presentation.note.screen.editorScreen.EditorScreenViewModel
import com.syncodec.graphite.presentation.note.screen.viewerScreen.ViewerScreenViewModel
import com.syncodec.graphite.presentation.ui.BaseContent
import com.syncodec.graphite.utils.Extra
import com.syncodec.graphite.utils.dataStore.DataStoreInstance
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel


class NoteActivity : ComponentActivity() {

	private val isEditing : MutableStateFlow<Boolean> = MutableStateFlow(false)
	private val editorScreenViewModel : EditorScreenViewModel by viewModel()
	private val viewerScreenViewModel : ViewerScreenViewModel by viewModel()
	private val editor : RichTextEditor by lazy {
		RichTextEditor(this).apply {
			lifecycleScope.launch(Dispatchers.IO) { loadEditor() }
		}
	}

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

		val dataStoreInstance = DataStoreInstance(this)

		setContent {
			BaseContent {

				val containerColor = MaterialTheme.colorScheme.background
				val contentColor = MaterialTheme.colorScheme.onBackground
				val typography by dataStoreInstance.getTypography.collectAsState(initial = null)

				val isEditorReady by editor.isReady.collectAsState(initial = false)

				LaunchedEffect(key1 = contentColor, key2 = typography, key3 = isEditorReady) {
					if (isEditorReady) {
						editor.setContentColor(contentColor)
						editor.setTypography(typography ?: "PT Mono")
						editorScreenViewModel.kitKatContent?.toJsonString()?.let {  editor.setContent(it) }
					}
				}

				this.onBackPressedDispatcher.addCallback {
					try {
						(window.decorView.rootView as ViewGroup).removeAllViews()
					} catch (e : Exception) {
					}
					finish()
				}

				val isEditing by isEditing.collectAsState()

				NoteScreen(
					editor = editor,
					isEditing = isEditing,
					onClickEditNote = { this.isEditing.tryEmit(true) },
					afterNoteSaved = { noteId ->
						viewerScreenViewModel.loadNote(noteId = noteId)
						lifecycleScope.launch {
							viewerScreenViewModel.isReady.collect { isReady ->
								if (isReady) {
//									editorScreenViewModel.loadNote(noteId = noteId)
									this@NoteActivity.isEditing.tryEmit(false)
									cancel()
								}
							}
						}
					},
					discardChanges = { this.isEditing.tryEmit(false) },
					onNoteDeleted = {
						try {
							(window.decorView.rootView as ViewGroup).removeAllViews()
						} catch (e : Exception) {
						}
						finish()
					},
					onClickBack = {
						try {
							(window.decorView.rootView as ViewGroup).removeAllViews()
						} catch (e : Exception) {
						}
						finish()
					},
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
					parentId?.let { editorScreenViewModel.initNewNote(parentId = it) } ?: Toast.makeText(this, "Parent ID is null", Toast.LENGTH_SHORT).show()

					lifecycleScope.launch {
						editorScreenViewModel.isReady.collect { if (it) this@NoteActivity.isEditing.tryEmit(true) }
					}
				} else {
					Toast.makeText(this, "Parent ID is null", Toast.LENGTH_SHORT).show()
				}

			} else {
				val hasNoteId = intent.hasExtra(Extra.Companion.Extra.NoteId.name)
				if (hasNoteId) {
					val noteId = intent.getByteArrayExtra(Extra.Companion.Extra.NoteId.name)?.let { RealmUUID.from(it) }

					noteId?.let {
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
									this@NoteActivity.isEditing.tryEmit(false)
									cancel()
								}
							}
						}
					} ?: run {
						Toast.makeText(this, "Error opening note", Toast.LENGTH_SHORT).show()
						finish()
					}
				} else {
					Toast.makeText(this, "Error opening note", Toast.LENGTH_SHORT).show()
					finish()
				}
			}
		} else {
			Toast.makeText(this, "Error opening note", Toast.LENGTH_SHORT).show()
			finish()
		}
	}

	override fun onRestoreInstanceState(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
		super.onRestoreInstanceState(savedInstanceState, persistentState)
		Log.d("npr71", "onRestoreInstanceState")
	}

	override fun onDestroy() {
		super.onDestroy()
		try {
			editor?.destroy()
		} catch (e : Exception) {
		}
		try {
			(window.decorView.rootView as ViewGroup).removeAllViews()
		} catch (e : Exception) {
		}
	}
}
