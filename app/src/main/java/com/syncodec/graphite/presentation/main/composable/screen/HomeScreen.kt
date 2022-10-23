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
import com.syncodec.graphite.di.model.BucketObject
import com.syncodec.graphite.di.model.ChapterObject
import com.syncodec.graphite.di.model.NoteObjectLite
import com.syncodec.graphite.utils.*
import io.realm.kotlin.types.ObjectId


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun HomeScreen(
	componentType : ComponentType,
	noteDayMap : Map<Long, List<NoteObjectLite>>,
	bucketList : List<BucketObject>?,
	notebookList : List<ChapterObject>,
	viewType : ViewType,
	onClickFab : () -> Unit,
	onClickNote : (ObjectId) -> Unit,
	onLongClickNote : (ObjectId) -> Unit,
	onClickBucket : (ObjectId) -> Unit,
	onLongClickBucket : (ObjectId) -> Unit,
	onClickNotebook : (ObjectId) -> Unit,
	onLongClickNotebook : (ObjectId) -> Unit
) {
	AnimatedContent(
		targetState = componentType,
		transitionSpec = { fadeIn(tween(300)) + scaleIn(tween(300), 0.71f) with fadeOut(tween(300)) + scaleOut(tween(300), 0.71f) },
		modifier = Modifier.fillMaxSize()
	) {
		when (it) {
			ComponentType.NOTE -> NoteScreen(
				noteDayMap = noteDayMap,
				viewType = viewType,
				onClickFab = onClickFab,
				onClickNote = onClickNote,
				onLongClickNote = onLongClickNote
			)

			ComponentType.BUCKET -> BucketScreen(
				bucketList = bucketList,
				onClickFab = onClickFab,
				onClickBucket = onClickBucket,
				onLongClickBucket = onLongClickBucket
			)

			ComponentType.NOTEBOOK -> NotebookScreen(
				notebookList = notebookList,
				onClickFab = onClickFab,
				onClickNotebook = onClickNotebook,
				onLongClickNotebook = onLongClickNotebook
			)
		}
	}
}
