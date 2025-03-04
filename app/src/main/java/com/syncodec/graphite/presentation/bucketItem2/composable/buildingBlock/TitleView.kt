package com.syncodec.graphite.presentation.bucketItem2.composable.buildingBlock

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.presentation.common.v2.selectable2.SelectableContainer2
import com.syncodec.graphite.presentation.common.v2.selectable2.SelectableContainer2Defaults


@Composable
fun TitleView(
    title: String? = null,
    subTitle: String? = null,
    isEditing: Boolean = false,
    onClick: () -> Unit = {}
) {
    SelectableContainer2(
        enabled = true,
        selected = false,
        shape = MaterialTheme.shapes.large,
        border = BorderStroke(width = 1.dp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.47f)),
        onClick = { if (isEditing) onClick() },
        onLongClick = {},
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                Text(
                    text = title ?: "-",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(height = 8.dp))
                Text(
                    text = subTitle ?: "-",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SelectableContainer2Defaults.defaultColors().onContainerColor.copy(alpha = 0.71f)
                )
            }
        }
    }
}
