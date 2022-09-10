package com.syncodec.graphite.presentation.note.composable.bottomSheet

import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.TagObject
import com.syncodec.graphite.presentation.custom.bottomSheet.BottomSheetHeader
import com.syncodec.graphite.presentation.custom.bottomSheet.BottomSheetStrip
import com.syncodec.graphite.presentation.custom.button.MenuButton
import com.syncodec.graphite.presentation.note.NoteViewModel
import com.syncodec.graphite.utils.getInverseBWColor
import com.syncodec.graphite.utils.getRandomColor


@Composable
fun TagBottomSheet(
	closeSheet: () -> Unit
) {
	val viewModel: NoteViewModel = viewModel()

	val tagList by viewModel.tagObjectList.collectAsState(initial = listOf())

	var newTagColor by remember { mutableStateOf(getRandomColor()) }

	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		modifier = Modifier
			.fillMaxWidth()
			.background(MaterialTheme.colorScheme.surface)
	) {
		BottomSheetStrip()

		BottomSheetHeader(
			title = "Tag",
			icon = R.drawable.ic_hashtag,
		)

		Spacer(modifier = Modifier.height(8.dp))

		TagSearchBar(
			tagColor = newTagColor,
			closeSheet = closeSheet
		) { tag, color ->
			TagObject().apply {
				this.tag = tag
				this.color = color.toArgb()
			}.apply {
				viewModel.putTag(tagObject = this)
				newTagColor = getRandomColor()
				closeSheet()
			}
		}

		Spacer(modifier = Modifier.height(8.dp))

		TagList(tagList = tagList) {

		}

		Spacer(modifier = Modifier.height(32.dp))
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TagSearchBar(
	tagColor: Color,
	closeSheet: () -> Unit,
	onAddTag: (String, Color) -> Unit
) {
	val context = LocalContext.current

	var searchQuery by remember { mutableStateOf("") }

	TextField(
		value = searchQuery,
		onValueChange = { searchQuery = it },
		placeholder = { Text(text = "Search or add tag") },
		keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
		keyboardActions = KeyboardActions(onDone = { closeSheet() }),
		colors = TextFieldDefaults.textFieldColors(
			containerColor = MaterialTheme.colorScheme.background,
			textColor = MaterialTheme.colorScheme.onBackground,
			cursorColor = MaterialTheme.colorScheme.primary,
			placeholderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.47f),
		),
		leadingIcon = {
			MenuButton(
				icon = R.drawable.ic_hashtag_2,
				contentDescription = "Hashtag",
				tint = tagColor
			) {

			}
		},
		trailingIcon = {
			Row {
				MenuButton(
					icon = R.drawable.ic_close,
					contentDescription = "Clear search",
					tint = MaterialTheme.colorScheme.onBackground.copy(0.71f)
				) { searchQuery = "" }
				MenuButton(
					icon = R.drawable.ic_add,
					contentDescription = "Add new tag",
					tint = MaterialTheme.colorScheme.onBackground.copy(0.71f)
				) {
					when {
						searchQuery.isBlank() -> Toast.makeText(context, "Tag is empty", Toast.LENGTH_SHORT).show()
						searchQuery.split(" ").size > 1 -> Toast.makeText(context, "Tag should not contains spaces", Toast.LENGTH_SHORT).show()
						else -> {
							onAddTag(searchQuery, tagColor)
							searchQuery = ""
						}
					}
				}
				Spacer(modifier = Modifier.width(4.dp))
			}
		},
		modifier = Modifier
			.fillMaxWidth()
			.clip(RoundedCornerShape(8.dp))
			.padding(horizontal = 16.dp)
	)
}

@Composable
private fun TagList(
	tagList: List<TagObject>,
	onClick: (TagObject) -> Unit
) {
	Column(
		modifier = Modifier
			.fillMaxWidth()
			.padding(horizontal = 16.dp)
			.verticalScroll(rememberScrollState())
	) {
		tagList.forEach { tag ->
			Tag(tag = tag) { onClick(tag) }
		}
	}
}

@Composable
private fun Tag(
	tag: TagObject,
	onClick: () -> Unit
) {
	Column(
		modifier = Modifier
			.fillMaxWidth()
			.clip(RoundedCornerShape(12.dp))
			.clickable { onClick() }
	) {
		Spacer(modifier = Modifier.height(4.dp))
		Row(
			modifier = Modifier.fillMaxWidth(),
			verticalAlignment = Alignment.CenterVertically
		) {
			Spacer(modifier = Modifier.width(11.dp))
			Icon(
				painter = painterResource(id = R.drawable.ic_hashtag),
				contentDescription = tag.tag,
				tint = MaterialTheme.colorScheme.onBackground.copy(0.71f)
			)
			Spacer(modifier = Modifier.width(8.dp))

			Card(
				colors = CardDefaults.cardColors(
					containerColor = Color(tag.color),
					contentColor = Color(tag.color).getInverseBWColor()
				),
				shape = RoundedCornerShape(12.dp),
				modifier = Modifier
			) {
				Text(
					text = tag.tag,
					modifier = Modifier.padding(12.dp, 6.dp),
					style = MaterialTheme.typography.bodyLarge,
				)
			}
			Spacer(modifier = Modifier.weight(1f))
			MenuButton(
				icon = R.drawable.ic_color_picker,
				contentDescription = "Color picker",
				tint = MaterialTheme.colorScheme.onBackground.copy(0.71f)
			) {}
			MenuButton(
				icon = R.drawable.ic_close,
				contentDescription = "Remove tag",
				tint = MaterialTheme.colorScheme.onBackground.copy(0.71f)
			) {}
		} // Row
		Spacer(modifier = Modifier.height(4.dp))
	}
}
