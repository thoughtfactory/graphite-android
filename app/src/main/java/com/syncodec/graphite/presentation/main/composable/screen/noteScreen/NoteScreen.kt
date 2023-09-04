package com.syncodec.graphite.presentation.main.composable.screen.noteScreen

import android.content.Intent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.BaseApplication
import com.syncodec.graphite.BuildConfig
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.LoadingView
import com.syncodec.graphite.presentation.common.dialog.dialog2.DeleteDialog
import com.syncodec.graphite.presentation.common.scaffold.GenericScaffold2
import com.syncodec.graphite.presentation.common.selectionAction.MainSelectionActionView
import com.syncodec.graphite.presentation.main.composable.buildingBlock.NoteFloatingActionButton
import com.syncodec.graphite.presentation.main.composable.screen.noteScreen.buildingBlock.noteGrid.NoteGrid
import com.syncodec.graphite.presentation.main.composable.screen.noteScreen.buildingBlock.noteList.NoteList
import com.syncodec.graphite.presentation.note2.NoteActivity2
import com.syncodec.graphite.utils.Extra
import com.syncodec.graphite.utils.ViewType
import com.syncodec.graphite.utils.dataStore.DataStoreInstance
import io.realm.kotlin.types.RealmUUID
import org.koin.androidx.compose.koinViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteScreen(
	isSelecting: Boolean = false,
	selectedIdList: Set<RealmUUID> = setOf(),
	onSelect: (RealmUUID) -> Unit = {},
	onUnSelectAll: () -> Unit = {},
) {
	val context = LocalContext.current
	val viewModel: NoteScreenViewModel = koinViewModel()

	val dataStoreInstance = remember { DataStoreInstance(context = context) }
	val viewType by dataStoreInstance.getViewType.collectAsState(null)

	val defaultChapterId by viewModel.defaultChapterId.collectAsState()
	val noteMap by viewModel.mappedNoteList.collectAsState()

	var isDeleteDialogVisible by remember { mutableStateOf(false) }

	fun onClickNote(id: RealmUUID) {
		if (isSelecting) {
			onSelect(id)
		}
		else {
			Intent(context, NoteActivity2::class.java).apply {
				putExtra(Extra.Companion.Extra.IsNew.name, false)
				putExtra(Extra.Companion.Extra.NoteId.name, id.bytes)
				putExtra(Extra.Companion.Extra.Filter.name, Extra.Companion.Filter.SingleRead.name)
				context.startActivity(this)
			}
		}
	}

	GenericScaffold2(
		floatingActionButton = {
			AnimatedVisibility(
				visible = !isSelecting,
				enter = fadeIn(tween(470)) + scaleIn(tween(470)),
				exit = fadeOut(tween(470)) + scaleOut(tween(470))
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
							Intent(context, NoteActivity2::class.java).apply {
								putExtra(Extra.Companion.Extra.IsNew.name, true)
								putExtra(Extra.Companion.Extra.ParentId.name, defaultChapterId?.bytes)
								putExtra(Extra.Companion.Extra.Filter.name, Extra.Companion.Filter.SingleRead.name)
								context.startActivity(this)
							}
						}
					}
				}
				else {
					NoteFloatingActionButton(isExpanded = true) {
						Intent(context, NoteActivity2::class.java).apply {
							putExtra(Extra.Companion.Extra.IsNew.name, true)
							putExtra(Extra.Companion.Extra.ParentId.name, defaultChapterId?.bytes)
							putExtra(Extra.Companion.Extra.Filter.name, Extra.Companion.Filter.SingleRead.name)
							context.startActivity(this)
						}
					}
				}
			}
		},
	) {
		AnimatedContent(
			targetState = viewType,
			transitionSpec = { (fadeIn(tween(470)) + scaleIn(tween(470), 0.71f)).togetherWith(fadeOut(tween(470)) + scaleOut(tween(470), 0.71f)) },
			modifier = Modifier.fillMaxSize(),
			label = "viewType_animation"
		) { viewType1 ->
			when (viewType1) {
				ViewType.List -> NoteList(
					noteMap = noteMap,
					isSelecting = isSelecting,
					selectedIdList = selectedIdList,
					onClickNote = ::onClickNote,
					onLongClickNote = { onSelect(it) },
				)

				ViewType.Grid -> NoteGrid(
					noteMap = noteMap,
					isSelecting = isSelecting,
					selectedIdList = selectedIdList,
					onClickNote = ::onClickNote,
					onLongClickNote = { onSelect(it) },
				)

				else -> LoadingView()
			}
		}

		val isAllFavourite by remember(noteMap.values, selectedIdList) { derivedStateOf { noteMap.values.flatten().filter { it.id in selectedIdList }.all { it.isFavourite } } }
		val isAllLocked by remember(noteMap.values, selectedIdList) { derivedStateOf { noteMap.values.flatten().filter { it.id in selectedIdList }.all { it.isLocked } } }
		MainSelectionActionView(
			modifier = Modifier
				.align(Alignment.BottomCenter)
				.padding(start = 24.dp, top = 0.dp, end = 24.dp, bottom = 32.dp),
			isSelecting = isSelecting,
			isAllItemFavourite = selectedIdList.isNotEmpty() && isAllFavourite,
			isAllItemLocked = selectedIdList.isNotEmpty() && isAllLocked,
			selectedItemCount = selectedIdList.size,
			onClickDelete = { isDeleteDialogVisible = true },
			onClickFavourite = { viewModel.onClickMultiFavourite(idList = selectedIdList, isAllFavourite = isAllFavourite) },
			onClickLock = { viewModel.onClickMultiLock(idList = selectedIdList, isAllLocked = isAllLocked) },
		)

		DeleteDialog(
			isDialogVisible = isDeleteDialogVisible,
			onDismissRequest = { isDeleteDialogVisible = false },
			title = stringResource(id = R.string.delete_items_multiple),
			contentText = stringResource(id = R.string.are_you_sure_delete_multiple),
			onConfirmDelete = { isDeleteDialogVisible = false; viewModel.delete(selectedIdList); onUnSelectAll() },
		)
	}
}
