package com.syncodec.graphite.presentation.notebook

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syncodec.graphite.di.Repository
import com.syncodec.graphite.di.model.ChapterObject
import com.syncodec.graphite.di.model.TagObject
import com.syncodec.graphite.presentation.notebook.composable.bottomSheet.BucketBottomSheetType
import com.syncodec.graphite.utils.encodeBase64
import io.realm.kotlin.types.ObjectId
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.cancellable


class NotebookViewModel : ViewModel() {

	var chapterObject: MutableState<ChapterObject?> = mutableStateOf(null)
	private var currentChapterId: ObjectId? = null

	val tagObjectList: SnapshotStateList<TagObject> = mutableStateListOf()

	val showEditChapterDialog: MutableState<Boolean> = mutableStateOf(false)
	val showManageTagDialog: MutableState<Boolean> = mutableStateOf(false)

	var bottomSheetType: MutableState<BucketBottomSheetType> = mutableStateOf(BucketBottomSheetType.MENU)

	val rootChapterId: MutableState<ObjectId?> = mutableStateOf(null)
	val rootColor: MutableState<Color?> = mutableStateOf(null)

	fun initNotebook(chapterId: ObjectId) {
		viewModelScope.launch(Dispatchers.IO) {
			loadChapter(chapterId = chapterId)
		}
		viewModelScope.launch(Dispatchers.IO) {
			Repository.getAllTagAsFlow().collect {
				withContext(Dispatchers.Main) {
					tagObjectList.clear()
					tagObjectList.addAll(it)
				}
			}
		}
	}

	fun putChapter(title: String, description: String, color: Color?, bitmap: Bitmap?) {
		if (chapterObject.value != null) {
			CoroutineScope(Dispatchers.IO).launch {
				try {
					ChapterObject().apply {
						this.title = title
						this.description = description
						this.color = color?.toArgb()
						this.thumbnail = bitmap?.encodeBase64()

						this.parentChapterId = chapterObject.value?.id

						Repository.putChapter(this.parentChapterId, this)
					}
				} catch (e: Exception) {
//	    		    TODO Show error message
					e.printStackTrace()
				}
			}
		} else {
//		    TODO Show error message
		}
	}

	fun updateChapter(
		title: String,
		description: String?,
		color: Color?,
		thumbnail: Bitmap?,
		isFavourite: Boolean,
		isLocked: Boolean,
	) {
		Repository.updateChapter(
			id = chapterObject.value!!.id,
			title = title,
			description = description,
			color = color?.toArgb(),
			thumbnail = thumbnail?.encodeBase64(),
			isFavourite = isFavourite,
			isLocked = isLocked
		)
	}

	fun loadChapter(chapterId: ObjectId) {
		currentChapterId = chapterId
		viewModelScope.launch {
			Repository.getChapterAsFlow(chapterId).cancellable().collect { it ->
				if (it != null) {
					if (it.id == currentChapterId) {
						withContext(Dispatchers.Main) {
							chapterObject.value = it
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

	fun toggleLock() {
		if (chapterObject.value != null) {
			try {
				Repository.updateChapter(
					id = chapterObject.value!!.id,
					title = chapterObject.value!!.title,
					description = chapterObject.value!!.description,
					color = chapterObject.value!!.color,
					thumbnail = chapterObject.value!!.thumbnail,
					isFavourite = chapterObject.value!!.isFavourite,
					isLocked = !chapterObject.value!!.isLocked
				)
			} catch (e: Exception) {
//				TODO Show error
				e.printStackTrace()
				Log.i("npr71", "Error updating chapter")
			}
		}
	}

	fun toggleFavourite() {
		if (chapterObject.value != null) {
			try {
				Repository.updateChapter(
					id = chapterObject.value!!.id,
					title = chapterObject.value!!.title,
					description = chapterObject.value!!.description,
					color = chapterObject.value!!.color,
					thumbnail = chapterObject.value!!.thumbnail,
					isFavourite = !chapterObject.value!!.isFavourite,
					isLocked = chapterObject.value!!.isLocked
				)
			} catch (e: Exception) {
//				TODO Show error
				e.printStackTrace()
				Log.i("npr71", "Error updating chapter")
			}
		}
	}

	fun updateTagConnection(tagObjectId : ObjectId) {
		Repository.updateTagConnection(tagObjectId = tagObjectId, chapterObject.value?.id)
	}
}
