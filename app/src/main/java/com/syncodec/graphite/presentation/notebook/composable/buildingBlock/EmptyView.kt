package com.syncodec.graphite.presentation.notebook.composable.buildingBlock

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R


@Composable
fun EmptyView() {
	val configuration = LocalConfiguration.current
	val screenWidth = configuration.screenWidthDp.dp

	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.Center,
		modifier = Modifier
			.fillMaxSize()
			.background(MaterialTheme.colorScheme.background)
	) {
		Image(
			painter = painterResource(id = R.drawable.il_not_found),
			contentDescription = "No entries found",
			modifier = Modifier.size(screenWidth * 3 / 4)
		)
		Spacer(modifier = Modifier.height(16.dp))
		Text(
			text = "No entries found",
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onBackground,
			fontWeight = FontWeight.Bold
		)
	}
}
