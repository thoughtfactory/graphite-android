package com.syncodec.graphite.presentation.note

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import com.syncodec.graphite.presentation.note.composable.KitKatScreen
import com.syncodec.graphite.presentation.base.BaseComposable
import com.syncodec.graphite.presentation.base.LocalIsDarkTheme
import com.syncodec.graphite.presentation.note.kitKat.KitKat
import com.syncodec.graphite.presentation.note.kitKat.KitKatAction
import com.syncodec.graphite.utils.Extra
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel


class NoteActivity : ComponentActivity() {

	private val viewModel: NoteViewModel by viewModel()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		val isRecreated = savedInstanceState?.getBoolean(IS_RECREATED) == true

		if (!isRecreated) {
			try {
				val hasFilter = intent.hasExtra(Extra.Companion.Extra.Filter.name)

				if (hasFilter) {
					when (intent.getStringExtra(Extra.Companion.Extra.Filter.name)?.let { Extra.Companion.Filter.valueOf(it) }) {
						Extra.Companion.Filter.SingleRead -> singleRead()
//					    Extra.Companion.Filter.READ_CHAPTER -> finish()
						else -> {
							Log.d("npr71", "unknown filter type")
							finish()
						}
					}
				} else {
					Log.d("npr71", "no extra filter")
					finish()
				}

			} catch (e: Exception) {
				finish()
			}
		}

		val kitKat = KitKat(this)
		kitKat.loadExternalEditor()

