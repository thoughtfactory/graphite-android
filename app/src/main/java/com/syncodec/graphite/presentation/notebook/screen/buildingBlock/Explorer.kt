package com.syncodec.graphite.presentation.notebook.screen.buildingBlock

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.di.model.ChapterObject
import com.syncodec.graphite.di.model.NoteObjectLite
import com.syncodec.graphite.di.model.TagObject
import com.syncodec.graphite.utils.dataStore.DataStoreInstance
import com.syncodec.graphite.utils.LocalIsAuthenticated
import com.syncodec.graphite.utils.SortBy
import com.syncodec.graphite.utils.SortOn
import io.realm.kotlin.types.RealmUUID


@Preview
@Composable
fun Explorer(
	noteObjectList : List<NoteObjectLite> = listOf(),
	chapterObjectList : List<ChapterObject> = listOf(),
	chapterNoteItemCount : Map<RealmUUID?, Int> = mapOf(),
	chapterChapterItemCount : Map<RealmUUID?, Int> = mapOf(),
	tagList : List<TagObject> = listOf(),
	isNoteListVisible : Boolean = true,
	isChapterListVisible : Boolean = true,
	isSelecting : Boolean = false,
	selectedIdList : List<RealmUUID> = listOf(),
	onToggleNoteVisibility : () -> Unit = {},
	onToggleChapterVisibility : () -> Unit = {},
	onClickNote : (RealmUUID) -> Unit = {},
	onLongClickNote : (RealmUUID) -> Unit = {},
	onClickChapter : (RealmUUID) -> Unit = {},
	onLongClickChapter : (RealmUUID) -> Unit = {},
) {
	val context = LocalContext.current
	val isAuthenticated = LocalIsAuthenticated.current

	val dataStoreInstance = remember { DataStoreInstance(context = context) }
	val sortBy by dataStoreInstance.getSortBy.collectAsState(initial = null)
	val sortOn by dataStoreInstance.getSortOn.collectAsState(initial = null)

	LazyColumn(
		modifier = Modifier.fillMaxSize()
	) {
		noteList(
			noteList = noteObjectList
				.filter { if (it.isLocked) isAuthenticated else true }
				.sortedWith(
					when (sortOn) {
						SortOn.Title -> if (sortBy == SortBy.Ascending) compareBy { it.title } else compareByDescending { it.title }
						SortOn.Timestamp -> if (sortBy == SortBy.Ascending) compareBy { it.userTimestamp } else compareByDescending { it.userTimestamp }
						SortOn.Modified -> if (sortBy == SortBy.Ascending) compareBy { it.modifiedTimestamp } else compareByDescending { it.modifiedTimestamp }
						else -> compareBy { it.title }
					}
				),
			tagList = tagList,
//			selectedIdList = selectedIdList,
			isVisible = isNoteListVisible,
			toggleVisibility = onToggleNoteVisibility,
			onClick = { onClickNote(it.id) },
			onLongClick = { onLongClickNote(it.id) },
		)
		chapterList(
			chapterList = chapterObjectList
				.filter { isChapterListVisible && (if (it.isLocked) isAuthenticated else true) }
				.sortedWith(
					when (sortOn) {
						SortOn.Title -> if (sortBy == SortBy.Ascending) compareBy { it.title } else compareByDescending { it.title }
						SortOn.Timestamp -> if (sortBy == SortBy.Ascending) compareBy { it.createdTimestamp } else compareByDescending { it.createdTimestamp }
						SortOn.Modified -> if (sortBy == SortBy.Ascending) compareBy { it.modifiedTimestamp } else compareByDescending { it.modifiedTimestamp }
						else -> compareBy { it.title }
					}
				),
			chapterNoteItemCount = chapterNoteItemCount,
			chapterChapterItemCount = chapterChapterItemCount,
			tagList = tagList,
//			selectedIdList = selectedIdList,
			isVisible = isChapterListVisible,
			toggleVisibility = onToggleChapterVisibility,
			onClick = { onClickChapter(it.id) }
		) { onLongClickChapter(it.id) }

		item { Spacer(modifier = Modifier.height(32.dp)) }
	}
}
