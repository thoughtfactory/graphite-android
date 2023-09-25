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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.base.ICON_BUTTON_SIZE


@Preview
@Composable
fun BottomSheetButton(
	modifier: Modifier = Modifier,
	title: String = "Title",
	icon: Int = R.drawable.ic_menu,
	containerColor: Color = MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp).copy(alpha = 0.31f),
	contentColor: Color = MaterialTheme.colorScheme.onSurface,
	onClick: () -> Unit = {}
) {
	Column(
		verticalArrangement = Arrangement.Center,
		horizontalAlignment = Alignment.CenterHorizontally,
		modifier = modifier
			.background(containerColor, MaterialTheme.shapes.large)
			.clip(MaterialTheme.shapes.large)
			.clickable(onClick = onClick)
	) {
		Spacer(modifier = Modifier.height(12.dp))
		Icon(
			painter = painterResource(id = icon),
			contentDescription = title,
			tint = contentColor,
			modifier = Modifier.requiredSize(ICON_BUTTON_SIZE)
		)
		Spacer(modifier = Modifier.height(4.dp))
		Text(
			text = title,
			style = MaterialTheme.typography.bodyMedium,
			color = contentColor,
			textAlign = TextAlign.Center,
			maxLines = 1,
			overflow = TextOverflow.Ellipsis,
			modifier = Modifier
				.fillMaxWidth()
				.padding(8.dp, 0.dp)
		)
		Spacer(modifier = Modifier.height(12.dp))
	}
}
