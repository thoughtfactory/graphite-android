package com.syncodec.graphite.presentation.note.screen.editorScreen.bottomSheet

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.animation.AnimatedText
import com.syncodec.graphite.presentation.common.attachment.AttachmentPreview
import com.syncodec.graphite.presentation.common.bottomSheet.GenericBottomSheet
import com.syncodec.graphite.presentation.common.bottomSheet.bottomSheetButtonGrid.BottomSheetButton
import com.syncodec.graphite.presentation.common.bottomSheet.bottomSheetButtonGrid.BottomSheetButtonGrid
import com.syncodec.graphite.presentation.common.button.MenuButton
import com.syncodec.graphite.presentation.common.button.MenuButtonDefaults
import com.syncodec.graphite.presentation.common.text.marqueeText.MarqueeText
import com.syncodec.graphite.utils.createTempAttachmentFileToExpose
import com.syncodec.graphite.utils.getFileName
import io.github.esentsov.PackagePrivate
import io.realm.kotlin.types.RealmUUID
import java.io.File


@OptIn(ExperimentalAnimationApi::class, ExperimentalFoundationApi::class)
@PackagePrivate
@Preview
@Composable
fun AttachmentBottomSheet(
	attachmentListSaved : List<File> = listOf(),
	attachmentListToAdd : List<Uri> = listOf(),
	attachmentListToRemove : List<File> = listOf(),
	onAddAttachmentToBuffer : (List<Uri>) -> Unit = {},
	onRemoveBufferedAttachment : (Uri) -> Unit = {},
	onRemoveSavedAttachment : (File) -> Unit = {},
) {
	val context = LocalContext.current

	var photoUri : Uri? by remember { mutableStateOf(null) }
	val takePicture = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { isCaptured ->
		try {
			if (isCaptured) photoUri?.let { uri -> onAddAttachmentToBuffer(listOf(uri)) }
		} catch (e : Exception) {
			Toast.makeText(context, "Failed to add attachment", Toast.LENGTH_SHORT).show()
		}
	}
	val openFilePicker = rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenMultipleDocuments()) { uriList ->
		try {
			if (uriList.isNotEmpty()) onAddAttachmentToBuffer(uriList)
		} catch (e : Exception) {
			Toast.makeText(context, "Failed to add attachment", Toast.LENGTH_SHORT).show()
		}
	}

	GenericBottomSheet(
		title = "Attachments",
		icon = R.drawable.ic_attachment_new
	) {
		BottomSheetButtonGrid(
			buttonList = listOf(
				{
					BottomSheetButton(title = "Camera", icon = R.drawable.ic_camera) {
						photoUri = createTempAttachmentFileToExpose(context = context, name = "${RealmUUID.random()}.jpg").first
						takePicture.launch(photoUri)
					}
				},
				{
					BottomSheetButton(title = "Gallery", icon = R.drawable.ic_gallery) {
						openFilePicker.launch(arrayOf("image/*", "video/*", "audio/*"))
					}
				},
				{
					BottomSheetButton(title = "File", icon = R.drawable.ic_file) { openFilePicker.launch(arrayOf("*/*")) }
				}
			)
		)

		AnimatedVisibility(
			visible = attachmentListToRemove.isNotEmpty(),
			enter = expandVertically(tween(300)),
			exit = shrinkVertically(tween(300))
		) {
			AnimatedText(
				text = "${attachmentListToRemove.size} attachment(s) will be removed on save",
				style = MaterialTheme.typography.titleMedium,
				color = MaterialTheme.colorScheme.onBackground,
				fontWeight = FontWeight.Bold,
				modifier = Modifier.padding(0.dp, 16.dp, 0.dp, 4.dp)
			)
		}

		if (attachmentListSaved.isNotEmpty() || attachmentListToAdd.isNotEmpty()) {
			Spacer(modifier = Modifier.height(4.dp))

			LazyVerticalGrid(
				columns = GridCells.Adaptive(120.dp),
				modifier = Modifier,
			) {
				attachmentListSaved.forEach { file ->
					item(key = file) {
						Box(
							contentAlignment = Alignment.Center,
							modifier = Modifier
								.padding(4.dp)
								.aspectRatio(1f)
								.background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.large)
								.clip(MaterialTheme.shapes.large)
								.animateItemPlacement()
						) {
							AttachmentPreview(file = file)
							AttachmentOverlay(
								fileName = file.name ?: "Unknown",
							) { onRemoveSavedAttachment(file) }
							androidx.compose.animation.AnimatedVisibility(
								visible = file in attachmentListToRemove,
								enter = fadeIn(tween(300)),
								exit = fadeOut(tween(300))
							) {
								Box(
									modifier = Modifier
										.fillMaxSize()
										.background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.47f))
								)
							}
						}
					}
				}
				attachmentListToAdd.forEach { uri ->
					item {
						Box(
							contentAlignment = Alignment.Center,
							modifier = Modifier
								.padding(4.dp)
								.aspectRatio(1f)
								.background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.large)
								.clip(MaterialTheme.shapes.large)
						) {
							AttachmentPreview(uri = uri)
							AttachmentOverlay(
								fileName = uri.getFileName(context) ?: "Unknown",
								type = context.contentResolver.getType(uri),
								isUnSaved = true,
							) { onRemoveBufferedAttachment(uri) }
						}
					}
				}
			}
		}
	}
}

@Preview
@Composable
private fun AttachmentOverlay(
	fileName : String = "file_name",
	type : String? = null,
	isUnSaved : Boolean = false,
	onClick : () -> Unit = {},
) {
	Column(
		modifier = Modifier
			.fillMaxSize()
			.padding(4.dp)
	) {
		Row(
			modifier = Modifier.fillMaxWidth()
		) {
			if (isUnSaved) Box(
				modifier = Modifier
					.requiredSize(32.dp)
					.padding(12.dp)
					.background(MaterialTheme.colorScheme.error, CircleShape)
			)
			Spacer(modifier = Modifier.weight(1f))
			MenuButton(
				icon = R.drawable.ic_close,
				colors = MenuButtonDefaults.menuButtonColors(
					containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.71f),
					iconColor = MaterialTheme.colorScheme.onErrorContainer
				),
				onClick = onClick
			)
		}
		Spacer(modifier = Modifier.weight(1f))
		Box(
			modifier = Modifier.padding(2.dp)
		) {
			MarqueeText(
				text = fileName,
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onPrimary,
				fontWeight = FontWeight.Bold,
				modifier = Modifier
					.fillMaxWidth()
					.background(MaterialTheme.colorScheme.primary, MaterialTheme.shapes.medium)
					.padding(8.dp)
			)
		}
	}
}
