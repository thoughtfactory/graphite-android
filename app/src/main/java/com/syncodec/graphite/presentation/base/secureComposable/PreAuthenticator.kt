package com.syncodec.graphite.presentation.base.secureComposable

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.LoadingView
import com.syncodec.graphite.presentation.common.button.CancelButton
import com.syncodec.graphite.utils.alice.AliceRequest2
import com.syncodec.graphite.utils.alice.getSecretData2
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import java.time.Instant


@Preview
@Composable
fun PreAuthenticator(
	content: @Composable (Int?, () -> Unit) -> Unit = { _, _ -> }
) {
	val context = LocalContext.current

	var tryAgainInSeconds by remember { mutableStateOf<Long?>(null) }
	var attemptCount by remember { mutableStateOf<Int?>(null) }

	LaunchedEffect(key1 = tryAgainInSeconds) {
		delay(1000)
		tryAgainInSeconds.let { tryAgainInSeconds1 ->
			if (tryAgainInSeconds1 != null && tryAgainInSeconds1 > 0) tryAgainInSeconds = tryAgainInSeconds1 - 1_000
		}
	}

	fun recalculateFailedCounter() {
		when (val lastFailedTimeRequest = context.getSecretData2("lastFailureTimeAttempt")) {
			is AliceRequest2.Success -> try {
				val lastFailedAttempt = Json.decodeFromString<LastFailedAttempt>(lastFailedTimeRequest.data.decodeToString())
				attemptCount = lastFailedAttempt.attemptCount
				tryAgainInSeconds = lastFailedAttempt.nextAttemptAt - Instant.now().toEpochMilli()
			} catch (_: Exception) {
				tryAgainInSeconds = 0
			}

			is AliceRequest2.KeyStoreNotInitialized -> tryAgainInSeconds = 0
			is AliceRequest2.KeyNotFound -> tryAgainInSeconds = 0
			is AliceRequest2.Error -> tryAgainInSeconds = 0
		}
	}

	LaunchedEffect(key1 = Unit) {
		recalculateFailedCounter()
	}

	tryAgainInSeconds.let {tryAgainInSeconds1 ->
		when {
			tryAgainInSeconds1 == null -> LoadingView()
			tryAgainInSeconds1 <= 0L -> content(attemptCount, ::recalculateFailedCounter)
			else -> WaitView(tryAgainInSeconds = tryAgainInSeconds1)
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun WaitView(
	tryAgainInSeconds: Long? = null,
	onClose : () -> Unit = {}
) {

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
			contentDescription = "Vault locked",
			modifier = Modifier.weight(1f)
		)

		Spacer(modifier = Modifier.height(24.dp))
		Spacer(modifier = Modifier.weight(1f))

		Text(
			text = "Try again in ${tryAgainInSeconds?.div(1000)} seconds",
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onBackground,
		)

		Spacer(modifier = Modifier.weight(1f))
		Spacer(modifier = Modifier.height(24.dp))
	}
}
