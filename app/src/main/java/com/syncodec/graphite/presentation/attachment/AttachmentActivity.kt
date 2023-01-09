package com.syncodec.graphite.presentation.attachment

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.syncodec.graphite.presentation.attachment.composable.dialog.AttachmentDialogType
import com.syncodec.graphite.presentation.attachment.composable.screen.AttachmentScreen
import com.syncodec.graphite.presentation.common.LocalCompositionIsSelected
import com.syncodec.graphite.presentation.common.LocalCompositionOnSelect
import com.syncodec.graphite.presentation.ui.BaseContent
import com.syncodec.graphite.utils.Extra
import com.syncodec.graphite.utils.tone
import dagger.hilt.android.AndroidEntryPoint
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File


@AndroidEntryPoint
class AttachmentActivity : ComponentActivity() {

	private val viewModel by viewModels<AttachmentViewModel>()

	override fun onCreate(savedInstanceState : Bundle?) {
		super.onCreate(savedInstanceState)

		setContent {
			BaseContent {
				val systemUiController = rememberSystemUiController()
				systemUiController.setStatusBarColor(MaterialTheme.colorScheme.background)
				systemUiController.setNavigationBarColor(MaterialTheme.colorScheme.surface.tone(isSystemInDarkTheme(), 1))

				val noteObject by viewModel.noteObject
				val chapterObject by viewModel.chapterObject
				val attachmentList = viewModel.attachmentList

				var isSelected by viewModel.isSelected
				val selectedAttachmentList = viewModel.selectedAttachmentList

				var showDeleteDialog by remember { mutableStateOf(false) }

				fun openDialog(type : AttachmentDialogType) {
					when (type) {
						AttachmentDialogType.DELETE -> showDeleteDialog = true
					}
				}

				fun closeDialog(type : AttachmentDialogType) {
					when (type) {
						AttachmentDialogType.DELETE -> showDeleteDialog = false
					}
				}

				this.onBackPressedDispatcher.addCallback(
					object : OnBackPressedCallback(true) {
						override fun handleOnBackPressed() {
							if (showDeleteDialog) {
								closeDialog(AttachmentDialogType.DELETE)
							} else if (isSelected) {
								isSelected = false
								selectedAttachmentList.clear()
							} else {
								finish()
							}
						}
					}
				)

				CompositionLocalProvider(
					LocalCompositionIsSelected provides isSelected,
					LocalSelectedAttachmentList provides selectedAttachmentList,
					LocalCompositionOnSelect provides { isSelected = it },
					LocalShowDeleteDialog provides showDeleteDialog,
					LocalOpenDialog provides ::openDialog,
					LocalCloseDialog provides ::closeDialog,
					LocalOnDelete provides {
						viewModel.deleteAttachment {
							CoroutineScope(Dispatchers.Main).launch {
								selectedAttachmentList.clear()
								isSelected = false
								onResume()
							}
						}
					},
				) {
					AttachmentScreen(
						noteObject = noteObject,
						chapterObject = chapterObject,
						attachmentList = attachmentList,
						onClickBack = { this.onBackPressedDispatcher.onBackPressed() },
					)
				}
			}
		}
	}

	override fun onResume() {
		super.onResume()

		val showAll = intent.getBooleanExtra(Extra.Companion.Constant.SHOW_ALL.name, false)
		val hasNoteId = intent.hasExtra(Extra.Companion.Constant.NOTE_ID.name)
		val hasChapterId = intent.hasExtra(Extra.Companion.Constant.CHAPTER_ID.name)

		viewModel.attachmentList.clear()

		try {
			when {
				showAll -> viewModel.loadAllData()
				hasNoteId -> {
//					val _attachmentList : MutableList<Triple<RealmUUID, File?, Uri?>> = mutableListOf()
					val noteId = intent.getByteArrayExtra(Extra.Companion.Constant.NOTE_ID.name)?.let { RealmUUID.from(it) }
					if (noteId != null) viewModel.loadDataFromNote(noteId) {
//						_attachmentList.addAll(it)
					} else finish()
//					CoroutineScope(Dispatchers.Main).launch {
//						viewModel.attachmentList.clear()
//						viewModel.attachmentList.addAll(_attachmentList.toList())
//					}
				}

				hasChapterId -> {
					val chapterId = intent.getByteArrayExtra(Extra.Companion.Constant.CHAPTER_ID.name)?.let { RealmUUID.from(it) }
					if (chapterId != null) {
						viewModel.loadDataFromChapter(chapterId)
					} else {
						finish()
					}
				}

				else -> finish()
			}
		} catch (e : Exception) {
//			e.printStackTrace()
			finish()
		}
	}

	companion object {
		val LocalSelectedAttachmentList = compositionLocalOf { mutableStateListOf<Triple<RealmUUID, File?, Uri?>>() }
		val LocalShowDeleteDialog = compositionLocalOf { false }
		val LocalOpenDialog = compositionLocalOf<(AttachmentDialogType) -> Unit> { {} }
		val LocalCloseDialog = compositionLocalOf<(AttachmentDialogType) -> Unit> { {} }
		val LocalOnDelete = compositionLocalOf { {} }
	}
}
