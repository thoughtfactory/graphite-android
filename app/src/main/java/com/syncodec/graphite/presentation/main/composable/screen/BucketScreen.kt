package com.syncodec.graphite.presentation.main.composable.screen

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.BucketObject
import com.syncodec.graphite.di.model.BucketType
import com.syncodec.graphite.presentation.main.composable.buildingBlock.BucketFloatingActionButton
import com.syncodec.graphite.utils.bucketTypeToIcon
import io.realm.kotlin.types.ObjectId
import kotlin.random.Random


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BucketScreen(
	bucketList: List<BucketObject>?,
	onClickFab: () -> Unit,
	onClickBucket: (ObjectId) -> Unit,
	onLongClickBucket: (ObjectId) -> Unit
) {
	Scaffold(
		modifier = Modifier.fillMaxSize(),
		floatingActionButton = {
			BucketFloatingActionButton(isExpanded = true, onClick = onClickFab)
		}
	) {
		Box(
			modifier = Modifier
				.fillMaxSize()
				.padding(it)
		) {
			if (bucketList.isNullOrEmpty()) {
				NoBucketCard()
			} else {
				LazyVerticalGrid(
					columns = GridCells.Adaptive(minSize = 144.dp),
					modifier = Modifier.padding(4.dp),
				) {
					bucketList.forEach {
						item {
							BucketCard(
								bucket = it,
								isSelected = false,
							) { onClickBucket(it.id) }
						}
					}
				}
			}
		}
	}
}

@Composable
private fun NoBucketCard() {
	Column(
		modifier = Modifier.fillMaxWidth(),
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.Center
	) {
		Spacer(modifier = Modifier.weight(1f))
		Image(
			painter = painterResource(id = if (Random.nextBoolean()) R.drawable.il_bucket_list_b else R.drawable.il_bucket_list_g),
			contentDescription = "No diary entries",
			modifier = Modifier.fillMaxWidth(0.64f)
		)

		Spacer(modifier = Modifier.height(24.dp))

		Text(
			text = "Two roads diverged in a wood and I – \nI took the one less traveled by,\nand that has made all the difference",
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onBackground,
			modifier = Modifier.fillMaxWidth(0.71f)
		)

		Spacer(modifier = Modifier.height(16.dp))

		Text(
			text = "~ Robert Frost, The Road Not Taken",
			style = MaterialTheme.typography.bodySmall,
			fontStyle = FontStyle.Italic,
			textAlign = TextAlign.End,
			color = MaterialTheme.colorScheme.onBackground,
			modifier = Modifier.fillMaxWidth(0.71f)
		)
		Spacer(modifier = Modifier.height(108.dp))
		Spacer(modifier = Modifier.weight(1f))
	}
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BucketCard(
	bucket: BucketObject,
	isSelected: Boolean,
	onClick: () -> Unit
) {
	val containerColor by animateColorAsState(
		if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent
	)

	OutlinedCard(
		shape = RoundedCornerShape(4.dp, 4.dp, 16.dp, 16.dp),
		border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondaryContainer),
		colors = CardDefaults.cardColors(containerColor),
		elevation = CardDefaults.outlinedCardElevation(defaultElevation = 0.dp),
		modifier = Modifier
			.height(96.dp)
			.padding(4.dp)
			.clip(RoundedCornerShape(4.dp, 4.dp, 16.dp, 16.dp))
			.combinedClickable(
				onClick = { onClick() },
				onLongClick = { }
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
					painter = painterResource(id = bucketTypeToIcon[BucketType.valueOf(bucket.bucketType)]!!),
					contentDescription = null,
					tint = MaterialTheme.colorScheme.onBackground,
					modifier = Modifier.size(24.dp)
				)
				Text(
					text = "${bucket.bucketItemList.size}",
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
