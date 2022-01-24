package com.syncodec.momento.diaryComponent.modalBottomSheet

import android.net.Uri
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberImagePainter
import com.syncodec.momento.custom.BottomSheetHeader
import com.syncodec.momento.custom.BottomSheetStrip
import com.syncodec.momento.diaryComponent.DiaryViewModel
import com.syncodec.momento.diaryComponent.TempMediaData
import com.syncodec.momento.konstant.MediaType
import compose.icons.TablerIcons
import compose.icons.tablericons.*


data class MediaBottomSheetButtonData(val title: String, val imageVector: ImageVector, val onClick: () -> Unit)

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)
@Composable
fun MediaBottomSheet() {
	val context = LocalContext.current
	val configuration = LocalConfiguration.current
	val screenHeight = configuration.screenHeightDp.dp
	val screenWidth = configuration.screenWidthDp.dp

	val viewModel: DiaryViewModel = viewModel()

	var uri: Uri? = null
	var mediaType: MediaType? = null

	val takePicture = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { isCaptured ->
		if (isCaptured) {
			viewModel.insertMedia(uri = uri!!, mediaType = mediaType!!)
		}
	}

	val mediaBottomSheetButtonDataList: List<MediaBottomSheetButtonData> = listOf(
		MediaBottomSheetButtonData(title = "Camera", imageVector = TablerIcons.Camera) {
			mediaType = MediaType.PHOTO
			uri = com.syncodec.momento.miscellaneous.createTempFileToExpose(context = context, mediaType = mediaType!!)
			takePicture.launch(uri)
		},
		MediaBottomSheetButtonData(title = "Gallery", imageVector = TablerIcons.Photo) {
		},
		MediaBottomSheetButtonData(title = "Audio", imageVector = TablerIcons.Microphone) {},
		MediaBottomSheetButtonData(title = "Draw", imageVector = TablerIcons.Writing) {},
	)

	Column(
		modifier = Modifier
			.fillMaxWidth()
			.background(MaterialTheme.colorScheme.background),
		horizontalAlignment = Alignment.CenterHorizontally
	) {

		BottomSheetStrip()

		BottomSheetHeader(
			title = "Media",
			imageVector = TablerIcons.Paperclip
		)

		LazyVerticalGrid(
			cells = GridCells.Fixed(4),
			modifier = Modifier
				.padding(24.dp, 0.dp)
		) {
			itemsIndexed(mediaBottomSheetButtonDataList) { _, mediaBottomSheetButtonData ->
				MediaBottomSheetButton(mediaBottomSheetButtonData)
			}
		}

		Spacer(modifier = Modifier.height(12.dp))

		LazyVerticalGrid(
			cells = GridCells.Adaptive(144.dp),
			modifier = Modifier
				.padding(24.dp, 0.dp, 24.dp, 32.dp)
		) {
			itemsIndexed(viewModel.mediaList) { index, tempMediaData ->
				MediaView(
					modifier = Modifier,
					tempMediaData = tempMediaData
				)
			}
		}
	}
}

@Composable
private fun MediaBottomSheetButton(
	mediaBottomSheetButtonData: MediaBottomSheetButtonData
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
				.clickable(true) { mediaBottomSheetButtonData.onClick() },
		) {
			Icon(
				imageVector = mediaBottomSheetButtonData.imageVector,
				contentDescription = null,
				tint = MaterialTheme.colorScheme.onSecondaryContainer,
				modifier = Modifier
					.requiredSize(24.dp)
			)
		}
		Text(
			text = mediaBottomSheetButtonData.title,
			style = MaterialTheme.typography.bodySmall,
			color = MaterialTheme.colorScheme.onBackground,
			textAlign = TextAlign.Center,
			maxLines = 2,
			modifier = Modifier
				.fillMaxWidth()
		)
	}
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun MediaView(
	modifier: Modifier,
	tempMediaData: TempMediaData
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
			.clickable(true) { },
	) {
		when (tempMediaData.mediaType) {
			MediaType.PHOTO -> {
				Image(
					rememberImagePainter(tempMediaData.uri),
					contentDescription = null,
					contentScale = ContentScale.Crop
				)
			}
		}
	}
}
