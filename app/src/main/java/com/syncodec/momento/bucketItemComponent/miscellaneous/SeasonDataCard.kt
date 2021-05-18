package com.syncodec.momento.bucketItemComponent.miscellaneous

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.syncodec.momento.database.bucket.SeasonData


@Composable
fun SeasonDataCard(seasonDataList: List<SeasonData>) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.horizontalScroll(rememberScrollState())
	) {
		seasonDataList.forEachIndexed { index, seasonData ->
			SeasonCard(seasonData = seasonData)
			if (index != seasonDataList.size -1) Spacer(modifier = Modifier.width(8.dp))
		}
	}
}

@Composable
private fun SeasonCard(seasonData: SeasonData) {
	Card(
		modifier = Modifier,
		elevation = 0.dp,
		shape = RoundedCornerShape(50),
		border = BorderStroke(2.dp, MaterialTheme.colorScheme.secondaryContainer),
		backgroundColor = Color.Transparent,
	) {
		Text(
			text = "Season ${seasonData.seasonNo}",
			color = MaterialTheme.colorScheme.onBackground,
			style = MaterialTheme.typography.bodyMedium,
			fontWeight = FontWeight.Bold,
			modifier = Modifier
				.padding(12.dp),
		)
	}
}
