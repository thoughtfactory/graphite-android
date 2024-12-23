package com.syncodec.graphite.presentation.settings.composable.buildingBlock

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.animation.AnimatedText
import com.syncodec.graphite.presentation.main.composable.buildingBlock.DropdownMenu
import com.syncodec.graphite.presentation.main.composable.buildingBlock.DropdownMenuItem
import com.syncodec.graphite.presentation.ui.ICON_SIZE


@OptIn(ExperimentalAnimationApi::class)
@Preview
@Composable
fun SettingButton(
	text : String = "Button",
	subText : String? = null,
	infoText : String? = null,
	icon : Int = R.drawable.ic_setting,
	subIcon : Int? = null,
	tint : Color = MaterialTheme.colorScheme.onBackground,
	subIconTint : Color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.47f),
	enabled : Boolean = true,
	onClick : () -> Unit = {},
) {
	Box(
		modifier = Modifier.clickable(enabled = enabled) { onClick() }
	) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier.padding(0.dp, 12.dp, 0.dp, 12.dp)
		) {
			Spacer(modifier = Modifier.width(16.dp))
			Icon(
				painter = painterResource(id = icon),
				contentDescription = text,
				tint = tint,
				modifier = Modifier.requiredSize(ICON_SIZE)
			)
			Spacer(modifier = Modifier.width(24.dp))
			Column(
				modifier = Modifier.weight(1f),
			) {
				Text(
					text = text,
					style = MaterialTheme.typography.bodyMedium,
					color = if (enabled) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.47f),
					fontWeight = FontWeight.Bold,
					modifier = Modifier.fillMaxWidth(),
				)
				infoText?.let {
					Spacer(modifier = Modifier.height(4.dp))
					AnimatedText(
						text = it,
						style = MaterialTheme.typography.bodySmall,
						color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.47f),
					)
				}
			}
			Spacer(modifier = Modifier.width(24.dp))
			AnimatedText(
				text = subText ?: "",
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.47f),
			)
			Spacer(modifier = Modifier.width(8.dp))
			subIcon?.let {
				Icon(
					painter = painterResource(id = it),
					contentDescription = text,
					tint = subIconTint,
					modifier = Modifier.requiredSize(ICON_SIZE)
				)
				Spacer(modifier = Modifier.width(8.dp))
			}
			Icon(
				painter = painterResource(id = R.drawable.ic_caret),
				contentDescription = text,
				tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.47f),
				modifier = Modifier
					.requiredSize(ICON_SIZE)
					.graphicsLayer { rotationZ = 90f }
			)
			Spacer(modifier = Modifier.width(16.dp))
		}
	}
}

@OptIn(ExperimentalAnimationApi::class)
@Preview
@Composable
fun SettingButtonWithDropdown(
	text : String = "Button",
	subText : String? = null,
	icon : Int = R.drawable.ic_setting,
	isDropdownMenuVisible : Boolean = false,
	dropdownMenuList : List<DropdownMenuItem> = listOf(),
	tint : Color = MaterialTheme.colorScheme.onBackground,
	onClick : (Boolean) -> Unit = {},
) {
	Box(
		modifier = Modifier.clickable { onClick(true) }
	) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier.padding(0.dp, 12.dp, 0.dp, 12.dp)
		) {
			Spacer(modifier = Modifier.width(16.dp))
			Icon(
				painter = painterResource(id = icon),
				contentDescription = text,
				tint = tint,
				modifier = Modifier.requiredSize(ICON_SIZE)
			)
			Spacer(modifier = Modifier.width(24.dp))
			Text(
				text = text,
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onBackground,
				fontWeight = FontWeight.Bold,
				modifier = Modifier.weight(1f),
			)
			Spacer(modifier = Modifier.width(8.dp))
			AnimatedText(
				text = subText ?: "",
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.47f),
			)
			Spacer(modifier = Modifier.width(8.dp))
			Icon(
				painter = painterResource(id = R.drawable.ic_caret),
				contentDescription = text,
				tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.47f),
				modifier = Modifier
					.requiredSize(ICON_SIZE)
					.graphicsLayer { rotationZ = 90f }
			)
			Spacer(modifier = Modifier.width(16.dp))
		}

		DropdownMenu(
			itemList = dropdownMenuList,
			isVisible = isDropdownMenuVisible
		) { onClick(false) }
	}
}
