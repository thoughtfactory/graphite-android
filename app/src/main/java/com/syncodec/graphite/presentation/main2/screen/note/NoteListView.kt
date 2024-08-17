package com.syncodec.graphite.presentation.main2.screen.note

import android.util.Log
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.syncodec.graphite.di.model.NoteObject
import com.syncodec.graphite.presentation.common.component.notePreview.NoteListCard2


@Composable
fun NoteListView(
    noteList: List<NoteObject>
) {

    Log.d("NoteListView", "noteList : ${noteList.size}")
    LazyColumn {
        items(items = noteList) { noteObject ->
            NoteListCard2(
                id = noteObject.id,
                timestamp = noteObject.userTimestamp.toString(),
                title = noteObject.title,
                contentThumbnail = noteObject.contentThumbnail,
                address = noteObject.address,
                latLng = noteObject.getLatLng(),
                isFavourite = noteObject.isFavourite,
                isLocked = noteObject.isLocked,
                tagList = listOf(),
                selected = false,
//                        colors = ,
//                        onClick = ,
//                        onLongClick = ,
            )
        }
    }
}
