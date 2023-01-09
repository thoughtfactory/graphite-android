package com.syncodec.graphite.presentation.main.composable.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.syncodec.graphite.di.model.BucketObject
import com.syncodec.graphite.di.model.ChapterObject
import com.syncodec.graphite.presentation.common.LocalCompositionIsSelected
import com.syncodec.graphite.presentation.common.LocalCompositionSelectedObjectIdList
import com.syncodec.graphite.presentation.main.composable.LocalCompositionIsNotebookRefreshing
import com.syncodec.graphite.presentation.main.composable.LocalCompositionOnRefresh
import com.syncodec.graphite.presentation.main.composable.buildingBlock.NotebookFloatingActionButton
import com.syncodec.graphite.presentation.main.composable.buildingBlock.notebook.NotebookCard
import com.syncodec.graphite.utils.DataStoreInstance
import com.syncodec.graphite.utils.LocalVaultIsOpened
import com.syncodec.graphite.utils.SortBy
import com.syncodec.graphite.utils.SortOn
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.SpringDragCancelledAnimation
import org.burnoutcrew.reorderable.detectReorder
import org.burnoutcrew.reorderable.rememberReorderableLazyGridState
import org.burnoutcrew.reorderable.reorderable


@OptIn(
	ExperimentalMaterial3Api::class,
	ExperimentalAnimationApi::class
)
@Composable
fun NotebookScreen(
	notebookList : List<ChapterObject>,
	notebookOrderList: List<RealmUUID>,
	sortOn : SortOn,
	sortBy : SortBy,
	onReorderNotebookList: (List<RealmUUID>) -> Unit,
	onClickFab : () -> Unit,
	onClickNotebook : (RealmUUID) -> Unit,
	onLongClickNotebook : (RealmUUID) -> Unit
) {
	val context = LocalContext.current
	val scope = rememberCoroutineScope()
	val dataStoreInstance = remember { DataStoreInstance(context = context) }

	val isNotebookRefreshing = LocalCompositionIsNotebookRefreshing.current
	val onRefresh = LocalCompositionOnRefresh.current

	val isSelected = LocalCompositionIsSelected.current
	val selectedRealmUUIDList = LocalCompositionSelectedObjectIdList.current

	val _notebookList : SnapshotStateList<ChapterObject> = remember { mutableStateListOf() }

	LaunchedEffect(key1 = notebookList, key2 = sortOn, key3 = sortBy) {
		_notebookList.clear()
		when (sortOn) {
			SortOn.TITLE -> if (sortBy == SortBy.ASCENDING) notebookList.sortedBy { it.title } else notebookList.sortedByDescending { it.title }
			SortOn.TIMESTAMP -> if (sortBy == SortBy.ASCENDING) notebookList.sortedBy { it.createdTimestamp } else notebookList.sortedByDescending { it.createdTimestamp }
			SortOn.MODIFIED -> if (sortBy == SortBy.ASCENDING) notebookList.sortedBy { it.modifiedTimestamp } else notebookList.sortedByDescending { it.modifiedTimestamp }
			SortOn.CUSTOM -> {
				notebookOrderList.forEach { realmUUID ->
					notebookList.firstOrNull { it.id == realmUUID }?.let { _notebookList.add(it) }
				}
				notebookList.filter { it.id !in notebookOrderList }
			}
			else -> if (sortBy == SortBy.ASCENDING) notebookList.sortedBy { it.title } else notebookList.sortedByDescending { it.title }
		}.apply { _notebookList.addAll(this) }
	}

	val state = rememberReorderableLazyGridState(
		dragCancelledAnimation = SpringDragCancelledAnimation(),
		onMove = { from, to ->
			_notebookList.apply { add(to.index, removeAt(from.index)) }
		},
		onDragEnd = { from, to ->
			scope.launch(Dispatchers.Default) { onReorderNotebookList(_notebookList.map { it.id }) }
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
				NotebookFloatingActionButton(isExpanded = true, onClick = onClickFab)
			}
		}
	) {
		Box(
			modifier = Modifier
				.fillMaxSize()
				.padding(it)
		) {
			SwipeRefresh(
				state = rememberSwipeRefreshState(isRefreshing = isNotebookRefreshing == true),
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
						items = _notebookList,
						key = { it.id.toString() }
					) { notebook ->
						ReorderableItem(
							reorderableState = state,
							key = notebook.id.toString(),
						) { isDragging ->
							NotebookCard(
								handleModifier = Modifier.detectReorder(state),
								title = notebook.title,
								color = notebook.color?.let { it1 -> Color(it1) },
								thumbnail = notebook.thumbnail,
								isSelected = notebook.id in selectedRealmUUIDList,
								isDragging = isDragging,
								onClick = { onClickNotebook(notebook.id) },
								onLongClick = { onLongClickNotebook(notebook.id) }
							)
						}
					}
				}
			}
		}
	}
}
