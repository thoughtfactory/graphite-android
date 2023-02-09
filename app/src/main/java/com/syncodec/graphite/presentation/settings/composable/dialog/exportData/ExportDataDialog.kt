package com.syncodec.graphite.presentation.settings.composable.dialog.exportData

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Checkbox
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.di.repository.AttachmentRepository.Companion.attachmentDirPath
import com.syncodec.graphite.presentation.common.dialog.GenericDialog
import com.syncodec.graphite.presentation.common.dialog.buildingBlock.DualActionButtons
import com.syncodec.graphite.utils.compress7z
import com.syncodec.graphite.utils.copyInDirectory
import com.syncodec.graphite.utils.share
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.commons.compress.archivers.sevenz.SevenZOutputFile
import org.koin.androidx.compose.koinViewModel
import java.io.File


@Preview
@Composable
fun ExportDataDialog(
	showDialog : Boolean = true,
	onDismiss : () -> Unit = {},
) {
	val context = LocalContext.current
	val scope = rememberCoroutineScope()
	val viewModel : ExportDataViewModel = koinViewModel()

	var isNotesSelected by remember { mutableStateOf(true) }
	var isBucketSelected by remember { mutableStateOf(true) }
	var isAttachmentSelected by remember { mutableStateOf(true) }
	var isTagSelected by remember { mutableStateOf(true) }

	var isExportingData by remember { mutableStateOf(false) }
	var attachmentCount by remember { mutableStateOf(0) }
	var attachmentProcessed by remember { mutableStateOf(0) }
	var bucketCount by remember { mutableStateOf(0) }
	var bucketProcessed by remember { mutableStateOf(0) }
	var bucketItemCount by remember { mutableStateOf(0) }
	var bucketItemProcessed by remember { mutableStateOf(0) }
	var chapterCount by remember { mutableStateOf(0) }
	var chapterProcessed by remember { mutableStateOf(0) }
	var noteCount by remember { mutableStateOf(0) }
	var noteProcessed by remember { mutableStateOf(0) }
	var tagCount by remember { mutableStateOf(0) }
	var tagProcessed by remember { mutableStateOf(0) }
	var packageCount by remember { mutableStateOf(0) }
	var packageProcessed by remember { mutableStateOf(0) }

	suspend fun exportData(exportObject : ExportDataViewModel.Companion.ExportObject) : File? {
		return null
//		try {
//			val snapshotFolder = File(context.cacheDir, "snapshot").apply { mkdirs() }
//			val currentSnapshotFolder = File(snapshotFolder, "snapshot_${System.currentTimeMillis()}")
//			currentSnapshotFolder.mkdirs()
//
//			val baseFile = File(currentSnapshotFolder.path, "base.json")
//			val attachmentFolder = File(currentSnapshotFolder, "attachment").apply { mkdirs() }
//			val bucketItemFolder = File(currentSnapshotFolder, "bucketItem").apply { mkdirs() }
//			val bucketFolder = File(currentSnapshotFolder, "bucket").apply { mkdirs() }
//			val chapterFolder = File(currentSnapshotFolder, "chapter").apply { mkdirs() }
//			val noteFolder = File(currentSnapshotFolder, "note").apply { mkdirs() }
//			val tagFolder = File(currentSnapshotFolder, "tag").apply { mkdirs() }
//
//			exportObject.baseObject?.let { baseFile.writeText(it) }
//
//			if (isAttachmentSelected) copyInDirectory(File(context.attachmentDirPath()), attachmentFolder)
//
//			withContext(Dispatchers.Main) {
//				exportObject.bucketList?.size?.let { bucketCount = it }
//				exportObject.bucketItemList?.size?.let { bucketItemCount = it }
//				exportObject.chapterList?.size?.let { chapterCount = it }
//				exportObject.noteList?.size?.let { noteCount = it }
//				exportObject.tagList?.size?.let { tagCount = it }
//			}
//
//			exportObject.bucketList?.let {
//				it.forEachIndexed { index, bucketObject ->
//					bucketObject.toSnapshot().toJsonString()?.let { File(bucketFolder.path, "${bucketObject.id}.json").writeText(it) }
//					withContext(Dispatchers.Main) { bucketProcessed = index + 1 }
//				}
//			}
//			exportObject.bucketItemList?.let {
//				it.forEachIndexed { index, bucketItemObject ->
//					bucketItemObject.toSnapshot().toJsonString()?.let { File(bucketItemFolder.path, "${bucketItemObject.id}.json").writeText(it) }
//					withContext(Dispatchers.Main) { bucketItemProcessed = index + 1 }
//				}
//			}
//			exportObject.chapterList?.let {
//				it.forEachIndexed { index, chapterObject ->
//					chapterObject.toSnapshot().toJsonString()?.let { File(chapterFolder.path, "${chapterObject.id}.json").writeText(it) }
//					withContext(Dispatchers.Main) { chapterProcessed = index + 1 }
//				}
//			}
//			exportObject.noteList?.let {
//				it.forEachIndexed { index, noteObject ->
//					noteObject.toSnapshot().toJsonString()?.let { File(noteFolder.path, "${noteObject.id}.json").writeText(it) }
//					withContext(Dispatchers.Main) { noteProcessed = index + 1 }
//				}
//			}
//			exportObject.tagList?.let {
//				it.forEachIndexed { index, tagObject ->
//					tagObject.toSnapshot().toJsonString()?.let { File(tagFolder.path, "${tagObject.id}.json").writeText(it) }
//					withContext(Dispatchers.Main) { tagProcessed = index + 1 }
//				}
//			}
//
//			val sevenZOutput = SevenZOutputFile(File(snapshotFolder, "${currentSnapshotFolder.name}.7z"))
//			compress7z(currentSnapshotFolder, sevenZOutput) { processes, total ->
//				scope.launch(Dispatchers.Main) { packageProcessed = processes; packageCount = total }
//			}
//
//			return File(snapshotFolder, "${currentSnapshotFolder.name}.7z")
//		} catch (e : Exception) {
//			e.printStackTrace()
//			return null
//		}
	}

	GenericDialog(
		showDialog = showDialog && ! isExportingData,
		title = "Export Data",
		contentText = "Select items you want to export",
		dualActionButton = {
			DualActionButtons(
				primaryText = "Export",
				secondaryText = "Cancel",
				onClickPrimary = {
					isExportingData = true
					scope.launch(Dispatchers.Default) {
						viewModel.exportData(
							isNotesSelected = isNotesSelected,
							isBucketSelected = isBucketSelected,
							isTagSelected = isTagSelected
						).let {
							exportData(it)?.share(context)
							isExportingData = false
							onDismiss()
							attachmentCount = 0
							attachmentProcessed = 0
							bucketCount = 0
							bucketProcessed = 0
							bucketItemCount = 0
							bucketItemProcessed = 0
							chapterCount = 0
							chapterProcessed = 0
							noteCount = 0
							noteProcessed = 0
							tagCount = 0
							tagProcessed = 0
							packageCount = 0
							packageProcessed = 0
						}
					}
				},
				onClickSecondary = onDismiss,
			)
		},
		onDismissRequest = onDismiss
	) {
		Spacer(modifier = Modifier.height(24.dp))
		ExportItemSelector(text = "Chapters and Notes", checked = isNotesSelected) { isNotesSelected = it }
		ExportItemSelector(text = "Buckets and Bucket Items", checked = isBucketSelected) { isBucketSelected = it }
		ExportItemSelector(text = "Attachments", checked = isAttachmentSelected) { isAttachmentSelected = it }
		ExportItemSelector(text = "Tags", checked = isTagSelected) { isTagSelected = it }
	}

	GenericDialog(
		showDialog = showDialog && isExportingData,
		title = "Exporting Data",
		contentText = "Please wait while we are exporting your data",
	) {
		Spacer(modifier = Modifier.height(6.dp))

		ProgressView(text = "Attachment", total = attachmentCount, processed = attachmentProcessed)
		Spacer(modifier = Modifier.height(2.dp))
		ProgressView(text = "Bucket", total = bucketItemCount, processed = bucketItemProcessed)
		Spacer(modifier = Modifier.height(2.dp))
		ProgressView(text = "Bucket item", total = bucketCount, processed = bucketProcessed)
		Spacer(modifier = Modifier.height(2.dp))
		ProgressView(text = "Chapter", total = chapterCount, processed = chapterProcessed)
		Spacer(modifier = Modifier.height(2.dp))
		ProgressView(text = "Note", total = noteCount, processed = noteProcessed)
		Spacer(modifier = Modifier.height(2.dp))
		ProgressView(text = "Tag", total = tagCount, processed = tagProcessed)

		Spacer(modifier = Modifier.height(12.dp))
		Text(
			text = "Packaging...",
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onBackground,
			fontWeight = FontWeight.Bold,
		)
		Spacer(modifier = Modifier.height(4.dp))
		LinearProgressIndicator(
			progress = (packageProcessed.toFloat() / maxOf(1, packageCount).toFloat()),
			color = MaterialTheme.colorScheme.onBackground,
			trackColor = MaterialTheme.colorScheme.background,
			modifier = Modifier.fillMaxWidth(),
		)
	}
}

