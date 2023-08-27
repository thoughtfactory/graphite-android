package com.syncodec.graphite.presentation.notebook.screen.buildingBlock

import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.di.model.NoteObjectLite
import com.syncodec.graphite.di.model.TagObject
import com.syncodec.graphite.utils.timeStampToPrettyFull
import io.realm.kotlin.types.RealmUUID


@OptIn(ExperimentalFoundationApi::class)
fun LazyListScope.noteList(
	noteList : List<NoteObjectLite> = listOf(),
	tagList : List<TagObject> = listOf(),
	selectedIdList : Set<RealmUUID> = setOf(),
	headerTitle : String = "Notes",
	headerSubTitle : String = "${noteList.size} notes",
	headerMinHeight : Dp = Dp.Hairline,
	headerBackgroundColor : Color? = null,
	isVisible : Boolean = true,
	toggleVisibility : (() -> Unit) = {},
	noteCardColors : NoteCardColors? = null,
	headerEnabled : Boolean = true,
	onClick : (NoteObjectLite) -> Unit = {},
	onLongClick : (NoteObjectLite) -> Unit = {},
) {
	item { Spacer(modifier = Modifier.height(8.dp)) }
	stickyHeader {
		Header(
			title = headerTitle,
			subTitle = headerSubTitle,
			minHeight = headerMinHeight,
			backgroundColor = headerBackgroundColor ?: MaterialTheme.colorScheme.background,
			enabled = selectedIdList.isEmpty() && headerEnabled,
		) { toggleVisibility() }
	}

	if (isVisible)
		noteList
			.forEach { note ->
				item(
					key = note.id.toString()
				) {
					Box(
						modifier = Modifier.animateItemPlacement(tween(300))
					) {
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
							colors = noteCardColors ?: NoteCardDefaults.noteCardColors(),
							onClick = { onClick(note) },
							onLongClick = { onLongClick(note) },
						)
					}
				}
			}
}
