package com.syncodec.momento.custom

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.syncodec.momento.miscellaneous.ThemeUtils.Companion.tone

data class ChipData(
	val title: String,
	val icon: Int,
	val isSelected: Boolean,
	val onClick: (() -> Unit)? = null
)

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
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
				targetValue = if (it.isSelected)
					MaterialTheme.colorScheme.surface.tone(isSystemInDarkTheme(), 3)
				else
					Color.Transparent,
				animationSpec = tween(durationMillis = 300)
			)
			val contentColor by animateColorAsState(
				targetValue = if (it.isSelected)
					MaterialTheme.colorScheme.onSurface.tone(isSystemInDarkTheme(), 3)
				else
					MaterialTheme.colorScheme.onBackground,
				animationSpec = tween(durationMillis = 300)
			)

			Card(
				border = BorderStroke(2.dp, MaterialTheme.colorScheme.surface.tone(isSystemInDarkTheme(), 3)),
				containerColor = containerColor,
				shape = RoundedCornerShape(20.dp),
				modifier = Modifier
					.height(32.dp)
					.clickable(
						interactionSource = remember { MutableInteractionSource() },
						indication = null
					) { it.onClick?.let { it1 -> it1() } },
			) {
				Row(
					verticalAlignment = Alignment.CenterVertically,
					modifier = Modifier
						.height(32.dp)
						.padding(12.dp, 0.dp)
					,
				) {
					Icon(
						painter = painterResource(id = it.icon),
						contentDescription = it.title,
						tint = contentColor,
						modifier = Modifier.requiredSize(16.dp)
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
