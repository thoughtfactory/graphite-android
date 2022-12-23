package com.syncodec.graphite.presentation.main.composable.bottomSheet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.bottomSheet.BottomSheetHeader
import com.syncodec.graphite.presentation.common.bottomSheet.BottomSheetStrip
import com.syncodec.graphite.presentation.ui.DeleteContainer
import com.syncodec.graphite.presentation.ui.DeleteContent


@Composable
fun SyncBottomSheet(
	onClickSyncNow: () -> Unit,
	onClickForceSync: () -> Unit,
) {

	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		modifier = Modifier
			.fillMaxWidth()
			.background(MaterialTheme.colorScheme.surface)
	) {

		BottomSheetStrip()

		BottomSheetHeader(
			title = "Synchronization",
			icon = R.drawable.ic_sync
		)

		Spacer(modifier = Modifier.height(8.dp))

		Box(
			modifier = Modifier
				.fillMaxWidth()
				.padding(24.dp, 0.dp)
				.background(MaterialTheme.colorScheme.background, RoundedCornerShape(16.dp))
		) {
			Row(
				verticalAlignment = Alignment.CenterVertically,
				modifier = Modifier
					.fillMaxWidth()
					.padding(16.dp, 12.dp)
			) {
				Icon(
					painter = painterResource(id = R.drawable.ic_dropbox),
					contentDescription = "Dropbox",
					tint = MaterialTheme.colorScheme.onSurface,
					modifier = Modifier.size(32.dp)
				)

				Spacer(modifier = Modifier.width(16.dp))

				Column(
					horizontalAlignment = Alignment.Start,
					modifier = Modifier.weight(1f)
				) {
					Text(
						text = "pnp.parmar@gmail.com",
						style = MaterialTheme.typography.bodyLarge,
						color = MaterialTheme.colorScheme.onSurface,
						fontWeight = FontWeight.Bold,
						modifier = Modifier
					)

					Text(
						text = "Last synced 2 days ago",
						style = MaterialTheme.typography.bodyMedium,
						color = MaterialTheme.colorScheme.onSurface,
						modifier = Modifier
					)
				}
			}
		}

		Spacer(modifier = Modifier.height(8.dp))

		Button(
			onClick = onClickSyncNow,
			modifier = Modifier
				.fillMaxWidth()
				.padding(24.dp, 0.dp),
			colors = ButtonDefaults.buttonColors(
				containerColor = MaterialTheme.colorScheme.primary,
				contentColor = MaterialTheme.colorScheme.onPrimary,
			)
		) {
			Text(
				text = "Sync Now",
				modifier = Modifier
			)
		}

		Button(
			onClick = onClickForceSync,
			modifier = Modifier
				.fillMaxWidth()
				.padding(24.dp, 0.dp),
			colors = ButtonDefaults.buttonColors(
				containerColor = Color.Companion.DeleteContainer,
				contentColor = Color.Companion.DeleteContent,
			)
		) {
			Text(
				text = "Force Sync",
				modifier = Modifier
			)
		}

		Spacer(modifier = Modifier.height(32.dp))
	}
}
