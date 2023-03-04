package com.syncodec.graphite.presentation.note.screen.editorScreen.bottomSheet

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.animation.AnimatedText
import com.syncodec.graphite.presentation.common.bottomSheet.GenericBottomSheet
import com.syncodec.graphite.presentation.common.bottomSheet.bottomSheetButtonGrid.BottomSheetButton
import com.syncodec.graphite.presentation.common.bottomSheet.bottomSheetButtonGrid.BottomSheetButtonGrid
import com.syncodec.graphite.presentation.common.button.MenuButton
import com.syncodec.graphite.presentation.common.button.MenuButtonDefaults
import com.syncodec.graphite.presentation.common.text.marqueeText.MarqueeText
import com.syncodec.graphite.presentation.note.screen.viewerScreen.composable.AttachmentPreview
import com.syncodec.graphite.utils.DataStoreInstance
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
			if (uriList.isNotEmpty()) onAddAttachmentToBuffer(uriList.take(8 - (attachmentListSaved.size + attachmentListToAdd.size)))
			else Toast.makeText(context, "No files selected", Toast.LENGTH_SHORT).show()
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
						if (attachmentListSaved.size + attachmentListToAdd.size < 8) takePicture.launch(photoUri)
						else Toast.makeText(context, "Join Graphite Pro to add more attachments", Toast.LENGTH_SHORT).show()
					}
				},
				{
					BottomSheetButton(title = "Gallery", icon = R.drawable.ic_gallery) {
						if (attachmentListSaved.size + attachmentListToAdd.size < 8) openFilePicker.launch(arrayOf("image/*", "video/*", "audio/*"))
						else Toast.makeText(context, "Join Graphite Pro to add more attachments", Toast.LENGTH_SHORT).show()

					}
				},
				{
					BottomSheetButton(title = "File", icon = R.drawable.ic_file) {
						if (attachmentListSaved.size + attachmentListToAdd.size < 8) openFilePicker.launch(arrayOf("*/*"))
						else Toast.makeText(context, "Join Graphite Pro to add more attachments", Toast.LENGTH_SHORT).show()
					}
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
			Spacer(modifier = Modifier.height(6.dp))
			PlainTextWarning()
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
								isDeleting = file in attachmentListToRemove,
							) { onRemoveSavedAttachment(file) }
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
	isDeleting : Boolean = false,
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
			if (isUnSaved) Icon(
				painter = painterResource(id = R.drawable.ic_new_sticker),
				contentDescription = "New attachment",
				tint = Color.Unspecified,
				modifier = Modifier
					.requiredSize(32.dp)
					.padding(2.dp)
					.graphicsLayer { rotationZ = -45f }
			)
			Spacer(modifier = Modifier.weight(1f))
			MenuButton(
				icon = R.drawable.ic_close,
				colors = MenuButtonDefaults.menuButtonColors(
					containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.71f),
					iconColor = MaterialTheme.colorScheme.onBackground,
					checkedContainerColor = MaterialTheme.colorScheme.error,
					checkedIconColor = MaterialTheme.colorScheme.onError,
				),
				checked = isDeleting,
				buttonSize = 16.dp,
				onClick = onClick
			)
		}
		Spacer(modifier = Modifier.weight(1f))
		Box(
			modifier = Modifier.padding(2.dp)
		) {
			MarqueeText(
				text = fileName,
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.onSurface,
				modifier = Modifier
					.fillMaxWidth()
					.background(MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp), MaterialTheme.shapes.small)
					.padding(8.dp)

			)
		}
	}
}

@Preview
@Composable
fun PlainTextWarning() {
	val context = LocalContext.current
	val dataStoreInstance = remember { DataStoreInstance(context = context) }

	val showUnencryptedAttachmentCard by dataStoreInstance.getShowUnencryptedAttachmentCard.collectAsState(initial = null)

	AnimatedVisibility(
		visible = showUnencryptedAttachmentCard == true,
		enter = expandVertically(tween(300)),
		exit = shrinkVertically(tween(300))
	) {
		Box(
			modifier = Modifier
				.fillMaxWidth()
				.background(MaterialTheme.colorScheme.errorContainer, MaterialTheme.shapes.large)
		) {
			Column(
				modifier = Modifier
					.fillMaxWidth()
					.padding(16.dp, 16.dp, 16.dp, 8.dp)
			) {
				Row(
					verticalAlignment = Alignment.CenterVertically,
					modifier = Modifier,
				) {
					Icon(
						painter = painterResource(id = R.drawable.ic_warning),
						contentDescription = "Plain Text Warning",
						tint = MaterialTheme.colorScheme.onErrorContainer,
					)
					Spacer(modifier = Modifier.width(12.dp))
					Text(
						text = "Unencrypted Data",
						style = MaterialTheme.typography.titleMedium,
						fontWeight = FontWeight.Bold,
						color = MaterialTheme.colorScheme.onErrorContainer,
					)
				}

				Spacer(modifier = Modifier.height(8.dp))

				Text(
					text = "Attachments are not encrypted and and are stored as raw files on the device.",
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.onErrorContainer
				)

				Spacer(modifier = Modifier.height(8.dp))

				Button(
					colors = ButtonDefaults.buttonColors(
						containerColor = MaterialTheme.colorScheme.error,
						contentColor = MaterialTheme.colorScheme.onError,
					),
					shape = MaterialTheme.shapes.medium,
					modifier = Modifier.fillMaxWidth(),
					onClick = { dataStoreInstance.putShowUnencryptedAttachmentCard(false) },
				) {
					Text(text = "Dismiss")
				}
			}
		}
	}
}
