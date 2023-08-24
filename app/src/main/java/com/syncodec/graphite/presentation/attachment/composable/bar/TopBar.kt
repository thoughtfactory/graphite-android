package com.syncodec.graphite.presentation.attachment.composable.bar

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.animation.AnimatedText
import com.syncodec.graphite.presentation.common.button.GenericButton
import com.syncodec.graphite.presentation.common.button.GenericButtonDefaults
import com.syncodec.graphite.utils.AuthenticatorScreen
import com.syncodec.graphite.utils.LocalAuthenticatorAction
import com.syncodec.graphite.utils.LocalIsAuthenticated


@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun TopBar(
	isSelecting : Boolean = false,
	selectedSize : Int = 0,
	onClickCancelSelect : () -> Unit = {},
	onClickDelete : () -> Unit = {},
) {
	val isAuthenticated = LocalIsAuthenticated.current
	val onAuthenticationAction = LocalAuthenticatorAction.current

	val onBackPressedDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

	Crossfade(
		targetState = isSelecting,
		animationSpec = tween(300)
	) {
		if (it) {
			TopAppBar(
				navigationIcon = {
					GenericButton(
						icon = R.drawable.ic_close,
						onClick = onClickCancelSelect,
					)
				},
				title = {
					AnimatedText(
						text = if (selectedSize == 0) "No items selected" else if (selectedSize == 1) "1 item selected" else "${selectedSize} items selected",
						color = MaterialTheme.colorScheme.onBackground,
					)
				},
				actions = {
					GenericButton(
						icon = R.drawable.ic_delete,
						colors = GenericButtonDefaults.deleteButtonColors(),
						onClick = onClickDelete
					)
				},
				colors = TopAppBarDefaults.topAppBarColors(
					containerColor = MaterialTheme.colorScheme.background,
					navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
					actionIconContentColor = MaterialTheme.colorScheme.onBackground,
				),
			)
		} else {
			TopAppBar(
				navigationIcon = {
					GenericButton(
						icon = R.drawable.ic_back,
						onClick = { onBackPressedDispatcher?.onBackPressed() },
					)
				},
				title = {
					Text(
						text = "Attachment",
						color = MaterialTheme.colorScheme.onBackground,
						fontWeight = FontWeight.Bold
					)
				},
				actions = {
					GenericButton(
						icon = R.drawable.ic_vault,
						tooltip = "Vault",
						checked = isAuthenticated,
						colors = GenericButtonDefaults.genericButtonColors(),
					) { onAuthenticationAction(AuthenticatorScreen.Authenticate) }
				},
				modifier = Modifier.fillMaxWidth(),
				colors = TopAppBarDefaults.topAppBarColors(
					containerColor = MaterialTheme.colorScheme.background,
					navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
					actionIconContentColor = MaterialTheme.colorScheme.onBackground,
				)
			)
		}
	}
}
