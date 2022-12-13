package com.syncodec.graphite

import android.app.Application
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.revenuecat.purchases.CacheFetchPolicy
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration
import com.revenuecat.purchases.PurchasesError
import com.revenuecat.purchases.interfaces.ReceiveCustomerInfoCallback
import com.syncodec.graphite.utils.DataStoreInstance
import com.syncodec.graphite.utils.alice.Alice
import com.syncodec.graphite.widget.home.DailyReadWorkerTask
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.TimeUnit


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

//		Instabug.Builder(this, "2d6140579e95c85aa01fde896537cd72")
//			.setInvocationEvents(InstabugInvocationEvent.SHAKE, InstabugInvocationEvent.FLOATING_BUTTON)
//			.build()

		dataStore = DataStoreInstance(this)
		Purchases.debugLogsEnabled = true
		val auth = Firebase.auth
		val purchasesConfiguration = PurchasesConfiguration
			.Builder(this, Alice.decrypt(BuildConfig.REVENUE_CAT_API_KEY, "lt3(3x4R7M^107!&4E74Z%*o8cp2i7y@") ?: "")
			.appUserID(auth.currentUser?.uid)
			.build()
		Purchases.configure(purchasesConfiguration)

		debug()


		if (auth.currentUser != null) {
			CoroutineScope(Dispatchers.Default).launch {
				dataStore.getSuperExpiryTime.collect {
					try {
						val currentTimestamp = System.currentTimeMillis()
						if (it == "") {
							getRevenueCatInfo(auth)
						} else {
							if (it.toLong() > currentTimestamp) {
								isPro.value = true
							} else {
								getRevenueCatInfo(auth)
							}
						}
					} catch (e : Exception) {
						getRevenueCatInfo(auth)
					}
				}
			}
		}
	}

	private fun getRevenueCatInfo(auth : FirebaseAuth) {
		Purchases
			.sharedInstance
			.apply {
				setAttributes(mapOf("\$email" to auth.currentUser?.email))
				getCustomerInfo(
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
	}

	fun debug() {
//		execute()
	}

	fun execute() = enqueueWorker()

	private fun enqueueWorker() {
		WorkManager
			.getInstance(this)
			.enqueue(buildRequest())
	}

	private fun buildRequest() : OneTimeWorkRequest {
		// 1 day
		return OneTimeWorkRequestBuilder<DailyReadWorkerTask>().build()
	}

	companion object {
		val isPro : MutableState<Boolean> = mutableStateOf(false)
	}
}
