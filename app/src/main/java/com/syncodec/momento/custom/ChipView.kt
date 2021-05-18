package com.syncodec.momento.custom

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

data class ChipData(
	val title: String,
	val imageVector: ImageVector,
	val isSelected: Boolean,
	val onClick: (() -> Unit)? = null
)

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ChipView(
	chipDataList: List<ChipData>,
) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.horizontalScroll(rememberScrollState()),
		verticalAlignment = Alignment.CenterVertically
	) {
		Spacer(modifier = Modifier.width(12.dp))
		chipDataList.forEach {
			val containerColor by animateColorAsState(
				targetValue = if (it.isSelected) MaterialTheme.colorScheme.primary else Color.Companion.Transparent,
				animationSpec = tween(durationMillis = 400)
			)
			val contentColor by animateColorAsState(
				targetValue = if (it.isSelected) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onSecondaryContainer,
				animationSpec = tween(durationMillis = 400)
			)

			Card(
				border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
				backgroundColor = containerColor,
				elevation = 0.dp,
				shape = RoundedCornerShape(20.dp),
				modifier = Modifier
					.height(32.dp)
					.clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { it.onClick?.let { it1 -> it1() } },
			) {
				Row(
					modifier = Modifier
						.padding(12.dp, 4.dp),
					verticalAlignment = Alignment.CenterVertically
				) {
					Icon(
						imageVector = it.imageVector,
						contentDescription = it.title,
						tint = contentColor,
						modifier = Modifier
							.requiredSize(16.dp)
					)
					Spacer(modifier = Modifier.width(6.dp))
					Text(
						text = it.title,
						style = MaterialTheme.typography.bodyMedium,
						color = contentColor,
					)
				}
			}
			Spacer(modifier = Modifier.width(8.dp))
		}
		Spacer(modifier = Modifier.width(4.dp))
	}
}
