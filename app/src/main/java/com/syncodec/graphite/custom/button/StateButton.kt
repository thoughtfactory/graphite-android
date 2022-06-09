package com.syncodec.graphite.custom.button

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.miscellaneous.ThemeUtils.Companion.tone


data class StateData(
	val title: String,
	val icon: Int,
	val stateTint: Color,
)

@Composable
fun StateButton(
	stateList: List<StateData>,
	currentState: Int,
	modifier: Modifier,
	onStateChange: (Int) -> Unit
) {
	val interactionSource = remember { MutableInteractionSource() }
	val spacerWeight by animateFloatAsState(targetValue = currentState.toFloat())

	Surface(
		modifier = modifier.fillMaxWidth(),
		color = MaterialTheme.colorScheme.surface.tone(isSystemInDarkTheme(), 1),
		tonalElevation = 0.dp,
		shape = RoundedCornerShape(12.dp)
	) {
		Row(
			modifier = Modifier.fillMaxSize()
		) {
			Spacer(modifier = Modifier.weight((spacerWeight + 0.00001).toFloat()))
			Box(
				modifier = Modifier
					.fillMaxHeight()
					.weight(1f)
					.clip(RoundedCornerShape(12.dp))
					.background(stateList[currentState].stateTint)
			)
			Spacer(modifier = Modifier.weight((stateList.size - spacerWeight - 1 + 0.00001).toFloat()))
		}

		Row(
			modifier = Modifier
				.fillMaxSize()
				.fillMaxHeight(),
			verticalAlignment = Alignment.CenterVertically
		) {
			stateList.forEachIndexed { index, state ->
				val textColor by animateColorAsState(targetValue = if (index == currentState) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface)
				Row(
					modifier = Modifier
						.weight(1f)
						.fillMaxHeight()
						.padding(8.dp, 0.dp)
						.clickable(
							interactionSource = interactionSource,
							indication = null
						) { onStateChange(index) },
					verticalAlignment = Alignment.CenterVertically,
					horizontalArrangement = Arrangement.Center
				) {
					Icon(
						painter = painterResource(id = state.icon),
						contentDescription = state.title,
						tint = textColor,
						modifier = Modifier.requiredSize(20.dp)
					)
					Spacer(modifier = Modifier.width(6.dp))
					Text(
						text = state.title,
						style = MaterialTheme.typography.bodyMedium,
						fontWeight = FontWeight.Bold,
						color = textColor,
						textAlign = TextAlign.Center,
						maxLines = 1,
						overflow = TextOverflow.Ellipsis,
						modifier = Modifier
					)
				}
			}
		}
	}
}
