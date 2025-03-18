package com.syncodec.graphite.presentation.bucket2.composable.bottomSheet.buildingBlock

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.ui.AnimationDefaults
import com.syncodec.graphite.presentation.ui.FavouriteContainer
import com.syncodec.graphite.presentation.ui.LockClosedContainer
import io.github.esentsov.PackagePrivate


@Composable
fun RowScope.FavouriteButton(
    isFavourite: Boolean = true,
    onClick: () -> Unit = {}
) {

    val containerColor by animateColorAsState(targetValue = if (isFavourite) Color.FavouriteContainer.copy(alpha = 0.47f) else Color.Transparent, animationSpec = AnimationDefaults.stateAnimationSpec())
    val borderColor by animateColorAsState(targetValue = if (isFavourite) Color.Transparent else MaterialTheme.colorScheme.onSurface, animationSpec = AnimationDefaults.stateAnimationSpec())

    Button(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(containerColor = containerColor, contentColor = MaterialTheme.colorScheme.onSurface),
        border = BorderStroke(width = 1.dp, color = borderColor),
        modifier = Modifier.weight(weight = 1f)
    ) {
        AnimatedContent(
            targetState = isFavourite,
            transitionSpec = { AnimationDefaults.Fade }
        ) {
            Icon(
                painter = painterResource(id = if (it) R.drawable.ic_fa_heart_solid else R.drawable.ic_fa_heart),
                contentDescription = null,
                modifier = Modifier.requiredSize(size = 20.dp)
            )
        }
        Spacer(modifier = Modifier.width(width = 8.dp))
        Text(text = stringResource(id = R.string.favourite))
    }
}

@Composable
fun RowScope.LockButton(
    isLocked: Boolean = false,
    onClick: () -> Unit = {}
) {

    val containerColor by animateColorAsState(targetValue = if (isLocked) Color.LockClosedContainer.copy(alpha = 0.71f) else Color.Transparent, animationSpec = AnimationDefaults.stateAnimationSpec())
    val borderColor by animateColorAsState(targetValue = if (isLocked) Color.Transparent else MaterialTheme.colorScheme.onSurface, animationSpec = AnimationDefaults.stateAnimationSpec())

    Button(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(containerColor = containerColor, contentColor = MaterialTheme.colorScheme.onSurface),
        border = BorderStroke(width = 1.dp, color = borderColor),
        modifier = Modifier.weight(weight = 1f)
    ) {
        AnimatedContent(
            targetState = isLocked,
            transitionSpec = { AnimationDefaults.Fade }
        ) {
            Icon(
                painter = painterResource(id = if (it) R.drawable.ic_fa_lock_closed_solid else R.drawable.ic_fa_lock_opened),
                contentDescription = null,
                modifier = Modifier.requiredSize(size = 20.dp)
            )
        }
        Spacer(modifier = Modifier.width(width = 8.dp))
        AnimatedContent(
            targetState = isLocked,
            transitionSpec = { AnimationDefaults.Fade }
        ) {
            Text(text = stringResource(id = if (it) R.string.locked else R.string.not_locked))
        }
    }
}

