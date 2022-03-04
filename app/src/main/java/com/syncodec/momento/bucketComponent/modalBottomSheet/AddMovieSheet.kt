package com.syncodec.momento.bucketComponent.modalBottomSheet

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.syncodec.momento.R
import com.syncodec.momento.custom.BottomSheetHeader
import com.syncodec.momento.custom.BottomSheetStrip
import com.syncodec.momento.custom.ClimateChangeMessage
import com.syncodec.momento.custom.LargeTextField
import com.syncodec.momento.konstant.Secret
import compose.icons.TablerIcons
import compose.icons.tablericons.Movie
import org.json.JSONObject
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalFoundationApi::class, androidx.compose.animation.ExperimentalAnimationApi::class)
@Composable
fun AddMovieSheet(
	onClick: (MovieData) -> Unit
) {
	val context = LocalContext.current

	val objectMapper = ObjectMapper().registerModule(KotlinModule())

	var movieNameText by rememberSaveable { mutableStateOf("") }
	var isMovieNameTextFocused by remember { mutableStateOf(false) }

	var sheetState by remember { mutableStateOf(SheetState.INIT) }

	val baseUrl = "https://api.themoviedb.org/3/search/movie?api_key=${Secret.TMDB_KEY}&language=en-US&query="
	val endUrl = "&page=1&include_adult=false"
	val requestQueue = Volley.newRequestQueue(context)
	var tag: String = "tag"

	val moviesData = remember { mutableStateListOf<MovieData>() }

	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		modifier = Modifier
			.fillMaxWidth()
			.heightIn(360.dp)
			.background(MaterialTheme.colorScheme.background),
	) {

		BottomSheetStrip()

		BottomSheetHeader(title = "What did you watch lately?", imageVector = TablerIcons.Movie)

		Spacer(modifier = Modifier.height(8.dp))

		LargeTextField(
			text = movieNameText,
			placeholder = "Search for movie",
			keyboardOptions = KeyboardOptions(
				capitalization = KeyboardCapitalization.None,
				autoCorrect = true,
				keyboardType = KeyboardType.Text,
				imeAction = ImeAction.Search
			),
			keyboardActions = KeyboardActions(
				onSearch = {
					sheetState = SheetState.SEARCHING

					requestQueue.cancelAll(tag)

					val requestUrl = "${baseUrl}${URLEncoder.encode(movieNameText, StandardCharsets.UTF_8.toString())}$endUrl"
					val stringRequest = StringRequest(
						Request.Method.GET,
						requestUrl,
						{ requestResult ->
							val jsonObject = JSONObject(requestResult)
							val docs = jsonObject.optJSONArray("results")
							val length = docs?.length() ?: 0
							moviesData.removeIf { true }
							for (i in 0 until length) {
								val movieData = objectMapper.readValue<MovieData>(docs!!.get(i).toString())
								moviesData.add(movieData)
							}
							sheetState = if (length > 0) {
								SheetState.RESULT_FOUND
							} else {
								SheetState.RESULT_NOT_FOUND
							}
						},
						{
							sheetState = SheetState.ERROR
							it.printStackTrace()
						}
					)

					tag = requestUrl
					stringRequest.tag = tag
					requestQueue.add(stringRequest)
				}
			),
			isFocused = isMovieNameTextFocused,
			onFocusChanged = { isMovieNameTextFocused = it },
			onValueChanged = { movieNameText = it }
		)

		Spacer(modifier = Modifier.height(8.dp))

		AnimatedContent(targetState = sheetState) {
			when (it) {
				SheetState.INIT -> ClimateChangeMessage()
				SheetState.SEARCHING -> {
					Box(
						contentAlignment = Alignment.Center,
						modifier = Modifier
							.fillMaxWidth()
							.height(256.dp),
					) {
						val lottieComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.lottie_loading))

						LottieAnimation(
							composition = lottieComposition,
							iterations = LottieConstants.IterateForever,
							modifier = Modifier
								.requiredSize(64.dp)
						)
					}
				}
				SheetState.RESULT_FOUND -> {
					LazyVerticalGrid(
						cells = GridCells.Adaptive(96.dp),
						modifier = Modifier
							.padding(8.dp)
					) {
						items(moviesData) { movieData ->
							MovieButton(
								movieData = movieData,
								modifier = Modifier
									.aspectRatio(0.75f),
							) { onClick(movieData)}
						}
					}
				}
				SheetState.RESULT_NOT_FOUND -> {
					Column(
						modifier = Modifier
							.height(256.dp)
					) {
						Image(
							painter = painterResource(id = R.drawable.il_result_unavailable_2),
							contentDescription = "No result found",
							modifier = Modifier
								.fillMaxWidth()
								.padding(16.dp)
						)

						Spacer(modifier = Modifier.height(16.dp))

						Text(
							text = "Sorry, we can't find that",
							style = MaterialTheme.typography.titleMedium,
							color = MaterialTheme.colorScheme.secondary,
							textAlign = TextAlign.Center,
							modifier = Modifier
								.fillMaxWidth()
						)
					}
				}
				SheetState.ERROR -> {
					Column(
						modifier = Modifier
							.height(256.dp)
					) {
						Image(
							painter = painterResource(id = R.drawable.il_result_unavailable_2),
							contentDescription = "Sorry, we cant find that now",
							modifier = Modifier
								.fillMaxWidth()
								.padding(16.dp)
						)

						Spacer(modifier = Modifier.height(16.dp))

						Text(
							text = "Sorry, we cant find that now",
							style = MaterialTheme.typography.titleMedium,
							color = MaterialTheme.colorScheme.secondary,
							textAlign = TextAlign.Center,
							modifier = Modifier
								.fillMaxWidth()
						)
					}
				}
			}
		}

		Spacer(modifier = Modifier.height(32.dp))
	}
}


