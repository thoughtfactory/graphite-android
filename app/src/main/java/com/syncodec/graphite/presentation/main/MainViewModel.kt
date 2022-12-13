package com.syncodec.graphite.presentation.main

import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ListenableWorker
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.syncodec.graphite.BaseApplication
import com.syncodec.graphite.di.model.BucketObject
import com.syncodec.graphite.di.model.BucketType
import com.syncodec.graphite.di.model.ChapterObject
import com.syncodec.graphite.di.model.NoteObjectLite
import com.syncodec.graphite.di.model.TagObject
import com.syncodec.graphite.di.repository.RealmNotInitializedException
import com.syncodec.graphite.di.repository.Repository2
import com.syncodec.graphite.di.repository.RepositoryState
import com.syncodec.graphite.utils.SortBy
import com.syncodec.graphite.utils.SortOn
import com.syncodec.graphite.utils.encodeBase64
import com.syncodec.graphite.widget.home.HomeWidget
import dagger.hilt.android.lifecycle.HiltViewModel
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


@HiltViewModel
class MainViewModel @Inject constructor(val repository2 : Repository2) : ViewModel() {

	val repositoryState = repository2.repositoryState

	val defaultNotebookId : MutableState<RealmUUID?> = mutableStateOf(null)
	val chapterObject : MutableState<ChapterObject?> = mutableStateOf(null)
	val notebookList : SnapshotStateList<ChapterObject> = mutableStateListOf()
	val noteList : SnapshotStateList<NoteObjectLite> = mutableStateListOf()
	val bucketObjectList : SnapshotStateList<BucketObject> = mutableStateListOf()

	val tagList : SnapshotStateList<TagObject> = mutableStateListOf()

	val refresher : MutableStateFlow<Int> = MutableStateFlow(0)
	private var _refresher = 0
	private var refreshCoroutine : CoroutineScope? = null

	val isNoteRefreshing : MutableState<Boolean> = mutableStateOf(true)
	val isBucketRefreshing : MutableState<Boolean> = mutableStateOf(true)
	val isNotebookRefreshing : MutableState<Boolean> = mutableStateOf(true)

	val isSelected : MutableState<Boolean> = mutableStateOf(false)
	val selectedObjectIdList : SnapshotStateList<RealmUUID> = mutableStateListOf()

	val showDeleteDialog : MutableState<Boolean> = mutableStateOf(false)
	val showExitDialog : MutableState<Boolean> = mutableStateOf(false)

	val isPro = BaseApplication.isPro.value

	init {
		refresher.tryEmit(_refresher + 1)
		viewModelScope.launch(Dispatchers.Default) {
			refresher.collect {
				refreshCoroutine?.cancel()
				refresh()
			}
		}
	}

	private fun refresh() {
		viewModelScope.launch(Dispatchers.Default) {
			refreshCoroutine?.cancel()
			refreshCoroutine = this

			isNoteRefreshing.value = true
			isBucketRefreshing.value = true
			isNotebookRefreshing.value = true

			repositoryState.collect {
				when (it) {
					RepositoryState.INIT -> null
					RepositoryState.LOCKED -> null
					RepositoryState.LOADING -> null
					RepositoryState.SUCCESS -> onRepositoryStateSuccess()
					RepositoryState.ERROR -> null
				}
			}
		}
	}

	private fun onRepositoryStateSuccess() {
		viewModelScope.launch(Dispatchers.Default) {
			if (repositoryState.value != RepositoryState.SUCCESS) this.cancel()
			debug()
			getNoteList()
		}

		viewModelScope.launch(Dispatchers.Default) {
			if (repositoryState.value != RepositoryState.SUCCESS) this.cancel()
			getNotebookList()
		}

		viewModelScope.launch(Dispatchers.Default) {
			if (repositoryState.value != RepositoryState.SUCCESS) this.cancel()
			getBucketList()
		}

		viewModelScope.launch(Dispatchers.Default) {
			if (repositoryState.value != RepositoryState.SUCCESS) this.cancel()
			getTagList()
		}
	}

	private suspend fun getNoteList() {
		try {
			repository2.getDefaultChapterId().collect { id ->
				withContext(Dispatchers.Main) { defaultNotebookId.value = id }
				if (id != null) {
					try {
						repository2.getChapterFromIdAsFlow(id).collect { notebook ->
							withContext(Dispatchers.Main) {
								chapterObject.value = notebook
								noteList.clear()
								noteList.addAll(notebook?.noteList?.map { note -> note.toLite() } ?: listOf())
								isNoteRefreshing.value = false
							}
						}
					} catch (e : RealmNotInitializedException) {
//						e.printStackTrace()
					} catch (e : Exception) {
//						e.printStackTrace()
					}
				}
			}
		} catch (e : RealmNotInitializedException) {
//			e.printStackTrace()
		} catch (e : Exception) {
//			e.printStackTrace()
		}
	}

