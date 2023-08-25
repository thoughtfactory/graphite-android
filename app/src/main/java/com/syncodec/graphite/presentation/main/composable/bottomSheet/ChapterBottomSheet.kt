package com.syncodec.graphite.presentation.main.composable.bottomSheet

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.media.ThumbnailUtils
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.syncodec.graphite.BaseApplication
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.bottomSheet.genericBottomSheet2.GenericBottomSheet2
import com.syncodec.graphite.presentation.common.bottomSheet.genericBottomSheet2.GenericBottomSheetSkeleton2
import com.syncodec.graphite.presentation.common.button.CancelButton
import com.syncodec.graphite.presentation.common.tab.GenericTabRow
import com.syncodec.graphite.presentation.common.tab.TabItem
import com.syncodec.graphite.utils.imageList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@Preview
@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ChapterBottomSheet(
	bottomSheetState: SheetState = rememberModalBottomSheetState(),
	isBottomSheetVisible: Boolean = true,
	onDismissRequest: () -> Unit = { },
	title : String = "New notebook",
	putNotebook: (String, String, Color?, Bitmap?) -> Unit = { _, _, _, _ -> },
) {
	val keyboardController = LocalSoftwareKeyboardController.current

	val context = LocalContext.current
	val scope = rememberCoroutineScope()

	var bucketTitleText by rememberSaveable { mutableStateOf("") }
	var bucketDescriptionText by rememberSaveable { mutableStateOf("") }

	var selectedColor by remember { mutableStateOf<Color?>(null) }
	var currentImage by remember { mutableStateOf<Int?>(null) }
	var currentImageUri by remember { mutableStateOf<Uri?>(null) }

	fun createNotebook() {
		scope.launch(Dispatchers.Default) {
			val bitmap = if (currentImage != null) {
				BitmapFactory.decodeResource(context.resources, currentImage!!)
			} else {
				currentImageUri?.let { it1 -> ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, it1)) }
			}

			val aspectRatio = if (bitmap != null) bitmap.width.toFloat() / bitmap.height.toFloat() else 1f

			val thumbnail = bitmap?.let { ThumbnailUtils.extractThumbnail(it, (192 * aspectRatio).toInt(), 192) }

			putNotebook(bucketTitleText, bucketDescriptionText, selectedColor, thumbnail)

			keyboardController?.hide()
			onDismissRequest()

			bucketTitleText = ""
			bucketDescriptionText = ""
			selectedColor = null
			currentImage = null
			currentImageUri = null
		}
	}

	GenericBottomSheet2(
		bottomSheetState = bottomSheetState,
		isBottomSheetVisible = isBottomSheetVisible,
		onDismissRequest = onDismissRequest,
	) {
		GenericBottomSheetSkeleton2(
			title = title
		) {
			OutlinedTextField(
				value = bucketTitleText,
				shape = MaterialTheme.shapes.medium,
				onValueChange = { bucketTitleText = it },
				label = { Text(text = stringResource(id = R.string.title)) },
				placeholder = { Text(text = stringResource(id = R.string.bucket_title_placeholder)) },
				trailingIcon = { CancelButton { bucketTitleText = "" } },
				maxLines = 1,
				singleLine = true,
				modifier = Modifier.fillMaxWidth()
			)

			Spacer(modifier = Modifier.height(4.dp))

			OutlinedTextField(
				value = bucketDescriptionText,
				shape = MaterialTheme.shapes.medium,
				onValueChange = { bucketDescriptionText = it },
				label = { Text(text = stringResource(id = R.string.description)) },
				placeholder = { Text(text = stringResource(id = R.string.bucket_description_placeholder)) },
				trailingIcon = { CancelButton { bucketDescriptionText = "" } },
				maxLines = 1,
				singleLine = true,
				modifier = Modifier.fillMaxWidth()
			)

			Spacer(modifier = Modifier.height(12.dp))

			CoverPicker(
				selectedColor = selectedColor,
				currentImage = currentImage,
				currentImageUri = currentImageUri,
				onSelectColor = { selectedColor = it; currentImage = null; currentImageUri = null },
				onChooseImage = { currentImage = it; selectedColor = null; currentImageUri = null },
				onPickImage = { currentImageUri = it; selectedColor = null; currentImage = null },
			)

			Spacer(modifier = Modifier.height(8.dp))

			Button(
				shape = MaterialTheme.shapes.medium,
				colors = ButtonDefaults.buttonColors(
					containerColor = MaterialTheme.colorScheme.primary,
					contentColor = MaterialTheme.colorScheme.onPrimary,
					disabledContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp).copy(alpha = 0.31f),
					disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.71f),
				),
				enabled = bucketTitleText.isNotEmpty() && (selectedColor != null || currentImage != null || currentImageUri != null),
				modifier = Modifier.fillMaxWidth(),
				onClick = { createNotebook() },
			) {
				Text(text = stringResource(id = R.string.create))
			}
		}
	}
}

