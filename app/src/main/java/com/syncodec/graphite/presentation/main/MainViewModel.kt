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
import com.syncodec.graphite.di.Repository
import com.syncodec.graphite.di.model.*
import com.syncodec.graphite.utils.*
import io.realm.kotlin.types.ObjectId
import kotlinx.coroutines.*
import org.json.JSONArray


class MainViewModel : ViewModel() {

	val defaultNotebookIdFlow = Repository.getDefaultNotebookId()

	val defaultNotebookId: MutableState<ObjectId?> = mutableStateOf(null)

	val chapterObject: MutableState<ChapterObject?> = mutableStateOf(null)
	val notebookList = Repository.getAllNotebookAsFlow()
	val noteList: SnapshotStateList<NoteObjectLite> = mutableStateListOf()
	val bucketObjectList = Repository.getAllBucketAsFlow()
	val quoteObject: MutableState<QuoteObject?> = mutableStateOf(null)

	var filterInclusivityState: MutableState<Int> = mutableStateOf(0)
	var sortOn: MutableState<SortOn> = mutableStateOf(SortOn.TIMESTAMP)
	var sortBy: MutableState<SortBy> = mutableStateOf(SortBy.DESCENDING)
	var viewType: MutableState<ViewType> = mutableStateOf(ViewType.LIST)

	init {
		initNotebook()
		initQuote()
	}

	private fun initNotebook() {
		viewModelScope.launch(Dispatchers.IO) {
			defaultNotebookIdFlow.collect {
				withContext(Dispatchers.Main) {
					defaultNotebookId.value = it
				}

				if (it != null) {
					Repository.getNotebookAsFlow(it).collect {
						withContext(Dispatchers.Main) {
							chapterObject.value = it
							noteList.clear()
							it?.noteList?.map { it.toLite() }?.let { it1 -> noteList.addAll(it1) }
						}
					}
				}
			}
		}
	}

	private fun initQuote() {
//		viewModelScope.launch(Dispatchers.Main) {
//			Repository.getQuoteByDate(date = quoteTimestampToKey(getToday() - (13L * 24 * 60 * 60 * 1000))) {
//				quoteObject.value = it
//				if ((it.bg == null) && (it.date != null)) {
//					Repository.getQuoteBgFromNetwork(date = it.date!!) {
//						Repository.getQuoteByDate(date = quoteTimestampToKey(getToday() - (13L * 24 * 60 * 60 * 1000))) {
//							quoteObject.value = it
//						}
//					}
//				}
//			}
//		}
	}

	fun putNotebook(title: String, description: String, color: Color?, bitmap: Bitmap?) {
		CoroutineScope(Dispatchers.IO).launch {
			try {
				ChapterObject().apply {
					this.title = title
					this.description = description
					this.color = color?.toArgb()

					Repository.putChapter(this)
				}
			} catch (e: Exception) {
//	    		TODO Show error message
				e.printStackTrace()
			}
		}
	}

	fun putBucket(
		title: String,
		description: String,
		bucketType: BucketType,
	) {
		BucketObject().apply {
			this.title = title
			this.description = description
			this.bucketType = bucketType.name

			Repository.putBucket(this)
		}
	}

//	fun getDefaultNoteList(chapterId: String?): Flow<List<NoteObjectLite>> {
//		return Repository.realm.query<NoteObject>("chapterId == $0", chapterId).find().asFlow().map { it.list.map { it.toLite() } }
//	}

	fun addDebugNotes(notebookId: String) {
		CoroutineScope(Dispatchers.IO).launch {
			val jsonArray = JSONArray(debugNoteData)
			for (i in 0 until jsonArray.length()) {
				try {
					NoteObject.getInstance().apply {
						val obj = jsonArray.getJSONObject(i)
						this.title = obj.optString("author")
						this.contentThumbnail = obj.optString("text")
						this.content =
							"{\"type\":\"doc\",\"content\":[{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":\"left\"},\"content\":[{\"type\":\"text\",\"text\":\"${
								obj.optString("text").repeat(500)
							}\"}]}]}"

						defaultNotebookId.value?.let { Repository.putNote(it, this){} }
						if (i % 100 == 0) {
							Log.i("npr71", "$i/${jsonArray.length()}")
						}
					}
				} catch (exception: Exception) {
					exception.printStackTrace()
				}
				delay(100)
			}
			Log.i("npr71", "done")
		}
	}
}
