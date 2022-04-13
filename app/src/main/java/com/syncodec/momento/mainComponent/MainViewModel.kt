package com.syncodec.momento.mainComponent

import android.app.Application
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.syncodec.momento.MainActivity
import com.syncodec.momento.Momento
import com.syncodec.momento.database.bucketItem.BucketItemType
import com.syncodec.momento.database.note.NoteDbEntry
import com.syncodec.momento.database.notebook.NotebookDbEntry
import com.syncodec.momento.miscellaneous.DataStore
import com.syncodec.momento.miscellaneous.generatePrimaryKey
import com.syncodec.momento.miscellaneous.logger
import com.syncodec.momento.repository.AttachmentRepository
import com.syncodec.momento.repository.BucketRepository
import com.syncodec.momento.repository.NoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.launch
import kotlin.collections.List
import kotlin.collections.forEach
import kotlin.collections.set

class MainViewModel(application: Application) : AndroidViewModel(application) {

	val firebaseAuth = FirebaseAuth.getInstance()
	val dataStore = DataStore(this.getApplication())
	private val noteRepository: NoteRepository =
		NoteRepository.getInstance(momento = application as Momento)
	private val attachmentRepository: AttachmentRepository =
		AttachmentRepository.getInstance(momento = application as Momento)
	private val bucketRepository: BucketRepository =
		BucketRepository.getInstance(momento = application as Momento)

	lateinit var activityState: MainActivity.ActivityState

	var defaultNotebookKey: String? = null
	var defaultNoteMap: SnapshotStateMap<String, NoteDbEntry> = mutableStateMapOf()
	val notebookMap: SnapshotStateMap<String, Pair<NotebookDbEntry, Int>> =
		noteRepository.notebookMap
	val bucketMap = bucketRepository.bucketMap

	init {
		viewModelScope.launch(Dispatchers.IO) {
			dataStore.getDefaultNotebookKey.collect { notebookKey ->
				defaultNotebookKey = notebookKey
				if (notebookKey != null) {
					viewModelScope.launch(Dispatchers.IO) {
						noteRepository.getNoteAsFlow(notebookKey).cancellable().collect {
							if (notebookKey != defaultNotebookKey) {
								this.coroutineContext.cancel()
							} else {
								try {
									defaultNoteMap.clear()
									it.forEach { defaultNoteMap[it.key] = it }
								} catch (exception: Exception) {

								}
							}
						}
					}
				} else {
					NotebookDbEntry(
						key = generatePrimaryKey(),
						createdTimestamp = System.currentTimeMillis()
					).apply {
						this.title = "Diary"
						this.description = "Default diary"
						this.color = Color(0xFF52616B).toArgb()
						this.bitmap = null

						noteRepository.insert(this)
						dataStore.putDefaultNotebookKey(key)
					}
				}
			}
		}
	}

	fun deleteNote(keyList: List<String>) {
		viewModelScope.launch(Dispatchers.IO) {
			keyList.forEach {
				logger("key : $it")
				defaultNoteMap[it]?.attachmentKeyList?.let { it1 ->
					attachmentRepository.delete(it1) }
			}
			noteRepository.deleteNote(keyList = keyList)
		}
	}

	fun insertNotebook(notebook: NotebookDbEntry) =
		viewModelScope.launch { noteRepository.putNotebook(notebook = notebook) }

	fun createNewBucket(bucketType: BucketItemType, title: String) = viewModelScope.launch {
		bucketRepository.putNewBucket(bucketType = bucketType, title = title)
	}
}
