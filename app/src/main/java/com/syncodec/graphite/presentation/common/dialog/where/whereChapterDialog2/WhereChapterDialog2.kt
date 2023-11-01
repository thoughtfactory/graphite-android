package com.syncodec.graphite.presentation.common.dialog.where.whereChapterDialog2

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.base.ANIMATION_DURATION_MILLIS
import com.syncodec.graphite.presentation.common.component.chapter.chapterList
import com.syncodec.graphite.presentation.common.dialog.where.whereChapterDialog2.bar.BottomBar
import com.syncodec.graphite.presentation.common.dialog.where.whereChapterDialog2.bar.TopBar
import com.syncodec.graphite.presentation.common.scaffold.GenericScaffold2
import com.syncodec.graphite.presentation.main.composable.bottomSheet.ChapterBottomSheet
import com.syncodec.graphite.presentation.main.composable.buildingBlock.EmptyView
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel


/**
 * Auto dismissible
 */
@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun WhereChapterDialog2(
	isDialogVisible: Boolean = true,
	onDismissRequest: () -> Unit = {},
	currentSelectedChapter: RealmUUID? = null,
	showEveryWhere : Boolean = true,
	onSelectChapter: (RealmUUID?) -> Unit = {}
) {
	val scope = rememberCoroutineScope()
	val viewModel: WhereChapterDialogViewModel2 = koinViewModel()

	val chapterPath by viewModel.currentChapterPath.collectAsState()
	val childChapterList by viewModel.childChapterObject.collectAsState()
	val childChapterCountMap by viewModel.childChapterCountMap.collectAsState()
	val childNoteCountMap by viewModel.childNoteCountMap.collectAsState()

	LaunchedEffect(key1 = currentSelectedChapter) { viewModel.exploreChapter(currentSelectedChapter) }

	BackHandler(enabled = isDialogVisible) { onDismissRequest() }
	BackHandler(enabled = isDialogVisible && chapterPath.isNotEmpty()) { viewModel.exploreChapter(chapterPath.getOrNull(1)?.id) }

	val bottomSheetState = rememberModalBottomSheetState()
	var isChapterBottomSheetVisible by remember { mutableStateOf(false) }

	AnimatedVisibility(
		visible = isDialogVisible,
		enter = slideInVertically(tween(ANIMATION_DURATION_MILLIS)) { it / 2 } + fadeIn(tween(ANIMATION_DURATION_MILLIS)),
		exit = slideOutVertically(tween(ANIMATION_DURATION_MILLIS)) { it / 2 } + fadeOut(tween(ANIMATION_DURATION_MILLIS))
	) {
		GenericScaffold2(
			topBar = {
				TopBar(
					chapterPath = chapterPath,
					onClickChapter = { viewModel.exploreChapter(it) },
					onClickCancel = onDismissRequest
				)
			},
			bottomBar = {
				BottomBar(
					showEveryWhere = showEveryWhere,
					onClickSelect = {
						onSelectChapter(chapterPath.getOrNull(0)?.id)
						onDismissRequest()
					},
					onClickEverywhere = {
						onSelectChapter(null)
						onDismissRequest()
					},
				)
			},
			floatingActionButton = {
				FloatingActionButton(
					onClick = { isChapterBottomSheetVisible = true }
				) {
					Icon(
						painter = painterResource(id = R.drawable.ic_fa_notebook),
						contentDescription = stringResource(id = R.string.new_chapter),
						modifier = Modifier.requiredSize(16.dp)
					)
				}
			}
		) {
			if (childChapterList.isEmpty()) EmptyView(
				image = R.drawable.il_empty_chapter,
				title = "No chapters found"
			)
			else LazyColumn(
				modifier = Modifier.fillMaxSize(),
			) {
				chapterList(
					chapterList = childChapterList,
					chapterNoteItemCount = childNoteCountMap,
					chapterChapterItemCount = childChapterCountMap,
					componentColumnCount = 1,
					onClick = { viewModel.exploreChapter(it.id) },
				)
			}
		}
	}

	ChapterBottomSheet(
		bottomSheetState = bottomSheetState,
		isBottomSheetVisible = isChapterBottomSheetVisible,
		onDismissRequest = { scope.launch { bottomSheetState.hide(); isChapterBottomSheetVisible = false } },
		title = stringResource(id = R.string.new_chapter),
		putChapter = { _, title, description, color, bitmap -> viewModel.putNotebook(title = title, description = description, color = color, bitmap = bitmap) },
	)
}
