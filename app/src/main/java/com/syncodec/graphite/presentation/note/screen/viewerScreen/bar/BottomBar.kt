package com.syncodec.graphite.presentation.note.screen.viewerScreen.bar

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.BottomAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.notification.NotePinNotification
import com.syncodec.graphite.presentation.common.button.MenuButton
import com.syncodec.graphite.presentation.common.button.MenuButtonDefaults
import com.syncodec.graphite.utils.AuthenticatorScreen
import com.syncodec.graphite.utils.LocalAuthenticatorAction
import com.syncodec.graphite.utils.LocalIsAuthenticated
import io.realm.kotlin.types.RealmUUID


@Preview
@Composable
fun BottomBar(
	isOperationPending : Boolean = false,
	noteId : RealmUUID? = null,
	onClickMetadata : () -> Unit = {},
	onClickPin : () -> Unit = {},
	onClickShare : () -> Unit = {},
	onClickEditNote : () -> Unit = {}
) {
	val context = LocalContext.current

	val isAuthenticated = LocalIsAuthenticated.current
	val authenticatorAction = LocalAuthenticatorAction.current

	BottomAppBar(
		modifier = Modifier.fillMaxWidth(),
		tonalElevation = 8.dp,
	) {
		Spacer(modifier = Modifier.width(12.dp))
		MenuButton(
			icon = R.drawable.ic_info,
			tooltip = "Info",
			colors = MenuButtonDefaults.menuButtonColorsOnSurface(),
			onClick = onClickMetadata
		)

		MenuButton(
			icon = R.drawable.ic_pin,
			tooltip = "Pin Note in Notification",
			checked = NotePinNotification.isNotificationPinned(context, noteId),
			colors = MenuButtonDefaults.menuButtonColorsOnSurface(),
			onClick = onClickPin
		)

		MenuButton(
			icon = R.drawable.ic_share,
			tooltip = "Share Note",
			colors = MenuButtonDefaults.menuButtonColorsOnSurface(),
			onClick = onClickShare
		)

		Spacer(modifier = Modifier.weight(1f))

		MenuButton(
			icon = R.drawable.ic_vault,
			tooltip = "Vault",
			checked = isAuthenticated,
			colors = MenuButtonDefaults.menuButtonColorsOnSurface(),
		) { authenticatorAction(AuthenticatorScreen.Authenticate) }

		MenuButton(
			icon = R.drawable.ic_pencil,
			tooltip = "Edit Note",
			colors = MenuButtonDefaults.menuButtonColorsOnSurface(),
			onClick = onClickEditNote
		)
		Spacer(modifier = Modifier.width(12.dp))
	}
}
