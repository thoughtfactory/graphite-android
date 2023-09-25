package com.syncodec.graphite.presentation.bucket.composable.screen.linkScreen

import android.graphics.Bitmap
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.kedia.ogparser.OpenGraphResult
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.BucketItemObject
import com.syncodec.graphite.di.model.BucketType
import com.syncodec.graphite.presentation.bucket.composable.buildingBlock.EmptyView
import com.syncodec.graphite.presentation.common.selectable.SelectableContainer
import com.syncodec.graphite.presentation.base.FavouriteContainer
import com.syncodec.graphite.presentation.base.LockClosedContainer
import com.syncodec.graphite.utils.decodeBase64ToBitmap
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.syncodec.graphite.presentation.common.reorderable.ReorderableItem
import com.syncodec.graphite.presentation.common.reorderable.SpringDragCancelledAnimation
import com.syncodec.graphite.presentation.common.reorderable.detectReorder
import com.syncodec.graphite.presentation.common.reorderable.lazyState.rememberReorderableLazyListState
import com.syncodec.graphite.presentation.common.reorderable.reorderable


@Preview
@Composable
fun BucketLinkListScreen(
	bucketItemList: List<BucketItemObject> = listOf(),
	isSelecting: Boolean = false,
	selectedIdList: Set<RealmUUID> = setOf(),
	onSelect: (RealmUUID) -> Unit = {},
	onClickBucketItem: (RealmUUID) -> Unit = {},
	onReorderBucketItemList: (List<RealmUUID>) -> Unit = {},
) {
	val scope = rememberCoroutineScope()

	var bucketItemListOrdered by remember { mutableStateOf<List<BucketItemObject>>(listOf()) }
	LaunchedEffect(bucketItemList) { bucketItemListOrdered = bucketItemList.toList() }
	val state = rememberReorderableLazyListState(
		dragCancelledAnimation = SpringDragCancelledAnimation(),
		onMove = { from, to ->
			bucketItemListOrdered.toMutableList().apply {
				add(to.index, removeAt(from.index))
				bucketItemListOrdered = toList()
			}
		},
		onDragEnd = { from, to ->
			scope.launch(Dispatchers.Default) { onReorderBucketItemList(bucketItemListOrdered.map { it.id }) }
		}
	)

	if (bucketItemListOrdered.isEmpty()) {
		EmptyView(bucketType = BucketType.LINK)
	} else {
		LazyColumn(
			state = state.listState,
			modifier = Modifier
				.fillMaxSize()
				.reorderable(state)
		) {
			bucketItemListOrdered.forEach { bucketItemObject ->
				item(
					key = bucketItemObject.id.toString()
				) {
					ReorderableItem(
						reorderableState = state,
						key = bucketItemObject.id.toString(),
					) { isDragging ->
						LinkItem(
							title = bucketItemObject.title,
							thumbnail = bucketItemObject.thumbnail,
							openGraphResult = bucketItemObject.getOpenGraphResult(),
							dragHandle = {
								Icon(
									painter = painterResource(id = R.drawable.ic_fa_grip),
									contentDescription = "Reorder",
									tint = MaterialTheme.colorScheme.onSurface,
									modifier = Modifier
										.size(16.dp)
										.detectReorder(state)
								)
							},
							isLocked = bucketItemObject.isLocked,
							isFavourite = bucketItemObject.isFavourite,
							isSelected = isDragging or (bucketItemObject.id in selectedIdList),
							onClick = { if (isSelecting) onSelect(bucketItemObject.id) else onClickBucketItem(bucketItemObject.id) },
							onLongClick = { onSelect(bucketItemObject.id) },
						)
					}
				}
			}
		}
	}
}

