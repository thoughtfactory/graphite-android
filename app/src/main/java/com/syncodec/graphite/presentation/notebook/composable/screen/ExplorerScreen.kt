package com.syncodec.graphite.presentation.notebook.composable.screen

import android.content.Intent
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.ChapterObject
import com.syncodec.graphite.di.model.NoteObjectLite
import com.syncodec.graphite.di.model.TagObject
import com.syncodec.graphite.presentation.common.LocalCompositionIsSelected
import com.syncodec.graphite.presentation.common.LocalCompositionOnSelect
import com.syncodec.graphite.presentation.common.LocalCompositionSelectedObjectIdList
import com.syncodec.graphite.presentation.main.composable.buildingBlock.EmptyView
import com.syncodec.graphite.presentation.note.NoteActivity
import com.syncodec.graphite.presentation.notebook.NotebookActivity
import com.syncodec.graphite.presentation.notebook.composable.buildingBlock.ChapterListCard
import com.syncodec.graphite.presentation.notebook.composable.buildingBlock.NoteListCard
import com.syncodec.graphite.utils.AttachmentType
import com.syncodec.graphite.utils.DataStoreInstance
import com.syncodec.graphite.utils.Extra
import com.syncodec.graphite.utils.LocalVaultIsOpened
import com.syncodec.graphite.utils.SortBy
import com.syncodec.graphite.utils.SortOn
import com.syncodec.graphite.utils.decodeBase64ToBitmap
import io.realm.kotlin.types.RealmUUID


@Preview
@Composable
fun ExplorerScreen() {
	val context = LocalContext.current
	val dataStoreInstance = remember { DataStoreInstance(context = context) }

	val chapterId = NotebookActivity.LocalChapterId.current

	val getChapter = NotebookActivity.LocalGetChapter.current

	var isNoteListVisible by remember { mutableStateOf(true) }
	var isChapterListVisible by remember { mutableStateOf(true) }

	val noteObjectList = NotebookActivity.LocalNoteObjectList.current
	val chapterObjectList = NotebookActivity.LocalChapterObjectList.current
	val tagList = NotebookActivity.LocalTagList.current

	val isSelected = LocalCompositionIsSelected.current
	val onSelect = LocalCompositionOnSelect.current
	val selectedObjectList = LocalCompositionSelectedObjectIdList.current

	val isVaultOpened = LocalVaultIsOpened.current

	val sortOn by dataStoreInstance.getSortOn.collectAsState(initial = SortOn.TIMESTAMP)
	val sortBy by dataStoreInstance.getSortBy.collectAsState(initial = SortBy.DESCENDING)

	val onRefresh = NotebookActivity.LocalOnRefresh.current

	val activityLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { onRefresh() }


	if (noteObjectList.isEmpty() && chapterObjectList.isEmpty()) {
		EmptyView(
			image = R.drawable.il_empty_chapter,
			title = "Keep a diary, and perhaps someday it will keep you",
			subTitle = "― Mae West",
		)
	} else {
		LazyColumn(
			modifier = Modifier.fillMaxSize()
		) {
			noteList(
				noteList = noteObjectList
					.filter { if (it.isLocked) isVaultOpened else true }
					.sortedWith(
						when (sortOn) {
							SortOn.TITLE -> if (sortBy == SortBy.ASCENDING) compareBy { it.title } else compareByDescending { it.title }
							SortOn.TIMESTAMP -> if (sortBy == SortBy.ASCENDING) compareBy { it.userTimestamp } else compareByDescending { it.userTimestamp }
							SortOn.MODIFIED -> if (sortBy == SortBy.ASCENDING) compareBy { it.modifiedTimestamp } else compareByDescending { it.modifiedTimestamp }
							else -> compareBy { it.title }
						}
					),
				tagList = tagList,
				selectedObjectIdList = selectedObjectList,
				isVisible = isNoteListVisible,
				toggleVisibility = { isNoteListVisible = ! isNoteListVisible },
				onClick = {
					if (isSelected) {
						if (it.id in selectedObjectList) selectedObjectList.remove(it.id) else selectedObjectList.add(it.id)
					} else {
						Intent(context, NoteActivity::class.java).apply {
							putExtra(Extra.Companion.Constant.IS_NEW.name, false)
							putExtra(Extra.Companion.Constant.CHAPTER_ID.name, chapterId?.bytes)
							putExtra(Extra.Companion.Constant.NOTE_ID.name, it.id.bytes)
							putExtra(Extra.Companion.Constant.FILTER.name, Extra.Companion.Filter.READ_CHAPTER.name)

							activityLauncher.launch(this)
						}
					}
				},
				onLongClick = {
					onSelect(true)
					if (it.id in selectedObjectList) selectedObjectList.remove(it.id) else selectedObjectList.add(it.id)
				}
			)
			chapterList(
				chapterList = chapterObjectList
					.filter { if (it.isLocked) isVaultOpened else true }
					.sortedWith(
						when (sortOn) {
							SortOn.TITLE -> if (sortBy == SortBy.ASCENDING) compareBy { it.title } else compareByDescending { it.title }
							SortOn.TIMESTAMP -> if (sortBy == SortBy.ASCENDING) compareBy { it.createdTimestamp } else compareByDescending { it.createdTimestamp }
							SortOn.MODIFIED -> if (sortBy == SortBy.ASCENDING) compareBy { it.modifiedTimestamp } else compareByDescending { it.modifiedTimestamp }
							else -> compareBy { it.title }
						}
					),
				tagList = tagList,
				selectedObjectIdList = selectedObjectList,
				isVisible = isChapterListVisible,
				isVaultOpened = isVaultOpened,
				toggleVisibility = { isChapterListVisible = ! isChapterListVisible },
				onClick = {
					if (isSelected) {
						if (it.id in selectedObjectList) selectedObjectList.remove(it.id) else selectedObjectList.add(it.id)
					} else {
						getChapter(it.id)
					}
				},
				onLongClick = {
					onSelect(true)
					if (it.id in selectedObjectList) selectedObjectList.remove(it.id) else selectedObjectList.add(it.id)
				}
			)

			item { Spacer(modifier = Modifier.height(32.dp)) }
		}
	}
}

