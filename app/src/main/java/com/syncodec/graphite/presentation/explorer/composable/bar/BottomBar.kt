package com.syncodec.graphite.presentation.explorer.composable.bar

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.di.model.ChapterObjectLite
import com.syncodec.graphite.presentation.common.animation.AnimatedText
import com.syncodec.graphite.presentation.common.bar.GenericBottomBar
import com.syncodec.graphite.presentation.common.button.VaultButton
import com.syncodec.graphite.presentation.base.ICON_BUTTON_SIZE
import com.syncodec.graphite.utils.getInverseBWColor


@Preview
@Composable
fun BottomBar(
	currentChapter: ChapterObjectLite? = null,
	onClickSearchIn: () -> Unit = {},
) {
	GenericBottomBar {
		SuggestionChip(
			label = {
				AnimatedText(
					text = currentChapter?.let { it.title ?: it.id.toString() } ?: "Everywhere",
					style = MaterialTheme.typography.bodyMedium,
					modifier = Modifier,
				)
			},
			shape = MaterialTheme.shapes.medium,
			colors = SuggestionChipDefaults.suggestionChipColors(
				containerColor = currentChapter?.color?.let { Color(it) } ?: MaterialTheme.colorScheme.primary,
				labelColor = currentChapter?.color?.let { Color(it).getInverseBWColor() } ?: MaterialTheme.colorScheme.onPrimary,
			),
			border = null,
			modifier = Modifier.height((ICON_BUTTON_SIZE * 2) - 2.dp),
			onClick = onClickSearchIn
		)

		Spacer(modifier = Modifier.weight(1f))

		Spacer(modifier = Modifier.width(12.dp))

		VaultButton()
	}
}
