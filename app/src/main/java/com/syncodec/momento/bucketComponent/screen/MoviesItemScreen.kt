package com.syncodec.momento.bucketComponent.screen

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberImagePainter
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.flowlayout.MainAxisAlignment
import com.syncodec.momento.R
import com.syncodec.momento.bucketComponent.BucketItemViewModel
import com.syncodec.momento.bucketComponent.modalBottomSheet.MovieData
import com.syncodec.momento.custom.ExpandableBox
import com.syncodec.momento.custom.LargeButton
import com.syncodec.momento.miscellaneous.generatePrimaryKey
import compose.icons.TablerIcons
import compose.icons.tablericons.*
import java.io.File
import kotlin.random.Random


@OptIn(ExperimentalMaterialApi::class, ExperimentalAnimationApi::class)
@Composable
fun MoviesItemScreen() {
	val context = LocalContext.current
	val configuration = LocalConfiguration.current
	val screenWidth = configuration.screenWidthDp.dp
	val screenHeight = configuration.screenHeightDp.dp
	val objectMapper = ObjectMapper().registerModule(KotlinModule())

	val viewModel: BucketItemViewModel = viewModel()
	val movieData: MovieData = objectMapper.readValue(viewModel.bucketItemDataJson.toString())
	val contentThumbnail: Boolean? = viewModel.isContentThumbnailAvailable

	val scrollState = rememberScrollState()

	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		modifier = Modifier
			.fillMaxWidth()
			.background(MaterialTheme.colorScheme.background)
			.verticalScroll(
				state = scrollState
			)
	) {
		Spacer(modifier = Modifier.height(24.dp))
		Card(
			elevation = 8.dp,
			shape = RoundedCornerShape(12.dp),
			modifier = Modifier
				.width(screenWidth * 0.5f)
				.aspectRatio(0.75f)
				.padding(0.dp),
		) {
			if (contentThumbnail != null) {
				Image(
					painter = rememberImagePainter(
						data = File(viewModel.contentThumbnailPath!!),
						builder = {
							crossfade(true)
						}
					),
					contentDescription = null,
					contentScale = ContentScale.Crop,
					modifier = Modifier
						.fillMaxSize()
				)
			}

			Box(
				modifier = Modifier
					.fillMaxSize()
					.clip(CircleShape),
				contentAlignment = Alignment.BottomEnd
			) {
				Icon(
					imageVector = TablerIcons.Pencil,
					contentDescription = null,
					tint = MaterialTheme.colorScheme.primaryContainer
				)
			}
		}

		Spacer(modifier = Modifier.height(24.dp))

		HeaderCard(movieData = movieData, movieCharactersData = viewModel.movieCharactersData)

		Spacer(modifier = Modifier.height(12.dp))

		ThoughtCard()

		Spacer(modifier = Modifier.height(12.dp))

		Tags(
			tagList = viewModel.bucketItem.tag
		) {}

		Spacer(modifier = Modifier.height(12.dp))

		Card(
			backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
			elevation = 0.dp,
			shape = RoundedCornerShape(12.dp),
			modifier = Modifier
				.fillMaxWidth()
				.padding(24.dp, 0.dp),
		) {
			Text(
				text = movieData.overview,
				color = MaterialTheme.colorScheme.onSecondaryContainer,
				style = MaterialTheme.typography.bodyMedium,
				modifier = Modifier
					.fillMaxWidth()
					.padding(16.dp)
			)
		}

		Spacer(modifier = Modifier.height(12.dp))

		LargeButton(
			text = "Mark as watched",
			backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
			textColor = MaterialTheme.colorScheme.onSecondaryContainer,
			modifier = Modifier
				.fillMaxWidth()
				.height(48.dp)
				.padding(24.dp, 0.dp)
				.focusable()
		) {

		}

		Spacer(modifier = Modifier.height(12.dp))

		LargeButton(
			text = "Open in themoviedb",
			backgroundColor = MaterialTheme.colorScheme.surface,
			textColor = MaterialTheme.colorScheme.onSurface,
			modifier = Modifier
				.fillMaxWidth()
				.height(48.dp)
				.padding(24.dp, 0.dp)
				.focusable()
		) {

		}

		Spacer(modifier = Modifier.height(48.dp))

		PowerByCard()

		Spacer(modifier = Modifier.height(16.dp))
	}
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun HeaderCard(
	movieData: MovieData,
	movieCharactersData: List<MovieCharacterData>
) {
	var expandInformation: Boolean by remember { mutableStateOf(false) }

	Card(
		modifier = Modifier
			.fillMaxWidth()
			.padding(24.dp, 0.dp),
		elevation = 0.dp,
		shape = RoundedCornerShape(12.dp),
		border = BorderStroke(1.dp, MaterialTheme.colorScheme.primaryContainer),
		backgroundColor = MaterialTheme.colorScheme.background,
		onClick = {
			expandInformation = !expandInformation
		}
	) {
		Column(
			modifier = Modifier
				.padding(16.dp)
		) {
			Row(
				verticalAlignment = Alignment.CenterVertically,
			) {
				Column(
					modifier = Modifier
						.weight(1f)
				) {
					Text(
						text = movieData.title,
						color = MaterialTheme.colorScheme.onSecondaryContainer,
						style = MaterialTheme.typography.titleLarge,
						fontWeight = FontWeight.Bold,
						modifier = Modifier,
					)

					Spacer(modifier = Modifier.height(4.dp))

					Row(
						verticalAlignment = Alignment.CenterVertically,
						modifier = Modifier,
					) {
						Icon(
							imageVector = TablerIcons.CalendarEvent,
							contentDescription = "Release date",
							tint = MaterialTheme.colorScheme.onBackground,
							modifier = Modifier
								.requiredSize(16.dp)
						)
						Spacer(modifier = Modifier.width(8.dp))
						Text(
							text = movieData.releaseDate?.substring(0, 4) ?: "Unavailable",
							color = MaterialTheme.colorScheme.primary,
							style = MaterialTheme.typography.bodyMedium,
							modifier = Modifier
								.fillMaxWidth()
								.padding(0.dp)
						)
					}
				}

				Spacer(modifier = Modifier.width(8.dp))

				Icon(
					imageVector = TablerIcons.CaretDown,
					contentDescription = "More information",
					tint = MaterialTheme.colorScheme.onBackground,
					modifier = Modifier
						.requiredSize(20.dp)
				)
				Spacer(modifier = Modifier.width(8.dp))
			}

			ExpandableBox(
				visible = expandInformation,
				initialVisibility = false
			) {
				Column {
					Spacer(modifier = Modifier.height(16.dp))

					movieCharactersData.forEachIndexed { index, movieCharacterData ->
						if (index < 5) {
							CharacterCard(
								name = movieCharacterData.name,
								role = movieCharacterData.character
							)
						}
					}
				}
			}
		}
	}
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun CharacterCard(
	name: String,
	role: String
) {
	Card(
		onClick = {},
		shape = RoundedCornerShape(4.dp),
		elevation = 0.dp,
		backgroundColor = MaterialTheme.colorScheme.background,
		modifier = Modifier
			.fillMaxWidth(),
	) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier
				.fillMaxWidth()
				.padding(8.dp, 4.dp),
		) {
			Text(
				text = name,
				style = MaterialTheme.typography.titleSmall,
				color = MaterialTheme.colorScheme.primary,
				modifier = Modifier
					.weight(1f)
			)
			Spacer(modifier = Modifier.width(16.dp))
			Text(
				text = role,
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.primary,
				modifier = Modifier
					.weight(1f)
			)
		}
	}
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun ThoughtCard() {
	val viewModel: BucketItemViewModel = viewModel()
	var showEditor by remember { mutableStateOf(false) }
	var thoughtString by remember { mutableStateOf("") }

	val focusRequester = remember { FocusRequester() }

	SideEffect {
		if (showEditor) {
			focusRequester.requestFocus()
		}
	}

	Card(
		backgroundColor = MaterialTheme.colorScheme.background,
		elevation = 0.dp,
		shape = RoundedCornerShape(12.dp),
		border = BorderStroke(1.dp, MaterialTheme.colorScheme.primaryContainer),
		modifier = Modifier
			.fillMaxWidth()
			.padding(24.dp, 0.dp)
			.clip(RoundedCornerShape(12.dp)),
	) {
		Column(
			modifier = Modifier
				.padding(0.dp)
		) {
			viewModel.contentList.forEachIndexed { index, content ->
				Box(
					modifier = Modifier
						.fillMaxWidth()
						.wrapContentHeight()
						.clickable { }
				) {
					Text(
						text = content,
						style = MaterialTheme.typography.bodyMedium,
						color = MaterialTheme.colorScheme.onBackground,
						modifier = Modifier
							.padding(16.dp, if (index == 0) 16.dp else 8.dp, 16.dp, 8.dp)
					)
				}

				Box(
					modifier = Modifier
						.fillMaxWidth()
						.height(1.dp)
						.padding(24.dp, 0.dp)
						.background(MaterialTheme.colorScheme.primaryContainer),
				)
			}

			AnimatedVisibility(
				visible = showEditor
			) {
				Column(
					horizontalAlignment = Alignment.CenterHorizontally,
					modifier = Modifier
						.fillMaxWidth()
						.wrapContentHeight()
						.padding(0.dp),
				) {
					Spacer(modifier = Modifier.height(0.dp))

					BasicTextField(
						value = thoughtString,
						onValueChange = { thoughtString = it },
						textStyle = MaterialTheme.typography.bodyMedium.copy(
							color = MaterialTheme.colorScheme.onBackground
						),
						cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
						modifier = Modifier
							.fillMaxWidth()
							.height(Dp.Infinity)
							.padding(16.dp)
							.focusRequester(focusRequester),
					) { innerTextField ->
						if (thoughtString.isEmpty()) {
							Text(
								"Write here",
								style = MaterialTheme.typography.bodyMedium,
								color = MaterialTheme.colorScheme.primaryContainer
							)
						}
						innerTextField()
					}
					Spacer(modifier = Modifier.height(8.dp))
					Row(
						modifier = Modifier
					) {
						Box(
							modifier = Modifier
								.weight(1f)
								.padding(4.dp)
								.background(MaterialTheme.colorScheme.background)
								.clip(RoundedCornerShape(12.dp))
								.clickable { showEditor = false },
							contentAlignment = Alignment.Center
						) {
							Row(
								modifier = Modifier
									.height(32.dp)
									.padding(4.dp),
								verticalAlignment = Alignment.CenterVertically,
								horizontalArrangement = Arrangement.Center
							) {
								Icon(
									imageVector = TablerIcons.X,
									contentDescription = "Discard",
									tint = MaterialTheme.colorScheme.onBackground,
									modifier = Modifier
										.requiredSize(16.dp)
								)
								Spacer(modifier = Modifier.width(8.dp))
								Text(
									text = "Discard",
									style = MaterialTheme.typography.bodyMedium,
									color = MaterialTheme.colorScheme.onBackground
								)
							}
						}

						Box(
							modifier = Modifier
								.weight(1f)
								.padding(4.dp)
								.background(MaterialTheme.colorScheme.background)
								.clip(RoundedCornerShape(12.dp))
								.clickable {
									if (thoughtString.isNotEmpty()) {
										viewModel.contentList.add(thoughtString)
										viewModel.updateBucketItem()
										thoughtString = ""
									}
								},
							contentAlignment = Alignment.Center
						) {
							Row(
								modifier = Modifier
									.height(32.dp)
									.padding(4.dp),
								verticalAlignment = Alignment.CenterVertically,
								horizontalArrangement = Arrangement.Center
							) {
								Icon(
									imageVector = TablerIcons.Check,
									contentDescription = "Save",
									tint = MaterialTheme.colorScheme.primary,
									modifier = Modifier
										.requiredSize(16.dp)
								)
								Spacer(modifier = Modifier.width(8.dp))
								Text(
									text = "Save",
									style = MaterialTheme.typography.bodyMedium,
									color = MaterialTheme.colorScheme.primary
								)
							}
						}
					}
				}
			}

			AnimatedVisibility(
				visible = !showEditor
			) {
				Box(
					modifier = Modifier
						.fillMaxWidth()
						.clickable(!showEditor) {
							showEditor = true
						}
				) {
					Row(
						verticalAlignment = Alignment.CenterVertically,
						modifier = Modifier
							.fillMaxWidth()
							.padding(16.dp)
					) {
						Icon(
							imageVector = TablerIcons.Plus,
							contentDescription = null,
							tint = MaterialTheme.colorScheme.primary
						)
						Spacer(modifier = Modifier.width(16.dp))
						Text(
							text = "Add your thoughts",
							color = MaterialTheme.colorScheme.primary,
							style = MaterialTheme.typography.titleSmall,
						)
					}
				}
			}
		}
	}
}

