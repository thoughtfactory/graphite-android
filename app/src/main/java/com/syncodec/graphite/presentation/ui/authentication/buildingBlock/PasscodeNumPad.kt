package com.syncodec.graphite.presentation.ui.authentication.buildingBlock

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R


@Composable
fun PasscodeNumPad(
	onEnter : (String) -> Unit
) {
	var passcode by remember { mutableStateOf("") }

	Column(
		modifier = Modifier.fillMaxWidth()
	) {
		PasscodeView(passcode = passcode)

		Spacer(modifier = Modifier.height(24.dp))

		Row(
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier
				.fillMaxWidth()
				.height(72.dp),
		) {
			NumPadButton(
				text = "1",
				modifier = Modifier.weight(1f)
			) {
				passcode += "1"
				passcode = passcode.take(4)
				if (passcode.length == 4) {
					onEnter(passcode)
					passcode = ""
				}
			}

			NumPadButton(
				text = "2",
				modifier = Modifier.weight(1f)
			) {
				passcode += "2"
				passcode = passcode.take(4)
				if (passcode.length == 4) {
					onEnter(passcode)
					passcode = ""
				}
			}

			NumPadButton(
				text = "3",
				modifier = Modifier.weight(1f)
			) {
				passcode += "3"
				passcode = passcode.take(4)
				if (passcode.length == 4) {
					onEnter(passcode)
					passcode = ""
				}
			}
		}

		Row(
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier
				.fillMaxWidth()
				.height(72.dp),
		) {
			NumPadButton(
				text = "4",
				modifier = Modifier.weight(1f)
			) {
				passcode += "4"
				passcode = passcode.take(4)
				if (passcode.length == 4) {
					onEnter(passcode)
					passcode = ""
				}
			}

			NumPadButton(
				text = "5",
				modifier = Modifier.weight(1f)
			) {
				passcode += "5"
				passcode = passcode.take(4)
				if (passcode.length == 4) {
					onEnter(passcode)
					passcode = ""
				}
			}

			NumPadButton(
				text = "6",
				modifier = Modifier.weight(1f)
			) {
				passcode += "6"
				passcode = passcode.take(4)
				if (passcode.length == 4) {
					onEnter(passcode)
					passcode = ""
				}
			}
		}

		Row(
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier
				.fillMaxWidth()
				.height(72.dp),
		) {
			NumPadButton(
				text = "7",
				modifier = Modifier.weight(1f)
			) {
				passcode += "7"
				passcode = passcode.take(4)
				if (passcode.length == 4) {
					onEnter(passcode)
					passcode = ""
				}
			}

			NumPadButton(
				text = "8",
				modifier = Modifier.weight(1f)
			) {
				passcode += "8"
				passcode = passcode.take(4)
				if (passcode.length == 4) {
					onEnter(passcode)
					passcode = ""
				}
			}

			NumPadButton(
				text = "9",
				modifier = Modifier.weight(1f)
			) {
				passcode += "9"
				passcode = passcode.take(4)
				if (passcode.length == 4) {
					onEnter(passcode)
					passcode = ""
				}
			}
		}

		Row(
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier
				.fillMaxWidth()
				.height(72.dp),
		) {
			NumPadBackspaceButton(
				modifier = Modifier.weight(1f)
			) { passcode = passcode.dropLast(1) }

			NumPadButton(
				text = "0",
				modifier = Modifier.weight(1f)
			) {
				passcode += "0"
				passcode = passcode.take(4)
				if (passcode.length == 4) {
					onEnter(passcode)
					passcode = ""
				}
			}

			Spacer(modifier = Modifier.weight(1f))
		}
	}
}


@Composable
private fun NumPadButton(
	modifier : Modifier = Modifier,
	text : String,
	onClick : () -> Unit
) {
	val interactionSource = remember { MutableInteractionSource() }

	Box(
		contentAlignment = Alignment.Center,
		modifier = modifier
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
	modifier : Modifier = Modifier,
	onClick : () -> Unit
) {
	val interactionSource = remember { MutableInteractionSource() }

	Box(
		contentAlignment = Alignment.Center,
		modifier = modifier
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
		) {
			Icon(
				painter = painterResource(id = R.drawable.ic_backspace),
				contentDescription = "Backspace",
				tint = MaterialTheme.colorScheme.onBackground
			)
		}
	}
}

@Composable
private fun PasscodeView(
	passcode : String,
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
}

@Composable
private fun PasscodeItemView(
	highLight : Boolean
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
