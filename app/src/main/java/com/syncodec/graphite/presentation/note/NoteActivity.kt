package com.syncodec.graphite.presentation.note

import android.os.Bundle
import android.widget.Toast
import android.window.OnBackInvokedDispatcher
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.core.os.BuildCompat
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.syncodec.graphite.presentation.common.printer.Printer
import com.syncodec.graphite.presentation.common.richText.RichTextEditor
import com.syncodec.graphite.presentation.common.richText.rememberRichTextEditor
import com.syncodec.graphite.presentation.note.composable.LocalCompositionAddress
import com.syncodec.graphite.presentation.note.composable.LocalCompositionAttachmentList
import com.syncodec.graphite.presentation.note.composable.LocalCompositionCloseBottomSheet
import com.syncodec.graphite.presentation.note.composable.LocalCompositionCloseDialog
import com.syncodec.graphite.presentation.note.composable.LocalCompositionColor
import com.syncodec.graphite.presentation.note.composable.LocalCompositionContent
import com.syncodec.graphite.presentation.note.composable.LocalCompositionContentThumbnail
import com.syncodec.graphite.presentation.note.composable.LocalCompositionCreatedTimestamp
import com.syncodec.graphite.presentation.note.composable.LocalCompositionIsFavourite
import com.syncodec.graphite.presentation.note.composable.LocalCompositionIsLocked
import com.syncodec.graphite.presentation.note.composable.LocalCompositionIsOperationPending
import com.syncodec.graphite.presentation.note.composable.LocalCompositionIsViewing
import com.syncodec.graphite.presentation.note.composable.LocalCompositionLocationState
import com.syncodec.graphite.presentation.note.composable.LocalCompositionModifiedTimestamp
import com.syncodec.graphite.presentation.note.composable.LocalCompositionNoteId
import com.syncodec.graphite.presentation.note.composable.LocalCompositionOpenBottomSheet
import com.syncodec.graphite.presentation.note.composable.LocalCompositionOpenDialog
import com.syncodec.graphite.presentation.note.composable.LocalCompositionParentChapterId
import com.syncodec.graphite.presentation.note.composable.LocalCompositionTitle
import com.syncodec.graphite.presentation.note.composable.LocalCompositionUserTimestamp
import com.syncodec.graphite.presentation.note.composable.LocalCompositionLatLng
import com.syncodec.graphite.presentation.note.composable.LocalCompositionNoteIdList
import com.syncodec.graphite.presentation.note.composable.LocalCompositionShowDeleteDialog
import com.syncodec.graphite.presentation.note.composable.LocalCompositionShowDiscardDialog
import com.syncodec.graphite.presentation.note.composable.LocalCompositionShowLocationPickerDialog
import com.syncodec.graphite.presentation.note.composable.LocalCompositionShowNotificationPermissionDialog
import com.syncodec.graphite.presentation.note.composable.LocalDeleteNote
import com.syncodec.graphite.presentation.note.composable.LocalDiscardChanges
import com.syncodec.graphite.presentation.note.composable.LocalEditNote
import com.syncodec.graphite.presentation.note.composable.LocalGetNote
import com.syncodec.graphite.presentation.note.composable.LocalSaveNote
import com.syncodec.graphite.presentation.note.composable.dialog.NoteDialogType
import com.syncodec.graphite.presentation.note.composable.bottomSheet.NoteBottomSheetType
import com.syncodec.graphite.presentation.note.composable.screen.NoteScreen
import com.syncodec.graphite.presentation.ui.BaseContent
import com.syncodec.graphite.utils.Extra
import com.syncodec.graphite.utils.LocalCompositionPremium
import com.syncodec.graphite.utils.LocalCompositionRichTextEditor
import dagger.hilt.android.AndroidEntryPoint
import io.realm.kotlin.types.ObjectId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@AndroidEntryPoint
class NoteActivity : ComponentActivity() {

	private val viewModel by viewModels<NoteViewModel>()

