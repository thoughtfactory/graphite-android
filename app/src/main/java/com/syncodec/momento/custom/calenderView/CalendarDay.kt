package com.syncodec.momento.custom.calenderView

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp

@Preview
@Composable
fun CalendarDay(
	modifier: Modifier = Modifier,
	@PreviewParameter(CalendarDayPreviewParameter::class)
	date: Int? = null,
) {
	Card(
		backgroundColor = Color.White.copy(alpha = 0.23f),
		elevation = 0.dp,
		modifier = modifier
	) {
		if (date != null)
			Text(
				text = "$date",
				textAlign = TextAlign.Center,
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onPrimaryContainer,
				modifier = Modifier
					.fillMaxWidth()
					.fillMaxHeight()
					.wrapContentHeight(align = Alignment.CenterVertically)
			)

	}
}

private class CalendarDayPreviewParameter : PreviewParameterProvider<String> {
	override val values = sequenceOf("1", "2", "3", "4")
}
