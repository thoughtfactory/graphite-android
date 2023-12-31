package com.syncodec.graphite.presentation.main.composable.screen.bucketScreen

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.local.BucketObjectLite
import com.syncodec.graphite.di.model.local.BucketType
import com.syncodec.graphite.di.repository.group.isAll
import com.syncodec.graphite.presentation.base.ANIMATION_DURATION_MILLIS
import com.syncodec.graphite.presentation.base.LocalAppDataStore
import com.syncodec.graphite.presentation.bucket.BucketActivity
import com.syncodec.graphite.presentation.common.LoadingView
import com.syncodec.graphite.presentation.common.component.bucket.BucketCard
import com.syncodec.graphite.presentation.common.dialog.dialog2.DeleteDialog
import com.syncodec.graphite.presentation.common.reorderable.ReorderableItem
import com.syncodec.graphite.presentation.common.reorderable.SpringDragCancelledAnimation
import com.syncodec.graphite.presentation.common.reorderable.detectReorder
import com.syncodec.graphite.presentation.common.reorderable.lazyState.rememberReorderableLazyGridState
import com.syncodec.graphite.presentation.common.reorderable.reorderable
import com.syncodec.graphite.presentation.common.scaffold.GenericScaffold2
import com.syncodec.graphite.presentation.common.selectionAction.MainSelectionActionView
import com.syncodec.graphite.presentation.main.composable.bottomSheet.BucketBottomSheet
import com.syncodec.graphite.presentation.main.composable.buildingBlock.BucketFloatingActionButton
import com.syncodec.graphite.presentation.main.composable.buildingBlock.EmptyView
import com.syncodec.graphite.utils.Extra
import com.syncodec.graphite.utils.SortOn
import com.syncodec.graphite.utils.isTablet
import com.syncodec.graphite.utils.xor
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import kotlin.random.Random


