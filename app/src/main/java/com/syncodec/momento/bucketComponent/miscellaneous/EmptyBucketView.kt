package com.syncodec.momento.bucketComponent.miscellaneous

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.syncodec.momento.R
import com.syncodec.momento.bucketComponent.BucketActivity
import com.syncodec.momento.database.bucketItem.BucketItemType


@Composable
fun EmptyBucketView(
	bucketTitle: String,
	bucketItemType: BucketItemType,
	onClick: (BucketActivity.Click) -> Unit
) {
	Column(
		modifier = Modifier
			.fillMaxSize()
	) {
		TopBar(
			bucketTitle = bucketTitle,
			bucketItemType = bucketItemType,
			showStateSelector = false
		) { onClick(it) }
		Column(
			modifier = Modifier
				.fillMaxSize(),
			horizontalAlignment = Alignment.CenterHorizontally,
			verticalArrangement = Arrangement.Center
		) {
			Spacer(modifier = Modifier.height(24.dp))
			Image(
				painter = painterResource(id = R.drawable.il_reading),
				contentDescription = "No entries found",
				modifier = Modifier
					.fillMaxWidth(0.5f)
			)

			Spacer(modifier = Modifier.height(24.dp))

			Text(
				text = "The town was paper, but the memories were not.",
				style = MaterialTheme.typography.bodyMedium,
				fontWeight = FontWeight.Bold,
				color = MaterialTheme.colorScheme.primary,
				modifier = Modifier
					.fillMaxWidth(0.71f)
			)

			Spacer(modifier = Modifier.height(16.dp))

			Text(
				text = "~ John Green, Paper Towns",
				style = MaterialTheme.typography.bodySmall,
				fontStyle = FontStyle.Italic,
				textAlign = TextAlign.End,
				color = MaterialTheme.colorScheme.primary,
				modifier = Modifier
					.fillMaxWidth(0.71f)
			)
		}
	}
}
