package com.syncodec.graphite.presentation.common.chapter

import android.graphics.ImageDecoder
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.flowlayout.FlowMainAxisAlignment
import com.google.accompanist.flowlayout.FlowRow
import com.syncodec.graphite.BaseApplication
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.button.stateButton.StateButton
import com.syncodec.graphite.presentation.common.button.stateButton.StateData
import com.syncodec.graphite.presentation.common.dialog.ColorPickerDialog
import com.syncodec.graphite.utils.getInverseBWColor
import com.syncodec.graphite.utils.getRandomColor
import com.syncodec.graphite.utils.imageList
import com.syncodec.graphite.utils.toHexString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@OptIn(ExperimentalAnimationApi::class)
@Preview
@Composable
fun ChapterCoverPicker(
	coverColor : Color? = null,
	coverImage : Int? = null,
	coverUri : Uri? = null,
	onPickColor : (Color) -> Unit = {},
	onPickImage : (Uri) -> Unit = {},
	onChooseImage : (Int) -> Unit = {},
) {

	val coverPickerTypeStateList = listOf(
		StateData(title = "Color"),
		StateData(title = "Image"),
	)
	var coverPickerState by rememberSaveable { mutableStateOf(0) }

	Box(
		modifier = Modifier
			.fillMaxWidth()
			.background(
				MaterialTheme.colorScheme
					.surfaceColorAtElevation(8.dp)
					.copy(alpha = 0.31f), MaterialTheme.shapes.medium
			)
	) {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.padding(12.dp)
		) {
			StateButton(
				stateList = coverPickerTypeStateList,
				currentState = coverPickerState,
				containerColor = MaterialTheme.colorScheme.background,
				modifier = Modifier
					.fillMaxWidth()
					.height(36.dp),
			) { coverPickerState = it }

			Spacer(modifier = Modifier.height(8.dp))

			AnimatedContent(
				targetState = coverPickerState,
				transitionSpec = { fadeIn(tween(300)) with fadeOut(tween(300)) using SizeTransform(clip = false) }
			) { state ->
				when (state) {
					0 -> ColorPicker(coverColor = coverColor ?: MaterialTheme.colorScheme.primary, onPickColor = onPickColor)
					1 -> ImagePicker(currentImage = coverImage, currentImageUri = coverUri, onChooseImage = onChooseImage, onPickImage = onPickImage)
				}
			}
		}
	}
}

@OptIn(ExperimentalAnimationApi::class)
@Preview
@Composable
private fun ColorPicker(
	coverColor : Color = getRandomColor(),
	onPickColor : (Color) -> Unit = {},
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

	var showColorPickerDialog by remember { mutableStateOf(false) }
//	ColorPickerDialog(
//		color = coverColor,
//		isDialogVisible = showColorPickerDialog,
//		onSelectColor = { onPickColor(it); showColorPickerDialog = false },
//		onDismissRequest = { showColorPickerDialog = false },
//	)

	Column(
		modifier = Modifier.fillMaxWidth()
	) {
		FlowRow(
			modifier = Modifier.fillMaxWidth(),
			mainAxisAlignment = FlowMainAxisAlignment.SpaceBetween,
			lastLineMainAxisAlignment = FlowMainAxisAlignment.Start,
		) {
			colorList.forEach { color ->
				Box(
					contentAlignment = Alignment.Center,
					modifier = Modifier
						.size(32.dp)
						.padding(4.dp)
						.background(color, MaterialTheme.shapes.small)
						.clip(MaterialTheme.shapes.small)
						.clickable { onPickColor(color) }
				) {
					androidx.compose.animation.AnimatedVisibility(
						visible = coverColor == color,
						enter = fadeIn(tween(300)),
						exit = fadeOut(tween(300))
					) {
						Icon(
							painter = painterResource(id = R.drawable.ic_check),
							contentDescription = null,
							tint = color.getInverseBWColor(),
							modifier = Modifier.size(16.dp)
						)
					}
				}
			}
		}

		Spacer(modifier = Modifier.height(12.dp))

		Spacer(
			modifier = Modifier
				.fillMaxWidth()
				.height(2.dp)
				.padding(12.dp, 0.dp)
				.background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.13f))
		)

		Spacer(modifier = Modifier.height(12.dp))

		Row(
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier
				.fillMaxWidth()
				.wrapContentHeight()
				.padding(8.dp, 0.dp),
		) {
			Text(
				text = "Custom",
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onBackground,
				modifier = Modifier
			)
			Spacer(modifier = Modifier.weight(1f))

			Box(
				modifier = Modifier
					.background(MaterialTheme.colorScheme.background, MaterialTheme.shapes.medium)
					.clip(MaterialTheme.shapes.medium)
					.clickable { showColorPickerDialog = true }
			) {
				Row(
					verticalAlignment = Alignment.CenterVertically,
					modifier = Modifier.padding(12.dp, 4.dp)
				) {
					AnimatedContent(
						targetState = coverColor,
						transitionSpec = { fadeIn(tween(300)) with fadeOut(tween(300)) using SizeTransform(clip = false) }
					) {
						Box(
							modifier = Modifier
								.size(32.dp)
								.padding(4.dp)
								.background(it, MaterialTheme.shapes.small)
						)
					}

					Spacer(modifier = Modifier.width(8.dp))

					AnimatedContent(
						targetState = coverColor,
						transitionSpec = { fadeIn(tween(300)) with fadeOut(tween(300)) using SizeTransform(clip = false) }
					) {
						Text(
							text = it.toHexString(),
							modifier = Modifier,
							style = MaterialTheme.typography.bodyMedium,
							color = MaterialTheme.colorScheme.onBackground,
						)
					}
				}
			}
		}
	}
}

