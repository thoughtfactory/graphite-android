package com.syncodec.graphite.presentation.bucketItem.composable.screen

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.bucketItem.BucketItemViewModel
import com.syncodec.graphite.presentation.bucketItem.composable.buildingBlock.MovieHeaderCard
import com.syncodec.graphite.presentation.bucketItem.composable.buildingBlock.OverviewCard
import com.syncodec.graphite.presentation.bucketItem.composable.buildingBlock.ThumbnailCard
import com.syncodec.graphite.presentation.custom.ErrorView
import com.syncodec.graphite.presentation.custom.LoadingView
import com.syncodec.graphite.presentation.custom.button.LargeButton
import com.syncodec.graphite.presentation.custom.button.stateButton.StateButton
import com.syncodec.graphite.presentation.custom.button.stateButton.StateData
import com.syncodec.graphite.utils.Status


@Composable
fun MovieScreen() {
	val viewModel: BucketItemViewModel = viewModel()

	val status by viewModel.status

	val title by viewModel.movieTitle
	val overview by viewModel.overview
	val runtime by viewModel.runtime
	val releaseDate by viewModel.releaseDate
	val thumbnail by viewModel.thumbnail

	Crossfade(status) {
		when (it) {
			Status.INIT -> LoadingView()
			Status.LOADING -> LoadingView()
			Status.LOADED -> LazyColumn(
				modifier = Modifier
					.fillMaxSize()
					.padding(16.dp, 0.dp),
				horizontalAlignment = Alignment.CenterHorizontally
			) {
				item { Spacer(modifier = Modifier.height(24.dp)) }

				item { ThumbnailCard(thumbnail = thumbnail) }
				item { Spacer(modifier = Modifier.height(24.dp)) }

				item {
					MovieHeaderCard(title = title, releaseDate = releaseDate, runtime = runtime)
				}

				item { Spacer(modifier = Modifier.height(12.dp)) }

				item {
					StateButton(
						stateList = listOf(
							StateData(
								title = "To Read",
								icon = R.drawable.ic_clock,
								stateTint = MaterialTheme.colorScheme.primary
							),
							StateData(
								title = "Reading",
								icon = R.drawable.ic_book,
								stateTint = Color(245, 118, 26)
							),
							StateData(
								title = "Read",
								icon = R.drawable.ic_done,
								stateTint = Color(81, 146, 89)
							),
						),
						currentState = 0,
						modifier = Modifier.height(32.dp)
					) { }
				}
				item { Spacer(modifier = Modifier.height(12.dp)) }

//		item{
//			ThoughtCard(
//				thoughtList = thoughtList,
//				onAction = onAction
//			)
//		}
//		item{ Spacer(modifier = Modifier.height(12.dp)) }

				item { OverviewCard(overview = overview) }
				item { Spacer(modifier = Modifier.height(12.dp)) }

				item {
					LargeButton(
						text = "View in Open Library",
						enabled = true,
						modifier = Modifier.fillMaxWidth()
					) {
//				val url = "https://openlibrary.org/${bookData.key}"
//				onAction(BucketItemActivity.Action.OPEN_LINK, url)
					}
				}
				item { Spacer(modifier = Modifier.height(32.dp)) }

				item {
					Image(
						painter = painterResource(id = R.drawable.il_open_library),
						contentDescription = "Open Library",
						contentScale = ContentScale.Fit,
						modifier = Modifier
							.fillMaxWidth()
							.height(128.dp)
					)
				}
				item { Spacer(modifier = Modifier.height(32.dp)) }

			}
			Status.ERROR -> ErrorView()
		}
	}
}
