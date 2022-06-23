package com.syncodec.graphite.bucketItemComponent.miscellaneous

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Tag(tag: String) {
	Card(
		colors = CardDefaults.cardColors(Color.Transparent),
		elevation = CardDefaults.cardElevation(0.dp),
		border = BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground),
		shape = RoundedCornerShape(50),
	) {
		Text(
			text = "# $tag",
			color = MaterialTheme.colorScheme.onBackground,
			style = MaterialTheme.typography.bodyMedium,
			modifier = Modifier.padding(12.dp),
		)
	}
}
