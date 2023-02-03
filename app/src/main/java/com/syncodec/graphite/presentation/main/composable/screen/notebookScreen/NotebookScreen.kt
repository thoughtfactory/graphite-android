package com.syncodec.graphite.presentation.main.composable.screen.notebookScreen

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.ChapterObject
import com.syncodec.graphite.presentation.common.ErrorView
import com.syncodec.graphite.presentation.common.LoadingView
import com.syncodec.graphite.presentation.common.scaffold.GenericScaffold
import com.syncodec.graphite.presentation.main.composable.buildingBlock.EmptyView
import com.syncodec.graphite.presentation.main.composable.buildingBlock.NotebookFloatingActionButton
import com.syncodec.graphite.presentation.main.composable.buildingBlock.notebook.NotebookCard
import com.syncodec.graphite.presentation.notebook.NotebookActivity
import com.syncodec.graphite.utils.ContentStatus
import com.syncodec.graphite.utils.DataStoreInstance
import com.syncodec.graphite.utils.Extra
import com.syncodec.graphite.utils.SortBy
import com.syncodec.graphite.utils.SortOn
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
fun NotebookScreen(
	isSelecting : Boolean,
	onSelect : (RealmUUID) -> Unit,
	selectedIdList : List<RealmUUID>,
	onClickFab : () -> Unit,
) {
	val context = LocalContext.current
	val viewModel : NotebookScreenViewModel = koinViewModel()

	val dataStoreInstance = remember { DataStoreInstance(context = context) }
	val sortBy by dataStoreInstance.getSortBy.collectAsState(null)
	val sortOn by dataStoreInstance.getSortOn.collectAsState(null)

	val contentStatus by viewModel.contentStatus.collectAsState()
	val isNotebookRefreshing by viewModel.isNotebookRefreshing.collectAsState()

	val notebookList by viewModel.notebookList.collectAsState()

	var notebookListOrder by remember { mutableStateOf(listOf<ChapterObject>()) }

	LaunchedEffect(key1 = notebookList, key2 = sortOn, key3 = sortBy) {
		when (sortOn) {
			SortOn.Title -> if (sortBy == SortBy.Ascending) notebookList.sortedBy { it.title } else notebookList.sortedByDescending { it.title }
			SortOn.Timestamp -> if (sortBy == SortBy.Ascending) notebookList.sortedBy { it.createdTimestamp } else notebookList.sortedByDescending { it.createdTimestamp }
			SortOn.Modified -> if (sortBy == SortBy.Ascending) notebookList.sortedBy { it.modifiedTimestamp } else notebookList.sortedByDescending { it.modifiedTimestamp }
			SortOn.CUSTOM -> notebookList
			else -> if (sortBy == SortBy.Ascending) notebookList.sortedBy { it.title } else notebookList.sortedByDescending { it.title }
		}.apply { notebookListOrder = this }
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


	fun onClickNotebook(id : RealmUUID) {
		if (isSelecting) {
			onSelect(id)
		} else {
			Intent(context, NotebookActivity::class.java).apply {
				putExtra(Extra.Companion.Extra.ChapterId.name, id.bytes)
				putExtra(Extra.Companion.Extra.Filter.name, Extra.Companion.Filter.READ_CHAPTER.name)
				activityLauncher.launch(this)
			}
		}
	}

	fun onLongClickNotebook(id : RealmUUID) = onSelect(id)

	val state = rememberReorderableLazyGridState(
		dragCancelledAnimation = SpringDragCancelledAnimation(),
		onMove = { from, to ->
			notebookListOrder.toMutableList().apply {
				add(to.index, removeAt(from.index))
				notebookListOrder = this
			}
		},
		onDragEnd = { from, to ->
			viewModel.onReorderNotebookList(notebookListOrder)
			dataStoreInstance.putSortOn(SortOn.CUSTOM)
		}
	)

	GenericScaffold(
		floatingActionButton = {
			NotebookFloatingActionButton(isExpanded = true, onClick = onClickFab)
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
					state = rememberSwipeRefreshState(isRefreshing = isNotebookRefreshing),
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
							items = notebookListOrder,
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
									isSelected = notebook.id in selectedIdList,
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
}
