package com.syncodec.graphite

import android.app.Application
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.revenuecat.purchases.CacheFetchPolicy
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration
import com.revenuecat.purchases.PurchasesError
import com.revenuecat.purchases.interfaces.ReceiveCustomerInfoCallback
import com.syncodec.graphite.utils.DataStoreInstance
import dagger.hilt.android.HiltAndroidApp
import java.io.File


@HiltAndroidApp
class BaseApplication : Application() {

	lateinit var dataStore : DataStoreInstance

	private lateinit var ROOT : String

	private val DATA : String = "data"
		get() = "$ROOT/$field"

	private val ATTACHMENT_DIR = "attachment"
		get() = "$DATA/$field"

	override fun onCreate() {
		super.onCreate()

		ROOT = applicationContext.filesDir.path
		File(DATA).mkdirs()
		File(ATTACHMENT_DIR).mkdirs()

		dataStore = DataStoreInstance(this)

		Purchases.debugLogsEnabled = true
		val auth = Firebase.auth
		val purchasesConfiguration = PurchasesConfiguration
			.Builder(this, BuildConfig.REVENUE_CAT_API_KEY)
			.appUserID(auth.currentUser?.uid)
			.build()
		Purchases.configure(purchasesConfiguration)


		if (auth.currentUser != null) {
			Purchases.sharedInstance.getCustomerInfo(
				fetchPolicy = CacheFetchPolicy.NOT_STALE_CACHED_OR_CURRENT,
				callback = object : ReceiveCustomerInfoCallback {
					override fun onError(error : PurchasesError) {
					}

					override fun onReceived(customerInfo : CustomerInfo) {
						isPro.value = customerInfo.entitlements["pro"]?.isActive == true
					}
				}
			)
		}

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

	companion object {
		val isPro: MutableState<Boolean> = mutableStateOf(false)
	}
}
