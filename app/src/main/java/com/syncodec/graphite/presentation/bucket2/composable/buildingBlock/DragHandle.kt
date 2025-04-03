package com.syncodec.graphite.presentation.bucket2.composable.buildingBlock

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.view.HapticFeedbackConstantsCompat
import androidx.core.view.ViewCompat
import com.syncodec.graphite.R
import sh.calvin.reorderable.ReorderableCollectionItemScope


@Composable
fun ReorderableCollectionItemScope.DragHandle(
    modifier: Modifier = Modifier,
    onDragStopped: () -> Unit
) {
    val view = LocalView.current

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size = 32.dp)
            .background(color = MaterialTheme.colorScheme.background.copy(alpha = 0.471f), shape = MaterialTheme.shapes.small)
            .draggableHandle(
                onDragStarted = { ViewCompat.performHapticFeedback(view, HapticFeedbackConstantsCompat.GESTURE_START) },
                onDragStopped = {
                    ViewCompat.performHapticFeedback(view, HapticFeedbackConstantsCompat.GESTURE_END)
                    onDragStopped()
                },
            ),
        content = { Icon(painter = painterResource(id = R.drawable.ic_fa_drag_handle), contentDescription = "Reorder", modifier = Modifier.requiredSize(size = 16.dp)) }
    )
}
