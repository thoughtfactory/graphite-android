package com.syncodec.graphite.presentation.bucketItem.composable.bar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.BottomAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.button.GenericButton
import com.syncodec.graphite.presentation.common.button.GenericButtonDefaults
import com.syncodec.graphite.utils.AuthenticatorScreen
import com.syncodec.graphite.utils.LocalAuthenticatorAction
import com.syncodec.graphite.utils.LocalIsAuthenticated


@Composable
fun BottomBar(
	isSaved : Boolean = false,
	onClickShare : () -> Unit,
	onClickDelete : () -> Unit,
	onClickAddReminder : () -> Unit,
) {
	val isAuthenticated = LocalIsAuthenticated.current
	val onAuthenticationAction = LocalAuthenticatorAction.current

	BottomAppBar(
		modifier = Modifier.fillMaxWidth(),
		tonalElevation = 8.dp,
	) {
		Spacer(modifier = Modifier.width(12.dp))

		GenericButton(
			icon = R.drawable.ic_share,
			tooltip = "Share",
			colors = GenericButtonDefaults.genericButtonColorsOnSurface(),
			onClick = onClickShare,
		)

		AnimatedVisibility(
			visible = isSaved,
			enter = scaleIn(tween(300)) + fadeIn(tween(300)),
			exit = scaleOut(tween(300)) + fadeOut(tween(300)),
		) {
			GenericButton(
				icon = R.drawable.ic_delete,
				tooltip = "Delete",
				colors = GenericButtonDefaults.deleteButtonColors(),
				onClick = onClickDelete,
			)
		}

		Spacer(modifier = Modifier.weight(1f))

		Spacer(modifier = Modifier.width(16.dp))

		GenericButton(
			icon = R.drawable.ic_vault,
			tooltip = "Vault",
			checked = isAuthenticated,
			colors = GenericButtonDefaults.genericButtonColorsOnSurface(),
			onClick = { onAuthenticationAction(AuthenticatorScreen.Authenticate) },
		)

		GenericButton(
			icon = R.drawable.ic_clock,
			tooltip = "Add reminder",
			colors = GenericButtonDefaults.genericButtonColorsOnSurface(),
			onClick = onClickAddReminder,
		)

		Spacer(modifier = Modifier.width(12.dp))
	}
}
