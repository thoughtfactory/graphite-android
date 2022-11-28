package com.syncodec.graphite.presentation.note.composable.bottomSheet

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.bottomSheet.BottomSheetHeader
import com.syncodec.graphite.presentation.common.bottomSheet.BottomSheetStrip
import com.syncodec.graphite.presentation.common.bottomSheet.bottomSheetButtonGrid.BottomSheetButton
import com.syncodec.graphite.presentation.common.bottomSheet.bottomSheetButtonGrid.BottomSheetButtonData
import com.syncodec.graphite.presentation.note.composable.LocalCompositionAttachmentList
import com.syncodec.graphite.presentation.note.composable.buildingBlock.AttachmentPreview
import com.syncodec.graphite.utils.createTempAttachmentFileToExpose
import io.realm.kotlin.types.RealmUUID
import java.io.File


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AttachmentBottomSheet(
	onAddAttachmentToBuffer : (List<Uri>) -> Unit,
	onRemoveAttachment : (File?, Uri?) -> Unit,
) {
	val context = LocalContext.current
	val attachmentList = LocalCompositionAttachmentList.current

	var photoUri : Uri? by remember { mutableStateOf(null) }

	val takePicture =
		rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { isCaptured ->
			try {
				if (isCaptured) {
					if (photoUri != null) {
						onAddAttachmentToBuffer(listOf(photoUri !!))
						photoUri = null
					}
				}
			} catch (e : Exception) {
				e.printStackTrace()
			}
		}
	val openFilePicker = rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenMultipleDocuments()) { uriList ->
		try {
//			Join pro to add more than 4 attachments
			onAddAttachmentToBuffer(uriList.take(minOf(4, 4 - attachmentList.size)))
		} catch (e : Exception) {
		}
	}

	val buttonList : List<BottomSheetButtonData> = remember {
		listOf(
			BottomSheetButtonData(title = "Camera", icon = R.drawable.ic_camera) {
				if (attachmentList.size < 4) {
					photoUri = createTempAttachmentFileToExpose(context = context, name = "${RealmUUID.random()}.jpg").first
					takePicture.launch(photoUri)
				} else {
					Toast.makeText(context, "Join Graphite Pro to add more attachments", Toast.LENGTH_SHORT).show()
				}
			},
			BottomSheetButtonData(title = "Gallery", icon = R.drawable.ic_gallery) {
				if (attachmentList.size < 4) {
					openFilePicker.launch(arrayOf("image/*", "video/*", "audio/*"))
				} else {
					Toast.makeText(context, "Join Graphite Pro to add more attachments", Toast.LENGTH_SHORT).show()
				}
			},
//		    TODO Add audio recording
			BottomSheetButtonData(title = "File", icon = R.drawable.ic_file) {
				if (attachmentList.size < 4) {
					openFilePicker.launch(arrayOf("*/*"))
				} else {
					Toast.makeText(context, "Join Graphite Pro to add more attachments", Toast.LENGTH_SHORT).show()
				}
			},
		)
	}
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
			buttonList.forEachIndexed { index, buttonData ->
				BottomSheetButton(
					modifier = Modifier.weight(1f),
					title = buttonData.title,
					icon = buttonData.icon,
					onClick = buttonData.onClick
				)

				if (index != buttonList.size - 1) Spacer(modifier = Modifier.width(8.dp))
			}
		}

		if (attachmentList.isNotEmpty()) {
			Spacer(modifier = Modifier.height(12.dp))

			LazyVerticalGrid(
				columns = GridCells.Adaptive(144.dp),
				modifier = Modifier.padding(20.dp, 0.dp),
			) {
				attachmentList.forEach { (file, uri) ->
					item(
						key = file?.path ?: uri.toString()
					) {
						Box(
							modifier = Modifier
								.padding(4.dp)
								.clip(RoundedCornerShape(28.dp))
								.animateItemPlacement()
						) {
							AttachmentPreview(
								uri = uri,
								file = file,
								clickable = false,
								showActionButton = true,
								modifier = Modifier
									.fillMaxWidth()
									.aspectRatio(1f),
								onRemove = { onRemoveAttachment(file, uri) },
							) {
								try {
									file?.let {
										Intent(Intent.ACTION_VIEW, FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", it)).apply {
											addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

											context.startActivity(this)
										}
									}
								} catch (e : Exception) {
									e.printStackTrace()
									Toast.makeText(context, "Error viewing file", Toast.LENGTH_SHORT).show()
								}
							}
						}
					}
				}
				item { Spacer(modifier = Modifier.height(8.dp)) }
				item { Spacer(modifier = Modifier.height(8.dp)) }
			}

			if (attachmentList.size > 3) {
				ProView()
				Spacer(modifier = Modifier.height(32.dp))
			}
		} else {
			Spacer(modifier = Modifier.height(32.dp))
		}
	}
}

@Composable
private fun ProView() {
	Box(
		modifier = Modifier
			.fillMaxWidth()
			.padding(24.dp, 0.dp)
			.background(MaterialTheme.colorScheme.background.copy(alpha = 0.71f), RoundedCornerShape(24.dp))
	) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier
				.fillMaxWidth()
				.padding(16.dp),
		) {
			Icon(
				painter = painterResource(id = R.drawable.ic_pro),
				contentDescription = "Pro",
				tint = Color.Unspecified,
				modifier = Modifier.requiredSize(48.dp)
			)

			Spacer(modifier = Modifier.width(8.dp))

			Text(
				text = "Join Graphite Pro to add more attachments",
				style = MaterialTheme.typography.bodyLarge,
				color = MaterialTheme.colorScheme.onBackground,
				fontWeight = FontWeight.Bold,
			)
		}
	}
}