@Composable
private fun Tags(
	tagList: MutableList<String>,
	onAddTag: (String) -> Unit
) {

	FlowRow(
		modifier = Modifier
			.fillMaxWidth()
			.padding(24.dp, 0.dp),
		mainAxisSpacing = 16.dp,
		crossAxisSpacing = 8.dp,
		mainAxisAlignment = MainAxisAlignment.Start
	) {
		for (i in 0 until 13) {
			Card(
				modifier = Modifier
					.clip(RoundedCornerShape(24.dp)),
				elevation = 0.dp,
				shape = RoundedCornerShape(24.dp),
				border = BorderStroke(1.dp, MaterialTheme.colorScheme.primaryContainer),
				backgroundColor = MaterialTheme.colorScheme.background
			) {
				Text(
					text = generatePrimaryKey(Random.nextInt(7) + 1),
					style = MaterialTheme.typography.titleSmall,
					color = MaterialTheme.colorScheme.primary,
					modifier = Modifier
						.padding(16.dp)
				)
			}
		}
	}
}

private class MovieDataMock : PreviewParameterProvider<MovieData> {
	override val values = sequenceOf(
		MovieData(
			id = "342470",
			title = "All the Bright Places",
			posterPath = "/4SafxuMKQiw4reBiWKVZJpJn80I.jpg",
			adult = false,
			backdropPath = "/tcrNJfyNEIqaBR8Ogkgnq5xQJnf.jpg",
			genre_ids = listOf("10749", "18"),
			originalLanguage = "en",
			originalTitle = "All the Bright Places",
			overview = "Two teens facing personal struggles form a powerful bond as they embark on a cathartic journey chronicling the wonders of Indiana.",
			popularity = 75.455,
			releaseDate = "2020-02-28",
			video = false,
			voteAverage = 7.7,
			voteCount = 2285
		)
	)
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun PowerByCard() {
	Card(
		modifier = Modifier
			.padding(24.dp, 0.dp),
		elevation = 0.dp,
		backgroundColor = MaterialTheme.colorScheme.background,
		shape = RoundedCornerShape(12.dp),
		onClick = {}
	) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier
				.padding(16.dp),
		) {
			Text(
				text = "Powered by",
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onBackground
			)
			Spacer(modifier = Modifier.width(16.dp))
			Image(
				painter = painterResource(id = R.drawable.il_tmdb),
				contentDescription = null,
				modifier = Modifier
					.height(12.dp)
			)
		}
	}
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class MovieCharacterData(
	@JsonProperty("id")
	val id: String,

	@JsonProperty("name")
	val name: String,

	@JsonProperty("character")
	val character: String
)