@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun MovieButton(
	modifier: Modifier,
	movieData: MovieData,
	onClick: () -> Unit = {}
) {
	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		modifier = Modifier
			.padding(8.dp),
	) {
		Card(
			elevation = 0.dp,
			backgroundColor = Color.Companion.Transparent,
			shape = RoundedCornerShape(12.dp),
			modifier = modifier,
			onClick = { onClick() }
		) {

			if (movieData.posterPath != null) {
				Image(
					painter = rememberImagePainter(
						data = "https://image.tmdb.org/t/p/w500${movieData.posterPath}",
						builder = { crossfade(true) }
					),
					contentDescription = movieData.title,
					modifier = Modifier.fillMaxSize(),
					contentScale = ContentScale.Crop
				)
			}
		}

		val releaseDate = if ((movieData.releaseDate?.length ?: 0) > 4) {
			" (${movieData.releaseDate?.substring(0, 4)})"
		} else {
			""
		}

		Text(
			text = "${movieData.title}$releaseDate",
			style = MaterialTheme.typography.bodyMedium,
			modifier = Modifier
				.padding(0.dp, 4.dp, 0.dp, 0.dp)
		)
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

@JsonIgnoreProperties(ignoreUnknown = true)
data class MovieData(
	@JsonProperty("id")
	val id: String,

	@JsonProperty("title")
	val title: String,

	@JsonProperty("poster_path")
	val posterPath: String?,

	@JsonProperty("original_language")
	val originalLanguage: String,

	@JsonProperty("original_title")
	val originalTitle: String,

	@JsonProperty("overview")
	val overview: String,

	@JsonProperty("release_date")
	val releaseDate: String?,

	@JsonProperty("genre_ids")
	val genre_ids: List<String>,

	@JsonProperty("adult")
	val adult: Boolean,

	@JsonProperty("backdrop_path")
	val backdropPath: String?,

	@JsonProperty("popularity")
	val popularity: Double,

	@JsonProperty("video")
	val video: Boolean,

	@JsonProperty("vote_average")
	val voteAverage: Double,

	@JsonProperty("vote_count")
	val voteCount: Int,

	@JsonProperty("character_data_list")
	var characterDataList: MutableList<MovieCharacterData> = mutableListOf()
) {
	companion object {
		val mock = MovieData(
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
	}
}
