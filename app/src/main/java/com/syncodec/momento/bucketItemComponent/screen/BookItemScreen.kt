package com.syncodec.momento.bucketItemComponent.screen

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.google.android.gms.maps.model.LatLng
import com.syncodec.momento.bucketComponent.modalBottomSheet.BookData
import com.syncodec.momento.custom.button.LargeButton
import com.syncodec.momento.custom.button.StateButton
import com.syncodec.momento.custom.button.StateData
import compose.icons.TablerIcons
import compose.icons.tablericons.*


@Composable
fun BookItemScreen(
	@PreviewParameter(MockBookData::class)
	bookData: BookData,
	thumbnail: Any? = null,
	thoughtList: SnapshotStateList<String> = mutableStateListOf(),
	initialState: Int,
	onStateChange: (Int) -> Unit = {}
) {
	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		modifier = Modifier
			.fillMaxWidth()
			.padding(16.dp, 0.dp)
			.background(MaterialTheme.colorScheme.background)
			.verticalScroll(state = rememberScrollState())
	) {
		Spacer(modifier = Modifier.height(24.dp))

		ThumbnailCard(thumbnail = thumbnail)
		Spacer(modifier = Modifier.height(24.dp))

		HeaderCard(bookData = bookData)
		Spacer(modifier = Modifier.height(12.dp))

		ThoughtCard(thoughtList = thoughtList)
		Spacer(modifier = Modifier.height(12.dp))

		StateButton(
			stateList = listOf(
				StateData(title = "To Read", icon = TablerIcons.Clock, color = MaterialTheme.colorScheme.primary),
				StateData(title = "Reading", icon = TablerIcons.Book, color = Color(245, 118, 26)),
				StateData(title = "Read", icon = TablerIcons.Check, color = Color(81, 146, 89)),
			),
			initialState = initialState,
			modifier = Modifier
				.height(48.dp)
		) {
			onStateChange(it)
		}
		Spacer(modifier = Modifier.height(12.dp))

		LargeButton(
			text = "View in Open Library",
			containerColor = MaterialTheme.colorScheme.tertiaryContainer,
			contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
			isClickable = true,
			modifier = Modifier
				.fillMaxWidth()
		) {

		}

		Spacer(modifier = Modifier.height(32.dp))
	}
}

@Composable
private fun ThumbnailCard(
	thumbnail: Any?
) {
	val configuration = LocalConfiguration.current
	val screenWidth = configuration.screenWidthDp.dp

	Card(
		elevation = 8.dp,
		shape = RoundedCornerShape(12.dp),
		modifier = Modifier
			.width(screenWidth * 0.5f)
			.aspectRatio(0.75f)
			.padding(0.dp),
	) {
		Image(
			painter = rememberImagePainter(
				data = thumbnail,
				builder = {
					crossfade(true)
				}
			),
			contentDescription = null,
			contentScale = ContentScale.Crop,
			modifier = Modifier
				.fillMaxSize()
		)

		Box(
			modifier = Modifier
				.fillMaxSize()
				.padding(5.dp),
			contentAlignment = Alignment.BottomEnd
		) {
			Card(
				modifier = Modifier
					.requiredSize(40.dp)
					.clip(CircleShape)
					.clickable { },
				shape = CircleShape,
				elevation = 0.dp,
				backgroundColor = Color.Black.copy(alpha = 0.47f)
			) {
				Icon(
					imageVector = TablerIcons.Pencil,
					contentDescription = null,
					tint = MaterialTheme.colorScheme.primaryContainer,
					modifier = Modifier
						.fillMaxSize()
						.padding(10.dp)
				)
			}
		}
	}
}

@Preview
@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun HeaderCard(
	@PreviewParameter(MockBookData::class)
	bookData: BookData
) {
	Card(
		modifier = Modifier
			.fillMaxWidth(),
		elevation = 0.dp,
		shape = RoundedCornerShape(12.dp),
		border = BorderStroke(2.dp, MaterialTheme.colorScheme.secondaryContainer),
		backgroundColor = Color.Transparent,
	) {
		Column(
			modifier = Modifier
				.padding(16.dp)
		) {
			Text(
				text = bookData.title,
				color = MaterialTheme.colorScheme.onBackground,
				style = MaterialTheme.typography.titleLarge,
				fontWeight = FontWeight.Bold,
				modifier = Modifier,
			)

			Spacer(modifier = Modifier.height(4.dp))

			Row(
				modifier = Modifier
					.fillMaxWidth()
			) {
				if (bookData.authorName != null && bookData.authorName.isNotEmpty()) {
					Text(
						text = bookData.authorName[0],
						color = MaterialTheme.colorScheme.primary,
						style = MaterialTheme.typography.bodyMedium,
						fontWeight = FontWeight.Bold,
						modifier = Modifier
					)
				}
				Spacer(modifier = Modifier.weight(1f))
				if (bookData.firstPublishYear != null) {
					Text(
						text = bookData.firstPublishYear.toString(),
						color = MaterialTheme.colorScheme.primary,
						style = MaterialTheme.typography.bodyMedium,
						fontWeight = FontWeight.Bold,
						modifier = Modifier
					)
				}
			}
		}
	}
}

