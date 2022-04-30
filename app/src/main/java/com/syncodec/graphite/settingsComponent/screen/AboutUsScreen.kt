package com.syncodec.graphite.settingsComponent.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.settingsComponent.SettingsActivity
import com.syncodec.graphite.settingsComponent.miscellaneous.SettingButton
import com.syncodec.graphite.R


@Composable
fun AboutUsScreen(
	onAction: (SettingsActivity.Action, Any?) -> Unit
) {
	val configuration = LocalConfiguration.current
	val screenWidth = configuration.screenWidthDp.dp

	LazyColumn(
		modifier = Modifier.fillMaxSize(),
		horizontalAlignment = Alignment.CenterHorizontally
	) {
		item {
			Column(
				modifier = Modifier
					.fillMaxWidth()
					.padding(0.dp, 32.dp),
				horizontalAlignment = Alignment.CenterHorizontally
			) {
				Spacer(modifier = Modifier.height(32.dp))
				Icon(
					painter = painterResource(id = R.drawable.ic_icon),
					contentDescription = null,
					tint = Color.Unspecified,
					modifier = Modifier.size(screenWidth / 3)
				)

				Spacer(modifier = Modifier.height(24.dp))

				Text(
					text = "Graphite",
					style = MaterialTheme.typography.titleMedium,
					color = MaterialTheme.colorScheme.onBackground
				)

				Spacer(modifier = Modifier.height(16.dp))

				Text(
					text = "0.0.1",
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.47f)
				)
				Spacer(modifier = Modifier.height(32.dp))
			}
		}

		item {
			SettingButton(
				title = "Instagram",
				icon = R.drawable.ic_instagram,
				tint = Color(0xFFFE7E6D)
			) { onAction(SettingsActivity.Action.INSTAGRAM, null) }
		}
	}
}
