package com.syncodec.graphite.presentation.notebook

import android.graphics.Bitmap
import android.os.Bundle
import android.widget.Toast
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
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.syncodec.graphite.di.model.ChapterObject
import com.syncodec.graphite.di.model.ChapterObjectLite
import com.syncodec.graphite.di.model.NoteObjectLite
import com.syncodec.graphite.presentation.common.LocalCompositionIsSelected
import com.syncodec.graphite.presentation.common.LocalCompositionOnSelect
import com.syncodec.graphite.presentation.common.LocalCompositionSelectedRealmUUIDList
import com.syncodec.graphite.presentation.notebook.composable.bottomSheet.NotebookBottomSheetType
import com.syncodec.graphite.presentation.notebook.composable.dialog.NotebookDialogType
import com.syncodec.graphite.presentation.notebook.composable.screen.NotebookScreen
import com.syncodec.graphite.presentation.ui.BaseContent
import com.syncodec.graphite.utils.Authenticator
import com.syncodec.graphite.utils.Extra
import com.syncodec.graphite.utils.LocalAuthenticatorAction
import com.syncodec.graphite.utils.LocalVaultIsOpened
import dagger.hilt.android.AndroidEntryPoint
import io.realm.kotlin.types.RealmUUID


@AndroidEntryPoint
class NotebookActivity : ComponentActivity() {

	private val viewModel by viewModels<NotebookViewModel>()

	@OptIn(ExperimentalComposeUiApi::class)
	override fun onCreate(savedInstanceState : Bundle?) {
		super.onCreate(savedInstanceState)

		val hasChapterId = intent.hasExtra(Extra.Companion.Constant.CHAPTER_ID.name)
		if (hasChapterId) {
			val chapterId = intent.getByteArrayExtra(Extra.Companion.Constant.CHAPTER_ID.name)?.let { RealmUUID.from(it) }

			if (chapterId == null) {
//				ERROR ChapterId is null
				finish()
			} else {
				viewModel.initNotebook(chapterId = chapterId)
			}
		} else {
//			ERROR ChapterId not found
			finish()
		}

		setContent {
			BaseContent {

				val systemUiController = rememberSystemUiController()
				systemUiController.setStatusBarColor(if (isSystemInDarkTheme()) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onBackground)
				systemUiController.setNavigationBarColor(if (isSystemInDarkTheme()) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onBackground)

				val keyboardController = LocalSoftwareKeyboardController.current
				val focusManager = LocalFocusManager.current

				val defaultChapterId by viewModel.defaultChapterId

				val rootChapterId by viewModel.rootChapterId
				val chapterObject by viewModel.chapterObjectLite
				val rootColor by viewModel.rootColor
				val tagList = viewModel.tagObjectList

				var showEditChapterDialog by remember { mutableStateOf(false) }
				var showDeleteDialog by remember { mutableStateOf(false) }

				val chapterId by viewModel.chapterId
				val createdTimestamp by viewModel.createdTimestamp
				val modifiedTimestamp by viewModel.modifiedTimestamp
				val title by viewModel.title
				val description by viewModel.description
				val color by viewModel.color
				val thumbnail by viewModel.thumbnail
				val isFavourite by viewModel.isFavourite
				val isLocked by viewModel.isLocked

				val parentChapterObjectList = viewModel.parentChapterObjectList

				val chapterObjectList = viewModel.chapterObjectList
				val noteObjectList = viewModel.noteObjectList

				var isSelected by viewModel.isSelected
				val selectedRealmUUIDList = viewModel.selectedRealmUUIDList

				val isVaultOpened = LocalVaultIsOpened.current
				val authenticator = LocalAuthenticatorAction.current

				fun openDialog(dialogType : NotebookDialogType) {
					when (dialogType) {
						NotebookDialogType.EDIT_CHAPTER -> showEditChapterDialog = true
						NotebookDialogType.DELETE -> showDeleteDialog = true
					}
				}

				fun closeDialog(dialogType : NotebookDialogType) {
					when (dialogType) {
						NotebookDialogType.EDIT_CHAPTER -> showEditChapterDialog = false
						NotebookDialogType.DELETE -> showDeleteDialog = false
					}
					try {
						keyboardController?.hide()
						focusManager.clearFocus()
					} catch (e : Exception) {
					}
				}

				this.onBackPressedDispatcher.addCallback(
					this, object : OnBackPressedCallback(true) {
						override fun handleOnBackPressed() {
							if (chapterObject == null) {
								finish()
							} else {
								if (showEditChapterDialog || showDeleteDialog) {
									showEditChapterDialog = false
									showDeleteDialog = false
								} else {
									if (chapterObject?.parentChapterId == null) {
										finish()
									} else {
										chapterObject?.parentChapterId?.let { viewModel.getAndLoadChapter(it) }
									}
								}
							}
						}
					}
				)

				CompositionLocalProvider(
					LocalDefaultChapterId provides defaultChapterId,
					LocalRootChapterId provides rootChapterId,
					LocalRootChapterColor provides rootColor,
					LocalChapterId provides chapterId,
					LocalCreatedTimestamp provides createdTimestamp,
					LocalModifiedTimestamp provides modifiedTimestamp,
					LocalTitle provides title,
					LocalDescription provides description,
					LocalColor provides color,
					LocalThumbnail provides thumbnail,
					LocalIsFavourite provides isFavourite,
					LocalIsLocked provides isLocked,
					LocalChapterObjectList provides chapterObjectList,
					LocalNoteObjectList provides noteObjectList,
					LocalParentChapterObjectList provides parentChapterObjectList,
					LocalPutNewChapter provides viewModel::putChapter,
					LocalGetChapter provides viewModel::getAndLoadChapter,
					LocalCompositionIsSelected provides isSelected,
					LocalCompositionOnSelect provides { isSelected = it },
					LocalCompositionSelectedRealmUUIDList provides selectedRealmUUIDList,
					LocalShowEditChapterDialog provides showEditChapterDialog,
					LocalShowDeleteDialog provides showDeleteDialog,
					LocalOpenDialog provides ::openDialog,
					LocalCloseDialog provides ::closeDialog,
					LocalOnToggleFavourite provides viewModel::toggleFavourite,
					LocalOnToggleLock provides {
						if (chapterId == defaultChapterId) Toast.makeText(this@NotebookActivity, "Cannot lock default chapter", Toast.LENGTH_SHORT).show()
						else if (isVaultOpened) viewModel.toggleLock() else authenticator(Authenticator.AUTHENTICATE)
					},
					LocalOnSetDefaultChapter provides {
						if (isLocked == true) Toast.makeText(this@NotebookActivity, "Cannot set locked chapter as default", Toast.LENGTH_SHORT).show()
						else viewModel.setDefaultChapter()
					},
					LocalOnUpdateChapter provides viewModel::updateChapter,
					LocalOnDelete provides {},
					LocalOnDeleteChapter provides {},
					LocalOnBackPressed provides { this.onBackPressedDispatcher.onBackPressed() },
				) {
					NotebookScreen()
				}
			}
		}
	}

