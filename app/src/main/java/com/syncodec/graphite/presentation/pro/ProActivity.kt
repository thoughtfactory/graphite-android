package com.syncodec.graphite.presentation.pro

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.Package
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesError
import com.revenuecat.purchases.getOfferingsWith
import com.revenuecat.purchases.interfaces.LogInCallback
import com.revenuecat.purchases.interfaces.PurchaseCallback
import com.revenuecat.purchases.interfaces.UpdatedCustomerInfoListener
import com.revenuecat.purchases.models.StoreTransaction
import com.syncodec.graphite.BaseApplication
import com.syncodec.graphite.presentation.pro.composable.screen.SubscriptionScreen
import com.syncodec.graphite.presentation.ui.BaseContent
import com.syncodec.graphite.utils.DataStoreInstance
import com.syncodec.graphite.utils.tone
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@AndroidEntryPoint
class ProActivity : ComponentActivity(), UpdatedCustomerInfoListener {

	private val monthlyPackage : MutableState<Package?> = mutableStateOf(null)
	private val annualPackage : MutableState<Package?> = mutableStateOf(null)
	private val lifetimePackage : MutableState<Package?> = mutableStateOf(null)

	override fun onCreate(savedInstanceState : Bundle?) {
		super.onCreate(savedInstanceState)

		getProducts()

		setContent {
			BaseContent {
				val systemUiController = rememberSystemUiController()
				systemUiController.setStatusBarColor(MaterialTheme.colorScheme.background)
				systemUiController.setNavigationBarColor(MaterialTheme.colorScheme.background)

				val _monthlyPackage by this.monthlyPackage
				val _annualPackage by this.annualPackage
				val _lifetimePackage by this.lifetimePackage

				CompositionLocalProvider(
					onBackPressed provides this::finish,
					ProActivity.monthlyPackage provides _monthlyPackage,
					ProActivity.annualPackage provides _annualPackage,
					ProActivity.lifetimePackage provides _lifetimePackage,
					onClickPackage provides this::purchaseProduct,
					ProActivity.onRestore provides this::onRestore,
				) {
					SubscriptionScreen()
				}
			}
		}
	}

	private fun getProducts() {
		try {
			Purchases.sharedInstance.getOfferingsWith(
				onError = { error ->
					Toast.makeText(this, "Error retrieving data. Please try again later.", Toast.LENGTH_SHORT).show()
				}
			) { offerings ->
				monthlyPackage.value = offerings.current?.monthly
				annualPackage.value = offerings.current?.annual
				lifetimePackage.value = offerings.current?.lifetime
			}
		} catch (e : Exception) {
			Toast.makeText(this, "Error retrieving data. Please try again later.", Toast.LENGTH_SHORT).show()
		}
	}

	private fun purchaseProduct(_package : Package?) {
		val auth = Firebase.auth

		if (auth.currentUser?.uid == null) {
			Toast.makeText(this, "Please login to make purchase", Toast.LENGTH_SHORT).show()
			return
		}

		if (_package == null) {
			Toast.makeText(this, "Error retrieving data. Please try again later.", Toast.LENGTH_SHORT).show()
			return
		}

		try {
			Purchases
				.sharedInstance
				.apply {
					logIn(auth.currentUser !!.uid, null)
					setAttributes(mapOf("\$email" to auth.currentUser?.email))
					purchasePackage(
						activity = this@ProActivity,
						packageToPurchase = _package,
						listener = object : PurchaseCallback {
							override fun onCompleted(storeTransaction : StoreTransaction, customerInfo : CustomerInfo) {
								BaseApplication.isPro.tryEmit(customerInfo.entitlements["pro"]?.isActive == true)
								if (BaseApplication.isPro.value) {
									CoroutineScope(Dispatchers.Main).launch {
										Toast.makeText(this@ProActivity, "Purchase completed", Toast.LENGTH_SHORT).show()
										this@ProActivity.finish()
									}
								} else {
									CoroutineScope(Dispatchers.Main).launch {
										Toast.makeText(this@ProActivity, "Purchase failed", Toast.LENGTH_SHORT).show()
									}
								}
							}

							override fun onError(error : PurchasesError, userCancelled : Boolean) {
								Toast.makeText(this@ProActivity, "Error purchasing product. Please try again later.", Toast.LENGTH_SHORT).show()
							}
						}
					)
				}
		} catch (e : Exception) {
			Toast.makeText(this, "Error making purchase. Please try again later.", Toast.LENGTH_SHORT).show()
		}
	}

