package com.syncodec.graphite.presentation.common.component.note

import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridScope
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.di.model.local.NoteObjectLite
import com.syncodec.graphite.di.model.local.TagObject
import com.syncodec.graphite.di.repository.group.RealmObjectGroupList
import com.syncodec.graphite.presentation.base.ANIMATION_DURATION_MILLIS
import com.syncodec.graphite.utils.SortOn
import com.syncodec.graphite.utils.timeStampToPrettyFull
import com.syncodec.graphite.utils.timeStampToTime
import io.realm.kotlin.types.RealmUUID


@OptIn(ExperimentalFoundationApi::class)
fun LazyListScope.noteList(
	noteGroupList: RealmObjectGroupList<NoteObjectLite> = RealmObjectGroupList(),
	tagList: List<TagObject> = listOf(),
	sortOn: SortOn = SortOn.Title,
	selectedIdList: Set<RealmUUID> = setOf(),
	onClick: (NoteObjectLite) -> Unit = {},
	onLongClick: (NoteObjectLite) -> Unit = {},
) {
	noteGroupList
		.groupList
		.forEach { (title, noteList) ->
			item(
				key = noteList.hashCode().toString(),
				contentType = { 3 }
			) {
				Box(
					modifier = Modifier
						.animateItemPlacement(tween(ANIMATION_DURATION_MILLIS))
						.padding(horizontal = 12.dp, vertical = 2.dp)
				) {
					NoteGroupHeader(
						text = title,
						subText = "${noteList.size} ${if (noteList.size == 1) "entry" else "entries"}",
						modifier = Modifier.animateItemPlacement(tween(ANIMATION_DURATION_MILLIS))
					)
				}
			}

			items(
				items = noteList,
				key = { it.id.toString() },
				contentType = { 4 }
			) { noteObjectLite ->
				NoteListCard2(
					id = noteObjectLite.id,
					timestamp = when (sortOn) {
						SortOn.Title -> noteObjectLite.userTimestamp.timeStampToPrettyFull()
						SortOn.Timestamp -> noteObjectLite.userTimestamp.timeStampToTime()
						SortOn.Modified -> noteObjectLite.userTimestamp.timeStampToPrettyFull()
						else -> noteObjectLite.userTimestamp.timeStampToPrettyFull()
					},
					title = noteObjectLite.title,
					contentThumbnail = noteObjectLite.contentThumbnail,
					address = noteObjectLite.address,
					latLng = noteObjectLite.latLng,
					isFavourite = noteObjectLite.isFavourite,
					isLocked = noteObjectLite.isLocked,
					tagList = tagList.filter { noteObjectLite.id in it.objectIdList }.map { it.toLite() },
					selected = noteObjectLite.id in selectedIdList,
					onClick = { onClick(noteObjectLite) },
					onLongClick = { onLongClick(noteObjectLite) },
					modifier = Modifier
						.animateItemPlacement(tween(ANIMATION_DURATION_MILLIS))
						.background(color = MaterialTheme.colorScheme.background)
						.padding(horizontal = 12.dp, vertical = 2.dp)
				)
			}
		}
}

@OptIn(ExperimentalFoundationApi::class)
fun LazyListScope.noteList(
	noteList: List<NoteObjectLite> = listOf(),
	tagList: List<TagObject> = listOf(),
	selectedIdList: Set<RealmUUID> = setOf(),
	onClick: (NoteObjectLite) -> Unit = {},
	onLongClick: (NoteObjectLite) -> Unit = {},
) {
	items(
		items = noteList,
		key = { it.id.toString() },
		contentType = { 4 }
	) { noteObjectLite ->
		NoteListCard2(
			id = noteObjectLite.id,
			timestamp = noteObjectLite.userTimestamp.timeStampToPrettyFull(),
			title = noteObjectLite.title,
			contentThumbnail = noteObjectLite.contentThumbnail,
			address = noteObjectLite.address,
			latLng = noteObjectLite.latLng,
			isFavourite = noteObjectLite.isFavourite,
			isLocked = noteObjectLite.isLocked,
			tagList = tagList.filter { noteObjectLite.id in it.objectIdList }.map { it.toLite() },
			selected = noteObjectLite.id in selectedIdList,
			onClick = { onClick(noteObjectLite) },
			onLongClick = { onLongClick(noteObjectLite) },
			modifier = Modifier
				.animateItemPlacement(tween(ANIMATION_DURATION_MILLIS))
				.background(color = MaterialTheme.colorScheme.background)
				.padding(horizontal = 12.dp, vertical = 2.dp)
		)
	}
}


@OptIn(ExperimentalFoundationApi::class)
fun LazyStaggeredGridScope.noteList(
	noteList: List<NoteObjectLite> = listOf(),
	tagList: List<TagObject> = listOf(),
	selectedIdList: Set<RealmUUID> = setOf(),
	onClick: (NoteObjectLite) -> Unit = {},
	onLongClick: (NoteObjectLite) -> Unit = {},
) {
	items(
		items = noteList,
		key = { it.id.toString() },
		contentType = { 4 }
	) { noteObjectLite ->
		NoteGridCard2(
			id = noteObjectLite.id,
			timestamp = noteObjectLite.userTimestamp.timeStampToPrettyFull(),
			title = noteObjectLite.title,
			contentThumbnail = noteObjectLite.contentThumbnail,
			address = noteObjectLite.address,
			latLng = noteObjectLite.latLng,
			isFavourite = noteObjectLite.isFavourite,
			isLocked = noteObjectLite.isLocked,
			tagList = tagList.filter { noteObjectLite.id in it.objectIdList }.map { it.toLite() },
			selected = noteObjectLite.id in selectedIdList,
			onClick = { onClick(noteObjectLite) },
			onLongClick = { onLongClick(noteObjectLite) },
			modifier = Modifier
				.animateItemPlacement(tween(ANIMATION_DURATION_MILLIS))
				.padding(horizontal = 12.dp, vertical = 2.dp)
		)
	}
}

