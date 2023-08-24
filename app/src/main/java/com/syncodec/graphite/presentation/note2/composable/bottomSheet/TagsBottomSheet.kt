package com.syncodec.graphite.presentation.note2.composable.bottomSheet

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.TagObject
import com.syncodec.graphite.presentation.common.bottomSheet.genericBottomSheet2.GenericBottomSheet2
import com.syncodec.graphite.presentation.common.bottomSheet.genericBottomSheet2.GenericBottomSheetSkeleton2
import com.syncodec.graphite.presentation.common.info.InfoCard
import com.syncodec.graphite.presentation.note2.composable.dialog.AddNewTagDialog
import com.syncodec.graphite.presentation.ui.IconButtonSize


@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun TagsBottomSheet(
	bottomSheetState: SheetState = rememberModalBottomSheetState(),
	isBottomSheetVisible: Boolean = false,
	onDismissRequest: () -> Unit = { },
	allTagList: List<TagObject> = listOf(),
	savedTagList: List<TagObject> = listOf(),
	newTagList: List<TagObject> = listOf(),
	toRemoveTagList: List<TagObject> = listOf(),
) {
	val scope = rememberCoroutineScope()

	var isAddTagDialogVisible by remember { mutableStateOf(false) }

	var searchQuery by remember { mutableStateOf("") }

	val filteredTagList by remember(allTagList, searchQuery) { derivedStateOf { allTagList.filter { searchQuery in it.tag } } }
	val isAddNewTagItemViewVisible by remember(allTagList, searchQuery) { derivedStateOf { searchQuery.isNotEmpty() && allTagList.none { it.tag == searchQuery } } }

	GenericBottomSheet2(
		bottomSheetState = bottomSheetState,
		isBottomSheetVisible = isBottomSheetVisible,
		onDismissRequest = onDismissRequest,
	) {
		GenericBottomSheetSkeleton2(
			title = "Tags",
		) {

			if (savedTagList.isEmpty() or newTagList.isEmpty()) {
				NoConnectedTagsView()
				Spacer(modifier = Modifier.height(12.dp))
			}

			DockedSearchBar(
				query = searchQuery,
				onQueryChange = { searchQuery = it },
				onSearch = {},
				active = true,
				onActiveChange = {},
				placeholder = { Text(text = "Search or add tag") },
				leadingIcon = {
					Icon(
						painter = painterResource(id = R.drawable.ic_fa_tag),
						contentDescription = "Tag",
						modifier = Modifier.requiredSize(IconButtonSize),
					)
				},
				trailingIcon = {
					AnimatedContent(
						targetState = searchQuery.isEmpty(),
						transitionSpec = { scaleIn(tween(470)) + fadeIn(tween(470)) togetherWith scaleOut(tween(470)) + fadeOut(tween(470)) },
						label = "trailingIcon_animation"
					) {
						if (it) {
							Icon(
								painter = painterResource(id = R.drawable.ic_fa_search),
								contentDescription = "Search in all tags",
								modifier = Modifier.requiredSize(IconButtonSize),
							)
						} else {
							Icon(
								painter = painterResource(id = R.drawable.ic_fa_plus),
								contentDescription = "Add a new tag",
								modifier = Modifier.requiredSize(IconButtonSize),
							)
						}
					}
				},
				colors = SearchBarDefaults.colors(
					containerColor = Color(ColorUtils.blendARGB(MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp).toArgb(), MaterialTheme.colorScheme.background.toArgb(), 0.71f))
				),
				modifier = Modifier.fillMaxWidth()
			) {
				Column(
					modifier = Modifier
						.fillMaxWidth()
						.verticalScroll(rememberScrollState())
				) {
					AddNewTagItemView(
						tag = searchQuery,
						isVisible = isAddNewTagItemViewVisible,
					) { isAddTagDialogVisible = true }
					repeat(13) {
						filteredTagList.forEach { tagObject ->
							TagItemView(
								tagObject = tagObject
							)
						}
					}
				}
			}
		}
	}

	AddNewTagDialog(
		isDialogVisible = isAddTagDialogVisible,
	)
}

@Preview
@Composable
private fun NoConnectedTagsView() {
	InfoCard(
		title = "Tag your notes",
		description = "Is it a recipe? Or you just described a beautiful place? Add tags to search and filter through your notes easily",
		icon = R.drawable.ic_fa_tag,
		shape = MaterialTheme.shapes.extraLarge,
	)
}

@Preview
@Composable
private fun AddNewTagItemView(
	tag: String = "npr",
	isVisible: Boolean = true,
	onClick: () -> Unit = {},
) {
	AnimatedVisibility(
		visible = isVisible,
		enter = expandVertically(tween(470)),
		exit = shrinkVertically(tween(470)),
		label = "addNewTagItemView_animation",
	) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier
				.fillMaxWidth()
				.height(64.dp)
				.clickable { onClick() }
				.padding(horizontal = 18.dp)
		) {
			Icon(
				painter = painterResource(id = R.drawable.ic_fa_plus),
				contentDescription = "Add new tag",
				modifier = Modifier.requiredSize(IconButtonSize)
			)
			Spacer(modifier = Modifier.width(24.dp))
			Text(
				text = tag
			)
		}
	}
}

@Preview
@Composable
private fun TagItemView(
	tagObject: TagObject = TagObject(),
	onClick: () -> Unit = {},
) {
	Row(
		verticalAlignment = Alignment.CenterVertically,
		modifier = Modifier
			.fillMaxWidth()
			.height(64.dp)
			.clickable { onClick() }
			.padding(horizontal = 18.dp)
	) {
		Icon(
			painter = painterResource(id = R.drawable.ic_fa_tag),
			contentDescription = "Tag",
			modifier = Modifier.requiredSize(IconButtonSize)
		)
		Spacer(modifier = Modifier.width(24.dp))
		Text(
			text = tagObject.tag
		)
	}
}