@OptIn(ExperimentalFoundationApi::class)
private fun LazyListScope.noteList(
	noteList : List<NoteObjectLite>,
	tagList : List<TagObject>,
	selectedObjectIdList : List<RealmUUID> = listOf(),
	isVisible : Boolean,
	toggleVisibility : () -> Unit,
	onClick : (NoteObjectLite) -> Unit,
	onLongClick : (NoteObjectLite) -> Unit
) {
	stickyHeader {
		Box(
			modifier = Modifier
				.fillMaxWidth()
				.background(MaterialTheme.colorScheme.background)
		) {
			Box(
				modifier = Modifier
					.fillMaxWidth()
					.padding(8.dp)
					.background(MaterialTheme.colorScheme.background)
					.clip(RoundedCornerShape(12.dp))
					.clickable { toggleVisibility() }
			) {
				Row(
					verticalAlignment = Alignment.Bottom,
					modifier = Modifier
						.fillMaxWidth()
						.padding(8.dp)
				) {
					Text(
						text = "Notes",
						style = MaterialTheme.typography.bodyLarge,
						color = MaterialTheme.colorScheme.onBackground,
						fontWeight = FontWeight.Bold
					)

					Spacer(modifier = Modifier.weight(1f))

					Text(
						text = "${noteList.size} notes",
						style = MaterialTheme.typography.bodyMedium,
						color = MaterialTheme.colorScheme.onBackground,
						fontWeight = FontWeight.Bold
					)
				}
			}
		}
	}

	noteList.forEach { note ->
		item(
			key = note.id.toString()
		) {
			var thumbnail by remember { mutableStateOf<Bitmap?>(null) }

			LaunchedEffect(key1 = note.id.hashCode(), key2 = note.thumbnail.hashCode()) {
				try {
					if (note.thumbnailType == AttachmentType.IMAGE.name.lowercase()) thumbnail = note.thumbnail
				} catch (e : Exception) {
//					e.printStackTrace()
				}
			}

			Box(
				modifier = Modifier.animateItemPlacement(tween(300))
			) {
				NoteListCard(
					id = note.id,
					timestamp = note.userTimestamp,
					isLocked = note.isLocked,
					isSelected = note.id in selectedObjectIdList,
					isFavourite = note.isFavourite,
					isLast = false,
					title = note.title,
					contentThumbnail = note.contentThumbnail,
					attachmentThumbnail = thumbnail,
					address = note.address,
					latLng = note.latLng,
					tagList = tagList.filter { note.id in it.objectIdList },
					isVisible = isVisible,
					selectedColor = MaterialTheme.colorScheme.surface,
					onClick = { onClick(note) },
				) { onLongClick(note) }
			}
		}
	}
}

