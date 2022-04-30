package com.syncodec.graphite.bucketItemComponent.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.bucketComponent.modalBottomSheet.ShowType
import com.syncodec.graphite.bucketItemComponent.BucketItemActivity
import com.syncodec.graphite.bucketItemComponent.miscellaneous.*
import com.syncodec.graphite.bucketItemComponent.miscellaneous.thought.ThoughtCard
import com.syncodec.graphite.custom.button.LargeButton
import com.syncodec.graphite.custom.button.StateButton
import com.syncodec.graphite.custom.button.StateData
import com.syncodec.graphite.database.bucketItem.MovieData
import com.syncodec.graphite.konstant.Konstant


@Composable
fun ShowMovieItemScreen(
	movieData: MovieData,
	thumbnail: Any? = null,
	currentState: Int,
	thoughtList: SnapshotStateList<String> = mutableStateListOf(),
	onAction: (BucketItemActivity.Action, Any?) -> Unit
) {
	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		modifier = Modifier
			.fillMaxWidth()
			.padding(16.dp, 0.dp)
			.background(MaterialTheme.colorScheme.background)
			.verticalScroll(state = rememberScrollState())
	) {
		Spacer(modifier = Modifier.height(24.dp))

		ThumbnailCard(thumbnail = thumbnail)
		Spacer(modifier = Modifier.height(24.dp))

		if (!movieData.tagline.isNullOrBlank()) {
			TaglineCard(tagline = movieData.tagline)
			Spacer(modifier = Modifier.height(16.dp))
		}

		ShowHeaderCard(
			title = movieData.title,
			inProduction = null,
			releaseDate =
			if (movieData.releaseDate != null && movieData.releaseDate.length > 3)
				movieData.releaseDate.substring(0, 4) else null,
			showType = ShowType.MOVIE,
			showLength = movieData.runtime,
			noSeason = null,
			noEpisode = null
		)
		Spacer(modifier = Modifier.height(12.dp))

		StateButton(
			stateList = listOf(
				StateData(
					title = "To Watch",
					icon = R.drawable.ic_clock,
					stateTint = MaterialTheme.colorScheme.primary
				),
				StateData(
					title = "Watching",
					icon = R.drawable.ic_show,
					stateTint = Color(245, 118, 26)
				),
				StateData(
					title = "Watched",
					icon = R.drawable.ic_done,
					stateTint = Color(81, 146, 89)
				),
			),
			currentState = currentState,
			modifier = Modifier.height(32.dp)
		) { onAction(BucketItemActivity.Action.STATE, it) }
		Spacer(modifier = Modifier.height(12.dp))

		ThoughtCard(
			thoughtList = thoughtList,
			onAction = onAction
		)
		Spacer(modifier = Modifier.height(12.dp))

		val tagList: MutableList<String> = mutableListOf()
		movieData.genreIds.forEach { Konstant.genreIdMap[it]?.let { it1 -> tagList.add(it1) } }
		TagCard(tagList = tagList)
		Spacer(modifier = Modifier.height(12.dp))

		if (movieData.overview != null) {
			OverviewCard(overview = movieData.overview)
			Spacer(modifier = Modifier.height(12.dp))
		}

		LargeButton(
			text = "View in TMDB",
			enabled = true,
			modifier = Modifier.fillMaxWidth()
		) {
			val url = "https://www.themoviedb.org/movie/${movieData.id}"
			onAction(BucketItemActivity.Action.OPEN_LINK, url)
		}

		Spacer(modifier = Modifier.height(32.dp))
	}
}
