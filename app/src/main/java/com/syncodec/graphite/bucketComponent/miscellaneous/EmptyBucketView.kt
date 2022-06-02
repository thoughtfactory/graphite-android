package com.syncodec.graphite.bucketComponent.miscellaneous

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.bucketComponent.BucketActivity
import com.syncodec.graphite.database.bucketItem.BucketItemType
import com.syncodec.graphite.R
import kotlin.random.Random


@Composable
fun EmptyBucketView(
	bucketTitle: String,
	bucketItemType: BucketItemType,
	onAction: (BucketActivity.Action, Any?) -> Unit
) {
	Column(
		modifier = Modifier
			.fillMaxSize()
			.background(MaterialTheme.colorScheme.background)
	) {
		TopBar(
			title = bucketTitle,
			bucketItemType = bucketItemType,
			showStateSelector = false,
			currentState = 0,
			onAction = onAction
		)
		Column(
			modifier = Modifier.fillMaxSize(),
			horizontalAlignment = Alignment.CenterHorizontally,
			verticalArrangement = Arrangement.Center
		) {
			Image(
				painter = painterResource(
					id = when (bucketItemType) {
						BucketItemType.TODO -> if (Random.nextBoolean()) R.drawable.il_todo_g else R.drawable.il_todo_b
						BucketItemType.BOOKS -> if (Random.nextBoolean()) R.drawable.il_book_b else R.drawable.il_book_g
						BucketItemType.SHOWS -> R.drawable.il_show
					}
				),
				contentDescription = "No items found",
				modifier = Modifier.fillMaxWidth(0.88f)
			)

			Spacer(modifier = Modifier.height(24.dp))

			Text(
				text = when (bucketItemType) {
					BucketItemType.TODO -> "Live your life by a compass, not a clock"
					BucketItemType.BOOKS -> "There is more treasure in books than in all the pirate’s loot on Treasure Island"
					BucketItemType.SHOWS -> "When your life flashes before your eyes at the end, make sure it's a good movie you're watching"
				},
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.primary,
				modifier = Modifier.fillMaxWidth(0.71f)
			)

			Spacer(modifier = Modifier.height(16.dp))

			Text(
				text = when (bucketItemType) {
					BucketItemType.TODO -> "~ Stephen R Covey"
					BucketItemType.BOOKS -> "~ Walt Disney"
					BucketItemType.SHOWS -> "~ Stewart Stafford"
				},
				style = MaterialTheme.typography.bodySmall,
				fontStyle = FontStyle.Italic,
				textAlign = TextAlign.End,
				color = MaterialTheme.colorScheme.primary,
				modifier = Modifier.fillMaxWidth(0.71f)
			)

			Spacer(modifier = Modifier.height(64.dp))
		}
	}
}
