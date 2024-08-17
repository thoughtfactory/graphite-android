package com.syncodec.graphite.presentation.main.composable.screen.notebookScreen

import android.content.Intent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.ChapterObject
import com.syncodec.graphite.presentation.common.LoadingView
import com.syncodec.graphite.presentation.common.scaffold.GenericScaffold
import com.syncodec.graphite.presentation.main.composable.buildingBlock.EmptyView
import com.syncodec.graphite.presentation.main.composable.buildingBlock.NotebookFloatingActionButton
import com.syncodec.graphite.presentation.main.composable.buildingBlock.notebook.NotebookCard
import com.syncodec.graphite.presentation.notebook.NotebookActivity
import com.syncodec.graphite.utils.ContentStatus
import com.syncodec.graphite.utils.DataStoreInstance
import com.syncodec.graphite.utils.Extra
import com.syncodec.graphite.utils.LocalIsAuthenticated
import com.syncodec.graphite.utils.SortOrder
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
	val scope = rememberCoroutineScope()

	val isAuthenticated = LocalIsAuthenticated.current

	val dataStoreInstance = remember { DataStoreInstance(context = context) }
	val sortBy by dataStoreInstance.getSortOrder.collectAsState(null)
	val sortOn by dataStoreInstance.getSortOn.collectAsState(null)

	val notebookListStatus by viewModel.notebookListStatus.collectAsState()
	var notebookList by remember { mutableStateOf(listOf<ChapterObject>()) }

	LaunchedEffect(notebookListStatus, sortBy, sortOn, isAuthenticated) {
		scope.launch(Dispatchers.Default) {
			(when (sortOn) {
				SortOn.Title -> if (sortBy == SortOrder.Ascending) notebookListStatus.dataOrNull?.sortedBy { it.title } else notebookListStatus.dataOrNull?.sortedByDescending { it.title }
				SortOn.CreatedTimestamp -> if (sortBy == SortOrder.Ascending) notebookListStatus.dataOrNull?.sortedBy { it.createdTimestamp } else notebookListStatus.dataOrNull?.sortedByDescending { it.createdTimestamp }
				SortOn.ModifiedTimestamp -> if (sortBy == SortOrder.Ascending) notebookListStatus.dataOrNull?.sortedBy { it.modifiedTimestamp } else notebookListStatus.dataOrNull?.sortedByDescending { it.modifiedTimestamp }
				SortOn.Custom -> notebookListStatus.dataOrNull
				else -> if (sortBy == SortOrder.Ascending) notebookListStatus.dataOrNull?.sortedBy { it.title } else notebookListStatus.dataOrNull?.sortedByDescending { it.title }
			} ?: listOf()).filter { if (it.isLocked) isAuthenticated else true }.let { withContext(Dispatchers.Main) { notebookList = it } }
		}
	}

	fun onClickNotebook(id : RealmUUID) {
		if (isSelecting) onSelect(id)
		else Intent(context, NotebookActivity::class.java).apply {
			putExtra(Extra.Companion.Extra.ChapterId.name, id.bytes)
			putExtra(Extra.Companion.Extra.Filter.name, Extra.Companion.Filter.READ_CHAPTER.name)
			context.startActivity(this)
		}
	}

	fun onLongClickNotebook(id : RealmUUID) = onSelect(id)

	val state = rememberReorderableLazyGridState(
		dragCancelledAnimation = SpringDragCancelledAnimation(),
		onMove = { from, to ->
			notebookList.toMutableList().apply {
				add(to.index, removeAt(from.index))
				notebookList = this
			}
		},
		onDragEnd = { from, to ->
			scope.launch(Dispatchers.Default) {
				viewModel.onReorderBucketList(notebookList.map { it.id })
				dataStoreInstance.putSortOn(SortOn.Custom)
			}
		}
	)

	GenericScaffold(
		floatingActionButton = {
			NotebookFloatingActionButton(isExpanded = true, onClick = onClickFab)
		},
		isFloatingActionButtonVisible = ! isSelecting,
	) {
		val pullRefreshState = rememberPullRefreshState(
			refreshing = notebookListStatus is ContentStatus.Loading,
			onRefresh = viewModel::refresh
		)

		Box(
			contentAlignment = Alignment.TopCenter,
			modifier = Modifier
				.fillMaxSize()
				.pullRefresh(pullRefreshState),
		) {
			Crossfade(
				targetState = notebookListStatus,
				animationSpec = tween(300)
			) { contentStatus ->
				when (contentStatus) {
					is ContentStatus.Init -> LoadingView()
					is ContentStatus.Loading -> LoadingView()
					is ContentStatus.LoadedEmpty -> EmptyView(
						image = remember { if (Random.nextBoolean()) R.drawable.il_bucket_list_b else R.drawable.il_bucket_list_g },
						title = "I think, therefore, I am",
						subTitle = "― René Descartes",
					)

					is ContentStatus.Loaded -> LazyVerticalGrid(
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
							items = notebookList,
							key = { it.id.toString() }
						) { chapterObject ->
							ReorderableItem(
								reorderableState = state,
								key = chapterObject.id.toString(),
							) { isDragging ->
								NotebookCard(
									handleModifier = Modifier.detectReorder(state),
									title = chapterObject.title,
									color = chapterObject.color?.let { it1 -> Color(it1) },
									thumbnail = chapterObject.thumbnail,
									isSelected = chapterObject.id in selectedIdList,
									isDragging = isDragging,
									onClick = { onClickNotebook(chapterObject.id) },
									onLongClick = { onLongClickNotebook(chapterObject.id) }
								)
							}
						}
					}

					is ContentStatus.Error -> LoadingView()
				}
			}

			PullRefreshIndicator(
				refreshing = notebookListStatus is ContentStatus.Loading,
				state = pullRefreshState,
				backgroundColor = MaterialTheme.colorScheme.background,
				contentColor = MaterialTheme.colorScheme.onBackground,
				scale = true,
			)
		}
	}
}
