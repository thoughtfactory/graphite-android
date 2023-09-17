package com.syncodec.graphite.presentation.bucket.composable.screen.linkScreen

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
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
import com.syncodec.graphite.presentation.common.reorderable.ReorderableItem
import com.syncodec.graphite.presentation.common.reorderable.detectReorder
import com.syncodec.graphite.presentation.common.reorderable.lazyState.rememberReorderableLazyVerticalStaggeredGridState
import com.syncodec.graphite.presentation.common.reorderable.reorderable
import com.syncodec.graphite.presentation.common.selectable.SelectableContainer
import com.syncodec.graphite.presentation.common.selectable.SelectableContainerDefaults
import com.syncodec.graphite.presentation.base.FavouriteContainer
import com.syncodec.graphite.presentation.base.LockClosedContainer
import com.syncodec.graphite.utils.decodeBase64ToBitmap
import com.syncodec.graphite.utils.isTablet
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@Preview
@Composable
fun BucketLinkGridScreen(
	bucketItemList: List<BucketItemObject> = listOf(),
	isSelecting: Boolean = false,
	selectedIdList: Set<RealmUUID> = setOf(),
	onClickBucketItem: (RealmUUID) -> Unit = {},
	onSelect: (RealmUUID) -> Unit = {},
	onReorderBucketItemList: (List<RealmUUID>) -> Unit = {},
) {
	val scope = rememberCoroutineScope()

	var bucketItemListOrdered by remember { mutableStateOf<List<BucketItemObject>>(listOf()) }
	LaunchedEffect(bucketItemList) { bucketItemListOrdered = bucketItemList.toList() }
	val state = rememberReorderableLazyVerticalStaggeredGridState(
		onMove = { from, to ->
			bucketItemListOrdered.toMutableList().apply {
				add(to.index, removeAt(from.index))
				bucketItemListOrdered = toList()
			}
		},
		onDragEnd = { from, to ->
			scope.launch(Dispatchers.Default) { onReorderBucketItemList(bucketItemListOrdered.map { it.id }) }
		},
	)

	if (bucketItemListOrdered.isEmpty()) {
		EmptyView(bucketType = BucketType.LINK)
	} else {
		LazyVerticalStaggeredGrid(
			state = state.lazyStaggeredGridState,
			columns = StaggeredGridCells.Adaptive(if (isTablet()) 256.dp else 144.dp),
			contentPadding = PaddingValues(8.dp),
			modifier = Modifier
				.fillMaxSize()
				.reorderable(state)
		) {
			items(
				items = bucketItemListOrdered,
				key = { it.id.toString() }
			) { bucketItemObject ->
				ReorderableItem(
					reorderableState = state,
					key = bucketItemObject.id.toString(),
				) { isDragging ->
					LinkItem(
						title = bucketItemObject.title,
						thumbnail = bucketItemObject.thumbnail,
						openGraphResult = bucketItemObject.getOpenGraphResult(),
						dragHandle = {
							Box(
								contentAlignment = Alignment.Center,
								modifier = Modifier
									.requiredSize(24.dp)
									.background(MaterialTheme.colorScheme.background, MaterialTheme.shapes.small)
									.detectReorder(state)
									.padding(2.dp)
							) {
								Icon(
									painter = painterResource(id = R.drawable.ic_fa_grip),
									contentDescription = stringResource(id = R.string.reorder_grip),
									tint = MaterialTheme.colorScheme.onBackground,
									modifier = Modifier.requiredSize(16.dp)
								)
							}
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
	val context = LocalContext.current
	val isTablet = isTablet()

	val thumbnail1 by remember(thumbnail) { derivedStateOf { thumbnail?.decodeBase64ToBitmap() } }

	val containerColor by animateColorAsState(
		targetValue = if (isSelected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surface.copy(alpha = 0.47f),
		animationSpec = tween(470),
		label = "containerColor_animation"
	)
	val contentColor by animateColorAsState(
		targetValue = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.47f),
		animationSpec = tween(470),
		label = "contentColor_animation"
	)

	SelectableContainer(
		isSelected = isSelected,
		colors = SelectableContainerDefaults.surfaceColors(
			containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.31f),
			contentColor = MaterialTheme.colorScheme.onSurface,
		),
		modifier = Modifier
			.fillMaxWidth()
			.padding(4.dp)
			.clip(MaterialTheme.shapes.large),
		onClick = onClick,
		onLongClick = onLongClick,
	) {
		Column(
			modifier = Modifier.fillMaxWidth()
		) {
			Box(
				modifier = Modifier
					.fillMaxWidth()
					.height(if (isTablet) 144.dp else 96.dp)
			) {
				SubcomposeAsyncImage(
					model = ImageRequest.Builder(context)
						.data(thumbnail1)
						.crossfade(470)
						.build(),
					contentDescription = stringResource(id = R.string.thumbnail),
					error = {
						Box(
							contentAlignment = Alignment.Center,
							modifier = Modifier
								.fillMaxSize()
								.background(containerColor)
						) {
							Icon(
								painter = painterResource(id = R.drawable.ic_fa_bucket_link),
								contentDescription = stringResource(id = R.string.thumbnail),
								tint = contentColor,
								modifier = Modifier.requiredSize(32.dp)
							)
						}
					},
					contentScale = ContentScale.Crop,
					modifier = Modifier.fillMaxSize()
				)

				Column(
					verticalArrangement = Arrangement.SpaceBetween,
					horizontalAlignment = Alignment.End,
					modifier = Modifier
						.fillMaxSize()
						.padding(8.dp)
				) {
					dragHandle()

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
										contentDescription = stringResource(id = R.string.locked),
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
										contentDescription = stringResource(id = R.string.favourite),
										tint = Color.FavouriteContainer,
										modifier = Modifier.requiredSize(14.dp)
									)
								}
							}
						}
					}
				}
			}

			Column(
				modifier = Modifier
					.fillMaxWidth()
					.padding(12.dp)
			) {
				Text(
					text = title ?: stringResource(id = R.string.untitled),
					style = MaterialTheme.typography.bodyMedium,
					fontWeight = if (title?.isNotEmpty() == true) FontWeight.Black else FontWeight.Normal,
					overflow = TextOverflow.Ellipsis,
					maxLines = if (isTablet) 3 else 2,
					modifier = Modifier.fillMaxWidth()
				)
				openGraphResult?.url?.let {
					Text(
						text = it,
						style = MaterialTheme.typography.bodySmall,
						color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.71f),
						maxLines = if (isTablet) 3 else 2,
						overflow = TextOverflow.Ellipsis
					)
				}

				openGraphResult?.siteName?.let {
					Text(
						text = it,
						style = MaterialTheme.typography.bodySmall,
						color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.71f),
						maxLines = if (isTablet) 3 else 2,
						overflow = TextOverflow.Ellipsis
					)
				}


				openGraphResult?.description?.let {
					Text(
						text = it,
						style = MaterialTheme.typography.bodySmall,
						color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.71f),
						maxLines = if (isTablet) 4 else 1,
						overflow = TextOverflow.Ellipsis
					)
					Spacer(modifier = Modifier.height(4.dp))
				}
			}
		}
	}
}
