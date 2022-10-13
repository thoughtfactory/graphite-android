package com.syncodec.graphite.presentation.settings.composable.buildingBlock

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSwitch(
	title: String,
	icon: Int? = null,
	subTitle: String? = null,
	isChecked: Boolean,
	onClick: () -> Unit
) {
	Card(
		colors = CardDefaults.cardColors(
			containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.31f),
			contentColor = MaterialTheme.colorScheme.onSurface,
		),
		modifier = Modifier.padding(12.dp, 4.dp),
		onClick = onClick
	) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier
				.fillMaxWidth()
				.padding(16.dp)
		) {
			if (icon != null) {
				Icon(
					painter = painterResource(id = icon),
					contentDescription = title,
					modifier = Modifier.requiredSize(24.dp)
				)
				Spacer(modifier = Modifier.width(16.dp))
			} else {
				Spacer(modifier = Modifier.width(40.dp))
			}

			Column(
				modifier = Modifier.weight(1f)
			) {
				Text(
					text = title,
					style = MaterialTheme.typography.titleMedium,
					color = MaterialTheme.colorScheme.onSurface,
					fontWeight = FontWeight.Bold
				)
				if (subTitle != null) {
					Spacer(modifier = Modifier.height(4.dp))
					Text(
						text = subTitle,
						style = MaterialTheme.typography.bodySmall,
						color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.71f),
					)
				}
			}

			Spacer(modifier = Modifier.width(16.dp))

			Switch(
				checked = isChecked,
				onCheckedChange = { onClick() },
				colors = SwitchDefaults.colors(
					checkedThumbColor = MaterialTheme.colorScheme.primary,
					checkedTrackColor = MaterialTheme.colorScheme.surface,
					checkedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.71f),
					uncheckedThumbColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.31f),
					uncheckedTrackColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.31f),
					uncheckedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.31f)
				),
			)
		}
	}
}