		setContent {

			BaseComposable {

				val isDarkTheme = LocalIsDarkTheme.current

				val isEditing by viewModel.isEditing.collectAsState()
				val isKitKatReady by kitKat.isReady.collectAsState()

				val noteObject by viewModel.noteObject.collectAsState()
				val locationData by viewModel.locationData.collectAsState()
				val parentChapter by viewModel.parentChapter.collectAsState()

				val attachmentList by viewModel.attachmentList.collectAsState()

				val allTagList by viewModel.allTagsList.collectAsState()
				val tagStateMap by viewModel.tagStateMap.collectAsState()

				val storedTitle by remember { derivedStateOf { noteObject?.title } }
				val storedContent by remember { derivedStateOf { noteObject?.content } }

				val kitKatFormat by kitKat.kitKatFormat.collectAsState()

				var isReCreateDataRestored by remember { mutableStateOf(false) }

				LaunchedEffect(key1 = isKitKatReady) {
					if (isKitKatReady) {
//						!!! These delays are disgusting
						delay(310)
						kitKat.onKitKatActionAsync(KitKatAction.Other.SetMaxHeight)
						if (isDarkTheme) kitKat.onKitKatActionAsync(KitKatAction.Other.EnableDarkMode)
						else kitKat.onKitKatActionAsync(KitKatAction.Other.DisableDarkMode)
						if (isRecreated) {
							Log.d("npr71", "recreated : setting cached title and content")
							viewModel.getKitKatFormat().let { cachedKitKatFormat ->
								kitKat.onKitKatActionAsync(KitKatAction.Edit.SetTitle(cachedKitKatFormat.kitKatTitle))
								kitKat.onKitKatActionAsync(KitKatAction.Edit.SetContent.Html(content = cachedKitKatFormat.kitKatContent?.drop(1)?.dropLast(1)))
								Log.d("npr71", "recreated : ${cachedKitKatFormat.kitKatContent}")
							}
						}
						isReCreateDataRestored = true
					}
				}

				LaunchedEffect(key1 = isKitKatReady, key2 = isEditing) {
					if (isKitKatReady) {
//						!!! These delays are disgusting
						delay(470)
						if (isEditing == true) kitKat.onKitKatActionAsync(KitKatAction.Edit.Enable)
						else kitKat.onKitKatActionAsync(KitKatAction.Edit.Disable)
					}
				}

				LaunchedEffect(key1 = isKitKatReady, key2 = storedTitle) {
					if (isKitKatReady) {
//						!!! These delays are disgusting
						delay(470)
						if (!isRecreated) {
							Log.d("npr71", "setting saved title")
							kitKat.onKitKatActionAsync(KitKatAction.Edit.SetTitle(storedTitle))
						}
					}
				}

				LaunchedEffect(key1 = isKitKatReady, key2 = storedContent) {
					if (isKitKatReady) {
//						!!! These delays are disgusting
						delay(470)
						if (!isRecreated) {
							Log.d("npr71", "setting saved content : $storedContent")
							kitKat.onKitKatActionAsync(KitKatAction.Edit.SetContent.auto(content = storedContent))
						}
					}
				}

				LaunchedEffect(key1 = kitKatFormat) {
					if (isReCreateDataRestored) viewModel.putKitKatFormat(kitKatFormat)
				}

				KitKatScreen(
					kitKat = kitKat,
					isEditing = isEditing,
					noteObject = noteObject,
					locationData = locationData,
					parentChapter = parentChapter,
					allTagList = allTagList,
					tagStateMap = tagStateMap,
					attachmentList = attachmentList,
					onClickSave = {
						viewModel.save()
						viewModel.isEditing.tryEmit(false)
					},
					onClickEdit = { viewModel.isEditing.tryEmit(true) },
					onSetUserTimestamp = viewModel::setUserTimestamp,
					onSetLocation = viewModel::setLocation,
					onClickRemoveLocation = { viewModel.setLocation(null, null) },
					onClickReloadLocation = viewModel::reloadLocation,
					onAddNewAttachment = viewModel::addNewAttachment,
					toggleAttachment = viewModel::toggleAttachment,
					onSelectChapter = viewModel::updateParent,
					onClickTag = viewModel::toggleTag,
					putTag = viewModel::putTag,
					onClickFavourite = viewModel::toggleFavourite,
					onClickLock = viewModel::toggleLocked,
					onConfirmDelete = { viewModel.delete(); finish() },
					onClickBack = { finish() }
				)
			}
		}
	}

	override fun onSaveInstanceState(outState: Bundle) {
		outState.putBoolean(IS_RECREATED, true)
		super.onSaveInstanceState(outState)
	}

	private fun singleRead() {
		Log.d("npr71", "singleRead")
		val hasIsNew = intent.hasExtra(Extra.Companion.Extra.IsNew.name)

		if (hasIsNew) {
			val isNew = intent.getBooleanExtra(Extra.Companion.Extra.IsNew.name, false)
			if (isNew) {
				Log.d("npr71", "new note")

				val hasParentId = intent.hasExtra(Extra.Companion.Extra.ParentId.name)
				if (hasParentId) {
					val parentId = try {
						intent.getByteArrayExtra(Extra.Companion.Extra.ParentId.name)?.let { RealmUUID.from(it) }
					} catch (_: Exception) {
						null
					}
					parentId?.let { viewModel.initNote(it) } ?: Toast.makeText(this, "Parent ID is null", Toast.LENGTH_SHORT).show()
				} else {
					Toast.makeText(this, "Parent ID is null", Toast.LENGTH_SHORT).show()
				}

				viewModel.isEditing.tryEmit(true)
			} else {
				Log.d("npr71", "load note")

				val hasNoteId = intent.hasExtra(Extra.Companion.Extra.NoteId.name)
				if (hasNoteId) {
					val noteId = try {
						intent.getByteArrayExtra(Extra.Companion.Extra.NoteId.name)?.let { RealmUUID.from(it) }
					} catch (_: Exception) {
						null
					}
					noteId?.let { viewModel.getNote(it) } ?: Toast.makeText(this, "Note ID is null", Toast.LENGTH_SHORT).show()
				} else {
					Toast.makeText(this, "Note ID is null", Toast.LENGTH_SHORT).show()
				}

				lifecycleScope.launch {
					delay(1000)
					viewModel.isEditing.tryEmit(false)
				}
			}
		}
	}

	companion object {
		const val IS_RECREATED = "is_recreated"
	}
}
