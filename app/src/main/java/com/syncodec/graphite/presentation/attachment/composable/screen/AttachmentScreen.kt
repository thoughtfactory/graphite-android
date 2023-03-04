package com.syncodec.graphite.presentation.attachment.composable.screen

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.di.model.NoteObjectLite
import com.syncodec.graphite.presentation.attachment.composable.bar.TopBar
import com.syncodec.graphite.presentation.attachment.composable.buildingBlock.AttachmentCard
import com.syncodec.graphite.presentation.attachment.composable.buildingBlock.AttachmentHeader
import com.syncodec.graphite.presentation.attachment.composable.buildingBlock.EmptyView
import com.syncodec.graphite.presentation.attachment.composable.dialog.AttachmentDialog
import com.syncodec.graphite.presentation.attachment.composable.dialog.AttachmentDialogType
import com.syncodec.graphite.presentation.common.ErrorView
import com.syncodec.graphite.presentation.common.LoadingView
import com.syncodec.graphite.presentation.common.scaffold.GenericScaffold
import com.syncodec.graphite.presentation.note.NoteActivity
import com.syncodec.graphite.utils.Extra
import com.syncodec.graphite.utils.LoaderStatus
import com.syncodec.graphite.utils.LocalIsAuthenticated
import com.syncodec.graphite.utils.viewExternally
import org.koin.androidx.compose.koinViewModel
import java.io.File


@Preview
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AttachmentScreen() {
	val context = LocalContext.current
	val viewModel : AttachmentScreenViewModel = koinViewModel()
	val haptic = LocalHapticFeedback.current

	val isAuthenticated = LocalIsAuthenticated.current

	val contentStatus by viewModel.loaderStatus.collectAsState()
	val enableNoteNavigation by viewModel.enableNoteNavigation.collectAsState()
	val noteAttachmentListMap by viewModel.noteAttachmentListMap.collectAsState()

	var isSelecting by remember { mutableStateOf(false) }
	var selectedFileList : List<File> by remember { mutableStateOf(listOf()) }

	var isDeleteDialogVisible by remember { mutableStateOf(false) }

	fun openDialog(editorDialogType : AttachmentDialogType) = when (editorDialogType) {
		AttachmentDialogType.Delete -> isDeleteDialogVisible = true
	}

	fun closeDialog(editorDialogType : AttachmentDialogType) = when (editorDialogType) {
		AttachmentDialogType.Delete -> isDeleteDialogVisible = false
	}

	BackHandler(enabled = isSelecting) {
		isSelecting = false
		selectedFileList = listOf()
	}

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

	fun onClickAttachment(file : File) {
		if (isSelecting) selectedFileList.toMutableList().apply {
			if (contains(file)) remove(file) else add(file)
			selectedFileList = this
		}
		else file.viewExternally(context = context)
	}

	fun onLongClickAttachment(file : File) {
		isSelecting = true
		selectedFileList.toMutableList().apply {
			if (contains(file)) remove(file) else add(file)
			selectedFileList = this
		}
		haptic.performHapticFeedback(HapticFeedbackType.LongPress)
	}

	fun onClickHeader(note : NoteObjectLite, attachmentList : List<File>) {
		if (isSelecting) {
			selectedFileList.toMutableList().apply {
				if (containsAll(attachmentList)) removeAll(attachmentList) else addAll(attachmentList)
				selectedFileList = this
			}
		} else {
			if (enableNoteNavigation) Intent(context, NoteActivity::class.java).apply {
				putExtra(Extra.Companion.Extra.IsNew.name, false)
				putExtra(Extra.Companion.Extra.NoteId.name, note.id.bytes)
				putExtra(Extra.Companion.Extra.Filter.name, Extra.Companion.Filter.SingleRead.name)

				activityLauncher.launch(this)
			}
			else Toast.makeText(context, "Navigating once more will result in loop", Toast.LENGTH_SHORT).show()
		}
	}

	fun onLongClickHeader(attachmentList : List<File>) {
		isSelecting = true
		selectedFileList.toMutableList().apply {
			if (containsAll(attachmentList)) removeAll(attachmentList) else addAll(attachmentList)
			selectedFileList = this
		}
		haptic.performHapticFeedback(HapticFeedbackType.LongPress)
	}

	GenericScaffold(
		topBar = {
			TopBar(
				isSelecting = isSelecting,
				selectedSize = selectedFileList.size,
				onClickCancelSelect = { isSelecting = false; selectedFileList = listOf() },
				onClickDelete = { openDialog(AttachmentDialogType.Delete) },
			)
		},
		bottomBar = {},
		dialogContent = {
			AttachmentDialog(
				isDeleteDialogVisible = isDeleteDialogVisible,
				onDelete = {
					selectedFileList.toList().let {
						viewModel.deleteAttachment(it)
						selectedFileList = listOf()
						isSelecting = false
					}
				},
				closeDialog = ::closeDialog
			)
		}
	) {
		Crossfade(
			targetState = contentStatus,
			animationSpec = tween(300)
		) {
			when (it) {
				LoaderStatus.Init -> LoadingView()
				LoaderStatus.Error -> ErrorView()
				LoaderStatus.Loading -> LoadingView()
				LoaderStatus.LoadedEmpty -> EmptyView()
				LoaderStatus.Loaded -> LazyVerticalGrid(
					columns = GridCells.Adaptive(144.dp),
				) {
					noteAttachmentListMap
						.filter { if (it.key.isLocked) isAuthenticated else true }
						.forEach { (note, attachmentList) ->
							item(
								span = { GridItemSpan(maxCurrentLineSpan) }
							) {
								AttachmentHeader(
									noteId = note.id,
									title = note.title,
									isFavourite = note.isFavourite,
									isLocked = note.isLocked,
									attachmentCount = attachmentList.size,
									onClick = { onClickHeader(note = note, attachmentList = attachmentList) },
									onLongClick = { onLongClickHeader(attachmentList = attachmentList) },
								)
							}
							attachmentList.forEach { file ->
								item {
									AttachmentCard(
										file = file,
										isSelected = file in selectedFileList,
										openNote = {},
										onShare = {},
										onClick = { onClickAttachment(file) },
										onLongClick = { onLongClickAttachment(file) },
									)
								}
							}
							item(span = { GridItemSpan(maxCurrentLineSpan) }) { Box(modifier = Modifier) }
						}
				}
			}
		}
	}
}
