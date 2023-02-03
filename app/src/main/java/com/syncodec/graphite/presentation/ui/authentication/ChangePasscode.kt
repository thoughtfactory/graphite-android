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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.button.MenuButton
import com.syncodec.graphite.presentation.ui.authentication.buildingBlock.PasscodeNumPad
import com.syncodec.graphite.utils.alice.getSecretData


enum class ChangePasscodeState {
	ENTER_OLD_PASSCODE,
	ENTER_NEW_PASSCODE,
	CONFIRM_NEW_PASSCODE,
	WRONG_PASSCODE,
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun ChangePasscode(
	onPasscodeAdded : (String) -> Unit,
	onClose : () -> Unit
) {
	val context = LocalContext.current
	val oldPasscode = context.getSecretData("passcode").data?.decodeToString()

	var passcodeState by remember { mutableStateOf(ChangePasscodeState.ENTER_OLD_PASSCODE) }
	var passcode1 by remember { mutableStateOf("") }
	var passcode2 by remember { mutableStateOf("") }

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
					onClick = onClose
				)
			},
			title = {},
			colors = TopAppBarDefaults.topAppBarColors(
				containerColor = MaterialTheme.colorScheme.background,
				navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
				titleContentColor = MaterialTheme.colorScheme.onBackground,
			)
		)

		Image(
			painter = painterResource(id = R.drawable.il_new_vault),
			contentDescription = "Add Passcode",
			modifier = Modifier.weight(1f)
		)

		Text(
			text = "Keep your data safe from intruders",
			style = MaterialTheme.typography.titleMedium,
			color = MaterialTheme.colorScheme.onBackground,
			fontWeight = FontWeight.Bold,
			modifier = Modifier.padding(24.dp)
		)

		Spacer(modifier = Modifier.height(24.dp))

		AnimatedContent(
			targetState = passcodeState,
			transitionSpec = { fadeIn(tween(300)) with fadeOut(tween(300)) },
		) {
			when (it) {
				ChangePasscodeState.ENTER_OLD_PASSCODE -> {
					Text(
						text = "Enter old passcode",
						style = MaterialTheme.typography.bodyMedium,
						color = MaterialTheme.colorScheme.onBackground,
					)
				}

				ChangePasscodeState.ENTER_NEW_PASSCODE -> {
					Text(
						text = "Enter new passcode",
						style = MaterialTheme.typography.bodyMedium,
						color = MaterialTheme.colorScheme.onBackground,
						fontWeight = FontWeight.Bold,
					)
				}

				ChangePasscodeState.CONFIRM_NEW_PASSCODE -> {
					Text(
						text = "Confirm new passcode",
						style = MaterialTheme.typography.bodyMedium,
						color = MaterialTheme.colorScheme.onBackground,
						fontWeight = FontWeight.Bold,
					)
				}

				ChangePasscodeState.WRONG_PASSCODE -> {
					Text(
						text = "Passcode doesn't match. Enter old passcode",
						style = MaterialTheme.typography.bodyMedium,
						color = MaterialTheme.colorScheme.error,
						fontWeight = FontWeight.Bold,
					)
				}
			}
		}

		Spacer(modifier = Modifier.height(16.dp))

		PasscodeNumPad {
			when (passcodeState) {
				ChangePasscodeState.ENTER_OLD_PASSCODE -> passcodeState =
					if (oldPasscode == it) ChangePasscodeState.ENTER_NEW_PASSCODE else ChangePasscodeState.WRONG_PASSCODE

				ChangePasscodeState.ENTER_NEW_PASSCODE -> {
					passcode1 = it
					passcodeState = ChangePasscodeState.CONFIRM_NEW_PASSCODE
				}

				ChangePasscodeState.CONFIRM_NEW_PASSCODE -> {
					passcode2 = it
					if (passcode1 == passcode2) onPasscodeAdded(passcode1) else passcodeState = ChangePasscodeState.WRONG_PASSCODE
				}

				ChangePasscodeState.WRONG_PASSCODE -> passcodeState =
					if (oldPasscode == it) ChangePasscodeState.ENTER_NEW_PASSCODE else ChangePasscodeState.WRONG_PASSCODE
			}
		}
	}
}
