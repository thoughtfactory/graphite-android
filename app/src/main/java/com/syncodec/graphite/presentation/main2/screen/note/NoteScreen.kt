package com.syncodec.graphite.presentation.main2.screen.note

import android.util.Log
import androidx.compose.runtime.Composable
import com.syncodec.graphite.di.model.NoteObject


@Composable
fun NoteScreen(
    noteList: List<NoteObject>
) {

    Log.d("NoteScreen", "NoteScreen")

    NoteListView(
        noteList = noteList
    )

}