@OptIn(ExperimentalAnimationApi::class)
@Preview
@Composable
private fun ImagePicker(
	currentImage : Int? = null,
	currentImageUri : Uri? = null,
	onChooseImage : (Int) -> Unit = {},
	onPickImage : (Uri) -> Unit = {}
) {
	val context = LocalContext.current
	val scope = rememberCoroutineScope()

	val isPro by BaseApplication.isPro.collectAsState()

	val openFilePicker = rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenDocument()) { uri ->
		uri?.let { onPickImage(it) } ?: run {
			scope.launch(Dispatchers.Main) {
				Toast.makeText(context, "No image selected", Toast.LENGTH_SHORT).show()
			}
		}
	}

	Column(
		modifier = Modifier.fillMaxWidth()
	) {
		LazyVerticalGrid(
			columns = GridCells.Adaptive(80.dp)
		) {
			item {
				var bitmap by remember {
					mutableStateOf(
						currentImageUri?.let { it1 ->
							ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, it1)).asImageBitmap()
						}
					)
				}

				LaunchedEffect(key1 = currentImageUri) {
					bitmap = currentImageUri?.let { ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, it)).asImageBitmap() }
				}

				Box(
					contentAlignment = Alignment.Center,
					modifier = Modifier
						.weight(1f)
						.aspectRatio(1.5f)
						.padding(4.dp)
						.background(MaterialTheme.colorScheme.background.copy(alpha = 0.71f), MaterialTheme.shapes.medium)
						.clip(MaterialTheme.shapes.medium)
						.clickable {
							if (isPro) openFilePicker.launch(arrayOf("image/*"))
							else Toast
								.makeText(context, "Join Graphite Pro to add custom cover in notebooks and chapters", Toast.LENGTH_SHORT)
								.show()
						},
				) {
					AnimatedContent(
						targetState = bitmap,
						transitionSpec = { fadeIn(tween(300)) with fadeOut(tween(300)) using SizeTransform(clip = false) },
						modifier = Modifier.fillMaxSize()
					) {
						it?.let {
							Image(
								bitmap = it,
								contentDescription = null,
								contentScale = ContentScale.Crop,
								modifier = Modifier.fillMaxWidth()
							)

							Box(
								modifier = Modifier
									.fillMaxSize()
									.background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.471f), MaterialTheme.shapes.medium)
							)
							Icon(
								painter = painterResource(id = R.drawable.ic_check),
								contentDescription = null,
								tint = MaterialTheme.colorScheme.background,
								modifier = Modifier.requiredSize(24.dp)
							)
						} ?: Icon(
							painter = painterResource(id = R.drawable.ic_gallery),
							contentDescription = null,
							modifier = Modifier.requiredSize(24.dp),
							tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.71f)
						)
					}
				}
			}
			imageList.forEach { image ->
				item {
					Box(
						contentAlignment = Alignment.Center,
						modifier = Modifier
							.weight(1f)
							.aspectRatio(1.5f)
							.padding(4.dp)
							.background(MaterialTheme.colorScheme.background, MaterialTheme.shapes.medium)
							.clip(MaterialTheme.shapes.medium)
							.clickable { onChooseImage(image) }
					) {
						Image(
							painter = painterResource(id = image),
							contentDescription = null,
							contentScale = ContentScale.Crop,
							modifier = Modifier
								.fillMaxSize()
								.clip(MaterialTheme.shapes.medium)
						)

						androidx.compose.animation.AnimatedVisibility(
							visible = currentImage == image,
							enter = fadeIn(tween(300)),
							exit = fadeOut(tween(300)),
							modifier = Modifier.fillMaxSize()
						) {
							Box(
								modifier = Modifier
									.fillMaxSize()
									.background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.471f), MaterialTheme.shapes.medium)
							)
							Icon(
								painter = painterResource(id = R.drawable.ic_check),
								contentDescription = null,
								tint = MaterialTheme.colorScheme.background,
								modifier = Modifier.requiredSize(24.dp)
							)
						}
					}
				}
			}
		}
	}
}
