package com.syncodec.graphite.premiumComponent

import android.icu.util.Calendar
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.qonversion.android.sdk.*
import com.qonversion.android.sdk.dto.QPermission
import com.qonversion.android.sdk.dto.products.QProduct
import com.qonversion.android.sdk.dto.products.QProductDuration
import com.syncodec.graphite.R
import com.syncodec.graphite.miscellaneous.DataStoreInstance
import com.syncodec.graphite.ui.theme.GraphiteBase


class PremiumActivity : ComponentActivity() {

	private val firebaseAuth = FirebaseAuth.getInstance()
	private val fireStore = Firebase.firestore

	private var qProductMonthly: MutableState<QProduct?> = mutableStateOf(null)
	private var qProductAnnually: MutableState<QProduct?> = mutableStateOf(null)

	private var selectedProduct: MutableState<QProduct?> = mutableStateOf(null)

	private val showLoadingView: MutableState<Boolean> = mutableStateOf(false)

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		Qonversion.apply {
			firebaseAuth.currentUser?.apply {
				setProperty(QUserProperties.CustomUserId, uid)
				email?.let { setProperty(QUserProperties.Email, it) }
			}
		}

		loadProduct()

		setContent {
			GraphiteBase {
				val systemUiController = rememberSystemUiController()
				systemUiController.setNavigationBarColor(MaterialTheme.colorScheme.background)
				systemUiController.setStatusBarColor(MaterialTheme.colorScheme.background)

				Screen()
			}
		}
	}

	private fun loadProduct() {
		Qonversion.products(
			object : QonversionProductsCallback {
				override fun onError(error: QonversionError) {
					Toast.makeText(
						this@PremiumActivity,
						"Error loading subscriptions. Please try again later",
						Toast.LENGTH_LONG
					).show()
				}

				override fun onSuccess(products: Map<String, QProduct>) {
					products.forEach {
						when (it.value.duration) {
							QProductDuration.Monthly -> qProductMonthly.value = it.value
							QProductDuration.Annual -> qProductAnnually.value = it.value
						}
					}
				}
			}
		)
	}

	private fun onAction(action: ACTION, data: Any? = null) {
		val dataStoreInstance = DataStoreInstance(this@PremiumActivity)

		when (action) {
			ACTION.BACK -> finish()
			ACTION.SELECT_SUBSCRIPTION -> selectedProduct.value = data as QProduct
			ACTION.SUBSCRIBE -> {
				showLoadingView.value = true

				if (firebaseAuth.currentUser == null) {
					Toast.makeText(
						this,
						"Please login before you subscribe",
						Toast.LENGTH_LONG
					).show()

					showLoadingView.value = false
				} else {
					selectedProduct.value?.let {
						Qonversion.purchase(
							this, it, object : QonversionPermissionsCallback {
								override fun onError(error: QonversionError) {
									Toast.makeText(
										this@PremiumActivity,
										"Error making purchase. Please try again later",
										Toast.LENGTH_LONG
									).show()

									showLoadingView.value = false
								}

								override fun onSuccess(permissions: Map<String, QPermission>) {
									permissions.forEach { (key, qPermission) ->
										qPermission.expirationDate?.time.also {
											if (it == null) {
												val calendar = Calendar.getInstance()
												calendar.add(Calendar.MONTH, 1)

												dataStoreInstance.putExpiryTime(calendar.timeInMillis)
											} else {
												dataStoreInstance.putExpiryTime(it)
											}

											Toast.makeText(
												this@PremiumActivity,
												"Welcome to Graphite Premium",
												Toast.LENGTH_LONG
											).show()
											finish()
										}
									}

									showLoadingView.value = false
								}
							}
						)
					}
				}
			}
			ACTION.RESTORE -> {
				showLoadingView.value = true

				if (firebaseAuth.currentUser == null) {
					Toast.makeText(
						this,
						"Please login before you subscribe",
						Toast.LENGTH_LONG
					).show()

					showLoadingView.value = false
				} else {
					Qonversion.restore(
						object : QonversionPermissionsCallback {
							override fun onError(error: QonversionError) {
								Toast.makeText(
									this@PremiumActivity,
									"Error restoring purchase. Please try again later",
									Toast.LENGTH_LONG
								).show()

								showLoadingView.value = false
							}

							override fun onSuccess(permissions: Map<String, QPermission>) {
								if (permissions.isEmpty()) {
									Toast.makeText(
										this@PremiumActivity,
										"No active subscriptions found",
										Toast.LENGTH_LONG
									).show()
								} else {
									var isSubscriptionActive: Boolean = false

									permissions.forEach { (key, qPermission) ->
										qPermission.expirationDate?.time.also {
											val calendar = Calendar.getInstance()
											calendar.add(Calendar.MONTH, 1)

											if (it == null) {
												dataStoreInstance.putExpiryTime(calendar.timeInMillis)
											} else {
												dataStoreInstance.putExpiryTime(it)
											}

											isSubscriptionActive =
												isSubscriptionActive or qPermission.isActive()
										}

										if (isSubscriptionActive) {
											Toast.makeText(
												this@PremiumActivity,
												"Purchase restored. Welcome to Graphite Premium",
												Toast.LENGTH_LONG
											).show()
											finish()
										} else {
											Toast.makeText(
												this@PremiumActivity,
												"No active subscription found",
												Toast.LENGTH_LONG
											).show()
										}
									}
								}

								showLoadingView.value = false
							}
						}
					)
				}
			}
			ACTION.RESTORE_SUPER -> {
				showLoadingView.value = true

				if (firebaseAuth.currentUser == null) {
					Toast.makeText(
						this,
						"Please login before you subscribe",
						Toast.LENGTH_LONG
					).show()

					showLoadingView.value = false
				} else {
					fireStore
						.collection("user")
						.document(firebaseAuth.currentUser!!.uid)
						.get()
						.addOnSuccessListener { documentSnapshot ->
							val expiryTimestamp = documentSnapshot.getTimestamp("override")?.seconds?.times(1000)
							val currentTimestamp = System.currentTimeMillis()
							if (expiryTimestamp != null && expiryTimestamp >  currentTimestamp) {
								dataStoreInstance.putSuperExpiryTime(expiryTimestamp)
								Toast.makeText(
									this,
									"Welcome to Graphite",
									Toast.LENGTH_LONG
								).show()
								finish()
							} else {
								Toast.makeText(
									this@PremiumActivity,
									"No active subscription found",
									Toast.LENGTH_LONG
								).show()
							}

							showLoadingView.value = false
						}
						.addOnFailureListener {
							Toast.makeText(
								this@PremiumActivity,
								"Error getting data. Please try again later",
								Toast.LENGTH_LONG
							).show()

							showLoadingView.value = false
						}
				}
			}
		}
	}

	@Composable
	private fun Screen() {
		Box(
			modifier = Modifier.fillMaxSize()
		) {
			LazyColumn(
				horizontalAlignment = Alignment.CenterHorizontally,
				modifier = Modifier
					.fillMaxSize()
					.background(MaterialTheme.colorScheme.background),
			) {
				item {
					Row(
						modifier = Modifier
							.fillMaxWidth()
							.height(64.dp)
					) {
						Spacer(modifier = Modifier.weight(1f))
						IconButton(onClick = { finish() }) {
							Icon(
								painter = painterResource(id = R.drawable.ic_close),
								contentDescription = "Back",
								tint = MaterialTheme.colorScheme.onBackground,
								modifier = Modifier
									.requiredSize(32.dp)
									.padding(4.dp)
							)
						}
						Spacer(modifier = Modifier.width(8.dp))
					}
				}

				item {
					Text(
						text = "GET ACCESS TO ALL PREMIUM CONTENT",
						fontFamily = FontFamily(Font(R.font.ubuntu_bold, FontWeight.Bold)),
						fontWeight = FontWeight.Normal,
						fontSize = 24.sp,
						lineHeight = 28.sp,
						letterSpacing = 2.sp,
						color = MaterialTheme.colorScheme.onBackground,
						modifier = Modifier.padding(24.dp, 0.dp),
					)
				}

				item { Spacer(modifier = Modifier.height(24.dp)) }

				item { PremiumItemText(text = "Richtext editor") }
				item { Spacer(modifier = Modifier.height(8.dp)) }
				item { PremiumItemText(text = "More attachments") }
				item { Spacer(modifier = Modifier.height(8.dp)) }
				item { PremiumItemText(text = "More notebooks") }
				item { Spacer(modifier = Modifier.height(8.dp)) }
				item { PremiumItemText(text = "More buckets") }

				item { Spacer(modifier = Modifier.height(32.dp)) }

				item {
					Box(
						modifier = Modifier.fillMaxWidth()
					) {
						Text(
							text = "SELECT YOUR SUBSCRIPTION",
							fontFamily = FontFamily(Font(R.font.ubuntu_bold, FontWeight.Bold)),
							fontWeight = FontWeight.Normal,
							fontSize = 16.sp,
							lineHeight = 18.sp,
							letterSpacing = 2.sp,
							color = MaterialTheme.colorScheme.onBackground,
							modifier = Modifier.padding(24.dp, 0.dp),
						)
					}
				}

				item { Spacer(modifier = Modifier.height(20.dp)) }

				item { qProductMonthly.value?.let { PremiumButton(qProduct = it) } }
				item { Spacer(modifier = Modifier.height(16.dp)) }
				item { qProductAnnually.value?.let { PremiumButton(qProduct = it) } }

				item { Spacer(modifier = Modifier.height(48.dp)) }
				item { SubscribeButton() }

				item { Spacer(modifier = Modifier.height(16.dp)) }
				item { RestoreButton() }
			}

			AnimatedVisibility(
				visible = showLoadingView.value,
				modifier = Modifier
					.fillMaxSize()
					.clickable { },
				enter = fadeIn(tween(300)),
				exit = fadeOut(tween(300))
			) {
				Box(
					modifier = Modifier
						.fillMaxSize()
						.background(MaterialTheme.colorScheme.background.copy(alpha = 0.13f)),
					contentAlignment = Alignment.Center
				) {
					CircularProgressIndicator(
						color = MaterialTheme.colorScheme.primary,
						strokeWidth = 4.dp
					)
				}
			}
		}
	}

	@Composable
	private fun PremiumItemText(
		text: String,
	) {
		Row(
			modifier = Modifier.fillMaxWidth(),
			verticalAlignment = Alignment.CenterVertically
		) {
			Spacer(modifier = Modifier.width(32.dp))

			Icon(
				painter = painterResource(id = R.drawable.ic_check_colored),
				contentDescription = null,
				tint = Color.Unspecified,
				modifier = Modifier.requiredSize(24.dp)
			)

			Spacer(modifier = Modifier.width(12.dp))

			Text(
				text = text,
				style = MaterialTheme.typography.bodyMedium,
				fontWeight = FontWeight.Bold,
				color = MaterialTheme.colorScheme.onBackground,
			)
		}
	}

	@OptIn(ExperimentalMaterial3Api::class)
	@Composable
	private fun PremiumButton(qProduct: QProduct) {
		val borderColor by animateColorAsState(
			targetValue =
			if (qProduct == selectedProduct.value) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.71f)
			else Color.Transparent
		)

		Card(
			colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface.copy(alpha = 0.47f)),
			shape = RoundedCornerShape(24.dp),
			border = BorderStroke(3.dp, borderColor),
			modifier = Modifier
				.fillMaxWidth()
				.padding(24.dp, 0.dp),
			onClick = { onAction(ACTION.SELECT_SUBSCRIPTION, qProduct) }
		) {
			Row(
				verticalAlignment = Alignment.CenterVertically,
				modifier = Modifier
					.fillMaxWidth()
					.padding(16.dp),
			) {
				Icon(
					painter = painterResource(
						id = when (qProduct.duration) {
							QProductDuration.Monthly -> R.drawable.ic_diamond_yellow
							QProductDuration.Annual -> R.drawable.ic_diamond_blue
							else -> R.drawable.ic_diamond_blue
						}
					),
					contentDescription = null,
					tint = Color.Unspecified,
					modifier = Modifier.requiredSize(48.dp)
				)

				Spacer(modifier = Modifier.width(16.dp))

				Column(
					modifier = Modifier
				) {
					qProduct.duration?.let {
						Text(
							text = it.name,
							style = MaterialTheme.typography.titleSmall,
							color = MaterialTheme.colorScheme.onSurface
						)
					}

					Spacer(modifier = Modifier.height(4.dp))

					qProduct.prettyPrice?.let {
						Row(
							modifier = Modifier
						) {
							Text(
								text = it,
								style = MaterialTheme.typography.bodyMedium,
								color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.71f)
							)

							Text(
								text = " / ",
								style = MaterialTheme.typography.bodyMedium,
								color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.71f)
							)

							Text(
								text = when (qProduct.duration) {
									QProductDuration.Monthly -> "month"
									QProductDuration.Annual -> "year"
									else -> ""
								},
								style = MaterialTheme.typography.bodyMedium,
								color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.71f)
							)
						}
					}

					if (qProduct.duration == QProductDuration.Annual) {
						val annualPrice = qProductAnnually.value?.skuDetail?.priceAmountMicros
						val monthlyPrice = qProductMonthly.value?.skuDetail?.priceAmountMicros

						Spacer(modifier = Modifier.height(8.dp))

						if (annualPrice != null && monthlyPrice != null) {
							Text(
								text = "Save ${(((monthlyPrice * 12) - annualPrice) * 100) / (monthlyPrice * 12)} %",
								style = MaterialTheme.typography.titleSmall,
								fontWeight = FontWeight.Bold,
								color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.71f)
							)

						}
					}
				}

				Spacer(modifier = Modifier.weight(1f))

				qProduct.prettyPrice?.let {
					Text(
						text = it,
						style = MaterialTheme.typography.titleSmall,
						fontWeight = FontWeight.Bold,
						color = Color(0xFFD19A66)
					)
				}
			}
		}
	}

	@OptIn(ExperimentalMaterialApi::class)
	@Composable
	private fun SubscribeButton() {
		val backgroundColor by animateColorAsState(
			targetValue = if (selectedProduct.value == null) Color(0x47FEAA00)
			else Color(0xFFFEAA00)
		)

		val iconBackgroundColor by animateColorAsState(
			targetValue = if (selectedProduct.value == null) Color(0x47F1A100)
			else Color(0xFFF1A100)
		)

		val elevation by animateDpAsState(
			targetValue = if (selectedProduct.value == null) 0.dp
			else 12.dp
		)

		androidx.compose.material.Card(
			shape = RoundedCornerShape(50),
			backgroundColor = backgroundColor,
			elevation = elevation,
			enabled = selectedProduct.value != null,
			modifier = Modifier
				.fillMaxWidth()
				.padding(24.dp, 0.dp),
			onClick = { onAction(ACTION.SUBSCRIBE) }
		) {
			Row(
				modifier = Modifier.padding(6.dp),
				verticalAlignment = Alignment.CenterVertically
			) {
				Spacer(modifier = Modifier.requiredSize(40.dp))
				Spacer(modifier = Modifier.weight(1f))
				Text(
					text = "Subscribe Now",
					style = MaterialTheme.typography.bodyLarge,
					fontWeight = FontWeight.Bold,
					color = Color.White,
				)
				Spacer(modifier = Modifier.weight(1f))
				Icon(
					painter = painterResource(id = R.drawable.ic_done),
					contentDescription = null,
					tint = Color.White,
					modifier = Modifier
						.requiredSize(40.dp)
						.clip(CircleShape)
						.background(iconBackgroundColor)
				)
			}
		}
	}

	@OptIn(ExperimentalFoundationApi::class)
	@Composable
	private fun RestoreButton() {
		androidx.compose.material.Card(
			shape = RoundedCornerShape(50),
			backgroundColor = MaterialTheme.colorScheme.primary,
			modifier = Modifier
				.fillMaxWidth()
				.padding(24.dp, 0.dp)
				.clip(RoundedCornerShape(50))
				.combinedClickable(
					onClick = { onAction(ACTION.RESTORE) },
					onLongClick = { onAction(ACTION.RESTORE_SUPER) }
				),
		) {
			Row(
				modifier = Modifier.padding(6.dp),
				verticalAlignment = Alignment.CenterVertically
			) {
				Spacer(modifier = Modifier.requiredSize(40.dp))
				Spacer(modifier = Modifier.weight(1f))
				Text(
					text = "Restore Purchase",
					style = MaterialTheme.typography.bodyLarge,
					fontWeight = FontWeight.Bold,
					color = MaterialTheme.colorScheme.onPrimary,
				)
				Spacer(modifier = Modifier.weight(1f))
				Icon(
					painter = painterResource(id = R.drawable.ic_history),
					contentDescription = null,
					tint = Color.White,
					modifier = Modifier
						.requiredSize(40.dp)
						.padding(8.dp)
						.clip(CircleShape)
				)
			}
		}
	}

	enum class ACTION {
		BACK,
		SELECT_SUBSCRIPTION,
		SUBSCRIBE,
		RESTORE,
		RESTORE_SUPER
	}
}
