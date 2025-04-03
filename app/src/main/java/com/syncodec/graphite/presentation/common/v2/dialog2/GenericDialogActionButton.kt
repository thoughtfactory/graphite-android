package com.syncodec.graphite.presentation.common.v2.dialog2

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp


object GenericDialogActionButton {

    @Composable
    fun RowScope.PrimaryButton(
        modifier: Modifier = Modifier,
        text: String,
        onClick: () -> Unit
    ) {
        Button(
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer),
            shape = MaterialTheme.shapes.medium,
            onClick = onClick,
            modifier = modifier
                .weight(weight = 1f)
                .padding(horizontal = 2.dp)
        ) {
            Text(text = text)
        }
    }

    @Composable
    fun RowScope.SecondaryButton(
        modifier: Modifier = Modifier,
        text: String,
        onClick: () -> Unit
    ) {
        Button (
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = MaterialTheme.colorScheme.onBackground),
            shape = MaterialTheme.shapes.medium,
            onClick = onClick,
            modifier = modifier
                .weight(weight = 1f)
                .padding(horizontal = 2.dp)
        ) {
            Text(text = text)
        }
    }

    @Composable
    fun RowScope.RedButton(
        modifier: Modifier = Modifier,
        text: String,
        onClick: () -> Unit
    ) {
        Button(
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer),
            shape = MaterialTheme.shapes.medium,
            onClick = onClick,
            modifier = modifier
                .weight(weight = 1f)
                .padding(horizontal = 2.dp)
        ) {
            Text(text = text)
        }
    }
}
