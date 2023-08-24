package com.syncodec.graphite.presentation.note2.composable.bar.editor

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltipBox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.shape.AbsoluteSmoothCornerShape
import com.syncodec.graphite.presentation.ui.IconButtonSize


@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun EditorButton(
	icon: Int = R.drawable.ic_fa_format_bold,
	tooltip: String = "",
	iconStaticColor: Color = MaterialTheme.colorScheme.onSurface,
	isChecked: Boolean = false,
	onClick: () -> Unit = {}
) {
	val containerColor by animateColorAsState(
		targetValue = if (isChecked) MaterialTheme.colorScheme.onSurface else Color.Transparent,
		animationSpec = tween(470),
		label = "containerColor_animation"
	)
	val contentColor by animateColorAsState(
		targetValue = if (isChecked) MaterialTheme.colorScheme.surface else iconStaticColor,
		animationSpec = tween(470),
		label = "contentColor_animation"
	)

	PlainTooltipBox(
		tooltip = {
			Text(text = tooltip)
		}
	) {
		Box(
			contentAlignment = Alignment.Center,
			modifier = Modifier
				.requiredSize(40.dp)
				.padding(2.dp)
				.background(containerColor, MaterialTheme.shapes.small)
				.clip(MaterialTheme.shapes.small)
				.clickable(onClickLabel = tooltip, role = Role.Button) { onClick() }
		) {
			Icon(
				painter = painterResource(id = icon),
				contentDescription = tooltip,
				tint = contentColor,
				modifier = Modifier.requiredSize(IconButtonSize)
			)
		}
	}
}