@Preview
@Composable
private fun LinkItem(
	title: String? = null,
	thumbnail: String? = null,
	openGraphResult: OpenGraphResult? = null,
	dragHandle: @Composable () -> Unit = {},
	isLocked: Boolean = false,
	isFavourite: Boolean = false,
	isSelected: Boolean = false,
	onClick: () -> Unit = {},
	onLongClick: () -> Unit = {},
) {
	val thumbnail1 by remember(thumbnail) { derivedStateOf { thumbnail?.decodeBase64ToBitmap() } }

	SelectableContainer(
		selected = isSelected,
		modifier = Modifier.height(96.dp),
		onClick = onClick,
		onLongClick = onLongClick,
	) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier
				.fillMaxWidth()
				.padding(horizontal = 12.dp, vertical = 8.dp)
		) {
			dragHandle()

			Spacer(modifier = Modifier.width(12.dp))

			Thumbnail(
				thumbnail = thumbnail1,
				contentDescription = stringResource(id = R.string.thumbnail),
				isSelected = isSelected
			)

			Spacer(modifier = Modifier.width(12.dp))

			Column(
				verticalArrangement = Arrangement.Top,
				modifier = Modifier
					.fillMaxHeight()
					.weight(1f)
			) {

				TitleText(
					title = title,
					isLocked = isLocked,
					isFavourite = isFavourite
				)

				openGraphResult?.url?.let {
					Text(
						text = it,
						style = MaterialTheme.typography.bodySmall,
						color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.71f),
						maxLines = 2,
						overflow = TextOverflow.Ellipsis
					)
				}

				openGraphResult?.siteName?.let {
					Text(
						text = it,
						style = MaterialTheme.typography.bodySmall,
						color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.71f),
						maxLines = 2,
						overflow = TextOverflow.Ellipsis
					)
				}


				openGraphResult?.description?.let {
					Text(
						text = it,
						style = MaterialTheme.typography.bodySmall,
						color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.71f),
						maxLines = 1,
						overflow = TextOverflow.Ellipsis
					)
					Spacer(modifier = Modifier.height(4.dp))
				}
			}
		}
	}
}

@Composable
private fun Thumbnail(
	thumbnail: Bitmap? = null,
	contentDescription: String? = null,
	isSelected: Boolean = false,
) {
	val context = LocalContext.current

	val containerColor by animateColorAsState(
		targetValue = if (isSelected) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.surface.copy(alpha = 0.47f),
		animationSpec = tween(470),
		label = "containerColor_animation"
	)
	val contentColor by animateColorAsState(
		targetValue = if (isSelected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.47f),
		animationSpec = tween(470),
		label = "contentColor_animation"
	)

	SubcomposeAsyncImage(
		model = ImageRequest.Builder(context)
			.data(thumbnail)
			.crossfade(470)
			.build(),
		contentDescription = contentDescription,
		error = {
			Box(
				contentAlignment = Alignment.Center,
				modifier = Modifier
					.fillMaxSize()
					.background(containerColor)
			) {
				Icon(
					painter = painterResource(id = R.drawable.ic_fa_bucket_link),
					contentDescription = contentDescription,
					tint = contentColor,
					modifier = Modifier.requiredSize(32.dp)
				)
			}
		},
		contentScale = ContentScale.Crop,
		modifier = Modifier
			.fillMaxHeight()
			.aspectRatio(1f)
			.clip(RoundedCornerShape(16.dp)),
	)
}

@Preview
@Composable
private fun TitleText(
	modifier: Modifier = Modifier,
	title: String? = "Title",
	isLocked: Boolean = false,
	isFavourite: Boolean = true,
) {
	Row(
		modifier = modifier,
		verticalAlignment = Alignment.CenterVertically
	) {
		Text(
			text = title ?: "Untitled",
			style = MaterialTheme.typography.bodyMedium,
			fontWeight = if (title?.isNotEmpty() == true) FontWeight.Black else FontWeight.Normal,
			overflow = TextOverflow.Ellipsis,
			maxLines = 1,
			modifier = Modifier.weight(1f)
		)

		if (isLocked || isFavourite) {
			Box(
				modifier = Modifier.background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.small)
			) {
				Row(
					verticalAlignment = Alignment.CenterVertically,
					modifier = Modifier.padding(8.dp, 4.dp)
				) {
					if (isLocked) {
						Icon(
							painter = painterResource(id = R.drawable.ic_fa_lock_close_solid),
							contentDescription = "Locked",
							tint = Color.LockClosedContainer,
							modifier = Modifier.requiredSize(14.dp)
						)
						if (isFavourite) Text(
							text = "·",
							style = MaterialTheme.typography.bodySmall,
							fontWeight = FontWeight.Bold,
							maxLines = 1,
							modifier = Modifier.padding(horizontal = 2.dp)
						)
					}
					if (isFavourite) {
						Icon(
							painter = painterResource(id = R.drawable.ic_fa_heart_solid),
							contentDescription = "Favourite",
							tint = Color.FavouriteContainer,
							modifier = Modifier.requiredSize(14.dp)
						)
					}
				}
			}
		}
	}
}
