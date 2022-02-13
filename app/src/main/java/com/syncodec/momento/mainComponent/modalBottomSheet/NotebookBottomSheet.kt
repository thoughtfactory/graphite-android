package com.syncodec.momento.mainComponent.modalBottomSheet

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.syncodec.momento.R
import com.syncodec.momento.custom.BottomSheetHeader
import com.syncodec.momento.custom.BottomSheetStrip
import com.syncodec.momento.custom.button.LargeButton
import com.syncodec.momento.mainComponent.MainViewModel
import com.syncodec.momento.mainComponent.screen.MomentoScreenType
import compose.icons.TablerIcons
import compose.icons.tablericons.Notebook
import compose.icons.tablericons.Pencil
import kotlinx.coroutines.launch
import kotlin.random.Random


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun NotebookBottomSheet() {

	val viewModel: MainViewModel = viewModel()

	val scope = rememberCoroutineScope()
	val focusManager = LocalFocusManager.current

	var notebookTitleText by rememberSaveable { mutableStateOf("") }
	var isNotebookTitleTextFocused by remember { mutableStateOf(false) }

	var notebookDescriptionText by rememberSaveable { mutableStateOf("") }
	var isNotebookDescriptionTextFocused by remember { mutableStateOf(false) }

	var notebookTheme by remember { mutableStateOf(NotebookTheme.COLOR) }
	var notebookColor: Color? = null
	var notebookImage: Bitmap? = null

	val createButtonColors = ButtonDefaults.buttonColors(
		contentColor = MaterialTheme.colorScheme.primaryContainer,
		disabledContentColor = Color.DarkGray,
		containerColor = MaterialTheme.colorScheme.onPrimaryContainer,
		disabledContainerColor = Color.LightGray
	)

	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		modifier = Modifier
			.fillMaxWidth()
			.heightIn(420.dp)
			.background(MaterialTheme.colorScheme.background),
	) {

		BottomSheetStrip()

		Spacer(modifier = Modifier.height(12.dp))

		BottomSheetHeader(
			title = "Writing a new book?",
			imageVector = TablerIcons.Notebook,
			subTitle = "Keep your notes organized in notebooks"
		)

		Spacer(modifier = Modifier.height(8.dp))

		LargeTextField(
			text = notebookTitleText,
			placeholder = "Give your book a title",
			isFocused = isNotebookTitleTextFocused,
			onFocusChanged = { isNotebookTitleTextFocused = it }
		) { notebookTitleText = it }

		Spacer(modifier = Modifier.height(12.dp))

		LargeTextField(
			text = notebookDescriptionText,
			placeholder = "And a little description",
			isFocused = isNotebookDescriptionTextFocused,
			onFocusChanged = { isNotebookDescriptionTextFocused = it }
		) { notebookDescriptionText = it }

		Spacer(modifier = Modifier.height(8.dp))

		ThemeChooser(
			notebookTheme = notebookTheme
		) {
			notebookTheme = it
		}

		Crossfade(
			targetState = notebookTheme,
			modifier = Modifier,
			animationSpec = tween(
				durationMillis = 400
			)
		) {
			when (it) {
				NotebookTheme.COLOR -> ColorChooser { color ->
					notebookColor = color
					notebookImage = null
				}
				NotebookTheme.IMAGE -> ImageChooser { image ->
					notebookColor = null
					notebookImage = image
				}
			}
		}

		Spacer(modifier = Modifier.height(16.dp))

		LargeButton(
			text = "Create",
			backgroundColor = createButtonColors.containerColor(enabled = notebookTitleText.isNotBlank()).value,
			textColor = createButtonColors.contentColor(enabled = notebookTitleText.isNotBlank()).value,
			modifier = Modifier
				.fillMaxWidth()
				.height(48.dp)
				.padding(24.dp, 0.dp)
				.focusable()
		) {
			viewModel.insertNotebook(
				title = notebookTitleText,
				description = notebookDescriptionText,
				color = notebookColor?.toArgb(),
				image = notebookImage
			)
			focusManager.clearFocus()
			scope.launch {
				viewModel.mainActivityState.bottomSheetState.hide()
			}

			notebookTitleText = ""
			notebookDescriptionText = ""
		}

		Spacer(modifier = Modifier.height(32.dp))
	}
}

