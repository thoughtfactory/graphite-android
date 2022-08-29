package com.syncodec.graphite.presentation.settings.composable.buildingBlock

import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp


@Composable
fun SettingsButton(
	title: String,
	subTitle: String? = null,
	icon: Int,
	isEnabled: Boolean,
	onClick: () -> Unit
) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.height(64.dp)
			.focusable(true)
			.clickable(isEnabled) { onClick() },
		verticalAlignment = Alignment.CenterVertically
	) {
		Spacer(modifier = Modifier.width(24.dp))

		Icon(painter = painterResource(id = icon), contentDescription = title)

		Spacer(modifier = Modifier.width(24.dp))

		Column(
			modifier = Modifier.fillMaxHeight(),
			verticalArrangement = Arrangement.Center
		) {
			Text(
				text = title,
				style = MaterialTheme.typography.titleMedium,
			)
			if (subTitle != null) {
				Text(
					text = title,
					style = MaterialTheme.typography.bodySmall,
				)
			}
		}

		Spacer(modifier = Modifier.width(32.dp))
	}
}
