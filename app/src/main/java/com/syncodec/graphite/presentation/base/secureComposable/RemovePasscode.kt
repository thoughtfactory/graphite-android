package com.syncodec.graphite.presentation.base.secureComposable

import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.base.secureComposable.buildingBlock.PasscodeNumPad
import com.syncodec.graphite.presentation.common.button.CancelButton
import com.syncodec.graphite.utils.alice.AliceRequest2
import com.syncodec.graphite.utils.alice.deleteSecretData
import com.syncodec.graphite.utils.alice.getSecretData2
import com.syncodec.graphite.utils.alice.putSecretData
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.Instant


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemovePasscode(
	onClose : () -> Unit,
) {
	val context = LocalContext.current

	PreAuthenticator {  attemptCount, recalculateFailedCounter ->
		Column(
			horizontalAlignment = Alignment.CenterHorizontally,
			modifier = Modifier
				.fillMaxSize()
				.background(MaterialTheme.colorScheme.background)
		) {
			TopAppBar(
				modifier = Modifier.fillMaxWidth(),
				navigationIcon = { CancelButton(onClick = onClose) },
				title = {},
				colors = TopAppBarDefaults.topAppBarColors(
					containerColor = MaterialTheme.colorScheme.background,
					navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
					titleContentColor = MaterialTheme.colorScheme.onBackground,
				)
			)

			Image(
				painter = painterResource(id = R.drawable.il_open_vault),
				contentDescription = "Remove Passcode",
				modifier = Modifier.weight(1f)
			)

			Text(
				text = "Remove Vault Passcode",
				style = MaterialTheme.typography.titleMedium,
				fontWeight = FontWeight.Bold,
				modifier = Modifier.padding(24.dp)
			)

			Spacer(modifier = Modifier.height(24.dp))

			AnimatedContent(
				targetState = attemptCount,
				transitionSpec = { fadeIn(tween(470)) togetherWith fadeOut(tween(470)) },
				label = "retry_animation"
			) { attemptCount1 ->
				Text(
					text = if (attemptCount1 == null || attemptCount1%3 == 0) "Enter passcode" else "Wrong passcode, please try again",
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.onBackground,
				)
			}

			Spacer(modifier = Modifier.height(16.dp))

			PasscodeNumPad {enteredPasscode ->
				val lastFailedTimeRequest = context.getSecretData2("lastFailureTimeAttempt")
				when (val passcode = context.getSecretData2("passcode")) {
					is AliceRequest2.Success -> {
						when {
							passcode.data.decodeToString() == enteredPasscode -> {
								Log.d("npr71", "correct passcode")
								context.deleteSecretData("passcode")
								context.deleteSecretData("lastFailureTimeAttempt")
								Toast.makeText(context, "Passcode removed", Toast.LENGTH_SHORT).show()
								onClose()
							}

							lastFailedTimeRequest is AliceRequest2.Success -> {
								Log.d("npr71", "wrong passcode : third strike")
								try {
									val lastFailedAttempt = Json.decodeFromString<LastFailedAttempt>(lastFailedTimeRequest.data.decodeToString())

									val nextAttemptAt = when {
										lastFailedAttempt.attemptCount in 0..1 -> Instant.now().toEpochMilli()
										lastFailedAttempt.attemptCount == 2 -> Instant.now().toEpochMilli() + 30_000
										lastFailedAttempt.attemptCount == 5 -> Instant.now().toEpochMilli() + 60_000
										lastFailedAttempt.attemptCount == 8 -> Instant.now().toEpochMilli() + 300_000
										lastFailedAttempt.attemptCount > 8 -> Instant.now().toEpochMilli() + 600_000
										else -> Instant.now().toEpochMilli()
									}

									context.putSecretData("lastFailureTimeAttempt", Json.encodeToString(LastFailedAttempt(nextAttemptAt = nextAttemptAt, attemptCount = lastFailedAttempt.attemptCount + 1)))
									recalculateFailedCounter()
								} catch (_: Exception) {
									context.putSecretData("lastFailureTimeAttempt", Json.encodeToString(LastFailedAttempt(nextAttemptAt = Instant.now().toEpochMilli(), attemptCount = 1)))
									recalculateFailedCounter()
								}
							}

							lastFailedTimeRequest !is AliceRequest2.Success -> {
								context.putSecretData("lastFailureTimeAttempt", Json.encodeToString(LastFailedAttempt(nextAttemptAt = Instant.now().toEpochMilli(), attemptCount = 1)))
								recalculateFailedCounter()
							}
						}
					}

					is AliceRequest2.KeyStoreNotInitialized -> Unit
					is AliceRequest2.KeyNotFound -> Unit
					is AliceRequest2.Error -> Unit
				}
			}
		}
	}
}
