package com.syncodec.graphite.presentation.note.composable.dialog.chapterSelectorDialog

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.ChapterObject
import com.syncodec.graphite.di.model.ChapterObjectLite
import com.syncodec.graphite.presentation.custom.button.MenuButton
import com.syncodec.graphite.utils.getInverseBWColor
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChapterSelectorDialog(
	showDialog: Boolean,
	parentChapter: ChapterObject,
	chapterList: List<ChapterObject>,
	onClickChapter: (ChapterObject) -> Unit,
	onSelectChapter: (ChapterObject) -> Unit,
	onDismiss: () -> Unit,
) {
	AnimatedVisibility(
		visible = showDialog,
		enter = fadeIn(tween(300)),
		exit = fadeOut(tween(300)),
		modifier = Modifier.fillMaxSize()
	) {
		Scaffold(
			modifier = Modifier.fillMaxSize(),
			topBar = {
				SmallTopAppBar(
					modifier = Modifier,
					title = {
						Text(text = "Move into")
					},
					navigationIcon = {
						MenuButton(
							icon = R.drawable.ic_close,
							tint = MaterialTheme.colorScheme.onBackground,
							onClick = onDismiss
						)
					}
				)
			},
			floatingActionButtonPosition = FabPosition.End,
			floatingActionButton = {
				FloatingActionButton(
					onClick = { onSelectChapter(parentChapter) },
				) {
					Icon(
						painter = painterResource(id = R.drawable.ic_check),
						contentDescription = "Select chapter"
					)
				}
			}
		) {
			Column(
				modifier = Modifier
					.fillMaxSize()
					.padding(it)

			) {
				NavigationBar(
					parentChapter = parentChapter,
				)
				ChapterList(
					parentChapter = parentChapter,
					modifier = Modifier
						.fillMaxWidth()
						.weight(1f),
					onClickChapter = onClickChapter
				)
			}
		}
	}
}

@Composable
private fun NavigationBar(parentChapter: ChapterObject) {
	val scope = rememberCoroutineScope()
	val path: SnapshotStateList<ChapterObjectLite> = SnapshotStateList()

	LaunchedEffect(key1 = null) {
		scope.launch {
			path.clear()
			path.addAll(parentChapter.getPath())
		}
	}

	LazyRow(
		modifier = Modifier.fillMaxWidth()
	) {
		path.forEach { chapterObjectLite ->
			item {
				PathButton(
					title = chapterObjectLite.title,
					color = Color(chapterObjectLite.color),
					onClick = { /*TODO*/ }
				)
			}
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PathButton(
	title: String,
	color: Color,
	onClick: () -> Unit,
) {
	Card(
		shape = RoundedCornerShape(12.dp),
		colors = CardDefaults.cardColors(
			containerColor = color,
			contentColor = color.getInverseBWColor(),
		),
		onClick = onClick,
		modifier = Modifier.padding(4.dp)
	) {
		Box(
			modifier = Modifier.fillMaxSize(),
			contentAlignment = Alignment.Center
		) {
			Text(
				text = title,
				style = MaterialTheme.typography.bodyMedium
			)
		}
	}
}

@Composable
private fun ChapterList(
	modifier: Modifier,
	parentChapter: ChapterObject,
	onClickChapter: (ChapterObject) -> Unit
) {
	LazyColumn(
		modifier = modifier
	) {
		item { Spacer(modifier = Modifier.height(4.dp)) }
		parentChapter.chapterList.forEach { chapterObject ->
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
					color = Color(chapterObject.color ?: Color.White.toArgb()),
					noteCount = chapterObject.noteList.size,
					chapterCount = chapterObject.chapterList.size,
					isVisible = true,
					selectedColor = MaterialTheme.colorScheme.surface,
					onClick = { onClickChapter(chapterObject) }
				)
			}
		}
	}
}
