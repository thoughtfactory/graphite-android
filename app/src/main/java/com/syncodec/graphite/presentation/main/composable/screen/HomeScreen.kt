package com.syncodec.graphite.presentation.main.composable.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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


@Preview
@Composable
fun HomeScreen(
	isSelecting: Boolean = false,
	selectedIdList: Set<RealmUUID> = setOf(),
	onSelect: (RealmUUID) -> Unit = {},
	onUnSelectAll: () -> Unit = {},
) {
	var currentScreen by remember { mutableIntStateOf(0) }

	Column(
		modifier = Modifier.fillMaxSize(),
	) {
		TabNavigator(
			currentScreen = currentScreen,
			isSelecting = isSelecting,
			onChangeScreen = { currentScreen = it }
		)

		Spacer(modifier = Modifier.height(6.dp))

		AnimatedContent(
			targetState = currentScreen,
			label = "currentScreen_animation"
		) {
			when (it) {
				ComponentType.Note.ordinal -> NoteScreen(
					isSelecting = isSelecting,
					selectedIdList = selectedIdList,
					onSelect = onSelect,
					onUnSelectAll = onUnSelectAll,
				)

				ComponentType.Bucket.ordinal -> BucketScreen(
					isSelecting = isSelecting,
					selectedIdList = selectedIdList,
					onSelect = onSelect,
					onUnSelectAll = onUnSelectAll,
				)

				ComponentType.Notebook.ordinal -> NotebookScreen(
					isSelecting = isSelecting,
					onSelect = onSelect,
					selectedIdList = selectedIdList,
					onUnSelectAll = onUnSelectAll,
				)
			}
		}
	}
}

@Preview
@Composable
private fun TabNavigator(
	currentScreen: Int = 0,
	isSelecting: Boolean = false,
	onChangeScreen: (Int) -> Unit = {}
) {
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
					TabItem(text = "Note", icon = R.drawable.ic_fa_note_duotone) { onChangeScreen(0) },
					TabItem(text = "List", icon = R.drawable.ic_fa_bucket_list_duotone) { onChangeScreen(1) },
					TabItem(text = "Notebook", icon = R.drawable.ic_fa_notebook_duotone) { onChangeScreen(2) },
				),
				selectedTabIndex = currentScreen,
				modifier = Modifier.weight(1f)
			)
			FilterButton()

			Spacer(modifier = Modifier.width(4.dp))
		}
	}
}
