package com.syncodec.graphite.presentation.custom.button

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.utils.getInverseBWColor
import com.syncodec.graphite.utils.tone


@Composable
fun MenuButton(
	icon: Int,
	contentDescription: String? = null,
	tint: Color = MaterialTheme.colorScheme.onSurface.tone(isSystemInDarkTheme(), 1),
	isEnabled: Boolean = true,
	isChecked: Boolean,
	onClick: () -> Unit
) {
	val containerColor by animateColorAsState(
		targetValue = if (isChecked) tint else Color.Transparent,
		animationSpec = tween(300)
	)
	val contentColor by animateColorAsState(
		targetValue = if (isChecked) tint.getInverseBWColor() else MaterialTheme.colorScheme.onSurface,
		animationSpec = tween(300)
	)

	Box(
		modifier = Modifier
			.requiredSize(44.dp)
			.padding(2.dp)
			.background(containerColor, RoundedCornerShape(25, 25, 25, 25))
			.clip(RoundedCornerShape(25, 25, 25, 25))
			.clickable(enabled = isEnabled, onClickLabel = contentDescription, role = Role.Button) { onClick() },
		contentAlignment = Alignment.Center
	) {
		Icon(
			painter = painterResource(id = icon),
			contentDescription = contentDescription,
			tint = contentColor,
			modifier = Modifier.requiredSize(24.dp)
		)
	}
}

@Composable
fun MenuButton(
	icon: Int,
	contentDescription: String? = null,
	tint: Color = MaterialTheme.colorScheme.onSurface,
	isEnabled: Boolean = true,
	onClick: () -> Unit
) {
	Box(
		modifier = Modifier
			.requiredSize(44.dp)
			.padding(2.dp)
			.background(Color.Transparent, RoundedCornerShape(25, 25, 25, 25))
			.clip(RoundedCornerShape(25, 25, 25, 25))
			.clickable(enabled = isEnabled, onClickLabel = contentDescription, role = Role.Button) { onClick() },
		contentAlignment = Alignment.Center
	) {
		Icon(
			painter = painterResource(id = icon),
			contentDescription = contentDescription,
			tint = tint,
			modifier = Modifier.requiredSize(24.dp)
		)
	}
}
