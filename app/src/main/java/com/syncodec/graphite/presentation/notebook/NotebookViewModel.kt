package com.syncodec.graphite.presentation.notebook

import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syncodec.graphite.BaseApplication
import com.syncodec.graphite.di.model.ChapterObject
import com.syncodec.graphite.di.model.ChapterObjectLite
import com.syncodec.graphite.di.model.NoteObjectLite
import com.syncodec.graphite.di.model.TagObject
import com.syncodec.graphite.di.repository.CallbackStatus
import com.syncodec.graphite.di.repository.RealmNotInitializedException
import com.syncodec.graphite.di.repository.Repository2
import com.syncodec.graphite.di.repository.RepositoryState
import com.syncodec.graphite.presentation.notebook.composable.bottomSheet.NotebookBottomSheetType
import com.syncodec.graphite.utils.decodeBase64ToBitmap
import com.syncodec.graphite.utils.encodeBase64
import dagger.hilt.android.lifecycle.HiltViewModel
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


@HiltViewModel
class NotebookViewModel @Inject constructor(private val repository2 : Repository2) : ViewModel() {

	val repositoryState = repository2.repositoryState

	val defaultChapterId : MutableState<RealmUUID?> = mutableStateOf(null)

	val chapterId : MutableState<RealmUUID?> = mutableStateOf(null)
	val createdTimestamp : MutableState<Long?> = mutableStateOf(null)
	val modifiedTimestamp : MutableState<Long?> = mutableStateOf(null)
	val title : MutableState<String?> = mutableStateOf(null)
	val description : MutableState<String?> = mutableStateOf(null)
	val color : MutableState<Color?> = mutableStateOf(null)
	val thumbnail : MutableState<Bitmap?> = mutableStateOf(null)
	val isFavourite : MutableState<Boolean?> = mutableStateOf(null)
	val isLocked : MutableState<Boolean?> = mutableStateOf(null)

	val chapterObjectLite: MutableState<ChapterObjectLite?> = mutableStateOf(null)

	val chapterObjectList: SnapshotStateList<ChapterObject> = mutableStateListOf()
	val noteObjectList : SnapshotStateList<NoteObjectLite> = mutableStateListOf()

	val currentChapterId : MutableState<RealmUUID?> = mutableStateOf(null)
	val parentChapterId : MutableState<RealmUUID?> = mutableStateOf(null)
	val parentChapterObjectList : SnapshotStateList<ChapterObjectLite> = mutableStateListOf()

	val tagObjectList : SnapshotStateList<TagObject> = mutableStateListOf()

	var bottomSheetType : MutableState<NotebookBottomSheetType> = mutableStateOf(NotebookBottomSheetType.MENU)

	val rootChapterId : MutableState<RealmUUID?> = mutableStateOf(null)
	val rootColor : MutableState<Color?> = mutableStateOf(null)

	val isSelected : MutableState<Boolean> = mutableStateOf(false)
	val selectedObjectIdList : SnapshotStateList<RealmUUID> = mutableStateListOf()

	val isPro = BaseApplication.isPro.value

	init {
		viewModelScope.launch(Dispatchers.Default) {
			repositoryState.collect {
				when (it) {
					RepositoryState.INIT -> null
					RepositoryState.LOCKED -> null
					RepositoryState.LOADING -> null
					RepositoryState.SUCCESS -> repository2.getDefaultChapterId().collect { defaultChapterId.value = it }
					RepositoryState.ERROR -> null
				}
			}
		}
	}

	fun initNotebook(chapterId : RealmUUID) {
		viewModelScope.launch(Dispatchers.Default) {
			repositoryState.collect {
				when (it) {
					RepositoryState.INIT -> null
					RepositoryState.LOCKED -> null
					RepositoryState.LOADING -> null
					RepositoryState.SUCCESS -> onRepositoryStateSuccess(chapterId = chapterId)
					RepositoryState.ERROR -> null
				}
			}
		}
	}

