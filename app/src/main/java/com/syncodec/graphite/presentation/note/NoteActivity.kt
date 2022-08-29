package com.syncodec.graphite.presentation.note

import android.os.Bundle
import android.util.Log
import android.window.OnBackInvokedDispatcher
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.core.os.BuildCompat
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.syncodec.graphite.presentation.custom.dialog.DeleteDialog
import com.syncodec.graphite.presentation.custom.dialog.DiscardDialog
import com.syncodec.graphite.presentation.custom.richText.RichTextEditor
import com.syncodec.graphite.presentation.custom.richText.rememberRichTextEditorWithLifecycle
import com.syncodec.graphite.presentation.ui.BaseContent
import com.syncodec.graphite.utils.Extra
import com.syncodec.graphite.utils.LocalRichTextEditor
import com.syncodec.graphite.utils.LocalCompositionPremium
import io.realm.kotlin.types.ObjectId


class NoteActivity : ComponentActivity() {

	private val viewModel by viewModels<NoteViewModel>()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		val hasIsNew = intent.hasExtra(Extra.Companion.Constant.IS_NEW.name)
		val hasChapterId = intent.hasExtra(Extra.Companion.Constant.CHAPTER_ID.name)
		val hasNoteId = intent.hasExtra(Extra.Companion.Constant.NOTE_ID.name)
		val hasFilter = intent.hasExtra(Extra.Companion.Constant.FILTER.name)

		if (hasIsNew && hasChapterId && hasFilter) {
			val isNew = intent.getBooleanExtra(Extra.Companion.Constant.IS_NEW.name, true)
			val chapterId = intent.getStringExtra(Extra.Companion.Constant.CHAPTER_ID.name)?.let { ObjectId.from(it) }
			val filterId = intent.getIntExtra(Extra.Companion.Constant.FILTER.name, -1)

			if (filterId < 0) {
				Log.i("npr71", "filterId not provided")
				finish()
			} else {
				val filter = Extra.Companion.Filter.values()[filterId]
				if (isNew && hasFilter) {
					if (chapterId != null) {
						viewModel.initNewData(chapterId, filter)
					} else {
						Log.i("npr71", "chapterId not provided")
						finish()
					}
				} else if (hasNoteId && hasFilter) {
					val noteId = intent.getStringExtra(Extra.Companion.Constant.NOTE_ID.name)?.let { ObjectId.from(it) }

					if (filterId == -1 || chapterId == null || noteId == null) {
						Log.i("npr71", "filterId is -1 || chapterId == null || noteId == null")
						finish()
					} else {
						viewModel.loadAndViewData(chapterId, noteId, filter)
					}

				} else {
					Log.i("npr71", "!isNew || !hasNoteId || !hasFilter")
					finish()
				}
			}
		} else {
			Log.i("npr71", "super finish")
			finish()
		}

		setContent {
			BaseContent {
				val systemUiController = rememberSystemUiController()
				systemUiController.setStatusBarColor(MaterialTheme.colorScheme.primary)
				systemUiController.setNavigationBarColor(MaterialTheme.colorScheme.surface)

				val richTextEditor = rememberRichTextEditorWithLifecycle()

				richTextEditor.setOnSaveData(listener = object : RichTextEditor.OnSaveDataListener {
					override fun onSaveData(data: String) {
						when(viewModel.isNew.value) {
							true -> viewModel.putNote(data = data)
							false -> viewModel.updateNote(data = data) {  }
							null -> null
						}
					}
				})

				var showDeleteDialog by viewModel.showDeleteDialog
				var showDiscardDialog by viewModel.showDiscardDialog
				val noteId by viewModel.noteId

				CompositionLocalProvider(
					LocalCompositionPremium provides true,
					LocalRichTextEditor provides richTextEditor
				) {
					NoteScreen()

					DeleteDialog(
						showDeleteDialog = showDeleteDialog,
						id = noteId,
						onDismiss = { showDeleteDialog = false }
					) { viewModel.deleteNote() }
					DiscardDialog(
						showDiscardDialog = showDiscardDialog,
						onDismiss = { showDiscardDialog = false }
					) {
						showDiscardDialog = false
						when (viewModel.isNew.value) {
							true -> finish()
							false -> viewModel.isViewer.value = true
							else -> finish()
						}
					}
				}
			}
		}
	}

	override fun onBackPressed() {
		if (viewModel.isNew.value == true && viewModel.isViewer.value) {
			super.onBackPressed()
		} else if(viewModel.isNew.value == true && !viewModel.isViewer.value) {
			viewModel.showDiscardDialog.value = true
		}
		else if (viewModel.isNew.value == false && viewModel.isViewer.value) {
			super.onBackPressed()
		} else if (viewModel.isNew.value == false && !viewModel.isViewer.value) {
//			TODO Show discard popup
			viewModel.showDiscardDialog.value = true
		} else if (viewModel.isNew.value == null) {
			super.onBackPressed()
		}
	}
}
