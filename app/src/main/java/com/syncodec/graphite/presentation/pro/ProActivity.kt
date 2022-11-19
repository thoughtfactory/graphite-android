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
import androidx.compose.runtime.rememberCoroutineScope
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.Package
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesError
import com.revenuecat.purchases.getOfferingsWith
import com.revenuecat.purchases.interfaces.PurchaseCallback
import com.revenuecat.purchases.interfaces.UpdatedCustomerInfoListener
import com.revenuecat.purchases.models.StoreTransaction
import com.syncodec.graphite.presentation.pro.composable.screen.SubscriptionScreen
import com.syncodec.graphite.presentation.ui.BaseContent
import dagger.hilt.android.AndroidEntryPoint


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
				val scope = rememberCoroutineScope()
				val systemUiController = rememberSystemUiController()
				systemUiController.setStatusBarColor(if (isSystemInDarkTheme()) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onBackground)
				systemUiController.setNavigationBarColor(if (isSystemInDarkTheme()) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onBackground)

				val _monthlyPackage by this.monthlyPackage
				val _annualPackage by this.annualPackage
				val _lifetimePackage by this.lifetimePackage

				CompositionLocalProvider(
					onBackPressed provides this::finish,
					ProActivity.monthlyPackage provides _monthlyPackage,
					ProActivity.annualPackage provides _annualPackage,
					ProActivity.lifetimePackage provides _lifetimePackage,
					onClickPackage provides this::purchaseProduct
				) {
					SubscriptionScreen()
				}
			}
		}
	}

	private fun getProducts() {
		try {
			Purchases.sharedInstance.getOfferingsWith(
				{ error ->
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
			Purchases.sharedInstance.logIn(auth.currentUser !!.uid, null)

			Purchases.sharedInstance.purchasePackage(
				activity = this,
				packageToPurchase = _package,
				listener = object : PurchaseCallback {
					override fun onCompleted(storeTransaction : StoreTransaction, customerInfo : CustomerInfo) {
						Toast.makeText(this@ProActivity, "Purchase completed", Toast.LENGTH_SHORT).show()
					}

					override fun onError(error : PurchasesError, userCancelled : Boolean) {
						Toast.makeText(this@ProActivity, "Error purchasing product. Please try again later.", Toast.LENGTH_SHORT).show()
					}
				}
			)
		} catch (e : Exception) {
			Toast.makeText(this, "Error making purchase. Please try again later.", Toast.LENGTH_SHORT).show()
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
	}
}
