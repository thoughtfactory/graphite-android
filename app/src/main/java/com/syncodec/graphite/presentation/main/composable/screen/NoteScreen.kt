package com.syncodec.graphite.presentation.main.composable.screen

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.with
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.syncodec.graphite.di.model.AttachmentObject
import com.syncodec.graphite.di.model.NoteObjectLite
import com.syncodec.graphite.presentation.common.LocalCompositionIsSelected
import com.syncodec.graphite.presentation.common.LocalCompositionSelectedRealmUUIDList
import com.syncodec.graphite.presentation.common.lazyView.isScrollingUp
import com.syncodec.graphite.presentation.main.composable.LocalCompositionIsNoteRefreshing
import com.syncodec.graphite.presentation.main.composable.LocalCompositionOnRefresh
import com.syncodec.graphite.presentation.main.composable.LocalCompositionTagList
import com.syncodec.graphite.presentation.main.composable.buildingBlock.NoteFloatingActionButton
import com.syncodec.graphite.presentation.main.composable.buildingBlock.YearProgressBar
import com.syncodec.graphite.presentation.main.composable.buildingBlock.notebook.NoEntryCard
import com.syncodec.graphite.presentation.main.composable.buildingBlock.notebook.NotebookHeaderCard
import com.syncodec.graphite.presentation.main.composable.buildingBlock.notebook.gridView.NoteGridCard
import com.syncodec.graphite.presentation.main.composable.buildingBlock.notebook.listView.NoteListCard
import com.syncodec.graphite.presentation.main.composable.buildingBlock.notebook.listView.NotebookTimelineSpacer
import com.syncodec.graphite.utils.SortBy
import com.syncodec.graphite.utils.SortOn
import com.syncodec.graphite.utils.ViewType
import com.syncodec.graphite.utils.timeStampToPrettyDay
import com.syncodec.graphite.utils.timestampToCalendarDay
import io.realm.kotlin.types.RealmUUID


