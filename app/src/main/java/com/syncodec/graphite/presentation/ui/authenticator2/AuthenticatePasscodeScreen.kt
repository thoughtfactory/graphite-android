package com.syncodec.graphite.presentation.ui.authenticator2

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R


@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun AuthenticatePasscodeScreen(
    currentPasscode: String,
    onConfirmPasscode: () -> Unit = {}
) {
    val context = LocalContext.current

    var noTry by remember { mutableIntStateOf(value = 0) }
    var passcode by remember { mutableStateOf(value = "") }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(weight = 1f)
        ) {
            Image(
                painter = painterResource(id = R.drawable.il_mm_lock_dark),
                contentDescription = null,
                modifier = Modifier.fillMaxWidth(fraction = 0.71f)
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(weight = 1f)
            ) {
                StatusTextView(noTry = noTry)
                Spacer(modifier = Modifier.height(height = 16.dp))
                PasscodeStatusView(passcode = passcode)
            }
        }

        KeypadSurface(
            onClickKey = { passcode += it; passcode = passcode.take(4) },
            onClickBackspace = { passcode = passcode.dropLast(1) },
            onClickOk = {
                if (passcode == currentPasscode) onConfirmPasscode()
                else {
                    noTry += 1
                    passcode = ""
                }
            }
        )
    }
}

@Composable
private fun StatusTextView(
    noTry: Int
) {
    AnimatedContent(
        modifier = Modifier,
        targetState = noTry
    ) { noTry1 ->
        Text(
            text = if (noTry1 == 0) stringResource(id = R.string.enter_passcode) else stringResource(id = R.string.passcode_incorrect) + " $noTry1",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun PasscodeStatusView(
    passcode: String,
) {
    Row {
        repeat(times = 4) {
            val color by animateColorAsState(
                targetValue = if (passcode.length > it) MaterialTheme.colorScheme.onSurface
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
