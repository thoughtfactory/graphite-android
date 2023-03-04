package com.syncodec.graphite.presentation.explorer.screen.explorerScreen.bar

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.ChapterObjectLite
import com.syncodec.graphite.presentation.common.animation.AnimatedText
import com.syncodec.graphite.presentation.common.button.MenuButton
import com.syncodec.graphite.presentation.common.button.MenuButtonDefaults
import com.syncodec.graphite.presentation.ui.IconButtonSize
import com.syncodec.graphite.utils.AuthenticatorScreen
import com.syncodec.graphite.utils.LocalAuthenticatorAction
import com.syncodec.graphite.utils.LocalIsAuthenticated
import com.syncodec.graphite.utils.getInverseBWColor


@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun BottomBar(
	searchInChapter : ChapterObjectLite? = null,
	onClickSearchIn : () -> Unit = {},
) {

	val isAuthenticated = LocalIsAuthenticated.current
	val onAuthenticationAction = LocalAuthenticatorAction.current

	BottomAppBar(
		modifier = Modifier.fillMaxWidth(),
		tonalElevation = 8.dp,
	) {
		Spacer(modifier = Modifier.width(12.dp))

		Text(
			text = "Searching in",
			style = MaterialTheme.typography.bodyLarge,
			color = MaterialTheme.colorScheme.onBackground,
		)
		Spacer(modifier = Modifier.width(8.dp))

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

		MenuButton(
			icon = R.drawable.ic_vault,
			tooltip = "Vault",
			checked = isAuthenticated,
			colors = MenuButtonDefaults.menuButtonColorsOnSurface(),
		) { onAuthenticationAction(AuthenticatorScreen.Authenticate) }

		Spacer(modifier = Modifier.width(12.dp))
	}
}
