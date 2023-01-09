package com.syncodec.graphite.presentation.common.button

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.presentation.ui.IconButtonSize


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun PrimaryButton(
	primaryText: String,
	primaryIcon: Int,
	primaryDescription: String,
	secondaryIcon: Int? = null,
	secondaryDescription: String? = null,
	bottomBarSpacingPx: Int?,
	onClickPrimary: () -> Unit,
	onClickSecondary: (() -> Unit)? = null,
) {
	AnimatedVisibility(
		visible = bottomBarSpacingPx != null,
		enter = fadeIn(tween(300)) + scaleIn(tween(300)),
		exit = fadeOut(tween(300)) + scaleOut(tween(300)),
	) {
		Column(
			modifier = Modifier.fillMaxSize(),
			horizontalAlignment = Alignment.CenterHorizontally
		) {
			with(LocalDensity.current) {
				Spacer(modifier = Modifier.height((bottomBarSpacingPx?.toDp() ?: 0.dp) - 20.dp))
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
						modifier = Modifier.requiredSize(IconButtonSize)
					)

					Spacer(modifier = Modifier.width(12.dp))

					Text(
						text = primaryText,
						style = MaterialTheme.typography.bodyMedium,
						color = MaterialTheme.colorScheme.onPrimary
					)

					Spacer(modifier = Modifier.width(if (secondaryIcon == null) 24.dp else 12.dp))
				}

				if (onClickSecondary != null && secondaryIcon != null) {
					Box(
						modifier = Modifier
							.width(2.dp)
							.height(16.dp)
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
							modifier = Modifier
								.requiredSize(IconButtonSize)
								.padding(2.dp)
						)
						Spacer(modifier = Modifier.width(16.dp))
					}
				}
			}
		}
	}
}

@Composable
fun PrimaryButton(
	primaryText: String,
	primaryIcon: Int,
	primaryDescription: String,
	secondaryIcon: Int? = null,
	secondaryDescription: String? = null,
	bottomSpacing: Dp = 0.dp,
	onClickPrimary: () -> Unit,
	onClickSecondary: (() -> Unit)? = null,
) {
	Column(
		modifier = Modifier.fillMaxSize(),
		horizontalAlignment = Alignment.CenterHorizontally
	) {
		Spacer(modifier = Modifier.weight(1f))

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
					modifier = Modifier.requiredSize(IconButtonSize)
				)

				Spacer(modifier = Modifier.width(12.dp))

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
						.height(16.dp)
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
						modifier = Modifier.requiredSize(IconButtonSize)
					)
					Spacer(modifier = Modifier.width(16.dp))
				}
			}
		}

		Spacer(modifier = Modifier.height(bottomSpacing))
	}
}
