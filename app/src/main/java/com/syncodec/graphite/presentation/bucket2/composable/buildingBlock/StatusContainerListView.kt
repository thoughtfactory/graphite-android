package com.syncodec.graphite.presentation.bucket2.composable.buildingBlock

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.ui.FavouriteContainer
import com.syncodec.graphite.presentation.ui.LockClosedContainer


@Composable
fun StatusContainer(
    modifier: Modifier = Modifier,
    isFavourite: Boolean,
    isLocked: Boolean,
) {
    val backgroundColor = MaterialTheme.colorScheme.background
    val favouriteColor = Color(ColorUtils.blendARGB(Color.FavouriteContainer.toArgb(), backgroundColor.toArgb(), 0.31f))
    val lockColor = Color(ColorUtils.blendARGB(Color.LockClosedContainer.toArgb(), backgroundColor.toArgb(), 0.31f))

    if (isFavourite || isLocked) Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .background(color = MaterialTheme.colorScheme.surfaceContainer, shape = MaterialTheme.shapes.small)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        if (isFavourite) Icon(
            painter = painterResource(id = R.drawable.ic_fa_heart_solid),
            contentDescription = stringResource(id = R.string.favourite),
            tint = favouriteColor,
            modifier = Modifier.size(size = 14.dp)
        )
        if (isFavourite && isLocked) Spacer(modifier = Modifier.width(width = 8.dp))
        if (isLocked) Icon(
            painter = painterResource(id = R.drawable.ic_fa_lock_closed_solid),
            contentDescription = stringResource(id = R.string.entry_locked),
            tint = lockColor,
            modifier = Modifier.size(size = 14.dp)
        )
    }
}

