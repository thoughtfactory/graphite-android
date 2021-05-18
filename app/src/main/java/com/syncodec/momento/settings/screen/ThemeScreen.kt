package com.syncodec.momento.settings.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.momento.miscellaneous.DataStore
import com.syncodec.momento.ui.theme.*
import kotlin.random.Random


@OptIn(ExperimentalFoundationApi::class)
@Preview
@Composable
fun ThemeScreen() {
	val context = LocalContext.current
	val dataStore = DataStore(context = context)

	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		modifier = Modifier
			.fillMaxSize()
			.background(MaterialTheme.colorScheme.background),
	) {
		Spacer(modifier = Modifier.height(16.dp))
		ThemeView()

		Spacer(modifier = Modifier.height(16.dp))

		LazyVerticalGrid(
			columns = GridCells.Fixed(4),
			modifier = Modifier
				.padding(16.dp, 0.dp),
		) {
			item { ThemeCard(colorScheme = lightColorScheme1) { dataStore.putTheme(1) } }
			item { ThemeCard(colorScheme = lightColorScheme2) { dataStore.putTheme(2) } }
			item { ThemeCard(colorScheme = lightColorScheme3) { dataStore.putTheme(3) } }
			item { ThemeCard(colorScheme = lightColorScheme4) { dataStore.putTheme(4) } }
			item { ThemeCard(colorScheme = lightColorScheme5) { dataStore.putTheme(5) } }
			item { ThemeCard(colorScheme = lightColorScheme6) { dataStore.putTheme(6) } }
			item { ThemeCard(colorScheme = lightColorScheme7) { dataStore.putTheme(7) } }
		}
	}
}

@Preview
@Composable
private fun ThemeView() {
	val configuration = LocalConfiguration.current
	val screenHeight = configuration.screenHeightDp.dp

	Box(
		modifier = Modifier
			.fillMaxWidth()
			.height(screenHeight.times(0.47f)),
		contentAlignment = Alignment.Center
	) {
		Card(
			backgroundColor = MaterialTheme.colorScheme.background,
			shape = RoundedCornerShape(12.dp),
			elevation = 16.dp,
			modifier = Modifier
				.fillMaxWidth(0.47f)
				.fillMaxHeight(),
		) {
			Column(
				modifier = Modifier
					.fillMaxSize()
			) {
				Box(
					modifier = Modifier
						.fillMaxWidth()
						.height(32.dp)
						.background(MaterialTheme.colorScheme.secondaryContainer)
				)
				Spacer(modifier = Modifier.height(6.dp))
				Box(
					modifier = Modifier
						.fillMaxWidth()
						.height(16.dp)
						.padding(8.dp, 0.dp)
						.clip(RoundedCornerShape(50))
						.background(MaterialTheme.colorScheme.secondaryContainer)
				) {
					Box(
						modifier = Modifier
							.fillMaxWidth(0.5f)
							.height(16.dp)
							.clip(RoundedCornerShape(50))
							.background(MaterialTheme.colorScheme.primary)
					)
				}
				Spacer(modifier = Modifier.height(6.dp))

				Card(
					border = BorderStroke(2.dp, MaterialTheme.colorScheme.secondaryContainer),
					elevation = 0.dp,
					shape = RoundedCornerShape(8.dp),
					backgroundColor = Color.Companion.Transparent,
					modifier = Modifier
						.fillMaxWidth()
						.height(72.dp)
						.padding(8.dp, 0.dp),
				) {
					Column(
						modifier = Modifier
							.fillMaxSize()
							.padding(8.dp)
					) {
						Row(modifier = Modifier.fillMaxWidth()) { repeat(6) { RandomTitleText(Random.nextBoolean()) } }
						Spacer(modifier = Modifier.height(8.dp))
						repeat(6) {
							Row(modifier = Modifier.fillMaxWidth()) { repeat(13) { RandomContentText(Random.nextBoolean()) } }
							Spacer(modifier = Modifier.height(4.dp))
						}
						Spacer(modifier = Modifier.height(4.dp))
						Row(modifier = Modifier.fillMaxWidth()) { repeat(13) { RandomTitleText(Random.nextBoolean()) } }
					}
				}

				Spacer(modifier = Modifier.weight(1f))
				Card(
					elevation = 8.dp,
					backgroundColor = MaterialTheme.colorScheme.primaryContainer,
					shape = RoundedCornerShape(8.dp),
					modifier = Modifier
						.requiredSize(40.dp, 32.dp)
						.padding(0.dp, 0.dp, 8.dp, 0.dp)
						.align(Alignment.End),
				) {}
				Spacer(modifier = Modifier.height(8.dp))
				Box(
					modifier = Modifier
						.fillMaxWidth()
						.height(32.dp)
						.background(MaterialTheme.colorScheme.secondaryContainer)
				)
			}
		}
	}
}

@Composable
private fun RandomTitleText(isLong: Boolean) {
	Row(modifier = Modifier) {
		Box(
			modifier = Modifier
				.width(width = if (isLong) 8.dp else 4.dp)
				.height(3.dp)
				.clip(RoundedCornerShape(50))
				.background(MaterialTheme.colorScheme.primary)
		)
		Spacer(modifier = Modifier.width(2.dp))
	}
}

@Composable
private fun RandomContentText(isLong: Boolean) {
	Row(modifier = Modifier) {
		Box(
			modifier = Modifier
				.width(width = if (isLong) 12.dp else 6.dp)
				.height(2.dp)
				.clip(RoundedCornerShape(50))
				.background(MaterialTheme.colorScheme.onBackground)
		)
		Spacer(modifier = Modifier.width(2.dp))
	}
}

@Composable
private fun ThemeCard(
	colorScheme: ColorScheme,
	onClick: () -> Unit
) {
//	Canvas(
//		modifier = Modifier
//		.fillMaxWidth()
//		.aspectRatio(0.85f)
//		.padding(12.dp)
//		.clip(RoundedCornerShape(8.dp))
//		.clickable { onClick() }
//	) {
//	}
	Column(
		modifier = Modifier
			.fillMaxWidth()
			.aspectRatio(0.85f)
			.padding(12.dp)
			.clip(RoundedCornerShape(8.dp))
			.clickable { onClick() }
	) {
		Box(
			modifier = Modifier
				.fillMaxWidth()
				.weight(1f)
				.background(colorScheme.primary)
		)
		Box(
			modifier = Modifier
				.fillMaxWidth()
				.weight(1f)
				.background(colorScheme.secondary)
		)
		Box(
			modifier = Modifier
				.fillMaxWidth()
				.weight(1f)
				.background(colorScheme.surface)
		)
		Box(
			modifier = Modifier
				.fillMaxWidth()
				.weight(1f)
				.background(colorScheme.background)
		)
	}
}
