package com.syncodec.graphite.presentation.common.bottomSheet.bottomSheetButtonGrid

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp


data class BottomSheetButtonData(
	val title: String,
	val icon: Int,
	val containerColor: Color? = null,
	val contentColor: Color? = null,
	val onClick: () -> Unit
)

@Composable
fun BottomSheetButton(
	modifier: Modifier,
	title: String,
	icon: Int,
	containerColor: Color? = null,
	contentColor: Color? = null,
	onClick: () -> Unit
) {
	val _containerColor = containerColor ?: MaterialTheme.colorScheme.background.copy(alpha = 0.71f)
	val _contentColor = contentColor ?: MaterialTheme.colorScheme.onBackground

	Column(
		verticalArrangement = Arrangement.Center,
		horizontalAlignment = Alignment.CenterHorizontally,
		modifier = modifier
			.background(_containerColor, RoundedCornerShape(20.dp))
			.clip(RoundedCornerShape(20.dp))
			.clickable(onClick = onClick)
	) {
		Spacer(modifier = Modifier.height(16.dp))
		Icon(
			painter = painterResource(id = icon),
			contentDescription = title,
			tint = _contentColor,
			modifier = Modifier.requiredSize(24.dp)
		)
		Spacer(modifier = Modifier.height(4.dp))
		Text(
			text = title,
			style = MaterialTheme.typography.bodyMedium,
			color = _contentColor,
			textAlign = TextAlign.Center,
			maxLines = 1,
			overflow = TextOverflow.Ellipsis,
			modifier = Modifier
				.fillMaxWidth()
				.padding(12.dp, 0.dp)
		)
		Spacer(modifier = Modifier.height(16.dp))
	}
}
