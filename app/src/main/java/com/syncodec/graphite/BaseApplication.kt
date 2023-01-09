package com.syncodec.graphite

import android.app.Application
import android.content.Intent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.revenuecat.purchases.CacheFetchPolicy
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration
import com.revenuecat.purchases.PurchasesError
import com.revenuecat.purchases.interfaces.ReceiveCustomerInfoCallback
import com.syncodec.graphite.presentation.main.MainActivity
import com.syncodec.graphite.service.WatchdogService
import com.syncodec.graphite.service.WatchdogServiceConnectionManager
import com.syncodec.graphite.utils.DataStoreInstance
import com.syncodec.graphite.utils.alice.Alice
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
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

//		watchWatchdog()

		ROOT = applicationContext.filesDir.path
		File(DATA).mkdirs()
		File(ATTACHMENT_DIR).mkdirs()

		dataStore = DataStoreInstance(this)
		Purchases.debugLogsEnabled = false
		val auth = Firebase.auth

		val purchasesConfiguration = PurchasesConfiguration
			.Builder(this, Alice.decrypt(BuildConfig.REVENUE_CAT_API_KEY, "lt3(3x4R7M^107!&4E74Z%*o8cp2i7y@") ?: "")
			.appUserID(auth.currentUser?.uid)
			.build()
		Purchases.configure(purchasesConfiguration)

		auth.currentUser?.uid?.let { uid ->
			CoroutineScope(Dispatchers.Default).launch {
				dataStore.getSuperExpiryTime.collect { superExpiryTimeString ->
					try {
						val currentTimestamp = System.currentTimeMillis()
						if (superExpiryTimeString == "") {
							getRevenueCatInfo(auth)
						} else {
							if (superExpiryTimeString.toLong() > currentTimestamp) isPro.tryEmit(true) else getRevenueCatInfo(auth)
						}
					} catch (e : Exception) {
						getRevenueCatInfo(auth)
					}
				}
			}
		}
	}

//	fun watchWatchdog() {
//		CoroutineScope(Dispatchers.Default).launch {
//			delay(5000)
//			while (true) {
//				if (MainActivity.isInStack) {
//					startWatchdog()
////		    		TODO: Set this to 5 seconds
//					delay(30000)
//				} else {
//					watchdogServiceConnection.unbindFromService()
//					break
//				}
//			}
//		}
//	}

//	var watchdogService : WatchdogService? = null
//	private val watchdogServiceConnection = WatchdogServiceConnectionManager(this) {
//		watchdogService = it
//	}
//
//	private fun startWatchdog() {
//		Intent(this.applicationContext, WatchdogService::class.java).apply {
//			watchdogServiceConnection.bindToService()
//		}
//	}

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
							isPro.tryEmit(customerInfo.entitlements["pro"]?.isActive == true)
						}
					}
				)
			}
	}

	companion object {
		val isPro : MutableStateFlow<Boolean> = MutableStateFlow(false)
	}
}
