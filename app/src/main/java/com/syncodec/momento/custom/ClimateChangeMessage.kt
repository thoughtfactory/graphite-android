package com.syncodec.momento.custom

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.syncodec.momento.R
import com.syncodec.momento.custom.modifier.dashedBorder


@Composable
fun ClimateChangeMessage() {

	var expanded by remember{ mutableStateOf(false) }

	Box(
		contentAlignment = Alignment.Center,
		modifier = Modifier
			.fillMaxWidth()
			.padding(24.dp)
			.background(MaterialTheme.colorScheme.background)
			.clip(shape = RoundedCornerShape(12.dp))
			.clickable { expanded = !expanded }
			.dashedBorder(
				width = 2.dp,
				color = MaterialTheme.colorScheme.primary,
				shape = RoundedCornerShape(4.dp),
				on = 8.dp,
				off = 8.dp
			),
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
					color = MaterialTheme.colorScheme.onPrimaryContainer,
					textAlign = TextAlign.Justify,
				)
			}

			ExpandableBox(
				visible = expanded
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
