package com.syncodec.graphite.presentation.main.composable.screen.bucketScreen

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.BucketObject
import com.syncodec.graphite.di.model.BucketType
import com.syncodec.graphite.presentation.bucket.BucketActivity
import com.syncodec.graphite.presentation.common.LoadingView
import com.syncodec.graphite.presentation.common.scaffold.GenericScaffold2
import com.syncodec.graphite.presentation.main.composable.bottomSheet.BucketBottomSheet
import com.syncodec.graphite.presentation.main.composable.buildingBlock.BucketFloatingActionButton
import com.syncodec.graphite.presentation.main.composable.buildingBlock.EmptyView
import com.syncodec.graphite.presentation.main.composable.screen.bucketScreen.buildingBlock.BucketCard
import com.syncodec.graphite.utils.ContentStatus
import com.syncodec.graphite.utils.dataStore.DataStoreInstance
import com.syncodec.graphite.utils.Extra
import com.syncodec.graphite.utils.LocalIsAuthenticated
import com.syncodec.graphite.utils.SortBy
import com.syncodec.graphite.utils.SortOn
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.SpringDragCancelledAnimation
import org.burnoutcrew.reorderable.detectReorder
import org.burnoutcrew.reorderable.rememberReorderableLazyGridState
import org.burnoutcrew.reorderable.reorderable
import org.koin.androidx.compose.koinViewModel
import kotlin.random.Random


