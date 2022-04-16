package com.syncodec.momento.settings.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.momento.custom.squircle.SquircleShape
import com.syncodec.momento.settings.SettingsActivity
import com.syncodec.momento.ui.theme.*
import kotlin.random.Random


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ThemeScreen(
	onClick: (SettingsActivity.Click, Int) -> Unit
) {
	val backgroundColorList : List<Pair<Color, Color>> = listOf(
		Pair(lightBackground0, darkBackground0),
		Pair(lightBackground1, darkBackground1),
		Pair(lightBackground2, darkBackground2),
		Pair(lightBackground3, darkBackground3),
		Pair(lightBackground4, darkBackground4),
		Pair(lightBackground5, darkBackground5),
		Pair(lightBackground6, darkBackground6),
		Pair(lightBackground7, darkBackground7),
		Pair(lightBackground8, darkBackground8),
		Pair(lightBackground9, darkBackground9),
	)

	val themeList : List<Pair<ColorScheme, ColorScheme>> = listOf(
		Pair(lightColorScheme0, darkColorScheme0),
		Pair(lightColorScheme1, darkColorScheme1),
		Pair(lightColorScheme2, darkColorScheme2),
		Pair(lightColorScheme3, darkColorScheme3),
		Pair(lightColorScheme4, darkColorScheme4),
		Pair(lightColorScheme5, darkColorScheme5),
		Pair(lightColorScheme6, darkColorScheme6),
	)

	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		modifier = Modifier
			.fillMaxSize()
			.background(MaterialTheme.colorScheme.background),
	) {
		Spacer(modifier = Modifier.height(16.dp))
		ThemeView()

		Spacer(modifier = Modifier.height(16.dp))

		LazyRow(
			modifier = Modifier.fillMaxWidth(),
			verticalAlignment = Alignment.CenterVertically,
		) {
			item { Spacer(modifier = Modifier.width(16.dp)) }
			backgroundColorList.forEachIndexed { index, data ->
				item {
					BackgroundCard(
						lightBackground = data.first,
						darkBackground = data.second
					) { onClick(SettingsActivity.Click.CHANGE_BACKGROUND, index) }
				}
				item { Spacer(modifier = Modifier.width(8.dp)) }
			}
			item { Spacer(modifier = Modifier.width(8.dp)) }
		}

		Spacer(modifier = Modifier.height(16.dp))

		LazyVerticalGrid(
			columns = GridCells.Fixed(4),
			modifier = Modifier.padding(16.dp, 0.dp),
		) {
			themeList.forEachIndexed { index, data ->
				item {
					ThemeCard(colorScheme = data.first) {
						onClick(SettingsActivity.Click.CHANGE_THEME, index)
					}
				}
			}
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
				modifier = Modifier.fillMaxSize()
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
							Row(modifier = Modifier.fillMaxWidth()) {
								repeat(13) {
									RandomContentText(
										Random.nextBoolean()
									)
								}
							}
							Spacer(modifier = Modifier.height(4.dp))
						}
						Spacer(modifier = Modifier.height(4.dp))
						Row(modifier = Modifier.fillMaxWidth()) {
							repeat(13) {
								RandomTitleText(
									Random.nextBoolean()
								)
							}
						}
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
private fun BackgroundCard(
	lightBackground: Color,
	darkBackground: Color,
	onClick: () -> Unit
) {
	Box(
		modifier = Modifier
			.requiredSize(48.dp)
			.clip(RoundedCornerShape(25))
			.clickable { onClick() }
	) {
		Column(
			modifier = Modifier.fillMaxSize()
		) {
			Box(
				modifier = Modifier
					.fillMaxWidth()
					.weight(1f)
					.background(lightBackground)
			)
			Box(
				modifier = Modifier
					.fillMaxWidth()
					.weight(1f)
					.background(darkBackground)
			)
		}
	}
}

@Composable
private fun ThemeCard(
	colorScheme: ColorScheme,
	onClick: () -> Unit
) {
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
