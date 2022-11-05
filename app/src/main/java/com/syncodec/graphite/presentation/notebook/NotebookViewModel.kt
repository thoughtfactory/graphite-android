package com.syncodec.graphite.presentation.notebook

import android.graphics.Bitmap
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syncodec.graphite.di.model.ChapterObject
import com.syncodec.graphite.di.model.ChapterObjectLite
import com.syncodec.graphite.di.model.TagObject
import com.syncodec.graphite.di.repository.CallbackStatus
import com.syncodec.graphite.di.repository.RealmNotInitializedException
import com.syncodec.graphite.di.repository.Repository2
import com.syncodec.graphite.di.repository.RepositoryState
import com.syncodec.graphite.presentation.notebook.composable.bottomSheet.NotebookBottomSheetType
import com.syncodec.graphite.utils.decodeBase64ToBitmap
import com.syncodec.graphite.utils.encodeBase64
import dagger.hilt.android.lifecycle.HiltViewModel
import io.realm.kotlin.types.ObjectId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


@HiltViewModel
class NotebookViewModel @Inject constructor(private val repository2 : Repository2) : ViewModel() {

	val repositoryState = repository2.repositoryState

	val defaultChapterId : MutableState<ObjectId?> = mutableStateOf(null)

	val chapterObject : MutableState<ChapterObject?> = mutableStateOf(null)

	val id : MutableState<ObjectId?> = mutableStateOf(null)
	val createdTimestamp : MutableState<Long?> = mutableStateOf(null)
	val modifiedTimestamp : MutableState<Long?> = mutableStateOf(null)
	val title : MutableState<String?> = mutableStateOf(null)
	val description : MutableState<String?> = mutableStateOf(null)
	val color : MutableState<Color?> = mutableStateOf(null)
	val thumbnail : MutableState<Bitmap?> = mutableStateOf(null)
	val isFavourite : MutableState<Boolean?> = mutableStateOf(null)
	val isLocked : MutableState<Boolean?> = mutableStateOf(null)

	val currentChapterId : MutableState<ObjectId?> = mutableStateOf(null)
	val parentChapterId : MutableState<ObjectId?> = mutableStateOf(null)
	val parentChapterObjectList : SnapshotStateList<ChapterObjectLite> = mutableStateListOf()

	val tagObjectList : SnapshotStateList<TagObject> = mutableStateListOf()

	var bottomSheetType : MutableState<NotebookBottomSheetType> = mutableStateOf(NotebookBottomSheetType.MENU)

	val rootChapterId : MutableState<ObjectId?> = mutableStateOf(null)
	val rootColor : MutableState<Color?> = mutableStateOf(null)

	val isSelected : MutableState<Boolean> = mutableStateOf(false)
	val selectedObjectIdList : SnapshotStateList<ObjectId> = mutableStateListOf()


	init {
		viewModelScope.launch(Dispatchers.IO) {
			repositoryState.collect {
				when (it) {
					RepositoryState.INIT -> Log.d("NotebookViewModel", "Init")
					RepositoryState.LOADING -> Log.d("NotebookViewModel", "Loading")
					RepositoryState.SUCCESS -> repository2.getDefaultChapterId().collect { defaultChapterId.value = it }
					RepositoryState.ERROR -> Log.d("NotebookViewModel", "Error")
				}
			}
		}
	}

	fun initNotebook(chapterId : ObjectId) {
		viewModelScope.launch(Dispatchers.IO) {
			repositoryState.collect {
				when (it) {
					RepositoryState.INIT -> Log.d("NotebookViewModel", "Init")
					RepositoryState.LOADING -> Log.d("NotebookViewModel", "Loading")
					RepositoryState.SUCCESS -> onRepositoryStateSuccess(chapterId = chapterId)
					RepositoryState.ERROR -> Log.d("NotebookViewModel", "Error")
				}
			}
		}
	}

	private fun onRepositoryStateSuccess(chapterId : ObjectId) {
		viewModelScope.launch(Dispatchers.IO) {
			if (repositoryState.value != RepositoryState.SUCCESS) this.cancel()
			getAndLoadChapter(chapterId = chapterId)
		}
		viewModelScope.launch(Dispatchers.IO) {
			if (repositoryState.value != RepositoryState.SUCCESS) this.cancel()
			try {
				repository2.getAllTagAsFlow().collect {
					withContext(Dispatchers.Main) {
						tagObjectList.clear()
						tagObjectList.addAll(it)
					}
				}
			} catch (e : RealmNotInitializedException) {
			} catch (e : Exception) {
			}
		}
	}


