package com.syncodec.graphite.presentation.common.component.note

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp


@Preview
@Composable
fun NoteGroupHeader(
	modifier: Modifier = Modifier,
	text : String = "",
	subText : String? = null
) {
	Row(
		verticalAlignment = Alignment.CenterVertically,
		modifier = modifier
			.fillMaxWidth()
			.background(color = MaterialTheme.colorScheme.background)
			.padding(top = 12.dp, bottom = 4.dp)
	) {
		Spacer(modifier = Modifier.width(12.dp))
		Text(
			text = text,
			color = MaterialTheme.colorScheme.onBackground,
			style = MaterialTheme.typography.titleMedium,
			fontWeight = FontWeight.Bold
		)

		Spacer(modifier = Modifier.weight(1f))

		subText?.let {
			Text(
				text = it,
				color = MaterialTheme.colorScheme.onBackground,
				style = MaterialTheme.typography.titleSmall,
			)
		}

		Spacer(modifier = Modifier.width(12.dp))
	}
}
