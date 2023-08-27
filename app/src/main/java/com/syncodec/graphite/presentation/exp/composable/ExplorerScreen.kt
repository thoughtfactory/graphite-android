package com.syncodec.graphite.presentation.exp.composable

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.di.model.ChapterObjectLite
import com.syncodec.graphite.di.model.NoteObjectLite
import com.syncodec.graphite.presentation.common.animation.AnimatedText
import com.syncodec.graphite.presentation.common.dialog.whereDialog2.WhereDialog2
import com.syncodec.graphite.presentation.common.scaffold.GenericScaffold2
import com.syncodec.graphite.presentation.exp.composable.bar.BottomBar
import com.syncodec.graphite.presentation.exp.composable.bar.TopBar
import com.syncodec.graphite.presentation.notebook.screen.buildingBlock.NoteCard
import com.syncodec.graphite.utils.timeStampToPrettyFull
import io.realm.kotlin.types.RealmUUID


@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun ExplorerScreen(
	screenTitle: String = "Explorer",
	currentChapter: ChapterObjectLite? = null,
	noteList: List<NoteObjectLite> = listOf(),
	onSelectChapter: (RealmUUID?) -> Unit = {},
	content: @Composable BoxScope.() -> Unit = {}
) {

	val scaffoldState = rememberBottomSheetScaffoldState()
	var isWhereDialogVisible by remember { mutableStateOf(false) }

	GenericScaffold2(
		topBar = {
			TopBar(title = screenTitle)
		},
		bottomBar = {
			BottomBar(
				currentChapter = currentChapter,
				onClickSearchIn = { isWhereDialogVisible = true },
			)
		},
		dialogContent = {
			WhereDialog2(
				isDialogVisible = isWhereDialogVisible,
				onDismissRequest = { isWhereDialogVisible = false },
				currentSelectedChapter = currentChapter?.id,
				onSelectChapter = onSelectChapter,
			)
		},
	) {
		BottomSheetScaffold(
			scaffoldState = scaffoldState,
			sheetDragHandle = { SheetHeader(noteCount = noteList.size) },
			sheetContent = {
				LazyColumn {
					noteList
						.forEach { note ->
							item(key = note.id.toString()) {
								NoteCard(
									id = note.id,
									timestamp = note.userTimestamp.timeStampToPrettyFull(),
									title = note.title,
									isFavourite = note.isFavourite,
									isLocked = note.isLocked,
									contentThumbnail = note.contentThumbnail,
									thumbnail = note.thumbnail,
									address = note.address,
									latLng = note.latLng,
									tagList = note.tagList,
//									isSelected = note.id in selectedIdList,
//									colors = noteCardColors ?: NoteCardDefaults.noteCardColors(),
//									onClick = { onClick(note) },
//									onLongClick = { onLongClick(note) },
								)
							}
						}

					if (noteList.isNotEmpty()) item { Spacer(modifier = Modifier.height(16.dp)) }
				}
			},
			sheetShape = RectangleShape,
			sheetContainerColor = MaterialTheme.colorScheme.background,
			containerColor = MaterialTheme.colorScheme.background,
		) {
			Box(modifier = Modifier.padding(it), content = content)
		}
	}
}

@Preview
@Composable
private fun SheetHeader(
	noteCount: Int = 0,
) {
	Row(
		verticalAlignment = Alignment.CenterVertically,
		modifier = Modifier
			.fillMaxWidth()
			.height(56.dp)
	) {
		Spacer(modifier = Modifier.width(24.dp))
		Text(
			text = "In visible region",
			style = MaterialTheme.typography.titleMedium,
			fontWeight = FontWeight.Bold,
		)
		Spacer(modifier = Modifier.weight(1f))
		AnimatedText(
			text = "$noteCount notes",
			style = MaterialTheme.typography.titleSmall,
			fontWeight = FontWeight.Bold,
		)
		Spacer(modifier = Modifier.width(24.dp))
	}
}