@OptIn(ExperimentalFoundationApi::class)
private fun LazyListScope.chapterList(
	chapterList : List<ChapterObject>,
	tagList : List<TagObject>,
	selectedObjectIdList : List<RealmUUID>,
	isVisible : Boolean,
	isVaultOpened : Boolean,
	toggleVisibility : () -> Unit,
	onClick : (ChapterObject) -> Unit,
	onLongClick : (ChapterObject) -> Unit
) {
	stickyHeader {
		Box(
			modifier = Modifier
				.fillMaxWidth()
				.background(MaterialTheme.colorScheme.background)
		) {
			Box(
				modifier = Modifier
					.fillMaxWidth()
					.padding(8.dp)
					.clip(RoundedCornerShape(12.dp))
					.clickable { toggleVisibility() }
			) {
				Row(
					verticalAlignment = Alignment.Bottom,
					modifier = Modifier
						.fillMaxWidth()
						.padding(8.dp)
				) {
					Text(
						text = "Chapters",
						style = MaterialTheme.typography.bodyLarge,
						color = MaterialTheme.colorScheme.onBackground,
						fontWeight = FontWeight.Bold
					)

					Spacer(modifier = Modifier.weight(1f))

					Text(
						text = "${chapterList.size} chapters",
						style = MaterialTheme.typography.bodyMedium,
						color = MaterialTheme.colorScheme.onBackground,
						fontWeight = FontWeight.Bold
					)
				}
			}
		}
	}

	chapterList.forEach { chapterObject ->
		item(
			key = chapterObject.id.toString()
		) {
			Box(
				modifier = Modifier.animateItemPlacement(tween(300))
			) {
				ChapterListCard(
					id = chapterObject.id,
					timestamp = chapterObject.createdTimestamp,
					isSelected = chapterObject.id in selectedObjectIdList,
					isLocked = chapterObject.isLocked,
					isFavourite = chapterObject.isFavourite,
					isDeleted = false,
					isLast = false,
					title = chapterObject.title,
					description = chapterObject.description,
					color = chapterObject.color?.let { Color(it) },
					thumbnail = chapterObject.thumbnail?.decodeBase64ToBitmap(),
					noteCount = chapterObject.noteList.filter { if (it.isLocked) isVaultOpened else true }.size,
					chapterCount = chapterObject.chapterList.filter { if (it.isLocked) isVaultOpened else true }.size,
					tagList = tagList.filter { it.objectIdList.contains(chapterObject.id) }.map { it.toLite() },
					isVisible = isVisible,
					selectedColor = MaterialTheme.colorScheme.surface,
					onClick = { onClick(chapterObject) },
					onLongClick = { onLongClick(chapterObject) }
				)
			}
		}
	}
}