	private fun onRepositoryStateSuccess(chapterId : RealmUUID) {
		viewModelScope.launch(Dispatchers.Default) {
			if (repositoryState.value != RepositoryState.SUCCESS) this.cancel()
			getAndLoadChapter(chapterId = chapterId)
		}
		viewModelScope.launch(Dispatchers.Default) {
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
		if (this.currentChapterId.value != null) {
			if (isPro) {
				CoroutineScope(Dispatchers.Default).launch {
					try {
						ChapterObject().apply {
							this.title = title
							this.description = description
							this.color = color?.toArgb()
							this.thumbnail = bitmap?.encodeBase64()

							this.parentId = this@NotebookViewModel.currentChapterId.value

							repository2.putChapter(this.parentId, this) { _, _ -> }
						}
					} catch (e : Exception) {
//	    		    TODO Show error message
//						e.printStackTrace()
					}
				}
			} else {
				viewModelScope.launch(Dispatchers.Main) {
					Toast.makeText(repository2.context, "Join Graphite Pro to add chapters in notebook", Toast.LENGTH_SHORT).show()
				}
			}
		} else {
			viewModelScope.launch(Dispatchers.Main) {
				Toast.makeText(repository2.context, "Error adding chapter", Toast.LENGTH_SHORT).show()
			}
		}
	}

	private fun updateChapter() {
		this.chapterId.value?.let {
			CoroutineScope(Dispatchers.Default).launch {
				try {
					ChapterObject().apply {
						this.id = it
						this.title = this@NotebookViewModel.title.value
						this.description = this@NotebookViewModel.description.value
						this.color = this@NotebookViewModel.color.value?.toArgb()
						this.thumbnail = this@NotebookViewModel.thumbnail.value?.encodeBase64()
						this.isFavourite = this@NotebookViewModel.isFavourite.value ?: false
						this.isLocked = this@NotebookViewModel.isLocked.value ?: false

						this.parentId = this@NotebookViewModel.parentChapterId.value

						repository2.putChapter(this.parentId, this) { _, _ -> }
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

	fun getAndLoadChapter(chapterId : RealmUUID?) {
		this.currentChapterId.value = chapterId

		viewModelScope.launch(Dispatchers.Default) {
			if (repositoryState.value != RepositoryState.SUCCESS) this.cancel()
			repository2.getChapterFromIdAsFlow(id = chapterId).collect {
//				!! Is it correct to not cancel the flow if chapterObject is null?
				if (it != null) {
					getParentChapter(it.id)
					if (it.id == currentChapterId.value) {
						withContext(Dispatchers.Default) {
							this@NotebookViewModel.chapterObjectLite.value = it.toLite()

							this@NotebookViewModel.chapterId.value = it.id
							this@NotebookViewModel.createdTimestamp.value = it.createdTimestamp
							this@NotebookViewModel.modifiedTimestamp.value = it.modifiedTimestamp
							this@NotebookViewModel.title.value = it.title
							this@NotebookViewModel.description.value = it.description
							this@NotebookViewModel.color.value = it.color?.let { color -> Color(color) }
							this@NotebookViewModel.thumbnail.value = it.thumbnail?.decodeBase64ToBitmap()
							this@NotebookViewModel.isFavourite.value = it.isFavourite
							this@NotebookViewModel.isLocked.value = it.isLocked

							this@NotebookViewModel.parentChapterId.value = it.parentId

							this@NotebookViewModel.chapterObjectList.clear()
							this@NotebookViewModel.chapterObjectList.addAll(it.chapterList)
							this@NotebookViewModel.noteObjectList.clear()
							this@NotebookViewModel.noteObjectList.addAll(it.noteList.map { it.toLite() })

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

	private fun getParentChapter(id : RealmUUID) {
		repository2.getParentChapterList(id = id) { list, e ->
			viewModelScope.launch(Dispatchers.Main) {
				parentChapterObjectList.clear()
				list?.let {
					parentChapterObjectList.addAll(it)
					chapterObjectLite.value?.let { parentChapterObjectList.add(0, it) }
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
		this.chapterId.value?.let {
			repository2.putDefaultChapterId(it) { callbackStatus ->
				if (callbackStatus == CallbackStatus.SUCCESS) Toast.makeText(repository2.context, "Default chapter updated", Toast.LENGTH_SHORT).show()
			}
		}
	}

	fun delete() {
		try {
			val toDeleteRealmUUIDList = selectedObjectIdList.toList()
			repository2.delete(toDeleteRealmUUIDList)
			selectedObjectIdList.clear()
			isSelected.value = false
		} catch (e : Exception) {

		}
	}

	fun delete(objectId: RealmUUID) {
		try {
			repository2.delete(listOf(objectId))
		} catch (e : Exception) {

		}
	}

	fun updateTagConnection(tagRealmUUID : RealmUUID) {
//		Repository.updateTagConnection(tagRealmUUID = tagRealmUUID, chapterObject.value?.id)
	}
}
