package com.syncodec.graphite.presentation.common.notebook

import android.graphics.ImageDecoder
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.BaseApplication
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.composable.ProTag
import com.syncodec.graphite.utils.imageList


@Composable
fun NotebookImageChooser(
	currentImage : Int?,
	currentImageUri : Uri?,
	keepStartPadding : Boolean = true,
	onPickImage : (Uri?) -> Unit,
	onChooseImage : (Int) -> Unit,
) {

	val context = LocalContext.current

	val isPro by BaseApplication.isPro

	val openFilePicker = rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenDocument()) { uri ->
		onPickImage(uri)
	}

	Row(
		modifier = Modifier
			.fillMaxWidth()
			.horizontalScroll(rememberScrollState())
	) {
		if (keepStartPadding) {
			Spacer(modifier = Modifier.width(24.dp))
		}

		Crossfade(targetState = currentImageUri) {
			if (it == null) {
				Crossfade(
					targetState = currentImage,
					animationSpec = tween(300)
				) {
					Column(
						horizontalAlignment = Alignment.CenterHorizontally,
						modifier = Modifier
					) {
						Box(
							contentAlignment = Alignment.Center,
							modifier = Modifier
								.width(64.dp)
								.height(40.dp)
								.background(MaterialTheme.colorScheme.background, RoundedCornerShape(16.dp))
								.clip(RoundedCornerShape(16.dp))
								.clickable {
									if (isPro) openFilePicker.launch(arrayOf("image/*"))
									else Toast
										.makeText(context, "Join Graphite Pro to add image as cover", Toast.LENGTH_SHORT)
										.show()
								}
						) {
							if (it != null) {
								Image(
									painter = painterResource(id = it),
									contentDescription = "Image $it",
									contentScale = ContentScale.Crop,
									modifier = Modifier.fillMaxWidth()
								)

								Box(
									modifier = Modifier
										.fillMaxSize()
										.background(Color.Black.copy(alpha = 0.47f))
								)
							}

							Icon(
								painter = painterResource(id = R.drawable.ic_gallery),
								contentDescription = "Image Picker",
								tint = if (it == null) MaterialTheme.colorScheme.onBackground else Color.White,
								modifier = Modifier.requiredSize(24.dp)
							)
						}
						if (! isPro) {
							Box(
								modifier = Modifier
									.graphicsLayer {
										this.scaleX = 0.71f
										this.scaleY = 0.71f
									}
							) { ProTag() }
						}
					}
				}
			} else {
				Column(
					horizontalAlignment = Alignment.CenterHorizontally,
					modifier = Modifier
				) {
					Box(
						contentAlignment = Alignment.Center,
						modifier = Modifier
							.width(64.dp)
							.height(40.dp)
							.background(MaterialTheme.colorScheme.background, RoundedCornerShape(16.dp))
							.clip(RoundedCornerShape(16.dp))
							.clickable {
								if (isPro) openFilePicker.launch(arrayOf("image/*"))
								else Toast
									.makeText(context, "Join Graphite Pro to add image as cover", Toast.LENGTH_SHORT)
									.show()
							}
					) {
						val bitmap = currentImageUri?.let { it1 -> ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, it1)) }
						bitmap?.asImageBitmap()?.let { it1 ->
							Image(
								bitmap = it1,
								contentDescription = "Image $it",
								contentScale = ContentScale.Crop,
								modifier = Modifier.fillMaxWidth()
							)
						}

						Box(
							modifier = Modifier
								.fillMaxSize()
								.background(Color.Black.copy(alpha = 0.47f))
						)

						Icon(
							painter = painterResource(id = R.drawable.ic_gallery),
							contentDescription = "Image Picker",
							tint = Color.White,
							modifier = Modifier.requiredSize(24.dp)
						)
					}
					if (! isPro) {
						Box(
							modifier = Modifier
								.graphicsLayer {
									this.scaleX = 0.71f
									this.scaleY = 0.71f
								}
						) { ProTag() }
					}
				}
			}
		}

		Spacer(modifier = Modifier.width(6.dp))

		for (image in imageList) {
			Column(
				horizontalAlignment = Alignment.CenterHorizontally,
				modifier = Modifier
			) {
				Box(
					modifier = Modifier
						.width(64.dp)
						.height(40.dp)
						.background(MaterialTheme.colorScheme.background, RoundedCornerShape(16.dp))
						.border(
							width = 4.dp,
							color = if (currentImage == image) MaterialTheme.colorScheme.primary else Color.Transparent,
							shape = RoundedCornerShape(16.dp)
						)
						.clip(RoundedCornerShape(16.dp))
						.clickable {
							if (isPro) onChooseImage(image)
							else Toast
								.makeText(context, "Join Graphite Pro to add image as cover", Toast.LENGTH_SHORT)
								.show()
						}
				) {
					Image(
						painter = painterResource(id = image),
						contentDescription = "Image $image",
						contentScale = ContentScale.Crop,
						modifier = Modifier.fillMaxWidth()
					)
				}

				if (! isPro) {
					Box(
						modifier = Modifier
							.graphicsLayer {
								this.scaleX = 0.71f
								this.scaleY = 0.71f
							}
					) { ProTag() }
				}
			}
			Spacer(modifier = Modifier.width(6.dp))
		}
		Spacer(modifier = Modifier.width(18.dp))
	}
}
