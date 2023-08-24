package com.syncodec.graphite.presentation.bucket.composable.bottomSheet

import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.BucketType
import com.syncodec.graphite.di.network.TMDBResponse
import com.syncodec.graphite.di.network.TMDBSearchResult
import com.syncodec.graphite.di.network.TMDbApi
import com.syncodec.graphite.presentation.bucketItem.activity.ShowBucketItemActivity
import com.syncodec.graphite.presentation.common.LoadingView
import com.syncodec.graphite.presentation.common.bottomSheet.genericBottomSheet2.GenericBottomSheet2
import com.syncodec.graphite.presentation.common.bottomSheet.genericBottomSheet2.GenericBottomSheetSkeleton2
import com.syncodec.graphite.presentation.common.button.ClearButton
import com.syncodec.graphite.presentation.common.button.SearchButton
import com.syncodec.graphite.presentation.common.info.InfoCard
import com.syncodec.graphite.presentation.common.info.InfoCardDefaults
import com.syncodec.graphite.presentation.common.tab.GenericTabRow
import com.syncodec.graphite.presentation.common.tab.TabItem
import com.syncodec.graphite.utils.ContentStatus
import com.syncodec.graphite.utils.Extra
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Preview
@Composable
fun AddShowBottomSheet(
	bottomSheetState: SheetState = rememberModalBottomSheetState(),
	isBottomSheetVisible: Boolean = false,
	onDismissRequest: () -> Unit = { },
	parentId: RealmUUID? = null,
) {
	val scope = rememberCoroutineScope()

	val keyboardController = LocalSoftwareKeyboardController.current

	var queryText by rememberSaveable { mutableStateOf("") }

	var showSearchType by rememberSaveable { mutableIntStateOf(0) }

	var contentStatus by remember { mutableStateOf<ContentStatus<TMDBSearchResult>>(ContentStatus.Init) }
	fun searchForShow(title: String) {
		scope.launch(Dispatchers.IO) {
			when (showSearchType) {
				0 -> TMDbApi.searchForMovieTitle(title = title) { tmdbResponse ->
					contentStatus = when (tmdbResponse) {
						is TMDBResponse.Loading -> ContentStatus.Loading
						is TMDBResponse.Success -> ContentStatus.Loaded(tmdbResponse.data)
						is TMDBResponse.Error -> ContentStatus.Error(tmdbResponse.message)
					}
				}

				1 -> TMDbApi.searchForTvTitle(title = title) { tmdbResponse ->
					contentStatus = when (tmdbResponse) {
						is TMDBResponse.Loading -> ContentStatus.Loading
						is TMDBResponse.Success -> ContentStatus.Loaded(tmdbResponse.data)
						is TMDBResponse.Error -> ContentStatus.Error(tmdbResponse.message)
					}
				}
			}
		}
	}

	GenericBottomSheet2(
		bottomSheetState = bottomSheetState,
		isBottomSheetVisible = isBottomSheetVisible,
		onDismissRequest = onDismissRequest,
	) {
		GenericBottomSheetSkeleton2(
			title = stringResource(id = R.string.add_show),
		) {
			OutlinedTextField(
				value = queryText,
				shape = MaterialTheme.shapes.medium,
				onValueChange = { queryText = it },
				label = { Text(text = stringResource(id = R.string.title)) },
				placeholder = { Text(text = stringResource(id = if (showSearchType == 0) R.string.search_for_movie else R.string.search_for_tv_show)) },
				trailingIcon = {
					Row {
						ClearButton { queryText = "" }
						SearchButton { searchForShow(queryText) }
						Spacer(modifier = Modifier.width(4.dp))
					}
				},
				maxLines = 1,
				singleLine = true,
				keyboardOptions = KeyboardOptions(
					capitalization = KeyboardCapitalization.None,
					autoCorrect = true,
					keyboardType = KeyboardType.Text,
					imeAction = ImeAction.Search,
				),
				keyboardActions = KeyboardActions {
					searchForShow(queryText)
					keyboardController?.hide()
				},
				modifier = Modifier.fillMaxWidth()
			)

			Spacer(modifier = Modifier.height(8.dp))
			GenericTabRow(
				tabItemList = listOf(
					TabItem(text = stringResource(id = R.string.movie), icon = R.drawable.ic_fa_bucket_show) { showSearchType = 0 },
					TabItem(text = stringResource(id = R.string.tv_show), icon = R.drawable.ic_fa_bucket_show) { showSearchType = 1 },
				),
				selectedTabIndex = showSearchType,
				modifier = Modifier.fillMaxWidth()
			)
			Spacer(modifier = Modifier.height(4.dp))

			AnimatedContent(
				targetState = contentStatus,
				label = "showSearchPreview_animation",
				modifier = Modifier.fillMaxWidth()
			) { contentStatus1 ->
				Column {
					Spacer(modifier = Modifier.height(8.dp))
					when (contentStatus1) {
						is ContentStatus.Init -> Unit
						is ContentStatus.Loading -> LoadingView(
							modifier = Modifier
								.fillMaxWidth()
								.padding(vertical = 12.dp)
						)

						is ContentStatus.LoadedEmpty -> Unit
						is ContentStatus.Loaded -> ShowGrid(
							tmdbSearchResult = contentStatus1.data,
							parentId = parentId
						)

						is ContentStatus.Error -> InfoCard(
							title = stringResource(id = R.string.link_preview_error_title),
							description = stringResource(id = R.string.link_preview_error_description),
							icon = R.drawable.ic_fa_warning,
							colors = InfoCardDefaults.errorCardColors()
						)
					}
					Spacer(modifier = Modifier.height(4.dp))
				}
			}

		}
	}
}


