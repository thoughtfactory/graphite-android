package com.syncodec.graphite.presentation.note2

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
import com.syncodec.graphite.presentation.note2.KitKat.Companion.KitKatAction
import com.syncodec.graphite.presentation.note2.composable.KitKatScreen
import com.syncodec.graphite.presentation.base.BaseComposable
import com.syncodec.graphite.presentation.base.LocalIsDarkTheme
import com.syncodec.graphite.utils.Extra
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

		val kitKat = KitKat(this)
		kitKat.loadExternalEditor()

		setContent {

			BaseComposable {

				val isDarkTheme = LocalIsDarkTheme.current

				val isEditing by noteViewModel2.isEditing.collectAsState()
				val isKitKatReady by kitKat.isReady.collectAsState()

				val noteObject by noteViewModel2.noteObject.collectAsState()
				val noteId by remember { derivedStateOf { noteObject?.id } }
				val createdTimestamp by remember { derivedStateOf { noteObject?.createdTimestamp } }
				val modifiedTimestamp by remember { derivedStateOf { noteObject?.modifiedTimestamp } }
				val userTimestamp by remember { derivedStateOf { noteObject?.userTimestamp } }
				val locationData by noteViewModel2.locationData.collectAsState()
				val isFavourite by remember { derivedStateOf { noteObject?.isFavourite } }
				val isLocked by remember { derivedStateOf { noteObject?.isLocked } }
				val parentChapter by noteViewModel2.parentChapter.collectAsState()

				val attachmentList by noteViewModel2.attachmentList.collectAsState()

				val allTagList by noteViewModel2.allTagsList.collectAsState()
				val tagStateMap by noteViewModel2.tagStateMap.collectAsState()

				val title by remember { derivedStateOf { noteObject?.title } }
				val storedContent by remember { derivedStateOf { noteObject?.content2 } }

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
							Log.d("npr71", "recreated : setting updated title and content")
							noteViewModel2.kitKatFormat.value?.kitKatTitle?.let { kitKat.onKitKatActionAsync(KitKatAction.Edit.SetTitle(it)) }
							noteViewModel2.kitKatFormat.value?.kitKatContent?.let { kitKat.onKitKatActionAsync(KitKatAction.Edit.SetContent(it)) }
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

				LaunchedEffect(key1 = isKitKatReady, key2 = title) {
					if (isKitKatReady) {
//						!!! These delays are disgusting
						delay(470)
						if (!isRecreated) {
							Log.d("npr71", "setting saved title")
							kitKat.onKitKatActionAsync(KitKatAction.Edit.SetTitle(title))
						}
					}
				}

				LaunchedEffect(key1 = isKitKatReady, key2 = storedContent) {
					if (isKitKatReady) {
//						!!! These delays are disgusting
						delay(470)
						if (!isRecreated) {
							Log.d("npr71", "setting saved content")
							kitKat.onKitKatActionAsync(KitKatAction.Edit.SetContent(storedContent))
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
					tagStateMap = tagStateMap,
					attachmentList = attachmentList,
					onClickSave = {
						noteViewModel2.save()
						noteViewModel2.isEditing.tryEmit(false)
					},
					onClickEdit = { noteViewModel2.isEditing.tryEmit(true) },
					onSetLocation = noteViewModel2::setLocation,
					onClickRemoveLocation = { noteViewModel2.setLocation(null, null) },
					onClickReloadLocation = noteViewModel2::reloadLocation,
					onAddNewAttachment = noteViewModel2::addNewAttachment,
					toggleAttachment = noteViewModel2::toggleAttachment,
					onSelectChapter = noteViewModel2::updateParent,
					onClickTag = noteViewModel2::toggleTag,
					putTag = noteViewModel2::putTag,
					onClickFavourite = noteViewModel2::toggleFavourite,
					onClickLock = noteViewModel2::toggleLocked,
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
