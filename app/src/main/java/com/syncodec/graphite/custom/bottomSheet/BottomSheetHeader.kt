package com.syncodec.graphite.custom.bottomSheet

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp


@Composable
fun BottomSheetHeader(
	title: String,
	icon: Int,
	subTitle: String? = null
) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.padding(24.dp, 0.dp),
		verticalAlignment = Alignment.CenterVertically
	) {
		Text(
			text = title,
			style = MaterialTheme.typography.titleLarge,
			color = MaterialTheme.colorScheme.primary
		)

		Spacer(modifier = Modifier.weight(1f))

		Icon(
			painter = painterResource(id = icon),
			contentDescription = null,
			tint = MaterialTheme.colorScheme.primary,
			modifier = Modifier.requiredSize(24.dp)
		)
	}

	if (subTitle != null) {
		Text(
			text = subTitle,
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onSurface,
			maxLines = 2,
			modifier = Modifier
				.fillMaxWidth()
				.padding(24.dp, 0.dp)
		)
	}

	Spacer(modifier = Modifier.height(12.dp))
}
