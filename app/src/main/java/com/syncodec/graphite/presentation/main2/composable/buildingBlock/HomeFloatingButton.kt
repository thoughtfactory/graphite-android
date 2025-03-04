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
import com.syncodec.graphite.presentation.ui.AnimationDefaults
import com.syncodec.graphite.presentation.ui.ICON_SIZE


@Composable
fun HomeFloatingActionButton(
    currentBackStackRoute: String?,
    isExpanded: Boolean = true,
    onClickNewNote: () -> Unit = {},
    onClickNewBucket: () -> Unit = {},
    onClickNewNotebook: () -> Unit = {},
) {
    AnimatedContent(
        targetState = currentBackStackRoute,
        transitionSpec = { AnimationDefaults.ScaleAndFade }
    ) {
        when(it) {
            HomeScreenData.NoteScreenData.route -> NoteFloatingActionButton(isExpanded = isExpanded, onClick = onClickNewNote)
            HomeScreenData.BucketScreenData.route -> BucketFloatingActionButton(isExpanded = isExpanded, onClick = onClickNewBucket)
            HomeScreenData.NotebookScreenData.route -> NotebookFloatingActionButton(isExpanded = isExpanded, onClick = onClickNewNotebook)
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
        text = { Text(text = stringResource(id = R.string.new_note)) },
        icon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_fa_pencil),
                contentDescription = stringResource(id = R.string.new_note),
                modifier = Modifier.requiredSize(size = ICON_SIZE)
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
        text = { Text(text = stringResource(id = R.string.new_list)) },
        icon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_fa_bucket),
                contentDescription = stringResource(id = R.string.new_bucket),
                modifier = Modifier.requiredSize(size = ICON_SIZE)
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
        text = { Text(text = stringResource(id = R.string.new_notebook)) },
        icon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_fa_notebook),
                contentDescription = stringResource(id = R.string.new_notebook),
                modifier = Modifier.requiredSize(size = ICON_SIZE)
            )
        },
        expanded = isExpanded,
        onClick = onClick
    )
}

