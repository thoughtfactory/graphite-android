package com.syncodec.graphite.bucketItemComponent.screen

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.bucketComponent.modalBottomSheet.BookData
import com.syncodec.graphite.bucketItemComponent.BucketItemActivity
import com.syncodec.graphite.bucketItemComponent.miscellaneous.BookHeaderCard
import com.syncodec.graphite.bucketItemComponent.miscellaneous.ThumbnailCard
import com.syncodec.graphite.bucketItemComponent.miscellaneous.thought.ThoughtCard
import com.syncodec.graphite.custom.button.LargeButton
import com.syncodec.graphite.custom.button.StateButton
import com.syncodec.graphite.custom.button.StateData


@Composable
fun BookItemScreen(
	bookData: BookData,
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

		BookHeaderCard(
			title = bookData.title,
			releaseDate = bookData.firstPublishYear.let { if (it != null) "$it" else null },
			author = bookData.authorName?.let { if (it.isNotEmpty()) it.first() else null }
		)
		Spacer(modifier = Modifier.height(12.dp))

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
			currentState = currentState,
			modifier = Modifier.height(32.dp)
		) { onAction(BucketItemActivity.Action.STATE, it) }
		Spacer(modifier = Modifier.height(12.dp))

		ThoughtCard(
			thoughtList = thoughtList,
			onAction = onAction
		)
		Spacer(modifier = Modifier.height(12.dp))

		LargeButton(
			text = "View in Open Library",
			enabled = true,
			modifier = Modifier.fillMaxWidth()
		) {

		}

		Spacer(modifier = Modifier.height(32.dp))
	}
}
