package com.syncodec.graphite.presentation.main.composable.screen.noteScreen

import android.content.Intent
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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.BaseApplication
import com.syncodec.graphite.BuildConfig
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.NoteObjectLite
import com.syncodec.graphite.presentation.common.ErrorView
import com.syncodec.graphite.presentation.common.LoadingView
import com.syncodec.graphite.presentation.common.scaffold.GenericScaffold
import com.syncodec.graphite.presentation.main.composable.buildingBlock.EmptyView
import com.syncodec.graphite.presentation.main.composable.buildingBlock.NoteFloatingActionButton
import com.syncodec.graphite.presentation.main.composable.screen.noteScreen.buildingBlock.noteGrid.NoteGrid
import com.syncodec.graphite.presentation.main.composable.screen.noteScreen.buildingBlock.noteList.NoteList
import com.syncodec.graphite.presentation.note.NoteActivity
import com.syncodec.graphite.utils.ContentStatus
import com.syncodec.graphite.utils.dataStore.DataStoreInstance
import com.syncodec.graphite.utils.Extra
import com.syncodec.graphite.utils.LocalIsAuthenticated
import com.syncodec.graphite.utils.SortBy
import com.syncodec.graphite.utils.SortOn
import com.syncodec.graphite.utils.ViewType
import com.syncodec.graphite.utils.timeStampToPrettyDay
import com.syncodec.graphite.utils.timestampToCalendarDay
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
	val isAuthenticated = LocalIsAuthenticated.current
	val sortBy by dataStoreInstance.getSortBy.collectAsState(null)
	val sortOn by dataStoreInstance.getSortOn.collectAsState(null)
	val viewType by dataStoreInstance.getViewType.collectAsState(null)

	val contentStatus by viewModel.contentStatus.collectAsState()

	val defaultChapterId by viewModel.defaultChapterId.collectAsState()
	val tagList by viewModel.tagList.collectAsState()

	val noteList = contentStatus.dataOrNull ?: listOf()

	var noteMap : Map<String, List<NoteObjectLite>> by remember { mutableStateOf(mapOf()) }
	LaunchedEffect(noteList, isAuthenticated, sortBy, sortOn) {
		noteList.filter { if (it.isLocked) isAuthenticated else true }.let { noteList ->
			noteMap = when (sortOn) {
				SortOn.Title -> (if (sortBy == SortBy.Ascending) noteList.sortedBy { it.title } else noteList.sortedByDescending { it.title }).let {
					it.groupBy { it.title?.firstOrNull()?.lowercase() ?: "." }
						.toSortedMap { o1, o2 ->
							when {
								o1 == "." -> if (sortBy == SortBy.Ascending) 1 else - 1
								o2 == "." -> if (sortBy == SortBy.Ascending) - 1 else 1
								else -> o1.compareTo(o2)
							}
						}
				}

				SortOn.Timestamp -> (if (sortBy == SortBy.Ascending) noteList.sortedBy { it.userTimestamp } else noteList.sortedByDescending { it.userTimestamp }).let {
					it.groupBy { timestampToCalendarDay(it.userTimestamp).timeStampToPrettyDay() }
				}

				SortOn.Modified -> (if (sortBy == SortBy.Ascending) noteList.sortedBy { it.modifiedTimestamp } else noteList.sortedByDescending { it.modifiedTimestamp }).let {
					it.groupBy { timestampToCalendarDay(it.modifiedTimestamp).timeStampToPrettyDay() }
				}

				else -> (if (sortBy == SortBy.Ascending) noteList.sortedBy { it.userTimestamp } else noteList.sortedByDescending { it.userTimestamp }).let {
					it.groupBy { timestampToCalendarDay(it.userTimestamp).timeStampToPrettyDay() }
				}
			}
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
				context.startActivity(this)
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
				if (BuildConfig.DEBUG) {
					Column(
						modifier = Modifier,
						horizontalAlignment = Alignment.End,
					) {
						FloatingActionButton(onClick = { BaseApplication.isPro.tryEmit(true) }) {
							Icon(painter = painterResource(id = R.drawable.ic_star), contentDescription = null)
						}

						Spacer(modifier = Modifier.height(16.dp))

						FloatingActionButton(onClick = { viewModel.addDebugData() }) {
							Icon(painter = painterResource(id = R.drawable.ic_bug), contentDescription = null)
						}

						Spacer(modifier = Modifier.height(16.dp))

						NoteFloatingActionButton(isExpanded = true) {
							Intent(context, NoteActivity::class.java).apply {
								putExtra(Extra.Companion.Extra.IsNew.name, true)
								putExtra(Extra.Companion.Extra.ParentId.name, defaultChapterId)
								putExtra(Extra.Companion.Extra.Filter.name, Extra.Companion.Filter.SingleRead.name)
								context.startActivity(this)
							}
						}
					}
				} else {
					NoteFloatingActionButton(isExpanded = true) {
						Intent(context, NoteActivity::class.java).apply {
							putExtra(Extra.Companion.Extra.IsNew.name, true)
							putExtra(Extra.Companion.Extra.ParentId.name, defaultChapterId)
							putExtra(Extra.Companion.Extra.Filter.name, Extra.Companion.Filter.SingleRead.name)
							context.startActivity(this)
						}
					}
				}
			}
		},
	) {
		val pullRefreshState = rememberPullRefreshState(
			refreshing = contentStatus is ContentStatus.Loading,
			onRefresh = viewModel::refresh
		)

		Box(
			contentAlignment = Alignment.TopCenter,
			modifier = Modifier
				.fillMaxSize()
				.pullRefresh(pullRefreshState)
		) {

			Crossfade(
				targetState = contentStatus,
				animationSpec = tween(300)
			) { contentStatus1 ->
				when (contentStatus1) {
					is ContentStatus.Init -> LoadingView()
					is ContentStatus.Error -> ErrorView()
					is ContentStatus.Loading -> LoadingView()
					is ContentStatus.LoadedEmpty -> EmptyView(
						image = remember { if (Random.nextBoolean()) R.drawable.il_writing_b else R.drawable.il_writing_g },
						title = "The town was paper, but the memories were not.",
						subTitle = "― John Green, Paper Towns",
					)

					is ContentStatus.Loaded -> {
						AnimatedContent(
							targetState = viewType,
							transitionSpec = { fadeIn(tween(300)) + scaleIn(tween(300), 0.71f) with fadeOut(tween(300)) + scaleOut(tween(300), 0.71f) },
							modifier = Modifier.fillMaxSize()
						) {
							when (it) {
								ViewType.List -> NoteList(
									noteMap = noteMap,
									isSelecting = isSelecting,
									selectedIdList = selectedIdList,
									tagList = tagList,
									onClickNote = ::onClickNote,
									onLongClickNote = ::onLongClickNote,
								)

								ViewType.Grid -> NoteGrid(
									noteMap = noteMap,
									isSelecting = isSelecting,
									selectedIdList = selectedIdList,
									tagList = tagList,
									onClickNote = ::onClickNote,
									onLongClickNote = ::onLongClickNote,
								)

								else -> LoadingView()
							}
						}
					}
				}
			}

			PullRefreshIndicator(
				refreshing = contentStatus is ContentStatus.Loading,
				state = pullRefreshState,
				backgroundColor = MaterialTheme.colorScheme.background,
				contentColor = MaterialTheme.colorScheme.onBackground,
				scale = true,
			)
		}
	}
}