	private suspend fun getNotebookList() {
		try {
			repository2.getChapterWithParentIdAsFlow(null).collect { _notebookList ->
				withContext(Dispatchers.Main) {
					notebookList.clear()
					notebookList.addAll(_notebookList)
					isNotebookRefreshing.value = false
				}
			}
		} catch (e : RealmNotInitializedException) {
//			e.printStackTrace()
		} catch (e : Exception) {
//			e.printStackTrace()
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
		} catch (e : RealmNotInitializedException) {

		} catch (e : Exception) {
		}
	}

	private suspend fun getTagList() {
		try {
			repository2.getAllTagAsFlow().collect { _tagList ->
				withContext(Dispatchers.Main) {
					tagList.clear()
					tagList.addAll(_tagList)
				}
			}
		} catch (e : RealmNotInitializedException) {
		} catch (e : Exception) {
		}
	}

	var filterInclusivityState : MutableState<Int> = mutableStateOf(0)
	var sortOn : MutableState<SortOn> = mutableStateOf(SortOn.TIMESTAMP)
	var sortBy : MutableState<SortBy> = mutableStateOf(SortBy.DESCENDING)

	fun putNotebook(title : String, description : String, color : Color?, bitmap : Bitmap?) {
		CoroutineScope(Dispatchers.Default).launch {
			try {
				if (notebookList.size >= 3 && !isPro) {
					withContext(Dispatchers.Main) {
						Toast.makeText(repository2.context, "Join Graphite Pro to add more notebooks", Toast.LENGTH_SHORT).show()
					}
				} else {
					ChapterObject().apply {
						this.title = title
						this.description = description
						this.color = color?.toArgb()
						this.thumbnail = bitmap?.encodeBase64()

						repository2.putChapter(null, this) { _, _ -> }
					}
				}
			} catch (e : Exception) {
//	    		TODO Show error message
//				e.printStackTrace()
			}
		}
	}

	fun putBucket(
		title : String?,
		description : String?,
		bucketType : BucketType,
	) {
		CoroutineScope(Dispatchers.Default).launch {
			if (bucketObjectList.find { it.bucketType == bucketType.name } != null && ! isPro) {
				val bucket = when(bucketType) {
					BucketType.TODO -> "Todo"
					BucketType.BOOK -> "Book"
					BucketType.SHOW -> "Show"
					BucketType.LINK -> "Link"
					BucketType.UNKNOWN -> "Unknown"
				}
				withContext(Dispatchers.Main) {
					Toast.makeText(repository2.context, "Join Graphite Pro to add more $bucket bucket", Toast.LENGTH_SHORT).show()
				}
			} else {
				BucketObject().apply {
					this.title = title
					this.description = description
					this.bucketType = bucketType.name

					repository2.putBucket(this) { _, _ -> }
				}
			}
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
//		CoroutineScope(Dispatchers.IO).launch {
//			for (i in 0 .. 100) {
//				val jsonObject = JSONObject(debugNoteData)
//				val jsonArray = jsonObject.getJSONArray("quotes")
//				for (i in 0 until jsonArray.length()) {
//					try {
//						NoteObject.getInstance().apply {
//							val obj = jsonArray.getJSONObject(i)
//							this.userTimestamp = System.currentTimeMillis() + Random.nextLong((- 1.5e+9).toLong(), 1.5e+9.toLong())
//							this.title = obj.optString("author")
//							this.contentThumbnail = obj.optString("quote")
//							this.content =
//								"{\"type\":\"doc\",\"content\":[{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":\"left\"},\"content\":[{\"type\":\"text\",\"text\":\"${
//									obj.optString("quote").repeat(500)
//								}\"}]}]}"
//
//							this.parentChapterId = defaultNotebookId.value
//							repository2.putNote(this) { _, _ -> }
//							if (i % 100 == 0) {
//								Log.i("npr71", "$i/${jsonArray.length()}")
//							}
//						}
//					} catch (exception : Exception) {
//						exception.printStackTrace()
//					}
//					delay(250)
//				}
//			}
//		}
	}

	fun onAuthenticate() {
		repository2.isAuthenticated.tryEmit(true)
	}

	fun onAuthFailure() {
		repository2.isAuthenticated.tryEmit(false)
	}

	fun onDeauthenticate() {
		repository2.isAuthenticated.tryEmit(false)
	}



	private val objectMapper = jsonMapper { addModule(kotlinModule()) }.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

	private fun debug() {
		CoroutineScope(Dispatchers.Default).launch {
			repository2.getDefaultChapterId().collectLatest {
				repository2.getChapterFromIdAsFlow(it).collect { notebook ->
					updateWidget(notebook?.noteList?.map { it.toLite() } ?: listOf())
				}
			}
		}
	}

	suspend fun updateWidget(noteList: List<NoteObjectLite>) {
		// Iterate through all the available glance id's.
		GlanceAppWidgetManager(repository2.context).getGlanceIds(HomeWidget::class.java).forEach { glanceId ->
			updateAppWidgetState(repository2.context, glanceId) { prefs ->
				prefs.clear()
				noteList.forEach {
					prefs[stringPreferencesKey(it.id.toString())] = objectMapper.writeValueAsString(it)
				}
			}
		}
		HomeWidget().updateAll(repository2.context)
	}}
