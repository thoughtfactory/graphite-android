package com.syncodec.graphite.presentation.sync.dropbox2.composable.dropboxConnectionCard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedCard
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp


@Preview
@Composable
fun DropboxConnectingCard(
	modifier: Modifier = Modifier,
) {
	OutlinedCard(
		modifier = modifier,
		colors = CardDefaults.outlinedCardColors(
			containerColor = Color.Transparent
		)
	) {
		Column(
			horizontalAlignment = Alignment.CenterHorizontally,
			verticalArrangement = Arrangement.Center,
			modifier = Modifier
				.fillMaxWidth()
				.height(128.dp)
				.padding(16.dp)
		) {
			CircularProgressIndicator()
		}
	}
}
