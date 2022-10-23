package com.syncodec.graphite.presentation.main

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syncodec.graphite.di.model.BucketObject
import com.syncodec.graphite.di.model.BucketType
import com.syncodec.graphite.di.model.ChapterObject
import com.syncodec.graphite.di.model.NoteObject
import com.syncodec.graphite.di.model.NoteObjectLite
import com.syncodec.graphite.di.repository.RealmNotInitializedException
import com.syncodec.graphite.di.repository.Repository2
import com.syncodec.graphite.di.repository.RepositoryState
import com.syncodec.graphite.utils.SortBy
import com.syncodec.graphite.utils.SortOn
import com.syncodec.graphite.utils.ViewType
import com.syncodec.graphite.utils.encodeBase64
import dagger.hilt.android.lifecycle.HiltViewModel
import io.realm.kotlin.types.ObjectId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import javax.inject.Inject
import kotlin.random.Random


@HiltViewModel
class MainViewModel @Inject constructor(private val repository2 : Repository2) : ViewModel() {

	val repositoryState = repository2.repositoryState

	val defaultNotebookId : MutableState<ObjectId?> = mutableStateOf(null)
	val chapterObject : MutableState<ChapterObject?> = mutableStateOf(null)
	val notebookList : SnapshotStateList<ChapterObject> = mutableStateListOf()
	val noteList : SnapshotStateList<NoteObjectLite> = mutableStateListOf()
	val bucketObjectList : SnapshotStateList<BucketObject> = mutableStateListOf()

	val refresher: MutableStateFlow<Int> = MutableStateFlow(0)
	private var _refresher = 0
	private var refreshCoroutine: CoroutineScope? = null

	val isNoteRefreshing : MutableState<Boolean> = mutableStateOf(true)
	val isBucketRefreshing : MutableState<Boolean> = mutableStateOf(true)
	val isNotebookRefreshing : MutableState<Boolean> = mutableStateOf(true)

	val isSelected: MutableState<Boolean> = mutableStateOf(false)
	val selectedObjectIdList: SnapshotStateList<ObjectId> = mutableStateListOf()

	val showDeleteDialog: MutableState<Boolean> = mutableStateOf(false)
	val showExitDialog: MutableState<Boolean> = mutableStateOf(false)

	init {
		refresher.tryEmit(_refresher+1)
		viewModelScope.launch(Dispatchers.IO) {
			refresher.collect {
				refreshCoroutine?.cancel()
				refresh()
			}
		}
	}

	private fun refresh() {
		viewModelScope.launch(Dispatchers.IO) {
			refreshCoroutine?.cancel()
			refreshCoroutine = this

			isNoteRefreshing.value = true
			isBucketRefreshing.value = true
			isNotebookRefreshing.value = true

			repositoryState.collect {
				when (it) {
					RepositoryState.INIT -> Log.d("MainViewModel", "Init")
					RepositoryState.LOADING -> Log.d("MainViewModel", "Loading")
					RepositoryState.SUCCESS -> onRepositoryStateSuccess()
					RepositoryState.ERROR -> Log.d("MainViewModel", "Error")
				}
			}
		}
	}

	private fun onRepositoryStateSuccess() {
		viewModelScope.launch(Dispatchers.IO) {
			if (repositoryState.value != RepositoryState.SUCCESS) this.cancel()
			getNoteList()
		}

		viewModelScope.launch(Dispatchers.IO) {
			if (repositoryState.value != RepositoryState.SUCCESS) this.cancel()
			getNotebookList()
		}

		viewModelScope.launch(Dispatchers.IO) {
			if (repositoryState.value != RepositoryState.SUCCESS) this.cancel()
			getBucketList()
		}
	}