@Preview
@Composable
private fun CoverPicker(
	selectedColor: Color? = null,
	currentImage: Int? = null,
	currentImageUri: Uri? = null,
	onSelectColor: (Color) -> Unit = {},
	onChooseImage: (Int) -> Unit = {},
	onPickImage: (Uri) -> Unit = {},
) {

	var coverType by remember { mutableIntStateOf(0) }

	Column(
		modifier = Modifier
			.background(MaterialTheme.colorScheme.surface.copy(alpha = 0.47f), MaterialTheme.shapes.medium)
			.padding(16.dp)
	) {
		GenericTabRow(
			tabItemList = listOf(
				TabItem(text = stringResource(id = R.string.color)) { coverType = 0 },
				TabItem(text = stringResource(id = R.string.image)) { coverType = 1 },
			),
			selectedTabIndex = coverType,
		)
		Spacer(modifier = Modifier.height(8.dp))

		AnimatedContent(
			targetState = coverType,
			label = "coverType_animation"
		) {
			when (it) {
				0 -> ColorPicker(selectedColor = selectedColor, onSelectColor = onSelectColor)
				1 -> ImagePicker(
					currentImage = currentImage,
					currentImageUri = currentImageUri,
					onChooseImage = onChooseImage,
					onPickImage = onPickImage,
				)

				else -> Unit
			}
		}
	}
}

@OptIn(ExperimentalLayoutApi::class)
@Preview
@Composable
private fun ColorPicker(
	selectedColor: Color? = null,
	onSelectColor: (Color) -> Unit = {},
) {
	val colorList = remember {
		listOf(
			Color(0xFFE57373),
			Color(0xFFF06292),
			Color(0xFFBA68C8),
			Color(0xFF9575CD),
			Color(0xFF7986CB),
			Color(0xFF64B5F6),
			Color(0xFF4FC3F7),
			Color(0xFF4DD0E1),
			Color(0xFF4DB6AC),
			Color(0xFF81C784),
			Color(0xFFAED581),
			Color(0xFFDCE775),
			Color(0xFFFFD54F),
			Color(0xFFFFB74D),
			Color(0xFFFF8A65),
			Color(0xFFA1887F),
			Color(0xFF90A4AE),
			Color(0xFFE0E0E0),
			Color(0xFFBDBDBD),
			Color(0xFF9E9E9E),
			Color(0xFF757575),
			Color(0xFF616161),
			Color(0xFF424242),
			Color(0xFF000000),
		)
	}

	FlowRow(
		modifier = Modifier.fillMaxWidth(),
		horizontalArrangement = Arrangement.SpaceAround
	) {
		colorList.forEach {

			val borderColor by animateColorAsState(targetValue = if (selectedColor == it) MaterialTheme.colorScheme.onBackground else Color.Transparent, label = "borderColor_animation")

			Box(
				modifier = Modifier
					.requiredSize(40.dp)
					.padding(4.dp)
					.background(it, MaterialTheme.shapes.small)
					.clip(MaterialTheme.shapes.small)
					.border(2.dp, borderColor, MaterialTheme.shapes.small)
					.clickable { onSelectColor(it) }
			)
		}
	}
}

