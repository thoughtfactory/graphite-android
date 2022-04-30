package com.syncodec.graphite.premiumComponent

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.syncodec.graphite.R
import com.syncodec.graphite.ui.theme.GraphiteTheme


class PremiumActivity : ComponentActivity() {

	@OptIn(
		ExperimentalPagerApi::class, ExperimentalMaterialApi::class,
		ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class
	)
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContent {
			GraphiteTheme {
				val systemUiController = rememberSystemUiController()
				systemUiController.setStatusBarColor(MaterialTheme.colorScheme.background)
				systemUiController.setNavigationBarColor(MaterialTheme.colorScheme.surface)

				Screen()
			}
		}
	}

	@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
	@Composable
	private fun Screen() {
		Scaffold(
			modifier = Modifier.fillMaxSize(),
			containerColor = MaterialTheme.colorScheme.background,
			topBar = { TopBar() }
		) {
			Column(
				horizontalAlignment = Alignment.CenterHorizontally,
				modifier = Modifier
					.fillMaxSize()
					.verticalScroll(rememberScrollState()),
			) {
				Spacer(modifier = Modifier.height(16.dp))
				Row(
					modifier = Modifier.fillMaxWidth(),
					horizontalArrangement = Arrangement.Center
				) {
					Spacer(modifier = Modifier.weight(1f))

					PriceCard(
						isMonthly = true,
						price = "$4.29",
						modifier = Modifier
							.weight(5f)
							.wrapContentHeight()
					)
					Spacer(modifier = Modifier.weight(1f))
					PriceCard(
						isMonthly = false,
						price = "$23.99",
						modifier = Modifier
							.weight(5f)
							.wrapContentHeight()
					)
					Spacer(modifier = Modifier.weight(1f))
				}

				Spacer(modifier = Modifier.height(24.dp))

				FeatureCard()

				Box(
					modifier = Modifier.weight(1f),
					contentAlignment = Alignment.Center
				) {
					Text(
						text = "Try 14 days for free",
						style = MaterialTheme.typography.titleSmall,
						color = MaterialTheme.colorScheme.onBackground,
					)
				}

				Button(
					onClick = { /*TODO*/ },
					colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
				) {
					Icon(
						painter = painterResource(id = R.drawable.ic_restore),
						contentDescription = "Restore purchase",
						tint = MaterialTheme.colorScheme.onPrimary,
						modifier = Modifier.requiredSize(24.dp)
					)
					Spacer(modifier = Modifier.width(8.dp))
					Text(
						text = "Restore Purchase",
						style = MaterialTheme.typography.bodyLarge,
						color = MaterialTheme.colorScheme.onPrimary,
					)
				}

				Spacer(modifier = Modifier.height(24.dp))
			}
		}
	}

	@Composable
	private fun TopBar() {
		SmallTopAppBar(
			navigationIcon = {
				IconButton(onClick = { finish() }) {
					Icon(
						painter = painterResource(id = R.drawable.ic_back),
						contentDescription = null,
						tint = MaterialTheme.colorScheme.onBackground,
						modifier = Modifier
							.requiredSize(32.dp)
							.padding(4.dp)
					)
				}
			},
			title = {
				Text(
					text = "Join Premium",
					modifier = Modifier,
					style = MaterialTheme.typography.titleMedium,
					color = MaterialTheme.colorScheme.onBackground
				)
			},
			colors = TopAppBarDefaults.smallTopAppBarColors(
				containerColor = MaterialTheme.colorScheme.background
			)
		)
	}

	@OptIn(ExperimentalMaterialApi::class)
	@Composable
	private fun PriceCard(
		modifier: Modifier,
		isMonthly: Boolean,
		price: String
	) {
		val containerColor = if (isMonthly) Color(0xFFCCD1E4) else Color(0xFFEEC373)
		val contentColor = if (isMonthly) Color(0xFF171717) else Color.Black

		androidx.compose.material.Card(
			shape = RoundedCornerShape(12.dp),
			backgroundColor = containerColor,
			elevation = 8.dp,
			modifier = modifier,
			onClick = {}
		) {
			Column(
				modifier = Modifier
					.fillMaxSize()
					.padding(12.dp)
			) {
				Spacer(modifier = Modifier.height(12.dp))
				Text(
					text = if (isMonthly) "Monthly" else "Annual",
					style = MaterialTheme.typography.titleSmall,
					color = contentColor
				)

				Spacer(modifier = Modifier.height(12.dp))

				Row(
					modifier = Modifier.fillMaxWidth(),
					verticalAlignment = Alignment.Bottom
				) {
					Text(
						text = price,
						style = MaterialTheme.typography.titleMedium,
						color = contentColor
					)
					Text(
						text = if (isMonthly) " / month" else " / year",
						style = MaterialTheme.typography.bodyMedium,
						color = contentColor
					)
				}

				Spacer(modifier = Modifier.height(32.dp))

				Text(
					text = if (isMonthly) "billed monthly" else "billed annually",
					style = MaterialTheme.typography.bodyMedium,
					color = contentColor
				)
				Text(
					text = "cancel anytime",
					style = MaterialTheme.typography.bodyMedium,
					color = contentColor
				)
				Spacer(modifier = Modifier.height(12.dp))
			}
		}
	}

	@OptIn(ExperimentalMaterial3Api::class)
	@Composable
	private fun FeatureCard() {
		Card(
			containerColor = MaterialTheme.colorScheme.surface,
			modifier = Modifier
				.fillMaxWidth()
				.padding(24.dp),
		) {
			Column(
				modifier = Modifier
					.fillMaxWidth()
					.padding(0.dp, 12.dp)
			) {
				Row(
					modifier = Modifier.fillMaxWidth(),
					verticalAlignment = Alignment.CenterVertically
				) {
					Spacer(modifier = Modifier.width(24.dp))
					Text(
						text = "Features",
						style = MaterialTheme.typography.bodyLarge,
						fontWeight = FontWeight.Bold,
						color = MaterialTheme.colorScheme.onSurface,
						modifier = Modifier.weight(2f)
					)

					Box(
						modifier = Modifier
							.width(2.dp)
							.height(32.dp)
							.clip(RoundedCornerShape(50))
							.background(MaterialTheme.colorScheme.onSurface)
					)

					Row(
						modifier = Modifier.weight(3f),
						verticalAlignment = Alignment.CenterVertically,
					) {
						Text(
							text = "Basic",
							style = MaterialTheme.typography.bodyLarge,
							fontWeight = FontWeight.Bold,
							textAlign = TextAlign.Center,
							color = MaterialTheme.colorScheme.onSurface,
							modifier = Modifier.weight(1f)
						)

						Box(
							modifier = Modifier
								.width(2.dp)
								.height(32.dp)
								.clip(RoundedCornerShape(50))
								.background(MaterialTheme.colorScheme.onSurface)
						)

						Text(
							text = "Premium",
							style = MaterialTheme.typography.bodyLarge,
							fontWeight = FontWeight.Bold,
							textAlign = TextAlign.Center,
							color = MaterialTheme.colorScheme.onSurface,
							modifier = Modifier.weight(1f)
						)
					}

					Spacer(modifier = Modifier.width(8.dp))
				}

				FeatureCardComponent(
					title = "Atlas",
					basicText = null,
					basicIcon = R.drawable.ic_check_colored,
					premiumText = null,
					premiumIcon = R.drawable.ic_check_colored
				)

				FeatureCardComponent(
					title = "Export Data",
					basicText = null,
					basicIcon = R.drawable.ic_check_colored,
					premiumText = null,
					premiumIcon = R.drawable.ic_check_colored
				)

				FeatureCardComponent(
					title = "Notebook",
					basicText = "4",
					basicIcon = null,
					premiumText = "Unlimited",
					premiumIcon = null
				)

				FeatureCardComponent(
					title = "Bucket",
					basicText = "4",
					basicIcon = null,
					premiumText = "Unlimited",
					premiumIcon = null
				)

				FeatureCardComponent(
					title = "Attachment",
					basicText = "4",
					basicIcon = null,
					premiumText = "Unlimited",
					premiumIcon = null
				)
			}
		}
	}

	@Composable
	private fun FeatureCardComponent(
		title: String,
		basicText: String?,
		basicIcon: Int?,
		premiumText: String?,
		premiumIcon: Int?
	) {
		Row(
			modifier = Modifier.fillMaxWidth(),
			verticalAlignment = Alignment.CenterVertically
		) {
			Spacer(modifier = Modifier.width(24.dp))
			Text(
				text = title,
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.71f),
				modifier = Modifier.weight(2f)
			)

			Box(
				modifier = Modifier
					.width(2.dp)
					.height(32.dp)
					.clip(RoundedCornerShape(50))
					.background(MaterialTheme.colorScheme.onSurface)
			)

			Row(
				modifier = Modifier.weight(3f),
				verticalAlignment = Alignment.CenterVertically,
				horizontalArrangement = Arrangement.SpaceEvenly
			) {
				if (basicText != null) {
					Text(
						text = basicText,
						style = MaterialTheme.typography.bodyMedium,
						color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.71f),
						textAlign = TextAlign.Center,
						modifier = Modifier.weight(2f)
					)
				}
				if (basicIcon != null) {
					Icon(
						painter = painterResource(id = basicIcon),
						contentDescription = null,
						tint = Color.Unspecified,
						modifier = Modifier
							.requiredSize(32.dp)
							.padding(4.dp)
					)
				}

				Box(
					modifier = Modifier
						.width(2.dp)
						.height(32.dp)
						.clip(RoundedCornerShape(50))
						.background(MaterialTheme.colorScheme.onSurface)
				)

				if (premiumText != null) {
					Text(
						text = premiumText,
						style = MaterialTheme.typography.bodyMedium,
						color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.71f),
						textAlign = TextAlign.Center,
						modifier = Modifier.weight(2f)
					)
				}
				if (premiumIcon != null) {
					Icon(
						painter = painterResource(id = premiumIcon),
						contentDescription = null,
						tint = Color.Unspecified,
						modifier = Modifier
							.requiredSize(32.dp)
							.padding(4.dp)
					)
				}

			}
			Spacer(modifier = Modifier.width(8.dp))
		}
	}
}
