package com.syncodec.graphite.presentation.main.composable.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.BucketObject
import com.syncodec.graphite.di.model.BucketType
import com.syncodec.graphite.presentation.main.composable.LocalCompositionIsBucketResreshing
import com.syncodec.graphite.presentation.main.composable.LocalCompositionIsSelected
import com.syncodec.graphite.presentation.main.composable.LocalCompositionOnRefresh
import com.syncodec.graphite.presentation.main.composable.LocalCompositionSelectedObjectIdList
import com.syncodec.graphite.presentation.main.composable.buildingBlock.BucketFloatingActionButton
import com.syncodec.graphite.presentation.ui.FavouriteContainer
import com.syncodec.graphite.presentation.ui.LockClosedContainer
import com.syncodec.graphite.utils.bucketTypeToIcon
import io.realm.kotlin.types.ObjectId
import kotlin.random.Random


@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun BucketScreen(
	bucketList : List<BucketObject>,
	onClickFab : () -> Unit,
	onClickBucket : (ObjectId) -> Unit,
	onLongClickBucket : (ObjectId) -> Unit
) {
	val isBucketRefreshing = LocalCompositionIsBucketResreshing.current
	val onRefresh = LocalCompositionOnRefresh.current

	val isSelected = LocalCompositionIsSelected.current
	val selectedObjectIdList = LocalCompositionSelectedObjectIdList.current

	Scaffold(
		modifier = Modifier.fillMaxSize(),
		floatingActionButton = {
			AnimatedVisibility(
				visible = ! isSelected,
				enter = fadeIn(tween(300)) + scaleIn(tween(300)),
				exit = fadeOut(tween(300)) + scaleOut(tween(300))
			) {
				BucketFloatingActionButton(isExpanded = true, onClick = onClickFab)
			}
		}
	) {
		Box(
			modifier = Modifier
				.fillMaxSize()
				.padding(it)
		) {
			if (bucketList.isEmpty()) {
				NoBucketCard()
			} else {
				SwipeRefresh(
					state = rememberSwipeRefreshState(isRefreshing = isBucketRefreshing == true),
					onRefresh = onRefresh
				) {
					LazyVerticalGrid(
						columns = GridCells.Adaptive(minSize = 144.dp),
						modifier = Modifier
							.padding(4.dp)
							.fillMaxSize(),
					) {
						bucketList.forEach {
							item {
								BucketCard(
									title = it.title,
									bucketSize = it.bucketItemList.size,
									bucketType = it.bucketType.let { BucketType.valueOf(it) },
									isLocked = it.isLocked,
									isFavourite = it.isFavourite,
									isSelected = it.id in selectedObjectIdList,
									onClick = { onClickBucket(it.id) },
									onLongClick = { onLongClickBucket(it.id) }
								)
							}
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
	title : String?,
	bucketSize : Int,
	bucketType : BucketType,
	isLocked: Boolean,
	isFavourite: Boolean,
	isSelected : Boolean,
	onClick : () -> Unit,
	onLongClick : () -> Unit
) {
	val containerColor by animateColorAsState(
		if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent
	)

	OutlinedCard(
		shape = RoundedCornerShape(4.dp, 4.dp, 16.dp, 16.dp),
		border = BorderStroke(2.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.17f)),
		colors = CardDefaults.cardColors(containerColor),
		elevation = CardDefaults.outlinedCardElevation(defaultElevation = 0.dp),
		modifier = Modifier
			.height(96.dp)
			.padding(4.dp)
			.clip(RoundedCornerShape(4.dp, 4.dp, 16.dp, 16.dp))
			.combinedClickable(
				onClick = onClick,
				onLongClick = onLongClick
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
				verticalAlignment = Alignment.CenterVertically,
				modifier = Modifier.fillMaxWidth()
			) {
				Icon(
					painter = painterResource(id = bucketTypeToIcon.getOrElse(bucketType) { R.drawable.ic_bucket }),
					contentDescription = null,
					tint = MaterialTheme.colorScheme.onBackground,
					modifier = Modifier.size(24.dp)
				)
				Spacer(modifier = Modifier.weight(1f))
				if (isFavourite) {
					Icon(
						painter = painterResource(id = R.drawable.ic_favourite),
						contentDescription = "Favourite",
						tint = Color.FavouriteContainer,
						modifier = Modifier.requiredSize(14.dp)
					)
					Spacer(modifier = Modifier.width(2.dp))
					Text(
						text = "·",
						style = MaterialTheme.typography.bodyMedium,
						color = MaterialTheme.colorScheme.onBackground,
						fontWeight = FontWeight.Bold,
						maxLines = 1,
						modifier = Modifier
					)
					Spacer(modifier = Modifier.width(2.dp))
				}
				if (isLocked) {
					Icon(
						painter = painterResource(id = R.drawable.ic_lock_close),
						contentDescription = "Locked",
						tint = Color.LockClosedContainer,
						modifier = Modifier.requiredSize(14.dp)
					)
					Spacer(modifier = Modifier.width(2.dp))
					Text(
						text = "·",
						style = MaterialTheme.typography.bodyMedium,
						color = MaterialTheme.colorScheme.onBackground,
						fontWeight = FontWeight.Bold,
						maxLines = 1,
						modifier = Modifier
					)
					Spacer(modifier = Modifier.width(2.dp))
				}

				Text(
					text = "$bucketSize",
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.onBackground,
				)
			}
			Text(
				text = title ?: "Untitled",
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onBackground,
				fontWeight = FontWeight.Bold,
				fontStyle = if (title.isNullOrEmpty()) FontStyle.Italic else FontStyle.Normal,
			)
		}
	}
}