@Preview
@Composable
fun ExportItemSelector(
	text : String = "Notes",
	checked : Boolean = true,
	onCheckedChange : (Boolean) -> Unit = {},
) {
	Row(
		verticalAlignment = Alignment.CenterVertically,
		modifier = Modifier.fillMaxWidth(),
	) {
		Checkbox(
			checked = checked,
			onCheckedChange = onCheckedChange,
			modifier = Modifier.requiredSize(32.dp),
			interactionSource = remember { MutableInteractionSource() },
		)
		Spacer(modifier = Modifier.width(8.dp))
		Text(
			text = text,
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onBackground,
			modifier = Modifier.weight(1f),
		)
	}
}

@Preview
@Composable
private fun ProgressView(
	text : String = "Notes",
	total : Int = 71,
	processed : Int = 47,
) {
	Box(
		modifier = Modifier
			.fillMaxWidth()
			.padding(12.dp, 8.dp)
	) {
		Row(
			modifier = Modifier.fillMaxWidth()
		) {
			Text(
				text = "$text: ",
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onBackground,
				fontWeight = FontWeight.Bold
			)

			Spacer(modifier = Modifier.weight(1f))

			Text(
				text = " $processed of $total",
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onBackground,
				fontWeight = FontWeight.Bold
			)
		}
	}
}
