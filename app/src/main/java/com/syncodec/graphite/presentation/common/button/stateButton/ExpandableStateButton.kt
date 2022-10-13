package com.syncodec.graphite.presentation.common.button.stateButton

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.presentation.common.ExpandableBox
import com.syncodec.graphite.presentation.common.ExpandableBoxOrientation
import com.syncodec.graphite.utils.getInverseBWColor


@Composable
fun ExpandableStateButton(
	stateList: List<StateData>,
	currentState: Int,
	modifier: Modifier,
	onStateChange: (Int) -> Unit
) {
	val interactionSource = remember { MutableInteractionSource() }
	val spacerWeight by animateFloatAsState(targetValue = currentState.toFloat())

	Box(
		modifier = modifier
	) {
		Row(
			modifier = Modifier.fillMaxWidth(),
		) {
			stateList.forEachIndexed { index, stateData ->
				Row(
					verticalAlignment = Alignment.CenterVertically,
					modifier = Modifier
						.background(
							color = if (index == currentState) stateData.stateTint ?: MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
							shape = RoundedCornerShape(12.dp)
						)
//						.border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
						.clip(RoundedCornerShape(12.dp))
						.clickable { onStateChange(index) }
				) {
					Spacer(modifier = Modifier.width(4.dp))
					if (stateData.icon == null) {
						Text(
							text = stateData.title,
							style = MaterialTheme.typography.bodyMedium,
							color = stateData.stateTint?.getInverseBWColor() ?: MaterialTheme.colorScheme.onSurface,
						)
					} else {
						Icon(
							painter = painterResource(id = stateData.icon),
							contentDescription = stateData.title,
							tint = if (index == currentState) stateData.stateTint?.getInverseBWColor() ?: MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
							modifier = Modifier
								.requiredSize(40.dp)
								.padding(8.dp)
						)
					}

					ExpandableBox(
						isVisible = index == currentState,
						orientation = ExpandableBoxOrientation.HORIZONTAL,
					) {
						Row(
							modifier = Modifier
						) {
							Text(
								text = stateData.title,
								style = MaterialTheme.typography.bodyMedium,
								color = stateData.stateTint?.getInverseBWColor() ?: MaterialTheme.colorScheme.onSurface,
							)
							Spacer(modifier = Modifier.width(4.dp))
						}
					}
					Spacer(modifier = Modifier.width(4.dp))
				}

				if (index != stateList.lastIndex) {
					Spacer(modifier = Modifier.width(8.dp))
				}
			}
		}
	}
}
