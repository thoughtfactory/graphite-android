package com.syncodec.graphite.presentation.common.button

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp


@Composable
fun WideButton(
    modifier: Modifier = Modifier,
    icon: Int? = null,
    text: String,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    onClick: () -> Unit
) {
    Button(
        modifier = modifier,
        colors = colors,
        shape = MaterialTheme.shapes.medium,
        onClick = onClick
    ) {
        icon?.let {
            Icon(
                painter = painterResource(it),
                contentDescription = text,
                modifier = Modifier.requiredSize(14.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(text = text)
    }
}

@Composable
fun WideTextButton(
    modifier: Modifier = Modifier,
    icon: Int? = null,
    text: String,
    colors: ButtonColors = ButtonDefaults.textButtonColors(),
    onClick: () -> Unit
) {
    TextButton(
        modifier = modifier,
        colors = colors,
        shape = MaterialTheme.shapes.medium,
        onClick = onClick
    ) {
        icon?.let {
            Icon(
                painter = painterResource(it),
                contentDescription = text,
                modifier = Modifier.requiredSize(14.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(text = text)
    }
}

@Composable
fun WideOutlinedButton(
    modifier: Modifier = Modifier,
    icon: Int? = null,
    text: String,
    colors: ButtonColors = ButtonDefaults.outlinedButtonColors(),
    border: BorderStroke? = ButtonDefaults.outlinedButtonBorder,
    onClick: () -> Unit
) {
    OutlinedButton(
        modifier = modifier,
        colors = colors,
        shape = MaterialTheme.shapes.medium,
        border = border,
        onClick = onClick
    ) {
        icon?.let {
            Icon(
                painter = painterResource(it),
                contentDescription = text,
                modifier = Modifier.requiredSize(14.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(text = text)
    }
}

