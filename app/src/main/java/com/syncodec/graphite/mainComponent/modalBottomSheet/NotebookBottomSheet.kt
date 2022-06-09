package com.syncodec.graphite.mainComponent.modalBottomSheet

import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.custom.LargeTextField
import com.syncodec.graphite.custom.bottomSheet.BottomSheetHeader
import com.syncodec.graphite.custom.bottomSheet.BottomSheetStrip
import com.syncodec.graphite.custom.button.LargeButton
import com.syncodec.graphite.database.notebook.NotebookDbEntry
import com.syncodec.graphite.database.notebook.NotebookTheme
import com.syncodec.graphite.konstant.Color.Companion.colorList
import com.syncodec.graphite.mainComponent.MainActivity
import com.syncodec.graphite.miscellaneous.ThemeUtils.Companion.bookCoverImageList
import com.syncodec.graphite.miscellaneous.generatePrimaryKey
import com.syncodec.graphite.ui.theme.PremiumCompositionLocal

@Composable
fun NotebookBottomSheet(
	onAction: (MainActivity.Action, Pair<NotebookDbEntry, Boolean>) -> Unit
) {
	val context = LocalContext.current
	val isPremium = PremiumCompositionLocal.current

	var notebookTitleText by rememberSaveable { mutableStateOf("") }

	var notebookDescriptionText by rememberSaveable { mutableStateOf("") }

	var notebookTheme by remember { mutableStateOf(NotebookTheme.COLOR) }
	var notebookColor by remember { mutableStateOf<Color?>(null) }
	var notebookImage by remember { mutableStateOf<Int?>(null) }

	var currentState by remember { mutableStateOf(0) }
	LaunchedEffect(key1 = currentState) {
		notebookTheme = if (currentState == 0) NotebookTheme.COLOR else NotebookTheme.IMAGE
	}

	Surface(
		shape = RoundedCornerShape(12.dp, 12.dp, 0.dp, 0.dp),
		color = MaterialTheme.colorScheme.surface,
		modifier = Modifier
			.fillMaxWidth()
			.heightIn(420.dp),
	) {
		Column(
			horizontalAlignment = Alignment.CenterHorizontally,
			modifier = Modifier.fillMaxWidth()
		) {

			BottomSheetStrip()

			BottomSheetHeader(
				title = "Writing a new book?",
				icon = R.drawable.ic_notebook,
				subTitle = "Keep your notes organized in notebooks"
			)

			Spacer(modifier = Modifier.height(8.dp))

			LargeTextField(
				modifier = Modifier
					.fillMaxWidth()
					.padding(24.dp, 0.dp),
				text = notebookTitleText,
				placeholder = "Give your book a title",
			) { notebookTitleText = it }

			Spacer(modifier = Modifier.height(8.dp))

			LargeTextField(
				modifier = Modifier
					.fillMaxWidth()
					.padding(24.dp, 0.dp),
				text = notebookDescriptionText,
				placeholder = "And a little description"
			) { notebookDescriptionText = it }

//			Spacer(modifier = Modifier.height(16.dp))

//			StateButton(
//				stateList = listOf(
//					StateData(
//						title = "Color",
//						icon = R.drawable.ic_color,
//						stateTint = MaterialTheme.colorScheme.primary
//					),
//					StateData(
//						title = "Image",
//						icon = R.drawable.ic_gallery,
//						stateTint = MaterialTheme.colorScheme.primary
//					)
//				),
//				currentState = currentState,
//				modifier = Modifier
//					.fillMaxWidth()
//					.height(32.dp)
//					.padding(24.dp, 0.dp),
//			) { currentState = it }

			Spacer(modifier = Modifier.height(16.dp))

			Crossfade(
				targetState = notebookTheme,
				modifier = Modifier,
				animationSpec = tween(durationMillis = 600)
			) {
				when (it) {
					NotebookTheme.COLOR -> ColorChooser(
						currentColor = notebookColor
					) { color ->
						notebookColor = color
						notebookImage = null
					}
					NotebookTheme.IMAGE -> ImageChooser(
						currentImage = notebookImage
					) { image ->
						notebookColor = null
						notebookImage = image
					}
				}
			}

			Spacer(modifier = Modifier.height(12.dp))

			LargeButton(
				text = "Create",
				enabled = !(notebookColor == null && notebookImage == null) && notebookTitleText.isNotBlank(),
				modifier = Modifier
					.fillMaxWidth()
					.padding(24.dp, 0.dp)
			) {
				when {
					notebookTitleText.isEmpty() -> Toast.makeText(
						context,
						"Notebook title cannot be empty",
						Toast.LENGTH_SHORT
					).show()
					notebookColor == null -> Toast.makeText(
						context,
						"Select a color for notebook",
						Toast.LENGTH_SHORT
					).show()
					else -> {
						NotebookDbEntry(
							key = generatePrimaryKey(),
							createdTimestamp = System.currentTimeMillis()
						).apply {
							this.title = notebookTitleText
							this.description = notebookDescriptionText
							this.color = notebookColor?.toArgb()
							this.bitmap =
								notebookImage?.let {
									BitmapFactory.decodeResource(
										context.resources,
										it
									)
								}

							onAction(MainActivity.Action.NEW_NOTEBOOK, Pair(this, isPremium))
						}

						notebookTitleText = ""
						notebookDescriptionText = ""
					}
				}
			}

			Spacer(modifier = Modifier.height(32.dp))
		}
	}
}

