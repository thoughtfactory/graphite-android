package com.syncodec.momento.mainComponent

import android.app.Application
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.syncodec.momento.MainActivity
import com.syncodec.momento.Momento
import com.syncodec.momento.database.UserDatabase
import com.syncodec.momento.database.bucketItem.BucketItemType
import com.syncodec.momento.database.note.NoteDbEntry
import com.syncodec.momento.database.notebook.NotebookDbEntry
import com.syncodec.momento.database.quote.QuoteDbEntry
import com.syncodec.momento.mainComponent.modalBottomSheet.BottomSheetType
import com.syncodec.momento.miscellaneous.DataStore
import com.syncodec.momento.miscellaneous.generatePrimaryKey
import com.syncodec.momento.repository.AttachmentRepository
import com.syncodec.momento.repository.BucketRepository
import com.syncodec.momento.repository.NoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.launch
import org.joda.time.DateTime
import java.io.File
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

	var quote: MutableState<QuoteDbEntry?> = mutableStateOf(null)
	var quoteBg: MutableState<File?> = mutableStateOf(null)

	var vaultState = (application as Momento).vaultState
	var bottomSheetType: MutableState<BottomSheetType> =
		mutableStateOf(BottomSheetType.MenuBottomSheet)
	var componentType: MutableState<MainActivity.ComponentType> =
		mutableStateOf(MainActivity.ComponentType.NOTE)
	var selectedItemList: SnapshotStateList<String> = mutableStateListOf()
	var showDeleteDialog: MutableState<Boolean> = mutableStateOf(false)

	var isSelected = mutableStateOf(false)
	var showArchived = mutableStateOf(false)
	var showFavourite = mutableStateOf(false)
	var showLocked = mutableStateOf(false)
	var showTrash = mutableStateOf(false)
	val bucketFilter: SnapshotStateList<BucketItemType> =
		mutableStateListOf(BucketItemType.TODO, BucketItemType.BOOKS, BucketItemType.SHOWS)

	var defaultNotebookKey: String? = null
	var defaultNoteMap: SnapshotStateMap<String, NoteDbEntry> = mutableStateMapOf()
	val notebookListFlow = noteRepository.notebookListFlow
	val bucketList = bucketRepository.bucketList

	init {
		getQuote()
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

	fun getQuote() {
		val date = DateTime.now()
		val d = date.dayOfMonth.toString().padStart(2, '0')
		val m = date.monthOfYear.toString().padStart(2, '0')
		val y = date.year.toString().padStart(2, '0')
		val dateString = "${d}_${m}_${y}"

		viewModelScope.launch(Dispatchers.IO) {
			UserDatabase.getInstance(getApplication()).quoteTableDao.getAsFlow(dateString)
				.collect {
					quote.value = it
					quoteBg.value = getApplication<Momento>().getQuoteBg(date = dateString)
				}
		}

	}

	fun delete(keyList: MutableList<String>) {
		viewModelScope.launch(Dispatchers.IO) {
			keyList.forEach {
				defaultNoteMap[it]?.attachmentKeyList?.let { it1 -> attachmentRepository.delete(it1) }
			}

			noteRepository.deleteNote(keyList = keyList)
			noteRepository.deleteNotebook(keyList = keyList)
			bucketRepository.deleteBucket(keyList = keyList)
		}
	}

	fun insertNotebook(notebook: NotebookDbEntry) =
		viewModelScope.launch { noteRepository.putNotebook(notebook = notebook) }

	fun createNewBucket(bucketType: BucketItemType, title: String) = viewModelScope.launch {
		bucketRepository.putNewBucket(bucketType = bucketType, title = title)
	}
}
