package com.syncodec.momento.custom

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp


@Composable
fun BottomSheetHeader(
	title: String,
	imageVector: ImageVector,
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
			style = MaterialTheme.typography.titleSmall,
			color = MaterialTheme.colorScheme.onBackground
		)

		Spacer(modifier = Modifier.weight(1f))

		Icon(
			imageVector = imageVector,
			contentDescription = null,
			tint = MaterialTheme.colorScheme.onBackground
		)
	}

	if (subTitle!=null) {
		Text(
			text = subTitle,
			style = MaterialTheme.typography.bodySmall,
			color = MaterialTheme.colorScheme.onBackground,
			maxLines = 2,
			modifier = Modifier
				.fillMaxWidth()
				.padding(24.dp, 0.dp)
				.alpha(0.47f)
		)
	}

	Spacer(modifier = Modifier.height(12.dp))
}

@Composable
fun BottomSheetHeader(
	title: String,
	painter: Painter,
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
			style = MaterialTheme.typography.titleSmall,
			color = MaterialTheme.colorScheme.onBackground
		)

		Spacer(modifier = Modifier.weight(1f))

		Icon(
			painter = painter,
			contentDescription = null,
			tint = MaterialTheme.colorScheme.onBackground
		)
	}

	if (subTitle!=null) {
		Text(
			text = subTitle,
			style = MaterialTheme.typography.bodySmall,
			color = MaterialTheme.colorScheme.onBackground,
			maxLines = 2,
			modifier = Modifier
				.fillMaxWidth()
				.padding(24.dp, 0.dp)
				.alpha(0.47f)
		)
	}

	Spacer(modifier = Modifier.height(12.dp))
}
