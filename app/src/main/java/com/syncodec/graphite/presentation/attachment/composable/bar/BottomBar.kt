package com.syncodec.graphite.presentation.attachment.composable.bar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.presentation.common.button.VaultButton


@Preview
@Composable
fun BottomBar() {
	Column(
		modifier = Modifier.fillMaxWidth()
	) {
		Spacer(
			modifier = Modifier
				.fillMaxWidth()
				.height(1.dp)
				.background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.47f))
		)
		BottomAppBar(
			modifier = Modifier.fillMaxWidth(),
			tonalElevation = 0.dp,
			containerColor = MaterialTheme.colorScheme.background,
			contentColor = MaterialTheme.colorScheme.onBackground,
		) {
			Spacer(modifier = Modifier.width(12.dp))

			Spacer(modifier = Modifier.weight(1f))

			VaultButton()

			Spacer(modifier = Modifier.width(12.dp))
		}
	}
}

