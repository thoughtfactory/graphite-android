package com.syncodec.graphite.presentation.attachment

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.syncodec.graphite.presentation.attachment.composable.screen.AttachmentScreen
import com.syncodec.graphite.presentation.attachment.composable.screen.AttachmentScreenViewModel
import com.syncodec.graphite.presentation.base.BaseComposable
import com.syncodec.graphite.utils.Extra
import io.realm.kotlin.types.RealmUUID
import org.koin.androidx.viewmodel.ext.android.viewModel


class AttachmentActivity : ComponentActivity() {

	private val viewModel: AttachmentScreenViewModel by viewModel()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		val showAll = intent.getBooleanExtra(Extra.Companion.Extra.ShowAll.name, false)
		if (showAll) viewModel.loadAll()
		else {
			val hasNoteId = intent.hasExtra(Extra.Companion.Extra.NoteId.name)
			val hasChapterId = intent.hasExtra(Extra.Companion.Extra.ChapterId.name)

			when {
				hasNoteId && hasChapterId -> {
					Toast.makeText(this.applicationContext, "Error reading data", Toast.LENGTH_SHORT).show()
					finish()
				}

				hasNoteId -> {
					val noteId = intent.getByteArrayExtra(Extra.Companion.Extra.NoteId.name)?.let { RealmUUID.from(it) }
					noteId?.let {
						viewModel.loadNote(noteId = it)
					} ?: run {
						Toast.makeText(this.applicationContext, "NoteId not found", Toast.LENGTH_SHORT).show()
						finish()
					}
				}

				hasChapterId -> {
					val chapterId = intent.getByteArrayExtra(Extra.Companion.Extra.ChapterId.name)?.let { RealmUUID.from(it) }
					chapterId?.let {
						viewModel.loadChapter(chapterId = it)
					} ?: run {
						Toast.makeText(this.applicationContext, "ChapterId not found", Toast.LENGTH_SHORT).show()
						finish()
					}
				}

				else -> {
					Toast.makeText(this.applicationContext, "Error reading data", Toast.LENGTH_SHORT).show()
					finish()
				}
			}
		}

		setContent {
			BaseComposable {

				val enableNoteNavigation by viewModel.enableNoteNavigation.collectAsState()
				val noteAttachmentListMap by viewModel.noteAttachmentListMap.collectAsState()

				AttachmentScreen(
					enableNoteNavigation = enableNoteNavigation,
					noteAttachmentListMap = noteAttachmentListMap,
					deleteAttachment = viewModel::deleteAttachment,
				)
			}
		}
	}
}