@Preview
@Composable
private fun ShowGrid(
	tmdbSearchResult: TMDBSearchResult = TMDBSearchResult.TMDbTvSearchResult(),
	parentId: RealmUUID? = null,
) {
	val context = LocalContext.current

	LazyVerticalGrid(
		columns = GridCells.Fixed(3),
		modifier = Modifier
	) {
		when (tmdbSearchResult) {
			is TMDBSearchResult.TMDbTvSearchResult -> {
				tmdbSearchResult.results?.filterNotNull()?.let { tvDataList ->
					items(tvDataList) { tvData ->
						ShowCard(
							title = tvData.name,
							posterPath = tvData.posterPath,
							releaseDate = tvData.firstAirDate,
							onClick = {
								Intent(context, ShowBucketItemActivity::class.java).apply {
									putExtra(Extra.Companion.Extra.IsNew.name, true)
									putExtra(Extra.Companion.Extra.BUCKET_ID.name, parentId?.bytes)
									putExtra(Extra.Companion.Extra.BUCKET_TYPE.name, BucketType.SHOW.name)
									putExtra(Extra.Companion.Extra.TV_ID.name, tvData.id)

									context.startActivity(this)
								}
							},
						)
					}
				}
			}

			is TMDBSearchResult.TMDbMovieSearchResult -> {
				tmdbSearchResult.results?.filterNotNull()?.let { movieDataList ->
					items(movieDataList) { movieData ->
						ShowCard(
							title = movieData.title,
							posterPath = movieData.posterPath,
							releaseDate = movieData.releaseDate,
							onClick = {
								Intent(context, ShowBucketItemActivity::class.java).apply {
									putExtra(Extra.Companion.Extra.IsNew.name, true)
									putExtra(Extra.Companion.Extra.BUCKET_ID.name, parentId?.bytes)
									putExtra(Extra.Companion.Extra.BUCKET_TYPE.name, BucketType.SHOW.name)
									putExtra(Extra.Companion.Extra.MOVIE_ID.name, movieData.id)

									context.startActivity(this)
								}
							},
						)
					}
				}
			}
		}
	}
}

@Composable
private fun ShowCard(
	title: String? = null,
	posterPath: String? = null,
	releaseDate: String? = null,
	onClick: () -> Unit = {}
) {
	val context = LocalContext.current

	var thumbnail by remember { mutableStateOf<Bitmap?>(null) }
	LaunchedEffect(key1 = posterPath) {
		withContext(Dispatchers.IO) {
			thumbnail = null
			thumbnail = TMDbApi.retrieveShowPoster(posterPath = posterPath)
		}
	}

	Column(
		horizontalAlignment = Alignment.Start,
		modifier = Modifier.padding(8.dp),
	) {
		SubcomposeAsyncImage(
			model = ImageRequest.Builder(context)
				.data(thumbnail)
				.crossfade(470)
				.build(),
			loading = { CircularProgressIndicator(modifier = Modifier.requiredSize(32.dp)) },
			error = {
				Text(
					text = "No image found",
					modifier = Modifier.padding(8.dp),
					style = MaterialTheme.typography.bodySmall,
					color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.71f)
				)
			},
			contentDescription = title,
			contentScale = ContentScale.Crop,
			modifier = Modifier
				.fillMaxWidth()
				.aspectRatio(0.6666f)
				.background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.large)
				.clip(MaterialTheme.shapes.large)
				.clickable { onClick() }
		)

		Spacer(modifier = Modifier.height(4.dp))

		Text(
			text = title ?: "Untitled",
			style = MaterialTheme.typography.bodySmall,
			color = MaterialTheme.colorScheme.onBackground,
			fontStyle = if (title == null) FontStyle.Italic else FontStyle.Normal,
		)

		releaseDate?.let {
			Spacer(modifier = Modifier.height(4.dp))
			Text(
				text = it,
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.onBackground,
			)
		}
	}
}