@Composable
private fun ColorChooser(
	currentColor: Color?,
	onChooseColor: (Color) -> Unit
) {
	LazyRow(
		modifier = Modifier
			.fillMaxWidth()
	) {
		item { Spacer(modifier = Modifier.width(20.dp)) }
		for (element in colorList) {
			item {
				BookCard(
					color = element,
					highlight = element == currentColor
				) { color, _ ->
					onChooseColor(color!!)
				}
			}
		}
		item { Spacer(modifier = Modifier.width(20.dp)) }
	}
}

@Composable
private fun ImageChooser(
	currentImage: Int?,
	onChooseImage: (Int) -> Unit
) {
	LazyRow(
		modifier = Modifier
			.fillMaxWidth()
	) {
		item { Spacer(modifier = Modifier.width(20.dp)) }
		for (element in bookCoverImageList) {
			item {
				BookCard(
					image = element,
					highlight = element == currentImage
				) { _, image ->
					onChooseImage(image!!)
				}
			}
		}
		item { Spacer(modifier = Modifier.width(20.dp)) }
	}
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun BookCard(
	color: Color? = null,
	image: Int? = null,
	highlight: Boolean = false,
	onClick: (Color?, Int?) -> Unit
) {
	val selectionColor by animateColorAsState(targetValue = if (highlight) MaterialTheme.colorScheme.primary else Color.Transparent)

	Column(
		modifier = Modifier,
		horizontalAlignment = Alignment.CenterHorizontally
	) {
		Card(
			elevation = if (highlight) 8.dp else 0.dp,
			shape = RoundedCornerShape(4.dp, 16.dp, 16.dp, 4.dp),
			backgroundColor = color ?: Color.Transparent,
			modifier = Modifier
				.width(80.dp)
				.aspectRatio(0.75f)
				.padding(2.dp, 0.dp),
			onClick = {
				onClick(color, image)
			}
		) {
			if (image != null) {
				Image(
					painter = painterResource(id = image),
					contentDescription = null,
					contentScale = ContentScale.Crop
				)
			}

			Row(
				modifier = Modifier
					.fillMaxSize()
			) {
				Box(
					modifier = Modifier
						.width(12.dp)
						.fillMaxHeight()
						.background(Color.Black.copy(alpha = 0.31f))
				)
			}
		}

		Spacer(modifier = Modifier.height(8.dp))

		Box(
			modifier = Modifier
				.width(88.dp)
				.height(4.dp)
				.clip(RoundedCornerShape(25))
				.background(selectionColor)
		)
	}
}
