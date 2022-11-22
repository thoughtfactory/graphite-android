package com.syncodec.graphite.presentation.atlas

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
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
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.syncodec.graphite.di.model.ChapterObjectLite
import com.syncodec.graphite.presentation.atlas.composable.screen.AtlasScreen
import com.syncodec.graphite.presentation.common.LocalCompositionCloseDialog
import com.syncodec.graphite.presentation.common.LocalCompositionIsSelected
import com.syncodec.graphite.presentation.common.LocalCompositionOnSelect
import com.syncodec.graphite.presentation.common.LocalCompositionOpenDialog
import com.syncodec.graphite.presentation.common.LocalCompositionSelectedObjectIdList
import com.syncodec.graphite.presentation.common.dialog.DialogType
import com.syncodec.graphite.presentation.note.NoteActivity
import com.syncodec.graphite.presentation.ui.BaseContent
import com.syncodec.graphite.utils.Extra
import dagger.hilt.android.AndroidEntryPoint
import io.realm.kotlin.types.RealmUUID


@AndroidEntryPoint
class AtlasActivity : ComponentActivity() {

	private val viewModel by viewModels<AtlasViewModel>()

	override fun onCreate(savedInstanceState : Bundle?) {
		super.onCreate(savedInstanceState)

		val showAll = intent.getBooleanExtra(Extra.Companion.Constant.SHOW_ALL.name, false)
		val hasChapterId = intent.hasExtra(Extra.Companion.Constant.CHAPTER_ID.name)

		if (showAll) {
			viewModel.onWhere(null)
		} else if (hasChapterId) {
			val chapterId = intent.getByteArrayExtra(Extra.Companion.Constant.CHAPTER_ID.name)?.let {
				try {
					RealmUUID.from(it)
				} catch (e : Exception) {
					null
				}
			}
			if (chapterId != null) {
				viewModel.onWhere(chapterId)
			} else {
				finish()
			}
		} else {
			finish()
		}

		setContent {
			BaseContent {
				val systemUiController = rememberSystemUiController()
				systemUiController.setStatusBarColor(if (isSystemInDarkTheme()) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onBackground)
				systemUiController.setNavigationBarColor(if (isSystemInDarkTheme()) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onBackground)

				val parentChapter by viewModel.chapterObject
				val noteList = viewModel.noteList
				val chapterList = viewModel.chapterList
				val chapterPath = viewModel.chapterPath

				var isSelected by viewModel.isSelected
				val selectedRealmUUIDList = viewModel.selectedRealmUUIDList

				var showDeleteDialog by remember { mutableStateOf(false) }
				var showWhereDialog by remember { mutableStateOf(false) }


				fun openDialog(dialogType : DialogType) {
					when (dialogType) {
						DialogType.DELETE -> showDeleteDialog = true
						DialogType.WHERE -> showWhereDialog = true
						else -> null
					}
				}

				fun closeDialog(dialogType : DialogType) {
					when (dialogType) {
						DialogType.DELETE -> showDeleteDialog = false
						DialogType.WHERE -> showWhereDialog = false
						else -> null
					}
				}

				this.onBackPressedDispatcher.addCallback(
					object : OnBackPressedCallback(true) {
						override fun handleOnBackPressed() {
							if (showWhereDialog) {
								if (parentChapter != null) {
									viewModel.onWhere(chapterPath.getOrNull(1)?.id)
								} else {
									closeDialog(DialogType.WHERE)
								}
							} else if (isSelected) {
								isSelected = false
								selectedRealmUUIDList.clear()
							} else {
								finish()
							}
						}
					}
				)

				val activityLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
					try {
						it.data?.let {
							val hasIntentAction = it.hasExtra(Extra.Companion.Constant.INTENT_ACTION.name)
							if (hasIntentAction) {
								val intentAction = it.getStringExtra(Extra.Companion.Constant.INTENT_ACTION.name)?.let { it1 ->
									Extra.Companion.IntentAction.valueOf(it1)
								}
								if (intentAction == Extra.Companion.IntentAction.DELETE) {
									val hasObjectId = it.hasExtra(Extra.Companion.Constant.OBJECT_ID.name)
									if(hasObjectId) {
										val realmUUID = it.getByteArrayExtra(Extra.Companion.Constant.OBJECT_ID.name)?.let { RealmUUID.from(it) }
										if (realmUUID != null) {
											selectedRealmUUIDList.add(realmUUID)
//											onDelete()
										}
									}
								}
							}
							Extra.Companion.Constant.INTENT_ACTION.name
							Extra.Companion.Constant.OBJECT_ID.name
						}
					} catch (e: Exception) {
						Toast.makeText(this, "Error performing action", Toast.LENGTH_SHORT).show()
					}
				}

				CompositionLocalProvider(
					LocalChapterList provides chapterList,
					LocalChapterPath provides chapterPath,
					LocalCompositionIsSelected provides isSelected,
					LocalCompositionOnSelect provides { isSelected = it },
					LocalCompositionSelectedObjectIdList provides selectedRealmUUIDList,
					LocalShowDeleteDialog provides showDeleteDialog,
					LocalShowWhereDialog provides showWhereDialog,
					LocalCompositionOpenDialog provides ::openDialog,
					LocalCompositionCloseDialog provides ::closeDialog,
					LocalOnDelete provides viewModel::delete,
					LocalOnWhere provides viewModel::onWhere,
					LocalSetOnWhere provides viewModel::setOnWhere,
				) {
					AtlasScreen(
						parentChapter = parentChapter,
						noteList = noteList.filter { if (parentChapter == null) true else it.parentChapterId == parentChapter?.id },
						onClickNote = {
							Intent(this, NoteActivity::class.java).apply {
								putExtra(Extra.Companion.Constant.IS_NEW.name, false)
								putExtra(Extra.Companion.Constant.NOTE_ID.name, it.bytes)
								putExtra(Extra.Companion.Constant.FILTER.name, Extra.Companion.Filter.SINGLE_READ.name)

								activityLauncher.launch(this)
							}
						},
						onLongClickNote = {},
					) { super.getOnBackPressedDispatcher().onBackPressed() }
				}
			}
		}
	}

	companion object {
		val LocalChapterList = compositionLocalOf<SnapshotStateList<ChapterObjectLite>> { mutableStateListOf() }
		val LocalChapterPath = compositionLocalOf<SnapshotStateList<ChapterObjectLite>> { mutableStateListOf() }

		val LocalShowDeleteDialog = compositionLocalOf { false }
		val LocalShowWhereDialog = compositionLocalOf { false }

		val LocalOnDelete = compositionLocalOf { {} }
		val LocalOnWhere = compositionLocalOf<(RealmUUID?) -> Unit> { {} }
		val LocalSetOnWhere = compositionLocalOf<(ChapterObjectLite?) -> Unit> { {} }
	}
}
