package com.syncodec.graphite.presentation.main.composable.bottomSheet.syncBottomSheet.buildingBlock

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R


@Preview
@Composable
fun NotLoggedInView(
	onClickManage : () -> Unit = {},
) {
	val context = LocalContext.current
	Column(
		modifier = Modifier.fillMaxWidth()
	) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier
				.fillMaxWidth()
				.background(
					MaterialTheme.colorScheme
						.surfaceColorAtElevation(8.dp)
						.copy(alpha = 0.13f), MaterialTheme.shapes.medium
				)
				.padding(12.dp, 8.dp),
		) {
			Icon(
				painter = painterResource(id = R.drawable.im_cloud_storage),
				contentDescription = "Cloud Storage",
				tint = Color.Unspecified,
				modifier = Modifier.requiredSize(48.dp)
			)
			Spacer(modifier = Modifier.width(8.dp))
			Column(
				modifier = Modifier.weight(1f)
			) {
				Text(
					text = "Keep your data safe in the cloud",
					style = MaterialTheme.typography.titleMedium,
					color = MaterialTheme.colorScheme.onSurface,
					fontWeight = FontWeight.Bold,
				)
				Text(
					text = "Sync your data across devices",
					style = MaterialTheme.typography.bodySmall,
					color = MaterialTheme.colorScheme.onSurface,
				)
			}
		}

		Spacer(modifier = Modifier.height(6.dp))

		Button(
			colors = ButtonDefaults.buttonColors(
				containerColor = MaterialTheme.colorScheme.primary,
				contentColor = MaterialTheme.colorScheme.onPrimary,
			),
			shape = MaterialTheme.shapes.medium,
			modifier = Modifier.fillMaxWidth(),
			onClick = onClickManage,
		) {
			Text(text = "Manage")
		}
	}
}
