package com.syncodec.graphite.presentation.ui.authentication

import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.button.MenuButton


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPasscodeScreen(
	onPasscodeAdded: (String) -> Unit
) {
	val activity = LocalContext.current as ComponentActivity
	Column(
		modifier = Modifier
			.fillMaxSize()
			.background(MaterialTheme.colorScheme.background)
	) {
		TopAppBar(
			modifier = Modifier.fillMaxWidth(),
			navigationIcon = {
				MenuButton(
					icon = R.drawable.ic_close,
					tint = MaterialTheme.colorScheme.onBackground
				) {
					activity.onBackPressed()
				}
			},
			title = {},
			colors = TopAppBarDefaults.smallTopAppBarColors(
				containerColor = MaterialTheme.colorScheme.background,
				navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
				titleContentColor = MaterialTheme.colorScheme.onBackground,
			)
		)

		Spacer(modifier = Modifier.weight(1f))

		PasscodeNumPad(
			modifier = Modifier.fillMaxWidth()
		)

		Spacer(modifier = Modifier.height(24.dp))
	}
}

@Composable
private fun PasscodeNumPad(
	modifier: Modifier = Modifier,
) {
	Column(modifier = modifier) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier
				.fillMaxWidth()
				.height(72.dp),
		) {
			NumPadButton(
				text = "1",
				modifier = Modifier.weight(1f)
			) {}

			NumPadButton(
				text = "2",
				modifier = Modifier.weight(1f)
			) {}

			NumPadButton(
				text = "3",
				modifier = Modifier.weight(1f)
			) {}
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
			) {}

			NumPadButton(
				text = "5",
				modifier = Modifier.weight(1f)
			) {}

			NumPadButton(
				text = "6",
				modifier = Modifier.weight(1f)
			) {}
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
			) {}

			NumPadButton(
				text = "8",
				modifier = Modifier.weight(1f)
			) {}

			NumPadButton(
				text = "9",
				modifier = Modifier.weight(1f)
			) {}
		}

		Row(
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier
				.fillMaxWidth()
				.height(72.dp),
		) {
			NumPadButton(
				modifier = Modifier.weight(1f),
				text = "EN"
			) {}

			NumPadButton(
				text = "0",
				modifier = Modifier.weight(1f)
			) {}

			NumPadButton(
				text = "BK",
				modifier = Modifier.weight(1f)
			) {}
		}
	}
}

@Composable
private fun NumPadButton(
	modifier: Modifier = Modifier,
	text: String,
	onClick: () -> Unit
) {
	val interactionSource = remember { MutableInteractionSource() }

	rememberRipple()

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
				.indication(interactionSource, rememberRipple())
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
