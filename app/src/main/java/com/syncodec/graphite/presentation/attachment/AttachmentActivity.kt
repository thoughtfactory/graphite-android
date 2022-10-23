package com.syncodec.graphite.presentation.attachment

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.syncodec.graphite.presentation.attachment.composable.screen.AttachmentScreen
import com.syncodec.graphite.presentation.ui.BaseContent
import com.syncodec.graphite.utils.Extra
import io.realm.kotlin.types.ObjectId


class AttachmentActivity : ComponentActivity() {

	private val viewModel by viewModels<AttachmentViewModel>()

	override fun onCreate(savedInstanceState : Bundle?) {
		super.onCreate(savedInstanceState)

		val showAllAttachments = intent.getBooleanExtra(Extra.Companion.Constant.SHOW_ALL_ATTACHMENTS.name, false)
		val hasNoteId = intent.hasExtra(Extra.Companion.Constant.NOTE_ID.name)
		val hasChapterId = intent.hasExtra(Extra.Companion.Constant.CHAPTER_ID.name)

		when {
			showAllAttachments -> viewModel.loadAllAttachments()
			hasNoteId -> {
				val noteId = intent.getStringExtra(Extra.Companion.Constant.NOTE_ID.name)?.let { ObjectId.from(it) }
				if (noteId != null) {
					viewModel.loadAndViewFromNoteData(noteId)
				} else {
					finish()
				}
			}

			hasChapterId -> {
				val chapterId = intent.getStringExtra(Extra.Companion.Constant.CHAPTER_ID.name)?.let { ObjectId.from(it) }
				if (chapterId != null) {
					viewModel.loadAndViewFromChapterData(chapterId)
				} else {
					finish()
				}
			}

			else -> finish()
		}

		setContent {
			BaseContent {
				val systemUiController = rememberSystemUiController()
				systemUiController.setStatusBarColor(if (isSystemInDarkTheme()) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onBackground)
				systemUiController.setNavigationBarColor(if (isSystemInDarkTheme()) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onBackground)

				val noteObject by viewModel.noteObject
				val chapterObject by viewModel.chapterObject
				val attachmentList = viewModel.attachmentList

				AttachmentScreen(
					noteObject = noteObject,
					chapterObject = chapterObject,
					attachmentList = attachmentList,
				)
			}
		}
	}
}
