package com.syncodec.graphite.presentation.common.component.chapter

import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.di.model.local.ChapterObject
import io.realm.kotlin.types.RealmUUID


@OptIn(ExperimentalFoundationApi::class)
fun LazyListScope.chapterList(
	chapterList: List<ChapterObject> = listOf(),
	chapterNoteItemCount: Map<RealmUUID?, Int> = mapOf(),
	chapterChapterItemCount: Map<RealmUUID?, Int> = mapOf(),
	selectedIdList: Set<RealmUUID> = setOf(),
	componentColumnCount: Int = 2,
	onClick: (ChapterObject) -> Unit = {},
	onLongClick: (ChapterObject) -> Unit = {},
) {
	items(
		count = chapterList.size,
		key = { chapterList[it].id.toString() },
		contentType = { 2 }
	) { index ->
		val chapterObject = chapterList[index]
		ChapterGridCard(
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
			selected = chapterObject.id in selectedIdList,
			onClick = { onClick(chapterObject) },
			onLongClick = { onLongClick(chapterObject) },
			modifier = Modifier
				.animateItemPlacement(tween(470))
				.padding(start = if (index % componentColumnCount == 0) 12.dp else 4.dp, top = 4.dp, end = if ((index + 1) % componentColumnCount == 0) 12.dp else 4.dp, bottom = 4.dp)
		)
	}
}

@OptIn(ExperimentalFoundationApi::class)
fun LazyStaggeredGridScope.chapterList(
	chapterList: List<ChapterObject> = listOf(),
	chapterNoteItemCount: Map<RealmUUID?, Int> = mapOf(),
	chapterChapterItemCount: Map<RealmUUID?, Int> = mapOf(),
	selectedIdList: Set<RealmUUID> = setOf(),
	componentColumnCount: Int = 2,
	onClick: (ChapterObject) -> Unit = {},
	onLongClick: (ChapterObject) -> Unit = {},
) {
	items(
		count = chapterList.size,
		key = { chapterList[it].id.toString() },
		contentType = { 2 }
	) { index ->
		val chapterObject = chapterList[index]
		ChapterGridCard(
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
			selected = chapterObject.id in selectedIdList,
			onClick = { onClick(chapterObject) },
			onLongClick = { onLongClick(chapterObject) },
			modifier = Modifier
				.animateItemPlacement(tween(470))
				.padding(start = if (index % componentColumnCount == 0) 12.dp else 4.dp, top = 4.dp, end = if ((index + 1) % componentColumnCount == 0) 12.dp else 4.dp, bottom = 4.dp)
		)
	}
}
