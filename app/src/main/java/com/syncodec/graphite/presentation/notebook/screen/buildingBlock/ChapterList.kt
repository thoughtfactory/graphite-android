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
import com.syncodec.graphite.di.model.ChapterObject
import com.syncodec.graphite.di.model.TagObject
import io.realm.kotlin.types.RealmUUID


@OptIn(ExperimentalFoundationApi::class)
fun LazyListScope.chapterList(
	chapterList : List<ChapterObject> = listOf(),
	chapterNoteItemCount : Map<RealmUUID?, Int> = mapOf(),
	chapterChapterItemCount : Map<RealmUUID?, Int> = mapOf(),
	tagList : List<TagObject> = listOf(),
	selectedIdList : List<RealmUUID> = listOf(),
	headerTitle : String = "Chapters",
	headerSubTitle : String = "${chapterList.size} chapters",
	headerMinHeight : Dp = Dp.Hairline,
	headerBackgroundColor : Color? = null,
	isVisible : Boolean = true,
	toggleVisibility : () -> Unit = {},
	headerEnabled : Boolean = true,
	onClick : (ChapterObject) -> Unit = {},
	onLongClick : (ChapterObject) -> Unit = {},
) {
	item { Spacer(modifier = Modifier.height(8.dp)) }

	stickyHeader {
		Header(
			title = headerTitle,
			subTitle = headerSubTitle,
			minHeight = headerMinHeight,
			backgroundColor = headerBackgroundColor ?: MaterialTheme.colorScheme.background,
			enabled = selectedIdList.isEmpty(),
		) { toggleVisibility() }
	}

	if (isVisible)
		chapterList
			.forEach { chapterObject ->
				item(
					key = chapterObject.id.toString()
				) {
					Box(
						modifier = Modifier.animateItemPlacement(tween(300))
					) {
						ChapterCard(
							id = chapterObject.id,
							createdTimestamp = chapterObject.createdTimestamp,
							modifiedTimestamp = chapterObject.modifiedTimestamp,
							title = chapterObject.title,
							description = chapterObject.description,
							isFavourite = chapterObject.isFavourite,
							isLocked = chapterObject.isLocked,
							color = chapterObject.color?.let { Color(it) },
							thumbnail = chapterObject.thumbnail,
							noteCount = chapterNoteItemCount[chapterObject.id] ?: 0,
							chapterCount = chapterChapterItemCount[chapterObject.id] ?: 0,
							isSelected = chapterObject.id in selectedIdList,
							onClick = { onClick(chapterObject) },
							onLongClick = { onLongClick(chapterObject) },
						)
					}
				}
			}

	item { Spacer(modifier = Modifier.height(96.dp)) }
}
