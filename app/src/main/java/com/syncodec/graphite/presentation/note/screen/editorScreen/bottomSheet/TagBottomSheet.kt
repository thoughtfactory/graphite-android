package com.syncodec.graphite.presentation.note.screen.editorScreen.bottomSheet

import android.content.Intent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.TagObjectLite
import com.syncodec.graphite.presentation.common.bottomSheet.GenericBottomSheet
import com.syncodec.graphite.presentation.tags.TagsActivity
import com.syncodec.graphite.utils.getInverseBWColor
import io.github.esentsov.PackagePrivate


@OptIn(ExperimentalFoundationApi::class)
@PackagePrivate
@Preview
@Composable
fun TagBottomSheet(
	tagList : List<TagObjectLite> = listOf(),
	tagListSaved : List<TagObjectLite> = listOf(),
	tagListToAdd : List<TagObjectLite> = listOf(),
	tagListToRemove : List<TagObjectLite> = listOf(),
	onAddTagToBuffer : (TagObjectLite) -> Unit = {},
	onRemoveBufferedTag : (TagObjectLite) -> Unit = {},
	onRemoveSavedTag : (TagObjectLite) -> Unit = {},
) {
	val context = LocalContext.current

	GenericBottomSheet(
		title = "Tags",
		icon = R.drawable.ic_tag
	) {

		ConnectedTagView(
			tagListSaved = tagListSaved,
			tagListToAdd = tagListToAdd,
			tagListToRemove = tagListToRemove,
			onRemoveBufferedTag = onRemoveBufferedTag,
			onRemoveSavedTag = onRemoveSavedTag,
		)

		LazyColumn(
			modifier = Modifier.fillMaxWidth()
		) {
			tagList
				.sortedBy { tagListToAdd.contains(it).not() }
				.forEach {
				item(key = it.id.toString()) {
					Box(
						modifier = Modifier.animateItemPlacement()
					) {
						TagItem(
							tag = it,
							isSelected = false,
						) {
							when {
								tagListSaved.contains(it) -> onRemoveSavedTag(it)
								tagListToAdd.contains(it) -> onRemoveBufferedTag(it)
								else -> onAddTagToBuffer(it)
							}
						}
					}
				}
			}

		}

		Spacer(modifier = Modifier.height(4.dp))

		Button(
			onClick = {
				Intent(context, TagsActivity::class.java).apply {
					context.startActivity(this)
				}
			},
			modifier = Modifier.fillMaxWidth()
		) {
			Text(text = "Manage Tags")
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun ConnectedTagView(
	tagListSaved : List<TagObjectLite> = listOf(),
	tagListToAdd : List<TagObjectLite> = listOf(),
	tagListToRemove : List<TagObjectLite> = listOf(),
	onRemoveBufferedTag : (TagObjectLite) -> Unit = {},
	onRemoveSavedTag : (TagObjectLite) -> Unit = {},
) {
	LazyRow(
		modifier = Modifier.fillMaxWidth()
	) {
		tagListSaved.forEach {
			item(key = it.tag.hashCode() + it.color.hashCode() + 1) {
				SuggestionChip(
					onClick = { onRemoveSavedTag(it) },
					label = {
						Text(
							text = it.tag,
							fontWeight = FontWeight.Bold
						)
					},
					colors = SuggestionChipDefaults.suggestionChipColors(
						containerColor = if (it in tagListToRemove) Color(it.color).copy(alpha = 0.47f) else Color(it.color),
						labelColor = Color(it.color).getInverseBWColor(),
					),
					border = null,
					modifier = Modifier
						.padding(4.dp, 0.dp)
						.animateItemPlacement()
				)
			}
		}
		tagListToAdd.forEach {
			item(key = it.tag.hashCode() + it.color.hashCode()) {
				SuggestionChip(
					onClick = { onRemoveBufferedTag(it) },
					icon = {
						Box(
							modifier = Modifier
								.requiredSize(4.dp)
								.background(Color(it.color).getInverseBWColor(), CircleShape)
						)
					},
					label = {
						Text(
							text = it.tag,
							fontWeight = FontWeight.Bold
						)
					},
					colors = SuggestionChipDefaults.suggestionChipColors(
						containerColor = Color(it.color),
						labelColor = Color(it.color).getInverseBWColor(),
					),
					border = null,
					modifier = Modifier
						.padding(4.dp, 0.dp)
						.animateItemPlacement()
				)
			}
		}
	}
}

@Composable
private fun TagItem(
	tag : TagObjectLite,
	isSelected : Boolean,
	onClick : () -> Unit,
) {
	Box(
		modifier = Modifier
			.fillMaxWidth()
			.height(48.dp)
			.padding(0.dp, 2.dp)
			.background(
				if (isSelected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surface.copy(alpha = 0.31f),
				MaterialTheme.shapes.medium
			)
			.clip(MaterialTheme.shapes.medium)
			.clickable { onClick() },
		contentAlignment = Alignment.CenterStart
	) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier
				.fillMaxWidth()
				.padding(4.dp)
		) {
			Spacer(modifier = Modifier.width(12.dp))

			Text(
				text = tag.tag,
				style = MaterialTheme.typography.bodyMedium,
				fontWeight = FontWeight.Bold,
				color = MaterialTheme.colorScheme.onSurface
			)

			Spacer(modifier = Modifier.weight(1f))

			Spacer(
				modifier = Modifier
					.width(80.dp)
					.height(12.dp)
					.background(Color(tag.color), RoundedCornerShape(25))
			)

			Spacer(modifier = Modifier.width(12.dp))
		}
	}
}
