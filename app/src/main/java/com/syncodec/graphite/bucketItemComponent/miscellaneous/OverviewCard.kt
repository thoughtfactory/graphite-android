package com.syncodec.graphite.bucketItemComponent.miscellaneous

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverviewCard(
	overview: String
) {
	Card(
		modifier = Modifier.fillMaxWidth(),
		colors = CardDefaults.cardColors(Color.Transparent),
		elevation = CardDefaults.cardElevation(0.dp),
		border = BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground),
		shape = RoundedCornerShape(12.dp),
	) {
		Column(
			modifier = Modifier.padding(16.dp)
		) {
			Text(
				text = overview,
				color = MaterialTheme.colorScheme.onBackground,
				style = MaterialTheme.typography.bodyMedium,
				modifier = Modifier,
			)
		}
	}
}
