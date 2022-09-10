package com.syncodec.graphite

import android.app.Application
import androidx.compose.ui.graphics.toArgb
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.syncodec.graphite.di.model.BaseObject
import com.syncodec.graphite.di.model.ChapterObject
import com.syncodec.graphite.di.Repository
import com.syncodec.graphite.utils.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File


class BaseApplication : Application() {

	lateinit var dataStore: DataStoreInstance

	private lateinit var ROOT: String

	private val DATA: String = "data"
		get() = "$ROOT/$field"

	private val ATTACHMENT_DIR = "attachment"
		get() = "$DATA/$field"

	override fun onCreate() {
		super.onCreate()

		ROOT = applicationContext.filesDir.path
		File(DATA).mkdirs()
		File(ATTACHMENT_DIR).mkdirs()

		dataStore = DataStoreInstance(this)

		migrate()

		updateQuoteData()
	}

	private fun migrate() {
		CoroutineScope(Dispatchers.Main).launch {
			Repository.getDefaultNotebookId().collect {
				when (it) {
					null -> {
						ChapterObject().also { chapterObject ->
							chapterObject.title = "Diary"
							chapterObject.description =
								"Default diary. Every notes will be saved in this notebook by default"
							chapterObject.color = getRandomColor().toArgb()

							Repository.putChapter(null, chapterObject)

							BaseObject().also { baseObject ->
								baseObject.defaultChapterId = chapterObject.id

								CoroutineScope(Dispatchers.IO).launch {
									Repository.putBase(baseObject)
								}
							}
						}
					}
				}
			}
		}
	}

	private fun updateQuoteData() {
//		CoroutineScope(Dispatchers.IO).launch {
//			val storage = Firebase.storage("gs://graphite-diary.appspot.com")
//			val storageRef = storage.reference
//			val quoteDirRef = storageRef.child("server/enQuote")
//
//			val quoteKeyList = Repository.getQuoteKeyList()
//
//			val maxDateSaved = quoteKeyList.maxOfOrNull { it?.let { it1 -> quoteKeyToTimestamp(it1) } ?: 0 } ?: 0
//			val minDate = getToday() - (14L * 24 * 60 * 60 * 1000)
//			val maxDate = getToday() + (7L * 24 * 60 * 60 * 1000)
//
////			Request for data only if next 3 days data is unavailable
//			if (maxDateSaved < maxDate) {
//				quoteDirRef.listAll()
//					.addOnSuccessListener { dateList ->
//						dateList.prefixes.forEach { date ->
//							if (date.name !in quoteKeyList && quoteKeyToTimestamp(date.name) ?: 0 > minDate) {
//								Repository.getQuoteFromNetwork(date = date.name) {
//									CoroutineScope(Dispatchers.IO).launch {
//										Repository.putQuote(it)
//									}
//								}
//							}
//						}
//					}
//			}
//		}
	}
}
