package com.syncodec.graphite.presentation.bucket.composable.buildingBlock

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp


@Composable
fun SearchResultStatusView(
	imageId : Int,
	text : String,
	contentDescription : String,
) {
	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		modifier = Modifier
			.heightIn(256.dp)
			.padding(24.dp, 0.dp)
			.background(MaterialTheme.colorScheme.background.copy(alpha = 0.71f), RoundedCornerShape(24.dp))
	) {
		Spacer(modifier = Modifier.height(24.dp))
		Image(
			painter = painterResource(id = imageId),
			contentDescription = contentDescription,
			modifier = Modifier.fillMaxWidth(0.71f)
		)

		Spacer(modifier = Modifier.height(16.dp))

		Text(
			text = text,
			style = MaterialTheme.typography.bodyLarge,
			color = MaterialTheme.colorScheme.onSurface,
			textAlign = TextAlign.Center,
			fontWeight = FontWeight.Bold,
			modifier = Modifier
				.fillMaxWidth()
				.padding(24.dp, 0.dp)
		)

		Spacer(modifier = Modifier.height(24.dp))
	}
}
