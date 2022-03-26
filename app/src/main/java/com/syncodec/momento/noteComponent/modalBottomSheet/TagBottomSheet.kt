package com.syncodec.momento.noteComponent.modalBottomSheet

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.flowlayout.FlowRow
import com.syncodec.momento.R
import com.syncodec.momento.custom.BottomSheetHeader
import com.syncodec.momento.custom.BottomSheetStrip
import com.syncodec.momento.custom.LargeTextField
import com.syncodec.momento.database.tag.TagDbEntry
import com.syncodec.momento.noteComponent.NoteActivity
import compose.icons.TablerIcons
import compose.icons.tablericons.Plus


@Composable
fun TagBottomSheet(
	tagList: List<TagDbEntry>,
	connectedTag: List<String>,
	onClick: (NoteActivity.Click, Any) -> Unit
) {
	Column(
		modifier = Modifier
			.fillMaxWidth()
			.heightIn(360.dp)
			.background(MaterialTheme.colorScheme.background),
		horizontalAlignment = Alignment.CenterHorizontally
	) {

		BottomSheetStrip()

		BottomSheetHeader(
			title = "Tag",
			painter = painterResource(id = R.drawable.ic_hashtag)
		)

		Spacer(modifier = Modifier.height(8.dp))

		SearchBar { click, data -> onClick(click, data) }

		Spacer(modifier = Modifier.height(12.dp))

		ConnectedTagCard(connectedTag = connectedTag) { click, tag -> onClick(click, tag) }

		Spacer(modifier = Modifier.height(12.dp))

		LazyColumn(
			modifier = Modifier.fillMaxWidth(),
		) {

			item {
				Box(
					modifier = Modifier
						.fillMaxWidth()
						.padding(8.dp, 0.dp)
						.height(1.dp)
						.background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.31f)),
				)
			}
			tagList.forEach {
				item { Tag(tag = it.tag) { click, tag -> onClick(click, tag) } }
				item {
					Box(
						modifier = Modifier
							.fillMaxWidth()
							.padding(8.dp, 0.dp)
							.height(1.dp)
							.background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.31f)),
					)
				}
			}

			item { Spacer(modifier = Modifier.height(128.dp)) }
		}

		Spacer(modifier = Modifier.height(32.dp))
	}
}

@Composable
private fun SearchBar(
	onClick: (NoteActivity.Click, Any) -> Unit
) {
	var tag by remember { mutableStateOf("") }
	var isFocused by remember { mutableStateOf(false) }

	Row(
		modifier = Modifier.fillMaxWidth(),
		horizontalArrangement = Arrangement.SpaceBetween
	) {
		LargeTextField(
			text = tag,
			placeholder = "Search or add new tag",
			isFocused = isFocused,
			onFocusChanged = { isFocused = it },
			modifier = Modifier
				.weight(1f)
				.padding(24.dp, 0.dp, 8.dp, 0.dp)
		) { tag = it }

		Row(modifier = Modifier) {
			IconButton(onClick = { onClick(NoteActivity.Click.ADD_TAG, tag) }) {
				Icon(
					imageVector = TablerIcons.Plus,
					contentDescription = "Add tag",
					tint = MaterialTheme.colorScheme.onBackground,
					modifier = Modifier.requiredSize(20.dp)
				)
			}
			Spacer(modifier = Modifier.width(12.dp))
		}
	}
}

@Composable
private fun ConnectedTagCard(
	connectedTag: List<String>,
	onClick: (NoteActivity.Click, String) -> Unit
) {
	FlowRow(
		modifier = Modifier
			.fillMaxWidth()
			.padding(12.dp, 0.dp),
		mainAxisSpacing = 6.dp,
		crossAxisSpacing = 0.dp
	) { connectedTag.forEach { ConnectedTag(it) { click, tag -> onClick(click, tag) } } }
}

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.material.ExperimentalMaterialApi::class)
@Composable
private fun ConnectedTag(
	tag: String,
	onClick: (NoteActivity.Click, String) -> Unit
) {
	Card(
		modifier = Modifier,
		elevation = 0.dp,
		backgroundColor = Color.Companion.Transparent,
		shape = RoundedCornerShape(50),
		border = BorderStroke(1.dp, MaterialTheme.colorScheme.primaryContainer),
		onClick = { onClick(NoteActivity.Click.CONNECT_TAG, tag) }
	) {
		Text(
			text = tag,
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onBackground,
			modifier = Modifier.padding(16.dp, 8.dp),
		)
	}
}

@Composable
private fun Tag(
	tag: String,
	onClick: (NoteActivity.Click, String) -> Unit
) {
	Row(
		verticalAlignment = Alignment.CenterVertically,
		modifier = Modifier
			.fillMaxWidth()
			.height(54.dp)
			.clickable { onClick(NoteActivity.Click.CONNECT_TAG, tag) },
	) {
		Spacer(modifier = Modifier.width(32.dp))

		Icon(
			painter = painterResource(id = R.drawable.ic_hashtag),
			contentDescription = null,
			tint = MaterialTheme.colorScheme.onBackground,
			modifier = Modifier.requiredSize(16.dp)
		)

		Spacer(modifier = Modifier.width(12.dp))

		Text(
			text = tag,
			modifier = Modifier,
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onBackground,
		)
	}
}