@Composable
private fun LargeTextField(
	text: String,
	placeholder: String,
	isFocused: Boolean,
	onFocusChanged: (Boolean) -> Unit,
	onValueChanged: (String) -> Unit
) {
	BasicTextField(
		modifier = Modifier
			.fillMaxWidth()
			.height(48.dp)
			.padding(24.dp, 0.dp)
			.background(
				if (text.isEmpty() && !isFocused) {
					Color.LightGray.copy(alpha = 0.13f)
				} else {
					MaterialTheme.colorScheme.background
				}
			)
			.onFocusChanged { onFocusChanged(it.isFocused) },
		value = text,
		onValueChange = { onValueChanged(it) },
		singleLine = true,
		cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
		textStyle = MaterialTheme.typography.bodyMedium,
		decorationBox = { innerTextField ->
			Card(
				backgroundColor = Color.Transparent,
				shape = RoundedCornerShape(2.dp),
				border = BorderStroke(1.dp, if (isFocused) MaterialTheme.colorScheme.primary else Color.LightGray),
				elevation = 0.dp
			) {
				Row(
					verticalAlignment = Alignment.CenterVertically,
					modifier = Modifier.padding(16.dp, 0.dp)
				) {
					Box {
						if (text.isEmpty()) {
							Text(
								placeholder,
								style = MaterialTheme.typography.bodyMedium,
								color = Color.LightGray
							)
						}
						innerTextField()
					}
				}
			}
		}
	)
}

private enum class NotebookTheme {
	COLOR,
	IMAGE
}

@Composable
private fun ThemeChooser(
	notebookTheme: NotebookTheme,
	onThemeChange: (NotebookTheme) -> Unit
) {
	val configuration = LocalConfiguration.current
	val screenWidth = configuration.screenWidthDp.dp

	val buttonWidth = (screenWidth - 48.dp) / 2

	val spacerWidth by animateDpAsState(
		targetValue = when (notebookTheme) {
			NotebookTheme.COLOR -> 0.dp
			NotebookTheme.IMAGE -> buttonWidth
		},
		tween(
			durationMillis = 400
		)
	)

	val colorColor by animateColorAsState(
		targetValue = if (notebookTheme == NotebookTheme.COLOR) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer,
		tween(durationMillis = 400)
	)
	val imageColor by animateColorAsState(
		targetValue = if (notebookTheme == NotebookTheme.IMAGE) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer,
		tween(durationMillis = 400)
	)

	val interactionSource = remember { MutableInteractionSource() }

	Column(
		modifier = Modifier
			.fillMaxWidth()
			.height(56.dp)
			.padding(24.dp, 0.dp),
		verticalArrangement = Arrangement.Center
	) {
		Box(
			modifier = Modifier
				.fillMaxWidth()
				.height(40.dp),
			contentAlignment = Alignment.Center
		) {
			Box(
				modifier = Modifier
					.fillMaxWidth()
					.height(28.dp)
					.padding(4.dp, 0.dp)
					.clip(RoundedCornerShape(12.dp))
					.background(MaterialTheme.colorScheme.secondaryContainer)
			)

			Row(
				modifier = Modifier
					.fillMaxSize()
			) {
				Spacer(modifier = Modifier.width(spacerWidth))
				Box(
					modifier = Modifier
						.fillMaxHeight()
						.width(buttonWidth)
						.clip(RoundedCornerShape(24.dp))
						.background(MaterialTheme.colorScheme.primary),
				)
			}

			Row(
				modifier = Modifier
					.fillMaxWidth()
					.height(28.dp),
				verticalAlignment = Alignment.CenterVertically
			) {
				Box(
					contentAlignment = Alignment.Center,
					modifier = Modifier
						.fillMaxHeight()
						.weight(1f)
						.clip(RoundedCornerShape(24.dp))
						.clickable(interactionSource = interactionSource, indication = null) {
							onThemeChange(NotebookTheme.COLOR)
						},
				) {
					Text(
						text = "Color",
						style = MaterialTheme.typography.bodySmall,
						color = colorColor,
						fontWeight = FontWeight.Bold,
						textAlign = TextAlign.Center,
						maxLines = 1
					)
				}
				Box(
					contentAlignment = Alignment.Center,
					modifier = Modifier
						.fillMaxHeight()
						.weight(1f)
						.clip(RoundedCornerShape(24.dp))
						.clickable(interactionSource = interactionSource, indication = null) {
							onThemeChange(NotebookTheme.IMAGE)
						},
				) {
					Text(
						text = "Image",
						style = MaterialTheme.typography.bodySmall,
						color = imageColor,
						fontWeight = FontWeight.Bold,
						textAlign = TextAlign.Center,
						maxLines = 1
					)
				}
			}
		}
	}
}