@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Preview
@Composable
fun BucketScreen(
	isSelecting: Boolean = false,
	selectedIdList: List<RealmUUID> = listOf(),
	onSelect: (RealmUUID) -> Unit = {},
) {
	val context = LocalContext.current
	val viewModel: BucketScreenViewModel = koinViewModel()
	val scope = rememberCoroutineScope()

	val isAuthenticated = LocalIsAuthenticated.current

	val dataStoreInstance = remember { DataStoreInstance(context = context) }
	val sortBy by dataStoreInstance.getSortBy.collectAsState(initial = null)
	val sortOn by dataStoreInstance.getSortOn.collectAsState(initial = null)
	var bucketFilter by rememberSaveable { mutableStateOf(setOf(BucketType.TODO, BucketType.BOOK, BucketType.SHOW, BucketType.LINK)) }

	val bucketListStatus by viewModel.bucketListStatus.collectAsState()
	var bucketList by remember { mutableStateOf(listOf<BucketObject>()) }
	val bucketItemCount by viewModel.bucketItemCount.collectAsState()

	LaunchedEffect(bucketListStatus, sortBy, sortOn, isAuthenticated) {
		scope.launch(Dispatchers.Default) {
			(when (sortOn) {
				SortOn.Title -> if (sortBy == SortBy.Ascending) bucketListStatus.dataOrNull?.sortedBy { it.title } else bucketListStatus.dataOrNull?.sortedByDescending { it.title }
				SortOn.Timestamp -> if (sortBy == SortBy.Ascending) bucketListStatus.dataOrNull?.sortedBy { it.createdTimestamp } else bucketListStatus.dataOrNull?.sortedByDescending { it.createdTimestamp }
				SortOn.Modified -> if (sortBy == SortBy.Ascending) bucketListStatus.dataOrNull?.sortedBy { it.modifiedTimestamp } else bucketListStatus.dataOrNull?.sortedByDescending { it.modifiedTimestamp }
				SortOn.Custom -> bucketListStatus.dataOrNull
				else -> if (sortBy == SortBy.Ascending) bucketListStatus.dataOrNull?.sortedBy { it.title } else bucketListStatus.dataOrNull?.sortedByDescending { it.title }
			} ?: listOf()).filter { if (it.isLocked) isAuthenticated else true }.let { withContext(Dispatchers.Main) { bucketList = it } }
		}
	}

	fun onClickBucket(id: RealmUUID) {
		if (isSelecting) onSelect(id)
		else Intent(context, BucketActivity::class.java).apply {
			putExtra(Extra.Companion.Extra.BUCKET_ID.name, id.bytes)
			context.startActivity(this)
		}
	}

	fun onLongClickBucket(id: RealmUUID) = onSelect(id)

	val state = rememberReorderableLazyGridState(
		dragCancelledAnimation = SpringDragCancelledAnimation(),
		onMove = { from, to ->
			bucketList.toMutableList().apply {
				add(to.index, removeAt(from.index))
				bucketList = this
			}
		},
		onDragEnd = { from, to ->
			scope.launch(Dispatchers.Default) {
				viewModel.onReorderBucketList(bucketList.map { it.id })
				dataStoreInstance.putSortOn(SortOn.Custom)
			}
		}
	)

	val bottomSheetState = rememberModalBottomSheetState()
	var isBucketBottomSheetVisible by remember { mutableStateOf(false) }

	GenericScaffold2(
		floatingActionButton = {
			BucketFloatingActionButton(isExpanded = true, onClick = { isBucketBottomSheetVisible = true })
		},
		isFloatingActionButtonVisible = !isSelecting,
	) {
		val pullRefreshState = rememberPullRefreshState(
			refreshing = bucketListStatus is ContentStatus.Loading,
			onRefresh = viewModel::refresh
		)

		Box(
			contentAlignment = Alignment.TopCenter,
			modifier = Modifier
				.fillMaxSize()
				.pullRefresh(pullRefreshState)
		) {
			Crossfade(
				targetState = bucketListStatus,
				animationSpec = tween(300),
				label = "bucketListStatus_crossfade"
			) { contentStatus ->
				when (contentStatus) {
					is ContentStatus.Init -> LoadingView()
					is ContentStatus.Loading -> LoadingView()
					is ContentStatus.LoadedEmpty -> EmptyView(
						image = remember { if (Random.nextBoolean()) R.drawable.il_bucket_list_b else R.drawable.il_bucket_list_g },
						title = "I think, therefore, I am",
						subTitle = "― René Descartes",
					)

					is ContentStatus.Loaded -> Column(
						modifier = Modifier.fillMaxSize()
					) {
						BucketListFilter(
							bucketFilter = bucketFilter,
							bucketList = bucketList,
							onClickFilterChip = {
								bucketFilter.toMutableSet().apply {
									if (it in this) remove(it) else add(it)
									bucketFilter = toSet()
								}
							}
						)
						Spacer(modifier = Modifier.height(8.dp))
						LazyVerticalGrid(
							columns = GridCells.Adaptive(144.dp),
							state = state.gridState,
							contentPadding = PaddingValues(horizontal = 10.dp),
							verticalArrangement = Arrangement.spacedBy(4.dp),
							horizontalArrangement = Arrangement.spacedBy(4.dp),
							modifier = Modifier
								.weight(1f)
								.reorderable(state)
						) {
							items(
								items = bucketList.filter { it.bucketType in bucketFilter.map { it.name } },
								key = { it.id.toString() }
							) { bucketObject ->
								ReorderableItem(
									reorderableState = state,
									key = bucketObject.id.toString(),
								) { isDragging ->
									BucketCard(
										title = bucketObject.title,
										bucketSize = bucketItemCount[bucketObject.id] ?: 0,
										bucketType = bucketObject.bucketType.let { BucketType.valueOf(it) },
										isLocked = bucketObject.isLocked,
										isFavourite = bucketObject.isFavourite,
										isSelecting = isSelecting,
										isSelected = bucketObject.id in selectedIdList,
										isDragging = isDragging,
										handle = {
											Icon(
												painter = painterResource(id = R.drawable.ic_fa_grip),
												contentDescription = "Reorder",
												tint = MaterialTheme.colorScheme.onSurface,
												modifier = Modifier
													.size(16.dp)
													.detectReorder(state)
											)
										},
										onClick = { onClickBucket(bucketObject.id) },
										onLongClick = { onLongClickBucket(bucketObject.id) },
									)
								}
							}
						}
					}

					is ContentStatus.Error -> LoadingView()
				}
			}

			PullRefreshIndicator(
				refreshing = bucketListStatus is ContentStatus.Loading,
				state = pullRefreshState,
				backgroundColor = MaterialTheme.colorScheme.background,
				contentColor = MaterialTheme.colorScheme.onBackground,
				scale = true,
			)
		}
	}

	BucketBottomSheet(
		bottomSheetState = bottomSheetState,
		isBottomSheetVisible = isBucketBottomSheetVisible,
		onDismissRequest = { scope.launch { bottomSheetState.hide(); isBucketBottomSheetVisible = false } },
		putBucket = { title, description, bucketType ->
			viewModel.putBucket(title = title, description = description, bucketType = bucketType) {
				scope.launch(Dispatchers.Main) { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
			}
			scope.launch { bottomSheetState.hide(); isBucketBottomSheetVisible = false }
		}
	)
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun BucketListFilter(
	bucketFilter: Set<BucketType> = setOf(),
	bucketList: List<BucketObject> = listOf(),
	onClickFilterChip: (BucketType) -> Unit = {}
) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.horizontalScroll(rememberScrollState())
	) {
		Spacer(modifier = Modifier.width(12.dp))

		FilterChip(
			leadingIcon = {
				Icon(
					painter = painterResource(id = R.drawable.ic_fa_bucket_todo),
					contentDescription = "Todo list",
					modifier = Modifier.requiredSize(16.dp)
				)
			},
			label = { Text(text = "Todo") },
			trailingIcon = { Text(text = bucketList.count { it.bucketType == BucketType.TODO.name }.toString()) },
			selected = BucketType.TODO in bucketFilter,
			onClick = { onClickFilterChip(BucketType.TODO) }
		)

		Spacer(modifier = Modifier.width(8.dp))

		FilterChip(
			leadingIcon = {
				Icon(
					painter = painterResource(id = R.drawable.ic_fa_bucket_book),
					contentDescription = "Book list",
					modifier = Modifier.requiredSize(16.dp)
				)
			},
			label = { Text(text = "Book") },
			trailingIcon = { Text(text = bucketList.count { it.bucketType == BucketType.BOOK.name }.toString()) },
			selected = BucketType.BOOK in bucketFilter,
			onClick = { onClickFilterChip(BucketType.BOOK) }
		)

		Spacer(modifier = Modifier.width(8.dp))

		FilterChip(
			leadingIcon = {
				Icon(
					painter = painterResource(id = R.drawable.ic_fa_bucket_show),
					contentDescription = "Show list",
					modifier = Modifier.requiredSize(16.dp)
				)
			},
			label = { Text(text = "Movies & Series") },
			trailingIcon = { Text(text = bucketList.count { it.bucketType == BucketType.SHOW.name }.toString()) },
			selected = BucketType.SHOW in bucketFilter,
			onClick = { onClickFilterChip(BucketType.SHOW) }
		)

		Spacer(modifier = Modifier.width(8.dp))
		FilterChip(
			leadingIcon = {
				Icon(
					painter = painterResource(id = R.drawable.ic_fa_bucket_link),
					contentDescription = "Link",
					modifier = Modifier.requiredSize(16.dp)
				)
			},
			label = { Text(text = "Link") },
			trailingIcon = { Text(text = bucketList.count { it.bucketType == BucketType.LINK.name }.toString()) },
			selected = BucketType.LINK in bucketFilter,
			onClick = { onClickFilterChip(BucketType.LINK) }
		)

		Spacer(modifier = Modifier.width(12.dp))
	}
}