@Preview
@OptIn(ExperimentalMaterialApi::class, ExperimentalAnimationApi::class)
@Composable
private fun ThoughtCard(
	@PreviewParameter(MockThoughtListList::class)
	thoughtList: SnapshotStateList<String>,
) {
	var showEditor by remember { mutableStateOf(false) }

	Card(
		modifier = Modifier
			.fillMaxWidth(),
		elevation = 0.dp,
		shape = RoundedCornerShape(12.dp),
		border = BorderStroke(2.dp, MaterialTheme.colorScheme.secondaryContainer),
		backgroundColor = Color.Transparent,
	) {
		Column(
			modifier = Modifier,
			horizontalAlignment = Alignment.CenterHorizontally
		) {
			thoughtList.forEachIndexed { index, thought ->
				ThoughtContent(
					thought = thought,
					isFirst = index == 0
				) { thoughtList[index] = it }
				Box(
					modifier = Modifier
						.fillMaxWidth(0.71f)
						.height(2.dp)
						.padding(16.dp, 0.dp)
						.background(MaterialTheme.colorScheme.secondaryContainer)
				)
			}

			AnimatedContent(targetState = showEditor) {
				if (it) ThoughtEditor(
					isFirst = thoughtList.isEmpty(),
					onSave = { thought ->
						thoughtList.add(thought)
					},
					onDiscard = { showEditor = false }
				)
				else AddThoughtButton { showEditor = true }
			}
		}
	}
}

@OptIn(ExperimentalAnimationApi::class, ExperimentalFoundationApi::class)
@Composable
private fun ThoughtContent(
	thought: String,
	isFirst: Boolean,
	onSave: (String) -> Unit
) {
	var showEditor by remember { mutableStateOf(false) }

	Box(
		modifier = Modifier
			.fillMaxWidth()
			.combinedClickable(
				onClick = {},
				onLongClick = { showEditor = true }
			)
	) {
		AnimatedContent(targetState = showEditor) {
			if (it) {
				Column(
					modifier = Modifier
				) {
					ThoughtEditor(
						text = thought,
						isFirst = isFirst,
						onSave = {
							onSave(it)
							showEditor = false
						},
						onDiscard = { showEditor = false }
					)
				}
			} else {
				Text(
					text = thought,
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.onBackground,
					modifier = Modifier
						.fillMaxWidth()
						.padding(16.dp, if (isFirst) 16.dp else 12.dp, 16.dp, 12.dp)
				)
			}
		}
	}
}

@Preview
@Composable
private fun AddThoughtButton(
	onClick: () -> Unit = {}
) {
	Box(
		modifier = Modifier
			.fillMaxWidth()
			.clickable { onClick() }
	) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier
				.fillMaxWidth()
				.padding(16.dp)
		) {
			Icon(
				imageVector = TablerIcons.Bulb,
				contentDescription = null,
				tint = MaterialTheme.colorScheme.primary
			)
			Spacer(modifier = Modifier.width(16.dp))
			Text(
				text = "Add your thoughts",
				color = MaterialTheme.colorScheme.primary,
				style = MaterialTheme.typography.titleSmall,
			)
		}
	}
}

