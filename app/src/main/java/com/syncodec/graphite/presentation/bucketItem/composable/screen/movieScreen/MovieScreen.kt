package com.syncodec.graphite.presentation.bucketItem.composable.screen.movieScreen

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.with
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.accompanist.flowlayout.FlowMainAxisAlignment
import com.google.accompanist.flowlayout.FlowRow
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.bucketItem.composable.screen.InfoSurface
import com.syncodec.graphite.presentation.common.button.stateButton.StateButton
import com.syncodec.graphite.presentation.common.button.stateButton.StateData
import com.syncodec.graphite.utils.ContentStatus
import com.valentinilk.shimmer.shimmer


@OptIn(ExperimentalAnimationApi::class)
@Preview
@Composable
fun MovieScreen(
	currentState : Int = 0,
	onChangeState : (Int) -> Unit = {}
) {
	val context = LocalContext.current
	val viewModel : MovieScreenViewModel = viewModel()

	val configuration = LocalConfiguration.current
	val screenWidth = configuration.screenWidthDp.dp

	val movieId by viewModel.movieId.collectAsState()
	val movieGenres by viewModel.movieGenres.collectAsState()
	val movieImdbId by viewModel.movieImdbId.collectAsState()
	val movieOverview by viewModel.movieOverview.collectAsState()
	val movieReleaseDate by viewModel.movieReleaseDate.collectAsState()
	val movieRuntime by viewModel.movieRuntime.collectAsState()
	val movieTitle by viewModel.movieTitle.collectAsState()
	val movieTagline by viewModel.movieTagline.collectAsState()

	val thumbnailStatus by viewModel.thumbnailContentStatus.collectAsState()

	val stateList = listOf(
		StateData(
			title = "To Watch",
			icon = R.drawable.ic_movie,
		),
		StateData(
			title = "Watching",
			icon = R.drawable.ic_advance,
		),
		StateData(
			title = "Watched",
			icon = R.drawable.ic_done,
		),
	)

	val scrollState = rememberScrollState()

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
				.background(
					MaterialTheme.colorScheme
						.surfaceColorAtElevation(8.dp)
						.copy(alpha = 0.31f), MaterialTheme.shapes.extraLarge
				)
				.clip(MaterialTheme.shapes.extraLarge)
				.then(if (thumbnailStatus is ContentStatus.Loaded) Modifier else Modifier.shimmer())
		) {
			when (thumbnailStatus) {
				is ContentStatus.Init -> Box(modifier = Modifier.fillMaxSize())
				is ContentStatus.Loading -> Box(modifier = Modifier.fillMaxSize())
				is ContentStatus.LoadedEmpty -> Text(
					text = "Thumbnail unavailable",
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.onSurface,
					fontWeight = FontWeight.Bold,
				)

				is ContentStatus.Loaded -> AsyncImage(
					model = ImageRequest.Builder(context)
						.data(thumbnailStatus.dataOrNull)
						.crossfade(300)
						.build(),
					placeholder = null,
					contentDescription = movieTitle,
					contentScale = ContentScale.Crop,
					modifier = Modifier.fillMaxSize()
				)

				is ContentStatus.Error -> Text(
					text = "Thumbnail unavailable",
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.onSurface,
					fontWeight = FontWeight.Bold,
				)
			}
		}

		Spacer(modifier = Modifier.height(24.dp))

		InfoSurface {
			Column(
				modifier = Modifier.fillMaxWidth()
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

		Spacer(modifier = Modifier.height(8.dp))

		StateButton(
			stateList = stateList,
			currentState = currentState,
			modifier = Modifier
				.fillMaxWidth()
				.padding(16.dp, 0.dp)
				.height(36.dp),
			onChangeState = onChangeState,
		)

		Spacer(modifier = Modifier.height(8.dp))

		InfoSurface {
			Column(
				modifier = Modifier.fillMaxWidth()
			) {
				Text(
					text = "Overview",
					style = MaterialTheme.typography.titleMedium,
					color = MaterialTheme.colorScheme.onSurface,
					fontWeight = FontWeight.Bold,
					overflow = TextOverflow.Ellipsis,
					modifier = Modifier.fillMaxWidth()
				)

				AnimatedContent(
					targetState = movieOverview,
					transitionSpec = { expandVertically(tween(300)) with shrinkVertically(tween(300)) },
					label = "movieOverview_animation"
				) {
					if (it.isNullOrEmpty()) {
						Text(
							text = "No overview available",
							style = MaterialTheme.typography.bodyMedium,
							color = MaterialTheme.colorScheme.onSurface,
						)
					} else {
						Text(
							text = it,
							style = MaterialTheme.typography.bodyMedium,
							color = MaterialTheme.colorScheme.onSurface,
							overflow = TextOverflow.Ellipsis,
						)
					}
				}
			}
		}

		movieGenres.filter { it.name != null }.let { genreList ->
			Spacer(modifier = Modifier.height(8.dp))
			FlowRow(
				mainAxisAlignment = FlowMainAxisAlignment.SpaceBetween,
				lastLineMainAxisAlignment = FlowMainAxisAlignment.Start,
				mainAxisSpacing = 8.dp,
				modifier = Modifier
					.fillMaxWidth()
					.padding(16.dp, 0.dp),
			) {
				genreList.forEach { genre ->
					Box(
						modifier = Modifier.background(MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp).copy(alpha = 0.31f), MaterialTheme.shapes.small)
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

		Spacer(modifier = Modifier.height(4.dp))

		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(16.dp, 0.dp)
		) {
			Button(
				shape = MaterialTheme.shapes.medium,
				onClick = {
					try {
						context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.imdb.com/title/$movieImdbId/")))
					} catch (e : Exception) {
						Toast.makeText(context, "Error opening link", Toast.LENGTH_SHORT).show()
					}
				},
				modifier = Modifier.weight(1f),
			) {
				Text(text = "Open in IMDb")
				Spacer(modifier = Modifier.width(6.dp))
				Icon(
					painter = painterResource(id = R.drawable.ic_launch),
					contentDescription = "Open in IMDb",
					modifier = Modifier.size(20.dp),
				)
			}

			Spacer(modifier = Modifier.width(8.dp))

			Button(
				shape = MaterialTheme.shapes.medium,
				onClick = {
					try {
						context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.themoviedb.org/movie/$movieId/")))
					} catch (e : Exception) {
						Toast.makeText(context, "Error opening link", Toast.LENGTH_SHORT).show()
					}
				},
				modifier = Modifier.weight(1f),
			) {
				Text(text = "Open in TMDB")
				Spacer(modifier = Modifier.width(6.dp))
				Icon(
					painter = painterResource(id = R.drawable.ic_launch),
					contentDescription = "Open in TMDB",
					modifier = Modifier.size(20.dp)
				)
			}
		}

		Spacer(modifier = Modifier.height(6.dp))

		InfoSurface(
			containerColor = MaterialTheme.colorScheme.background,
			onClick = {
				try {
					context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.themoviedb.org/")))
				} catch (e : Exception) {
					Toast.makeText(context, "Error opening link", Toast.LENGTH_SHORT).show()
				}
			}
		) {
			Column(
				modifier = Modifier.fillMaxWidth()
			) {
				Text(
					text = "Source: The Movie Database",
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.onBackground,
					fontWeight = FontWeight.Bold,
					overflow = TextOverflow.Ellipsis,
					modifier = Modifier.fillMaxWidth()
				)

				Spacer(modifier = Modifier.height(8.dp))

				Image(
					painter = painterResource(id = R.drawable.il_tmdb),
					contentDescription = "TMDB Logo",
					contentScale = ContentScale.Fit,
					modifier = Modifier.height(16.dp)
				)

				Spacer(modifier = Modifier.height(8.dp))

				Text(
					text = "This product uses the TMDB API but is not endorsed or certified by TMDB.",
					style = MaterialTheme.typography.bodySmall,
					color = MaterialTheme.colorScheme.onBackground,
				)
			}
		}

		Spacer(modifier = Modifier.height(32.dp))
	}
}
