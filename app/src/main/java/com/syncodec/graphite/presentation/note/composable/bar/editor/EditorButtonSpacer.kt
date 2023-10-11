package com.syncodec.graphite.presentation.note.composable.bar.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp


@Composable
fun EditorButtonSpacer() {
	Row(
		modifier = Modifier.height(40.dp),
		verticalAlignment = Alignment.CenterVertically
	) {
		Spacer(modifier = Modifier.width(4.dp))
		Spacer(
			modifier = Modifier
				.width(2.dp)
				.height(20.dp)
				.clip(RoundedCornerShape(50))
				.background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.47f))
		)
		Spacer(modifier = Modifier.width(4.dp))
	}
}

