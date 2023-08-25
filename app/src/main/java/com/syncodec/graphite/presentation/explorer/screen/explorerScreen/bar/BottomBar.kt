package com.syncodec.graphite.presentation.explorer.screen.explorerScreen.bar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.di.model.ChapterObjectLite
import com.syncodec.graphite.presentation.common.animation.AnimatedText
import com.syncodec.graphite.presentation.common.button.VaultButton
import com.syncodec.graphite.presentation.ui.IconButtonSize
import com.syncodec.graphite.utils.getInverseBWColor


@Composable
fun BottomBar(
	searchInChapter : ChapterObjectLite? = null,
	onClickSearchIn : () -> Unit = {},
) {
	Column(
		modifier = Modifier.fillMaxWidth()
	) {
		Spacer(
			modifier = Modifier
				.fillMaxWidth()
				.height(1.dp)
				.background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.47f))
		)
		BottomAppBar(
			modifier = Modifier.fillMaxWidth(),
			tonalElevation = 0.dp,
			containerColor = MaterialTheme.colorScheme.background,
			contentColor = MaterialTheme.colorScheme.onBackground
		) {
			Spacer(modifier = Modifier.width(12.dp))

			SuggestionChip(
				label = {
					AnimatedText(
						text = searchInChapter?.let { it.title ?: it.id.toString() } ?: "Everywhere",
						style = MaterialTheme.typography.bodyMedium,
						modifier = Modifier,
					)
				},
				shape = MaterialTheme.shapes.medium,
				colors = SuggestionChipDefaults.suggestionChipColors(
					containerColor = searchInChapter?.color?.let { Color(it) } ?: MaterialTheme.colorScheme.primary,
					labelColor = searchInChapter?.color?.let { Color(it).getInverseBWColor() } ?: MaterialTheme.colorScheme.onPrimary,
				),
				border = null,
				modifier = Modifier.height((IconButtonSize * 2) - 2.dp),
				onClick = onClickSearchIn
			)

			Spacer(modifier = Modifier.weight(1f))

			VaultButton()

			Spacer(modifier = Modifier.width(12.dp))
		}
	}
}
