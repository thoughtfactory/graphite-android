package com.syncodec.graphite.presentation.bucket.composable.bottomSheet

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.syncodec.graphite.R
import com.syncodec.graphite.di.Repository
import com.syncodec.graphite.di.model.BucketType
import com.syncodec.graphite.di.network.TMDbMovieSearchResult
import com.syncodec.graphite.di.network.TMDbTvSearchResult
import com.syncodec.graphite.presentation.bucket.BucketActivity
import com.syncodec.graphite.presentation.bucket.BucketViewModel
import com.syncodec.graphite.presentation.bucketItem.BucketItemActivity
import com.syncodec.graphite.presentation.common.ClimateChangeMessage
import com.syncodec.graphite.presentation.common.text.LargeTextField
import com.syncodec.graphite.presentation.common.LoadingView
import com.syncodec.graphite.presentation.common.bottomSheet.BottomSheetHeader
import com.syncodec.graphite.presentation.common.bottomSheet.BottomSheetStrip
import com.syncodec.graphite.presentation.common.button.stateButton.StateButton
import com.syncodec.graphite.presentation.common.button.stateButton.StateData
import com.syncodec.graphite.utils.Extra
import com.syncodec.graphite.utils.Status
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.SocketTimeoutException


@OptIn(ExperimentalComposeUiApi::class, ExperimentalAnimationApi::class)
@Composable
fun AddShowBottomSheet(
	closeSheet : () -> Unit
) {
	val activity : BucketActivity = LocalContext.current as BucketActivity
	val viewModel : BucketViewModel = viewModel()
	val scope = rememberCoroutineScope()
	val keyboardController = LocalSoftwareKeyboardController.current

	var queryText by rememberSaveable { mutableStateOf("") }
	var isTextFocused by rememberSaveable { mutableStateOf(false) }
	val focusRequester = remember { FocusRequester() }

	var status : Status by remember { mutableStateOf(Status.INIT) }
	var tmDbMovieSearchResult : TMDbMovieSearchResult? by remember { mutableStateOf(null) }
	var tmDbTvSearchResult : TMDbTvSearchResult? by remember { mutableStateOf(null) }

	var currentState by rememberSaveable { mutableStateOf(0) }

	val onSearch = {
		status = Status.LOADING
		focusRequester.freeFocus()
		keyboardController?.hide()
		tmDbMovieSearchResult = null
		scope.launch(Dispatchers.IO) {
			try {
				if (currentState == 0) {
					Repository
						.tmDbApi
						.searchForMovieTitle(queryText) {
							if (it == null) {
								status = Status.ERROR
							} else {
								tmDbMovieSearchResult = it
								tmDbTvSearchResult = null
								status = Status.LOADED
							}
						}
				} else {
					Repository
						.tmDbApi
						.searchForTvTitle(queryText) {
							if (it == null) {
								status = Status.ERROR
							} else {
								tmDbMovieSearchResult = null
								tmDbTvSearchResult = it
								status = Status.LOADED
							}
						}
				}
			} catch (e : SocketTimeoutException) {
//				TODO update error message and image
				scope.launch(Dispatchers.Main) { Toast.makeText(activity, "Timeout getting search results", Toast.LENGTH_SHORT).show() }
				status = Status.ERROR
				e.printStackTrace()
			} catch (e : Exception) {
//				TODO update error message and image
				status = Status.ERROR
				e.printStackTrace()
			}
		}
	}

	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		modifier = Modifier
			.fillMaxWidth()
			.background(MaterialTheme.colorScheme.surface)
	) {

		BottomSheetStrip()

		BottomSheetHeader(
			title = "What did you watch",
			icon = R.drawable.ic_show,
		)

		Spacer(modifier = Modifier.height(8.dp))

		LargeTextField(
			text = queryText,
			placeholder = if (currentState == 0) "Search for movie" else "Search for tv show",
			keyboardOptions = KeyboardOptions.Default.copy(
				capitalization = KeyboardCapitalization.None,
				autoCorrect = true,
				keyboardType = KeyboardType.Text,
				imeAction = ImeAction.Search
			),
			isFocused = isTextFocused,
			focusRequester = focusRequester,
			onFocusChanged = { isTextFocused = it },
			onValueChanged = { queryText = it },
			keyboardActions = KeyboardActions(
				onSearch = { onSearch() },
				onDone = { onSearch() }
			),
			modifier = Modifier.padding(24.dp, 0.dp)
		)

		Spacer(modifier = Modifier.height(8.dp))

		StateButton(
			stateList = listOf(
				StateData(
					title = "Movie",
					icon = R.drawable.ic_show,
					stateTint = MaterialTheme.colorScheme.primary
				),
				StateData(
					title = "Tv Show",
					icon = R.drawable.ic_tv,
					stateTint = MaterialTheme.colorScheme.primary
				)
			),
			containerColor = MaterialTheme.colorScheme.background,
			currentState = currentState,
			modifier = Modifier
				.fillMaxWidth()
				.height(36.dp)
				.padding(24.dp, 0.dp)
		) { currentState = it }

		Spacer(modifier = Modifier.height(16.dp))

		AnimatedContent(targetState = status) {
			when (it) {
				Status.INIT -> ClimateChangeMessage()
				Status.LOADING -> {
					Box(
						contentAlignment = Alignment.Center,
						modifier = Modifier
							.fillMaxWidth()
							.height(256.dp),
					) { LoadingView() }
				}

				Status.LOADED -> {
//						TODO    What if list is empty?
					LazyVerticalGrid(
						columns = GridCells.Fixed(3),
						contentPadding = PaddingValues(20.dp, 0.dp),
					) {
						if (currentState == 0) {
							tmDbMovieSearchResult?.results?.forEach { movieDataResult ->
								if (movieDataResult != null) {
									item {
										ShowCard(
											title = movieDataResult.title,
											posterPath = movieDataResult.posterPath,
											releaseDate = movieDataResult.releaseDate,
										) {
											focusRequester.freeFocus()
											keyboardController?.hide()

											if (viewModel.bucketObject.value == null || movieDataResult.id == null) {
												Toast.makeText(activity, "Error adding movie to bucket", Toast.LENGTH_SHORT).show()
											} else {
												Intent(activity, BucketItemActivity::class.java).apply {
													putExtra(Extra.Companion.Constant.IS_NEW.name, true)
													putExtra(Extra.Companion.Constant.BUCKET_ID.name, viewModel.bucketObject.value !!.id.toString())
													putExtra(Extra.Companion.Constant.BUCKET_TYPE.name, BucketType.SHOW.name)
													putExtra(Extra.Companion.Constant.MOVIE_ID.name, movieDataResult.id)

													activity.startActivity(this)
												}
											}
										}
									}
								}
							}
							item { Spacer(modifier = Modifier.height(32.dp)) }
							item { Spacer(modifier = Modifier.height(32.dp)) }
							item { Spacer(modifier = Modifier.height(32.dp)) }
						} else if (currentState == 1) {
							tmDbTvSearchResult?.results?.forEach { tvDataResult ->
								if (tvDataResult != null) {
									item {
										ShowCard(
											title = tvDataResult.name,
											posterPath = tvDataResult.posterPath,
											releaseDate = tvDataResult.firstAirDate,
										) {
											focusRequester.freeFocus()
											keyboardController?.hide()

											if (viewModel.bucketObject.value == null || tvDataResult.id == null) {
												Toast.makeText(activity, "Error adding movie to bucket", Toast.LENGTH_SHORT).show()
											} else {
												Intent(activity, BucketItemActivity::class.java).apply {
													putExtra(Extra.Companion.Constant.IS_NEW.name, true)
													putExtra(Extra.Companion.Constant.BUCKET_ID.name, viewModel.bucketObject.value !!.id.toString())
													putExtra(Extra.Companion.Constant.BUCKET_TYPE.name, BucketType.SHOW.name)
													putExtra(Extra.Companion.Constant.TV_ID.name, tvDataResult.id)

													activity.startActivity(this)
												}
											}
										}
									}
								}
							}
							item { Spacer(modifier = Modifier.height(32.dp)) }
							item { Spacer(modifier = Modifier.height(32.dp)) }
							item { Spacer(modifier = Modifier.height(32.dp)) }
						}
					}
				}

				Status.ERROR -> {
					Column(
						modifier = Modifier.heightIn(256.dp),
						horizontalAlignment = Alignment.CenterHorizontally
					) {
						Spacer(modifier = Modifier.height(24.dp))
						Image(
							painter = painterResource(id = R.drawable.il_error),
							contentDescription = "No result found",
							modifier = Modifier.fillMaxWidth(0.71f)
						)

						Spacer(modifier = Modifier.height(16.dp))

						Text(
							text = "Sorry, we could not find that",
							style = MaterialTheme.typography.bodyLarge,
							color = MaterialTheme.colorScheme.onSurface,
							textAlign = TextAlign.Center,
							modifier = Modifier.fillMaxWidth()
						)
					}
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
		horizontalAlignment = Alignment.CenterHorizontally,
		modifier = Modifier.padding(4.dp),
	) {
		AsyncImage(
			model = ImageRequest.Builder(context)
				.data(if (posterPath.isNullOrBlank()) "" else "https://image.tmdb.org/t/p/w500${posterPath}")
				.crossfade(300)
				.build(),
			placeholder = null,
			contentDescription = title,
			contentScale = ContentScale.Crop,
			modifier = Modifier
				.aspectRatio(0.6666f)
				.background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
				.clip(RoundedCornerShape(12.dp))
				.clickable { onClick() },
		)

		Text(
			text = "${title} ${if ((releaseDate?.length ?: 0) > 4) "(${releaseDate?.substring(0, 4)})" else ""}",
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onBackground,
			modifier = Modifier.padding(0.dp, 4.dp, 0.dp, 0.dp)
		)
	}
}
