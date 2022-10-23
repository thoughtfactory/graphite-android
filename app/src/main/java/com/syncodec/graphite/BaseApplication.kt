package com.syncodec.graphite

import android.app.Application
import com.syncodec.graphite.utils.DataStoreInstance
import dagger.hilt.android.HiltAndroidApp
import java.io.File


@HiltAndroidApp
class BaseApplication : Application() {

	lateinit var dataStore: DataStoreInstance

	private lateinit var ROOT: String

	private val DATA: String = "data"
		get() = "$ROOT/$field"

	private val ATTACHMENT_DIR = "attachment"
		get() = "$DATA/$field"

	override fun onCreate() {
		super.onCreate()

//		repository = RepositoryModule_ProvideRepositoryFactory()

		ROOT = applicationContext.filesDir.path
		File(DATA).mkdirs()
		File(ATTACHMENT_DIR).mkdirs()



//		Purchases.debugLogsEnabled = true
//		Purchases.configure(PurchasesConfiguration.Builder(this, BuildConfig.REVENUE_CAT_API_KEY).build())

//		Purchases.sharedInstance.getOfferingsWith(
//			onError = { error ->
//				/* Optional error handling */
//				Log.e("npr71", "Error getting offerings: $error")
//			},
//			onSuccess = { offerings ->
//				// Display current offering with offerings.current
//				Log.i("npr71", "onSuccess: ${offerings.all}")
//			}
//		)

		dataStore = DataStoreInstance(this)

//		migrate()

		updateQuoteData()
	}

	private fun migrate() {
//		CoroutineScope(Dispatchers.Main).launch {
//
//			repository.noteRepository.getDefaultNotebookId().collect {
//				when (it) {
//					null -> {
//						ChapterObject().also { chapterObject ->
//							chapterObject.title = "Diary"
//							chapterObject.description = "Default diary. Every notes will be saved in this notebook by default"
//							chapterObject.color = getRandomColor().toArgb()
//
//							repository.noteRepository.putChapter(null, chapterObject)
//
//							BaseObject().also { baseObject ->
//								baseObject.defaultChapterId = chapterObject.id
//
//								CoroutineScope(Dispatchers.IO).launch {
//									repository.noteRepository.putBase(baseObject)
//								}
//							}
//						}
//					}
//				}
//			}
//		}
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
