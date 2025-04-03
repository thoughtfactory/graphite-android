package com.syncodec.graphite.presentation.bucket2.composable.buildingBlock

import androidx.compose.animation.AnimatedVisibility
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
import com.syncodec.graphite.di.modelObjectBox.BucketBoxEncrypted
import com.syncodec.graphite.presentation.ui.AnimationDefaults
import com.syncodec.graphite.presentation.ui.ICON_SIZE


@Composable
fun BucketFloatingButton(
    bucketType: BucketBoxEncrypted.BucketType = BucketBoxEncrypted.BucketType.Unknown,
    isSelecting: Boolean = false,
    onClickTodo: () -> Unit = {},
    onClickBook: () -> Unit = {},
    onClickShow: () -> Unit = {},
    onClickLink: () -> Unit = {},
    onClickLocation: () -> Unit = {},
) {
    AnimatedVisibility(
        visible = !isSelecting,
        enter = AnimationDefaults.ExpandVerticallyEnter,
        exit = AnimationDefaults.ShrinkVerticallyExit
    ) {
        when (bucketType) {
            BucketBoxEncrypted.BucketType.Todo -> TodoFloatingActionButton(onClick = onClickTodo)
            BucketBoxEncrypted.BucketType.Book -> BookFloatingActionButton(onClick = onClickBook)
            BucketBoxEncrypted.BucketType.Show -> ShowFloatingActionButton(onClick = onClickShow)
            BucketBoxEncrypted.BucketType.Link -> LinkFloatingActionButton(onClick = onClickLink)
            BucketBoxEncrypted.BucketType.Location -> LocationFloatingActionButton(onClick = onClickLocation)
            BucketBoxEncrypted.BucketType.Unknown -> Unit
        }
    }
}

@Preview
@Composable
private fun TodoFloatingActionButton(
    onClick: () -> Unit = {},
) {
    FloatingActionButton(
        text = stringResource(id = R.string.add_todo),
        icon = R.drawable.ic_fa_todo,
        onClick = onClick
    )
}

@Preview
@Composable
private fun BookFloatingActionButton(
    onClick: () -> Unit = {},
) {
    FloatingActionButton(
        text = stringResource(id = R.string.add_book),
        icon = R.drawable.ic_fa_books,
        onClick = onClick
    )
}

@Preview
@Composable
private fun ShowFloatingActionButton(
    onClick: () -> Unit = {},
) {
    FloatingActionButton(
        text = stringResource(id = R.string.add_show),
        icon = R.drawable.ic_fa_film,
        onClick = onClick
    )
}

@Preview
@Composable
private fun LinkFloatingActionButton(
    onClick: () -> Unit = {},
) {
    FloatingActionButton(
        text = stringResource(id = R.string.add_link),
        icon = R.drawable.ic_fa_link,
        onClick = onClick
    )
}

@Preview
@Composable
private fun LocationFloatingActionButton(
    onClick: () -> Unit = {},
) {
    FloatingActionButton(
        text = stringResource(id = R.string.add_location),
        icon = R.drawable.ic_fa_map_pin,
        onClick = onClick
    )
}

@Composable
private fun FloatingActionButton(
    text: String,
    icon: Int,
    onClick: () -> Unit = {},
) {
    ExtendedFloatingActionButton(
        text = { Text(text = text) },
        icon = {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = text,
                modifier = Modifier.requiredSize(size = ICON_SIZE)
            )
        },
        expanded = true,
        onClick = onClick
    )
}

