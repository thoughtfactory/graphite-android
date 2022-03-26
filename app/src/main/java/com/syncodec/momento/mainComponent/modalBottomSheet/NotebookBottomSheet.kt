package com.syncodec.momento.mainComponent.modalBottomSheet

import android.graphics.BitmapFactory
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.*
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.syncodec.momento.R
import com.syncodec.momento.custom.BottomSheetHeader
import com.syncodec.momento.custom.BottomSheetStrip
import com.syncodec.momento.custom.LargeTextField
import com.syncodec.momento.custom.button.LargeButton
import com.syncodec.momento.custom.button.StateButton
import com.syncodec.momento.custom.button.StateData
import com.syncodec.momento.konstant.Color.Companion.colorList
import com.syncodec.momento.mainComponent.MainViewModel
import compose.icons.TablerIcons
import compose.icons.tablericons.*
import kotlinx.coroutines.launch

private val imageList: List<Int> = listOf(
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

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun NotebookBottomSheet() {
	val context = LocalContext.current

	val viewModel: MainViewModel = viewModel()

	val scope = rememberCoroutineScope()
	val focusManager = LocalFocusManager.current

	var notebookTitleText by rememberSaveable { mutableStateOf("") }
	var isNotebookTitleTextFocused by remember { mutableStateOf(false) }

	var notebookDescriptionText by rememberSaveable { mutableStateOf("") }
	var isNotebookDescriptionTextFocused by remember { mutableStateOf(false) }

	var notebookTheme by remember { mutableStateOf(NotebookTheme.COLOR) }
	var notebookColor by remember { mutableStateOf<Color?>(null) }
	var notebookImage by remember { mutableStateOf<Int?>(null) }

	var currentState by remember { mutableStateOf(0)}
	LaunchedEffect(key1 = currentState) {
		notebookTheme = if (currentState == 0) NotebookTheme.COLOR else NotebookTheme.IMAGE
	}

	val containerColor by animateColorAsState(
		targetValue = if (!(notebookColor == null && notebookImage == null) && notebookTitleText.isNotBlank()) MaterialTheme.colorScheme.onPrimaryContainer else Color.LightGray,
		animationSpec = tween(durationMillis = 600)
	)
	val contentColor by animateColorAsState(
		targetValue = if (!(notebookColor == null && notebookImage == null) && notebookTitleText.isNotBlank()) MaterialTheme.colorScheme.primaryContainer else Color.DarkGray,
		animationSpec = tween(durationMillis = 600)
	)

	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		modifier = Modifier
			.fillMaxWidth()
			.heightIn(420.dp)
			.background(MaterialTheme.colorScheme.background),
	) {

		BottomSheetStrip()

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
			onFocusChanged = { isNotebookTitleTextFocused = it },
			modifier = Modifier
				.fillMaxWidth()
				.padding(24.dp, 0.dp)
		) { notebookTitleText = it }

		Spacer(modifier = Modifier.height(8.dp))

		LargeTextField(
			text = notebookDescriptionText,
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
				StateData(title = "Color", icon = TablerIcons.ColorSwatch, color = MaterialTheme.colorScheme.primary),
				StateData(title = "Image", icon = TablerIcons.Photo, color = MaterialTheme.colorScheme.primary)
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
			text = "Create",
			containerColor = containerColor,
			contentColor = contentColor,
			isClickable = !(notebookColor == null && notebookImage == null) && notebookTitleText.isNotBlank(),
			isElevated = !(notebookColor == null && notebookImage == null) && notebookTitleText.isNotBlank(),
			modifier = Modifier
				.fillMaxWidth()
				.padding(24.dp, 0.dp)
		) {
			viewModel.insertNotebook(
				title = notebookTitleText,
				description = notebookDescriptionText,
				color = notebookColor?.toArgb(),
				image = notebookImage?.let { BitmapFactory.decodeResource(context.resources, it) }
			)
			focusManager.clearFocus()
			scope.launch {
				viewModel.activityState.bottomSheetState.hide()
			}

			notebookTitleText = ""
			notebookDescriptionText = ""
		}

		Spacer(modifier = Modifier.height(32.dp))
	}
}

private enum class NotebookTheme {
	COLOR,
	IMAGE
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
		for (element in imageList) {
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
	val selectionColor by animateColorAsState(targetValue = if (highlight) MaterialTheme.colorScheme.secondary else Color.Transparent)

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
