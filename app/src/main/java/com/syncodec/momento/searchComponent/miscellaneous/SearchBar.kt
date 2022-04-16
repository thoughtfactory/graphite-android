package com.syncodec.momento.searchComponent.miscellaneous

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.syncodec.momento.R
import com.syncodec.momento.custom.ChipData
import com.syncodec.momento.custom.ChipView
import com.syncodec.momento.searchComponent.SearchViewModel
import compose.icons.TablerIcons
import compose.icons.tablericons.*


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SearchBar() {
	val viewModel: SearchViewModel = viewModel()

	var query by viewModel.activityState.query

	val vaultState by viewModel.activityState.vaultState
	var showArchived by viewModel.activityState.showArchived
	var showFavourite by viewModel.activityState.showFavourite
	var showLocked by viewModel.activityState.showLocked
	var showDiary by viewModel.activityState.showDiary
	var showNotebook by viewModel.activityState.showNotebook
	var showChapter by viewModel.activityState.showChapter
	var showNote by viewModel.activityState.showNote
	var showBucket by viewModel.activityState.showBucket

	val stateFilterChipDataList: MutableList<ChipData> = mutableListOf(
		ChipData(title = "All", icon = R.drawable.ic_write, isSelected = !showArchived && !showFavourite && !showLocked) {
			showArchived = false
			showFavourite = false
			showLocked = false
		},
		ChipData(title = "Archived", icon = R.drawable.ic_archive, isSelected = showArchived) { showArchived = !showArchived },
		ChipData(title = "Favourite", icon = R.drawable.ic_favourite, isSelected = showFavourite) { showFavourite = !showFavourite },
		ChipData(title = "Locked", icon = R.drawable.ic_lock_close, isSelected = showLocked) { showLocked = !showLocked },
	)

	val componentFilterChipDataList: MutableList<ChipData> = mutableListOf(
		ChipData(title = "All", icon = R.drawable.ic_note, isSelected = !showDiary && !showNotebook && !showChapter && !showNote && !showBucket) {
			showDiary = false
			showNotebook = false
			showChapter = false
			showNote = false
			showBucket = false
		},
//		ChipData(title = "Diary", icon = TablerIcons.Signature, isSelected = showDiary) { showDiary = !showDiary },
//		ChipData(title = "Notebook", icon = TablerIcons.Book, isSelected = showNotebook) { showNotebook = !showNotebook },
//		ChipData(title = "Chapter", icon = TablerIcons.Notebook, isSelected = showChapter) { showChapter = !showChapter },
//		ChipData(title = "Note", icon = TablerIcons.Note, isSelected = showNote) { showNote = !showNote },
//		ChipData(title = "Bucket", icon = TablerIcons.Bucket, isSelected = showBucket) { showBucket = !showBucket },
	)

	Column(
		modifier = Modifier
			.fillMaxWidth()
			.background(MaterialTheme.colorScheme.secondaryContainer),
	) {
		Spacer(modifier = Modifier.height(16.dp))

		SearchField(
			query = query
		) {
			query = it
		}

		Spacer(modifier = Modifier.height(12.dp))

		ChipView(chipDataList = stateFilterChipDataList)

		Spacer(modifier = Modifier.height(8.dp))

		ChipView(chipDataList = componentFilterChipDataList)

		Spacer(modifier = Modifier.height(12.dp))
	}
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun SearchField(
	query: String,
	onQueryChange: (String) -> Unit
) {
	val keyboardController = LocalSoftwareKeyboardController.current
	val focusRequester = remember { FocusRequester() }

	Card(
		elevation = 0.dp,
		shape = RoundedCornerShape(12.dp),
		backgroundColor = MaterialTheme.colorScheme.background,
		modifier = Modifier
			.fillMaxWidth()
			.height(56.dp)
			.padding(12.dp, 0.dp)
	) {
		BasicTextField(
			value = query,
			onValueChange = { onQueryChange(it) },
			singleLine = true,
			cursorBrush = SolidColor(MaterialTheme.colorScheme.onBackground),
			textStyle = MaterialTheme.typography.bodyMedium.copy(
				color = MaterialTheme.colorScheme.onBackground,
				fontWeight = FontWeight.Bold
			),
			modifier = Modifier
				.fillMaxWidth()
				.height(48.dp)
				.padding(20.dp, 0.dp)
				.clip(RoundedCornerShape(12.dp))
				.focusRequester(focusRequester),
			decorationBox = { innerTextField ->
				Card(
					modifier = Modifier
						.fillMaxWidth(),
					backgroundColor = Color.Transparent,
					elevation = 0.dp,
					shape = RoundedCornerShape(12.dp),
				) {
					Box(
						contentAlignment = Alignment.CenterStart,
						modifier = Modifier
							.fillMaxWidth()
					) {
						if (query.isEmpty()) {
							Text(
								text = "Search for diary, notes, bucket etc...",
								style = MaterialTheme.typography.bodyMedium,
								color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.71f),
								fontWeight = FontWeight.Bold
							)
						}
						innerTextField()
					}
				}
			}
		)
	}

	LaunchedEffect(key1 = Unit) {
		focusRequester.requestFocus()
		keyboardController?.show()
	}
}