	fun putChapter(title : String?, description : String?, color : Color?, bitmap : Bitmap?) {
		if (chapterObject.value != null) {
			CoroutineScope(Dispatchers.IO).launch {
				try {
					ChapterObject().apply {
						this.title = title
						this.description = description
						this.color = color?.toArgb()
						this.thumbnail = bitmap?.encodeBase64()

						this.parentChapterId = chapterObject.value?.id

						repository2.putChapter(this.parentChapterId, this) { _, _ -> }
					}
				} catch (e : Exception) {
//	    		    TODO Show error message
					e.printStackTrace()
				}
			}
		} else {
//		    TODO Show error message
		}
	}

	private fun updateChapter() {
		this.id.value?.let {
			CoroutineScope(Dispatchers.IO).launch {
				try {
					ChapterObject().apply {
						this.id = it
						this.title = this@NotebookViewModel.title.value
						this.description = this@NotebookViewModel.description.value
						this.color = this@NotebookViewModel.color.value?.toArgb()
						this.thumbnail = this@NotebookViewModel.thumbnail.value?.encodeBase64()
						this.isFavourite = this@NotebookViewModel.isFavourite.value ?: false
						this.isLocked = this@NotebookViewModel.isLocked.value ?: false

						this.parentChapterId = this@NotebookViewModel.parentChapterId.value

						repository2.putChapter(this.parentChapterId, this) { _, _ -> }
					}
				} catch (e : Exception) {

				}
			}
		}
	}

	fun updateChapter(title : String?, description : String?, color : Color?, bitmap : Bitmap?) {
		this.title.value = title
		this.description.value = description
		this.color.value = color
		this.thumbnail.value = bitmap
		updateChapter()
	}

	fun getAndLoadChapter(chapterId : ObjectId) {
		this.currentChapterId.value = chapterId

		viewModelScope.launch(Dispatchers.IO) {
			if (repositoryState.value != RepositoryState.SUCCESS) this.cancel()
			repository2.getChapterFromIdAsFlow(id = chapterId).collect {
//				WARN: Is it correct to not cancel the flow if chapterObject is null?
				if (it != null) {
					getParentChapter(it.id)
					if (it.id == currentChapterId.value) {
						withContext(Dispatchers.Main) {
							this@NotebookViewModel.chapterObject.value = it

							this@NotebookViewModel.id.value = it.id
							this@NotebookViewModel.createdTimestamp.value = it.createdTimestamp
							this@NotebookViewModel.modifiedTimestamp.value = it.modifiedTimestamp
							this@NotebookViewModel.title.value = it.title
							this@NotebookViewModel.description.value = it.description
							this@NotebookViewModel.color.value = it.color?.let { color -> Color(color) }
							this@NotebookViewModel.thumbnail.value = it.thumbnail?.decodeBase64ToBitmap()
							this@NotebookViewModel.isFavourite.value = it.isFavourite
							this@NotebookViewModel.isLocked.value = it.isLocked

							this@NotebookViewModel.parentChapterId.value = it.parentChapterId

							if (rootChapterId.value == null) {
								rootChapterId.value = it.id
							}
							if (rootColor.value == null) {
								rootColor.value = it.color?.let { it1 -> Color(it1) } ?: Color.Unspecified
							}
						}
					} else {
						this.cancel()
					}
				}
			}
		}
	}

	private fun getParentChapter(id : ObjectId) {
		repository2.getParentChapterList(id = id) { list, e ->
			viewModelScope.launch(Dispatchers.Main) {
				parentChapterObjectList.clear()
				list?.let {
					parentChapterObjectList.addAll(it)
					chapterObject.value?.let { parentChapterObjectList.add(0, it.toLite()) }
				}
			}
		}
	}

	fun toggleLock() {
		isLocked.value = isLocked.value?.not()
		updateChapter()
	}

	fun toggleFavourite() {
		isFavourite.value = isFavourite.value?.not()
		updateChapter()
	}

	fun setDefaultChapter() {
		this.id.value?.let {
			repository2.putDefaultChapterId(it) { callbackStatus ->
				if (callbackStatus == CallbackStatus.SUCCESS) Toast.makeText(repository2.context, "Default chapter updated", Toast.LENGTH_SHORT).show()
			}
		}
	}

	fun updateTagConnection(tagObjectId : ObjectId) {
//		Repository.updateTagConnection(tagObjectId = tagObjectId, chapterObject.value?.id)
	}
}
