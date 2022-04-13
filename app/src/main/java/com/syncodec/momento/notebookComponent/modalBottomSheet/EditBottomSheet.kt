package com.syncodec.momento.notebookComponent.modalBottomSheet

import android.graphics.BitmapFactory
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.syncodec.momento.R
import com.syncodec.momento.custom.LargeTextField
import com.syncodec.momento.custom.bottomSheet.BottomSheetHeader
import com.syncodec.momento.custom.bottomSheet.BottomSheetStrip
import com.syncodec.momento.custom.button.LargeButton
import com.syncodec.momento.custom.button.StateButton
import com.syncodec.momento.custom.button.StateData
import com.syncodec.momento.database.notebook.NotebookDbEntry
import com.syncodec.momento.database.notebook.NotebookTheme
import com.syncodec.momento.miscellaneous.ThemeUtils.Companion.bookCoverImageList
import com.syncodec.momento.miscellaneous.ThemeUtils.Companion.tone
import com.syncodec.momento.notebookComponent.NotebookActivity


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun EditBottomSheet(
	notebookDbEntry: NotebookDbEntry,
	onAction: (NotebookActivity.Action, Any?) -> Unit
) {
	val context = LocalContext.current
	val focusManager = LocalFocusManager.current

	var notebookTitleText by rememberSaveable { mutableStateOf(notebookDbEntry.title) }
	var isNotebookTitleTextFocused by remember { mutableStateOf(false) }

	var notebookDescriptionText by rememberSaveable { mutableStateOf(notebookDbEntry.description) }
	var isNotebookDescriptionTextFocused by remember { mutableStateOf(false) }

	var notebookTheme by remember { mutableStateOf(NotebookTheme.COLOR) }
	var notebookColor by remember { mutableStateOf(notebookDbEntry.color?.let { Color(it) }) }
	var notebookImage by remember { mutableStateOf<Int?>(null) }

	var currentState by remember { mutableStateOf(0) }
	LaunchedEffect(key1 = currentState) {
		notebookTheme = if (currentState == 0) NotebookTheme.COLOR else NotebookTheme.IMAGE
	}

	Surface(
		shape = RoundedCornerShape(12.dp, 12.dp, 0.dp, 0.dp),
		color = MaterialTheme.colorScheme.surface.tone(isSystemInDarkTheme(), 2),
		modifier = Modifier
			.fillMaxWidth()
			.heightIn(180.dp),
	) {
		Column(
			horizontalAlignment = Alignment.CenterHorizontally,
			modifier = Modifier
				.fillMaxWidth()
				.heightIn(420.dp)
		) {

			BottomSheetStrip()

			BottomSheetHeader(
				title = "Writing a new book?",
				icon = R.drawable.ic_notebook,
				subTitle = "Keep your notes organized in notebooks"
			)

			Spacer(modifier = Modifier.height(8.dp))

			LargeTextField(
				text = notebookTitleText,
				placeholder = "Give your book a title",
				isFocused = isNotebookTitleTextFocused,
				onFocusChanged = { isNotebookTitleTextFocused = it },
				modifier = Modifier
					.fillMaxWidth()
					.padding(24.dp, 0.dp)
			) { notebookTitleText = it }

			Spacer(modifier = Modifier.height(8.dp))

			LargeTextField(
				text = notebookDescriptionText ?: "",
				placeholder = "And a little description",
				isFocused = isNotebookDescriptionTextFocused,
				onFocusChanged = { isNotebookDescriptionTextFocused = it },
				modifier = Modifier
					.fillMaxWidth()
					.padding(24.dp, 0.dp)
			) { notebookDescriptionText = it }

			Spacer(modifier = Modifier.height(16.dp))

			StateButton(
				stateList = listOf(
					StateData(
						title = "Color",
						icon = R.drawable.ic_color,
						stateTint = MaterialTheme.colorScheme.primary
					),
					StateData(
						title = "Image",
						icon = R.drawable.ic_gallery,
						stateTint = MaterialTheme.colorScheme.primary
					)
				),
				currentState = currentState,
				modifier = Modifier
					.fillMaxWidth()
					.height(32.dp)
					.padding(24.dp, 0.dp),
			) { currentState = it }

			Spacer(modifier = Modifier.height(12.dp))

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

			Spacer(modifier = Modifier.height(16.dp))

			LargeButton(
				text = "Update",
				enabled = !(notebookColor == null && notebookImage == null) && notebookTitleText.isNotBlank(),
				modifier = Modifier
					.fillMaxWidth()
					.padding(24.dp, 0.dp)
			) {
				notebookDbEntry.apply {
					this.title = notebookTitleText
					this.description = notebookDescriptionText
					this.color = notebookColor?.toArgb()
					this.bitmap =
						notebookImage?.let { BitmapFactory.decodeResource(context.resources, it) }

					onAction(NotebookActivity.Action.UPDATE_NOTEBOOK, this)
				}

				focusManager.clearFocus()
				notebookTitleText = ""
				notebookDescriptionText = ""
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
		for (element in com.syncodec.momento.konstant.Color.colorList) {
			item {
				BookCard(
					color = element,
					highlight = element == currentColor
				) { color, _ -> onChooseColor(color!!) }
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
				) { _, image -> onChooseImage(image!!) }
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
			onClick = { onClick(color, image) }
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
