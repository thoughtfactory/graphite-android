package com.syncodec.momento.settings.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.syncodec.momento.settings.SettingsActivity
import com.syncodec.momento.settings.miscellaneous.SettingButton


@Composable
fun PreferenceScreen(
	onClick: (SettingsActivity.Click) -> Unit
) {
	LazyColumn(
		modifier = Modifier
			.fillMaxSize()
	) {
		item { ThemeButton(title = "Theme", subTitle = "Material you") { onClick(SettingsActivity.Click.THEME) } }
		item { SettingButton(title = "Font Family", subTitle = "Roboto") { onClick(SettingsActivity.Click.FONT_FAMILY) } }
		item { SettingButton(title = "Font Size", subTitle = "13pt") { onClick(SettingsActivity.Click.ABOUT_US) } }
		item { SettingButton(title = "Dark theme", "System") { onClick(SettingsActivity.Click.ABOUT_US) } }
	}
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ThemeButton(
	title: String,
	subTitle: String? = null,
	onClick: () -> Unit
) {
	Card(
		modifier = Modifier
			.fillMaxWidth()
			.height(64.dp),
		elevation = 0.dp,
		backgroundColor = MaterialTheme.colorScheme.background,
		onClick = { onClick() }
	) {
		Row(
			modifier = Modifier
				.fillMaxSize()
				.padding(24.dp, 0.dp),
			verticalAlignment = Alignment.CenterVertically
		) {
			Column(
				modifier = Modifier
					.fillMaxHeight()
					.weight(1f),
				verticalArrangement = Arrangement.Center
			) {
				Text(
					text = title,
					style = MaterialTheme.typography.bodyMedium,
					fontWeight = FontWeight.Bold,
					color = MaterialTheme.colorScheme.onBackground,
					modifier = Modifier
				)

				if (subTitle != null) {
					Text(
						text = subTitle,
						style = MaterialTheme.typography.bodySmall,
						color = MaterialTheme.colorScheme.onSurfaceVariant,
						modifier = Modifier
					)
				}
			}

			Card(
				modifier = Modifier.requiredSize(32.dp),
				backgroundColor = MaterialTheme.colorScheme.primaryContainer,
				border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
				elevation = 0.dp,
				shape = RoundedCornerShape(50)
			){}
		}
	}
}
