package com.syncodec.momento.custom

import androidx.compose.animation.animateColor
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.consumePositionChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt


@Composable
fun DraggableCard(
	isRevealed: Boolean,
	cardOffset: Float,
	onExpand: () -> Unit,
	onCollapse: () -> Unit,
//	content: @Composable () -> Unit,
) {
	val offsetX = remember { mutableStateOf(0f) }

	val cardBgColor by animateColorAsState(
		targetValue = if (isRevealed) Color.Companion.Red else Color.Companion.Green,
		animationSpec = tween(durationMillis = 1000),
	)

	val offsetTransition by animateFloatAsState(
		targetValue = if (isRevealed) cardOffset - offsetX.value else -offsetX.value,
		animationSpec = tween(durationMillis = 1000),
	)

	val cardElevation by animateDpAsState(
		targetValue = if (isRevealed) 40.dp else 2.dp,
		animationSpec = tween(durationMillis = 1000),
	)

	Card(
		modifier = Modifier
			.fillMaxWidth()
			.padding(horizontal = 16.dp, vertical = 8.dp)
			.offset { IntOffset((offsetX.value + offsetTransition).roundToInt(), 0) }
			.pointerInput(Unit) {
				detectHorizontalDragGestures { change, dragAmount ->
					val original = Offset(offsetX.value, 0f)
					val summed = original + Offset(x = dragAmount, y = 0f)
					val newValue = Offset(x = summed.x.coerceIn(0f, cardOffset), y = 0f)
					if (newValue.x >= 10) {
						onExpand()
						return@detectHorizontalDragGestures
					} else if (newValue.x <= 0) {
						onCollapse()
						return@detectHorizontalDragGestures
					}
					change.consumePositionChange()
					offsetX.value = newValue.x
				}
			},
		backgroundColor = cardBgColor,
		shape = RoundedCornerShape(0.dp),
		elevation = cardElevation,
		content = {
			Card(
				modifier = Modifier
					.fillMaxWidth()
					.height(144.dp),
				backgroundColor = MaterialTheme.colorScheme.secondaryContainer
			) {

			}
		}
	)
}
