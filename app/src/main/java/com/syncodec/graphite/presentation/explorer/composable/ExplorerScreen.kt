package com.syncodec.graphite.presentation.explorer.composable

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.di.model.ChapterObjectLite
import com.syncodec.graphite.di.model.NoteObjectLite
import com.syncodec.graphite.presentation.common.animation.AnimatedText
import com.syncodec.graphite.presentation.common.dialog.whereDialog2.WhereDialog2
import com.syncodec.graphite.presentation.common.scaffold.GenericScaffold2
import com.syncodec.graphite.presentation.common.selectionAction.ExplorerSelectionActionView
import com.syncodec.graphite.presentation.explorer.composable.bar.BottomBar
import com.syncodec.graphite.presentation.explorer.composable.bar.TopBar
import com.syncodec.graphite.presentation.note2.NoteActivity2
import com.syncodec.graphite.presentation.notebook.screen.buildingBlock.NoteCard
import com.syncodec.graphite.utils.Extra
import com.syncodec.graphite.utils.timeStampToPrettyFull
import com.syncodec.graphite.utils.xor
import io.realm.kotlin.types.RealmUUID


@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun ExplorerScreen(
	screenTitle: String = "Explorer",
	bottomSheetTitle: String = "In visible region",
	currentChapter: ChapterObjectLite? = null,
	noteList: List<NoteObjectLite> = listOf(),
	onExploreChapter: (RealmUUID?) -> Unit = {},
	content: @Composable BoxScope.() -> Unit = {}
) {
	var isWhereDialogVisible by remember { mutableStateOf(false) }

	GenericScaffold2(
		topBar = { TopBar(title = screenTitle) },
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
				onSelectChapter = onExploreChapter,
			)
		},
	) {
		ExplorerView(
			bottomSheetTitle = bottomSheetTitle,
			noteList = noteList,
			content = content,
		)
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun ExplorerView(
	bottomSheetTitle: String = "In visible region",
	noteList: List<NoteObjectLite> = listOf(),
	content: @Composable BoxScope.() -> Unit = {}
) {
	val context = LocalContext.current

	var isSelecting by remember { mutableStateOf(false) }
	var selectedIdList: Set<RealmUUID> by remember { mutableStateOf(setOf()) }
	fun onSelect(id: RealmUUID) {
		if (!isSelecting) isSelecting = true
		selectedIdList.toMutableSet().apply {
			xor(id)
			selectedIdList = toSet()
		}
	}

	BackHandler(enabled = isSelecting) {
		isSelecting = false
		selectedIdList = setOf()
	}

	BottomSheetScaffold(
		sheetShape = RectangleShape,
		sheetContainerColor = MaterialTheme.colorScheme.background,
		containerColor = MaterialTheme.colorScheme.background,
		sheetDragHandle = {
			SheetHeader(
				title = bottomSheetTitle,
				noteCount = noteList.size,
			)
		},
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
								isSelected = note.id in selectedIdList,
								onClick = {
									if (isSelecting) onSelect(id = note.id)
									else {
										Intent(context, NoteActivity2::class.java).apply {
											putExtra(Extra.Companion.Extra.IsNew.name, false)
											putExtra(Extra.Companion.Extra.NoteId.name, note.id.bytes)
											putExtra(Extra.Companion.Extra.Filter.name, Extra.Companion.Filter.SingleRead.name)
											context.startActivity(this)
										}
									}
								},
								onLongClick = { onSelect(id = note.id) },
							)
						}
					}

				if (noteList.isNotEmpty()) item { Spacer(modifier = Modifier.height(16.dp)) }
			}

			ExplorerSelectionActionView(
				modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 24.dp),
				isSelecting = isSelecting,
				isAllItemFavourite = selectedIdList.isNotEmpty() && noteList.filter { it.id in selectedIdList }.all { it.isFavourite },
				isAllItemLocked = selectedIdList.isNotEmpty() && noteList.filter { it.id in selectedIdList }.all { it.isLocked },
				selectedItemCount = selectedIdList.size,
				onClickDelete = {},
				onClickFavourite = {},
				onClickLock = {},
			)
		},
	) {
		Box(modifier = Modifier.padding(it), content = content)
	}
}

@Preview
@Composable
private fun SheetHeader(
	title: String = "In visible region",
	noteCount: Int = 0,
) {
	Column(
		modifier = Modifier
			.fillMaxWidth()
			.background(MaterialTheme.colorScheme.background)
	) {
		Divider()
		Row(
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier
				.fillMaxWidth()
				.height(56.dp)
		) {
			Spacer(modifier = Modifier.width(24.dp))
			Text(
				text = title,
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
}
