package com.syncodec.graphite.presentation.attachment.composable.screen

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.local.NoteObjectLite
import com.syncodec.graphite.presentation.attachment.composable.bar.BottomBar
import com.syncodec.graphite.presentation.attachment.composable.bar.TopBar
import com.syncodec.graphite.presentation.attachment.composable.buildingBlock.AttachmentCard
import com.syncodec.graphite.presentation.attachment.composable.buildingBlock.AttachmentHeader
import com.syncodec.graphite.presentation.attachment.composable.buildingBlock.EmptyView
import com.syncodec.graphite.presentation.common.dialog.dialog2.DeleteDialog
import com.syncodec.graphite.presentation.common.scaffold.GenericScaffold2
import com.syncodec.graphite.presentation.common.selectionAction.AttachmentSelectionActionView
import com.syncodec.graphite.presentation.note.NoteActivity
import com.syncodec.graphite.utils.Extra
import com.syncodec.graphite.utils.share
import com.syncodec.graphite.utils.viewExternally
import com.syncodec.graphite.utils.xor
import java.io.File


@Preview
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttachmentScreen(
	enableNoteNavigation: Boolean = false,
	noteAttachmentListMap: Map<NoteObjectLite, Set<File>> = mapOf(),
	deleteAttachment: (Set<File>) -> Unit = {},
) {
	val context = LocalContext.current
	val haptic = LocalHapticFeedback.current

	var isSelecting by remember { mutableStateOf(false) }
	var selectedFileList: Set<File> by remember { mutableStateOf(setOf()) }

	var isDeleteDialogVisible by remember { mutableStateOf(false) }

	BackHandler(enabled = isSelecting) {
		isSelecting = false
		selectedFileList = setOf()
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
							} catch (e: Exception) {
								null
							}
						}
					}
				}
				Extra.Companion.Extra.INTENT_ACTION.name
				Extra.Companion.Extra.OBJECT_ID.name
			}
		} catch (e: Exception) {
			Toast.makeText(context, "Error performing action", Toast.LENGTH_SHORT).show()
		}
	}

	fun onClickAttachment(file: File) {
		if (isSelecting) selectedFileList.toMutableSet().apply {
			xor(file)
			selectedFileList = this
		}
		else file.viewExternally(context = context)
	}

	fun onLongClickAttachment(file: File) {
		isSelecting = true
		selectedFileList.toMutableSet().apply {
			xor(file)
			selectedFileList = this
		}
		haptic.performHapticFeedback(HapticFeedbackType.LongPress)
	}

	fun onClickHeader(note: NoteObjectLite, attachmentList: Set<File>) {
		if (isSelecting) {
			selectedFileList.toMutableSet().apply {
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

	fun onLongClickHeader(attachmentList: Set<File>) {
		isSelecting = true
		selectedFileList.toMutableSet().apply {
			if (containsAll(attachmentList)) removeAll(attachmentList) else addAll(attachmentList)
			selectedFileList = this
		}
		haptic.performHapticFeedback(HapticFeedbackType.LongPress)
	}

	GenericScaffold2(
		topBar = { TopBar() },
		bottomBar = { BottomBar() },
		isTopBarVisible = !isSelecting,
		isBottomBarVisible = !isSelecting,
		dialogContent = {
			DeleteDialog(
				isDialogVisible = isDeleteDialogVisible,
				onDismissRequest = { isDeleteDialogVisible = false },
				title = stringResource(id = R.string.delete_items_multiple),
				contentText = stringResource(id = R.string.are_you_sure_delete_multiple),
				onConfirmDelete = { deleteAttachment(selectedFileList.toSet()); selectedFileList = setOf(); isDeleteDialogVisible = false }
			)
		}
	) {
		if (noteAttachmentListMap.isEmpty()) EmptyView()
		else LazyVerticalGrid(
			columns = GridCells.Adaptive(144.dp),
			modifier = Modifier.fillMaxSize()
		) {
			noteAttachmentListMap
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
							onClick = { onClickHeader(note = note, attachmentList = attachmentList.toSet()) },
							onLongClick = { onLongClickHeader(attachmentList = attachmentList.toSet()) },
						)
					}
					attachmentList.forEach { file ->
						item {
							AttachmentCard(
								file = file,
								isSelected = file in selectedFileList,
								onClick = { onClickAttachment(file) },
							) { onLongClickAttachment(file) }
						}
					}
					item(span = { GridItemSpan(maxCurrentLineSpan) }) { Box(modifier = Modifier) }
				}
		}

		AttachmentSelectionActionView(
			modifier = Modifier
				.padding(start = 24.dp, top = 0.dp, end = 24.dp, bottom = 32.dp)
				.align(Alignment.BottomCenter),
			isSelecting = isSelecting,
			selectedItemCount = selectedFileList.size,
			onClickShare = { selectedFileList.share(context = context) },
			onClickDelete = { isDeleteDialogVisible = true },
		)
	}
}