@Composable
private fun ColorChooser(
	onChooseColor: (Color) -> Unit
) {
	val colorList: List<Color> = listOf(
		Color.Red,
		Color.Green,
		Color.Blue
	)

	LazyRow(
		modifier = Modifier
			.fillMaxWidth()
	) {
		item {
			Spacer(modifier = Modifier.width(8.dp))
			BookCard(
				color = colorList.first()
			) { color, _ ->
				onChooseColor(color!!)
			}
		}
		for (i in 1 until colorList.size - 1) {
			item {
				BookCard(
					color = colorList[i]
				) { color, _ ->
					onChooseColor(color!!)
				}
			}
		}
		item {
			BookCard(
				color = colorList.last()
			) { color, _ ->
				onChooseColor(color!!)
			}
			Spacer(modifier = Modifier.width(8.dp))
		}
	}
}

@Composable
private fun ImageChooser(
	onChooseImage: (Bitmap) -> Unit
) {
	val context = LocalContext.current

	val imageList: List<Int> = listOf(
		R.drawable.book_cover_1,
		R.drawable.background,
		R.drawable.book_cover_1,
		R.drawable.book_cover_1,
		R.drawable.book_cover_1,
		R.drawable.book_cover_1,
		R.drawable.book_cover_1,
		R.drawable.book_cover_1,
		R.drawable.book_cover_1,
		R.drawable.book_cover_1,
	)

	LazyRow(
		modifier = Modifier
			.fillMaxWidth()
	) {
		item {
			Spacer(modifier = Modifier.width(8.dp))
			BookCard(
				image = imageList.first()
			) { _, image ->
				onChooseImage(BitmapFactory.decodeResource(context.resources, image!!))
			}
		}
		for (i in 1 until imageList.size - 1) {
			item {
				BookCard(
					image = imageList[i]
				) { _, image ->
					onChooseImage(BitmapFactory.decodeResource(context.resources, image!!))
				}
			}
		}
		item {
			BookCard(
				image = imageList.last()
			) { _, image ->
				onChooseImage(BitmapFactory.decodeResource(context.resources, image!!))
			}
			Spacer(modifier = Modifier.width(8.dp))
		}
	}
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun BookCard(
	color: Color? = null,
	image: Int? = null,
	onClick: (Color?, Int?) -> Unit
) {
	Card(
		elevation = 8.dp,
		shape = RoundedCornerShape(4.dp, 16.dp, 16.dp, 4.dp),
		backgroundColor = color ?: Color.Transparent,
		modifier = Modifier
			.width(80.dp)
			.aspectRatio(0.75f)
			.padding(4.dp),
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
}
