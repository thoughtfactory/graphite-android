package com.syncodec.graphite.presentation.pro.composable.buildingBlock

import androidx.compose.runtime.Composable
import com.revenuecat.purchases.Package


@Composable
fun LifetimePackageView(
	lifetimePackage : Package? = null,
	onClickPackage : (Package?) -> Unit = {},
) {
//	Box(
//		modifier = Modifier
//			.fillMaxWidth()
//			.padding(24.dp, 0.dp)
//			.background(
//				MaterialTheme.colorScheme
//					.surfaceColorAtElevation(8.dp)
//					.copy(alpha = 0.31f), MaterialTheme.shapes.extraLarge
//			)
//			.clip(MaterialTheme.shapes.extraLarge)
//			.clickable { onClickPackage(lifetimePackage) }
//	) {
//		Crossfade(targetState = lifetimePackage) { _package ->
//			if (_package == null) {
//
//			} else {
//				Row(
//					verticalAlignment = Alignment.CenterVertically,
//					modifier = Modifier
//						.fillMaxWidth()
//						.padding(20.dp)
//				) {
//
//					Column(
//						modifier = Modifier.weight(1f)
//					) {
//						Text(
//							text = "Keep forever",
//							style = MaterialTheme.typography.titleMedium.copy(fontFamily = montserratFontFamily),
//							fontWeight = FontWeight.Bold,
//						)
//
//						Spacer(modifier = Modifier.height(4.dp))
//
//						Text(
//							text = "Unlock all features forever",
//							style = MaterialTheme.typography.bodyMedium.copy(fontFamily = montserratFontFamily),
//						)
//					}
//
//					Spacer(modifier = Modifier.width(12.dp))
//
//					Text(
//						text = _package.product.price,
//						style = MaterialTheme.typography.titleLarge.copy(fontFamily = montserratFontFamily),
//						fontWeight = FontWeight.Bold
//					)
//				}
//			}
//		}
//	}
}
