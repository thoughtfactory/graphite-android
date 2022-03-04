package com.syncodec.momento.bucketItemComponent.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.syncodec.momento.bucketComponent.modalBottomSheet.BookData
import com.syncodec.momento.bucketComponent.modalBottomSheet.MovieData
import com.syncodec.momento.bucketItemComponent.miscellaneous.HeaderCard
import com.syncodec.momento.bucketItemComponent.miscellaneous.HeaderData
import com.syncodec.momento.bucketItemComponent.miscellaneous.ThumbnailCard
import com.syncodec.momento.bucketItemComponent.thought.ThoughtCard
import com.syncodec.momento.custom.button.LargeButton
import com.syncodec.momento.custom.button.StateButton
import com.syncodec.momento.custom.button.StateData
import compose.icons.TablerIcons
import compose.icons.tablericons.Book
import compose.icons.tablericons.Check
import compose.icons.tablericons.Clock


@Composable
fun MovieItemScreen(
	movieData: MovieData,
	thumbnail: Any? = null,
	thoughtList: SnapshotStateList<String> = mutableStateListOf(),
	initialState: Int,
	onStateChange: (Int) -> Unit = {}
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

		HeaderCard(
			headerData = HeaderData(
				title = movieData.title,
				releaseDate = movieData.releaseDate,
				director = null
			)
		)
		Spacer(modifier = Modifier.height(12.dp))

		ThoughtCard(thoughtList = thoughtList)
		Spacer(modifier = Modifier.height(12.dp))

		StateButton(
			stateList = listOf(
				StateData(title = "To Watch", icon = TablerIcons.Clock, color = MaterialTheme.colorScheme.primary),
				StateData(title = "Watching", icon = TablerIcons.Book, color = Color(245, 118, 26)),
				StateData(title = "Watched", icon = TablerIcons.Check, color = Color(81, 146, 89)),
			),
			initialState = initialState,
			modifier = Modifier
				.height(48.dp)
		) {
			onStateChange(it)
		}
		Spacer(modifier = Modifier.height(12.dp))

		LargeButton(
			text = "View in TMDB",
			containerColor = MaterialTheme.colorScheme.tertiaryContainer,
			contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
			isClickable = true,
			modifier = Modifier
				.fillMaxWidth()
		) {

		}

		Spacer(modifier = Modifier.height(32.dp))
	}
}
