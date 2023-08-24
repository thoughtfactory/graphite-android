package com.syncodec.graphite.presentation.main.composable.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.button.FilterButton
import com.syncodec.graphite.presentation.common.tab.GenericTabRow
import com.syncodec.graphite.presentation.common.tab.TabItem
import com.syncodec.graphite.presentation.main.composable.screen.bucketScreen.BucketScreen
import com.syncodec.graphite.presentation.main.composable.screen.noteScreen.NoteScreen
import com.syncodec.graphite.presentation.main.composable.screen.notebookScreen.NotebookScreen
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.launch


@OptIn(ExperimentalFoundationApi::class)
@Preview
@Composable
fun HomeScreen(
	isSelecting: Boolean = false,
	onSelect: (RealmUUID) -> Unit = {},
	selectedIdList: List<RealmUUID> = listOf(),
) {
	val pagerState = rememberPagerState { 3 }

	Column(
		modifier = Modifier.fillMaxSize(),
	) {
		TabNavigator(
			pagerState = pagerState,
			isSelecting = isSelecting,
		)

		Spacer(modifier = Modifier.height(6.dp))

		HorizontalPager(
			state = pagerState,
			userScrollEnabled = false,  //  Dont be oversmart and enable this or you will break scroll in bucket filter tags
		) {
			when (it) {
				ComponentType.Note.ordinal -> NoteScreen(
					isSelecting = isSelecting,
					onSelect = onSelect,
					selectedIdList = selectedIdList,
				)

				ComponentType.Bucket.ordinal -> BucketScreen(
					isSelecting = isSelecting,
					selectedIdList = selectedIdList,
					onSelect = onSelect,
				)

				ComponentType.Notebook.ordinal -> NotebookScreen(
					isSelecting = isSelecting,
					onSelect = onSelect,
					selectedIdList = selectedIdList,
				)
			}
		}
	}
}

@OptIn(ExperimentalFoundationApi::class)
@Preview
@Composable
private fun TabNavigator(
	pagerState: PagerState = rememberPagerState { 3 },
	isSelecting: Boolean = false,
) {
	val scope = rememberCoroutineScope()

	AnimatedVisibility(
		visible = !isSelecting,
		enter = expandVertically(tween(470)),
		exit = shrinkVertically(tween(470)),
	) {
		Row(
			modifier = Modifier.fillMaxWidth(),
			horizontalArrangement = Arrangement.Center,
			verticalAlignment = Alignment.CenterVertically
		) {
			Spacer(modifier = Modifier.width(12.dp))
			GenericTabRow(
				tabItemList = listOf(
					TabItem(text = "Note", icon = R.drawable.ic_fa_note_duotone) { scope.launch { pagerState.animateScrollToPage(0) } },
					TabItem(text = "List", icon = R.drawable.ic_fa_bucket_list_duotone) { scope.launch { pagerState.animateScrollToPage(1) } },
					TabItem(text = "Notebook", icon = R.drawable.ic_fa_notebook_duotone) { scope.launch { pagerState.animateScrollToPage(2) } },
				),
				selectedTabIndex = pagerState.currentPage,
				modifier = Modifier.weight(1f)
			)
			FilterButton()

			Spacer(modifier = Modifier.width(4.dp))
		}
	}
}
