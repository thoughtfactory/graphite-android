package com.syncodec.graphite.presentation.ui.authentication

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.button.MenuButton
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

	var addPasscodeState by remember { mutableStateOf(AddPasscodeState.ENTER_PASSCODE) }
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

		Spacer(modifier = Modifier.weight(1f))

		Icon(
			painter = painterResource(id = R.drawable.il_vault),
			contentDescription = "Add Passcode",
			tint = Color.Companion.Unspecified,
			modifier = Modifier
				.fillMaxWidth(0.71f)
				.padding(24.dp)
		)

		Spacer(modifier = Modifier.weight(1f))

		AnimatedContent(
			targetState = addPasscodeState
		) {
			when(it) {
				AddPasscodeState.ENTER_PASSCODE -> {
					Text(
						text = "Enter a passcode",
						style = MaterialTheme.typography.titleMedium,
						color = MaterialTheme.colorScheme.onBackground,
						modifier = Modifier.padding(24.dp)
					)
				}
				AddPasscodeState.CONFIRM_PASSCODE -> {
					Text(
						text = "Confirm your passcode",
						style = MaterialTheme.typography.titleMedium,
						color = MaterialTheme.colorScheme.onBackground,
						modifier = Modifier.padding(24.dp)
					)
				}
				AddPasscodeState.WRONG_PASSCODE -> {
					Text(
						text = "Wrong passcode",
						style = MaterialTheme.typography.titleMedium,
						color = MaterialTheme.colorScheme.onBackground,
						modifier = Modifier.padding(24.dp)
					)
				}
			}
		}

		Spacer(modifier = Modifier.height(24.dp))

		PasscodeNumPad {
			when(addPasscodeState) {
				AddPasscodeState.ENTER_PASSCODE -> {
					passcode1 = it
					addPasscodeState = AddPasscodeState.CONFIRM_PASSCODE
				}
				AddPasscodeState.CONFIRM_PASSCODE -> {
					passcode2 = it
					if (passcode1 == passcode2) {
						onPasscodeAdded(passcode1)
					} else {
						addPasscodeState = AddPasscodeState.WRONG_PASSCODE
					}
				}
				AddPasscodeState.WRONG_PASSCODE -> {
					passcode1 = it
					addPasscodeState = AddPasscodeState.CONFIRM_PASSCODE
				}
			}
		}

		Spacer(modifier = Modifier.height(24.dp))
	}
}