@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun NoteScreen(
	noteList : List<NoteObjectLite>,
	sortOn : SortOn,
	sortBy : SortBy,
	viewType : ViewType,
	onClickFab : () -> Unit,
	onClickNote : (RealmUUID) -> Unit,
	onLongClickNote : (RealmUUID) -> Unit
) {
	val isNoteRefreshing = LocalCompositionIsNoteRefreshing.current
	val onRefresh = LocalCompositionOnRefresh.current

	val isSelected = LocalCompositionIsSelected.current
	val selectedRealmUUIDList = LocalCompositionSelectedRealmUUIDList.current

	val lazyListState = rememberLazyListState()
	val lazyGridState = rememberLazyGridState()

	val noteMap : MutableMap<String, MutableList<NoteObjectLite>> = mutableMapOf()
	when(sortOn) {
		SortOn.TITLE -> {
			val _noteList = if (sortBy == SortBy.ASCENDING) {
				noteList.sortedBy { it.title }
			} else {
				noteList.sortedByDescending { it.title }
			}
			_noteList.forEach { note ->
				val sorter = note.title?.firstOrNull()?.uppercase() ?: "."
				if (noteMap.containsKey(sorter)) noteMap[sorter] !!.add(note)
				else noteMap[sorter] = mutableListOf(note)
			}
		}
		SortOn.TIMESTAMP -> {
			val _noteList = if (sortBy == SortBy.ASCENDING) {
				noteList.sortedBy { it.userTimestamp }
			} else {
				noteList.sortedByDescending { it.userTimestamp }
			}
			_noteList.forEach { note ->
				val sorter = timestampToCalendarDay(note.userTimestamp).timeStampToPrettyDay()
				if (noteMap.containsKey(sorter)) noteMap[sorter] !!.add(note)
				else noteMap[sorter] = mutableListOf(note)
			}
		}
		SortOn.MODIFIED -> {
			val _noteList = if (sortBy == SortBy.ASCENDING) {
				noteList.sortedBy { it.modifiedTimestamp }
			} else {
				noteList.sortedByDescending { it.modifiedTimestamp }
			}
			_noteList.forEach { note ->
				val timestamp = timestampToCalendarDay(note.modifiedTimestamp).timeStampToPrettyDay()
				if (noteMap.containsKey(timestamp)) noteMap[timestamp] !!.add(note)
				else noteMap[timestamp] = mutableListOf(note)
			}
		}
		else -> null
	}

	Scaffold(
		floatingActionButton = {
			AnimatedVisibility(
				visible = ! isSelected,
				enter = fadeIn(tween(300)) + scaleIn(tween(300)),
				exit = fadeOut(tween(300)) + scaleOut(tween(300))
			) {
				NoteFloatingActionButton(isExpanded = lazyListState.isScrollingUp() || lazyGridState.isScrollingUp(), onClick = onClickFab)
			}
		},
		floatingActionButtonPosition = FabPosition.End
	) {
		Box(
			modifier = Modifier.padding(it)
		) {
			Crossfade(
				targetState = noteMap.isEmpty(),
				animationSpec = tween(600)
			) {
				if (it) {
					NoEntryCard()
				} else {
					AnimatedContent(
						targetState = viewType,
						transitionSpec = { fadeIn(tween(300)) + scaleIn(tween(300), 0.71f) with fadeOut(tween(300)) + scaleOut(tween(300), 0.71f) },
						modifier = Modifier.fillMaxSize()
					) {
						when (it) {
							ViewType.LIST -> ListView(
								lazyListState = lazyListState,
								isRefreshing = isNoteRefreshing,
								isSelected = isSelected,
								selectedRealmUUIDList = selectedRealmUUIDList,
								noteDayMap = noteMap,
								onClickNote = onClickNote,
								onLongClickNote = onLongClickNote,
								onRefresh = onRefresh
							)

							ViewType.GRID -> GridView(
								lazyListState = lazyListState,
								isRefreshing = isNoteRefreshing,
								isSelected = isSelected,
								selectedRealmUUIDList = selectedRealmUUIDList,
								noteDayMap = noteMap,
								onClickNote = onClickNote,
								onLongClickNote = onLongClickNote,
								onRefresh = onRefresh
							)
						}
					}
				}
			}
		}
	}
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ListView(
	lazyListState : LazyListState,
	isRefreshing : Boolean,
	isSelected : Boolean,
	selectedRealmUUIDList : List<RealmUUID>,
	noteDayMap : Map<String, List<NoteObjectLite>>,
	onClickNote : (RealmUUID) -> Unit,
	onLongClickNote : (RealmUUID) -> Unit,
	onRefresh : () -> Unit
) {
	val tagList = LocalCompositionTagList.current

	SwipeRefresh(
		state = rememberSwipeRefreshState(isRefreshing = isRefreshing),
		onRefresh = onRefresh
	) {
		LazyColumn(
			modifier = Modifier.fillMaxSize(),
			state = lazyListState
		) {
			item {
				YearProgressBar(showCard = ! isSelected)
			}

			noteDayMap.forEach { (day, noteList) ->
				val sortedList = noteList.sortedBy { - it.userTimestamp }

				val entrySize = sortedList.size

				stickyHeader(
					key = day,
					contentType = day
				) {
					if (entrySize != 0) {
						NotebookHeaderCard(
							title = day,
							noEntries = "$entrySize ${if (entrySize == 1) "entry" else "entries"}",
							color = MaterialTheme.colorScheme.background
						)
					}
				}

				val lastEntryKey = if (entrySize != 0) sortedList.last().id else null

				sortedList.forEach { note ->
					item(
						key = note.id.toString(),
						contentType = note
					) {
						var thumbnail by remember { mutableStateOf<Bitmap?>(null) }

						LaunchedEffect(key1 = note.id.hashCode() + note.thumbnail.hashCode()) {
							try {
								if (note.thumbnailType == AttachmentObject.Companion.Type.IMAGE.name) {
									thumbnail = note.thumbnail?.let { BitmapFactory.decodeByteArray(note.thumbnail, 0, it.size) }
								}
							} catch (e : Exception) {
								e.printStackTrace()
							}
						}

						Box(
							modifier = Modifier.animateItemPlacement(tween(300))
						) {
							NoteListCard(
								id = note.id,
								parentChapterId = note.parentChapterId,
								timestamp = note.userTimestamp,
								showFullTime = false,
								isLocked = note.isLocked,
								isSelected = selectedRealmUUIDList.contains(note.id),
								isFavourite = note.isFavourite,
								isDeleted = false,
								isLast = note.id == lastEntryKey,
								title = note.title,
								contentThumbnail = note.contentThumbnail,
								attachmentCount = note.attachmentCount,
								attachmentThumbnail = thumbnail,
								address = note.address,
								tagList = tagList.filter { it.RealmUUIDList.contains(note.id) },
								latLng = note.latLng,
								isVisible = true,
								isSwipable = true,
								containerColor = MaterialTheme.colorScheme.background,
								selectedColor = MaterialTheme.colorScheme.surface,
								onClick = { onClickNote(note.id) },
								onLongClick = { onLongClickNote(note.id) },
							)
						}

						NotebookTimelineSpacer(isVisible = note.id != lastEntryKey)
					}
				}
			}

			item { Spacer(modifier = Modifier.height(128.dp)) }
		}
	}
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun GridView(
	lazyListState : LazyListState,
	isRefreshing : Boolean,
	isSelected : Boolean,
	selectedRealmUUIDList : List<RealmUUID>,
	noteDayMap : Map<String, List<NoteObjectLite>>,
	onClickNote : (RealmUUID) -> Unit,
	onLongClickNote : (RealmUUID) -> Unit,
	onRefresh : () -> Unit
) {
	SwipeRefresh(
		state = rememberSwipeRefreshState(isRefreshing = isRefreshing),
		onRefresh = onRefresh
	) {
		LazyVerticalStaggeredGrid(
			columns = StaggeredGridCells.Fixed(2),
			modifier = Modifier.fillMaxSize(),
			contentPadding = PaddingValues(8.dp, 0.dp),
		) {
			noteDayMap.forEach { day, noteList ->
				noteList.forEach { note ->
					item(
						key = note.id.toString(),
						contentType = note
					) {
						var thumbnail by remember { mutableStateOf<Bitmap?>(null) }

						LaunchedEffect(key1 = note.id.hashCode() + note.thumbnail.hashCode()) {
							try {
								if (note.thumbnailType == AttachmentObject.Companion.Type.IMAGE.name) {
									thumbnail = note.thumbnail?.let { BitmapFactory.decodeByteArray(note.thumbnail, 0, it.size) }
								}
							} catch (e : Exception) {
								e.printStackTrace()
							}
						}

						NoteGridCard(
							id = note.id,
							timestamp = note.userTimestamp,
							showFullTime = true,
							isLocked = note.isLocked,
							isSelected = note.id in selectedRealmUUIDList,
							isFavourite = note.isFavourite,
							isDeleted = false,
							isLast = false,
							title = note.title,
							contentThumbnail = note.contentThumbnail,
							attachmentCount = note.attachmentCount,
							attachmentThumbnail = thumbnail,
							address = note.address,
							latLng = note.latLng,
							selectedColor = MaterialTheme.colorScheme.surface,
							onClick = { onClickNote(note.id) },
							onLongClick = { onLongClickNote(note.id) },
						)
					}
				}
			}
		}
//		LazyColumn(
//			modifier = Modifier.fillMaxSize(),
//			state = lazyListState
//		) {
//			noteDayMap.forEach { (day, noteList) ->
//				item {
//					NotebookHeaderCard(
//						title = day.timeStampToPrettyDay(),
//						noEntries = noteList.size.toString(),
//						color = MaterialTheme.colorScheme.background
//					)
//				}
//
//				for (i in 0 until ((noteList.size + 1) / 2)) {
//					item {
//						Row(
//							modifier = Modifier.fillMaxWidth()
//						) {
//							val note1 = noteList.getOrNull(i * 2)
//							val note2 = noteList.getOrNull(i * 2 + 1)
//
//							var thumbnail1 by remember { mutableStateOf<Bitmap?>(null) }
//							var thumbnail2 by remember { mutableStateOf<Bitmap?>(null) }
//
//							LaunchedEffect(key1 = note1?.id.hashCode() + note1?.thumbnail.hashCode()) {
//								try {
//									if (note1?.thumbnailType == AttachmentObject.Companion.Type.IMAGE.name) {
//										thumbnail1 = note1.thumbnail?.let { BitmapFactory.decodeByteArray(note1.thumbnail, 0, it.size) }
//									}
//								} catch (e : Exception) {
////									e.printStackTrace()
//								}
//							}
//							LaunchedEffect(key1 = note2?.id.hashCode() + note2?.thumbnail.hashCode()) {
//								try {
//									if (note2?.thumbnailType == AttachmentObject.Companion.Type.IMAGE.name) {
//										thumbnail2 = note2.thumbnail?.let { BitmapFactory.decodeByteArray(note2.thumbnail, 0, it.size) }
//									}
//								} catch (e : Exception) {
//									e.printStackTrace()
//								}
//							}
//
//							Spacer(modifier = Modifier.width(14.dp))
//
//							Spacer(
//								modifier = Modifier
//									.width(4.dp)
//									.height(128.dp)
//									.background(MaterialTheme.colorScheme.surface)
//							)
//
//							Spacer(modifier = Modifier.width(6.dp))
//
//							Box(
//								modifier = Modifier.weight(1f)
//							) {
//								if (note1 != null) {
//									NoteGridCard(
//										id = note1.id,
//										timestamp = note1.userTimestamp,
//										showFullTime = false,
//										isLocked = note1.isLocked,
//										isSelected = selectedRealmUUIDList.contains(note1.id),
//										isFavourite = note1.isFavourite,
//										isDeleted = false,
//										isLast = false,
//										title = note1.title,
//										contentThumbnail = note1.contentThumbnail,
//										attachmentCount = note1.attachmentCount,
//										attachmentThumbnail = thumbnail1,
//										address = note1.address,
//										latLng = note1.latLng,
//										isVisible = true,
//										selectedColor = MaterialTheme.colorScheme.surface,
//										onClick = { onClickNote(note1.id) },
//										onLongClick = { onLongClickNote(note1.id) },
//									)
//								}
//							}
//							Box(
//								modifier = Modifier.weight(1f)
//							) {
//								if (note2 != null) {
//									NoteGridCard(
//										id = note2.id,
//										timestamp = note2.userTimestamp,
//										showFullTime = false,
//										isLocked = note2.isLocked,
//										isSelected = selectedRealmUUIDList.contains(note2.id),
//										isFavourite = note2.isFavourite,
//										isDeleted = false,
//										isLast = false,
//										title = note2.title,
//										contentThumbnail = note2.contentThumbnail,
//										attachmentCount = note2.attachmentCount,
//										attachmentThumbnail = thumbnail2,
//										address = note2.address,
//										latLng = note2.latLng,
//										isVisible = true,
//										selectedColor = MaterialTheme.colorScheme.surface,
//										onClick = { onClickNote(note2.id) },
//										onLongClick = { onLongClickNote(note2.id) },
//									)
//								}
//							}
//
//							Spacer(modifier = Modifier.width(8.dp))
//						}
//					}
//				}
//			}
//		}
	}
}
