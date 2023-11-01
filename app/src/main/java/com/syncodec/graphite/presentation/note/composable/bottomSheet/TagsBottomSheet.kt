package com.syncodec.graphite.presentation.note.composable.bottomSheet

import android.widget.Toast
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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.local.TagObject
import com.syncodec.graphite.presentation.common.bottomSheet.genericBottomSheet2.GenericBottomSheet2
import com.syncodec.graphite.presentation.common.bottomSheet.genericBottomSheet2.GenericBottomSheetSkeleton2
import com.syncodec.graphite.presentation.common.info.InfoCard
import com.syncodec.graphite.presentation.note.NoteViewModel
import com.syncodec.graphite.presentation.tags.composable.bottomSheet.AddTagBottomSheet
import com.syncodec.graphite.presentation.tags.composable.buildingBlock.TagItemView
import com.syncodec.graphite.presentation.base.ICON_SIZE
import com.syncodec.graphite.utils.dataStore.DataStoreInstance
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun TagsBottomSheet(
	bottomSheetState: SheetState = rememberModalBottomSheetState(),
	isBottomSheetVisible: Boolean = false,
	onDismissRequest: () -> Unit = { },
	allTagList: List<TagObject> = listOf(),
	tagStateMap: Map<TagObject, NoteViewModel.Companion.TagObjectState> = mapOf(),
	onClickTag: (TagObject) -> Unit = {},
	putTag: (String, Color) -> Boolean = { _, _ -> false },
) {
	val context = LocalContext.current

	val scope = rememberCoroutineScope()

	var searchQuery by remember { mutableStateOf("") }

	val filteredTagList by remember(allTagList, searchQuery) { derivedStateOf { allTagList.filter { searchQuery in it.tag } } }
	val isAddNewTagItemViewVisible by remember(allTagList, searchQuery) { derivedStateOf { searchQuery.isNotEmpty() && allTagList.none { it.tag == searchQuery } } }

	val newTagBottomSheetState = rememberModalBottomSheetState()
	var isNewTagBottomSheetVisible by remember { mutableStateOf(false) }

	GenericBottomSheet2(
		bottomSheetState = bottomSheetState,
		isBottomSheetVisible = isBottomSheetVisible,
		onDismissRequest = onDismissRequest,
	) {
		GenericBottomSheetSkeleton2(
			title = stringResource(id = R.string.tags),
		) {

			NoConnectedTagsView()

			Spacer(modifier = Modifier.height(12.dp))

			ConnectedTagView(tagStateMap = tagStateMap, onClickTag = onClickTag)

			Spacer(modifier = Modifier.height(12.dp))

			DockedSearchBar(
				query = searchQuery,
				onQueryChange = { searchQuery = it },
				onSearch = {},
				active = true,
				onActiveChange = {},
				placeholder = { Text(text = stringResource(id = R.string.search_or_add_tag)) },
				leadingIcon = {
					Icon(
						painter = painterResource(id = R.drawable.ic_fa_tag),
						contentDescription = "Tag",
						modifier = Modifier.requiredSize(ICON_SIZE),
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
								modifier = Modifier.requiredSize(ICON_SIZE),
							)
						} else {
							Icon(
								painter = painterResource(id = R.drawable.ic_fa_plus),
								contentDescription = "Add a new tag",
								modifier = Modifier.requiredSize(ICON_SIZE),
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
					) { isNewTagBottomSheetVisible = true }
					filteredTagList.forEach { tagObject ->
						TagItemView(
							tagObject = tagObject,
							onClick = { onClickTag(tagObject) },
						)
					}
				}
			}
		}
	}

	AddTagBottomSheet(
		bottomSheetState = newTagBottomSheetState,
		isBottomSheetVisible = isNewTagBottomSheetVisible,
		onDismissRequest = { scope.launch { newTagBottomSheetState.hide(); isNewTagBottomSheetVisible = false } },
		tagText = searchQuery,
		onChangeTagText = { searchQuery = it.lowercase().split(" ").firstOrNull() ?: "" },
		onCreateTag = { tag, color ->
			if (!putTag(tag, color)) Toast.makeText(context, context.getText(R.string.toast_duplicate_tag), Toast.LENGTH_SHORT).show()
			else scope.launch { newTagBottomSheetState.hide(); isNewTagBottomSheetVisible = false }
		},
	)
}

@Preview
@Composable
private fun NoConnectedTagsView() {
	val context = LocalContext.current
	val dataStoreInstance = remember { DataStoreInstance(context) }
	val showTagInfoCard by dataStoreInstance.showTagInfoCard.collectAsState(initial = false)

	AnimatedVisibility(
		visible = showTagInfoCard,
		enter = expandVertically(tween(470)),
		exit = shrinkVertically(tween(470)),
		label = "showTagInfoCard_visibility"
	) {
		InfoCard(
			title = stringResource(id = R.string.tag_info_title),
			description = stringResource(id = R.string.tag_info_message),
			icon = R.drawable.ic_fa_tag,
			shape = MaterialTheme.shapes.extraLarge,
			buttonText = stringResource(id = R.string.dismiss),
			onClickButton = { dataStoreInstance.putShowTagInfoCard(false) }
		)
	}
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
				modifier = Modifier.requiredSize(ICON_SIZE)
			)
			Spacer(modifier = Modifier.width(24.dp))
			Text(
				text = tag
			)
		}
	}
}

@OptIn(ExperimentalLayoutApi::class)
@Preview
@Composable
private fun ConnectedTagView(
	tagStateMap: Map<TagObject, NoteViewModel.Companion.TagObjectState> = mapOf(),
	onClickTag: (TagObject) -> Unit = {},
) {
	FlowRow(
		modifier = Modifier.fillMaxWidth()
	) {
		tagStateMap.forEach { (tagObject, tagObjectState) ->
			ConnectedTagItem(
				tagObject = tagObject,
				tagObjectState = tagObjectState,
				onClick = { onClickTag(tagObject) }
			)
		}
	}
}

@Preview
@Composable
private fun ConnectedTagItem(
	tagObject: TagObject = TagObject.getRandomInstance(),
	tagObjectState: NoteViewModel.Companion.TagObjectState = NoteViewModel.Companion.TagObjectState.New,
	onClick: () -> Unit = {}
) {
	val containerColor = Color(tagObject.color).copy(alpha = 0.13f)
	val contentColor = Color(tagObject.color)

	SuggestionChip(
		label = {
			Text(
				text = tagObject.tag,
				style = MaterialTheme.typography.bodyMedium,
				fontWeight = FontWeight.Bold,
			)
		},
		icon = when (tagObjectState) {
			NoteViewModel.Companion.TagObjectState.Saved -> null
			NoteViewModel.Companion.TagObjectState.New -> {
				{
					Icon(
						painter = painterResource(id = R.drawable.ic_fa_plus),
						contentDescription = "New tag",
						modifier = Modifier.requiredSize(14.dp)
					)
				}
			}

			NoteViewModel.Companion.TagObjectState.ToRemove -> {
				{
					Icon(
						painter = painterResource(id = R.drawable.ic_fa_x),
						contentDescription = "Remove tag",
						modifier = Modifier.requiredSize(14.dp)
					)
				}
			}
		},
		colors = SuggestionChipDefaults.suggestionChipColors(containerColor = containerColor, labelColor = contentColor, iconContentColor = contentColor),
		border = null,
		modifier = Modifier.padding(horizontal = 4.dp),
		onClick = onClick
	)
}

