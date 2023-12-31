package com.syncodec.graphite.presentation.common.genericBottomSheet2

import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.State
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.presentation.common.button.GenericButtonColors


@Preview
@Composable
fun GenericBottomSheetButton(
	modifier: Modifier = Modifier,
	text: String = "",
	enabled: Boolean = true,
	colors: ButtonColors = GenericBottomSheetButtonDefaults.primaryButtonColors(),
	onClick : () -> Unit = { },
) {
	Button(
		shape = MaterialTheme.shapes.medium,
		colors = colors,
		enabled = enabled,
		modifier = modifier,
		onClick = onClick,
	) {
		Text(text = text)
	}
}
object GenericBottomSheetButtonDefaults {
	@Composable
	fun primaryButtonColors(): ButtonColors = ButtonDefaults.buttonColors(
		containerColor = MaterialTheme.colorScheme.primary,
		contentColor = MaterialTheme.colorScheme.onPrimary,
		disabledContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp).copy(alpha = 0.31f),
		disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.71f),
	)
	@Composable
	fun secondaryButtonColors(): ButtonColors = ButtonDefaults.buttonColors(
		containerColor = MaterialTheme.colorScheme.surface,
		contentColor = MaterialTheme.colorScheme.onSurface,
		disabledContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp).copy(alpha = 0.31f),
		disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.71f),
	)

	@Composable
	fun warningButtonColors(): ButtonColors = ButtonDefaults.buttonColors(
		containerColor = MaterialTheme.colorScheme.errorContainer,
		contentColor = MaterialTheme.colorScheme.onErrorContainer,
		disabledContainerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.42f),
		disabledContentColor = MaterialTheme.colorScheme.onErrorContainer,
	)
}

