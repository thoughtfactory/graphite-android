package com.syncodec.graphite.presentation.common.bottomSheet.bottomSheetButtonGrid

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@Composable
fun ColumnScope.BottomSheetButtonGrid(
	buttonList : List<(@Composable () -> Unit)?>,
) {
	this.apply {
		for (i in 0 until ((buttonList.size - 1) / 3) + 1) {
			Row(
				modifier = Modifier.fillMaxWidth()
			) {
				for (j in 0 until 3) {
					val buttonData = buttonList.getOrNull(i * 3 + j)
					buttonData?.let {
						Box(
							modifier = Modifier.weight(1f)
						) {
							it()
						}
					} ?: Spacer(modifier = Modifier.weight(1f))
					if (j != 2) Spacer(modifier = Modifier.width(4.dp))
				}
			}
			if (i != ((buttonList.size - 1) / 3)) Spacer(modifier = Modifier.height(4.dp))
		}
	}
}
