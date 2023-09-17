package com.syncodec.graphite.presentation.note.screen.editorScreen.bar.bottomBar.buildingBlock

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.base.IconButtonSize


@Preview
@Composable
fun ToolbarButton(
	modifier : Modifier = Modifier,
	icon : Int = R.drawable.ic_format_bold,
	contentDescription : String? = null,
	isChecked : Boolean = false,
	padding: Int = 0,
	onClick : () -> Unit = {},
) {
	val containerColor by animateColorAsState(
		targetValue = if (isChecked) MaterialTheme.colorScheme.onSurface else Color.Transparent,
		animationSpec = tween(300)
	)
	val contentColor by animateColorAsState(
		targetValue = if (isChecked) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurface,
		animationSpec = tween(300)
	)

	Box(
		contentAlignment = Alignment.Center,
		modifier = modifier
			.requiredSize(40.dp)
			.padding(2.dp)
			.background(containerColor, MaterialTheme.shapes.small)
			.clip(MaterialTheme.shapes.small)
			.clickable(onClickLabel = contentDescription, role = Role.Button) { onClick() }
	) {
		Icon(
			painter = painterResource(id = icon),
			contentDescription = contentDescription,
			tint = contentColor,
			modifier = Modifier
				.requiredSize(IconButtonSize)
				.padding(padding.dp)
		)
	}
}
