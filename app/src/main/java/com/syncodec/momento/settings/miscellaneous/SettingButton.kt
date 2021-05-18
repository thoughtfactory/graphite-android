package com.syncodec.momento.settings.miscellaneous

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SettingButton(
	title: String,
	subTitle: String? = null,
	enabled: Boolean = true,
	onClick: () -> Unit
) {
	Card(
		modifier = Modifier
			.fillMaxWidth()
			.height(64.dp),
		elevation = 0.dp,
		backgroundColor = MaterialTheme.colorScheme.background,
		enabled = enabled,
		onClick = { onClick() }
	) {
		Column(
			modifier = Modifier
				.fillMaxSize()
				.padding(24.dp, 0.dp),
			verticalArrangement = Arrangement.Center
		) {
			Text(
				text = title,
				style = MaterialTheme.typography.bodyMedium,
				fontWeight = FontWeight.Bold,
				color = if (enabled) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onBackground.copy(0.47f),
				modifier = Modifier
			)

			if (subTitle != null) {
				Text(
					text = subTitle,
					style = MaterialTheme.typography.bodySmall,
					color = if (enabled) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onBackground.copy(0.47f),
					modifier = Modifier
				)
			}
		}
	}
}
