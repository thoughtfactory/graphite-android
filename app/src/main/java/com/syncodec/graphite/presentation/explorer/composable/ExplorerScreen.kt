package com.syncodec.graphite.presentation.explorer.composable

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.di.model.ChapterObjectLite
import com.syncodec.graphite.di.model.NoteObjectLite
import com.syncodec.graphite.presentation.base.LocalAppDataStore
import com.syncodec.graphite.presentation.common.LoadingView
import com.syncodec.graphite.presentation.common.animation.AnimatedText
import com.syncodec.graphite.presentation.common.dialog.where.whereChapterDialog2.WhereChapterDialog2
import com.syncodec.graphite.presentation.common.scaffold.GenericScaffold2
import com.syncodec.graphite.presentation.common.selectionAction.ExplorerSelectionActionView
import com.syncodec.graphite.presentation.explorer.composable.bar.BottomBar
import com.syncodec.graphite.presentation.explorer.composable.bar.TopBar
import com.syncodec.graphite.presentation.main.composable.screen.noteScreen.buildingBlock.noteGrid.NoteGrid
import com.syncodec.graphite.presentation.main.composable.screen.noteScreen.buildingBlock.noteList.NoteList
import com.syncodec.graphite.utils.ViewType
import io.realm.kotlin.types.RealmUUID


@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun ExplorerScreen(
	screenTitle: String = "Explorer",
	bottomSheetTitle: String = "In visible region",
	currentChapter: ChapterObjectLite? = null,
	noteList: List<NoteObjectLite> = listOf(),
	onExploreChapter: (RealmUUID?) -> Unit = {},
	content: @Composable BoxScope.() -> Unit = {}
) {
	var isWhereDialogVisible by remember { mutableStateOf(false) }

	GenericScaffold2(
		topBar = { TopBar(title = screenTitle) },
		bottomBar = {
			BottomBar(
				currentChapter = currentChapter,
				onClickSearchIn = { isWhereDialogVisible = true },
			)
		},
		dialogContent = {
			WhereChapterDialog2(
				isDialogVisible = isWhereDialogVisible,
				onDismissRequest = { isWhereDialogVisible = false },
				currentSelectedChapter = currentChapter?.id,
				onSelectChapter = onExploreChapter,
			)
		},
	) {
		ExplorerView(
			bottomSheetTitle = bottomSheetTitle,
			noteList = noteList,
			content = content,
		)
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun ExplorerView(
	bottomSheetTitle: String = "In visible region",
	noteList: List<NoteObjectLite> = listOf(),
	isSelecting: Boolean = false,
	selectedIdList: Set<RealmUUID> = setOf(),
	onSelect: (RealmUUID) -> Unit = {},
	onClickNote: (RealmUUID) -> Unit = {},
	onClickDelete: () -> Unit = {},
	onClickFavourite: () -> Unit = {},
	onClickLock: () -> Unit = {},
	content: @Composable BoxScope.() -> Unit = {}
) {
	val dataStoreInstance = LocalAppDataStore.current
	val viewType by dataStoreInstance.getViewType.collectAsState(null)

	BottomSheetScaffold(
		sheetShape = RectangleShape,
		sheetContainerColor = MaterialTheme.colorScheme.background,
		containerColor = MaterialTheme.colorScheme.background,
		sheetDragHandle = {
			SheetHeader(
				title = bottomSheetTitle,
				noteCount = noteList.size,
			)
		},
		sheetContent = {
			Box(
				modifier = Modifier.fillMaxSize()
			) {
				if (noteList.isEmpty()) Spacer(modifier = Modifier.height(24.dp))
				else AnimatedContent(
					targetState = viewType,
					transitionSpec = { (fadeIn(tween(470)) + scaleIn(tween(470), 0.71f)).togetherWith(fadeOut(tween(470)) + scaleOut(tween(470), 0.71f)) },
					modifier = Modifier.fillMaxSize(),
					label = "viewType_animation"
				) { viewType1 ->
					when (viewType1) {
						ViewType.List -> NoteList(
//						    lazyListState = rememberLazyListState(),
							noteList = noteList,
							tagList = listOf(),
							isSelecting = isSelecting,
							selectedIdList = selectedIdList,
							onClickNote = onClickNote,
							onLongClickNote = onSelect,
							modifier = Modifier.fillMaxSize(),
						)

						ViewType.Grid -> NoteGrid(
//						    lazyListState = rememberLazyListState(),
							noteList = noteList,
							tagList = listOf(),
							isSelecting = isSelecting,
							selectedIdList = selectedIdList,
							onClickNote = onClickNote,
							onLongClickNote = onSelect,
							modifier = Modifier.fillMaxSize(),
						)

						else -> LoadingView()
					}
				}

				ExplorerSelectionActionView(
					isSelecting = isSelecting,
					isAllItemFavourite = false,
					isAllItemLocked = false,
					selectedItemCount = selectedIdList.size,
					onClickDelete = onClickDelete,
					onClickFavourite = onClickFavourite,
					onClickLock = onClickLock,
					modifier = Modifier
						.padding(start = 24.dp, end = 24.dp, bottom = 24.dp)
						.align(Alignment.BottomCenter),
				)
			}
		},
	) {
		Box(modifier = Modifier.padding(it), content = content)
	}
}

@Preview
@Composable
private fun SheetHeader(
	title: String = "In visible region",
	noteCount: Int = 0,
) {
	Column(
		modifier = Modifier
			.fillMaxWidth()
			.background(MaterialTheme.colorScheme.background)
	) {
		Divider()
		Row(
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier
				.fillMaxWidth()
				.height(56.dp)
		) {
			Spacer(modifier = Modifier.width(24.dp))
			Text(
				text = title,
				style = MaterialTheme.typography.titleMedium,
				fontWeight = FontWeight.Bold,
			)
			Spacer(modifier = Modifier.weight(1f))
			AnimatedText(
				text = "$noteCount notes",
				style = MaterialTheme.typography.titleSmall,
				fontWeight = FontWeight.Bold,
			)
			Spacer(modifier = Modifier.width(24.dp))
		}
	}
}
