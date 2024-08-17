package com.syncodec.graphite.presentation.common.bottomSheet

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GenericBottomSheetInfo(
    modifier: Modifier = Modifier,
    key: String,
    value: String,
    icon: Int? = null,
    suffix: (@Composable () -> Unit)? = null,
    colors: GenericBottomSheetInfo2Colors = GenericBottomSheetInfoDefaults.infoColors(),
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null
) {
    val containerColor by colors.containerColor(enabled = enabled)
    val contentColor by colors.contentColor(enabled = enabled)
    val iconColor by colors.iconColor(enabled = enabled)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(2.dp)
            .background(containerColor, MaterialTheme.shapes.medium)
            .clip(MaterialTheme.shapes.medium)
            .combinedClickable(enabled = enabled && (onClick != null || onLongClick != null), onClick = onClick ?: {}, onLongClick = onLongClick ?: {})
            .padding(16.dp),
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = key,
                color = contentColor,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = value,
                color = contentColor.copy(alpha = 0.71f),
                style = MaterialTheme.typography.bodyMedium
            )
        }
        icon?.let {
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                painter = painterResource(id = it),
                contentDescription = null,
                tint = iconColor ?: contentColor,
                modifier = Modifier.requiredSize(24.dp)
            )
        }

        suffix?.let {
            Spacer(modifier = Modifier.width(8.dp))
            it()
        }
    }
}

@Immutable
data class GenericBottomSheetInfo2Colors(
    val containerColor: Color,
    val contentColor: Color,
    val iconColor: Color? = null,
    val disabledContainerColor: Color,
    val disabledContentColor: Color,
    val disabledIconColor: Color? = null
) {
    @Composable
    internal fun containerColor(enabled: Boolean): State<Color> = rememberUpdatedState(if (enabled) containerColor else disabledContainerColor)

    @Composable
    internal fun contentColor(enabled: Boolean): State<Color> = rememberUpdatedState(if (enabled) contentColor else disabledContentColor)

    @Composable
    internal fun iconColor(enabled: Boolean): State<Color?> = rememberUpdatedState(if (enabled) iconColor else disabledIconColor)
}

object GenericBottomSheetInfoDefaults {
    @Composable
    fun infoColors(
        containerColor: Color = Color(ColorUtils.blendARGB(MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp).toArgb(), MaterialTheme.colorScheme.background.toArgb(), 0.88f)),
        contentColor: Color = MaterialTheme.colorScheme.onSurface,
        iconColor: Color? = null,
        disabledContainerColor: Color = Color(ColorUtils.blendARGB(MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp).toArgb(), MaterialTheme.colorScheme.background.toArgb(), 0.88f)),
        disabledContentColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.31f),
        disabledIconColor: Color? = null
    ): GenericBottomSheetInfo2Colors = GenericBottomSheetInfo2Colors(
        containerColor = containerColor,
        contentColor = contentColor,
        iconColor = iconColor,
        disabledContainerColor = disabledContainerColor,
        disabledContentColor = disabledContentColor,
        disabledIconColor = disabledIconColor
    )

    @Composable
    fun primaryInfoColors(
        containerColor: Color = MaterialTheme.colorScheme.primary,
        contentColor: Color = MaterialTheme.colorScheme.onPrimary,
        iconColor: Color? = null,
        disabledContainerColor: Color = MaterialTheme.colorScheme.primary,
        disabledContentColor: Color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.31f),
        disabledIconColor: Color? = null
    ): GenericBottomSheetInfo2Colors = GenericBottomSheetInfo2Colors(
        containerColor = containerColor,
        contentColor = contentColor,
        iconColor = iconColor,
        disabledContainerColor = disabledContainerColor,
        disabledContentColor = disabledContentColor,
        disabledIconColor = disabledIconColor
    )

    @Composable
    fun errorInfoColors(
        containerColor: Color = MaterialTheme.colorScheme.errorContainer,
        contentColor: Color = MaterialTheme.colorScheme.onErrorContainer,
        iconColor: Color? = null,
        disabledContainerColor: Color = MaterialTheme.colorScheme.errorContainer,
        disabledContentColor: Color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.31f),
        disabledIconColor: Color? = null
    ): GenericBottomSheetInfo2Colors = GenericBottomSheetInfo2Colors(
        containerColor = containerColor,
        contentColor = contentColor,
        iconColor = iconColor,
        disabledContainerColor = disabledContainerColor,
        disabledContentColor = disabledContentColor,
        disabledIconColor = disabledIconColor
    )

    @Composable
    fun warningInfoColors(
        containerColor: Color = Color(0x80F3B664),
        contentColor: Color = MaterialTheme.colorScheme.onBackground,
        iconColor: Color? = null,
        disabledContainerColor: Color = Color(ColorUtils.blendARGB(Color(0xFFF3B664).toArgb(), MaterialTheme.colorScheme.background.toArgb(), 0.42f)),
        disabledContentColor: Color = MaterialTheme.colorScheme.onBackground,
        disabledIconColor: Color? = null
    ): GenericBottomSheetInfo2Colors = GenericBottomSheetInfo2Colors(
        containerColor = containerColor,
        contentColor = contentColor,
        iconColor = iconColor,
        disabledContainerColor = disabledContainerColor,
        disabledContentColor = disabledContentColor,
        disabledIconColor = disabledIconColor
    )

    @Composable
    fun pendingColors(
        containerColor: Color = Color(0x807C93C3),
        contentColor: Color = MaterialTheme.colorScheme.onBackground,
        iconColor: Color? = null,
        disabledContainerColor: Color = Color(ColorUtils.blendARGB(Color(0xFF7C93C3).toArgb(), MaterialTheme.colorScheme.background.toArgb(), 0.42f)),
        disabledContentColor: Color = MaterialTheme.colorScheme.onBackground,
        disabledIconColor: Color? = null
    ): GenericBottomSheetInfo2Colors = GenericBottomSheetInfo2Colors(
        containerColor = containerColor,
        contentColor = contentColor,
        iconColor = iconColor,
        disabledContainerColor = disabledContainerColor,
        disabledContentColor = disabledContentColor,
        disabledIconColor = disabledIconColor
    )

    @Composable
    fun successColors(
        containerColor: Color = Color(0x8089B9AD),
        contentColor: Color = MaterialTheme.colorScheme.onBackground,
        iconColor: Color? = null,
        disabledContainerColor: Color = Color(ColorUtils.blendARGB(Color(0xFF89B9AD).toArgb(), MaterialTheme.colorScheme.background.toArgb(), 0.42f)),
        disabledContentColor: Color = MaterialTheme.colorScheme.onBackground,
        disabledIconColor: Color? = null
    ): GenericBottomSheetInfo2Colors = GenericBottomSheetInfo2Colors(
        containerColor = containerColor,
        contentColor = contentColor,
        iconColor = iconColor,
        disabledContainerColor = disabledContainerColor,
        disabledContentColor = disabledContentColor,
        disabledIconColor = disabledIconColor
    )
}

