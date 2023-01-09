package com.syncodec.graphite.presentation.main.composable.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.BucketObject
import com.syncodec.graphite.di.model.BucketType
import com.syncodec.graphite.presentation.common.LocalCompositionIsSelected
import com.syncodec.graphite.presentation.common.LocalCompositionSelectedObjectIdList
import com.syncodec.graphite.presentation.main.composable.LocalCompositionIsBucketRefreshing
import com.syncodec.graphite.presentation.main.composable.LocalCompositionOnRefresh
import com.syncodec.graphite.presentation.main.composable.buildingBlock.BucketFloatingActionButton
import com.syncodec.graphite.presentation.main.composable.buildingBlock.EmptyView
import com.syncodec.graphite.presentation.ui.FavouriteContainer
import com.syncodec.graphite.presentation.ui.IconButtonSize
import com.syncodec.graphite.presentation.ui.LockClosedContainer
import com.syncodec.graphite.utils.DataStoreInstance
import com.syncodec.graphite.utils.LocalVaultIsOpened
import com.syncodec.graphite.utils.SortBy
import com.syncodec.graphite.utils.SortOn
import com.syncodec.graphite.utils.bucketTypeToIcon
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.SpringDragCancelledAnimation
import org.burnoutcrew.reorderable.detectReorder
import org.burnoutcrew.reorderable.rememberReorderableLazyGridState
import org.burnoutcrew.reorderable.reorderable
import kotlin.random.Random


