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
import io.realm.kotlin.types.RealmUUID


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun HomeScreen(
	componentType : ComponentType,
	noteList : List<NoteObjectLite>,
	bucketList : List<BucketObject>,
	bucketOrderList: List<RealmUUID>,
	notebookList : List<ChapterObject>,
	notebookOrderList: List<RealmUUID>,
	sortOn : SortOn,
	sortBy : SortBy,
	onReorderBucketList : (List<RealmUUID>) -> Unit,
	onReorderNotebookList : (List<RealmUUID>) -> Unit,
	viewType : ViewType,
	onClickFab : () -> Unit,
	onClickNote : (RealmUUID) -> Unit,
	onLongClickNote : (RealmUUID) -> Unit,
	onClickBucket : (RealmUUID) -> Unit,
	onLongClickBucket : (RealmUUID) -> Unit,
	onClickNotebook : (RealmUUID) -> Unit,
	onLongClickNotebook : (RealmUUID) -> Unit
) {
	AnimatedContent(
		targetState = componentType,
		transitionSpec = { fadeIn(tween(300)) + scaleIn(tween(300), 0.71f) with fadeOut(tween(300)) + scaleOut(tween(300), 0.71f) },
		modifier = Modifier.fillMaxSize()
	) {
		when (it) {
			ComponentType.NOTE -> NoteScreen(
				noteList = noteList,
				sortOn = sortOn,
				sortBy = sortBy,
				viewType = viewType,
				onClickFab = onClickFab,
				onClickNote = onClickNote,
				onLongClickNote = onLongClickNote
			)

			ComponentType.BUCKET -> BucketScreen(
				bucketList = bucketList,
				bucketOrderList = bucketOrderList,
				sortOn = sortOn,
				sortBy = sortBy,
				onReorderBucketList = onReorderBucketList,
				onClickFab = onClickFab,
				onClickBucket = onClickBucket,
				onLongClickBucket = onLongClickBucket
			)

			ComponentType.NOTEBOOK -> NotebookScreen(
				notebookList = notebookList,
				notebookOrderList = notebookOrderList,
				onReorderNotebookList = onReorderNotebookList,
				onClickFab = onClickFab,
				onClickNotebook = onClickNotebook,
				onLongClickNotebook = onLongClickNotebook
			)
		}
	}
}
