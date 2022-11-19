package com.syncodec.graphite.presentation.common.notebook

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.utils.colorList
import com.syncodec.graphite.utils.getInverseBWColor


@Composable
fun NotebookColorChooser(
	currentColor : Color?,
	onClickColorPicker : () -> Unit,
	onChooseColor : (Color) -> Unit,
) {
	val containerColor by animateColorAsState(
		targetValue = currentColor ?: MaterialTheme.colorScheme.background,
		animationSpec = tween(300)
	)
	val contentColor by animateColorAsState(
		targetValue = (currentColor ?: MaterialTheme.colorScheme.background).getInverseBWColor(),
		animationSpec = tween(300)
	)

	Row(
		modifier = Modifier
			.fillMaxWidth()
			.horizontalScroll(rememberScrollState())
	) {
		Spacer(modifier = Modifier.width(24.dp))

		Box(
			contentAlignment = Alignment.Center,
			modifier = Modifier
				.width(64.dp)
				.height(40.dp)
				.background(containerColor, RoundedCornerShape(16.dp))
				.clip(RoundedCornerShape(16.dp))
				.clickable { onClickColorPicker() }
		) {
			Icon(
				painter = painterResource(id = R.drawable.ic_color_picker),
				contentDescription = "Color Picker",
				tint = contentColor,
				modifier = Modifier.requiredSize(24.dp)
			)
		}

		Spacer(modifier = Modifier.width(6.dp))

		for (color in colorList) {
			val borderColor by animateColorAsState(
				targetValue = if (color == currentColor) color.getInverseBWColor().copy(alpha = 0.47f) else Color.Transparent,
				animationSpec = tween(300)
			)
			Box(
				modifier = Modifier
					.width(64.dp)
					.height(40.dp)
					.background(color, RoundedCornerShape(16.dp))
					.border(width = 4.dp, color = borderColor, shape = RoundedCornerShape(16.dp))
					.clip(RoundedCornerShape(16.dp))
					.clickable { onChooseColor(color) }
			)
			Spacer(modifier = Modifier.width(6.dp))
		}
		Spacer(modifier = Modifier.width(18.dp))
	}
}
