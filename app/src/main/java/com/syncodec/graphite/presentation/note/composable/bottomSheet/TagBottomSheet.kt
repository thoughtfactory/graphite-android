package com.syncodec.graphite.presentation.note.composable.bottomSheet

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.TagObject
import com.syncodec.graphite.presentation.common.bottomSheet.GenericBottomSheet
import com.syncodec.graphite.presentation.note.composable.LocalCompositionTagList
import com.syncodec.graphite.presentation.note.composable.LocalCompositionTagListBuffer
import com.syncodec.graphite.presentation.note.composable.LocalOnClickTag
import com.syncodec.graphite.presentation.tags.TagsActivity
import com.syncodec.graphite.utils.getInverseBWColor


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TagBottomSheet() {
	val context = LocalContext.current

	val tagList = LocalCompositionTagList.current
	val tagListBuffer = LocalCompositionTagListBuffer.current

	val onClickTag = LocalOnClickTag.current

	GenericBottomSheet(
		title = "Tags",
		icon = R.drawable.ic_tag
	) {

		ConnectedTagView(
			tagList = tagListBuffer,
		) {
			onClickTag(it)
		}

		LazyColumn(
			modifier = Modifier.fillMaxWidth()
		) {
			tagList
				.sortedBy { tagListBuffer.contains(it) }
				.reversed()
				.forEach {
					item(
						key = it.id.toString()
					) {
						Box(
							modifier = Modifier.animateItemPlacement()
						) {
							TagItem(
								tag = it,
								isSelected = tagListBuffer.contains(it),
							) { onClickTag(it) }
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
	tagList : List<TagObject>,
	onClick : (TagObject) -> Unit
) {
	LazyRow(
		modifier = Modifier.fillMaxWidth()
	) {
		tagList.forEach {
			item(
				key = it.tag.hashCode() + it.color.hashCode()
			) {
				SuggestionChip(
					onClick = { onClick(it) },
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
	tag : TagObject,
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
