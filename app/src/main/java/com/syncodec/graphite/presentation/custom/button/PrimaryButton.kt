package com.syncodec.graphite.presentation.custom.button

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp


@Composable
fun PrimaryButton(
	primaryText: String,
	primaryIcon: Int,
	primaryDescription: String,
	secondaryIcon: Int? = null,
	secondaryDescription: String? = null,
	bottomBarSpacingPx: Int,
	onClickPrimary: () -> Unit,
	onClickSecondary: (() -> Unit)? = null,
) {
	Column(
		modifier = Modifier.fillMaxSize(),
		horizontalAlignment = Alignment.CenterHorizontally
	) {
		with(LocalDensity.current) {
			Spacer(modifier = Modifier.height(bottomBarSpacingPx.toDp() - 20.dp))
		}

		Row(
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier
				.height(40.dp)
				.background(MaterialTheme.colorScheme.primary, RoundedCornerShape(50))
				.clip(RoundedCornerShape(50)),
		) {
			Row(
				verticalAlignment = Alignment.CenterVertically,
				modifier = Modifier
					.fillMaxHeight()
					.clickable { onClickPrimary() }
			) {
				Spacer(modifier = Modifier.width(if (secondaryIcon == null) 24.dp else 16.dp))
				Icon(
					painter = painterResource(id = primaryIcon),
					contentDescription = primaryDescription,
					tint = MaterialTheme.colorScheme.onPrimary,
				)

				Spacer(modifier = Modifier.width(8.dp))

				Text(
					text = primaryText,
					style = MaterialTheme.typography.bodyMedium,
					fontWeight = FontWeight.Bold,
					color = MaterialTheme.colorScheme.onPrimary
				)

				Spacer(modifier = Modifier.width(if (secondaryIcon == null) 24.dp else 12.dp))
			}

			if (onClickSecondary != null && secondaryIcon != null) {
				Box(
					modifier = Modifier
						.width(2.dp)
						.height(24.dp)
						.background(MaterialTheme.colorScheme.onPrimary, RoundedCornerShape(50))
				)

				Row(
					verticalAlignment = Alignment.CenterVertically,
					modifier = Modifier
						.fillMaxHeight()
						.clickable { onClickSecondary() }
				) {
					Spacer(modifier = Modifier.width(12.dp))
					Icon(
						painter = painterResource(id = secondaryIcon),
						contentDescription = secondaryDescription,
						tint = MaterialTheme.colorScheme.onPrimary,
					)
					Spacer(modifier = Modifier.width(16.dp))
				}
			}
		}
	}
}
