package com.syncodec.graphite.presentation.common.v2.dialog2

import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color


object GenericDialogActionButton {

    @Composable
    fun PrimaryButton(
        modifier: Modifier = Modifier,
        text: String,
        onClick: () -> Unit
    ) {
        Button(
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer),
            shape = MaterialTheme.shapes.medium,
            modifier = modifier,
            onClick = onClick
        ) {
            Text(text = text)
        }
    }

    @Composable
    fun SecondaryButton(
        modifier: Modifier = Modifier,
        text: String,
        onClick: () -> Unit
    ) {
        Button (
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = MaterialTheme.colorScheme.onBackground),
            shape = MaterialTheme.shapes.medium,
            modifier = modifier,
            onClick = onClick
        ) {
            Text(text = text)
        }
    }

    @Composable
    fun RedButton(
        modifier: Modifier = Modifier,
        text: String,
        onClick: () -> Unit
    ) {
        Button(
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer),
            shape = MaterialTheme.shapes.medium,
            modifier = modifier,
            onClick = onClick
        ) {
            Text(text = text)
        }
    }


}
