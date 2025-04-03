package com.syncodec.graphite.presentation.ui.authenticator2

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R


private enum class PasscodeState {
    Passcode1,
    Passcode2,
    IsError
}

@Composable
fun AddPasscodeScreen(
    onAddPasscode: (String) -> Unit = {}
) {

    var passcodeState: PasscodeState by remember { mutableStateOf(value = PasscodeState.Passcode1) }

    var passcode1 by remember { mutableStateOf(value = "") }
    var passcode2 by remember { mutableStateOf(value = "") }

    fun onClickKey(keyString: String) {
        when (passcodeState) {
            PasscodeState.Passcode1 -> passcode1 += keyString
            PasscodeState.Passcode2 -> passcode2 += keyString
            PasscodeState.IsError -> passcode1 += keyString
        }
        passcode1 = passcode1.take(4)
        passcode2 = passcode2.take(4)
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .weight(weight = 1f)
        ) {
            Spacer(modifier = Modifier.weight(weight = 1f))
            Image(
                painter = painterResource(id = R.drawable.il_mm_lock_dark),
                contentDescription = null,
                modifier = Modifier.fillMaxWidth(fraction = 0.71f)
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceAround,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(weight = 1f)
            ) {
                StatusTextView(passcodeState = passcodeState)
                PasscodeStatusView(
                    passcodeState = passcodeState,
                    passcode1 = passcode1,
                    passcode2 = passcode2,
                )
            }
        }

        KeypadSurface(
            onClickKey = ::onClickKey,
            onClickBackspace = {
                when (passcodeState) {
                    PasscodeState.Passcode1 -> passcode1 = passcode1.dropLast(1)
                    PasscodeState.Passcode2 -> passcode2 = passcode2.dropLast(1)
                    PasscodeState.IsError -> passcode1 = passcode1.dropLast(1)
                }
            },
            onClickOk = {
                when (passcodeState) {
                    PasscodeState.Passcode1 -> if (passcode1.length == 4) passcodeState = PasscodeState.Passcode2
                    PasscodeState.Passcode2 -> if (passcode1 == passcode2) onAddPasscode(passcode1) else {
                        passcodeState = PasscodeState.IsError
                        passcode1 = ""
                        passcode2 = ""
                    }
                    PasscodeState.IsError -> if (passcode1.length == 4) passcodeState = PasscodeState.Passcode2
                }
            }
        )
    }
}

@Composable
private fun StatusTextView(
    passcodeState: PasscodeState
) {
    AnimatedContent(
        modifier = Modifier,
        targetState = passcodeState
    ) { passcodeState1 ->
        Text(
            text = when (passcodeState1) {
                PasscodeState.Passcode1 -> stringResource(id = R.string.add_passcode)
                PasscodeState.Passcode2 -> stringResource(id = R.string.confirm_passcode)
                PasscodeState.IsError -> stringResource(id = R.string.error_confirm_passcode)
            },
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun PasscodeStatusView(
    passcodeState: PasscodeState,
    passcode1: String,
    passcode2: String,
) {
    Row {
        repeat(times = 4) {
            val color by animateColorAsState(
                targetValue = if (
                    when (passcodeState) {
                        PasscodeState.Passcode1 -> passcode1.length > it
                        PasscodeState.Passcode2 -> passcode2.length > it
                        PasscodeState.IsError -> passcode1.length > it
                    }
                ) MaterialTheme.colorScheme.onSurface
                else MaterialTheme.colorScheme.surface
            )
            Box(
                modifier = Modifier
                    .requiredSize(size = 24.dp)
                    .padding(all = 4.dp)
                    .background(color = color, shape = CircleShape)
            )
        }
    }
}
