package com.syncodec.graphite.presentation.note2.composable.bar.editor

import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.base.ICON_BUTTON_SIZE
import com.syncodec.graphite.presentation.base.LocalIsPro


@Preview
@Composable
fun EditorButton(
	icon: Int = R.drawable.ic_fa_format_bold,
	tooltip: String = "",
	iconStaticColor: Color = MaterialTheme.colorScheme.onSurface,
	isChecked: Boolean = false,
	isPremium: Boolean = false,
	onClick: () -> Unit = {}
) {
	val isPro = LocalIsPro.current

	if (!isPremium || isPro) {
		StandardEditorButton(
			icon = icon,
			tooltip = tooltip,
			iconStaticColor = iconStaticColor,
			isChecked = isChecked,
			onClick = onClick,
		)
	} else {
		ProEditorButton(
			icon = icon,
			tooltip = tooltip,
			iconStaticColor = iconStaticColor,
			isChecked = isChecked,
		)
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun ProEditorButton(
	icon: Int = R.drawable.ic_fa_format_bold,
	tooltip: String = "",
	iconStaticColor: Color = MaterialTheme.colorScheme.onSurface,
	isChecked: Boolean = false,
) {
	val context = LocalContext.current

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
		tooltip = { Text(text = tooltip) }
	) {
		Box(
			contentAlignment = Alignment.Center,
			modifier = Modifier
				.requiredSize(40.dp)
				.padding(2.dp)
				.background(containerColor, MaterialTheme.shapes.small)
				.clip(MaterialTheme.shapes.small)
				.clickable(onClickLabel = tooltip, role = Role.Button) {
					Toast
						.makeText(context, "Join Graphite pro to unlock full potential of editor", Toast.LENGTH_SHORT)
						.show()
				}
				.tooltipTrigger()
		) {
			Icon(
				painter = painterResource(id = icon),
				contentDescription = tooltip,
				tint = contentColor,
				modifier = Modifier.requiredSize(ICON_BUTTON_SIZE)
			)

			Box(
				modifier = Modifier
					.fillMaxSize()
					.background(MaterialTheme.colorScheme.background.copy(alpha = 0.71f))
			)

			Icon(
				painter = painterResource(id = R.drawable.ic_fa_lock_close_solid),
				contentDescription = null,
				tint = MaterialTheme.colorScheme.onBackground,
				modifier = Modifier
					.requiredSize(20.dp)
					.padding(4.dp)
					.align(Alignment.BottomEnd)
			)
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun StandardEditorButton(
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
		tooltip = { Text(text = tooltip) }
	) {
		Box(
			contentAlignment = Alignment.Center,
			modifier = Modifier
				.requiredSize(40.dp)
				.padding(2.dp)
				.background(containerColor, MaterialTheme.shapes.small)
				.clip(MaterialTheme.shapes.small)
				.clickable(onClickLabel = tooltip, role = Role.Button) { onClick() }
				.tooltipTrigger()
		) {
			Icon(
				painter = painterResource(id = icon),
				contentDescription = tooltip,
				tint = contentColor,
				modifier = Modifier.requiredSize(ICON_BUTTON_SIZE)
			)
		}
	}
}
