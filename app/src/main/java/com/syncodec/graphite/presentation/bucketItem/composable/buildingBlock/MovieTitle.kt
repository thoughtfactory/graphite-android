package com.syncodec.graphite.presentation.bucketItem.composable.buildingBlock

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import com.syncodec.graphite.R


@Preview
@Composable
fun MovieTitleView(
	title: String? = null,
	tagLine : String? = null,
) {
	Column(
		modifier = Modifier
			.fillMaxWidth()
			.padding(2.dp)
			.background(
				Color(
					ColorUtils.blendARGB(
						MaterialTheme.colorScheme
							.surfaceColorAtElevation(8.dp)
							.toArgb(), MaterialTheme.colorScheme.background.toArgb(), 0.88f
					)
				),
				MaterialTheme.shapes.medium
			)
			.padding(16.dp)
	) {
		title?.let {
			Text(
				text = it,
				style = MaterialTheme.typography.titleLarge,
				fontWeight = FontWeight.Bold
			)
		} ?: Text(
			text = stringResource(id = R.string.untitled),
			style = MaterialTheme.typography.titleLarge,
			fontStyle = FontStyle.Italic,
		)
		tagLine?.let {
			Spacer(modifier = Modifier.height(4.dp))
			Text(
				text = it,
				style = MaterialTheme.typography.bodyMedium,
				fontWeight = FontWeight.Bold
			)
		}
	}
}

