package com.syncodec.momento.diaryComponent.modalBottomSheet

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.annotation.ExperimentalCoilApi
import coil.compose.ImagePainter
import coil.compose.rememberImagePainter
import coil.fetch.VideoFrameUriFetcher
import coil.request.videoFrameMillis
import com.syncodec.momento.custom.BottomSheetHeader
import com.syncodec.momento.custom.BottomSheetStrip
import com.syncodec.momento.diaryComponent.DiaryViewModel
import com.syncodec.momento.diaryComponent.TempAttachmentData
import com.syncodec.momento.miscellaneous.createTempFileToExpose
import com.syncodec.momento.miscellaneous.generatePrimaryKey
import compose.icons.TablerIcons
import compose.icons.tablericons.*
import java.nio.file.spi.FileTypeDetector


data class AttachmentBottomSheetButtonData(val title: String, val imageVector: ImageVector, val onClick: () -> Unit)

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)
@Composable
fun AttachmentBottomSheet() {
	val context = LocalContext.current

	val viewModel: DiaryViewModel = viewModel()

	var photoUri: Uri? = null

	val takePicture = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { isCaptured ->
		if (isCaptured) {
			viewModel.insertAttachment(
				uri = photoUri!!,
				mimeType = context.contentResolver.getType(photoUri!!)
			)
		}
	}

	val openMediaPicker = rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenMultipleDocuments()) { uriList ->
		uriList.forEach {
			viewModel.insertAttachment(
				uri = it,
				mimeType = context.contentResolver.getType(it)
			)
		}
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
			cells = GridCells.Fixed(4),
			modifier = Modifier
				.padding(24.dp, 0.dp)
		) {
			itemsIndexed(attachmentBottomSheetButtonDataLists) { _, attachmentBottomSheetButtonData ->
				AttachmentBottomSheetButton(attachmentBottomSheetButtonData)
			}
		}

		Spacer(modifier = Modifier.height(12.dp))

		LazyVerticalGrid(
			cells = GridCells.Adaptive(144.dp),
			modifier = Modifier
				.padding(24.dp, 0.dp, 24.dp, 32.dp)
		) {
			itemsIndexed(viewModel.attachmentList) { _, tempAttachmentData ->
				AttachmentView(
					tempAttachmentData = tempAttachmentData
				) {

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
			modifier = Modifier
				.fillMaxWidth()
		)
	}
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalCoilApi::class)
@Composable
private fun AttachmentView(
	tempAttachmentData: TempAttachmentData,
	onClick: () -> Unit
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
		onClick = { onClick() }
	) {
		when (tempAttachmentData.mimeType?.split("/")?.first()) {
			"image" -> {
				Image(
					painter = rememberImagePainter(tempAttachmentData.uri),
					contentDescription = null,
					contentScale = ContentScale.Crop,
					modifier = Modifier
						.fillMaxSize()
				)
			}
			"video" -> Image(
				painter = rememberImagePainter(
					data = tempAttachmentData.uri,
					builder = {
						fetcher(VideoFrameUriFetcher(context))
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
				modifier = Modifier
					.fillMaxSize()
			)
			else -> Image(
				painter = rememberImagePainter(tempAttachmentData.uri),
				contentDescription = null,
				contentScale = ContentScale.Crop,
				modifier = Modifier
					.fillMaxSize()
			)
		}

		Box(
			modifier = Modifier
				.fillMaxSize(),
			contentAlignment = Alignment.TopEnd
		) {
			IconButton(onClick = { /*TODO*/ }) {
				Icon(
					imageVector = TablerIcons.X,
					contentDescription = "Remove attachment",
					tint = Color.Companion.White
				)
			}
		}
	}
}
