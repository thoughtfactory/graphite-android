package com.syncodec.graphite.presentation.main2.composable.screen

import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.di.modelObjectBox.ChapterBox
import kotlinx.coroutines.flow.Flow
import androidx.compose.runtime.getValue


@Composable
fun NotebookScreen(
    allChapterBoxListFlow: Flow<List<ChapterBox>>
) {

    val allChapterBoxList by allChapterBoxListFlow.collectAsState(listOf())



    LazyVerticalGrid(
        modifier = Modifier.fillMaxSize(),
        columns = GridCells.Adaptive(96.dp)
    ) {
        items(
            items = allChapterBoxList
        ) {
            Text(text = "${it.title}")
        }
    }
}

@Composable
private fun NotebookCard() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.66f)
    ) {

    }
}
