package com.syncodec.graphite.presentation.common.bottomSheet.bottomSheetButtonGrid

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@Composable
fun BottomSheetButtonGrid(
	buttonList: List<BottomSheetButtonData>,
) {
	for (i in 0 until (buttonList.size / 3) + 1) {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.padding(24.dp, 0.dp)
		) {
			Row(
				modifier = Modifier.fillMaxWidth()
			) {
				for (j in 0 until 3) {
					val buttonData = buttonList.getOrNull(i * 3 + j)
					if (buttonData != null) {
						BottomSheetButton(
							title = buttonData.title,
							icon = buttonData.icon,
							containerColor = buttonData.containerColor,
							contentColor = buttonData.contentColor,
							modifier = Modifier.weight(1f),
							onClick = buttonData.onClick
						)
					} else {
						Box(modifier = Modifier.weight(1f))
					}
					if (j != 2) Spacer(modifier = Modifier.width(8.dp))
				}
			}
			if (i != (buttonList.size / 3)) {
				Spacer(modifier = Modifier.height(8.dp))
			}
		}
	}
}
