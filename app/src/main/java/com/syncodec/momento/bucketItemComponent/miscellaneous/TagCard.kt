package com.syncodec.momento.bucketItemComponent.miscellaneous

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.flowlayout.MainAxisAlignment

@Composable
fun TagCard(
	tagList: List<String>
) {
	FlowRow(
		modifier = Modifier
			.fillMaxWidth(),
		mainAxisAlignment = MainAxisAlignment.Start,
		mainAxisSpacing = 8.dp
	) {
		tagList.forEach { Tag(tag = it) }
	}
}

@Composable
private fun Tag(tag: String) {
	Card(
		modifier = Modifier,
		elevation = 0.dp,
		shape = RoundedCornerShape(50),
		border = BorderStroke(2.dp, MaterialTheme.colorScheme.secondaryContainer),
		backgroundColor = Color.Transparent,
	) {
		Text(
			text = "#$tag",
			color = MaterialTheme.colorScheme.onBackground,
			style = MaterialTheme.typography.bodyMedium,
			fontWeight = FontWeight.Bold,
			modifier = Modifier
				.padding(12.dp),
		)
	}
}
