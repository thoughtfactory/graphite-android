package com.syncodec.graphite.presentation.note.composable.bottomSheet

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.custom.bottomSheet.BottomSheetHeader
import com.syncodec.graphite.presentation.custom.bottomSheet.BottomSheetStrip
import com.syncodec.graphite.presentation.custom.button.MenuBottomSheetButton
import com.syncodec.graphite.presentation.custom.button.MenuBottomSheetButtonData
import com.syncodec.graphite.presentation.note.NoteActivity
import com.syncodec.graphite.presentation.note.NoteViewModel
import com.syncodec.graphite.presentation.note.composable.buildingBlock.renderAttachment.AttachmentPreview
import com.syncodec.graphite.utils.createTempFileToExpose
import com.syncodec.graphite.utils.generatePrimaryKey


@Composable
fun AttachmentBottomSheet(
	closeSheet: () -> Unit
) {
	val activity: NoteActivity = LocalContext.current as NoteActivity
	val viewModel: NoteViewModel = viewModel()

	val attachmentList = viewModel.attachmentListVisible.toList().sortedBy {
		it.second.third.createdTimestamp
	}


	var photoUri: Uri? = null
	val takePicture =
		rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { isCaptured ->
			if (isCaptured) {
				if (photoUri != null) {
					viewModel.bufferAttachment(listOf(photoUri!!))
					photoUri = null
				}
			}
		}
	val openFilePicker = rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenMultipleDocuments()) { uriList ->
		viewModel.bufferAttachment(uriList)
	}

	val buttonList: List<MenuBottomSheetButtonData> = listOf(
		MenuBottomSheetButtonData(title = "Camera", icon = R.drawable.ic_camera) {
			photoUri = createTempFileToExpose(
				context = activity,
				key = generatePrimaryKey(),
				extension = ".jpg"
			).first
			takePicture.launch(photoUri)
		},
		MenuBottomSheetButtonData(title = "Gallery", icon = R.drawable.ic_gallery) {
			openFilePicker.launch(arrayOf("image/*", "video/*", "audio/*"))
		},
		MenuBottomSheetButtonData(title = "Audio", icon = R.drawable.ic_mic) {
		},
		MenuBottomSheetButtonData(title = "File", icon = R.drawable.ic_file) {
			openFilePicker.launch(arrayOf("*/*"))
		},
	)

	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		modifier = Modifier
			.fillMaxWidth()
			.background(MaterialTheme.colorScheme.surface)
	) {

		BottomSheetStrip()

		BottomSheetHeader(
			title = "Attachment",
			icon = R.drawable.ic_attachment,
		)

		Spacer(modifier = Modifier.height(8.dp))

		Row(
			modifier = Modifier.padding(24.dp, 0.dp),
			horizontalArrangement = Arrangement.SpaceBetween
		) {
			buttonList.forEach { MenuBottomSheetButton(it, modifier = Modifier.weight(1f)) }
		}

		if (attachmentList.isNotEmpty()) {
			Spacer(modifier = Modifier.height(12.dp))

			LazyVerticalGrid(
				columns = GridCells.Adaptive(144.dp),
				modifier = Modifier.padding(24.dp, 0.dp),
			) {
				attachmentList.forEachIndexed { index, (id, data) ->
					item {
						Box(
							modifier = Modifier
								.padding(4.dp)
								.clip(RoundedCornerShape(28.dp))
						) {
							AttachmentPreview(
								attachment = data.third,
								uri = data.first,
								file = data.second,
								clickable = false,
								showActionButton = true,
								modifier = Modifier
									.fillMaxWidth()
									.aspectRatio(1f),
								onRemove = { attachmentList.drop(index) },
							) {
								try {
									Intent(Intent.ACTION_VIEW, FileProvider.getUriForFile(activity, "com.syncodec.fileprovider", data.second)).apply {
										addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

										activity.startActivity(this)
									}
								} catch (e: Exception) {
									e.printStackTrace()
									Toast.makeText(activity, "Error viewing file", Toast.LENGTH_SHORT).show()
								}
							}
						}
					}
				}
				item { Spacer(modifier = Modifier.height(32.dp)) }
				item { Spacer(modifier = Modifier.height(32.dp)) }
			}
		} else {
			Spacer(modifier = Modifier.height(32.dp))
		}
	}
}
