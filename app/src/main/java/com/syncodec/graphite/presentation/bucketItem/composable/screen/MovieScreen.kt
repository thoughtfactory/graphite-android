package com.syncodec.graphite.presentation.bucketItem.composable.screen

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.accompanist.flowlayout.FlowMainAxisAlignment
import com.google.accompanist.flowlayout.FlowRow
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.bucketItem.composable.LocalCompositionMovieAdult
import com.syncodec.graphite.presentation.bucketItem.composable.LocalCompositionMovieGenres
import com.syncodec.graphite.presentation.bucketItem.composable.LocalCompositionMovieHomepage
import com.syncodec.graphite.presentation.bucketItem.composable.LocalCompositionMovieId
import com.syncodec.graphite.presentation.bucketItem.composable.LocalCompositionMovieImdbId
import com.syncodec.graphite.presentation.bucketItem.composable.LocalCompositionMovieOriginalLanguage
import com.syncodec.graphite.presentation.bucketItem.composable.LocalCompositionMovieOriginalTitle
import com.syncodec.graphite.presentation.bucketItem.composable.LocalCompositionMovieOverview
import com.syncodec.graphite.presentation.bucketItem.composable.LocalCompositionMoviePosterPath
import com.syncodec.graphite.presentation.bucketItem.composable.LocalCompositionMovieReleaseDate
import com.syncodec.graphite.presentation.bucketItem.composable.LocalCompositionMovieRuntime
import com.syncodec.graphite.presentation.bucketItem.composable.LocalCompositionMovieTagline
import com.syncodec.graphite.presentation.bucketItem.composable.LocalCompositionMovieTitle
import com.syncodec.graphite.presentation.bucketItem.composable.LocalCompositionOnChangeState
import com.syncodec.graphite.presentation.bucketItem.composable.LocalCompositionState
import com.syncodec.graphite.presentation.bucketItem.composable.LocalCompositionThumbnail
import com.syncodec.graphite.presentation.common.LocalCompositionOpenDialog
import com.syncodec.graphite.presentation.common.button.stateButton.StateButton
import com.syncodec.graphite.presentation.common.button.stateButton.StateData
import com.syncodec.graphite.presentation.common.dialog.DialogType


