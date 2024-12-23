package com.syncodec.graphite.presentation.settings.composable.buildingBlock

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.BaseApplication
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.animation.AnimatedText
import com.syncodec.graphite.presentation.ui.ICON_SIZE


@OptIn(ExperimentalAnimationApi::class)
@Preview
@Composable
fun SettingSwitch(
	text : String = "Switch",
	subText : String? = null,
	icon : Int = R.drawable.ic_setting,
	isChecked : Boolean = false,
	enabled : Boolean = true,
	onCheckedChange : (Boolean) -> Unit = {},
) {
	Box(
		modifier = Modifier.clickable(enabled = enabled) { onCheckedChange(! isChecked) }
	) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier.padding(0.dp, 12.dp, 0.dp, 12.dp)
		) {
			Spacer(modifier = Modifier.width(16.dp))
			Icon(
				painter = painterResource(id = icon),
				contentDescription = text,
				modifier = Modifier.requiredSize(ICON_SIZE)
			)
			Spacer(modifier = Modifier.width(24.dp))
			Text(
				text = text,
				style = MaterialTheme.typography.bodyMedium,
				color = if (enabled) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.47f),
				fontWeight = FontWeight.Bold,
				modifier = Modifier.weight(1f)
			)
			Spacer(modifier = Modifier.width(8.dp))

			subText?.let {
				AnimatedText(
					text = subText,
					style = MaterialTheme.typography.bodySmall,
					color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.47f),
				)
				Spacer(modifier = Modifier.width(8.dp))
			}

			Switch(
				checked = isChecked && enabled,
				colors = SwitchDefaults.colors(
					checkedThumbColor = MaterialTheme.colorScheme.primary,
					checkedTrackColor = MaterialTheme.colorScheme.surface,
					checkedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.71f),
					uncheckedThumbColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.31f),
					uncheckedTrackColor = MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp).copy(alpha = 0.31f),
					uncheckedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.31f),
					disabledCheckedThumbColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.47f),
					disabledCheckedTrackColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.47f),
					disabledCheckedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.71f).copy(alpha = 0.47f),
					disabledUncheckedThumbColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.31f).copy(alpha = 0.47f),
					disabledUncheckedTrackColor = MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp).copy(alpha = 0.31f).copy(alpha = 0.47f),
					disabledUncheckedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.31f).copy(alpha = 0.47f),
				),
				onCheckedChange = {
					if (enabled) onCheckedChange(it)
				},
				enabled = enabled,
				modifier = Modifier
					.height(0.dp)
					.padding(0.dp)
					.graphicsLayer {
						scaleX = 0.71f
						scaleY = 0.71f
					}
			)
			Spacer(modifier = Modifier.width(8.dp))
		}
	}
}

@Preview
@Composable
fun SettingSwitchWithPro(
	text : String = "Switch",
	icon : Int = R.drawable.ic_setting,
	isChecked : Boolean = false,
	onCheckedChange : (Boolean) -> Unit = {},
) {
	val isPro by BaseApplication.isPro.collectAsState()

	Box(
		modifier = Modifier.clickable { onCheckedChange(! isChecked) }
	) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier.padding(0.dp, 12.dp, 0.dp, 12.dp)
		) {
			Spacer(modifier = Modifier.width(16.dp))
			Icon(
				painter = painterResource(id = icon),
				contentDescription = text,
				modifier = Modifier.requiredSize(ICON_SIZE)
			)
			Spacer(modifier = Modifier.width(24.dp))
			Text(
				text = text,
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onBackground,
				fontWeight = FontWeight.Bold,
				modifier = Modifier.weight(1f)
			)
			Spacer(modifier = Modifier.width(8.dp))

			if (! isPro) {
				Icon(
					painter = painterResource(id = R.drawable.ic_lock_close),
					contentDescription = "Pro",
					tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.71f),
					modifier = Modifier.requiredSize(ICON_SIZE)
				)
				Spacer(modifier = Modifier.width(8.dp))
			}

			Switch(
				checked = isChecked,
				colors = SwitchDefaults.colors(
					checkedThumbColor = MaterialTheme.colorScheme.primary,
					checkedTrackColor = MaterialTheme.colorScheme.surface,
					checkedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.71f),
					uncheckedThumbColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.31f),
					uncheckedTrackColor = MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp).copy(alpha = 0.31f),
					uncheckedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.31f)
				),
				onCheckedChange = onCheckedChange,
				modifier = Modifier
					.height(0.dp)
					.padding(0.dp)
					.graphicsLayer {
						scaleX = 0.71f
						scaleY = 0.71f
					}
			)
			Spacer(modifier = Modifier.width(8.dp))
		}
	}
}
