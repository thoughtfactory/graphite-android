package com.syncodec.graphite.presentation.common.v2.buildingBlock

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.utils.onlyIf
import com.syncodec.graphite.utils.onlyIfComposable


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun KeyValueCard(
    modifier: Modifier = Modifier,
    key: String,
    value: String,
    colors: CardColors = KeyValueDefaults.backgroundColors(),
    shape: Shape = MaterialTheme.shapes.medium,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
) {
    Card(
        colors = colors,
        shape = shape,
        modifier = modifier.onlyIf(predicate = onClick != null || onLongClick!=null) { combinedClickable(enabled = true, onClick = { onClick?.invoke() }, onClickLabel = value, onLongClick = onLongClick, onLongClickLabel = value) }
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp)
        ) {
            Text(
                text = key,
                style = MaterialTheme.typography.bodySmall,
                color = colors.contentColor.copy(alpha = 0.71f)
            )
            Spacer(modifier = Modifier.height(height = 4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

object KeyValueDefaults {

    @Composable
    fun backgroundColors(
        containerColor: Color = MaterialTheme.colorScheme.background,
        onContainerColor: Color = MaterialTheme.colorScheme.onBackground,
    ): CardColors = CardDefaults.cardColors(
        containerColor = containerColor,
        contentColor = onContainerColor
    )
}

