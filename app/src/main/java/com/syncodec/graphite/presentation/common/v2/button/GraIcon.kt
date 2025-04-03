package com.syncodec.graphite.presentation.common.v2.button

import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import com.syncodec.graphite.presentation.ui.ICON_SIZE


@Composable
fun GraIcon(
    icon: Int,
    contentDescription: String? = null,
    size: Dp = ICON_SIZE,
    tint: Color = LocalContentColor.current
) {
    Icon(
        painter = painterResource(id = icon),
        contentDescription = contentDescription,
        modifier = Modifier.requiredSize(size = size),
        tint = tint
    )
}