	@OptIn(ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class)
	override fun onCreate(savedInstanceState : Bundle?) {
		super.onCreate(savedInstanceState)

		try {
			val hasFilter = intent.hasExtra(Extra.Companion.Constant.FILTER.name)

			if (hasFilter) {
				when (intent.getStringExtra(Extra.Companion.Constant.FILTER.name)?.let { Extra.Companion.Filter.valueOf(it) }) {
					Extra.Companion.Filter.SINGLE_READ -> singleRead()
					Extra.Companion.Filter.READ_CHAPTER -> chapterRead()
					null -> null
				}
			} else {
				finish()
			}

		} catch (e : Exception) {
			e.printStackTrace()
			finish()
		}


		setContent {
			BaseContent {
				val systemUiController = rememberSystemUiController()
				systemUiController.setStatusBarColor(if (isSystemInDarkTheme()) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onBackground)
				systemUiController.setNavigationBarColor(if (isSystemInDarkTheme()) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onBackground)

				val isNew by viewModel.isNew
				val isViewing by viewModel.isViewing
				val isOperationPending by viewModel.isOperationPending
				val locationSnackbarHostState = viewModel.locationSnackbarHostState

				val richTextEditor = rememberRichTextEditor()

				richTextEditor.setOnSaveData(object : RichTextEditor.OnSaveDataListener {
					override fun onSaveData(data : String) {
						when (isNew) {
							true -> viewModel.putNote(data = data)
							false -> viewModel.putNote(data = data)
							null -> null
						}
					}
				})

				richTextEditor.setOnPrintData(object : RichTextEditor.OnPrintDataListener {
					override fun onPrintData(data : String) {
//							viewModel.printNote(data = data)

						CoroutineScope(Dispatchers.Main).launch {
							val printer = Printer(this@NoteActivity)
							printer.createWebPrintJob(data)
						}

					}
				})

				val scope = rememberCoroutineScope()
				val keyboardController = LocalSoftwareKeyboardController.current

				val noteId by viewModel.noteId
				val noteIdList = viewModel.noteIdList
				val parentChapterId by viewModel.parentChapterId

				val createdTimestamp by viewModel.createdTimestamp
				val modifiedTimestamp by viewModel.modifiedTimestamp
				val userTimestamp by viewModel.userTimestamp
				val title by viewModel.title
				val color by viewModel.color
				val latLng by viewModel.latLng
				val address by viewModel.address
				val contentThumbnail by viewModel.contentThumbnail
				val content by viewModel.content
				val isFavourite by viewModel.isFavourite
				val isLocked by viewModel.isLocked

				val attachmentList = viewModel.attachmentListBuffer

				val locationState by viewModel.locationState

				var showLocationPickerDialog by viewModel.showLocationPickerDialog
				var showNotificationPermissionDialog by viewModel.showNotificationPermissionDialog
				var showDiscardDialog by viewModel.showDiscardDialog
				var showDeleteDialog by viewModel.showDeleteDialog

				var bottomSheetType : NoteBottomSheetType by rememberSaveable { mutableStateOf(NoteBottomSheetType.MENU) }
				val modalBottomSheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden) { keyboardController?.hide(); true }
				fun openSheet(_bottomSheetType : NoteBottomSheetType) {
					bottomSheetType = _bottomSheetType; scope.launch { modalBottomSheetState.show() }
				}

				fun closeSheet() {
					scope.launch { modalBottomSheetState.hide() }
				}

				fun openDialog(_noteDialogType : NoteDialogType) {
					when (_noteDialogType) {
						NoteDialogType.LOCATION_PICKER -> showLocationPickerDialog = true
						NoteDialogType.NOTIFICATION_PERMISSION -> showNotificationPermissionDialog = true
						NoteDialogType.DISCARD -> showDiscardDialog = true
						NoteDialogType.DELETE -> showDeleteDialog = true
						else -> null
					}
				}

				fun closeDialog(_noteDialogType : NoteDialogType) {
					when (_noteDialogType) {
						NoteDialogType.LOCATION_PICKER -> showLocationPickerDialog = false
						NoteDialogType.NOTIFICATION_PERMISSION -> showNotificationPermissionDialog = false
						NoteDialogType.DISCARD -> showDiscardDialog = false
						NoteDialogType.DELETE -> showDeleteDialog = false
						else -> null
					}
				}

				fun onSave() {
					when {
						isOperationPending -> Toast.makeText(this, "Please wait while data is being saved", Toast.LENGTH_SHORT).show()
						richTextEditor.isReady.value -> richTextEditor.exec("editor.getData();")
						else -> Toast.makeText(this, "Please wait while editor is being loaded", Toast.LENGTH_SHORT).show()
					}
				}

				onBackPressedDispatcher.addCallback(
					this, object : OnBackPressedCallback(true) {
						override fun handleOnBackPressed() {
							if (showNotificationPermissionDialog || showLocationPickerDialog || showDeleteDialog) {
								closeDialog(NoteDialogType.LOCATION_PICKER)
								closeDialog(NoteDialogType.NOTIFICATION_PERMISSION)
								closeDialog(NoteDialogType.DELETE)
							} else if (modalBottomSheetState.isVisible) {
								closeSheet()
							} else if (isOperationPending) {
								Toast.makeText(this@NoteActivity, "Please wait while data is being saved", Toast.LENGTH_SHORT).show()
							} else if (isViewing == false && isNew == false) {
								openDialog(NoteDialogType.DISCARD)
							} else {
								finish()
							}
						}
					}
				)

				CompositionLocalProvider(
					LocalCompositionPremium provides true,
					LocalCompositionRichTextEditor provides richTextEditor,
					LocalCompositionNoteId provides noteId,
					LocalCompositionNoteIdList provides noteIdList,
					LocalCompositionParentChapterId provides parentChapterId,
					LocalCompositionIsViewing provides isViewing,
					LocalCompositionIsOperationPending provides isOperationPending,
					LocalCompositionContentThumbnail provides contentThumbnail,
					LocalCompositionContent provides content,
					LocalCompositionCreatedTimestamp provides createdTimestamp,
					LocalCompositionModifiedTimestamp provides modifiedTimestamp,
					LocalCompositionUserTimestamp provides userTimestamp,
					LocalCompositionTitle provides title,
					LocalCompositionColor provides color,
					LocalCompositionLatLng provides latLng,
					LocalCompositionAddress provides address,
					LocalCompositionIsLocked provides isLocked,
					LocalCompositionIsFavourite provides isFavourite,
					LocalCompositionAttachmentList provides attachmentList,
					LocalCompositionLocationState provides locationState,
					LocalCompositionOpenBottomSheet provides ::openSheet,
					LocalCompositionCloseBottomSheet provides ::closeSheet,
					LocalCompositionOpenDialog provides ::openDialog,
					LocalCompositionCloseDialog provides ::closeDialog,
					LocalCompositionShowLocationPickerDialog provides showLocationPickerDialog,
					LocalCompositionShowNotificationPermissionDialog provides showNotificationPermissionDialog,
					LocalCompositionShowDiscardDialog provides showDiscardDialog,
					LocalCompositionShowDeleteDialog provides showDeleteDialog,
					LocalSaveNote provides ::onSave,
					LocalGetNote provides this.viewModel::getNote,
					LocalEditNote provides this.viewModel::editNote,
					LocalDiscardChanges provides this.viewModel::discardChanges,
					LocalDeleteNote provides this.viewModel::deleteNote,
				) {
					NoteScreen(
						locationSnackbarHostState = locationSnackbarHostState,
						modalBottomSheetState = modalBottomSheetState,
						bottomSheetType = bottomSheetType,
						onClickBack = { onBackPressed() },
						onClickLock = viewModel::toggleLock,
						onClickFavourite = viewModel::toggleFavourite,
						onAddAttachmentToBuffer = viewModel::addAttachmentToBuffer,
						onRemoveAttachment = viewModel::removeAttachmentFromBuffer,
						onUpdateTitle = {},
						onRemoveLocation = viewModel::onRemoveLocation,
						onReloadLocation = viewModel::getLocation,
						setLocation = viewModel::setLocation,
					)
				}
			}
		}
	}

	override fun onStop() {
		super.onStop()

//		TODO()
	}

	private fun singleRead() {
		val hasNoteId = intent.hasExtra(Extra.Companion.Constant.NOTE_ID.name)
		if (hasNoteId) {
			val noteId = intent.getStringExtra(Extra.Companion.Constant.NOTE_ID.name)?.let { ObjectId.from(it) }
			if (noteId != null) {
				viewModel.singleRead(noteId)
			} else {
				finish()
			}
		} else {
			finish()
		}
	}

	private fun chapterRead() {
		val hasIsNew = intent.hasExtra(Extra.Companion.Constant.IS_NEW.name)
		val hasChapterId = intent.hasExtra(Extra.Companion.Constant.CHAPTER_ID.name)
		val hasNoteId = intent.hasExtra(Extra.Companion.Constant.NOTE_ID.name)

		if (hasIsNew && hasChapterId) {
			val isNew = intent.getBooleanExtra(Extra.Companion.Constant.IS_NEW.name, false)
			val chapterId = intent.getStringExtra(Extra.Companion.Constant.CHAPTER_ID.name)?.let { ObjectId.from(it) }

			if (isNew) {
				if (chapterId != null) {
					viewModel.chapterReadNew(chapterId)
				} else {
					finish()
				}
			} else {
				val noteId = intent.getStringExtra(Extra.Companion.Constant.NOTE_ID.name)?.let { ObjectId.from(it) }
				if (chapterId != null && noteId != null) {
					viewModel.chapterRead(chapterId, noteId)
				} else {
					finish()
				}
			}
		} else {
			finish()
		}
	}
}
