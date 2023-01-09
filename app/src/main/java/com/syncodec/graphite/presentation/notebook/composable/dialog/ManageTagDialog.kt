package com.syncodec.graphite.presentation.notebook.composable.dialog

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.ChapterObject
import com.syncodec.graphite.di.model.TagObject
import com.syncodec.graphite.di.model.TagObjectLite
import com.syncodec.graphite.presentation.common.button.MenuButton
import com.syncodec.graphite.presentation.common.dialog.buildingBlock.DialogTextField


@Composable
fun ManageTagDialog(
	chapterObject: ChapterObject,
	tagList: List<TagObject>,
	showDialog: Boolean,
	onClick: (TagObject) -> Unit,
	onDismiss: () -> Unit
) {
	AnimatedVisibility(
		visible = showDialog,
		enter = fadeIn(animationSpec = tween(300)),
		exit = fadeOut(animationSpec = tween(300))
	) {
		Column(
			modifier = Modifier
				.fillMaxSize()
				.background(MaterialTheme.colorScheme.background)
		) {
//			TagSearchBar(
//				tagColor = getRandomColor(),
//				onAddTag = { _, _ -> }
//			)

			Button(
				onClick = { /*TODO*/ },
				modifier = Modifier.fillMaxWidth().padding(12.dp, 0.dp),
				colors = ButtonDefaults.buttonColors(
					containerColor = MaterialTheme.colorScheme.primary,
					contentColor = MaterialTheme.colorScheme.onPrimary
				)
			) {
				Text(
					text = "Manage Tags",
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.onPrimary,
					fontWeight = FontWeight.Bold
				)
			}

			Spacer(modifier = Modifier.height(4.dp))

			TagListView(
				allTag = tagList,
				selectedTag = tagList.filter { it.objectIdList.contains(chapterObject.id) }.map { it.toLite() },
				onClick = onClick
			)
		}
	}
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TagListView(
	allTag: List<TagObject>,
	selectedTag: List<TagObjectLite>,
	onClick: (TagObject) -> Unit
) {
	LazyColumn(
		modifier = Modifier.fillMaxWidth()
	) {
		allTag.sortedBy { it.tag in selectedTag.map { it.tag } }.reversed().forEach { tag ->
			item(key = tag.tag) {
				TagItemView(
					tag = tag,
					isSelected = selectedTag.any { it.id == tag.id },
					modifier = Modifier.animateItemPlacement(tween(300)),
					onClick = { onClick(tag) }
				)
			}
			item { Spacer(modifier = Modifier.height(8.dp)) }
		}
	}
}

@Composable
private fun TagItemView(
	modifier: Modifier = Modifier,
	tag: TagObject,
	isSelected: Boolean,
	onClick: () -> Unit
) {
	Box(
		modifier = modifier
			.fillMaxWidth()
			.padding(16.dp, 0.dp)
			.background(MaterialTheme.colorScheme.surface.copy(alpha = 0.71f), RoundedCornerShape(16.dp))
			.clip(RoundedCornerShape(16.dp))
			.clickable { onClick() }
	) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier
				.fillMaxWidth()
				.padding(0.dp, 12.dp)
		) {
			Spacer(modifier = Modifier.width(16.dp))

			Icon(
				painter = painterResource(id = R.drawable.ic_tag),
				contentDescription = tag.tag,
				tint = MaterialTheme.colorScheme.onSurface,
				modifier = Modifier.size(24.dp)
			)

			Spacer(modifier = Modifier.width(8.dp))

			Text(
				text = tag.tag,
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onSurface,
				fontWeight = FontWeight.Bold
			)

			Spacer(modifier = Modifier.weight(1f))

			AnimatedVisibility(
				visible = isSelected,
				enter = fadeIn(animationSpec = tween(300)),
				exit = fadeOut(animationSpec = tween(300))
			) {
				Box(
					modifier = Modifier
						.size(8.dp)
						.background(MaterialTheme.colorScheme.onBackground, CircleShape)
				)
			}

			Spacer(modifier = Modifier.width(8.dp))

			Box(
				modifier = Modifier
					.width(80.dp)
					.height(12.dp)
					.background(Color(tag.color), RoundedCornerShape(50))
			)

			Spacer(modifier = Modifier.width(16.dp))
		}
	}
}

@Composable
private fun TagSearchBar(
	tagColor: Color,
	onAddTag: (String, Color) -> Unit
) {

	var searchQuery by remember { mutableStateOf("") }

	DialogTextField(
		value = searchQuery,
		label = "Search or Add tag",
		placeholder = "Connect em' all",
		maxLines = 1,
		leadingIcon = {
			MenuButton(
				icon = R.drawable.ic_tag,
				tint = tagColor
			) {}
		},
		trailingIcon = {
			Row(
				modifier = Modifier
			) {
				MenuButton(
					icon = R.drawable.ic_close,
					tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.71f),
				) { searchQuery = "" }
				MenuButton(
					icon = R.drawable.ic_add,
					tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.71f),
				) { searchQuery = "" }
			}
		},
		containerColor = MaterialTheme.colorScheme.surface,
		contentColor = MaterialTheme.colorScheme.onSurface,
	) { searchQuery = (it ?: searchQuery).replace(" ", "").lowercase() }
}