	private suspend fun getNoteList() {
		try {
			repository2.getDefaultNotebookId().collect { id ->
				withContext(Dispatchers.Main) { defaultNotebookId.value = id }
				if (id != null) {
					try {
						repository2.getNotebookAsFlow(id).collect { notebook ->
							withContext(Dispatchers.Main) {
								chapterObject.value = notebook
								noteList.clear()
								noteList.addAll(notebook?.noteList?.map { note -> note.toLite() } ?: listOf())
								isNoteRefreshing.value = false
							}
						}
					} catch(e: RealmNotInitializedException) {
						e.printStackTrace()
					} catch (e: Exception) {
						e.printStackTrace()
					}
				}
			}
		} catch(e: RealmNotInitializedException) {
			e.printStackTrace()
		} catch (e: Exception) {
			e.printStackTrace()
		}
	}

	private suspend fun getNotebookList() {
		try {
			repository2.getAllNotebookAsFlow().collect { _notebookList ->
				withContext(Dispatchers.Main) {
					notebookList.clear()
					notebookList.addAll(_notebookList)
					isNotebookRefreshing.value = false
				}
			}
		} catch(e: RealmNotInitializedException) {
		} catch (e: Exception) {
		}
	}

	private suspend fun getBucketList() {
		try {
			repository2.getAllBucketAsFlow().collect { _bucketObjectList ->
				withContext(Dispatchers.Main) {
					bucketObjectList.clear()
					bucketObjectList.addAll(_bucketObjectList)
					isBucketRefreshing.value = false
				}
			}
		} catch(e: RealmNotInitializedException) {

		} catch (e: Exception) {
		}
	}


	var filterInclusivityState : MutableState<Int> = mutableStateOf(0)
	var sortOn : MutableState<SortOn> = mutableStateOf(SortOn.TIMESTAMP)
	var sortBy : MutableState<SortBy> = mutableStateOf(SortBy.DESCENDING)

	fun putNotebook(title : String, description : String, color : Color?, bitmap : Bitmap?) {
		CoroutineScope(Dispatchers.IO).launch {
			try {
				ChapterObject().apply {
					this.title = title
					this.description = description
					this.color = color?.toArgb()
					this.thumbnail = bitmap?.encodeBase64()

//					Repository.putChapter(null, this)
				}
			} catch (e : Exception) {
//	    		TODO Show error message
				e.printStackTrace()
			}
		}
	}

	fun putBucket(
		title : String?,
		description : String?,
		bucketType : BucketType,
	) {
		BucketObject().apply {
			this.title = title
			this.description = description
			this.bucketType = bucketType.name

//			Repository.putBucket(this)
		}
	}

	fun delete() {
		try {
			val toDeleteObjectIdList = selectedObjectIdList.toList()
			repository2.delete(toDeleteObjectIdList)
			selectedObjectIdList.clear()
			isSelected.value = false
		} catch (e : Exception) {

		}
	}

	fun addDebugNotes(debugNoteData : String) {
		CoroutineScope(Dispatchers.IO).launch {
			val jsonObject = JSONObject(debugNoteData)
			val jsonArray = jsonObject.getJSONArray("quotes")
			for (i in 0 until jsonArray.length()) {
				try {
					NoteObject.getInstance().apply {
						val obj = jsonArray.getJSONObject(i)
						this.userTimestamp = System.currentTimeMillis() + Random.nextLong((- 1.5e+9).toLong(), 1.5e+9.toLong())
						this.title = obj.optString("author")
						this.contentThumbnail = obj.optString("quote")
						this.content =
							"{\"type\":\"doc\",\"content\":[{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":\"left\"},\"content\":[{\"type\":\"text\",\"text\":\"${
								obj.optString("quote").repeat(500)
							}\"}]}]}"

//						defaultNotebookId.value?.let { Repository.putNote(it, this){} }
						if (i % 100 == 0) {
							Log.i("npr71", "$i/${jsonArray.length()}")
						}
					}
				} catch (exception : Exception) {
					exception.printStackTrace()
				}
				delay(250)
			}
		}
	}
}
