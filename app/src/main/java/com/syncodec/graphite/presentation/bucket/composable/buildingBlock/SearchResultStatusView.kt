package com.syncodec.graphite.presentation.bucket.composable.buildingBlock

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.syncodec.graphite.R


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
			.background(MaterialTheme.colorScheme.surface.copy(alpha = 0.71f), MaterialTheme.shapes.medium)
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
			fontFamily = FontFamily(Font(R.font.graduate_regular, FontWeight.Normal)),
			fontWeight = FontWeight.Bold,
			fontSize = 14.sp,
			lineHeight = 16.sp,
			letterSpacing = 2.sp,
			color = MaterialTheme.colorScheme.onSurface,
			textAlign = TextAlign.Center,
			modifier = Modifier
				.fillMaxWidth()
				.padding(horizontal = 24.dp)
		)

		Spacer(modifier = Modifier.height(24.dp))
	}
}
