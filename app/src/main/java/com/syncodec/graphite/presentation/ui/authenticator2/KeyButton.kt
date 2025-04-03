package com.syncodec.graphite.presentation.ui.authenticator2

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import io.github.esentsov.PackagePrivate


@OptIn(ExperimentalLayoutApi::class)
@PackagePrivate
@Composable
fun RowScope.Key(
    text: String,
    onClick: (String) -> Unit = {}
) {
    KeyContainer(
        onClick = { onClick(text) }
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleLarge
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@PackagePrivate
@Composable
fun RowScope.KeyContainer(
    onClick: () -> Unit = {},
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .weight(weight = 1f)
            .aspectRatio(ratio = 1.5f)
            .clickable(enabled = true, onClick = onClick),
        content = content
    )
}

@Composable
fun ColumnScope.KeypadSurface(
    onClickKey: (String) -> Unit = {},
    onClickBackspace: () -> Unit = {},
    onClickOk: () -> Unit = {},
) {
    with(this) {
        Row(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Key(text = "1", onClick = onClickKey)
            Key(text = "2", onClick = onClickKey)
            Key(text = "3", onClick = onClickKey)
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Key(text = "4", onClick = onClickKey)
            Key(text = "5", onClick = onClickKey)
            Key(text = "6", onClick = onClickKey)
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Key(text = "7", onClick = onClickKey)
            Key(text = "8", onClick = onClickKey)
            Key(text = "9", onClick = onClickKey)
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
        ) {
            KeyContainer(onClick = onClickBackspace) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_fa_back),
                    contentDescription = null,
                    modifier = Modifier.requiredSize(size = 24.dp)
                )
            }
            Key(text = "0", onClick = onClickKey)
            KeyContainer(onClick = onClickOk) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_fa_check),
                    contentDescription = null,
                    modifier = Modifier.requiredSize(size = 24.dp)
                )
            }
        }
    }
}
