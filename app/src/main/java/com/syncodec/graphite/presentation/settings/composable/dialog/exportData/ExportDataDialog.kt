package com.syncodec.graphite.presentation.settings.composable.dialog.exportData

import android.widget.Toast
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.dialog.GenericDialog
import com.syncodec.graphite.presentation.common.dialog.buildingBlock.DualActionButtons
import com.syncodec.graphite.utils.compress7z
import com.syncodec.graphite.utils.share
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
	val uriHandler = LocalUriHandler.current

	var isNotesSelected by remember { mutableStateOf(true) }
	var isBucketSelected by remember { mutableStateOf(true) }
	var isAttachmentSelected by remember { mutableStateOf(true) }
	var isTagSelected by remember { mutableStateOf(true) }

	var isExportingData by remember { mutableStateOf(false) }

	fun exportData() {
		isExportingData = true
		scope.launch(Dispatchers.Default) {
			viewModel.exportData(
				isNotesSelected = isNotesSelected,
				isBucketSelected = isBucketSelected,
				isTagSelected = isTagSelected
			).let { exportObject ->

				File(context.cacheDir, "export").let { exportDir ->
					exportDir.deleteRecursively()
					exportDir.mkdirs()

					val exportDirName = "graphite_export_${System.currentTimeMillis()}"
					val exportFileDir = File(exportDir, exportDirName).also { exportFileDir ->
						exportFileDir.mkdirs()

						exportObject.baseObject?.let { baseObject ->
							File(exportFileDir, "base_object.json").let { baseObjectFile ->
								baseObjectFile.createNewFile()
								viewModel.objectMapper.writeValueAsString(baseObject).let { jsonString -> baseObjectFile.writeText(jsonString) }
							}
						}
						exportObject.bucketList?.let { bucketList ->
							File(exportFileDir, "bucket").let { bucketFile ->
								bucketFile.mkdirs()
								bucketList.forEach { bucket ->
									File(bucketFile, "${bucket.id}.json").let { bucketFile ->
										bucketFile.createNewFile()
										viewModel.objectMapper.writeValueAsString(bucket).let { jsonString -> bucketFile.writeText(jsonString) }
									}
								}
							}
						}
						exportObject.bucketItemList?.let { bucketItemList ->
							File(exportFileDir, "bucketItem").let { bucketItemFile ->
								bucketItemFile.mkdirs()
								bucketItemList.forEach { bucketItem ->
									File(bucketItemFile, "${bucketItem.id}.json").let { bucketItemFile ->
										bucketItemFile.createNewFile()
										viewModel.objectMapper.writeValueAsString(bucketItem).let { jsonString -> bucketItemFile.writeText(jsonString) }
									}
								}
							}
						}
						exportObject.chapterList?.let { chapterList ->
							File(exportFileDir, "chapter").let { chapterFile ->
								chapterFile.mkdirs()
								chapterList.forEach { chapter ->
									File(chapterFile, "${chapter.id}.json").let { chapterFile ->
										chapterFile.createNewFile()
										viewModel.objectMapper.writeValueAsString(chapter).let { jsonString -> chapterFile.writeText(jsonString) }
									}
								}
							}
						}
						exportObject.noteList?.let { noteList ->
							File(exportFileDir, "note").let { noteFile ->
								noteFile.mkdirs()
								noteList.forEach { note ->
									File(noteFile, "${note.id}.json").let { noteFile ->
										noteFile.createNewFile()
										viewModel.objectMapper.writeValueAsString(note).let { jsonString -> noteFile.writeText(jsonString) }
									}
								}
							}
						}
						exportObject.tagList?.let { tagList ->
							File(exportFileDir, "tag").let { tagFile ->
								tagFile.mkdirs()
								tagList.forEach { tag ->
									File(tagFile, "${tag.id}.json").let { tagFile ->
										tagFile.createNewFile()
										viewModel.objectMapper.writeValueAsString(tag).let { jsonString -> tagFile.writeText(jsonString) }
									}
								}
							}
						}
						viewModel.attachmentRepository.getAttachmentDir().let {
							File(exportFileDir, "attachment").let { attachmentFile ->
								attachmentFile.mkdirs()
								it.copyRecursively(attachmentFile)
							}
						}
					}

					val sevenZFile = File(exportDir, "$exportDirName.7z")
					val sevenZOutput = SevenZOutputFile(sevenZFile)
					compress7z(exportFileDir, sevenZOutput) { progress, total -> }

					sevenZOutput.close()
					sevenZFile.share(context)
				}

				isExportingData = false
				onDismiss()
			}
		}
	}

	GenericDialog(
		showDialog = showDialog && ! isExportingData,
		title = "Export Data",
		contentText = "Select items you want to export",
		dualActionButton = {
			DualActionButtons(
				primaryText = "Export",
				secondaryText = "Cancel",
				onClickPrimary = { exportData() },
				onClickSecondary = onDismiss,
			)
		},
		thirdActionButton = {
			this.apply {
				Row(
					verticalAlignment = Alignment.Bottom,
					modifier = Modifier.fillMaxWidth(),
				) {
					ClickableText(
						text = buildAnnotatedString {
							val text = "Learn more about exporting data"
							this.addStyle(
								style = SpanStyle(
									color = MaterialTheme.colorScheme.primary,
									textDecoration = TextDecoration.Underline,
									fontStyle = FontStyle.Italic,
									fontSize = MaterialTheme.typography.bodyMedium.fontSize,
									fontFamily = MaterialTheme.typography.bodyMedium.fontFamily,
								),
								start = 0,
								end = text.length
							)
							append(text)
						},
						onClick = {
							try {
								uriHandler.openUri("https://graphite.syncodec.com/#/data/export/a_thing_or_two_about_exported_data")
							} catch (e : Exception) {
								Toast.makeText(context, "Error opening link", Toast.LENGTH_SHORT).show()
							}
						},
					)
					Spacer(modifier = Modifier.requiredWidth(4.dp))
					Icon(
						painter = painterResource(id = R.drawable.ic_launch),
						contentDescription = "Learn how to import data",
						tint = MaterialTheme.colorScheme.onBackground,
						modifier = Modifier.requiredSize(16.dp)
					)
				}
				Spacer(modifier = Modifier.height(8.dp))
			}
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
		contentText = "Please wait while your data is being exported",
	) {
		Spacer(modifier = Modifier.height(12.dp))

		LinearProgressIndicator(
			color = MaterialTheme.colorScheme.onBackground,
			trackColor = MaterialTheme.colorScheme.background,
			modifier = Modifier.fillMaxWidth(),
		)
	}
}

@Preview
@Composable
private fun ExportItemSelector(
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
