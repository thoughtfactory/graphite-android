package com.syncodec.graphite.presentation.calendar.composable.bar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.ChapterObjectLite
import com.syncodec.graphite.presentation.common.LocalCompositionIsSelected
import com.syncodec.graphite.presentation.common.LocalCompositionOpenDialog
import com.syncodec.graphite.presentation.common.button.MenuButton
import com.syncodec.graphite.presentation.common.dialog.DialogType
import com.syncodec.graphite.utils.Authenticator
import com.syncodec.graphite.utils.LocalAuthenticatorAction
import com.syncodec.graphite.utils.LocalVaultIsOpened
import com.syncodec.graphite.utils.tone


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomBar(
	parentChapter: ChapterObjectLite?
) {

	val isVaultOpened = LocalVaultIsOpened.current
	val onAuthenticatorAction = LocalAuthenticatorAction.current

	val isSelected = LocalCompositionIsSelected.current

	val openDialog = LocalCompositionOpenDialog.current

	val containerColor = MaterialTheme.colorScheme.surface.tone(isSystemInDarkTheme(), 1)
	val contentColor = MaterialTheme.colorScheme.onSurface.tone(isSystemInDarkTheme(), 1)

	AnimatedVisibility(
		visible = !isSelected,
//		enter = slideInVertically(animationSpec = tween(300), initialOffsetY = { it }),
		enter = expandVertically(tween(300)),
//		exit = slideOutVertically(animationSpec = tween(300), targetOffsetY = { it }),
		exit = shrinkVertically(tween(300)),
	) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier
				.fillMaxWidth()
				.height(80.dp)
				.background(containerColor)
		) {
			Spacer(modifier = Modifier.width(16.dp))

			Text(
				text = "Searching in",
				style = MaterialTheme.typography.bodyLarge,
				color = contentColor,
				fontWeight = FontWeight.Bold
			)

			Spacer(modifier = Modifier.width(16.dp))

			SuggestionChip(
				label = {
					Text(
						text = if (parentChapter == null) "Everywhere" else parentChapter.title ?: parentChapter.id.toString(),
						style = MaterialTheme.typography.bodyMedium,
						fontWeight = FontWeight.Bold,
						modifier = Modifier,
					)
				},
				shape = MaterialTheme.shapes.medium,
				colors = SuggestionChipDefaults.suggestionChipColors(
					containerColor = MaterialTheme.colorScheme.primary,
					labelColor = MaterialTheme.colorScheme.onPrimary,
				),
				onClick = { openDialog(DialogType.WHERE) },
			)

			Spacer(modifier = Modifier.weight(1f))

			MenuButton(
				icon = R.drawable.ic_vault,
				contentDescription = "Vault",
				tint = if (isVaultOpened) MaterialTheme.colorScheme.onBackground else contentColor,
				containerColor = if (isVaultOpened) MaterialTheme.colorScheme.background else Color.Transparent
			) { onAuthenticatorAction(Authenticator.AUTHENTICATE) }

			Spacer(modifier = Modifier.width(16.dp))
		}
	}
}
