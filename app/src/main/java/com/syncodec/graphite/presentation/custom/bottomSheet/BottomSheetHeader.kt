package com.syncodec.graphite.presentation.custom.bottomSheet

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp


@Composable
fun BottomSheetHeader(
	title: String,
	icon: Int,
	subTitle: String? = null,
	contentColor: Color? = null,
) {

	val _contentColor = contentColor ?: MaterialTheme.colorScheme.onSurface

	Row(
		verticalAlignment = Alignment.CenterVertically,
		modifier = Modifier
			.fillMaxWidth()
			.padding(24.dp, 0.dp)
	) {
		Text(
			text = title,
			style = MaterialTheme.typography.titleLarge,
			color = _contentColor,
			fontWeight = FontWeight.Bold
		)

		Spacer(modifier = Modifier.weight(1f))

		Icon(
			painter = painterResource(id = icon),
			contentDescription = null,
			tint = _contentColor,
			modifier = Modifier.requiredSize(24.dp)
		)
	}

	if (subTitle != null) {
		Text(
			text = subTitle,
			style = MaterialTheme.typography.bodyMedium,
			color = _contentColor,
			maxLines = 2,
			modifier = Modifier
				.fillMaxWidth()
				.padding(24.dp, 0.dp)
		)
	}

	Spacer(modifier = Modifier.height(12.dp))
}