	private fun onRestore() {
		val auth = Firebase.auth
		if (auth.currentUser?.uid == null) {
			Toast.makeText(this, "Please login to restore purchase", Toast.LENGTH_SHORT).show()
		} else {
			superRestore(
				onSuccess = {},
				onFailure = {
					Purchases
						.sharedInstance
						.apply {
							setAttributes(mapOf("\$email" to auth.currentUser?.email))
							logIn(
								newAppUserID = auth.currentUser !!.uid,
								callback = object : LogInCallback {
									override fun onError(error : PurchasesError) {
									}

									override fun onReceived(customerInfo : CustomerInfo, created : Boolean) {
										BaseApplication.isPro.tryEmit(customerInfo.entitlements["pro"]?.isActive == true)
										if (BaseApplication.isPro.value) {
											CoroutineScope(Dispatchers.Main).launch {
												Toast.makeText(this@ProActivity, "Purchase restored", Toast.LENGTH_SHORT).show()
												this@ProActivity.finish()
											}
										} else {
											CoroutineScope(Dispatchers.Main).launch {
												Toast.makeText(this@ProActivity, "No purchase found", Toast.LENGTH_SHORT).show()
											}
										}
									}
								}
							)
						}
				}
			)
		}
	}

	private fun superRestore(onSuccess : () -> Unit, onFailure : () -> Unit) {
		val dataStoreInstance = DataStoreInstance(this@ProActivity)
		val auth = Firebase.auth
		val fireStore = Firebase.firestore

		try {
			fireStore
				.collection("user")
				.document(auth.currentUser !!.uid)
				.get()
				.addOnSuccessListener { documentSnapshot ->
					val expiryTimestamp = documentSnapshot.getTimestamp("override")?.seconds?.times(1000)
					val currentTimestamp = System.currentTimeMillis()
					if ((expiryTimestamp != null) && (expiryTimestamp > currentTimestamp)) {
						dataStoreInstance.putSuperExpiryTime(expiryTimestamp)
						CoroutineScope(Dispatchers.Main).launch {
							Toast.makeText(this@ProActivity, "Welcome to Graphite Pro", Toast.LENGTH_LONG).show()
						}
						onSuccess()
						finish()
					} else {
//					CoroutineScope(Dispatchers.Main).launch {
//						Toast.makeText(this@ProActivity, "No active subscription found", Toast.LENGTH_LONG).show()
//					}
						onFailure()
					}
				}
				.addOnFailureListener {
//				CoroutineScope(Dispatchers.Main).launch {
//					Toast.makeText(this@ProActivity, "Error getting data. Please try again later", Toast.LENGTH_SHORT).show()
//				}
					onFailure()
				}
		} catch (e : Exception) {
			onFailure()
		}
	}

	override fun onReceived(customerInfo : CustomerInfo) {

	}

	companion object {
		val onBackPressed = compositionLocalOf<() -> Unit> { {} }

		val monthlyPackage = compositionLocalOf<Package?> { null }
		val annualPackage = compositionLocalOf<Package?> { null }
		val lifetimePackage = compositionLocalOf<Package?> { null }

		val onClickPackage = compositionLocalOf<(Package?) -> Unit> { {} }

		val onRestore = compositionLocalOf<() -> Unit> { {} }
	}
}
