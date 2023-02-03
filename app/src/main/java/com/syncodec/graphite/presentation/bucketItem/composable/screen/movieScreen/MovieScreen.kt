package com.syncodec.graphite.presentation.bucketItem.composable.screen.movieScreen

import android.widget.Toast
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.accompanist.flowlayout.FlowMainAxisAlignment
import com.google.accompanist.flowlayout.FlowRow
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.bucketItem.composable.screen.InfoSurface
import com.syncodec.graphite.presentation.common.LocalCompositionOpenDialog
import com.syncodec.graphite.presentation.common.button.stateButton.StateButton
import com.syncodec.graphite.presentation.common.button.stateButton.StateData
import com.syncodec.graphite.presentation.common.dialog.DialogType


@Composable
fun MovieScreen(
	currentState : Int = 0,
	onChangeState : (Int) -> Unit = {}
) {
	val context = LocalContext.current
	val viewModel : MovieScreenViewModel = viewModel()

	val configuration = LocalConfiguration.current
	val screenWidth = configuration.screenWidthDp.dp

	val uriHandler = LocalUriHandler.current

	val movieId by viewModel.movieId.collectAsState()
	val movieAdult by viewModel.movieAdult.collectAsState()
	val movieGenres by viewModel.movieGenres.collectAsState()
	val movieHomepage by viewModel.movieHomepage.collectAsState()
	val movieImdbId by viewModel.movieImdbId.collectAsState()
	val movieOriginalLanguage by viewModel.movieOriginalLanguage.collectAsState()
	val movieOriginalTitle by viewModel.movieOriginalTitle.collectAsState()
	val movieOverview by viewModel.movieOverview.collectAsState()
	val moviePosterPath by viewModel.moviePosterPath.collectAsState()
	val movieReleaseDate by viewModel.movieReleaseDate.collectAsState()
	val movieRuntime by viewModel.movieRuntime.collectAsState()
	val movieTitle by viewModel.movieTitle.collectAsState()
	val movieTagline by viewModel.movieTagline.collectAsState()

	val thumbnail by viewModel.thumbnail.collectAsState()

	val openDialog = LocalCompositionOpenDialog.current

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
				.background(MaterialTheme.colorScheme.surface.copy(alpha = 0.71f), MaterialTheme.shapes.extraLarge)
				.clip(MaterialTheme.shapes.extraLarge)
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

		InfoSurface(
			onClick = { openDialog(DialogType.SHOW_INFO) }
		) {
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
			containerColor = MaterialTheme.colorScheme.surface,
			modifier = Modifier
				.fillMaxWidth()
				.padding(16.dp, 0.dp)
				.height(40.dp),
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

				Text(
					text = movieOverview ?: "",
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.onSurface,
				)
			}
		}

		Spacer(modifier = Modifier.height(8.dp))

		FlowRow(
			mainAxisAlignment = FlowMainAxisAlignment.SpaceBetween,
			lastLineMainAxisAlignment = FlowMainAxisAlignment.Start,
			mainAxisSpacing = 8.dp,
			modifier = Modifier
				.fillMaxWidth()
				.padding(16.dp, 0.dp),
		) {
			movieGenres.forEach { genre ->
				if (genre.name != null) {
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

		Spacer(modifier = Modifier.height(4.dp))

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

		Spacer(modifier = Modifier.height(6.dp))

		InfoSurface(
			onClick = { uriHandler.openUri("https://www.themoviedb.org/") }
		) {
			Column(
				modifier = Modifier.fillMaxWidth()
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
