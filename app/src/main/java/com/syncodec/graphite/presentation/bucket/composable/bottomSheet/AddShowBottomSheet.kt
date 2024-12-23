package com.syncodec.graphite.presentation.bucket.composable.bottomSheet

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.with
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.BucketType
import com.syncodec.graphite.di.network.ApiStatus
import com.syncodec.graphite.di.network.MovieData
import com.syncodec.graphite.di.network.ShowType
import com.syncodec.graphite.di.network.TMDbMovieSearchResult
import com.syncodec.graphite.di.network.TMDbTvSearchResult
import com.syncodec.graphite.di.network.TvData
import com.syncodec.graphite.presentation.bucket.composable.buildingBlock.SearchResultStatusView
import com.syncodec.graphite.presentation.bucketItem.BucketItemActivity
import com.syncodec.graphite.presentation.common.LoadingView
import com.syncodec.graphite.presentation.common.bottomSheet.BottomSheetTextField
import com.syncodec.graphite.presentation.common.bottomSheet.BottomSheetTextFieldDefaults
import com.syncodec.graphite.presentation.common.bottomSheet.GenericBottomSheet
import com.syncodec.graphite.presentation.common.button.MenuButton
import com.syncodec.graphite.presentation.common.button.MenuButtonDefaults
import com.syncodec.graphite.presentation.common.button.stateButton.StateButton
import com.syncodec.graphite.presentation.common.button.stateButton.StateData
import com.syncodec.graphite.utils.Extra
import com.syncodec.graphite.utils.Status
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel


