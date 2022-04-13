package com.syncodec.momento.noteComponent.modalBottomSheet

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.focusable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import coil.fetch.VideoFrameUriFetcher
import coil.request.videoFrameMillis
import com.syncodec.momento.R
import com.syncodec.momento.custom.bottomSheet.BottomSheetHeader
import com.syncodec.momento.custom.bottomSheet.BottomSheetStrip
import com.syncodec.momento.custom.button.MenuBottomSheetButton
import com.syncodec.momento.custom.button.MenuBottomSheetButtonData
import com.syncodec.momento.database.attachment.AttachmentDbEntry
import com.syncodec.momento.database.attachment.getMimeType
import com.syncodec.momento.miscellaneous.ThemeUtils.Companion.tone
import com.syncodec.momento.miscellaneous.FileUtils.Companion.createTempFileToExpose
import com.syncodec.momento.miscellaneous.generatePrimaryKey
import com.syncodec.momento.noteComponent.NoteActivity
import compose.icons.TablerIcons
import compose.icons.tablericons.X


@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)
@Composable
fun AttachmentBottomSheet(
	attachmentMap: SnapshotStateMap<String, Pair<AttachmentDbEntry, Uri>>,
	onAction: (NoteActivity.Action, Any?) -> Unit
) {
	val context = LocalContext.current
	var photoUri: Uri? = null
	val takePicture =
		rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { isCaptured ->
			if (isCaptured) onAction(NoteActivity.Action.INSERT_PICTURE, photoUri)
		}

	val openMediaPicker =
		rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenMultipleDocuments()) { uriList ->
			onAction(NoteActivity.Action.INSERT_MEDIA, uriList)
		}

	val openFilePicker =
		rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenDocument()) { uriList ->
			Log.i("npr71", "uri : $uriList")
		}

	val buttonDataList: List<MenuBottomSheetButtonData> = listOf(
		MenuBottomSheetButtonData(title = "Camera", icon = R.drawable.ic_camera) {
			photoUri = createTempFileToExpose(
				context = context,
				key = generatePrimaryKey(),
				extension = ".jpg"
			)
			takePicture.launch(photoUri)
		},
		MenuBottomSheetButtonData(title = "Gallery", icon = R.drawable.ic_gallery) {
			openMediaPicker.launch(arrayOf("image/*", "video/*", "audio/*"))
		},
		MenuBottomSheetButtonData(title = "File", icon = R.drawable.ic_file) {
			openFilePicker.launch(arrayOf("*/*"))
		},
	)

	Surface(
		shape = RoundedCornerShape(12.dp, 12.dp, 0.dp, 0.dp),
		color = MaterialTheme.colorScheme.surface.tone(isSystemInDarkTheme(), 2)
	) {
		Column(
			modifier = Modifier,
			horizontalAlignment = Alignment.CenterHorizontally
		) {

			BottomSheetStrip()

			BottomSheetHeader(
				title = "Attachment",
				icon = R.drawable.ic_attachment
			)

			LazyVerticalGrid(
				columns = GridCells.Fixed(4),
				modifier = Modifier.padding(24.dp, 0.dp),
			) { buttonDataList.forEach { item { MenuBottomSheetButton(it) } } }

			Spacer(modifier = Modifier.height(12.dp))

			LazyVerticalGrid(
				columns = GridCells.Adaptive(144.dp),
				modifier = Modifier.padding(24.dp, 0.dp, 24.dp, 32.dp),
			) {
				attachmentMap.forEach { (_, data) ->
					item {
						AttachmentView(
							attachment = data.first,
							uri = data.second
						) { click, data -> onAction(click, data) }
					}
				}
			}
		}
	}
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalCoilApi::class)
@Composable
private fun AttachmentView(
	attachment: AttachmentDbEntry,
	uri: Uri,
	onAction: (NoteActivity.Action, Any?) -> Unit
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
//		onClick = { onAction(NoteActivity.Action.OPEN_ATTACHMENT, uri) }
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
			IconButton(onClick = {
				onAction(NoteActivity.Action.REMOVE_ATTACHMENT, attachment.key)
			}) {
				Icon(
					imageVector = TablerIcons.X,
					contentDescription = "Remove attachment",
					tint = Color.Companion.White
				)
			}
		}
	}
}
