package com.syncodec.graphite.presentation.pro

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
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
import com.syncodec.graphite.utils.ContentStatus
import com.syncodec.graphite.utils.DataStoreInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch


class ProActivity : ComponentActivity(), UpdatedCustomerInfoListener {

	private val auth = Firebase.auth
	private val productPackage = MutableStateFlow<ContentStatus<ProductPackage>>(ContentStatus.Init)

	override fun onCreate(savedInstanceState : Bundle?) {
		super.onCreate(savedInstanceState)

		getProducts()

		setContent {
			BaseContent {
				val productPackage1 by productPackage.collectAsState()

				SubscriptionScreen(
					contentStatus = productPackage1,
					userEmail = auth.currentUser?.email,
					onClickPackage = this::purchaseProduct,
					onRestore = this::onRestore,
				)
			}
		}
	}

	private fun getProducts() {
		productPackage.tryEmit(ContentStatus.Loading)
		try {
			Purchases.sharedInstance.getOfferingsWith(
				onError = { error ->
					productPackage.tryEmit(ContentStatus.Error("Error retrieving data. Please try again later."))
					Toast.makeText(this, "Error retrieving data. Please try again later.", Toast.LENGTH_SHORT).show()
				}
			) { offerings ->
				val monthlyPackage = offerings.current?.monthly
				val annualPackage = offerings.current?.annual
				val lifetimePackage = offerings.current?.lifetime
				if (monthlyPackage == null || annualPackage == null || lifetimePackage == null) {
					productPackage.tryEmit(ContentStatus.Error("Error retrieving data. Please try again later."))
					Toast.makeText(this, "Error retrieving data. Please try again later.", Toast.LENGTH_SHORT).show()
					return@getOfferingsWith
				} else {
					productPackage.tryEmit(ContentStatus.Loaded(ProductPackage(monthlyPackage, annualPackage, lifetimePackage)))
				}
			}
		} catch (e : Exception) {
			Toast.makeText(this, "Error retrieving data. Please try again later.", Toast.LENGTH_SHORT).show()
		}
	}

	private fun purchaseProduct(toPurchasePackage : Package?) {
		if (auth.currentUser?.uid == null) {
			Toast.makeText(this, "Please login to make purchase", Toast.LENGTH_SHORT).show()
			return
		}

		if (toPurchasePackage == null) {
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
						packageToPurchase = toPurchasePackage,
						listener = object : PurchaseCallback {
							override fun onCompleted(storeTransaction : StoreTransaction, customerInfo : CustomerInfo) {
								BaseApplication.isPro.tryEmit(customerInfo.entitlements["pro"]?.isActive == true)
								if (BaseApplication.isPro.value) {
									lifecycleScope.launch(Dispatchers.Main) {
										Toast.makeText(this@ProActivity, "Purchase completed", Toast.LENGTH_SHORT).show()
										this@ProActivity.finish()
									}
								} else {
									lifecycleScope.launch(Dispatchers.Main) {
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
				onFailure = { revenueCatRestore() }
			)
		}
	}

	private fun revenueCatRestore() {
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
								lifecycleScope.launch(Dispatchers.Main) {
									Toast.makeText(this@ProActivity.applicationContext, "Welcome to Graphite Pro", Toast.LENGTH_SHORT).show()
									this@ProActivity.finish()
								}
							} else {
								lifecycleScope.launch(Dispatchers.Main) {
									Toast.makeText(this@ProActivity.applicationContext, "No purchase found", Toast.LENGTH_SHORT).show()
								}
							}
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

					expiryTimestamp?.minus(currentTimestamp)?.let {
						if (it > 0) {
							dataStoreInstance.putSuperExpiryTime(expiryTimestamp)
							BaseApplication.isPro.tryEmit(true)
							lifecycleScope.launch(Dispatchers.Main) {
								Toast.makeText(this@ProActivity, "Welcome to Graphite Pro", Toast.LENGTH_LONG).show()
							}
							onSuccess()
							finish()
						} else onFailure()
					} ?: onFailure()
				}
				.addOnFailureListener {
					onFailure()
				}
		} catch (e : Exception) {
			onFailure()
		}
	}

	override fun onReceived(customerInfo : CustomerInfo) {

	}

	companion object {
		data class ProductPackage(
			val monthlyPackage : Package,
			val annualPackage : Package,
			val lifetimePackage : Package,
		)
	}
}
