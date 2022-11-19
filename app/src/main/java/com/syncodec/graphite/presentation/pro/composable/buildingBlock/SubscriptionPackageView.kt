package com.syncodec.graphite.presentation.pro.composable.buildingBlock

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.revenuecat.purchases.Package
import com.syncodec.graphite.presentation.pro.ProActivity


@Composable
fun SubscriptionPackageView() {

	val monthlyPackage = ProActivity.monthlyPackage.current
	val annualPackage = ProActivity.annualPackage.current

	val savePercent = monthlyPackage?.product?.originalPriceAmountMicros?.let { monthlyPrice ->
		annualPackage?.product?.originalPriceAmountMicros?.let { annualPrice ->
			(monthlyPrice * 12) - annualPrice
		}
	}?.let { saveAmount ->
		(saveAmount / (monthlyPackage.product.originalPriceAmountMicros.toFloat() * 12)) * 100
	}?.toInt()

	var monthlySize by remember { mutableStateOf<IntSize?>(null) }
	var annualSize by remember { mutableStateOf<IntSize?>(null) }

	val onClickPackage = ProActivity.onClickPackage.current

	Row(
		horizontalArrangement = Arrangement.SpaceAround,
		modifier = Modifier
			.fillMaxWidth()
			.padding(24.dp, 0.dp)
	) {
		MonthlySubscriptionView(
			monthlyPackage = monthlyPackage,
			modifier = Modifier
				.height(with(LocalDensity.current) { annualSize?.height?.toDp() ?: 0.dp })
				.onGloballyPositioned { monthlySize = it.size }
				.clip(RoundedCornerShape(24.dp))
				.clickable { onClickPackage(monthlyPackage) }
		)
		AnnualSubscriptionView(
			annualPackage = annualPackage,
			savePercent = savePercent,
			modifier = Modifier
				.onGloballyPositioned { annualSize = it.size }
				.clip(RoundedCornerShape(24.dp))
				.clickable { onClickPackage(annualPackage) }
		)
	}
}

@Composable
private fun MonthlySubscriptionView(
	modifier : Modifier = Modifier,
	monthlyPackage : Package?
) {
	val configuration = LocalConfiguration.current
	val screenHeight = configuration.screenHeightDp.dp
	val screenWidth = configuration.screenWidthDp.dp

	val containerColor = MaterialTheme.colorScheme.surface
	val contentColor = MaterialTheme.colorScheme.onSurface

	Box(
		modifier = modifier
			.width(screenWidth * 2 / 5)
			.background(containerColor, RoundedCornerShape(24.dp))
			.clip(RoundedCornerShape(24.dp))
	) {
		Crossfade(targetState = monthlyPackage) { _package ->
			if (_package == null) {

			} else {
				Column(
					horizontalAlignment = Alignment.CenterHorizontally,
					modifier = Modifier.padding(8.dp, 16.dp),
				) {
					Text(
						text = "Monthly Plan",
						style = MaterialTheme.typography.titleMedium,
						color = contentColor,
						fontWeight = FontWeight.Bold
					)

					Spacer(modifier = Modifier.height(32.dp))

					Text(
						text = _package.product.price,
						style = MaterialTheme.typography.titleLarge,
						color = contentColor,
						fontWeight = FontWeight.Bold
					)

					Text(
						text = "per month",
						style = MaterialTheme.typography.bodySmall,
						color = contentColor,
						fontWeight = FontWeight.Bold,
						textAlign = TextAlign.Center,
						modifier = Modifier.fillMaxWidth()
					)


					Spacer(modifier = Modifier.weight(1f))

					Text(
						text = "Pay monthly\ncancel anytime",
						style = MaterialTheme.typography.bodySmall,
						color = contentColor,
						textAlign = TextAlign.Center,
					)
				}
			}
		}
	}
}

@Composable
private fun AnnualSubscriptionView(
	modifier : Modifier = Modifier,
	annualPackage : Package?,
	savePercent : Int? = null
) {
	val configuration = LocalConfiguration.current
	val screenHeight = configuration.screenHeightDp.dp
	val screenWidth = configuration.screenWidthDp.dp

	val containerColor = MaterialTheme.colorScheme.surface
	val contentColor = MaterialTheme.colorScheme.onSurface

	Box(
		modifier = modifier
			.width(screenWidth * 2 / 5)
			.background(containerColor, RoundedCornerShape(24.dp))
			.clip(RoundedCornerShape(24.dp))
	) {
		Crossfade(targetState = annualPackage) { _package ->
			if (_package == null) {

			} else {
				Column(
					horizontalAlignment = Alignment.CenterHorizontally,
					modifier = Modifier.padding(8.dp, 16.dp)
				) {
					Text(
						text = "Annual Plan",
						style = MaterialTheme.typography.titleMedium,
						color = contentColor,
						fontWeight = FontWeight.Bold
					)

					Spacer(modifier = Modifier.height(32.dp))

					Text(
						text = _package.product.price,
						style = MaterialTheme.typography.titleLarge,
						color = contentColor,
						fontWeight = FontWeight.Bold
					)

					Text(
						text = "per year",
						style = MaterialTheme.typography.bodySmall,
						color = contentColor,
						fontWeight = FontWeight.Bold,
						textAlign = TextAlign.Center,
						modifier = Modifier.fillMaxWidth()
					)

					Spacer(modifier = Modifier.height(12.dp))

					Box(
						modifier = Modifier.background(MaterialTheme.colorScheme.primary, RoundedCornerShape(50))
					) {
						Text(
							text = "Save $savePercent%",
							style = MaterialTheme.typography.bodySmall,
							color = MaterialTheme.colorScheme.onPrimary,
							fontWeight = FontWeight.Bold,
							textAlign = TextAlign.Center,
							modifier = Modifier.padding(8.dp, 4.dp)
						)
					}

					Spacer(modifier = Modifier.height(12.dp))

					Text(
						text = "Pay annually\ncancel anytime",
						style = MaterialTheme.typography.bodySmall,
						color = contentColor,
						textAlign = TextAlign.Center,
					)
				}
			}
		}
	}
}
