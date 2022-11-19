package com.syncodec.graphite.presentation.search.composable.bar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.LocalCompositionIsSelected
import com.syncodec.graphite.presentation.common.button.MenuButton
import com.syncodec.graphite.presentation.search.SearchActivity
import com.syncodec.graphite.presentation.search.composable.dialog.SearchDialogType
import com.syncodec.graphite.utils.Authenticator
import com.syncodec.graphite.utils.LocalAuthenticatorAction
import com.syncodec.graphite.utils.LocalVaultIsOpened
import com.syncodec.graphite.utils.tone


@Composable
fun BottomBar() {

	val isVaultOpened = LocalVaultIsOpened.current
	val onAuthenticatorAction = LocalAuthenticatorAction.current

	val containerColor = MaterialTheme.colorScheme.surface.tone(isSystemInDarkTheme(), 1)
	val contentColor = MaterialTheme.colorScheme.onSurface.tone(isSystemInDarkTheme(), 1)

	val parentChapter = SearchActivity.parentChapter.current

	val openDialog = SearchActivity.openDialog.current

	val isSelected = LocalCompositionIsSelected.current

	AnimatedVisibility(
		visible = !isSelected,
		enter = slideInVertically(animationSpec = tween(300), initialOffsetY = { it }),
		exit = slideOutVertically(animationSpec = tween(300), targetOffsetY = { it }),
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
			)

			Spacer(modifier = Modifier.width(16.dp))

			Button(
				shape = RoundedCornerShape(12.dp),
				onClick = { openDialog(SearchDialogType.WHERE) },
			) {
				Text(text = if (parentChapter == null) "Everywhere" else parentChapter.title ?: parentChapter.id.toString())
			}

			Spacer(modifier = Modifier.weight(1f))

			MenuButton(
				icon = R.drawable.ic_vault,
				contentDescription = "Vault",
				tint = if (isVaultOpened) MaterialTheme.colorScheme.onBackground else contentColor,
				containerColor = if (isVaultOpened) MaterialTheme.colorScheme.background else Color.Companion.Transparent
			) { onAuthenticatorAction(Authenticator.AUTHENTICATE) }

			Spacer(modifier = Modifier.width(16.dp))
		}
	}
}
