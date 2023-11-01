package com.syncodec.graphite.presentation.main.composable.screen.notebookScreen

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.local.ChapterObject
import com.syncodec.graphite.di.repository.group.isAll
import com.syncodec.graphite.presentation.common.LoadingView
import com.syncodec.graphite.presentation.common.dialog.dialog2.DeleteDialog
import com.syncodec.graphite.presentation.common.reorderable.ReorderableItem
import com.syncodec.graphite.presentation.common.reorderable.SpringDragCancelledAnimation
import com.syncodec.graphite.presentation.common.reorderable.detectReorder
import com.syncodec.graphite.presentation.common.reorderable.lazyState.rememberReorderableLazyGridState
import com.syncodec.graphite.presentation.common.reorderable.reorderable
import com.syncodec.graphite.presentation.common.scaffold.GenericScaffold2
import com.syncodec.graphite.presentation.common.selectionAction.MainSelectionActionView
import com.syncodec.graphite.presentation.main.composable.bottomSheet.ChapterBottomSheet
import com.syncodec.graphite.presentation.main.composable.buildingBlock.EmptyView
import com.syncodec.graphite.presentation.main.composable.buildingBlock.NotebookFloatingActionButton
import com.syncodec.graphite.presentation.main.composable.buildingBlock.notebook.NotebookCard
import com.syncodec.graphite.presentation.notebook.NotebookActivity
import com.syncodec.graphite.utils.Extra
import com.syncodec.graphite.utils.SortOn
import com.syncodec.graphite.utils.dataStore.DataStoreInstance
import com.syncodec.graphite.utils.isTablet
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import kotlin.random.Random


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotebookScreen(
	isSelecting: Boolean = false,
	selectedIdList: Set<RealmUUID> = setOf(),
	onSelect: (RealmUUID) -> Unit = {},
	onUnSelectAll: () -> Unit = {},
) {
	val context = LocalContext.current
	val viewModel: NotebookScreenViewModel = koinViewModel()
	val scope = rememberCoroutineScope()

	val dataStoreInstance = remember { DataStoreInstance(context = context) }

	val notebookList by viewModel.notebookList.collectAsState()
	var orderedNotebookList by remember { mutableStateOf<List<ChapterObject>?>(null) }
	LaunchedEffect(key1 = notebookList) {
		orderedNotebookList = notebookList
	}

	fun onClickNotebook(id: RealmUUID) {
		if (isSelecting) onSelect(id)
		else Intent(context, NotebookActivity::class.java).apply {
			putExtra(Extra.Companion.Extra.ChapterId.name, id.bytes)
			putExtra(Extra.Companion.Extra.Filter.name, Extra.Companion.Filter.READ_CHAPTER.name)
			context.startActivity(this)
		}
	}

	fun onLongClickNotebook(id: RealmUUID) = onSelect(id)

	val state = rememberReorderableLazyGridState(
		dragCancelledAnimation = SpringDragCancelledAnimation(),
		onMove = { from, to ->
			orderedNotebookList?.toMutableList()?.apply {
				add(to.index, removeAt(from.index))
				orderedNotebookList = this
			}
		},
		onDragEnd = { from, to ->
			scope.launch(Dispatchers.Default) {
				viewModel.onReorderNotebookList(orderedNotebookList?.map { it.id } ?: listOf())
				dataStoreInstance.putSortOn(SortOn.Custom)
			}
		}
	)

	val bottomSheetState = rememberModalBottomSheetState()
	var isNotebookBottomSheetVisible by remember { mutableStateOf(false) }

	var isDeleteDialogVisible by remember { mutableStateOf(false) }

	GenericScaffold2(
		floatingActionButton = {
			NotebookFloatingActionButton(isExpanded = true) { isNotebookBottomSheetVisible = true }
		},
		isFloatingActionButtonVisible = !isSelecting,
	) {

		when {
			orderedNotebookList == null -> LoadingView()
			orderedNotebookList!!.isEmpty() -> EmptyView(
				image = remember { if (Random.nextBoolean()) R.drawable.il_bucket_list_b else R.drawable.il_bucket_list_g },
				title = "I think, therefore, I am",
				subTitle = "― René Descartes",
			)

			else -> LazyVerticalGrid(
				columns = GridCells.Adaptive(if (isTablet()) 256.dp else 144.dp),
				state = state.gridState,
				contentPadding = PaddingValues(horizontal = 8.dp),
				verticalArrangement = Arrangement.spacedBy(4.dp),
				horizontalArrangement = Arrangement.spacedBy(4.dp),
				modifier = Modifier
					.fillMaxSize()
					.reorderable(state)
			) {
				items(
					items = orderedNotebookList!!,
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
		}

		val isAllFavourite by isAll(isSelecting = isSelecting, selectedIdList = selectedIdList, objectList = orderedNotebookList, idGetter = ChapterObject::id, propGetter = ChapterObject::isFavourite)
		val isAllLocked by isAll(isSelecting = isSelecting, selectedIdList = selectedIdList, objectList = orderedNotebookList, idGetter = ChapterObject::id, propGetter = ChapterObject::isLocked)
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

	ChapterBottomSheet(
		bottomSheetState = bottomSheetState,
		isBottomSheetVisible = isNotebookBottomSheetVisible,
		onDismissRequest = { scope.launch { bottomSheetState.hide(); isNotebookBottomSheetVisible = false } },
		title = stringResource(id = R.string.new_notebook),
		putChapter = { _, title, description, color, bitmap -> viewModel.putNotebook(title = title, description = description, color = color, bitmap = bitmap) },
	)
}
