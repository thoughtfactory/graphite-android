package com.syncodec.graphite.noteComponent.miscellaneous

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.syncodec.graphite.noteComponent.NoteViewModel
import com.syncodec.graphite.konstant.ErrorCode
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


sealed class NotificationType {
	object UrlSelectionNotification : NotificationType()
}

@Composable
fun NotificationLayout() {
	val noteViewModel: NoteViewModel = viewModel()
	when (noteViewModel.activityState.notificationType.value) {
		NotificationType.UrlSelectionNotification -> NotificationCard(
			errorCode = ErrorCode.Companion.ErrorCode.URL_RANGE_SELECTION_ERROR,
		)
	}
}


@Composable
fun NotificationCard(
	errorCode: ErrorCode.Companion.ErrorCode,
) {
	val scope = rememberCoroutineScope()
	val noteViewModel: NoteViewModel = viewModel()

	SideEffect {
		scope.launch {
			delay(3200)
			noteViewModel.activityState.isNotificationVisible.value = false
		}
	}

	val enterFadeIn = remember {
		fadeIn(
			animationSpec = TweenSpec(
				durationMillis = 200,
				easing = FastOutLinearInEasing
			)
		)
	}
	val exitFadeOut = remember {
		fadeOut(
			animationSpec = TweenSpec(
				durationMillis = 200,
				easing = LinearOutSlowInEasing
			)
		)
	}

	AnimatedVisibility(
		visible = noteViewModel.activityState.isNotificationVisible.value,
		enter = enterFadeIn,
		exit = exitFadeOut
	) {
		Box(
			contentAlignment = Alignment.BottomCenter,
			modifier = Modifier
				.fillMaxWidth()
				.padding(0.dp, 0.dp, 0.dp, 16.dp),
		) {
			Card(
				elevation = 0.dp,
				shape = RoundedCornerShape(16.dp),
				backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
				modifier = Modifier
					.fillMaxWidth()
					.padding(12.dp)
			) {
				Row(
					verticalAlignment = Alignment.CenterVertically,
					modifier = Modifier
						.padding(16.dp),
				) {
					Icon(
						painter = painterResource(id = ErrorCode.errorCodeResourceMap[errorCode]!!),
						contentDescription = ErrorCode.errorCodeMessageMap[errorCode],
						tint = MaterialTheme.colorScheme.onSecondaryContainer
					)

					Spacer(modifier = Modifier.width(16.dp))

					Text(
						text = ErrorCode.errorCodeMessageMap[errorCode]!!,
						style = MaterialTheme.typography.bodyMedium,
						color = MaterialTheme.colorScheme.onSecondaryContainer
					)
				}
			}
		}
	}
}
