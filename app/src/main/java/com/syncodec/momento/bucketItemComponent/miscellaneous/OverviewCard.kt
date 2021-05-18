package com.syncodec.momento.bucketItemComponent.miscellaneous

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun OverviewCard(
	overview: String
) {
	Card(
		modifier = Modifier
			.fillMaxWidth(),
		elevation = 0.dp,
		shape = RoundedCornerShape(12.dp),
		border = BorderStroke(2.dp, MaterialTheme.colorScheme.secondaryContainer),
		backgroundColor = Color.Transparent,
	) {
		Column(
			modifier = Modifier
				.padding(16.dp)
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
