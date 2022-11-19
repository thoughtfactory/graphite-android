package com.syncodec.graphite.presentation.ui.authentication

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.button.MenuButton
import com.syncodec.graphite.presentation.ui.authentication.buildingBlock.PasscodeNumPad


@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun AuthenticatorScreen(
	noTry : Int,
	onAuthenticate : (String) -> Unit,
	onClose : () -> Unit
) {
	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		modifier = Modifier
			.fillMaxSize()
			.background(MaterialTheme.colorScheme.background)
	) {
		TopAppBar(
			modifier = Modifier.fillMaxWidth(),
			navigationIcon = {
				MenuButton(
					icon = R.drawable.ic_close,
					tint = MaterialTheme.colorScheme.onBackground,
					onClick = onClose
				)
			},
			title = {},
			colors = TopAppBarDefaults.smallTopAppBarColors(
				containerColor = MaterialTheme.colorScheme.background,
				navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
				titleContentColor = MaterialTheme.colorScheme.onBackground,
			)
		)

		Image(
			painter = painterResource(id = R.drawable.il_open_vault),
			contentDescription = "Add Passcode",
			modifier = Modifier.weight(1f)
		)

		Text(
			text = "Open Vault",
			style = MaterialTheme.typography.titleMedium,
			color = MaterialTheme.colorScheme.onBackground,
			fontWeight = FontWeight.Bold,
			modifier = Modifier.padding(24.dp)
		)

		Spacer(modifier = Modifier.height(24.dp))

		AnimatedContent(
			targetState = noTry,
			transitionSpec = { fadeIn(tween(300)) with fadeOut(tween(300)) },
		) {
			if (it == 0) {
				Text(
					text = "Enter passcode",
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.onBackground,
				)
			} else {
				Text(
					text = "Wrong passcode, please try again",
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.error,
				)
			}
		}


		Spacer(modifier = Modifier.height(16.dp))

		PasscodeNumPad(onEnter = onAuthenticate)
	}
}
