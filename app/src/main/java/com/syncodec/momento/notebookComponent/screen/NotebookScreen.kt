package com.syncodec.momento.notebookComponent.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.syncodec.momento.custom.entry.EntryCard
import com.syncodec.momento.custom.entry.EntryHeaderCard
import com.syncodec.momento.custom.entry.EntryTimelineSpacer
import com.syncodec.momento.custom.entry.NoEntryCard
import com.syncodec.momento.database.diary.Note
import com.syncodec.momento.database.notebook.Chapter
import com.syncodec.momento.notebookComponent.NotebookActivity
import com.syncodec.momento.notebookComponent.ViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NotebookScreen() {
	val viewModel: ViewModel = viewModel()
	val scope = rememberCoroutineScope()

	var isSelected by viewModel.activityState.isSelected

	val notebook by viewModel.notebook
	val currentRoute = viewModel.currentRoute
	val currentRouteName = viewModel.currentRouteName


	val selectedItemList = viewModel.activityState.selectedItemList

	val chapterList = viewModel.chapterList.filter { it.notebookRoute == currentRoute }
	val noteList = viewModel.noteList.filter { it.notebookRoute == currentRoute }
	val isContentEmpty: Boolean = chapterList.isEmpty() && noteList.isEmpty()

	var showContent by viewModel.activityState.showContent

	var showChapter by remember { mutableStateOf(true) }
	var showNote by remember { mutableStateOf(true) }

	SideEffect {
		showChapter = true
	}

	if (isContentEmpty) {
		Column(
			modifier = Modifier
				.fillMaxSize()
		) {
			Spacer(modifier = Modifier.height(76.dp))
			NoEntryCard()
		}
	} else {
		Column(
			modifier = Modifier.fillMaxSize()
		) {
//			ComponentChooser {}
			LazyColumn(
				modifier = Modifier
			) {
				item { Spacer(modifier = Modifier.height(16.dp)) }

				stickyHeader {
					EntryHeaderCard(
						title = "Chapter",
						noEntries = "${chapterList.size} ${if (chapterList.size == 1) "chapter" else "chapters"}"
					) {
						showChapter = !showChapter
					}
				}

				chapterList.forEachIndexed { index: Int, chapter: Chapter ->
					item {
						val tint = MaterialTheme.colorScheme.secondaryContainer

						AnimatedVisibility(
							visible = showChapter
						) {
							EntryCard(
								timestamp = chapter.modifiedTimestamp,
								isLocked = false,
								isSelected = chapter.primaryKey in selectedItemList,
								isArchived = false,
								isFavourite = false,
								isDeleted = false,
								isLast = index == chapterList.size - 1,
								title = null,
								contentThumbnail = chapter.title,
								attachmentCount = 0,
								attachmentThumbnail = null,
								address = null,
								tint = tint,
								onClick = {
									if (isSelected) {
										isSelected = true
										if (chapter.primaryKey in selectedItemList) {
											selectedItemList.remove(chapter.primaryKey)
										} else {
											selectedItemList.add(chapter.primaryKey)
										}
									} else {
										scope.launch {
//											for animating purpose
											showContent = false
											delay(NotebookActivity.ANIMATION_DURATION.toLong())

											currentRoute.add(chapter.primaryKey)
											currentRouteName.add(chapter.title)

											showContent = true
										}
									}
								},
								onLongClick = {
									isSelected = true
									selectedItemList.add(chapter.primaryKey)
								},
							).apply {
								EntryCard(entryCard = this)
							}
						}

						EntryTimelineSpacer(
							tint = tint,
							isVisible = index != chapterList.size - 1
						)
					}
				}

				stickyHeader {
					EntryHeaderCard(
						title = "Note",
						noEntries = "${noteList.size} ${if (noteList.size == 1) "note" else "notes"}"
					) {
						showNote = !showNote
					}
				}
				noteList.forEachIndexed { index: Int, note: Note ->
					item {
						val tint = MaterialTheme.colorScheme.secondaryContainer

						AnimatedVisibility(
							visible = showNote
						) {
							EntryCard(
								timestamp = note.modifiedTimestamp,
								isLocked = false,
								isSelected = note.primaryKey in selectedItemList,
								isArchived = false,
								isFavourite = false,
								isDeleted = false,
								isLast = index == noteList.size - 1,
								title = note.title,
								contentThumbnail = note.contentThumbnail,
								attachmentCount = 0,
								attachmentThumbnail = null,
								address = null,
								tint = tint,
								isVisible = showNote,
								onClick = {
								},
								onLongClick = {
									isSelected = true
									selectedItemList.add(note.primaryKey)
								},
							).apply {
								EntryCard(entryCard = this)
							}
						}

						EntryTimelineSpacer(
							tint = tint,
							isVisible = index != noteList.size - 1
						)
					}
				}

				item {
					Spacer(modifier = Modifier.height(128.dp))
				}
			}
		}
	}
}
