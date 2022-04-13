package com.syncodec.momento.custom.bottomSheet

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.syncodec.momento.miscellaneous.ThemeUtils.Companion.tone


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

	if (subTitle != null) {
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
			style = MaterialTheme.typography.titleSmall,
			color = MaterialTheme.colorScheme.onSurface
		)

		Spacer(modifier = Modifier.weight(1f))

		Icon(
			painter = painterResource(id = icon),
			contentDescription = null,
			tint = MaterialTheme.colorScheme.onSurface,
			modifier = Modifier.requiredSize(20.dp)
		)
	}

	if (subTitle != null) {
		Text(
			text = subTitle,
			style = MaterialTheme.typography.bodySmall,
			color = MaterialTheme.colorScheme.onSurface.tone(isSystemInDarkTheme(), 1),
			maxLines = 2,
			modifier = Modifier
				.fillMaxWidth()
				.padding(24.dp, 0.dp)
		)
	}

	Spacer(modifier = Modifier.height(12.dp))
}
