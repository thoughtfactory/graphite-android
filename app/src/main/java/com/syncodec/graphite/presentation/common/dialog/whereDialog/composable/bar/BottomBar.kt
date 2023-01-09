package com.syncodec.graphite.presentation.common.dialog.whereDialog.composable.bar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.button.MenuButton
import com.syncodec.graphite.utils.Authenticator
import com.syncodec.graphite.utils.LocalAuthenticatorAction
import com.syncodec.graphite.utils.LocalVaultIsOpened
import com.syncodec.graphite.utils.tone


@Preview
@Composable
fun BottomBar(
	onMove : () -> Unit = {},
) {
	val isVaultOpened = LocalVaultIsOpened.current
	val onAuthenticatorAction = LocalAuthenticatorAction.current

	Row(
		verticalAlignment = Alignment.CenterVertically,
		modifier = Modifier
			.fillMaxWidth()
			.height(80.dp)
			.background(MaterialTheme.colorScheme.surface.tone(isSystemInDarkTheme(), 1)),
	) {
		Spacer(modifier = Modifier.width(16.dp))
		Row(
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier
				.height(44.dp)
				.weight(1f)
				.background(MaterialTheme.colorScheme.background, MaterialTheme.shapes.medium)
				.clip(MaterialTheme.shapes.medium)
				.clickable { onMove() }
		) {
			Spacer(modifier = Modifier.width(16.dp))
			Text(
				text = "Select",
				style = MaterialTheme.typography.bodyMedium,
				fontWeight = FontWeight.Normal
			)
			Spacer(modifier = Modifier.width(16.dp))
		}
		Spacer(modifier = Modifier.width(48.dp))
		MenuButton(
			icon = R.drawable.ic_vault,
			contentDescription = "Vault",
			tint = if (isVaultOpened) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurface,
			containerColor = if (isVaultOpened) MaterialTheme.colorScheme.background else Color.Transparent
		) { onAuthenticatorAction(Authenticator.AUTHENTICATE) }
		Spacer(modifier = Modifier.width(16.dp))
	}
}
