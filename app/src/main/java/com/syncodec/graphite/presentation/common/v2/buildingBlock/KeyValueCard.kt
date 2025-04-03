package com.syncodec.graphite.presentation.common.v2.buildingBlock

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.ui.ColorDefaults.BlueColor
import com.syncodec.graphite.utils.onlyIf


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun KeyValueCard(
    modifier: Modifier = Modifier,
    key: String? = null,
    value: String,
    colors: CardColors = KeyValueDefaults.backgroundColors(),
    shape: Shape = MaterialTheme.shapes.medium,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
) {
    Card(
        colors = colors,
        shape = shape,
        modifier = modifier.onlyIf(predicate = onClick != null || onLongClick != null) { combinedClickable(enabled = true, onClick = { onClick?.invoke() }, onClickLabel = value, onLongClick = onLongClick, onLongClickLabel = value) }
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp)
        ) {
            if (key != null) {
                Text(
                    text = key,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.contentColor.copy(alpha = 0.71f)
                )
                Spacer(modifier = Modifier.height(height = 4.dp))
            }
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

object KeyValueCard {
    data class Colors(
        val containerColor: Color,
        val contentColor: Color,
        val outlineColor: Color = Color.Transparent
    )

    object Defaults {
        @Composable
        fun backgroundLikeColors(
            containerColor: Color = MaterialTheme.colorScheme.background,
            contentColor: Color = MaterialTheme.colorScheme.onBackground,
            outlineColor: Color = Color.Transparent
        ) = Colors(containerColor = containerColor, contentColor = contentColor, outlineColor = outlineColor)

        @Composable
        fun surfaceLikeColors(
            containerColor: Color = MaterialTheme.colorScheme.surface,
            contentColor: Color = MaterialTheme.colorScheme.onSurface,
            outlineColor: Color = Color.Transparent
        ) = Colors(containerColor = containerColor, contentColor = contentColor, outlineColor = outlineColor)

        @Composable
        fun infoColors(
            containerColor: Color = Color.BlueColor,
            contentColor: Color = MaterialTheme.colorScheme.onSurface,
            outlineColor: Color = Color.Transparent
        ) = Colors(containerColor = containerColor, contentColor = contentColor, outlineColor = outlineColor)
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun Composable(
        modifier: Modifier = Modifier,
        key: String? = null,
        value: String,
        colors: Colors = Defaults.backgroundLikeColors(),
        shape: Shape = MaterialTheme.shapes.medium,
        onClick: (() -> Unit)? = null,
        onLongClick: (() -> Unit)? = null,
    ) {
        Surface(
            color = colors.containerColor,
            contentColor = colors.contentColor,
            shape = shape,
            border = BorderStroke(width = 1.dp, color = colors.outlineColor),
            modifier = modifier
                .clip(shape = shape)
                .padding(horizontal = 4.dp, vertical = 2.dp)
                .onlyIf(predicate = onClick != null || onLongClick != null) { combinedClickable(enabled = true, onClick = { onClick?.invoke() }, onClickLabel = value, onLongClick = onLongClick, onLongClickLabel = value) }
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp)
            ) {
                if (key != null) {
                    Text(
                        text = key,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = colors.contentColor.copy(alpha = 0.71f)
                    )
                    Spacer(modifier = Modifier.height(height = 4.dp))
                }
                Text(
                    text = value.ifBlank { stringResource(id = R.string.no_data) },
                    style = MaterialTheme.typography.bodySmall,
                    fontStyle = if (value.isBlank()) FontStyle.Italic else FontStyle.Normal,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