@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun BucketScreen(
	isSelecting: Boolean = false,
	selectedIdList: Set<RealmUUID> = setOf(),
	onSelect: (RealmUUID) -> Unit = {},
	onUnSelectAll: () -> Unit = {},
) {
	val context = LocalContext.current
	val viewModel: BucketScreenViewModel = koinViewModel()
	val scope = rememberCoroutineScope()

	val appDataStore = LocalAppDataStore.current

	var bucketFilter by rememberSaveable { mutableStateOf(setOf(BucketType.TODO, BucketType.BOOK, BucketType.SHOW, BucketType.LINK)) }
	val bucketList by viewModel.bucketList.collectAsState()
	var orderedBucketList by remember { mutableStateOf<List<BucketObjectLite>?>(null) }
	LaunchedEffect(key1 = bucketList) { orderedBucketList = bucketList?.toList() }

	BackHandler(enabled = isSelecting) { onUnSelectAll() }

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
			orderedBucketList?.toMutableList()?.apply {
				add(to.index, removeAt(from.index))
				orderedBucketList = this
			}
		},
		onDragEnd = { from, to ->
			scope.launch(Dispatchers.Default) {
				viewModel.onReorderBucketList(orderedBucketList?.map { it.id } ?: listOf())
				appDataStore.putSortOn(SortOn.Custom)
			}
		}
	)

	val bottomSheetState = rememberModalBottomSheetState()
	var isBucketBottomSheetVisible by remember { mutableStateOf(false) }

	var isDeleteDialogVisible by remember { mutableStateOf(false) }

	GenericScaffold2(
		floatingActionButton = {
			BucketFloatingActionButton(isExpanded = true) { isBucketBottomSheetVisible = true }
		},
		isFloatingActionButtonVisible = !isSelecting,
	) {
		when {
			orderedBucketList == null -> LoadingView()
			orderedBucketList!!.isEmpty() -> EmptyView(
				image = remember { if (Random.nextBoolean()) R.drawable.il_bucket_list_b else R.drawable.il_bucket_list_g },
				title = "I think, therefore, I am",
				subTitle = "― René Descartes",
				modifier = Modifier.fillMaxSize()
			)

			else -> Column(
				modifier = Modifier.fillMaxSize()
			) {
				BucketListFilter(
					bucketFilter = bucketFilter,
					bucketList = orderedBucketList ?: listOf(),
					isSelecting = isSelecting,
					onClickFilterChip = { bucketFilter.toMutableSet().apply { xor(it); bucketFilter = toSet() } }
				)
				Spacer(modifier = Modifier.height(8.dp))
				LazyVerticalGrid(
					columns = GridCells.Adaptive(if (isTablet()) 256.dp else 144.dp),
					state = state.gridState,
					contentPadding = PaddingValues(horizontal = 10.dp),
					verticalArrangement = Arrangement.spacedBy(4.dp),
					horizontalArrangement = Arrangement.spacedBy(4.dp),
					modifier = Modifier
						.weight(1f)
						.reorderable(state)
				) {
					items(
						items = orderedBucketList?.filter { it.bucketType in bucketFilter } ?: listOf(),
						contentType = { 0 },
						key = { it.id.toString() }
					) { bucketObject ->
						ReorderableItem(
							reorderableState = state,
							key = bucketObject.id.toString(),
						) { isDragging ->
							BucketCard(
								title = bucketObject.title,
								bucketSize = bucketObject.bucketItemCount,
								bucketType = bucketObject.bucketType,
								isLocked = bucketObject.isLocked,
								isFavourite = bucketObject.isFavourite,
								selected = bucketObject.id in selectedIdList,
								isDragging = isDragging,
								handle = {
									androidx.compose.animation.AnimatedVisibility(
										visible = !isSelecting,
										enter = scaleIn(tween(ANIMATION_DURATION_MILLIS)),
										exit = scaleOut(tween(ANIMATION_DURATION_MILLIS))
									) {
										Icon(
											painter = painterResource(id = R.drawable.ic_fa_grip),
											contentDescription = "Reorder",
											tint = MaterialTheme.colorScheme.onSurface,
											modifier = Modifier
												.size(16.dp)
												.detectReorder(state)
										)
									}
								},
								onClick = { onClickBucket(bucketObject.id) },
								onLongClick = { onLongClickBucket(bucketObject.id) },
							)
						}
					}
				}
			}
		}

		val isAllFavourite by isAll(isSelecting = isSelecting, selectedIdList = selectedIdList, objectList = orderedBucketList, idGetter = BucketObjectLite::id, propGetter = BucketObjectLite::isFavourite)
		val isAllLocked by isAll(isSelecting = isSelecting, selectedIdList = selectedIdList, objectList = orderedBucketList, idGetter = BucketObjectLite::id, propGetter = BucketObjectLite::isLocked)
		MainSelectionActionView(
			modifier = Modifier
				.align(Alignment.BottomCenter)
				.padding(start = 24.dp, top = 0.dp, end = 24.dp, bottom = 32.dp),
			isSelecting = isSelecting,
			isAllItemFavourite = isAllFavourite,
			isAllItemLocked = isAllLocked,
			selectedItemCount = selectedIdList.size,
			onClickDelete = { isDeleteDialogVisible = true },
			onClickFavourite = { viewModel.onClickMultiFavourite(idList = selectedIdList, isAllFavourite = isAllFavourite) },
			onClickLock = { viewModel.onClickMultiLock(idList = selectedIdList, isAllLocked = isAllLocked) },
		)

		DeleteDialog(
			isDialogVisible = isDeleteDialogVisible,
			onDismissRequest = { isDeleteDialogVisible = false },
			title = stringResource(id = R.string.delete_items_multiple),
			contentText = stringResource(id = R.string.are_you_sure_delete_selected_bucket),
			onConfirmDelete = { isDeleteDialogVisible = false; viewModel.delete(selectedIdList); onUnSelectAll() },
		)
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

@Preview
@Composable
private fun BucketListFilter(
	bucketFilter: Set<BucketType> = setOf(),
	bucketList: List<BucketObjectLite> = listOf(),
	isSelecting: Boolean = false,
	onClickFilterChip: (BucketType) -> Unit = {}
) {
	AnimatedVisibility(
		visible = !isSelecting,
		enter = expandVertically(tween(470)),
		exit = shrinkVertically(tween(470))
	) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.horizontalScroll(rememberScrollState())
		) {
			Spacer(modifier = Modifier.width(12.dp))

			BucketFilterChip(
				icon = R.drawable.ic_fa_bucket_todo,
				text = stringResource(id = R.string.todo),
				bucketCount = bucketList.count { it.bucketType == BucketType.TODO },
				isSelected = BucketType.TODO in bucketFilter,
				onClick = { onClickFilterChip(BucketType.TODO) }
			)

			Spacer(modifier = Modifier.width(8.dp))

			BucketFilterChip(
				icon = R.drawable.ic_fa_bucket_book,
				text = stringResource(id = R.string.book),
				bucketCount = bucketList.count { it.bucketType == BucketType.BOOK },
				isSelected = BucketType.BOOK in bucketFilter,
				onClick = { onClickFilterChip(BucketType.BOOK) }
			)

			Spacer(modifier = Modifier.width(8.dp))

			BucketFilterChip(
				icon = R.drawable.ic_fa_bucket_show,
				text = stringResource(id = R.string.show),
				bucketCount = bucketList.count { it.bucketType == BucketType.SHOW },
				isSelected = BucketType.SHOW in bucketFilter,
				onClick = { onClickFilterChip(BucketType.SHOW) }
			)

			Spacer(modifier = Modifier.width(8.dp))

			BucketFilterChip(
				icon = R.drawable.ic_fa_bucket_link,
				text = stringResource(id = R.string.link),
				bucketCount = bucketList.count { it.bucketType == BucketType.LINK },
				isSelected = BucketType.LINK in bucketFilter,
				onClick = { onClickFilterChip(BucketType.LINK) }
			)

			Spacer(modifier = Modifier.width(12.dp))
		}
	}
}

@Composable
@Preview
private fun BucketFilterChip(
	icon: Int = R.drawable.ic_fa_bucket_link,
	text: String = stringResource(id = R.string.link),
	bucketCount: Int = 0,
	isSelected: Boolean = false,
	onClick: () -> Unit = {},
) {
	FilterChip(
		leadingIcon = {
			Icon(
				painter = painterResource(id = icon),
				contentDescription = text,
				tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.69f),
				modifier = Modifier.requiredSize(16.dp)
			)
		},
		label = { Text(text = text) },
		trailingIcon = { Text(text = "$bucketCount") },
		selected = isSelected,
		colors = FilterChipDefaults.filterChipColors(
			containerColor = Color.Transparent,
			labelColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.69f),
			iconColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.69f),
			selectedContainerColor = MaterialTheme.colorScheme.primary,
			selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
			selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary,
			selectedTrailingIconColor = MaterialTheme.colorScheme.onPrimary,
		),
		onClick = onClick
	)
}
