package com.syncodec.graphite.presentation.common.button.stateButton

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp


data class StateData(
	val title: String,
	val icon: Int? = null,
	val stateTint: Color? = null,
)

@Composable
fun StateButton(
	modifier: Modifier,
	stateList: List<StateData>,
	containerColor: Color = MaterialTheme.colorScheme.surface,
	currentState: Int,
	onStateChange: (Int) -> Unit
) {
	val interactionSource = remember { MutableInteractionSource() }
	val spacerWeight by animateFloatAsState(targetValue = currentState.toFloat())

	Box(
		contentAlignment = Alignment.Center,
		modifier = modifier
			.fillMaxWidth()
			.background(containerColor, RoundedCornerShape(31))
			.border(4.dp, containerColor, RoundedCornerShape(31))
	) {
		Row(
			modifier = Modifier.fillMaxSize(),
			verticalAlignment = Alignment.CenterVertically
		) {
			Spacer(modifier = Modifier.weight((spacerWeight + 0.00001).toFloat()))
			Box(
				modifier = Modifier
					.fillMaxHeight()
					.weight(1f)
					.background(stateList[currentState].stateTint ?: MaterialTheme.colorScheme.primary, RoundedCornerShape(31))
					.border(4.dp, containerColor, RoundedCornerShape(31))
			)
			Spacer(modifier = Modifier.weight((stateList.size - spacerWeight - 1 + 0.00001).toFloat()))
		}

		Row(
			modifier = Modifier.fillMaxSize(),
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
					if (state.icon != null) {
						Icon(
							painter = painterResource(id = state.icon),
							contentDescription = state.title,
							tint = textColor,
							modifier = Modifier.requiredSize(20.dp)
						)
						Spacer(modifier = Modifier.width(6.dp))
					}
					Text(
						text = state.title,
						style = MaterialTheme.typography.bodySmall,
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
