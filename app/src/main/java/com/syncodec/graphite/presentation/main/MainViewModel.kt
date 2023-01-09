package com.syncodec.graphite.presentation.main

import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
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
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.syncodec.graphite.BaseApplication
import com.syncodec.graphite.di.model.BaseObject
import com.syncodec.graphite.di.model.BucketObject
import com.syncodec.graphite.di.model.BucketType
import com.syncodec.graphite.di.model.ChapterObject
import com.syncodec.graphite.di.model.NoteObjectLite
import com.syncodec.graphite.di.model.TagObject
import com.syncodec.graphite.di.repository.RealmNotInitializedException
import com.syncodec.graphite.di.repository.Repository2
import com.syncodec.graphite.di.repository.RepositoryState
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

	val baseObject: MutableState<BaseObject?> = mutableStateOf(null)
	val defaultNotebookId : MutableState<RealmUUID?> = mutableStateOf(null)
	val chapterObject : MutableState<ChapterObject?> = mutableStateOf(null)

	val notebookList : SnapshotStateList<ChapterObject> = mutableStateListOf()
	val notebookOrderList : SnapshotStateList<RealmUUID> = mutableStateListOf()

	val noteList : SnapshotStateList<NoteObjectLite> = mutableStateListOf()

	val bucketObjectList : SnapshotStateList<BucketObject> = mutableStateListOf()
	val bucketObjectOrderList : SnapshotStateList<RealmUUID> = mutableStateListOf()

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

		viewModelScope.launch(Dispatchers.Default) {
			if (repositoryState.value != RepositoryState.SUCCESS) this.cancel()
			repository2.getBaseObjectAsFlow().collectLatest {
				baseObject.value = it
				it?.bucketIdOrderList?.let {
					withContext(Dispatchers.Main) {
						bucketObjectOrderList.clear()
						bucketObjectOrderList.addAll(it)
					}
				}

				it?.notebookIdOrderList?.let {
					withContext(Dispatchers.Main) {
						notebookOrderList.clear()
						notebookOrderList.addAll(it)
					}
				}
			}
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

	fun putNotebook(title : String, description : String, color : Color?, bitmap : Bitmap?) {
		CoroutineScope(Dispatchers.Default).launch {
			try {
				if (notebookList.size >= 3 && ! BaseApplication.isPro.value) {
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
			if (bucketObjectList.find { it.bucketType == bucketType.name } != null && ! BaseApplication.isPro.value) {
				val bucket = when (bucketType) {
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

	fun onReorderBucketList(bucketIdList:List<RealmUUID>) {
		CoroutineScope(Dispatchers.Default).launch {
			repository2.reorderBucketList(bucketIdList) { _, _ -> }
		}
	}

	fun onReorderNotebookList(notebookIdList:List<RealmUUID>) {
		CoroutineScope(Dispatchers.Default).launch {
			repository2.reorderNotebookList(notebookIdList) { _, _ -> }
		}
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

	suspend fun updateWidget(noteList : List<NoteObjectLite>) {
		// Iterate through all the available glance id's.
		GlanceAppWidgetManager(repository2.context)
			.getGlanceIds(HomeWidget::class.java)
			.forEach { glanceId ->
				updateAppWidgetState(repository2.context, glanceId) { prefs ->
					prefs.clear()
					noteList.forEach {
						prefs[stringPreferencesKey(it.id.toString())] = objectMapper.writeValueAsString(it)
					}
				}
			}
		HomeWidget().updateAll(repository2.context)
	}
}
