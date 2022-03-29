package com.syncodec.momento.noteComponent.modalBottomSheet

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import coil.fetch.VideoFrameUriFetcher
import coil.request.videoFrameMillis
import com.syncodec.momento.custom.BottomSheetHeader
import com.syncodec.momento.custom.BottomSheetStrip
import com.syncodec.momento.database.attachment.AttachmentDbEntry
import com.syncodec.momento.database.attachment.getMimeType
import com.syncodec.momento.miscellaneous.FileUtils.Companion.createTempFileToExpose
import com.syncodec.momento.miscellaneous.generatePrimaryKey
import com.syncodec.momento.noteComponent.NoteActivity
import compose.icons.TablerIcons
import compose.icons.tablericons.*


data class AttachmentBottomSheetButtonData(val title: String, val imageVector: ImageVector, val onClick: () -> Unit)

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)
@Composable
fun AttachmentBottomSheet(
	attachmentMap: SnapshotStateMap<String, Pair<AttachmentDbEntry, Uri>>,
	onClick: (NoteActivity.Click, Any?) -> Unit
) {
	val context = LocalContext.current
	var photoUri: Uri? = null
	val takePicture = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { isCaptured ->
		if (isCaptured) onClick(NoteActivity.Click.INSERT_PICTURE, photoUri)
	}

	val openMediaPicker = rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenMultipleDocuments()) { uriList ->
		onClick(NoteActivity.Click.INSERT_MEDIA, uriList)
	}

	val openFilePicker = rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenDocument()) { uriList ->
		Log.i("npr71", "uri : $uriList")
	}

	val attachmentBottomSheetButtonDataLists: List<AttachmentBottomSheetButtonData> = listOf(
		AttachmentBottomSheetButtonData(title = "Camera", imageVector = TablerIcons.Camera) {
			photoUri = createTempFileToExpose(
				context = context,
				primaryKey = generatePrimaryKey(),
				mimeType = "image/*"
			)
			takePicture.launch(photoUri)
		},
		AttachmentBottomSheetButtonData(title = "Gallery", imageVector = TablerIcons.Photo) {
			openMediaPicker.launch(arrayOf("image/*", "video/*", "audio/*"))
		},
		AttachmentBottomSheetButtonData(title = "Audio", imageVector = TablerIcons.Microphone) {},
		AttachmentBottomSheetButtonData(title = "File", imageVector = TablerIcons.File) {
			openFilePicker.launch(arrayOf("*/*"))
		},
	)

	Column(
		modifier = Modifier
			.fillMaxWidth()
			.background(MaterialTheme.colorScheme.background),
		horizontalAlignment = Alignment.CenterHorizontally
	) {

		BottomSheetStrip()

		BottomSheetHeader(
			title = "Attachment",
			imageVector = TablerIcons.Paperclip
		)

		LazyVerticalGrid(
			columns = GridCells.Fixed(4),
			modifier = Modifier
				.padding(24.dp, 0.dp),
		) {
			itemsIndexed(attachmentBottomSheetButtonDataLists) { _, attachmentBottomSheetButtonData ->
				AttachmentBottomSheetButton(attachmentBottomSheetButtonData)
			}
		}

		Spacer(modifier = Modifier.height(12.dp))

		LazyVerticalGrid(
			columns = GridCells.Adaptive(144.dp),
			modifier = Modifier
				.padding(24.dp, 0.dp, 24.dp, 32.dp),
		) {
			attachmentMap.forEach { (_, data) ->
				item {
					AttachmentView(
						attachment = data.first,
						uri = data.second
					) { click, data -> onClick(click, data)}
				}
			}
		}
	}
}

@Composable
private fun AttachmentBottomSheetButton(
	attachmentBottomSheetButtonData: AttachmentBottomSheetButtonData
) {
	Column(
		horizontalAlignment = Alignment.CenterHorizontally
	) {
		Card(
			elevation = 0.dp,
			backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
			shape = RoundedCornerShape(8.dp),
			modifier = Modifier
				.fillMaxWidth()
				.aspectRatio(1f)
				.padding(6.dp)
				.focusable(true)
				.clip(RoundedCornerShape(8.dp))
				.clickable(true) { attachmentBottomSheetButtonData.onClick() },
		) {
			Icon(
				imageVector = attachmentBottomSheetButtonData.imageVector,
				contentDescription = null,
				tint = MaterialTheme.colorScheme.onSecondaryContainer,
				modifier = Modifier
					.requiredSize(24.dp)
			)
		}
		Text(
			text = attachmentBottomSheetButtonData.title,
			style = MaterialTheme.typography.bodySmall,
			color = MaterialTheme.colorScheme.onBackground,
			textAlign = TextAlign.Center,
			maxLines = 2,
			modifier = Modifier.fillMaxWidth()
		)
	}
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalCoilApi::class)
@Composable
private fun AttachmentView(
	attachment: AttachmentDbEntry,
	uri: Uri,
	onClick: (NoteActivity.Click, String) -> Unit
) {
	val context = LocalContext.current
	Card(
		elevation = 0.dp,
		backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
		shape = RoundedCornerShape(12.dp),
		modifier = Modifier
			.fillMaxWidth()
			.aspectRatio(1f)
			.padding(4.dp)
			.focusable(true),
		onClick = { onClick(NoteActivity.Click.OPEN_ATTACHMENT, attachment.key) }
	) {
		when (attachment.getMimeType()) {
			"image" -> {
				Image(
					painter = rememberImagePainter(
						data = uri,
						builder = { crossfade(true) }
					),
					contentDescription = null,
					contentScale = ContentScale.Crop,
					modifier = Modifier.fillMaxSize()
				)
			}
			"video" -> Image(
				painter = rememberImagePainter(
					data = uri,
					builder = {
						fetcher(VideoFrameUriFetcher(context))
						crossfade(true)
						// optionally set frame location
						videoFrameMillis(1000)
						this.listener(
							onError = { request, exception ->
								Log.d("npr71", "error : ${exception.message}")
							}
						)
					}
				),
				contentDescription = null,
				contentScale = ContentScale.Crop,
				modifier = Modifier.fillMaxSize()
			)
			else -> Image(
				painter = rememberImagePainter(
					data = uri,
					builder = { crossfade(true) }
				),
				contentDescription = null,
				contentScale = ContentScale.Crop,
				modifier = Modifier.fillMaxSize()
			)
		}

		Box(
			modifier = Modifier
				.fillMaxSize(),
			contentAlignment = Alignment.TopEnd
		) {
			IconButton(onClick = { onClick(NoteActivity.Click.REMOVE_ATTACHMENT, attachment.key) }) {
				Icon(
					imageVector = TablerIcons.X,
					contentDescription = "Remove attachment",
					tint = Color.Companion.White
				)
			}
		}
	}
}
