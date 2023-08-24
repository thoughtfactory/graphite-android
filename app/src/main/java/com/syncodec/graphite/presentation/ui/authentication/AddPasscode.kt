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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.button.GenericButton
import com.syncodec.graphite.presentation.ui.authentication.buildingBlock.PasscodeNumPad


enum class AddPasscodeState {
	ENTER_PASSCODE,
	CONFIRM_PASSCODE,
	WRONG_PASSCODE,
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun AddPasscodeScreen(
	onPasscodeAdded : (String) -> Unit,
	onClose : () -> Unit
) {
	var passcodeState by remember { mutableStateOf(AddPasscodeState.ENTER_PASSCODE) }
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
				GenericButton(
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
			text = "Keep your data away from intruders",
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
				AddPasscodeState.ENTER_PASSCODE -> {
					Text(
						text = "Enter new passcode",
						style = MaterialTheme.typography.bodyMedium,
						color = MaterialTheme.colorScheme.onBackground,
					)
				}

				AddPasscodeState.CONFIRM_PASSCODE -> {
					Text(
						text = "Confirm passcode",
						style = MaterialTheme.typography.bodyMedium,
						color = MaterialTheme.colorScheme.onBackground,
						fontWeight = FontWeight.Bold,
					)
				}

				AddPasscodeState.WRONG_PASSCODE -> {
					Text(
						text = "Passcode doesn't match. Enter new passcode",
						style = MaterialTheme.typography.bodyMedium,
						color = MaterialTheme.colorScheme.onBackground,
						fontWeight = FontWeight.Bold,
					)
				}
			}
		}

		Spacer(modifier = Modifier.height(16.dp))

		PasscodeNumPad {
			when (passcodeState) {
				AddPasscodeState.ENTER_PASSCODE -> {
					passcode1 = it
					passcodeState = AddPasscodeState.CONFIRM_PASSCODE
				}

				AddPasscodeState.CONFIRM_PASSCODE -> {
					passcode2 = it
					if (passcode1 == passcode2) onPasscodeAdded(passcode1) else passcodeState = AddPasscodeState.WRONG_PASSCODE
				}

				AddPasscodeState.WRONG_PASSCODE -> {
					passcode1 = it
					passcodeState = AddPasscodeState.CONFIRM_PASSCODE
				}
			}
		}
	}
}
