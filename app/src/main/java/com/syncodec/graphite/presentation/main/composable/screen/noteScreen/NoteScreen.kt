package com.syncodec.graphite.presentation.main.composable.screen.noteScreen

import android.content.Intent
import android.graphics.Bitmap
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.NoteObjectLite
import com.syncodec.graphite.presentation.common.ErrorView
import com.syncodec.graphite.presentation.common.LoadingView
import com.syncodec.graphite.presentation.common.scaffold.GenericScaffold
import com.syncodec.graphite.presentation.main.composable.buildingBlock.EmptyView
import com.syncodec.graphite.presentation.main.composable.buildingBlock.NoteFloatingActionButton
import com.syncodec.graphite.presentation.main.composable.buildingBlock.notebook.gridView.NoteGridCard
import com.syncodec.graphite.presentation.main.composable.screen.noteScreen.buildingBlock.NoteList
import com.syncodec.graphite.presentation.note.NoteActivity
import com.syncodec.graphite.utils.ContentStatus
import com.syncodec.graphite.utils.DataStoreInstance
import com.syncodec.graphite.utils.Extra
import com.syncodec.graphite.utils.ViewType
import io.realm.kotlin.types.RealmUUID
import org.koin.androidx.compose.koinViewModel
import kotlin.random.Random


@OptIn(ExperimentalAnimationApi::class, ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)
@Composable
fun NoteScreen(
	isSelecting : Boolean,
	onSelect : (RealmUUID) -> Unit,
	selectedIdList : List<RealmUUID>,
) {
	val context = LocalContext.current
	val viewModel : NoteScreenViewModel = koinViewModel()

	val dataStoreInstance = remember { DataStoreInstance(context = context) }
	val sortBy by dataStoreInstance.getSortBy.collectAsState(null)
	val sortOn by dataStoreInstance.getSortOn.collectAsState(null)
	val viewType by dataStoreInstance.getViewType.collectAsState(null)

	val contentStatus by viewModel.contentStatus.collectAsState()

	val defaultChapterId by viewModel.defaultChapterId.collectAsState()
	val isNoteRefreshing by viewModel.isNoteRefreshing.collectAsState()
	val noteList by viewModel.noteList.collectAsState()
	val tagList by viewModel.tagList.collectAsState()

	val activityLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
		try {
			it.data?.let { intent ->
				val hasIntentAction = intent.hasExtra(Extra.Companion.Extra.INTENT_ACTION.name)
				if (hasIntentAction) {
					val intentAction = intent.getStringExtra(Extra.Companion.Extra.INTENT_ACTION.name)?.let { it1 ->
						Extra.Companion.IntentAction.valueOf(it1)
					}
					if (intentAction == Extra.Companion.IntentAction.DELETE) {
						val hasObjectId = intent.hasExtra(Extra.Companion.Extra.OBJECT_ID.name)
						if (hasObjectId) intent.getByteArrayExtra(Extra.Companion.Extra.OBJECT_ID.name)?.let { bytes ->
							try {
								viewModel.delete(listOf(RealmUUID.from(bytes)))
							} catch (e : Exception) {
								null
							}
						}
					}
				}
				Extra.Companion.Extra.INTENT_ACTION.name
				Extra.Companion.Extra.OBJECT_ID.name
			}
		} catch (e : Exception) {
			Toast.makeText(context, "Error performing action", Toast.LENGTH_SHORT).show()
		}
	}

	fun onClickNote(id : RealmUUID) {
		if (isSelecting) {
			onSelect(id)
		} else {
			Intent(context, NoteActivity::class.java).apply {
				putExtra(Extra.Companion.Extra.IsNew.name, false)
				putExtra(Extra.Companion.Extra.NoteId.name, id.bytes)
				putExtra(Extra.Companion.Extra.Filter.name, Extra.Companion.Filter.SingleRead.name)

				activityLauncher.launch(this)
			}
		}
	}

	fun onLongClickNote(id : RealmUUID) = onSelect(id)

	GenericScaffold(
		floatingActionButton = {
			AnimatedVisibility(
				visible = ! isSelecting,
				enter = fadeIn(tween(300)) + scaleIn(tween(300)),
				exit = fadeOut(tween(300)) + scaleOut(tween(300))
			) {
				NoteFloatingActionButton(isExpanded = true) {
					Intent(context, NoteActivity::class.java).apply {
						putExtra(Extra.Companion.Extra.IsNew.name, true)
						putExtra(Extra.Companion.Extra.ParentId.name, defaultChapterId?.bytes)
						putExtra(Extra.Companion.Extra.Filter.name, Extra.Companion.Filter.SingleRead.name)

						activityLauncher.launch(this)
					}
				}
			}
		},
	) {
		Crossfade(
			targetState = contentStatus,
			animationSpec = tween(300)
		) {
			when (it) {
				ContentStatus.Init -> LoadingView()
				ContentStatus.Error -> ErrorView()
				ContentStatus.Loading -> LoadingView()
				ContentStatus.LoadedEmpty -> EmptyView(
					image = remember { if (Random.nextBoolean()) R.drawable.il_writing_b else R.drawable.il_writing_g },
					title = "The town was paper, but the memories were not.",
					subTitle = "― John Green, Paper Towns",
				)

				ContentStatus.Loaded -> AnimatedContent(
					targetState = viewType,
					transitionSpec = { fadeIn(tween(300)) + scaleIn(tween(300), 0.71f) with fadeOut(tween(300)) + scaleOut(tween(300), 0.71f) },
					modifier = Modifier.fillMaxSize()
				) {
					when (it) {
						ViewType.LIST -> NoteList(
							noteList = noteList,
							isRefreshing = isNoteRefreshing,
							isSelecting = isSelecting,
							selectedIdList = selectedIdList,
							tagList = tagList,
							onClickNote = ::onClickNote,
							onLongClickNote = ::onLongClickNote,
							onRefresh = viewModel::refresh,
						)

						ViewType.GRID -> null
						else -> LoadingView()
					}
				}
			}
		}
	}
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun GridView(
	lazyStaggeredGridState : LazyStaggeredGridState,
	isRefreshing : Boolean,
	isSelecting : Boolean,
	selectedIdList : List<RealmUUID>,
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
			state = lazyStaggeredGridState,
			columns = StaggeredGridCells.Fixed(2),
			contentPadding = PaddingValues(8.dp, 0.dp),
			modifier = Modifier.fillMaxSize(),
		) {
			noteDayMap.forEach { day, noteList ->
				noteList.forEach { note ->
					item(
						key = note.id.toString(),
						contentType = note
					) {
						var thumbnail by remember { mutableStateOf<Bitmap?>(null) }

//						LaunchedEffect(key1 = note.id.hashCode() + note.thumbnail.hashCode()) {
//							try {
//								if (note.thumbnailType == AttachmentType.IMAGE.name.lowercase()) thumbnail = note.thumbnail
//							} catch (e : Exception) {
////								e.printStackTrace()
//							}
//						}

						NoteGridCard(
							id = note.id,
							timestamp = note.userTimestamp,
							showFullTime = true,
							isLocked = note.isLocked,
							isSelected = note.id in selectedIdList,
							isFavourite = note.isFavourite,
							isDeleted = false,
							isLast = false,
							title = note.title,
							contentThumbnail = note.contentThumbnail,
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
