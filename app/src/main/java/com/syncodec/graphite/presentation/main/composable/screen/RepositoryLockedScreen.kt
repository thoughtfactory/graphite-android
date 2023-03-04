package com.syncodec.graphite.presentation.main.composable.screen

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.animation.AnimatedText


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun RepositoryLockedScreen(
	errorMessage : String?,
	onUnlock : () -> Unit
) {

	val configuration = LocalConfiguration.current
	val screenWidth = configuration.screenWidthDp.dp

	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.Center,
		modifier = Modifier.fillMaxSize()
	) {
		Image(
			painter = painterResource(id = R.drawable.il_repository_locked),
			contentDescription = "Repository Locked",
			contentScale = ContentScale.Fit,
			modifier = Modifier.requiredSize(screenWidth * 2 / 3)
		)

		Spacer(modifier = Modifier.height(12.dp))

		Text(
			text = "Your data is locked behind your fingerprint",
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onBackground
		)

		Spacer(modifier = Modifier.height(12.dp))

		AnimatedText(
			text = errorMessage,
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.error
		)

		Spacer(modifier = Modifier.height(24.dp))

		Button(
			shape = MaterialTheme.shapes.medium,
			onClick = onUnlock,
		) {
			Icon(
				painter = painterResource(id = R.drawable.ic_biometric),
				contentDescription = "Unlock with biometric"
			)
			Spacer(modifier = Modifier.width(8.dp))
			Text(text = "Unlock with biometric")
		}
	}
}
