package com.syncodec.graphite.noteComponent.modalBottomSheet

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ChipDefaults
import androidx.compose.material.FilterChip
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.accompanist.flowlayout.FlowRow
import com.syncodec.graphite.R
import com.syncodec.graphite.custom.LargeTextField
import com.syncodec.graphite.custom.bottomSheet.BottomSheetHeader
import com.syncodec.graphite.custom.bottomSheet.BottomSheetStrip
import com.syncodec.graphite.database.tag.TagDbEntry
import com.syncodec.graphite.miscellaneous.ThemeUtils.Companion.tone
import com.syncodec.graphite.noteComponent.NoteActivity


@Composable
fun TagBottomSheet(
	tagList: List<TagDbEntry>,
	connectedTag: List<String>,
	onAction: (NoteActivity.Action, Any) -> Unit
) {
	Surface(
		shape = RoundedCornerShape(12.dp, 12.dp, 0.dp, 0.dp),
		color = MaterialTheme.colorScheme.surface,
		modifier = Modifier.heightIn(360.dp),
	) {
		Column(
			modifier = Modifier,
			horizontalAlignment = Alignment.CenterHorizontally
		) {

			BottomSheetStrip()

			BottomSheetHeader(title = "Tag", icon = R.drawable.ic_hashtag)

			Spacer(modifier = Modifier.height(8.dp))

			SearchBar { click, data -> onAction(click, data) }

			AnimatedVisibility(
				visible = connectedTag.isNotEmpty()
			) {
				Spacer(modifier = Modifier.height(6.dp))
				ConnectedTagView(connectedTag = connectedTag) { click, tag -> onAction(click, tag) }
			}

			LazyColumn(modifier = Modifier.fillMaxWidth()) {
				tagList.forEach {
					item { Tag(tag = it.tag) { action, tag -> onAction(action, tag) } }
					item {
						Box(
							modifier = Modifier
								.fillMaxWidth()
								.padding(24.dp,0.dp)
								.height(1.dp)
								.background(MaterialTheme.colorScheme.onSurface.copy(0.71f)),
						)
					}
				}

				item { Spacer(modifier = Modifier.height(128.dp)) }
			}

			Spacer(modifier = Modifier.height(32.dp))
		}
	}
}

@Composable
private fun SearchBar(
	onClick: (NoteActivity.Action, Any) -> Unit
) {
	var tag by remember { mutableStateOf("") }
	var isFocused by remember { mutableStateOf(false) }

	Row(
		modifier = Modifier.fillMaxWidth(),
		horizontalArrangement = Arrangement.SpaceBetween
	) {
		LargeTextField(
			modifier = Modifier
				.weight(1f)
				.padding(24.dp, 0.dp, 8.dp, 0.dp),
			text = tag,
			placeholder = "Add new tag"
		) { tag = it }

		Row(modifier = Modifier) {
			IconButton(
				onClick = {
					onClick(NoteActivity.Action.ADD_TAG, tag)
					tag = ""
				}
			) {
				Icon(
					painter = painterResource(id = R.drawable.ic_add),
					contentDescription = "Add tag",
					tint = MaterialTheme.colorScheme.onSurface.tone(isSystemInDarkTheme(), 2),
					modifier = Modifier
						.requiredSize(32.dp)
						.padding(4.dp)
				)
			}
			Spacer(modifier = Modifier.width(12.dp))
		}
	}
}

@Composable
private fun ConnectedTagView(
	connectedTag: List<String>,
	onClick: (NoteActivity.Action, String) -> Unit
) {
	FlowRow(
		modifier = Modifier
			.fillMaxWidth()
			.padding(12.dp, 0.dp),
		mainAxisSpacing = 6.dp,
		crossAxisSpacing = 0.dp
	) { connectedTag.forEach { ConnectedTag(it) { click, tag -> onClick(click, tag) } } }
}

@OptIn(androidx.compose.material.ExperimentalMaterialApi::class)
@Composable
private fun ConnectedTag(
	tag: String,
	onClick: (NoteActivity.Action, String) -> Unit
) {
	FilterChip(
		selected = true,
		onClick = { onClick(NoteActivity.Action.CONNECT_TAG, tag) },
		colors = ChipDefaults.filterChipColors(
			backgroundColor = MaterialTheme.colorScheme.primary
		)
	) {
		Text(
			text = tag,
			style = MaterialTheme.typography.bodyMedium,
			fontWeight = FontWeight.Bold,
			color = MaterialTheme.colorScheme.onPrimary
		)
	}
}

@Composable
private fun Tag(
	tag: String,
	onClick: (NoteActivity.Action, String) -> Unit
) {
	Row(
		verticalAlignment = Alignment.CenterVertically,
		modifier = Modifier
			.fillMaxWidth()
			.height(54.dp)
			.clickable { onClick(NoteActivity.Action.CONNECT_TAG, tag) },
	) {
		Spacer(modifier = Modifier.width(32.dp))

		Icon(
			painter = painterResource(id = R.drawable.ic_hashtag),
			contentDescription = null,
			tint = MaterialTheme.colorScheme.onBackground,
			modifier = Modifier.requiredSize(20.dp)
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
