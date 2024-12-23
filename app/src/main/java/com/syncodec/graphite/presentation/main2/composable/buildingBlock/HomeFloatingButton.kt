package com.syncodec.graphite.presentation.main2.composable.buildingBlock

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.main2.composable.bar.HomeScreenData
import com.syncodec.graphite.presentation.main2.composable.bar.MainScreenData
import com.syncodec.graphite.presentation.ui.ICON_SIZE


@Composable
fun HomeFloatingActionButton(
    homeScreenData: HomeScreenData,
    isExpanded: Boolean = true,
    onClickNewNote: () -> Unit = {},
    onClickNewBucket: () -> Unit = {},
    onClickNewNotebook: () -> Unit = {},
) {
    AnimatedContent(
        targetState = homeScreenData
    ) {
        when(it) {
            HomeScreenData.NoteScreenData -> NoteFloatingActionButton(isExpanded = isExpanded, onClick = onClickNewNote)
            HomeScreenData.BucketScreenData -> BucketFloatingActionButton(isExpanded = isExpanded, onClick = onClickNewBucket)
            HomeScreenData.NotebookScreenData -> NotebookFloatingActionButton(isExpanded = isExpanded, onClick = onClickNewNotebook)
        }
    }
}

@Preview
@Composable
private fun NoteFloatingActionButton(
    isExpanded: Boolean = true,
    onClick: () -> Unit = {},
) {
    ExtendedFloatingActionButton(
        text = {
            Text(text = stringResource(R.string.new_note))
        },
        icon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_fa_pencil),
                contentDescription = stringResource(R.string.new_note),
                modifier = Modifier.requiredSize(ICON_SIZE)
            )
        },
        expanded = isExpanded,
        onClick = onClick
    )
}

@Preview
@Composable
private fun BucketFloatingActionButton(
    isExpanded: Boolean = true,
    onClick: () -> Unit = {},
) {
    ExtendedFloatingActionButton(
        text = {
            Text(text = stringResource(R.string.new_list))
        },
        icon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_fa_bucket),
                contentDescription = stringResource(R.string.new_bucket),
                modifier = Modifier.requiredSize(ICON_SIZE)
            )
        },
        expanded = isExpanded,
        onClick = onClick
    )
}

@Preview
@Composable
private fun NotebookFloatingActionButton(
    isExpanded: Boolean = true,
    onClick: () -> Unit = {},
) {
    ExtendedFloatingActionButton(
        text = {
            Text(text = stringResource(R.string.new_notebook))
        },
        icon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_fa_notebook),
                contentDescription = stringResource(R.string.new_notebook),
                modifier = Modifier.requiredSize(ICON_SIZE)
            )
        },
        expanded = isExpanded,
        onClick = onClick
    )
}

