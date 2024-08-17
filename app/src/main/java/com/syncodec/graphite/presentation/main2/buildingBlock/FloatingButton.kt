package com.syncodec.graphite.presentation.main2.buildingBlock

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandIn
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.base.ANIMATION_DURATION_MILLIS
import com.syncodec.graphite.presentation.base.SCALE_AND_FADE_TRANSFORM
import com.syncodec.graphite.presentation.main2.HomeComponent
import com.syncodec.graphite.presentation.main2.MainComponent


@Preview
@Composable
fun NoteFloatingActionButton(
    isExpanded: Boolean = true,
    onClick: () -> Unit = {},
) {
    ExtendedFloatingActionButton(
        text = { Text(text = stringResource(id = R.string.new_note)) },
        icon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_fa_pen),
                contentDescription = stringResource(id = R.string.new_note),
                modifier = Modifier.requiredSize(16.dp)
            )
        },
        expanded = isExpanded,
        onClick = onClick
    )
}

@Preview
@Composable
fun BucketFloatingActionButton(
    isExpanded: Boolean = true,
    onClick: () -> Unit = {},
) {
    ExtendedFloatingActionButton(
        text = { Text(text = stringResource(id = R.string.new_list)) },
        icon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_fa_bucket_list),
                contentDescription = stringResource(id = R.string.new_list),
                modifier = Modifier.requiredSize(16.dp)
            )
        },
        expanded = isExpanded,
        onClick = onClick
    )
}

@Preview
@Composable
fun NotebookFloatingActionButton(
    isExpanded: Boolean = true,
    onClick: () -> Unit = {},
) {
    ExtendedFloatingActionButton(
        text = { Text(text = stringResource(id = R.string.new_notebook)) },
        icon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_fa_notebook),
                contentDescription = stringResource(id = R.string.new_notebook),
                modifier = Modifier.requiredSize(16.dp)
            )
        },
        expanded = isExpanded,
        onClick = onClick
    )
}

@Composable
fun HomeScreenFloatingButton(
    currentHomeRoute: HomeComponent,
    currentMainRoute: MainComponent,
    isExpanded: Boolean,
    onClickNewNote: () -> Unit,
    onClickNewBucket: () -> Unit,
    onClickNewNotebook: () -> Unit,
) {

    AnimatedVisibility(
        visible = currentMainRoute is MainComponent.Home,
        enter = expandIn(tween(ANIMATION_DURATION_MILLIS)),
        exit = shrinkOut(tween(ANIMATION_DURATION_MILLIS)),
        label = "HomeScreenFab_visibility"
    ) {
        AnimatedContent(
            targetState = currentHomeRoute,
            transitionSpec = { SCALE_AND_FADE_TRANSFORM },
            label = "HomeScreenFab_animation"
        ) { currentRoute ->
            when (currentRoute) {
                is HomeComponent.Note -> NoteFloatingActionButton(isExpanded = isExpanded, onClick = onClickNewNote)
                is HomeComponent.Bucket -> BucketFloatingActionButton(isExpanded = isExpanded, onClick = onClickNewBucket)
                is HomeComponent.Notebook -> NotebookFloatingActionButton(isExpanded = isExpanded, onClick = onClickNewNotebook)
            }
        }
    }


}
