package com.syncodec.graphite.presentation.note2

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import com.syncodec.graphite.presentation.note2.KitKat.Companion.KitKatAction
import com.syncodec.graphite.presentation.note2.composable.KitKatScreen
import com.syncodec.graphite.presentation.ui.BaseContent
import com.syncodec.graphite.utils.Extra
import com.syncodec.graphite.utils.dataStore.DataStoreInstance
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel


class NoteActivity2 : ComponentActivity() {

	private val noteViewModel2: NoteViewModel2 by viewModel()

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


		val dataStoreInstance = DataStoreInstance(this)

		val kitKat = KitKat(this)
		kitKat.loadExternalEditor()

		setContent {

			BaseContent {

				val isEditing by noteViewModel2.isEditing.collectAsState()
				val isKitKatReady by kitKat.isReady.collectAsState()

				val noteId by noteViewModel2.noteId.collectAsState()
				val createdTimestamp by noteViewModel2.createdTimestamp.collectAsState()
				val modifiedTimestamp by noteViewModel2.modifiedTimestamp.collectAsState()
				val userTimestamp by noteViewModel2.userTimestamp.collectAsState()
				val locationData by noteViewModel2.locationData.collectAsState()
				val isFavourite by noteViewModel2.isFavourite.collectAsState()
				val isLocked by noteViewModel2.isLocked.collectAsState()
				val parentId by noteViewModel2.parentId.collectAsState()
				val parentChapter by noteViewModel2.parentChapter.collectAsState()

				val savedFileList by noteViewModel2.savedFileList.collectAsState()
				val newFileList by noteViewModel2.newFileList.collectAsState()

				val allTagList by noteViewModel2.allTagsList.collectAsState()

				val title by noteViewModel2.title.collectAsState()
				val storedContent by noteViewModel2.content.collectAsState()

				val kitKatFormat by kitKat.kitKatFormat.collectAsState()

				var isReCreateDataRestored by remember { mutableStateOf(false) }

				LaunchedEffect(key1 = isKitKatReady) {
					if (isKitKatReady) {
//						!!! These delays are disgusting
						delay(310)
						kitKat.onKitKatAction(KitKatAction.Other.SetMaxHeight)
						if (isRecreated) {
							Log.d("npr71", "recreated : setting updated title and content")
							noteViewModel2.kitKatFormat.value?.kitKatTitle?.let { kitKat.onKitKatAction(KitKatAction.Edit.SetTitle(it)) }
							noteViewModel2.kitKatFormat.value?.kitKatContent?.let { kitKat.onKitKatAction(KitKatAction.Edit.SetContent(it)) }
						}
						isReCreateDataRestored = true
					}
				}

				LaunchedEffect(key1 = isKitKatReady, key2 = isEditing) {
					if (isKitKatReady) {
//						!!! These delays are disgusting
						delay(470)
						if (isEditing == true) kitKat.onKitKatAction(KitKatAction.Edit.Enable)
						else kitKat.onKitKatAction(KitKatAction.Edit.Disable)
					}
				}

				LaunchedEffect(key1 = isKitKatReady, key2 = title) {
					if (isKitKatReady) {
//						!!! These delays are disgusting
						delay(470)
						if (!isRecreated) {
							Log.d("npr71", "setting saved title")
							kitKat.onKitKatAction(KitKatAction.Edit.SetTitle(title))
						}
					}
				}

				LaunchedEffect(key1 = isKitKatReady, key2 = storedContent) {
					if (isKitKatReady) {
//						!!! These delays are disgusting
						delay(470)
						if (!isRecreated) {
							Log.d("npr71", "setting saved content")
							kitKat.onKitKatAction(KitKatAction.Edit.SetContent(storedContent))
						}
					}
				}

				LaunchedEffect(key1 = kitKatFormat) {
					if (isReCreateDataRestored) noteViewModel2.kitKatFormat.tryEmit(kitKatFormat)
				}

				KitKatScreen(
					kitKat = kitKat,
					noteId = noteId,
					isEditing = isEditing,
					createdTimestamp = createdTimestamp,
					modifiedTimestamp = modifiedTimestamp,
					userTimestamp = userTimestamp,
					locationData = locationData,
					isFavourite = isFavourite,
					isLocked = isLocked,
					parentChapter = parentChapter,
					allTagList = allTagList,
					savedFileList = savedFileList,
					newFileList = newFileList,
//					toRemoveFileList =,
					onClickSave = {
						noteViewModel2.save(kitKatFormat)
						noteViewModel2.isEditing.tryEmit(false)
					},
					onClickEdit = { noteViewModel2.isEditing.tryEmit(true) },
					onSetLocation = { latLng, address -> noteViewModel2.setLocation(latLng, address) },
					onClickRemoveLocation = { noteViewModel2.setLocation(null, null) },
					onClickReloadLocation = { noteViewModel2.reloadLocation() },
					onAddNewFile = { noteViewModel2.addNewFileToBuffer(it) },
					onRemoveNewFile = {},
					onRemoveSavedFile = {},
					onClickFavourite = { noteViewModel2.toggleFavourite() },
					onClickLock = { noteViewModel2.toggleLocked() },
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
					parentId?.let { noteViewModel2.initNote(it) } ?: Toast.makeText(this, "Parent ID is null", Toast.LENGTH_SHORT).show()
				} else {
					Toast.makeText(this, "Parent ID is null", Toast.LENGTH_SHORT).show()
				}

				noteViewModel2.isEditing.tryEmit(true)
			} else {
				Log.d("npr71", "load note")

				val hasNoteId = intent.hasExtra(Extra.Companion.Extra.NoteId.name)
				if (hasNoteId) {
					val noteId = try {
						intent.getByteArrayExtra(Extra.Companion.Extra.NoteId.name)?.let { RealmUUID.from(it) }
					} catch (_: Exception) {
						null
					}
					noteId?.let { noteViewModel2.getNote(it) } ?: Toast.makeText(this, "Note ID is null", Toast.LENGTH_SHORT).show()
				} else {
					Toast.makeText(this, "Note ID is null", Toast.LENGTH_SHORT).show()
				}

				lifecycleScope.launch {
					delay(1000)
					noteViewModel2.isEditing.tryEmit(false)
				}
			}
		}
	}

	companion object {
		const val IS_RECREATED = "is_recreated"
	}
}
