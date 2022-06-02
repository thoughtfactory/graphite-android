package com.syncodec.graphite.noteComponent.modalBottomSheet

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.IconButton
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import com.syncodec.graphite.R
import com.syncodec.graphite.custom.bottomSheet.BottomSheetHeader
import com.syncodec.graphite.custom.bottomSheet.BottomSheetStrip
import com.syncodec.graphite.custom.button.MenuBottomSheetButton
import com.syncodec.graphite.custom.button.MenuBottomSheetButtonData
import com.syncodec.graphite.custom.squircle.SquircleShape
import com.syncodec.graphite.database.attachment.AttachmentDbEntry
import com.syncodec.graphite.miscellaneous.FileUtils.Companion.createTempFileToExpose
import com.syncodec.graphite.miscellaneous.generatePrimaryKey
import com.syncodec.graphite.noteComponent.NoteActivity
import com.syncodec.graphite.ui.theme.PremiumCompositionLocal


@Composable
fun AttachmentBottomSheet(
	attachmentMap: Map<String, Pair<AttachmentDbEntry, Uri?>>,
	onAction: (NoteActivity.Action, Any?) -> Unit
) {
	val context = LocalContext.current
	val isPremium = PremiumCompositionLocal.current

	var photoUri: Uri? = null
	val takePicture =
		rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { isCaptured ->
			if (isCaptured) onAction(NoteActivity.Action.INSERT_FILE, Pair(listOf(photoUri), isPremium))
		}

	val openFilePicker =
		rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenMultipleDocuments()) { uriList ->
			onAction(NoteActivity.Action.INSERT_FILE, Pair(uriList, isPremium))
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
			openFilePicker.launch(arrayOf("image/*", "video/*", "audio/*"))
		},
		MenuBottomSheetButtonData(title = "Audio", icon = R.drawable.ic_mic) {
			Toast.makeText(context, "Coming soon...", Toast.LENGTH_SHORT).show()
		},
		MenuBottomSheetButtonData(title = "File", icon = R.drawable.ic_file) {
			openFilePicker.launch(arrayOf("*/*"))
		},
	)

	Surface(
		shape = RoundedCornerShape(12.dp, 12.dp, 0.dp, 0.dp),
		color = MaterialTheme.colorScheme.surface,
		modifier = Modifier.heightIn(128.dp),
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
							uri = data.second,
							onAction = onAction
						)
					}
				}
			}
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AttachmentView(
	attachment: AttachmentDbEntry,
	uri: Uri?,
	onAction: (NoteActivity.Action, Any?) -> Unit
) {
	val context = LocalContext.current
	Card(
		elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
		containerColor = MaterialTheme.colorScheme.surface,
		shape = SquircleShape(6.0),
		modifier = Modifier
			.fillMaxWidth()
			.aspectRatio(1f)
			.padding(4.dp)
			.focusable(true),
	) {
		Box(
			modifier = Modifier.fillMaxSize()
		) {
//			TODO
			when ("image") {
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
//				"video" -> Image(
//					painter = rememberImagePainter(
//						data = uri,
//						builder = {
//							fetcher(VideoFrameUriFetcher(context))
//							crossfade(true)
//							// optionally set frame location
//							videoFrameMillis(1000)
//							this.listener(
//								onError = { request, exception ->
//								}
//							)
//						}
//					),
//					contentDescription = null,
//					contentScale = ContentScale.Crop,
//					modifier = Modifier.fillMaxSize()
//				)
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
				contentAlignment = Alignment.TopEnd,
				modifier = Modifier
					.fillMaxSize()
					.padding(0.dp),
			) {
				IconButton(
					onClick = {
						onAction(NoteActivity.Action.REMOVE_ATTACHMENT, attachment.key)
					}
				) {
					Icon(
						painter = painterResource(id = R.drawable.ic_close),
						contentDescription = "Remove attachment",
						tint = Color.Companion.White,
						modifier = Modifier
							.requiredSize(32.dp)
							.padding(4.dp)
					)
				}
			}
		}
	}
}
