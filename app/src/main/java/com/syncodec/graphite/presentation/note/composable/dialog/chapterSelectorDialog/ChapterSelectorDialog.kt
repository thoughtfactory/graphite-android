package com.syncodec.graphite.presentation.note.composable.dialog.chapterSelectorDialog

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.ChapterObject
import com.syncodec.graphite.di.model.ChapterObjectLite
import com.syncodec.graphite.presentation.common.button.MenuButton
import com.syncodec.graphite.presentation.main.composable.buildingBlock.notebook.NotebookCard
import com.syncodec.graphite.utils.decodeBase64ToBitmap
import com.syncodec.graphite.utils.getInverseBWColor
import io.realm.kotlin.types.ObjectId


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChapterSelectorDialog(
	showDialog: Boolean,
	currentParentChapter: ChapterObject,
	newParentChapter: ChapterObject,
	allChapterList: List<ChapterObject>,
	onClickChapter: (ObjectId) -> Unit,
	onSelectChapter: (ObjectId) -> Unit,
	onDismiss: () -> Unit,
) {

	var showNotebook by remember { mutableStateOf(false) }

	AnimatedVisibility(
		visible = showDialog,
		enter = fadeIn(tween(300)),
		exit = fadeOut(tween(300)),
		modifier = Modifier.fillMaxSize()
	) {
		Scaffold(
			topBar = {
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
							text = "Move into",
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
			},
			floatingActionButtonPosition = FabPosition.End,
			floatingActionButton = {
				FloatingActionButton(
					onClick = { onSelectChapter(newParentChapter.id) },
				) {
					Icon(
						painter = painterResource(id = R.drawable.ic_check),
						contentDescription = "Select chapter"
					)
				}
			},
			modifier = Modifier
				.fillMaxSize()
				.clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {}
		) {
			Column(
				modifier = Modifier
					.fillMaxSize()
					.padding(it)
			) {
				Navigator(
					chapterObjectLiteList = newParentChapter.getPath(),
					rootChapterId = currentParentChapter.id,
					color = currentParentChapter.color?.let { it1 -> Color(it1) },
					thumbnail = currentParentChapter.thumbnail,
					showNotebook = { showNotebook = true },
				) {
					onClickChapter(it)
					showNotebook = false
				}

				Crossfade(targetState = showNotebook) {
					if (it) {
						NotebookView(
							allChapterList = allChapterList.filter { it.parentChapterId == null },
						) {
							onClickChapter(it)
							showNotebook = false
						}
					} else {
						ChapterView(
							parentChapter = newParentChapter,
							modifier = Modifier
								.fillMaxWidth()
								.weight(1f),
							onClickChapter = onClickChapter
						)
					}
				}
			}
		}
	}
}


@Composable
fun Navigator(
	chapterObjectLiteList: List<ChapterObjectLite>,
	rootChapterId: ObjectId?,
	color: Color?,
	thumbnail: String?,
	showNotebook: () -> Unit,
	onClickChapter: (ObjectId) -> Unit
) {
	Box(
		modifier = Modifier.fillMaxWidth()
	) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier.horizontalScroll(rememberScrollState())
		) {
			Spacer(modifier = Modifier.width(8.dp))

			NavigatorItem(
				title = "•",
				color = MaterialTheme.colorScheme.surface,
			) { showNotebook() }

			NavigatorItem(
				title = "/",
				color = color
			) { if (rootChapterId != null) onClickChapter(rootChapterId) }

			chapterObjectLiteList.forEach {
				NavigatorItem(
					title = it.title,
					color = it.color?.let { it1 -> Color(it1) }
				) { onClickChapter(it.id) }
			}

			Spacer(modifier = Modifier.width(8.dp))
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NavigatorItem(
	title: String,
	color: Color?,
	onClick: () -> Unit
) {
	var isVisible by remember { mutableStateOf(false) }

	LaunchedEffect(key1 = isVisible) {
		isVisible = true
	}

	AnimatedVisibility(
		visible = isVisible,
		enter = fadeIn(tween(300)),
		exit = fadeOut(tween(300))
	) {
		Row(
			modifier = Modifier,
			verticalAlignment = Alignment.CenterVertically
		) {
			SuggestionChip(
				onClick = onClick,
				label = {
					Text(
						text = title,
						fontWeight = FontWeight.Bold
					)
				},
				colors = SuggestionChipDefaults.suggestionChipColors(
					containerColor = color ?: MaterialTheme.colorScheme.surface,
					labelColor = color?.getInverseBWColor() ?: MaterialTheme.colorScheme.onSurface,
				),
				border = SuggestionChipDefaults.suggestionChipBorder(borderColor = color ?: MaterialTheme.colorScheme.surface),
			)

			Spacer(modifier = Modifier.width(4.dp))

			Icon(
				painter = painterResource(id = R.drawable.ic_chevron_right),
				contentDescription = null,
				tint = MaterialTheme.colorScheme.onBackground
			)

			Spacer(modifier = Modifier.width(4.dp))
		}
	}
}

@Composable
private fun NotebookView(
	allChapterList: List<ChapterObject>,
	onClick: (ObjectId) -> Unit
) {
	LazyVerticalGrid(
		columns = GridCells.Adaptive(144.dp),
		horizontalArrangement = Arrangement.Center,
		modifier = Modifier
			.fillMaxSize()
			.padding(12.dp, 0.dp),
	) {
		allChapterList.forEach { notebook ->
			item {
				NotebookCard(
					title = notebook.title,
					color = notebook.color?.let { Color(it) },
					thumbnail = notebook.thumbnail,
					isSelected = false,
					onClick = { onClick(notebook.id) },
					onLongClick = { }
				)
			}
		}
		item { Spacer(modifier = Modifier.height(96.dp)) }
		item { Spacer(modifier = Modifier.height(96.dp)) }
	}
}

@Composable
private fun ChapterView(
	modifier: Modifier,
	parentChapter: ChapterObject,
	onClickChapter: (ObjectId) -> Unit
) {
	Crossfade(
		targetState = parentChapter,
		animationSpec = tween(300)
	) {
		LazyColumn(
			modifier = modifier
		) {
			item { Spacer(modifier = Modifier.height(4.dp)) }
			it.chapterList.forEach { chapterObject ->
				item {
					ChapterListCard(
						id = chapterObject.id,
						timestamp = chapterObject.createdTimestamp,
						isLocked = chapterObject.isLocked,
						isSelected = false,
						isFavourite = chapterObject.isFavourite,
						isDeleted = false,
						isLast = false,
						title = chapterObject.title,
						description = chapterObject.description,
						color = chapterObject.color?.let { it1 -> Color(it1) },
						thumbnail = chapterObject.thumbnail?.decodeBase64ToBitmap(),
						noteCount = chapterObject.noteList.size,
						chapterCount = chapterObject.chapterList.size,
						isVisible = true,
						selectedColor = MaterialTheme.colorScheme.surface,
						onClick = { onClickChapter(chapterObject.id) }
					)
				}
			}
		}
	}
}
