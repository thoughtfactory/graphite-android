package com.syncodec.graphite.presentation.main.composable.screen.bucketScreen

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.syncodec.graphite.presentation.bucket.BucketActivity
import com.syncodec.graphite.presentation.common.ErrorView
import com.syncodec.graphite.presentation.common.LoadingView
import com.syncodec.graphite.presentation.common.scaffold.GenericScaffold
import com.syncodec.graphite.presentation.main.composable.buildingBlock.BucketFloatingActionButton
import com.syncodec.graphite.presentation.main.composable.buildingBlock.EmptyView
import com.syncodec.graphite.presentation.ui.FavouriteContainer
import com.syncodec.graphite.presentation.ui.IconButtonSize
import com.syncodec.graphite.presentation.ui.LockClosedContainer
import com.syncodec.graphite.utils.ContentStatus
import com.syncodec.graphite.utils.DataStoreInstance
import com.syncodec.graphite.utils.Extra
import com.syncodec.graphite.utils.SortBy
import com.syncodec.graphite.utils.SortOn
import com.syncodec.graphite.utils.bucketTypeToIcon
import io.realm.kotlin.types.RealmUUID
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.SpringDragCancelledAnimation
import org.burnoutcrew.reorderable.detectReorder
import org.burnoutcrew.reorderable.rememberReorderableLazyGridState
import org.burnoutcrew.reorderable.reorderable
import org.koin.androidx.compose.koinViewModel
import kotlin.random.Random


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun BucketScreen(
	isSelecting : Boolean,
	onSelect : (RealmUUID) -> Unit,
	selectedIdList : List<RealmUUID>,
	onClickFab : () -> Unit,
) {
	val context = LocalContext.current
	val viewModel : BucketScreenViewModel = koinViewModel()

	val dataStoreInstance = remember { DataStoreInstance(context = context) }
	val sortBy by dataStoreInstance.getSortBy.collectAsState(initial = null)
	val sortOn by dataStoreInstance.getSortOn.collectAsState(initial = null)

	val contentStatus by viewModel.contentStatus.collectAsState()
	val isBucketRefreshing by viewModel.isBucketRefreshing.collectAsState()

	val bucketList by viewModel.bucketList.collectAsState()

	var bucketListOrder by remember { mutableStateOf(listOf<BucketObject>()) }

	LaunchedEffect(key1 = bucketList, key2 = sortOn, key3 = sortBy) {
		when (sortOn) {
			SortOn.Title -> if (sortBy == SortBy.Ascending) bucketList.sortedBy { it.title } else bucketList.sortedByDescending { it.title }
			SortOn.Timestamp -> if (sortBy == SortBy.Ascending) bucketList.sortedBy { it.createdTimestamp } else bucketList.sortedByDescending { it.createdTimestamp }
			SortOn.Modified -> if (sortBy == SortBy.Ascending) bucketList.sortedBy { it.modifiedTimestamp } else bucketList.sortedByDescending { it.modifiedTimestamp }
			SortOn.CUSTOM -> bucketList
			else -> if (sortBy == SortBy.Ascending) bucketList.sortedBy { it.title } else bucketList.sortedByDescending { it.title }
		}.apply { bucketListOrder = this }
	}

	val activityLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
		try {
			it.data?.let { intent ->
				val hasIntentAction = intent.hasExtra(Extra.Companion.Extra.INTENT_ACTION.name)
				if (hasIntentAction) {
					val intentAction = intent.getStringExtra(Extra.Companion.Extra.INTENT_ACTION.name)?.let { it1 ->
						Extra.Companion.IntentAction.valueOf(it1)
					}
					if (intentAction == Extra.Companion.IntentAction.DELETE) {
						val hasObjectId = intent.hasExtra(Extra.Companion.Extra.OBJECT_ID.name)
						if (hasObjectId) intent.getByteArrayExtra(Extra.Companion.Extra.OBJECT_ID.name)?.let { bytes ->
							try {
								viewModel.delete(listOf(RealmUUID.from(bytes)))
							} catch (e : Exception) {
								null
							}
						}
					}
				}
				Extra.Companion.Extra.INTENT_ACTION.name
				Extra.Companion.Extra.OBJECT_ID.name
			}
		} catch (e : Exception) {
			Toast.makeText(context, "Error performing action", Toast.LENGTH_SHORT).show()
		}
	}

	fun onClickBucket(id : RealmUUID) {
		if (isSelecting) {
			onSelect(id)
		} else {
			Intent(context, BucketActivity::class.java).apply {
				putExtra(Extra.Companion.Extra.BUCKET_ID.name, id.bytes)
				activityLauncher.launch(this)
			}
		}
	}

	fun onLongClickBucket(id : RealmUUID) = onSelect(id)

	val state = rememberReorderableLazyGridState(
		dragCancelledAnimation = SpringDragCancelledAnimation(),
		onMove = { from, to ->
			bucketListOrder.toMutableList().apply {
				add(to.index, removeAt(from.index))
				bucketListOrder = this
			}
		},
		onDragEnd = { from, to ->
			viewModel.onReorderBucketList(bucketListOrder)
			dataStoreInstance.putSortOn(SortOn.CUSTOM)
		}
	)

	GenericScaffold(
		floatingActionButton = {
			BucketFloatingActionButton(isExpanded = true, onClick = onClickFab)
		},
		isFloatingActionButtonVisible = !isSelecting,
	) {
		Crossfade(
			targetState = contentStatus,
			animationSpec = tween(300)
		) { contentStatus ->
			when (contentStatus) {
				ContentStatus.Init -> LoadingView()
				ContentStatus.Error -> ErrorView()
				ContentStatus.Loading -> LoadingView()
				ContentStatus.LoadedEmpty -> EmptyView(
					image = remember { if (Random.nextBoolean()) R.drawable.il_bucket_list_b else R.drawable.il_bucket_list_g },
					title = "I think, therefore, I am",
					subTitle = "― René Descartes",
				)

				ContentStatus.Loaded -> SwipeRefresh(
					state = rememberSwipeRefreshState(isRefreshing = isBucketRefreshing),
					modifier = Modifier.fillMaxSize(),
					onRefresh = viewModel::refresh
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
							items = bucketListOrder,
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
									isSelected = bucketObject.id in selectedIdList,
									isDragging = isDragging,
									onClick = { onClickBucket(bucketObject.id) },
									onLongClick = { onLongClickBucket(bucketObject.id) },
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
