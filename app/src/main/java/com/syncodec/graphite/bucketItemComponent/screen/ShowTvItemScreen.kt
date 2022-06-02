package com.syncodec.graphite.bucketItemComponent.screen

import androidx.compose.foundation.Image
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.bucketComponent.modalBottomSheet.ShowType
import com.syncodec.graphite.bucketItemComponent.BucketItemActivity
import com.syncodec.graphite.bucketItemComponent.miscellaneous.*
import com.syncodec.graphite.bucketItemComponent.miscellaneous.thought.ThoughtCard
import com.syncodec.graphite.custom.button.LargeButton
import com.syncodec.graphite.custom.button.StateButton
import com.syncodec.graphite.custom.button.StateData
import com.syncodec.graphite.database.bucketItem.TvData
import com.syncodec.graphite.konstant.Konstant


@Composable
fun ShowTvItemScreen(
	tvData: TvData,
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

		if (!tvData.tagline.isNullOrBlank()) {
			TaglineCard(tagline = tvData.tagline)
			Spacer(modifier = Modifier.height(12.dp))
		}

		ShowHeaderCard(
			title = tvData.name,
			inProduction = tvData.inProduction,
			releaseDate = if ((tvData.firstAirDate?.length ?: 0) > 4)
				tvData.firstAirDate?.substring(0, 4) else null,
			showType = ShowType.TV,
			showLength = tvData.episodeRunTime.firstOrNull(),
			noSeason = tvData.noSeason,
			noEpisode = tvData.noEpisode
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
		tvData.genreIds.forEach { Konstant.genreIdMap[it]?.let { it1 -> tagList.add(it1) } }
		TagCard(tagList = tagList)
		Spacer(modifier = Modifier.height(12.dp))

		if (tvData.overview != null) {
			OverviewCard(overview = tvData.overview)
			Spacer(modifier = Modifier.height(12.dp))
		}

		LargeButton(
			text = "Open in TMDB",
			enabled = true,
			modifier = Modifier.fillMaxWidth()
		) {
			val url = "https://www.themoviedb.org/tv/${tvData.id}"
			onAction(BucketItemActivity.Action.OPEN_LINK, url)
		}

		Spacer(modifier = Modifier.height(32.dp))

		Image(
			painter = painterResource(id = R.drawable.il_tmdb),
			contentDescription = "TMDB: The movie database",
			contentScale = ContentScale.Fit,
			modifier = Modifier
				.fillMaxWidth()
				.height(24.dp)
		)

		Spacer(modifier = Modifier.height(32.dp))
	}
}
