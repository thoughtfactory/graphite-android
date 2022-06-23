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
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.BuildConfig
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
					style = MaterialTheme.typography.headlineLarge,
					color = MaterialTheme.colorScheme.onBackground
				)

				Spacer(modifier = Modifier.height(16.dp))

				Text(
					text = BuildConfig.VERSION_NAME,
					style = MaterialTheme.typography.titleLarge,
					fontWeight = FontWeight.Bold,
					color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.71f)
				)
				Spacer(modifier = Modifier.height(32.dp))
			}
		}

		item {
			SettingButton(
				title = "Spread a word",
				leadingIcon = R.drawable.ic_share,
			) { onAction(SettingsActivity.Action.SHARE_A_WORD, null) }
		}
		item {
			SettingButton(
				title = "Rate Us",
				leadingIcon = R.drawable.ic_rate_us,
			) { onAction(SettingsActivity.Action.RATE_US, null) }
		}
		item {
			SettingButton(
				title = "Open Source Licenses",
				leadingIcon = R.drawable.ic_code,
			) { onAction(SettingsActivity.Action.OPEN_SOURCE_LICENSES, null) }
		}
		item {
			SettingButton(
				title = "Find us on Instagram",
				leadingIcon = R.drawable.ic_instagram,
			) { onAction(SettingsActivity.Action.INSTAGRAM, null) }
		}

		item { Spacer(modifier = Modifier.height(80.dp)) }

		item { MadeWithLove() }

		item { Spacer(modifier = Modifier.height(80.dp)) }
	}
}

@Composable
private fun MadeWithLove() {
	Row(
		modifier = Modifier.fillMaxWidth(),
		horizontalArrangement = Arrangement.Center,
		verticalAlignment = Alignment.CenterVertically
	) {
		Text(
			text = "made with",
			style = MaterialTheme.typography.bodyLarge,
			fontFamily = FontFamily(Font(R.font.graduate_regular, FontWeight.Normal)),
			fontWeight = FontWeight.Bold,
			color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.47f)
		)

		Spacer(modifier = Modifier.width(6.dp))

		Icon(
			painter = painterResource(id = R.drawable.ic_love),
			contentDescription = "Love",
			tint = Color.Unspecified,
			modifier = Modifier.requiredSize(14.dp)
		)

		Spacer(modifier = Modifier.width(6.dp))

		Text(
			text = "on",
			style = MaterialTheme.typography.bodyLarge,
			fontFamily = FontFamily(Font(R.font.graduate_regular, FontWeight.Normal)),
			fontWeight = FontWeight.Bold,
			color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.47f)
		)

		Spacer(modifier = Modifier.width(6.dp))

		Icon(
			painter = painterResource(id = R.drawable.ic_earth),
			contentDescription = "Earth",
			tint = Color.Unspecified,
			modifier = Modifier.requiredSize(16.dp)
		)
	}
}
