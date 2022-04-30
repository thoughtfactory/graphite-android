package com.syncodec.graphite.custom

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R


@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun ClimateChangeMessage() {

	var expanded by remember{ mutableStateOf(false) }

	Card(
		modifier = Modifier
			.fillMaxWidth()
			.padding(24.dp)
			.background(Color.Transparent),
		shape = RoundedCornerShape(12.dp),
		border = BorderStroke(2.dp, MaterialTheme.colorScheme.primaryContainer),
		onClick = {expanded = !expanded}
	) {
		Column(
			horizontalAlignment = Alignment.CenterHorizontally,
			modifier = Modifier
				.padding(16.dp),
		) {
			Row(
				verticalAlignment = Alignment.CenterVertically,
			) {
				Icon(
					painter = painterResource(id = R.drawable.ic_green),
					contentDescription = null,
					tint = Color.Unspecified,
					modifier = Modifier
						.requiredSize(48.dp)
						.padding(0.dp, 0.dp, 12.dp, 0.dp)
				)
				Text(
					text = "Internet is already responsible for 2% of greenhouse gas emission. Search only for most accurate title.",
					style = MaterialTheme.typography.bodySmall,
					fontWeight = FontWeight.Bold,
					color = MaterialTheme.colorScheme.onPrimaryContainer,
					textAlign = TextAlign.Justify,
				)
			}

			ExpandableBox(
				isVisible = expanded
			) {
				Column {
					Spacer(modifier = Modifier.height(16.dp))

					Text(
						text = "There are two numbers you need to know about climate change. The first is 51 billion. The other is zero. Fifty-one billion is how many tons of greenhouse gases the world typically adds to the atmosphere every year, and zero is our target by 2050",
						style = MaterialTheme.typography.bodySmall,
						color = MaterialTheme.colorScheme.onBackground,
						textAlign = TextAlign.Justify,
					)

					Spacer(modifier = Modifier.height(8.dp))

					Text(
						text = "~ How to avoid a climate disaster, Bill Gates",
						style = MaterialTheme.typography.bodySmall,
						color = MaterialTheme.colorScheme.onBackground,
						textAlign = TextAlign.End,
						modifier = Modifier
							.fillMaxWidth()
					)
				}
			}
		}
	}
}
