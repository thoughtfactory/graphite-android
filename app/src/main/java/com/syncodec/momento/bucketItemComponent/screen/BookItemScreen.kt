package com.syncodec.momento.bucketItemComponent.screen

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.syncodec.momento.bucketComponent.modalBottomSheet.BookData
import com.syncodec.momento.bucketItemComponent.BucketItemActivity
import com.syncodec.momento.bucketItemComponent.miscellaneous.BookHeaderCard
import com.syncodec.momento.bucketItemComponent.miscellaneous.ThumbnailCard
import com.syncodec.momento.bucketItemComponent.thought.ThoughtCard
import com.syncodec.momento.custom.button.LargeButton


@Composable
fun BookItemScreen(
	bookData: BookData,
	thumbnail: Any? = null,
	thoughtList: SnapshotStateList<String> = mutableStateListOf(),
	currentBookState: Int,
	onClick: (BucketItemActivity.Click, Int) -> Unit
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

		ThoughtCard(thoughtList = thoughtList) {}
		Spacer(modifier = Modifier.height(12.dp))

//		StateButton(
//			stateList = listOf(
//				StateData(title = "To Read", icon = TablerIcons.Clock, color = MaterialTheme.colorScheme.primary),
//				StateData(title = "Reading", icon = TablerIcons.Book, color = Color(245, 118, 26)),
//				StateData(title = "Read", icon = TablerIcons.Check, color = Color(81, 146, 89)),
//			),
//			currentState = currentBookState,
//			modifier = Modifier
//				.height(48.dp)
//		) { onClick(BucketItemActivity.Click.STATE, it) }
		Spacer(modifier = Modifier.height(12.dp))

		LargeButton(
			text = "View in Open Library",
			enabled = true,
			modifier = Modifier
				.fillMaxWidth()
		) {

		}

		Spacer(modifier = Modifier.height(32.dp))
	}
}
