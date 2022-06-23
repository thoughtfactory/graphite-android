package com.syncodec.graphite.settingsComponent.miscellaneous

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SettingButton(
	title: String,
	subTitle: String? = null,
	leadingIcon: Int,
	trailingIcon: Int? = null,
	enabled: Boolean = true,
	onClickTrailingIcon: (() -> Unit)? = null,
	onClick: () -> Unit,
) {
	val tint by animateColorAsState(
		targetValue = if (enabled) MaterialTheme.colorScheme.onBackground
		else MaterialTheme.colorScheme.onBackground.copy(0.47f)
	)

	Row(
		modifier = Modifier
			.fillMaxWidth()
			.height(64.dp)
			.clickable(enabled = enabled) { onClick() },
		verticalAlignment = Alignment.CenterVertically
	) {
		Spacer(modifier = Modifier.width(16.dp))
		Icon(
			painter = painterResource(id = leadingIcon),
			contentDescription = title,
			modifier = Modifier.requiredSize(28.dp),
			tint = tint
		)
		Spacer(modifier = Modifier.width(24.dp))
		Column(
			modifier = Modifier.weight(1f),
			verticalArrangement = Arrangement.Center
		) {
			Text(
				text = title,
				style = MaterialTheme.typography.titleMedium,
				color = tint,
				modifier = Modifier
			)

			AnimatedContent(targetState = subTitle) {
				if (it != null) {
					Spacer(modifier = Modifier.height(2.dp))
					Text(
						text = it,
						style = MaterialTheme.typography.bodySmall,
						color = tint,
						modifier = Modifier.weight(1f)
					)
				}
			}
		}

		if (trailingIcon != null) {
			Spacer(modifier = Modifier.width(24.dp))

			IconButton(onClick = { onClickTrailingIcon?.invoke() }) {
				Icon(
					painter = painterResource(id = trailingIcon),
					contentDescription = null,
					modifier = Modifier.requiredSize(24.dp),
					tint = tint
				)
			}
		}

		Spacer(modifier = Modifier.width(16.dp))
	}
}
