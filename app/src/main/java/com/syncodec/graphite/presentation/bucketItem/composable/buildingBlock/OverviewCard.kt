package com.syncodec.graphite.presentation.bucketItem.composable.buildingBlock

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.valentinilk.shimmer.shimmer


@Composable
fun OverviewCard(
	overview: String?
) {
	AnimatedVisibility(
		visible = overview != null,
		enter = expandVertically(tween(600)) + fadeIn(tween(1200)),
		exit = shrinkVertically(tween(600)) + fadeOut(tween(1200))
	) {
		Box(
			modifier = Modifier
				.fillMaxWidth()
				.clip(RoundedCornerShape(12.dp))
				.border(1.dp, MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
		) {
			Text(
				text = overview ?: "",
				color = MaterialTheme.colorScheme.onBackground,
				style = MaterialTheme.typography.bodyMedium,
				modifier = Modifier.padding(16.dp),
			)
		}
	}
}

@Composable
fun OverviewShimmerCard() {
	Box(
		modifier = Modifier
			.fillMaxWidth()
			.clip(RoundedCornerShape(12.dp))
			.border(1.dp, MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
			.shimmer()
	) {
		Text(
			text = "Overview",
			color = MaterialTheme.colorScheme.onBackground,
			style = MaterialTheme.typography.bodyMedium,
			modifier = Modifier.padding(16.dp),
		)
	}
}
