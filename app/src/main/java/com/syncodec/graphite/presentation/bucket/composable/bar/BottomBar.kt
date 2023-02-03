package com.syncodec.graphite.presentation.bucket.composable.bar

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.bucket.composable.LocalCompositionOpenBottomSheet
import com.syncodec.graphite.presentation.bucket.composable.bottomSheet.BucketBottomSheetType
import com.syncodec.graphite.presentation.common.button.MenuButton
import com.syncodec.graphite.presentation.common.button.MenuButtonDefaults
import com.syncodec.graphite.utils.AuthenticatorScreen
import com.syncodec.graphite.utils.LocalAuthenticatorAction
import com.syncodec.graphite.utils.LocalIsAuthenticated
import com.syncodec.graphite.utils.tone


@Preview
@Composable
fun BottomBar() {
	val containerColor = MaterialTheme.colorScheme.surface.tone(isSystemInDarkTheme(), 1)
	val contentColor = MaterialTheme.colorScheme.onSurface.tone(isSystemInDarkTheme(), 1)

	val openSheet = LocalCompositionOpenBottomSheet.current

	val isAuthenticated = LocalIsAuthenticated.current
	val onAuthenticatorAction = LocalAuthenticatorAction.current

	Row(
		verticalAlignment = Alignment.CenterVertically,
		modifier = Modifier
			.fillMaxWidth()
			.height(80.dp)
			.background(containerColor)
	) {
		Spacer(modifier = Modifier.width(16.dp))

//			MenuButton(
//				icon = R.drawable.ic_share,
//				contentDescription = "Share bucket",
//				tint = contentColor
//			) { onShare() }

		Spacer(modifier = Modifier.weight(1f))

		Spacer(modifier = Modifier.width(16.dp))

		MenuButton(
			icon = R.drawable.ic_vault,
			tooltip = "Vault",
			colors = MenuButtonDefaults.menuButtonColorsOnSurface(),
			checked = isAuthenticated,
		) { onAuthenticatorAction(AuthenticatorScreen.Authenticate) }

		MenuButton(
			icon = R.drawable.ic_menu,
			tooltip = "Menu"
		) {
			openSheet(BucketBottomSheetType.MENU)
		}

		Spacer(modifier = Modifier.width(16.dp))
	}
}
