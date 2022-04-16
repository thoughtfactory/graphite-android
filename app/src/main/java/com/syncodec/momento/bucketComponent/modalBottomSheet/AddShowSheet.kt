package com.syncodec.momento.bucketComponent.modalBottomSheet

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.syncodec.momento.R
import com.syncodec.momento.bucketComponent.BucketActivity
import com.syncodec.momento.bucketComponent.miscellaneous.ShowSearchLargeTextField
import com.syncodec.momento.custom.ClimateChangeMessage
import com.syncodec.momento.custom.bottomSheet.BottomSheetHeader
import com.syncodec.momento.custom.bottomSheet.BottomSheetStrip
import com.syncodec.momento.konstant.Secret
import com.syncodec.momento.miscellaneous.ThemeUtils.Companion.tone
import org.json.JSONObject
import java.io.Serializable
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalFoundationApi::class, ExperimentalAnimationApi::class)
@Composable
fun AddShowSheet(
	dataType: BucketActivity.DataType,
	onAction: (BucketActivity.Action, Any?) -> Unit
) {
	val context = LocalContext.current

	var showNameText by rememberSaveable { mutableStateOf("") }
	var isShowNameTextFocused by remember { mutableStateOf(false) }

	var sheetState by remember { mutableStateOf(SheetState.INIT) }

	val tvBaseUrl = "https://api.themoviedb.org/3/search/tv?api_key=${Secret.TMDB_KEY}&language=en-US&query="
	val movieBaseUrl = "https://api.themoviedb.org/3/search/movie?api_key=${Secret.TMDB_KEY}&language=en-US&query="
	val endUrl = "&page=1"
	val requestQueue = Volley.newRequestQueue(context)

	var tvTag = "tvTag"
	var movieTag = "movieTag"

	val showDataList = remember { mutableStateListOf<ShowData>() }

	Surface(
		shape = RoundedCornerShape(12.dp, 12.dp, 0.dp, 0.dp),
		color= MaterialTheme.colorScheme.surface,
		modifier = Modifier.heightIn(360.dp),
	) {
		Column(
			horizontalAlignment = Alignment.CenterHorizontally,
			modifier = Modifier
		) {
			BottomSheetStrip()

			BottomSheetHeader(
				title = "What did you watch lately?",
				icon = R.drawable.ic_show
			)

			Spacer(modifier = Modifier.height(8.dp))

			ShowSearchLargeTextField(
				text = showNameText,
				placeholder = "Search for movie or show",
				dataType = dataType,
				keyboardOptions = KeyboardOptions(
					capitalization = KeyboardCapitalization.None,
					autoCorrect = true,
					keyboardType = KeyboardType.Text,
					imeAction = ImeAction.Search
				),
				keyboardActions = KeyboardActions(
					onSearch = {
						sheetState = SheetState.SEARCHING

						requestQueue.cancelAll(tvTag)
						requestQueue.cancelAll(movieTag)

						StringRequest(
							Request.Method.GET,
							"${if (dataType == BucketActivity.DataType.TV) tvBaseUrl else movieBaseUrl}${
								URLEncoder.encode(
									showNameText,
									StandardCharsets.UTF_8.toString()
								)
							}$endUrl",
							{ requestResult ->
								val jsonObject = JSONObject(requestResult)
								val docs = jsonObject.optJSONArray("results")
								val length = docs?.length() ?: 0
								showDataList.removeIf { true }
								for (i in 0 until length) {
									val showDataJson = docs!!.getJSONObject(i)
									showDataJson.apply {
										ShowData(
											id = getString("id"),
											showType = if (dataType == BucketActivity.DataType.TV) ShowType.TV else ShowType.MOVIE,
											title = if (dataType == BucketActivity.DataType.TV) getString("name") else getString("title"),
											posterPath = getString("poster_path"),
											releaseDate = if (dataType == BucketActivity.DataType.TV) optString("first_air_date") else optString("release_date")
										).apply { showDataList.add(this) }
									}
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
						).apply {
							tvTag = this.url
							movieTag = this.url

							requestQueue.add(this)
						}

					}
				),
				isFocused = isShowNameTextFocused,
				onFocusChanged = { isShowNameTextFocused = it },
				onValueChanged = { showNameText = it }
			) { onAction(BucketActivity.Action.DATA_TYPE_SELECT, null) }

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
							columns = GridCells.Adaptive(96.dp),
							modifier = Modifier
								.padding(8.dp)
						) {
							items(showDataList) { showData ->
								ShowCard(
									showData = showData,
									modifier = Modifier
										.aspectRatio(0.75f),
								) { onAction(BucketActivity.Action.ADD_SHOW, showData) }
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
}


@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun ShowCard(
	modifier: Modifier,
	showData: ShowData,
	onClick: () -> Unit = {}
) {
	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		modifier = Modifier.padding(8.dp),
	) {
		Card(
			containerColor = MaterialTheme.colorScheme.surface.tone(isSystemInDarkTheme(), 1),
			elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
			shape = RoundedCornerShape(12.dp),
			modifier = modifier,
			onClick = { onClick() }
		) {
			if (showData.posterPath != null) {
				Image(
					painter = rememberImagePainter(
						data = "https://image.tmdb.org/t/p/w500${showData.posterPath}",
						builder = { crossfade(true) }
					),
					contentDescription = showData.title,
					modifier = Modifier.fillMaxSize(),
					contentScale = ContentScale.Crop
				)
			}
		}

		val releaseDate = if ((showData.releaseDate?.length ?: 0) > 4) {
			" (${showData.releaseDate?.substring(0, 4)})"
		} else {
			""
		}

		Text(
			text = "${showData.title}$releaseDate",
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onBackground,
			modifier = Modifier
				.padding(0.dp, 4.dp, 0.dp, 0.dp)
		)
	}
}

enum class ShowType {
	MOVIE,
	TV
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class ShowData(
	@JsonProperty("id")
	val id: String,

	@JsonProperty("show_type")
	val showType: ShowType,

	@JsonProperty("title")
	val title: String,

	@JsonProperty("poster_path")
	val posterPath: String?,

	@JsonProperty("release_date")
	val releaseDate: String?,
) : Serializable {
	companion object {
		val mock = ShowData(
			id = "342470",
			showType = ShowType.MOVIE,
			title = "All the Bright Places",
			posterPath = "/4SafxuMKQiw4reBiWKVZJpJn80I.jpg",
			releaseDate = "2020-02-28",
		)
	}
}
