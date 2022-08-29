package com.syncodec.graphite.presentation.main.composable.buildingBlock

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp


data class DropdownMenuItem(
	val title: String,
	val icon: Int?,
	val onClick: () -> Unit
)

@Composable
fun DropdownMenu(
	itemList: List<DropdownMenuItem>,
	isVisible: Boolean,
	offset: DpOffset? = null,
	onDismissRequest: () -> Unit,
) {
	val configuration = LocalConfiguration.current
	val screenWidth = configuration.screenWidthDp.dp
	val screenHeight = configuration.screenHeightDp.dp

	DropdownMenu(
		expanded = isVisible,
		onDismissRequest = onDismissRequest,
		modifier = Modifier.background(MaterialTheme.colorScheme.background),
		offset = offset ?: DpOffset(screenWidth, screenHeight)
	) {
		itemList.forEach {
			DropdownMenuItem(
				onClick = it.onClick,
				leadingIcon = {
					if (it.icon != null) {
						Icon(
							painter = painterResource(id = it.icon),
							contentDescription = null,
							tint = MaterialTheme.colorScheme.onBackground,
							modifier = Modifier.requiredSize(20.dp)
						)
					}
				},
				text = {
					Text(
						text = it.title,
						style = MaterialTheme.typography.bodyMedium,
						color = MaterialTheme.colorScheme.onBackground,
						fontWeight = FontWeight.Bold,
					)
				},
			)
		}
	}
}
