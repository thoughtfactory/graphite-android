package com.syncodec.graphite.presentation.settings.composable.buildingBlock

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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.ui.IconButtonSize


@Preview
@Composable
fun SettingSwitch(
	text : String = "Switch",
	icon : Int = R.drawable.ic_setting,
	isChecked : Boolean = false,
	onCheckedChange : (Boolean) -> Unit = {},
) {
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
				modifier = Modifier.requiredSize(IconButtonSize)
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

			Switch(
				checked = isChecked,
				colors = SwitchDefaults.colors(
					checkedThumbColor = MaterialTheme.colorScheme.primary,
					checkedTrackColor = MaterialTheme.colorScheme.surface,
					checkedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.71f),
					uncheckedThumbColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.31f),
					uncheckedTrackColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.31f),
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
