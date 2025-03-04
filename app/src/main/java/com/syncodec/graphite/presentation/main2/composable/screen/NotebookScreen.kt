package com.syncodec.graphite.presentation.main2.composable.screen

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.di.modelObjectBox.ChapterBox
import kotlinx.coroutines.flow.Flow
import androidx.compose.runtime.getValue
import com.syncodec.graphite.presentation.main2.composable.buildingBlock.notebook.NotebookCard
import kotlinx.serialization.InternalSerializationApi
import sh.calvin.reorderable.rememberReorderableLazyListState


@OptIn(InternalSerializationApi::class)
@Composable
fun NotebookScreen(
    allChapterBoxListFlow: Flow<List<ChapterBox>>
) {

    val allChapterBoxList by allChapterBoxListFlow.collectAsState(listOf())
    val lazyListState = rememberLazyListState()
    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
        // Update the list
    }

    LazyVerticalGrid(
        modifier = Modifier.fillMaxSize(),
        columns = GridCells.Adaptive(minSize = 144.dp),
        contentPadding = PaddingValues(all = 8.dp)
    ) {
        items(
            items = allChapterBoxList
        ) { chapterBox ->
            NotebookCard(
                titleText = chapterBox.title,
                descriptionText = chapterBox.description,
                thumbnail = chapterBox.thumbnail,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(ratio = 1f)
                    .padding(all = 6.dp)
            )
        }
    }
}
