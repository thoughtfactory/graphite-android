package com.syncodec.graphite.presentation.note.composable.dialog

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.accompanist.flowlayout.FlowCrossAxisAlignment
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.flowlayout.MainAxisAlignment
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.TagObject
import com.syncodec.graphite.presentation.common.button.MenuButton
import com.syncodec.graphite.presentation.common.dialog.ColorPickerDialog
import com.syncodec.graphite.presentation.common.dialog.buildingBlock.DialogTextField
import com.syncodec.graphite.utils.getInverseBWColor
import com.syncodec.graphite.utils.getRandomColor
import io.realm.kotlin.types.ObjectId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@OptIn(
	ExperimentalAnimationApi::class,
	ExperimentalFoundationApi::class
)
@Composable
fun TagDialog(
	showDialog : Boolean,
	noteId : ObjectId?,
	allTagList : List<TagObject>,
	onAddTag : (String, Color) -> Unit,
	onClickTag : (ObjectId) -> Unit,
	onDismiss : () -> Unit
) {
	val scope = rememberCoroutineScope()

	var tag by remember { mutableStateOf("") }

	var doesTagExist by remember { mutableStateOf(true) }
	var showColorPickerDialog by remember { mutableStateOf(false) }

	val listState = rememberLazyListState()

	LaunchedEffect(key1 = tag) {
		scope.launch(Dispatchers.IO) {
			doesTagExist = if (tag.isNotBlank()) allTagList.any { it.tag == tag } else true
		}
	}

	Box(
		modifier = Modifier.fillMaxSize()
	) {
		ColorPickerDialog(
			color = getRandomColor(),
			showDialog = showColorPickerDialog,
			onSelectColor = {
				onAddTag(tag, it)
				tag = ""
				showColorPickerDialog = false
			},
			onDismiss = { showColorPickerDialog = false }
		)

		AnimatedVisibility(
			visible = showDialog,
			enter = fadeIn(tween(300)),
			exit = fadeOut(tween(300)),
			modifier = Modifier.fillMaxSize()
		) {
			Box(
				modifier = Modifier
					.fillMaxSize()
					.background(MaterialTheme.colorScheme.background)
					.clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {}
			) {
				Column(
					modifier = Modifier.fillMaxSize()
				) {
					TagTopAppBar(onDismiss = onDismiss)

					Spacer(modifier = Modifier.height(16.dp))

					DialogTextField(
						text = tag,
						label = "Tag",
						placeholder = "Add or Search tag",
						leadingIcon = {
							MenuButton(
								icon = R.drawable.ic_hashtag,
								isEnabled = false,
								onClick = { }
							)
						},
						trailingIcon = {
							AnimatedVisibility(
								visible = ! doesTagExist,
								enter = fadeIn(tween(300)) + scaleIn(tween(300)),
								exit = fadeOut(tween(300)) + scaleOut(tween(300)),
							) {
								MenuButton(
									icon = R.drawable.ic_add,
									onClick = { showColorPickerDialog = true }
								)
							}
						},
						onKeyboardAction = {
//				            titleFocusRequester.freeFocus()
//	        			    descriptionFocusRequester.captureFocus()
						},
						containerColor = MaterialTheme.colorScheme.surface,
						modifier = Modifier
							.fillMaxWidth()
							.padding(16.dp, 0.dp),
					) { tag = (it ?: "").replace(" ", "") }

					Spacer(modifier = Modifier.height(16.dp))

					FlowRow(
						mainAxisSpacing = 4.dp,
						mainAxisAlignment = MainAxisAlignment.SpaceBetween,
						crossAxisAlignment = FlowCrossAxisAlignment.Center,
						lastLineMainAxisAlignment = MainAxisAlignment.Start,
						modifier = Modifier
							.fillMaxWidth()
							.padding(16.dp, 0.dp)
					) {
						allTagList.filter { it.objectIdList.contains(noteId) }.forEach {
							ConnectedTagItem(tag = it) { onClickTag(it.id) }
						}
					}

					Spacer(modifier = Modifier.height(16.dp))

					Crossfade(targetState = allTagList) {
						if (it.isEmpty()) {
							EmptyView()
						} else {
							LazyColumn(
								modifier = Modifier.fillMaxSize(),
								state = listState
							) {
								it.sortedByDescending {
									if (tag.isBlank()) it.objectIdList.contains(noteId) else it.tag.contains(tag)
								}.forEach {
									item(key = it.id.toString()) {
										TagItem(
											tag = it,
											isConnected = it.objectIdList.contains(noteId),
											modifier = Modifier.animateItemPlacement()
										) { onClickTag(it.id) }
									}
								}
								item { Spacer(modifier = Modifier.height(128.dp)) }
							}
						}
					}
				}
			}
		}
	}
}

@Composable
private fun EmptyView() {
	val configuration = LocalConfiguration.current
	val screenWidth = configuration.screenWidthDp.dp

	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.Center,
		modifier = Modifier
			.fillMaxSize()
			.background(MaterialTheme.colorScheme.background)
	) {
		Image(
			painter = painterResource(id = R.drawable.il_no_tag),
			contentDescription = "No tags found",
			modifier = Modifier.size(screenWidth * 3 / 4)
		)
		Spacer(modifier = Modifier.height(16.dp))
		Text(
			text = "Uh oh... No tags found.\nTry adding some new.",
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onBackground,
			fontWeight = FontWeight.Bold
		)
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TagTopAppBar(
	onDismiss : () -> Unit
) {
	TopAppBar(
		modifier = Modifier,
		navigationIcon = {
			MenuButton(
				icon = R.drawable.ic_close,
				tint = MaterialTheme.colorScheme.onBackground,
				onClick = onDismiss
			)
		},
		title = {
			Text(
				text = "Manage Tags",
				color = MaterialTheme.colorScheme.onBackground,
				fontWeight = FontWeight.Bold
			)
		},
		colors = TopAppBarDefaults.smallTopAppBarColors(
			containerColor = MaterialTheme.colorScheme.background,
			navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
			titleContentColor = MaterialTheme.colorScheme.onBackground,
		),
	)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConnectedTagItem(
	tag : TagObject,
	onClick : () -> Unit
) {
	SuggestionChip(
		onClick = onClick,
		label = {
			Text(
				text = tag.tag,
				fontWeight = FontWeight.Bold
			)
		},
		colors = SuggestionChipDefaults.suggestionChipColors(
			containerColor = Color(tag.color).copy(alpha = 0.47f),
			labelColor = Color(tag.color).getInverseBWColor()
		)
	)
}

@Composable
private fun TagItem(
	modifier : Modifier = Modifier,
	tag : TagObject,
	isConnected : Boolean,
	onClick : () -> Unit
) {
	Box(
		modifier = modifier
			.fillMaxWidth()
			.padding(16.dp, 4.dp)
			.background(
				if (isConnected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surface.copy(alpha = 0.31f),
				RoundedCornerShape(12.dp)
			)
			.clip(RoundedCornerShape(12.dp))
			.clickable { onClick() }
	) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier
				.fillMaxWidth()
				.padding(12.dp)
		) {
			Text(
				text = tag.tag,
				color = MaterialTheme.colorScheme.onSurface,
				fontWeight = FontWeight.Bold
			)

			Spacer(modifier = Modifier.weight(1f))

			Box(
				modifier = Modifier
					.width(64.dp)
					.height(8.dp)
					.background(Color(tag.color), RoundedCornerShape(50))
			)
		}
	}
}