@Preview
@Composable
fun ThoughtEditor(
	text: String = "",
	isFirst: Boolean = false,
	onSave: (String) -> Unit = {},
	onDiscard: () -> Unit = {}
) {
	val context = LocalContext.current
	var thought by remember { mutableStateOf(text) }
	val focusRequester = remember { FocusRequester() }

	Column(
		modifier = Modifier
	) {
		BasicTextField(
			value = thought,
			onValueChange = { thought = it },
			textStyle = MaterialTheme.typography.bodyMedium.copy(
				color = MaterialTheme.colorScheme.onBackground
			),
			cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
			modifier = Modifier
				.fillMaxWidth()
				.heightIn(64.dp, Dp.Infinity)
				.padding(16.dp, if (isFirst) 16.dp else 12.dp, 16.dp, 16.dp)
				.focusRequester(focusRequester),
		) { innerTextField ->
			if (thought.isEmpty()) {
				Text(
					"What are you thinking?",
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.primaryContainer
				)
			}
			innerTextField()
		}
		Spacer(modifier = Modifier.height(8.dp))
		ThoughtController(
			onSave = {
				if (thought.isNotEmpty()) {
					onSave(thought)
					thought = ""
				} else Toast
					.makeText(context, "Don't keep the field empty", Toast.LENGTH_SHORT)
					.show()
			},
			onDiscard = onDiscard
		)
	}
}

@Preview
@Composable
private fun ThoughtController(
	onSave: () -> Unit = {},
	onDiscard: () -> Unit = {}
) {
	Row(
		modifier = Modifier
	) {
		Box(
			modifier = Modifier
				.weight(1f)
				.padding(4.dp)
				.clip(RoundedCornerShape(12.dp))
				.clickable { onDiscard() },
			contentAlignment = Alignment.Center
		) {
			Row(
				modifier = Modifier
					.height(32.dp)
					.padding(4.dp),
				verticalAlignment = Alignment.CenterVertically,
				horizontalArrangement = Arrangement.Center
			) {
				Icon(
					imageVector = TablerIcons.X,
					contentDescription = "Discard",
					tint = MaterialTheme.colorScheme.onBackground,
					modifier = Modifier
						.requiredSize(16.dp)
				)
				Spacer(modifier = Modifier.width(8.dp))
				Text(
					text = "Discard",
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.onBackground
				)
			}
		}

		Box(
			modifier = Modifier
				.weight(1f)
				.padding(4.dp)
				.clip(RoundedCornerShape(12.dp))
				.background(MaterialTheme.colorScheme.primaryContainer)
				.clickable { onSave() },
			contentAlignment = Alignment.Center
		) {
			Row(
				modifier = Modifier
					.height(32.dp)
					.padding(4.dp),
				verticalAlignment = Alignment.CenterVertically,
				horizontalArrangement = Arrangement.Center
			) {
				Icon(
					imageVector = TablerIcons.Check,
					contentDescription = "Save",
					tint = MaterialTheme.colorScheme.onPrimaryContainer,
					modifier = Modifier
						.requiredSize(16.dp)
				)
				Spacer(modifier = Modifier.width(8.dp))
				Text(
					text = "Save",
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.onPrimaryContainer
				)
			}
		}
	}
}

@Composable
private fun LocationCard(
	latLng: LatLng?
) {
	Card(
		modifier = Modifier
			.fillMaxWidth(),
		elevation = 0.dp,
		shape = RoundedCornerShape(12.dp),
		border = BorderStroke(2.dp, MaterialTheme.colorScheme.secondaryContainer),
		backgroundColor = Color.Transparent,
	) {
		Box(
			modifier = Modifier
				.fillMaxWidth()
				.clickable { }
		) {
			Row(
				verticalAlignment = Alignment.CenterVertically,
				modifier = Modifier
					.fillMaxWidth()
					.padding(16.dp)
			) {
				Icon(
					imageVector = TablerIcons.MapPin,
					contentDescription = null,
					tint = MaterialTheme.colorScheme.primary
				)
				Spacer(modifier = Modifier.width(16.dp))
				Text(
					text = "Add location",
					color = MaterialTheme.colorScheme.primary,
					style = MaterialTheme.typography.titleSmall,
				)
			}
		}
	}
}

private class MockThoughtListList : PreviewParameterProvider<List<String>> {
	override val values = sequenceOf(
		listOf(
			"“A snowball in the face is surely the perfect beginning to a lasting friendship.”\n" +
					"― Markus Zusak, The Book Thief",
			"“A small but noteworthy note. I've seen so many young men over the years who think they're running at other young men. They are not. They are running at me.”\n" +
					"― Markus Zusak, The Book Thief"
		)
	)
}

private class MockBookData : PreviewParameterProvider<BookData> {
	override val values = sequenceOf(
		BookData(
			key = "",
			title = "The Book Thief",
			coverI = "",
			authorName = listOf("Markus Zusak"),
			firstPublishYear = 2005
		)
	)
}
