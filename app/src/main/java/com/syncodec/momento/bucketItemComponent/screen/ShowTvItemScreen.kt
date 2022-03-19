package com.syncodec.momento.bucketItemComponent.screen

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
import com.syncodec.momento.bucketItemComponent.BucketItemActivity
import com.syncodec.momento.bucketItemComponent.miscellaneous.*
import com.syncodec.momento.bucketItemComponent.thought.ThoughtCard
import com.syncodec.momento.custom.button.LargeButton
import com.syncodec.momento.custom.button.StateButton
import com.syncodec.momento.custom.button.StateData
import com.syncodec.momento.database.bucket.TvData
import com.syncodec.momento.konstant.Konstant
import compose.icons.TablerIcons
import compose.icons.tablericons.Book
import compose.icons.tablericons.Check
import compose.icons.tablericons.Clock


@Composable
fun ShowTvItemScreen(
	tvData: TvData,
	thumbnail: Any? = null,
	thoughtList: SnapshotStateList<String> = mutableStateListOf(),
	currentState: Int,
	onClick: (BucketItemActivity.Click, Any?) -> Unit
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
			releaseDate = if ((tvData.firstAirDate?.length ?: 0) > 4) tvData.firstAirDate?.substring(0, 4) else null,
			showLength = tvData.episodeRunTime.firstOrNull(),
			inProduction = tvData.inProduction,
			noSeason = tvData.noSeason,
			noEpisode = tvData.noEpisode
		)
		Spacer(modifier = Modifier.height(12.dp))

		StateButton(
			stateList = listOf(
				StateData(title = "To Watch", icon = TablerIcons.Clock, color = MaterialTheme.colorScheme.primary),
				StateData(title = "Watching", icon = TablerIcons.Book, color = Color(245, 118, 26)),
				StateData(title = "Watched", icon = TablerIcons.Check, color = Color(81, 146, 89)),
			),
			currentState = currentState,
			modifier = Modifier
				.height(48.dp)
		) { onClick(BucketItemActivity.Click.STATE, it) }
		Spacer(modifier = Modifier.height(12.dp))

		ThoughtCard(thoughtList = thoughtList) { onClick(it, null) }
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
			text = "View in TMDB",
			containerColor = MaterialTheme.colorScheme.secondaryContainer,
			contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
			isClickable = true,
			modifier = Modifier
				.fillMaxWidth()
		) {

		}

		Spacer(modifier = Modifier.height(32.dp))
	}
}
