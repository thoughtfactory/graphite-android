package com.syncodec.momento.custom.button

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.syncodec.momento.miscellaneous.ThemeUtils.Companion.tone


@OptIn(ExperimentalMaterialApi::class, ExperimentalAnimationApi::class,
	ExperimentalMaterial3Api::class
)
@Composable
fun LargeButton(
	text: String,
	enabled: Boolean,
	modifier: Modifier,
	onClick: () -> Unit
) {
	Button(
		onClick = { onClick() },
		colors = ButtonDefaults.buttonColors(
			containerColor = MaterialTheme.colorScheme.primaryContainer,
			contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
			disabledContainerColor = MaterialTheme.colorScheme.surface.tone(isSystemInDarkTheme(), 1),
			disabledContentColor = MaterialTheme.colorScheme.onSurface.tone(isSystemInDarkTheme(), 1)
		),
		enabled = enabled,
		modifier = modifier
	) {
		Text(
			text = text,
			style = MaterialTheme.typography.bodyLarge,
		)
	}
}
