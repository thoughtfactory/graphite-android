package com.syncodec.graphite.presentation.settings.composable.screen

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.pro.ProActivity
import com.syncodec.graphite.presentation.settings.SettingsActivity
import com.syncodec.graphite.presentation.settings.composable.buildingBlock.GenericSettingsScreen
import com.syncodec.graphite.presentation.settings.composable.buildingBlock.SettingsButton
import com.syncodec.graphite.presentation.ui.LocalIsPro
import com.syncodec.graphite.utils.tone


@Preview
@Composable
fun ThemeScreen() {
	val context = LocalContext.current

	val scrollState = SettingsActivity.LocalScrollState.current
	val onNavigate = SettingsActivity.LocalOnNavigate.current

	val uriHandler = LocalUriHandler.current

	val isPro = LocalIsPro.current


	Column(
		modifier = Modifier
			.fillMaxSize()
			.verticalScroll(scrollState)
	) {
		Spacer(modifier = Modifier.height(8.dp))

		ScreenView()

		Spacer(modifier = Modifier.height(16.dp))


	}
}

@Preview
@Composable
private fun ScreenView() {

	val configuration = LocalConfiguration.current
	val screenHeight = configuration.screenHeightDp.dp
	val screenWidth = configuration.screenWidthDp.dp

	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		modifier = Modifier.fillMaxWidth()
	) {
		Column(
			modifier = Modifier
				.width(screenWidth * 2 / 3)
				.height(screenHeight * 2 / 3)
				.shadow(16.dp, RoundedCornerShape(12.dp))
				.background(MaterialTheme.colorScheme.background, RoundedCornerShape(12.dp))
		) {
			Box(
				modifier = Modifier
					.fillMaxWidth()
					.height(24.dp)
					.background(if (isSystemInDarkTheme()) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onBackground)
			)

			Spacer(modifier = Modifier.height(8.dp))

			Box(
				contentAlignment = Alignment.CenterStart,
				modifier = Modifier
					.fillMaxWidth()
					.padding(8.dp, 0.dp)
			) {
				Box(modifier = Modifier
					.fillMaxWidth()
					.height(24.dp)
					.background(MaterialTheme.colorScheme.surface, RoundedCornerShape(25))
				)

				Box(modifier = Modifier
					.fillMaxWidth(0.31f)
					.height(18.dp)
					.padding(start = 3.dp)
					.background(MaterialTheme.colorScheme.primary, RoundedCornerShape(25))
				)
			}

			Spacer(modifier = Modifier.height(8.dp))

			Box(
				modifier = Modifier
					.fillMaxWidth()
					.height(72.dp)
					.padding(8.dp, 0.dp)
					.border(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.17f), RoundedCornerShape(8.dp)),
			)

			Spacer(modifier = Modifier.weight(1f))

			Row(
				modifier = Modifier.fillMaxWidth()
			) {
				Spacer(modifier = Modifier.weight(1f))

				Box(
					modifier = Modifier
						.width(96.dp)
						.height(36.dp)
						.background(MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
				)

				Spacer(modifier = Modifier.width(8.dp))
			}

			Spacer(modifier = Modifier.height(8.dp))

			Box(
				modifier = Modifier
					.fillMaxWidth()
					.height(48.dp)
					.background(MaterialTheme.colorScheme.surface.tone(isSystemInDarkTheme(), 1))
			)
		}
	}
}
