package com.syncodec.graphite.presentation.note.composable.buildingBlock.renderAttachment

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.text.marqueeText.MarqueeText
import com.syncodec.graphite.utils.mimeTypeIconMap


@Composable
fun RenderGeneric(
	type: String?,
	name: String?,
) {
	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		modifier = Modifier
			.fillMaxSize()
			.padding(8.dp)
	) {
		Spacer(modifier = Modifier.weight(1f))
		Icon(
			painter = painterResource(id = mimeTypeIconMap.getOrDefault(type, R.drawable.ic_file)),
			contentDescription = name,
			tint = MaterialTheme.colorScheme.onSurface,
			modifier = Modifier.requiredSize(48.dp)
		)
		Spacer(modifier = Modifier.height(8.dp))
		Box(
			modifier = Modifier.weight(1f),
			contentAlignment = Alignment.TopCenter
		) {
			MarqueeText(
				text = name ?: "Untitled",
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onSurface
			)
		}
	}
}
