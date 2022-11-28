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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.BaseApplication
import com.syncodec.graphite.presentation.common.composable.ExperimentalTag
import com.syncodec.graphite.presentation.common.composable.ProTag


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSwitch(
	title: String,
	icon: Int? = null,
	subTitle: String? = null,
	isProFeature: Boolean = false,
	isExperimental: Boolean = false,
	isChecked: Boolean,
	onClick: () -> Unit
) {
	val isPro by BaseApplication.isPro

	Card(
		colors = CardDefaults.cardColors(
			containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.47f),
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
			icon?.let {
				Icon(
					painter = painterResource(id = it),
					contentDescription = title,
					modifier = Modifier.requiredSize(24.dp)
				)
				Spacer(modifier = Modifier.width(16.dp))
			} ?: Spacer(modifier = Modifier.width(40.dp))

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

			if (isExperimental) {
				Spacer(modifier = Modifier.width(16.dp))
				ExperimentalTag()
			}

			if (isProFeature && !isPro) {
				Spacer(modifier = Modifier.width(16.dp))
				ProTag()
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
