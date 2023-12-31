package com.syncodec.graphite.presentation.note.composable.bar.editor

import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.base.ANIMATION_DURATION_MILLIS
import com.syncodec.graphite.presentation.base.ICON_SIZE
import com.syncodec.graphite.presentation.base.LocalIsPro
import com.syncodec.graphite.utils.timeStampToTime
import com.syncodec.graphite.utils.toDate
import com.syncodec.graphite.utils.toMonthYear
import java.time.Instant


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
		animationSpec = tween(ANIMATION_DURATION_MILLIS),
		label = "containerColor_animation"
	)
	val contentColor by animateColorAsState(
		targetValue = if (isChecked) MaterialTheme.colorScheme.surface else iconStaticColor,
		animationSpec = tween(ANIMATION_DURATION_MILLIS),
		label = "contentColor_animation"
	)

	TooltipBox(
		positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
		state = rememberTooltipState(),
		tooltip = {
			PlainTooltip {
				Text(text = tooltip)
			}
		}
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
		) {
			Icon(
				painter = painterResource(id = icon),
				contentDescription = tooltip,
				tint = contentColor,
				modifier = Modifier.requiredSize(ICON_SIZE)
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

	TooltipBox(
		positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
		state = rememberTooltipState(),
		tooltip = {
			PlainTooltip {
				Text(text = tooltip)
			}
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
				modifier = Modifier.requiredSize(ICON_SIZE)
			)
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun DatePickerButton(
	userTimestamp: Long = Instant.now().toEpochMilli(),
	onClick: () -> Unit = {},
) {
	TooltipBox(
		positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
		state = rememberTooltipState(),
		tooltip = {
			PlainTooltip {
				Text(text = stringResource(R.string.date))
			}
		}
	) {
		Surface(
			color = Color.Transparent,
			contentColor = MaterialTheme.colorScheme.onSurface,
			onClick = onClick,
			modifier = Modifier
				.requiredHeight(40.dp)
				.padding(horizontal = 4.dp, vertical = 2.dp)
		) {
			Row(
				verticalAlignment = Alignment.CenterVertically,
				modifier = Modifier.fillMaxHeight(),
			) {
				Text(
					text = userTimestamp.toDate(),
					style = MaterialTheme.typography.headlineMedium,
					fontWeight = FontWeight.Bold
				)
				Spacer(modifier = Modifier.width(8.dp))
				Column {
					Text(
						text = userTimestamp.toMonthYear(),
						style = MaterialTheme.typography.bodySmall,
						fontWeight = FontWeight.Bold
					)
					Text(
						text = userTimestamp.timeStampToTime(),
						style = MaterialTheme.typography.bodyMedium,
						fontWeight = FontWeight.Bold
					)
				}
			}
		}
	}
}
