package com.syncodec.graphite.presentation.custom.info

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoCard(
	title: String,
	icon: Int,
	color: Color,
	text: String
) {
	Card(
		shape = RoundedCornerShape(12.dp),
		colors = CardDefaults.cardColors(color),
		modifier = Modifier
			.fillMaxWidth()
			.padding(12.dp),
	) {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.padding(12.dp)
		) {
			Row(
				modifier = Modifier,
				verticalAlignment = Alignment.CenterVertically
			) {
				Icon(
					painter = painterResource(id = icon),
					contentDescription = title,
					tint = Color.White,
					modifier = Modifier.requiredSize(24.dp)
				)
				Spacer(modifier = Modifier.width(8.dp))
				Text(
					text = title,
					style = MaterialTheme.typography.titleMedium,
					color = Color.White
				)
			}
			Spacer(modifier = Modifier.width(4.dp))
			Row(modifier = Modifier) {
				Spacer(modifier = Modifier.width(40.dp))
				Text(
					text = text,
					style = MaterialTheme.typography.titleSmall,
					color = Color.White
				)
			}
		}
	}
}