	companion object {
		val LocalDefaultChapterId = compositionLocalOf<RealmUUID?> { null }
		val LocalRootChapterId = compositionLocalOf<RealmUUID?> { null }
		val LocalRootChapterColor = compositionLocalOf<Color?> { null }

		val LocalChapterId = compositionLocalOf<RealmUUID?> { null }
		val LocalCreatedTimestamp = compositionLocalOf<Long?> { null }
		val LocalModifiedTimestamp = compositionLocalOf<Long?> { null }
		val LocalTitle = compositionLocalOf<String?> { null }
		val LocalDescription = compositionLocalOf<String?> { null }
		val LocalColor = compositionLocalOf<Color?> { null }
		val LocalThumbnail = compositionLocalOf<Bitmap?> { null }
		val LocalIsFavourite = compositionLocalOf<Boolean?> { null }
		val LocalIsLocked = compositionLocalOf<Boolean?> { null }

		val LocalChapterObjectList = compositionLocalOf<SnapshotStateList<ChapterObject>> { mutableStateListOf() }
		val LocalNoteObjectList = compositionLocalOf<SnapshotStateList<NoteObjectLite>> { mutableStateListOf() }

		val LocalParentChapterObjectList = compositionLocalOf<SnapshotStateList<ChapterObjectLite>> { mutableStateListOf() }

		val LocalPutNewChapter = compositionLocalOf<(String?, String?, Color?, Bitmap?) -> Unit> { { _, _, _, _ -> } }
		val LocalGetChapter = compositionLocalOf<(RealmUUID?) -> Unit> { {} }

		val LocalShowEditChapterDialog = compositionLocalOf<Boolean> { false }
		val LocalShowDeleteDialog = compositionLocalOf<Boolean> { false }

		val LocalOpenBottomSheet = compositionLocalOf<(NotebookBottomSheetType) -> Unit> { {} }
		val LocalCloseBottomSheet = compositionLocalOf { { } }
		val LocalOpenDialog = compositionLocalOf<(NotebookDialogType) -> Unit> { error("No data provided") }
		val LocalCloseDialog = compositionLocalOf<(NotebookDialogType) -> Unit> { error("No data provided") }

		val LocalOnToggleFavourite = compositionLocalOf { {} }
		val LocalOnToggleLock = compositionLocalOf { {} }
		val LocalOnSetDefaultChapter = compositionLocalOf { {} }
		val LocalOnUpdateChapter = compositionLocalOf<(String?, String?, Color?, Bitmap?) -> Unit> { { _, _, _, _ -> } }
		val LocalOnDelete = compositionLocalOf { {} }
		val LocalOnDeleteChapter = compositionLocalOf { {} }
		val LocalOnBackPressed = compositionLocalOf { {} }
	}
}