@OptIn(ExperimentalComposeUiApi::class, ExperimentalAnimationApi::class)
@Preview
@Composable
fun AddShowBottomSheet() {
	val context = LocalContext.current
	val scope = rememberCoroutineScope()
	val viewModel : BucketBottomSheetViewModel = koinViewModel()

	val bucketObject by viewModel.bucketObject.collectAsState()

	val keyboardController = LocalSoftwareKeyboardController.current

	var queryText by rememberSaveable { mutableStateOf("") }
	var isTextFocused by remember { mutableStateOf(false) }

	var status : Status by remember { mutableStateOf(Status.INIT) }
	var tmDbMovieSearchResult : TMDbMovieSearchResult? by remember { mutableStateOf(null) }
	var tmDbTvSearchResult : TMDbTvSearchResult? by remember { mutableStateOf(null) }

	var currentState by rememberSaveable { mutableStateOf(0) }

	val onSearch = {
		status = Status.LOADING
		keyboardController?.hide()
		tmDbMovieSearchResult = null
		tmDbTvSearchResult = null
		when (currentState) {
			0 -> viewModel.searchForMovie(queryText) { apiResult ->
				scope.launch(Dispatchers.Main) {
					when (apiResult.status) {
						ApiStatus.LOADING -> {
							status = Status.LOADING
						}

						ApiStatus.SUCCESS -> {
							status = Status.LOADED
							tmDbMovieSearchResult = apiResult.data
						}

						ApiStatus.ERROR -> {
							status = Status.ERROR
							tmDbMovieSearchResult = null
						}
					}
				}
			}

			1 -> viewModel.searchForTvShow(queryText) { apiResult ->
				scope.launch(Dispatchers.Main) {
					when (apiResult.status) {
						ApiStatus.LOADING -> {
							status = Status.LOADING
						}

						ApiStatus.SUCCESS -> {
							status = Status.LOADED
							tmDbTvSearchResult = apiResult.data
						}

						ApiStatus.ERROR -> {
							status = Status.ERROR
							tmDbTvSearchResult = null
						}
					}
				}
			}

			else -> null
		}
	}

	GenericBottomSheet(
		title = "What did you watch",
		icon = R.drawable.ic_show,
	) {

		BottomSheetTextField(
			value = queryText,
			placeholder = if (currentState == 0) "Search for a movie" else "Search for a tv show",
			actionButtons = {
				MenuButton(
					icon = R.drawable.ic_search,
					colors = MenuButtonDefaults.menuButtonColorsOnSurface()
				) { onSearch() }
			},
			keyboardOptions = KeyboardOptions.Default.copy(
				capitalization = KeyboardCapitalization.None,
				keyboardType = KeyboardType.Text,
				imeAction = ImeAction.Search
			),
			keyboardActions = KeyboardActions(
				onSearch = { onSearch() },
				onDone = { onSearch() }
			),
			colors = BottomSheetTextFieldDefaults.textFieldColors(),
			onValueChange = { queryText = it },
		)

		Spacer(modifier = Modifier.height(4.dp))

		StateButton(
			stateList = listOf(
				StateData(
					title = "Movie",
					icon = R.drawable.ic_movie,
					stateTint = MaterialTheme.colorScheme.primary
				),
				StateData(
					title = "Tv Show",
					icon = R.drawable.ic_tv,
					stateTint = MaterialTheme.colorScheme.primary
				)
			),
			currentState = currentState,
			modifier = Modifier
				.fillMaxWidth()
				.height(36.dp)
		) { currentState = it }

		Spacer(modifier = Modifier.height(4.dp))

		AnimatedContent(
			targetState = status,
			transitionSpec = { scaleIn(tween(300)) + fadeIn(tween(300)) with scaleOut(tween(300)) + fadeOut(tween(300)) }
		) {
			when (it) {
				Status.INIT -> {
					SearchResultStatusView(
						imageId = R.drawable.il_bucket_show_search,
						text = "It's a bird, it's a plane, it's a Superman!",
						contentDescription = "Search for a show",
					)
				}

				Status.LOADING -> {
					Box(
						contentAlignment = Alignment.Center,
						modifier = Modifier
							.fillMaxWidth()
							.height(256.dp),
					) { LoadingView() }
				}

				Status.LOADED -> LoadedView(
					tmDbMovieSearchResult = tmDbMovieSearchResult,
					tmDbTvSearchResult = tmDbTvSearchResult,
					currentState = currentState,
					onClickMovie = { movieData ->
						keyboardController?.hide()

						if (bucketObject == null || movieData.id == null) {
							Toast.makeText(context, "Error adding movie to bucket", Toast.LENGTH_SHORT).show()
						} else {
							Intent(context, BucketItemActivity::class.java).apply {
								putExtra(Extra.Companion.Extra.IsNew.name, true)
								putExtra(Extra.Companion.Extra.BUCKET_ID.name, bucketObject?.id?.bytes)
								putExtra(Extra.Companion.Extra.BUCKET_TYPE.name, BucketType.SHOW.name)
								putExtra(Extra.Companion.Extra.SHOW_TYPE.name, ShowType.MOVIE.name)
								putExtra(Extra.Companion.Extra.MOVIE_ID.name, movieData.id)

								context.startActivity(this)
							}
						}
					},
					onClickTv = { tvData ->
						keyboardController?.hide()

						if (bucketObject == null || tvData.id == null) {
							Toast.makeText(context, "Error adding movie to bucket", Toast.LENGTH_SHORT).show()
						} else {
							Intent(context, BucketItemActivity::class.java).apply {
								putExtra(Extra.Companion.Extra.IsNew.name, true)
								putExtra(Extra.Companion.Extra.BUCKET_ID.name, bucketObject?.id?.bytes)
								putExtra(Extra.Companion.Extra.BUCKET_TYPE.name, BucketType.SHOW.name)
								putExtra(Extra.Companion.Extra.SHOW_TYPE.name, ShowType.TV.name)
								putExtra(Extra.Companion.Extra.TV_ID.name, tvData.id)

								context.startActivity(this)
							}
						}

					}
				)

				Status.ERROR -> {
					SearchResultStatusView(
						imageId = R.drawable.il_bucket_search_error,
						text = "Oops, something went wrong. Try again?",
						contentDescription = "Error getting search results"
					)
				}
			}
		}
	}
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun LoadedView(
	tmDbMovieSearchResult : TMDbMovieSearchResult? = null,
	tmDbTvSearchResult : TMDbTvSearchResult? = null,
	currentState : Int = 0,
	onClickMovie : (MovieData) -> Unit = {},
	onClickTv : (TvData) -> Unit = {},
) {
	if (tmDbMovieSearchResult?.results?.isEmpty() == true || tmDbTvSearchResult?.results?.isEmpty() == true) {
		SearchResultStatusView(
			imageId = R.drawable.il_bucket_search_not_found,
			text = "Uh oh, we couldn't find anything. Try again?",
			contentDescription = "Show not found"
		)
	} else {
		AnimatedContent(
			targetState = currentState,
			transitionSpec = { scaleIn(tween(300)) + fadeIn(tween(300)) with scaleOut(tween(300)) + fadeOut(tween(300)) }
		) {
			if (it == 0) {
				LazyVerticalGrid(
					columns = GridCells.Fixed(3),
				) {
					tmDbMovieSearchResult?.results?.forEach { movieData ->
						movieData?.let {
							item {
								ShowCard(
									title = it.title,
									posterPath = it.posterPath,
									releaseDate = it.releaseDate,
								) { onClickMovie(it) }
							}
						}
					}
					item { Spacer(modifier = Modifier.height(32.dp)) }
					item { Spacer(modifier = Modifier.height(32.dp)) }
					item { Spacer(modifier = Modifier.height(32.dp)) }
				}
			} else {
				LazyVerticalGrid(
					columns = GridCells.Fixed(3),
				) {
					tmDbTvSearchResult?.results?.forEach { tvData ->
						tvData?.let {
							item {
								ShowCard(
									title = it.name,
									posterPath = it.posterPath,
									releaseDate = it.firstAirDate,
								) { onClickTv(it) }
							}
						}
					}
					item { Spacer(modifier = Modifier.height(32.dp)) }
					item { Spacer(modifier = Modifier.height(32.dp)) }
					item { Spacer(modifier = Modifier.height(32.dp)) }
				}
			}
		}
	}
}

@Composable
private fun ShowCard(
	title : String?,
	posterPath : String?,
	releaseDate : String?,
	onClick : () -> Unit
) {
	val context = LocalContext.current

	Column(
		horizontalAlignment = Alignment.Start,
		modifier = Modifier.padding(8.dp),
	) {
		Box(
			contentAlignment = Alignment.Center,
			modifier = Modifier
				.aspectRatio(0.6666f)
				.background(MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp).copy(alpha = 0.71f), MaterialTheme.shapes.medium)
				.clip(MaterialTheme.shapes.medium)
				.clickable { onClick() }
		) {
			var isError by remember { mutableStateOf(false) }
			posterPath?.let {
				AsyncImage(
					model = ImageRequest.Builder(context)
						.data(if (it.isBlank()) "" else "https://image.tmdb.org/t/p/w500${it}")
						.crossfade(300)
						.build(),
					placeholder = null,
					onError = { isError = true },
					contentDescription = title,
					contentScale = ContentScale.Crop,
					modifier = Modifier
						.aspectRatio(0.6666f)
						.background(MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp).copy(alpha = 0.71f), MaterialTheme.shapes.medium)
						.clip(MaterialTheme.shapes.medium)
						.clickable { onClick() },
				)
			} ?: Text(
				text = "No cover",
				modifier = Modifier.padding(8.dp),
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.71f)
			)
			if (isError) {
				Text(
					text = "No cover",
					modifier = Modifier.padding(8.dp),
					style = MaterialTheme.typography.bodySmall,
					color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.71f)
				)
			}
		}

		Spacer(modifier = Modifier.height(4.dp))

		Text(
			text = "$title ${if ((releaseDate?.length ?: 0) > 4) "(${releaseDate?.substring(0, 4)})" else ""}",
			style = MaterialTheme.typography.bodySmall,
			color = MaterialTheme.colorScheme.onBackground,
			modifier = Modifier
		)
	}
}