@Composable
fun MovieScreen() {
	val context = LocalContext.current

	val movieId = LocalCompositionMovieId.current
	val movieAdult = LocalCompositionMovieAdult.current
	val movieGenres = LocalCompositionMovieGenres.current
	val movieHomepage = LocalCompositionMovieHomepage.current
	val movieImdbId = LocalCompositionMovieImdbId.current
	val movieOriginalLanguage = LocalCompositionMovieOriginalLanguage.current
	val movieOriginalTitle = LocalCompositionMovieOriginalTitle.current
	val movieOverview = LocalCompositionMovieOverview.current
	val moviePosterPath = LocalCompositionMoviePosterPath.current
	val movieReleaseDate = LocalCompositionMovieReleaseDate.current
	val movieRuntime = LocalCompositionMovieRuntime.current
	val movieTitle = LocalCompositionMovieTitle.current
	val movieTagline = LocalCompositionMovieTagline.current

	val thumbnail = LocalCompositionThumbnail.current

	val currentState = LocalCompositionState.current ?: 0
	val onChangeState = LocalCompositionOnChangeState.current

	val uriHandler = LocalUriHandler.current

	val configuration = LocalConfiguration.current
	val screenWidth = configuration.screenWidthDp.dp

	val stateList = listOf(
		StateData(
			title = "To Watch",
			icon = R.drawable.ic_movie,
		),
		StateData(
			title = "Watching",
			icon = R.drawable.ic_clock,
		),
		StateData(
			title = "Watched",
			icon = R.drawable.ic_check,
		),
	)

	val scrollState = rememberScrollState()

	val openDialog = LocalCompositionOpenDialog.current

	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		modifier = Modifier
			.fillMaxSize()
			.verticalScroll(scrollState),
	) {
		Spacer(modifier = Modifier.height(8.dp))

		Box(
			contentAlignment = Alignment.Center,
			modifier = Modifier
				.width(screenWidth / 2)
				.aspectRatio(0.6666f)
				.background(MaterialTheme.colorScheme.surface, RoundedCornerShape(24.dp))
				.clip(RoundedCornerShape(24.dp))
		) {
			AsyncImage(
				model = ImageRequest.Builder(context)
					.data(thumbnail)
					.crossfade(300)
					.build(),
				placeholder = null,
				contentDescription = movieTitle,
				contentScale = ContentScale.Crop,
				modifier = Modifier.fillMaxSize(),
			)
		}

		Spacer(modifier = Modifier.height(24.dp))

		Box(
			modifier = Modifier
				.fillMaxWidth()
				.padding(16.dp, 0.dp)
				.background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
				.clip(RoundedCornerShape(16.dp))
				.clickable { openDialog(DialogType.SHOW_INFO) }
		) {
			Column(
				modifier = Modifier
					.fillMaxWidth()
					.padding(16.dp)
			) {
				Row(
					verticalAlignment = Alignment.Bottom,
					modifier = Modifier.fillMaxWidth(),
				) {
					Text(
						text = movieTitle ?: "",
						style = MaterialTheme.typography.titleLarge,
						color = MaterialTheme.colorScheme.onSurface,
						fontWeight = FontWeight.Bold,
						overflow = TextOverflow.Ellipsis,
						modifier = Modifier.weight(1f),
					)

					Spacer(modifier = Modifier.width(16.dp))

					Text(
						text = movieRuntime?.toString()?.plus(" minutes") ?: "",
						style = MaterialTheme.typography.bodyMedium,
						color = MaterialTheme.colorScheme.onSurface
					)
				}

				Row(
					modifier = Modifier.fillMaxWidth()
				) {
					Text(
						text = movieTagline ?: "",
						style = MaterialTheme.typography.bodyMedium,
						color = MaterialTheme.colorScheme.onSurface,
					)

					Spacer(modifier = Modifier.weight(1f))

					Spacer(modifier = Modifier.width(8.dp))

					Text(
						text = movieReleaseDate ?: "",
						style = MaterialTheme.typography.bodyMedium,
						color = MaterialTheme.colorScheme.onSurface
					)
				}
			}
		}

		Spacer(modifier = Modifier.height(4.dp))

		StateButton(
			stateList = stateList,
			currentState = currentState,
			containerColor = MaterialTheme.colorScheme.surface,
			modifier = Modifier
				.fillMaxWidth()
				.padding(16.dp, 0.dp)
				.height(32.dp),
			onStateChange = onChangeState
		)

		Spacer(modifier = Modifier.height(4.dp))

		Box(
			modifier = Modifier
				.fillMaxWidth()
				.padding(16.dp, 0.dp)
				.background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
		) {
			Column(
				modifier = Modifier
					.fillMaxWidth()
					.padding(16.dp)
			) {
				Text(
					text = "Overview",
					style = MaterialTheme.typography.titleMedium,
					color = MaterialTheme.colorScheme.onSurface,
					fontWeight = FontWeight.Bold,
					overflow = TextOverflow.Ellipsis,
					modifier = Modifier.fillMaxWidth()
				)

				Text(
					text = movieOverview ?: "",
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.onSurface,
				)
			}
		}

		Spacer(modifier = Modifier.height(4.dp))

		FlowRow(
			modifier = Modifier
				.fillMaxWidth()
				.padding(16.dp, 0.dp),
			mainAxisAlignment = FlowMainAxisAlignment.SpaceBetween,
			lastLineMainAxisAlignment = FlowMainAxisAlignment.Start,
			mainAxisSpacing = 8.dp,
		) {
			movieGenres.forEach { genre ->
				if (genre?.name != null) {
					Box(
						modifier = Modifier.background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
					) {
						Text(
							text = genre.name ?: return@Box,
							style = MaterialTheme.typography.bodyMedium,
							color = MaterialTheme.colorScheme.onSurface,
							fontWeight = FontWeight.Bold,
							modifier = Modifier.padding(12.dp, 8.dp)
						)
					}
				}
			}
		}

		Spacer(modifier = Modifier.height(2.dp))

		Button(
			onClick = {
				try {
					uriHandler.openUri("https://www.themoviedb.org/movie/$movieId/")
				} catch (e : Exception) {
					Toast.makeText(context, "Error opening link", Toast.LENGTH_SHORT).show()
				}
			},
			modifier = Modifier
				.fillMaxWidth()
				.padding(16.dp, 0.dp)
		) {
			Text(text = "Open in TMDB")
		}

		Spacer(modifier = Modifier.height(2.dp))

		Box(
			modifier = Modifier
				.fillMaxWidth()
				.padding(16.dp, 0.dp)
				.background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
				.clip(RoundedCornerShape(16.dp))
				.clickable { uriHandler.openUri("https://www.themoviedb.org/") }
		) {
			Column(
				modifier = Modifier
					.fillMaxWidth()
					.padding(16.dp)
			) {
				Text(
					text = "Source: The Movie Database",
					style = MaterialTheme.typography.titleMedium,
					color = MaterialTheme.colorScheme.onSurface,
					fontWeight = FontWeight.Bold,
					overflow = TextOverflow.Ellipsis,
					modifier = Modifier.fillMaxWidth()
				)

				Spacer(modifier = Modifier.height(12.dp))

				Image(
					painter = painterResource(id = R.drawable.il_tmdb),
					contentDescription = "TMDB Logo",
					contentScale = ContentScale.Fit,
					modifier = Modifier.padding(0.dp)
				)
			}
		}

		Spacer(modifier = Modifier.height(32.dp))
	}
}
