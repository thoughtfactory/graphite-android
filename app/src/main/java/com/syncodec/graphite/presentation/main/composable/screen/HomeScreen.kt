package com.syncodec.graphite.presentation.main.composable.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.with
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.syncodec.graphite.presentation.main.composable.screen.bucketScreen.BucketScreen
import com.syncodec.graphite.presentation.main.composable.screen.noteScreen.NoteScreen
import com.syncodec.graphite.presentation.main.composable.screen.notebookScreen.NotebookScreen
import com.syncodec.graphite.utils.*
import io.realm.kotlin.types.RealmUUID


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun HomeScreen(
	componentType : ComponentType = ComponentType.Note,
	isSelecting : Boolean = false,
	onSelect : (RealmUUID) -> Unit = {},
	selectedIdList : List<RealmUUID> = listOf(),
	onClickNewList : () -> Unit = {},
	onClickNewNotebook : () -> Unit = {},
) {
	AnimatedContent(
		targetState = componentType,
		transitionSpec = { fadeIn(tween(300)) + scaleIn(tween(300), 0.71f) with fadeOut(tween(300)) + scaleOut(tween(300), 0.71f) },
		modifier = Modifier.fillMaxSize()
	) {
		when (it) {
			ComponentType.Note -> NoteScreen(
				isSelecting = isSelecting,
				onSelect = onSelect,
				selectedIdList = selectedIdList,
			)

			ComponentType.Bucket -> BucketScreen(
				isSelecting = isSelecting,
				onSelect = onSelect,
				selectedIdList = selectedIdList,
				onClickFab = onClickNewList,
			)

			ComponentType.Notebook -> NotebookScreen(
				isSelecting = isSelecting,
				onSelect = onSelect,
				selectedIdList = selectedIdList,
				onClickFab = onClickNewNotebook,
			)
		}
	}
}
