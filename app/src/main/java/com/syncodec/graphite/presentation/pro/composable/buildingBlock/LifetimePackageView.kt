package com.syncodec.graphite.presentation.pro.composable.buildingBlock

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.revenuecat.purchases.Package
import com.syncodec.graphite.presentation.ui.MontserratTypography
import com.syncodec.graphite.presentation.ui.montserratFontFamily


@Composable
fun LifetimePackageView(
	lifetimePackage : Package? = null,
	onClickPackage : (Package?) -> Unit = {},
) {
	Box(
		modifier = Modifier
			.fillMaxWidth()
			.padding(24.dp, 0.dp)
			.background(
				MaterialTheme.colorScheme
					.surfaceColorAtElevation(8.dp)
					.copy(alpha = 0.31f), MaterialTheme.shapes.extraLarge
			)
			.clip(MaterialTheme.shapes.extraLarge)
			.clickable { onClickPackage(lifetimePackage) }
	) {
		Crossfade(targetState = lifetimePackage) { _package ->
			if (_package == null) {

			} else {
				Row(
					verticalAlignment = Alignment.CenterVertically,
					modifier = Modifier
						.fillMaxWidth()
						.padding(20.dp)
				) {

					Column(
						modifier = Modifier.weight(1f)
					) {
						Text(
							text = "Keep forever",
							style = MaterialTheme.typography.titleMedium.copy(fontFamily = montserratFontFamily),
							fontWeight = FontWeight.Bold,
						)

						Spacer(modifier = Modifier.height(4.dp))

						Text(
							text = "Unlock all features forever",
							style = MaterialTheme.typography.bodyMedium.copy(fontFamily = montserratFontFamily),
						)
					}

					Spacer(modifier = Modifier.width(12.dp))

					Text(
						text = _package.product.price,
						style = MaterialTheme.typography.titleLarge.copy(fontFamily = montserratFontFamily),
						fontWeight = FontWeight.Bold
					)
				}
			}
		}
	}
}
