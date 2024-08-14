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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
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
import com.syncodec.graphite.presentation.ui.montserratFontFamily


@Composable
fun SubscriptionPackageView(
	monthlyPackage : Package? = null,
	annualPackage : Package? = null,
	onClickPackage : (Package?) -> Unit = { _ -> },
) {
//	val savePercent = monthlyPackage?.product?.originalPriceAmountMicros?.let { monthlyPrice ->
//		annualPackage?.product?.originalPriceAmountMicros?.let { annualPrice ->
//			(monthlyPrice * 12) - annualPrice
//		}
//	}?.let { saveAmount ->
//		(saveAmount / (monthlyPackage.product.originalPriceAmountMicros.toFloat() * 12)) * 100
//	}?.toInt()
//
//	var monthlySize by remember { mutableStateOf<IntSize?>(null) }
//	var annualSize by remember { mutableStateOf<IntSize?>(null) }
//
//	Row(
//		horizontalArrangement = Arrangement.SpaceAround,
//		modifier = Modifier
//			.fillMaxWidth()
//			.padding(24.dp, 0.dp)
//	) {
//		MonthlySubscriptionView(
//			monthlyPackage = monthlyPackage,
//			modifier = Modifier
//				.height(with(LocalDensity.current) { annualSize?.height?.toDp() ?: 0.dp })
//				.onGloballyPositioned { monthlySize = it.size }
//				.clip(MaterialTheme.shapes.large)
//				.clickable { onClickPackage(monthlyPackage) }
//		)
//		AnnualSubscriptionView(
//			annualPackage = annualPackage,
//			savePercent = savePercent,
//			modifier = Modifier
//				.onGloballyPositioned { annualSize = it.size }
//				.clip(MaterialTheme.shapes.large)
//				.clickable { onClickPackage(annualPackage) }
//		)
//	}
}

@Composable
private fun MonthlySubscriptionView(
	modifier : Modifier = Modifier,
	monthlyPackage : Package?
) {
	val configuration = LocalConfiguration.current
	val screenWidth = configuration.screenWidthDp.dp

	val containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp).copy(alpha = 0.31f)
	val contentColor = MaterialTheme.colorScheme.onSurface

	Box(
		modifier = modifier
			.width(screenWidth * 2 / 5)
			.background(containerColor, MaterialTheme.shapes.extraLarge)
			.clip(MaterialTheme.shapes.extraLarge)
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
						style = MaterialTheme.typography.titleMedium.copy(fontFamily = montserratFontFamily),
						color = contentColor,
						fontWeight = FontWeight.Bold
					)

					Spacer(modifier = Modifier.height(32.dp))

//					Text(
//						text = _package.product.price,
//						style = MaterialTheme.typography.titleLarge.copy(fontFamily = montserratFontFamily),
//						color = contentColor,
//						fontWeight = FontWeight.Bold
//					)

					Text(
						text = "per month",
						style = MaterialTheme.typography.bodySmall.copy(fontFamily = montserratFontFamily),
						color = contentColor,
						fontWeight = FontWeight.Bold,
						textAlign = TextAlign.Center,
						modifier = Modifier.fillMaxWidth()
					)


					Spacer(modifier = Modifier.weight(1f))

					Text(
						text = "Pay monthly\ncancel anytime",
						style = MaterialTheme.typography.bodySmall.copy(fontFamily = montserratFontFamily),
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
	val screenWidth = configuration.screenWidthDp.dp

	val containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp).copy(alpha = 0.31f)
	val contentColor = MaterialTheme.colorScheme.onSurface

	Box(
		modifier = modifier
			.width(screenWidth * 2 / 5)
			.background(containerColor, MaterialTheme.shapes.extraLarge)
			.clip(MaterialTheme.shapes.extraLarge)
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
						style = MaterialTheme.typography.titleMedium.copy(fontFamily = montserratFontFamily),
						color = contentColor,
						fontWeight = FontWeight.Bold
					)

					Spacer(modifier = Modifier.height(32.dp))

//					Text(
//						text = _package.product.price,
//						style = MaterialTheme.typography.titleLarge.copy(fontFamily = montserratFontFamily),
//						color = contentColor,
//						fontWeight = FontWeight.Bold
//					)

					Text(
						text = "per year",
						style = MaterialTheme.typography.bodySmall.copy(fontFamily = montserratFontFamily),
						color = contentColor,
						fontWeight = FontWeight.Bold,
						textAlign = TextAlign.Center,
						modifier = Modifier.fillMaxWidth()
					)

					Spacer(modifier = Modifier.height(12.dp))

					Box(
						modifier = Modifier.background(MaterialTheme.colorScheme.primary, MaterialTheme.shapes.medium)
					) {
						Text(
							text = "Save $savePercent%",
							style = MaterialTheme.typography.bodySmall.copy(fontFamily = montserratFontFamily),
							color = MaterialTheme.colorScheme.onPrimary,
							fontWeight = FontWeight.Bold,
							textAlign = TextAlign.Center,
							modifier = Modifier.padding(12.dp, 8.dp)
						)
					}

					Spacer(modifier = Modifier.height(12.dp))

					Text(
						text = "Pay annually\ncancel anytime",
						style = MaterialTheme.typography.bodySmall.copy(fontFamily = montserratFontFamily),
						color = contentColor,
						textAlign = TextAlign.Center,
					)
				}
			}
		}
	}
}
