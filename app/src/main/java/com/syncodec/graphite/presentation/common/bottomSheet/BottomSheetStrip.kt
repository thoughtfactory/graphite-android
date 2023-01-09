package com.syncodec.graphite.presentation.common.bottomSheet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp


@Composable
fun BottomSheetStrip(
	contentColor : Color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.47f)
) {
	Box(
		modifier = Modifier
			.width(48.dp)
			.height(4.dp)
			.background(contentColor, RoundedCornerShape(50))
	)

	Spacer(modifier = Modifier.height(8.dp))
}
