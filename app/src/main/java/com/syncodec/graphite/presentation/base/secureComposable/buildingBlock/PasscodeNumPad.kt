package com.syncodec.graphite.presentation.base.secureComposable.buildingBlock

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.base.IconButtonSize
import com.syncodec.graphite.utils.isTablet


@Preview
@Composable
fun PasscodeNumPad(
	enabled : Boolean = true,
	onEnter: (String) -> Unit = {}
) {
	var passcode by remember { mutableStateOf("") }

	fun onClickKey(key: String) {
		passcode += key
		passcode = passcode.take(4)
		if (passcode.length == 4) {
			onEnter(passcode)
			passcode = ""
		}
	}

	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		modifier = Modifier.fillMaxWidth()
	) {
		Row(
			horizontalArrangement = Arrangement.Center,
			modifier = Modifier.fillMaxWidth()
		) {
			PasscodeItemView(highLight = passcode.isNotEmpty())
			PasscodeItemView(highLight = passcode.length > 1)
			PasscodeItemView(highLight = passcode.length > 2)
			PasscodeItemView(highLight = passcode.length > 3)
		}


		Spacer(modifier = Modifier.height(24.dp))

		LazyVerticalGrid(
			columns = GridCells.Fixed(3),
			modifier = if (isTablet()) Modifier.widthIn(max = 420.dp) else Modifier.fillMaxWidth()
		) {
			listOf("1", "2", "3", "4", "5", "6", "7", "8", "9").forEach { key ->
				item {
					NumPadButton(
						text = key,
						enabled = enabled,
						onClick = { onClickKey(key) }
					)
				}
			}
			item {
				Spacer(modifier = Modifier.weight(1f))
			}
			item {
				NumPadButton(
					text = "0",
					enabled = enabled,
					onClick = { onClickKey("0") }
				)
			}
			item {
				NumPadBackspaceButton(
					modifier = Modifier.weight(1f),
				) { passcode = passcode.dropLast(1) }
			}
		}

		Spacer(modifier = Modifier.height(32.dp))
	}
}


@Composable
private fun NumPadButton(
	text: String,
	enabled: Boolean = true,
	onClick: () -> Unit
) {
	val interactionSource = remember { MutableInteractionSource() }

	Box(
		contentAlignment = Alignment.Center,
		modifier = Modifier
			.aspectRatio(2f)
			.clickable(
				indication = null,
				interactionSource = interactionSource,
				enabled = enabled,
				onClick = onClick
			)
	) {
		Box(
			contentAlignment = Alignment.Center,
			modifier = Modifier
				.requiredSize(48.dp)
				.clip(CircleShape)
				.indication(interactionSource, rememberRipple(color = MaterialTheme.colorScheme.onBackground))
		) {
			Text(
				text = text,
				style = MaterialTheme.typography.titleMedium,
				color = MaterialTheme.colorScheme.onBackground,
				fontWeight = FontWeight.Bold
			)
		}
	}
}

@Composable
private fun NumPadBackspaceButton(
	modifier: Modifier = Modifier,
	onClick: () -> Unit
) {
	val interactionSource = remember { MutableInteractionSource() }

	Box(
		contentAlignment = Alignment.Center,
		modifier = modifier
			.aspectRatio(2f)
			.clickable(
				indication = null,
				interactionSource = interactionSource,
				onClick = onClick
			)
	) {
		Box(
			contentAlignment = Alignment.Center,
			modifier = Modifier
				.requiredSize(64.dp)
				.clip(CircleShape)
				.indication(interactionSource, rememberRipple(color = MaterialTheme.colorScheme.onBackground))
		) {
			Icon(
				painter = painterResource(id = R.drawable.ic_fa_backsapce),
				contentDescription = stringResource(id = R.string.backspace),
				tint = MaterialTheme.colorScheme.onBackground,
				modifier = Modifier.requiredSize(IconButtonSize)
			)
		}
	}
}

@Composable
private fun PasscodeItemView(
	highLight: Boolean
) {
	val color by animateColorAsState(
		targetValue = if (highLight) MaterialTheme.colorScheme.onBackground.copy(alpha = 0.71f) else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.17f),
		animationSpec = tween(durationMillis = 300)
	)

	Box(
		modifier = Modifier
			.requiredSize(24.dp)
			.padding(4.dp)
			.clip(CircleShape)
			.background(color)
	)
}
