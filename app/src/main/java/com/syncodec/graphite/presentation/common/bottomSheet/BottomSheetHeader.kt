package com.syncodec.graphite.presentation.common.bottomSheet

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
	contentColor: Color = MaterialTheme.colorScheme.onBackground,
) {

	Row(
		verticalAlignment = Alignment.CenterVertically,
		modifier = Modifier.fillMaxWidth()
	) {
		Text(
			text = title,
			style = MaterialTheme.typography.titleMedium,
			color = contentColor,
			fontWeight = FontWeight.Bold
		)

		Spacer(modifier = Modifier.weight(1f))

		Icon(
			painter = painterResource(id = icon),
			contentDescription = null,
			tint = contentColor,
			modifier = Modifier.requiredSize(24.dp)
		)
	}

	if (subTitle != null) {
		Text(
			text = subTitle,
			style = MaterialTheme.typography.bodyMedium,
			color = contentColor,
			maxLines = 2,
			modifier = Modifier
				.fillMaxWidth()
				.padding(24.dp, 0.dp)
		)
	}

	Spacer(modifier = Modifier.height(12.dp))
}
