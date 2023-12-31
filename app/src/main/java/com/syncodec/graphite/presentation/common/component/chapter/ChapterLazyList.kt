package com.syncodec.graphite.presentation.common.component.chapter

import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.di.model.local.ChapterObject
import com.syncodec.graphite.presentation.base.ANIMATION_DURATION_MILLIS
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
		count = (chapterList.size + 1) / 2,
		key = { chapterList.getOrNull(it * 2)?.id?.toString() + chapterList.getOrNull(it * 2 + 1)?.id?.toString() },
		contentType = { 2 }
	) { index ->
		val chapterObject1 = chapterList.getOrNull(index * 2)
		val chapterObject2 = chapterList.getOrNull(index * 2 + 1)

		Row(
			modifier = Modifier
				.fillMaxWidth()
				.background(color = MaterialTheme.colorScheme.background)
				.padding(horizontal = 8.dp)
				.animateItemPlacement(tween(ANIMATION_DURATION_MILLIS))
		) {
			chapterObject1?.let {
				ChapterListCard(
					id = chapterObject1.id,
					createdTimestamp = chapterObject1.createdTimestamp,
					modifiedTimestamp = chapterObject1.modifiedTimestamp,
					title = chapterObject1.title,
					description = chapterObject1.description,
					isFavourite = chapterObject1.isFavourite,
					isLocked = chapterObject1.isLocked,
					color = chapterObject1.color?.let { Color(it) },
					thumbnail = chapterObject1.thumbnail,
					noteCount = chapterNoteItemCount[chapterObject1.id] ?: 0,
					chapterCount = chapterChapterItemCount[chapterObject1.id] ?: 0,
					selected = chapterObject1.id in selectedIdList,
					onClick = { onClick(chapterObject1) },
					onLongClick = { onLongClick(chapterObject1) },
					modifier = Modifier.weight(1f)
				)
			}

			chapterObject2?.let {
				ChapterListCard(
					id = chapterObject2.id,
					createdTimestamp = chapterObject2.createdTimestamp,
					modifiedTimestamp = chapterObject2.modifiedTimestamp,
					title = chapterObject2.title,
					description = chapterObject2.description,
					isFavourite = chapterObject2.isFavourite,
					isLocked = chapterObject2.isLocked,
					color = chapterObject2.color?.let { Color(it) },
					thumbnail = chapterObject2.thumbnail,
					noteCount = chapterNoteItemCount[chapterObject2.id] ?: 0,
					chapterCount = chapterChapterItemCount[chapterObject2.id] ?: 0,
					selected = chapterObject2.id in selectedIdList,
					onClick = { onClick(chapterObject2) },
					onLongClick = { onLongClick(chapterObject2) },
					modifier = Modifier.weight(1f)
				)
			} ?: Spacer(modifier = Modifier.weight(1f))
		}
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