@Preview
@Composable
private fun ImagePicker(
	currentImage: Int? = null,
	currentImageUri: Uri? = null,
	onChooseImage: (Int) -> Unit = {},
	onPickImage: (Uri) -> Unit = {},
) {
	val context = LocalContext.current
	val scope = rememberCoroutineScope()

	val isPro by BaseApplication.isPro.collectAsState()
	val bitmap by remember(currentImageUri) {
		derivedStateOf {
			currentImageUri?.let { it1 ->
				try {
					ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, it1))
				} catch (e: Exception) {
					e.printStackTrace()
					null
				}
			}
		}
	}

	val openFilePicker = rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenDocument()) { uri ->
		uri?.let { onPickImage(it) } ?: run {
			scope.launch(Dispatchers.Main) {
				Toast.makeText(context, "No image selected", Toast.LENGTH_SHORT).show()
			}
		}
	}

	LazyVerticalGrid(
		columns = GridCells.Adaptive(96.dp),
	) {
		item {
			Box(
				contentAlignment = Alignment.Center,
				modifier = Modifier
					.fillMaxWidth()
					.aspectRatio(1.5f)
					.padding(4.dp)
					.background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.medium)
					.clip(MaterialTheme.shapes.large)
					.clickable {
						if (isPro) openFilePicker.launch(arrayOf("image/*"))
						else Toast
							.makeText(context, context.getText(R.string.toast_chapter_cover_pro), Toast.LENGTH_SHORT)
							.show()
					}
			) {
				SubcomposeAsyncImage(
					model = ImageRequest.Builder(context)
						.data(bitmap)
						.crossfade(470)
						.build(),
					error = {
						if (bitmap == null) {
							Box(
								modifier = Modifier.fillMaxSize(),
								contentAlignment = Alignment.Center,
							) {
								Text(
									text = stringResource(id = R.string.select_from_gallery),
									style = MaterialTheme.typography.labelMedium,
									color = MaterialTheme.colorScheme.onSurface,
								)
							}
						}
					},
					contentDescription = "Image",
					contentScale = ContentScale.Crop,
					modifier = Modifier.fillMaxSize()
				)
				SelectedImageOverlay(isSelected = bitmap != null)
			}
		}
		imageList.forEach { imageInt ->
			item {
				Box(
					contentAlignment = Alignment.Center,
					modifier = Modifier
						.fillMaxWidth()
						.aspectRatio(1.5f)
						.padding(4.dp)
						.background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.medium)
						.clip(MaterialTheme.shapes.large)
						.clickable { onChooseImage(imageInt) }
				) {
					SubcomposeAsyncImage(
						model = ImageRequest.Builder(context)
							.data(ImageBitmap.imageResource(imageInt).asAndroidBitmap())
							.crossfade(470)
							.build(),
						loading = { CircularProgressIndicator(modifier = Modifier.requiredSize(32.dp)) },
						error = {
							Text(
								text = "No image found",
								modifier = Modifier.padding(8.dp),
								style = MaterialTheme.typography.bodySmall,
								color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.71f)
							)
						},
						contentDescription = "Image",
						contentScale = ContentScale.Crop,
						modifier = Modifier.fillMaxSize()
					)
					SelectedImageOverlay(isSelected = currentImage == imageInt)
				}
			}
		}
	}
}

@Composable
private fun SelectedImageOverlay(
	isSelected: Boolean = true,
) {
	AnimatedVisibility(
		visible = isSelected,
		enter = fadeIn(tween(470)),
		exit = fadeOut(tween(470)),
		label = "selectedImageOverlay_animation",
	) {
		Box(
			contentAlignment = Alignment.Center,
			modifier = Modifier
				.fillMaxSize()
				.background(MaterialTheme.colorScheme.background.copy(alpha = 0.47f))
		) {
			Icon(
				painter = painterResource(id = R.drawable.ic_fa_check),
				contentDescription = "Selected image",
				tint = MaterialTheme.colorScheme.onBackground,
				modifier = Modifier.requiredSize(24.dp)
			)
		}
	}
}