@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun BucketScreen(
	bucketList : List<BucketObject>,
	bucketOrderList: List<RealmUUID>,
	sortOn : SortOn,
	sortBy : SortBy,
	onReorderBucketList : (List<RealmUUID>) -> Unit,
	onClickFab : () -> Unit,
	onClickBucket : (RealmUUID) -> Unit,
	onLongClickBucket : (RealmUUID) -> Unit
) {
	val context = LocalContext.current
	val scope = rememberCoroutineScope()
	val dataStoreInstance = remember { DataStoreInstance(context = context) }

	val isBucketRefreshing = LocalCompositionIsBucketRefreshing.current
	val onRefresh = LocalCompositionOnRefresh.current

	val isSelected = LocalCompositionIsSelected.current
	val selectedRealmUUIDList = LocalCompositionSelectedObjectIdList.current

	val _bucketList : SnapshotStateList<BucketObject> = remember { mutableStateListOf() }

	LaunchedEffect(key1 = bucketList, key2 = sortOn, key3 = sortBy) {
		_bucketList.clear()
		when (sortOn) {
			SortOn.TITLE -> if (sortBy == SortBy.ASCENDING) bucketList.sortedBy { it.title } else bucketList.sortedByDescending { it.title }
			SortOn.TIMESTAMP -> if (sortBy == SortBy.ASCENDING) bucketList.sortedBy { it.createdTimestamp } else bucketList.sortedByDescending { it.createdTimestamp }
			SortOn.MODIFIED -> if (sortBy == SortBy.ASCENDING) bucketList.sortedBy { it.modifiedTimestamp } else bucketList.sortedByDescending { it.modifiedTimestamp }
			SortOn.CUSTOM -> {
				bucketOrderList.forEach { realmUUID ->
					bucketList.firstOrNull { it.id == realmUUID }?.let { _bucketList.add(it) }
				}
				bucketList.filter { it.id !in bucketOrderList }
			}
			else -> if (sortBy == SortBy.ASCENDING) bucketList.sortedBy { it.title } else bucketList.sortedByDescending { it.title }
		}.apply { _bucketList.addAll(this) }
	}

	val state = rememberReorderableLazyGridState(
		dragCancelledAnimation = SpringDragCancelledAnimation(),
		onMove = { from, to ->
			_bucketList.apply {
				add(to.index, removeAt(from.index))
			}
		},
		onDragEnd = { from, to ->
			scope.launch(Dispatchers.Default) { onReorderBucketList(_bucketList.map { it.id }) }
			dataStoreInstance.putSortOn(SortOn.CUSTOM)
		}
	)

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
			if (_bucketList.isEmpty()) {
				EmptyView(
					image = remember { if (Random.nextBoolean()) R.drawable.il_bucket_list_b else R.drawable.il_bucket_list_g },
					title = "I think, therefore, I am",
					subTitle = "― René Descartes",
				)
			} else {
				SwipeRefresh(
					state = rememberSwipeRefreshState(isRefreshing = isBucketRefreshing == true),
					modifier = Modifier.fillMaxSize(),
					onRefresh = onRefresh
				) {
					LazyVerticalGrid(
						columns = GridCells.Adaptive(144.dp),
						state = state.gridState,
						contentPadding = PaddingValues(horizontal = 8.dp),
						verticalArrangement = Arrangement.spacedBy(4.dp),
						horizontalArrangement = Arrangement.spacedBy(4.dp),
						modifier = Modifier
							.fillMaxSize()
							.reorderable(state)
					) {
						items(
							items = _bucketList,
							key = { it.id.toString() }
						) { bucketObject ->
							ReorderableItem(
								reorderableState = state,
								key = bucketObject.id.toString(),
							) { isDragging ->
								BucketCard(
									handleModifier = Modifier.detectReorder(state),
									title = bucketObject.title,
									bucketSize = bucketObject.bucketItemList.size,
									bucketType = bucketObject.bucketType.let { BucketType.valueOf(it) },
									isLocked = bucketObject.isLocked,
									isFavourite = bucketObject.isFavourite,
									isSelected = bucketObject.id in selectedRealmUUIDList,
									isDragging = isDragging,
									onClick = { onClickBucket(bucketObject.id) },
									onLongClick = { onLongClickBucket(bucketObject.id) }
								)
							}
						}
					}
				}
			}
		}
	}
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BucketCard(
	handleModifier : Modifier,
	title : String?,
	bucketSize : Int,
	bucketType : BucketType,
	isLocked : Boolean,
	isFavourite : Boolean,
	isSelected : Boolean,
	isDragging : Boolean,
	onClick : () -> Unit,
	onLongClick : () -> Unit
) {
	val scale by animateFloatAsState(targetValue = if (isDragging) 1.13f else 1f)

	val containerColor by animateColorAsState(
		if (isSelected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.background
	)

	Box(
		modifier = Modifier
			.graphicsLayer {
				this.scaleX = scale
				this.scaleY = scale
			}
	) {
		Box(
			modifier = Modifier
				.height(96.dp)
				.padding(4.dp)
				.background(containerColor, RoundedCornerShape(4.dp, 4.dp, 16.dp, 16.dp))
				.border(2.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.17f), RoundedCornerShape(4.dp, 4.dp, 16.dp, 16.dp))
				.clip(RoundedCornerShape(4.dp, 4.dp, 16.dp, 16.dp))
				.combinedClickable(
					onClick = onClick,
					onLongClick = onLongClick
				)
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
						modifier = Modifier.requiredSize(IconButtonSize)
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
				Row(
					verticalAlignment = Alignment.CenterVertically,
					modifier = Modifier.fillMaxWidth()
				) {
					Text(
						text = title ?: "Untitled",
						style = MaterialTheme.typography.bodyMedium,
						color = MaterialTheme.colorScheme.onBackground,
						fontWeight = FontWeight.Bold,
						fontStyle = if (title.isNullOrEmpty()) FontStyle.Italic else FontStyle.Normal,
						modifier = Modifier.weight(1f)
					)

					Spacer(modifier = Modifier.width(12.dp))

					Icon(
						painter = painterResource(id = R.drawable.ic_reorder),
						contentDescription = "Reorder",
						tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.47f),
						modifier = handleModifier.size(16.dp)
					)
				}
			}
		}
	}
}
