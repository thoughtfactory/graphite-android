package com.syncodec.graphite.presentation.main.composable.screen

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.syncodec.graphite.di.model.BucketObject
import com.syncodec.graphite.di.model.ChapterObject
import com.syncodec.graphite.di.model.NoteObjectLite
import com.syncodec.graphite.utils.*
import io.realm.kotlin.types.ObjectId


@Composable
fun HomeScreen(
	componentType: ComponentType,
	notebookId: ObjectId?,
	noteDayMap: Map<Long, List<NoteObjectLite>>,
	bucketList: List<BucketObject>?,
	notebookList: List<ChapterObject>,
	viewType: ViewType,
	onClickFab: () -> Unit,
	onClickNote: (ObjectId) -> Unit,
	onLongClickNote: (ObjectId) -> Unit,
	onClickBucket: (ObjectId) -> Unit,
	onLongClickBucket: (ObjectId) -> Unit,
	onClickNotebook: (ObjectId) -> Unit,
	onLongClickNotebook: (ObjectId) -> Unit
) {
	Crossfade(
		targetState = componentType,
		modifier = Modifier.fillMaxSize()
	) {
		when (it) {
			ComponentType.NOTE -> NoteScreen(
				notebookId = notebookId,
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
