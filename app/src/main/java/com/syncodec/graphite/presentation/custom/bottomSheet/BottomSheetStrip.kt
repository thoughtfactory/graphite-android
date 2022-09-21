package com.syncodec.graphite.presentation.custom.bottomSheet

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
	contentColor : Color? = MaterialTheme.colorScheme.onSurface,
) {

	val _contentColor = contentColor ?: MaterialTheme.colorScheme.onSurface

	Spacer(modifier = Modifier.height(16.dp))

	Box(
		modifier = Modifier
			.width(96.dp)
			.height(3.dp)
			.background(_contentColor)
	)

	Spacer(modifier = Modifier.height(12.dp))
}
