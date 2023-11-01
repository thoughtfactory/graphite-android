package com.syncodec.graphite.presentation.common.component.note

import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridScope
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.di.model.local.NoteObjectLite
import com.syncodec.graphite.di.model.local.TagObject
import com.syncodec.graphite.utils.timeStampToPrettyFull
import io.realm.kotlin.types.RealmUUID


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
		contentType = { 2 }
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
				.animateItemPlacement(tween(470))
				.padding(2.dp)
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
		contentType = { 2 }
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
				.animateItemPlacement(tween(470))
				.padding(2.dp)
		)
	}
}

