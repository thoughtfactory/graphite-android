package com.syncodec.graphite.mainComponent.screen

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.MainActivity
import com.syncodec.graphite.R
import com.syncodec.graphite.database.bucket.BucketDbEntry
import com.syncodec.graphite.database.bucketItem.BucketItemType
import com.syncodec.graphite.konstant.ResourceMap

@ExperimentalMaterialApi
@ExperimentalFoundationApi
@Composable
fun BucketScreen(
	bucketList: List<BucketDbEntry>,
	selectedItemList: List<String>,
	bucketFilter: List<BucketItemType>,
	onAction: (MainActivity.Action, Any?) -> Unit
) {
	Column(
		modifier = Modifier.fillMaxSize()
	) {
		if (bucketList.isNotEmpty()) {
			Spacer(modifier = Modifier.height(8.dp))
			LazyVerticalGrid(
				columns = GridCells.Adaptive(minSize = 144.dp),
				modifier = Modifier.padding(4.dp),
			) {
				bucketList.forEach {
					if (it.bucketItemType in bucketFilter) {
						item {
							BucketCard(
								bucket = it,
								isSelected = it.key in selectedItemList,
								onAction = onAction
							)
						}
					}
				}
			}
		} else {
			NoBucketCard()
		}
	}
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun NoBucketCard() {
	Box(
		modifier = Modifier.fillMaxSize(),
		contentAlignment = Alignment.Center
	) {
		Column(
			modifier = Modifier
				.fillMaxWidth(),
			horizontalAlignment = Alignment.CenterHorizontally,
			verticalArrangement = Arrangement.Center
		) {
			Image(
				painter = painterResource(id = R.drawable.il_reading),
				contentDescription = "No diary entries",
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
				modifier = Modifier.fillMaxWidth(0.71f)
			)
		}
	}
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class,
	ExperimentalMaterial3Api::class
)
@Composable
private fun BucketCard(
	bucket: BucketDbEntry,
	isSelected: Boolean,
	onAction: (MainActivity.Action, Any?) -> Unit
) {
	val containerColor by animateColorAsState(
		if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent
	)

	OutlinedCard(
		shape = RoundedCornerShape(12.dp),
		border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondaryContainer),
		containerColor = containerColor,
		elevation = CardDefaults.outlinedCardElevation(defaultElevation = 0.dp),
		modifier = Modifier
			.height(96.dp)
			.padding(4.dp)
			.clip(RoundedCornerShape(12.dp))
			.combinedClickable(
				onClick = { onAction(MainActivity.Action.CLICK_BUCKET, bucket.key) },
				onLongClick = { onAction(MainActivity.Action.LONG_CLICK_BUCKET, bucket.key) }
			),
	) {
		Column(
			modifier = Modifier
				.padding(12.dp)
				.fillMaxWidth()
				.fillMaxHeight(),
			verticalArrangement = Arrangement.SpaceBetween,
			horizontalAlignment = Alignment.Start
		) {
			Row(
				horizontalArrangement = Arrangement.SpaceBetween,
				verticalAlignment = Alignment.CenterVertically,
				modifier = Modifier.fillMaxWidth()
			) {
				Icon(
					painter = painterResource(id = ResourceMap.bucketTypeToIcon[bucket.bucketItemType]!!),
					contentDescription = null,
					tint = MaterialTheme.colorScheme.onBackground,
					modifier = Modifier.size(24.dp)
				)
				Text(
					text = "${bucket.bucketSize}",
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.onBackground,
				)
			}
			Text(
				text = bucket.title,
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onBackground,
				fontWeight = FontWeight.ExtraBold
			)
		}
	}
}
