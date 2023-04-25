package com.syncodec.graphite.presentation.sync.oneDrive.screen

import android.content.Intent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.report.ReportActivity
import com.syncodec.graphite.presentation.common.scaffold.GenericScaffold
import com.syncodec.graphite.presentation.settings.composable.buildingBlock.SettingButton
import com.syncodec.graphite.presentation.ui.DeleteContainer
import com.syncodec.graphite.presentation.ui.DeleteContent


@OptIn(ExperimentalMaterialApi::class)
@Preview
@Composable
fun OneDriveSyncScreen(
	onClickConnect : () -> Unit = {},
) {
	GenericScaffold(
//		topBar = { TopBar() },
		dialogContent = {
		},
	) {
		Column(
			modifier = Modifier.fillMaxSize()
		) {
			ExperimentalCard()
			SettingButton(
				text = "Connect with Google Drive",
				icon = R.drawable.ic_logo_google_drive,
				tint = Color.Unspecified,
				onClick = onClickConnect,
			)
			SettingButton(
				text = "Test connection",
				icon = R.drawable.ic_test_connection,
			) { }
			SettingButton(
				text = "Disconnect",
				icon = R.drawable.ic_cloud_x,
			)
		}
	}
}

@Preview
@Composable
private fun ExperimentalCard() {
	val context = LocalContext.current

	val infiniteTransition = rememberInfiniteTransition()
	val scale by infiniteTransition.animateFloat(
		initialValue = 1f,
		targetValue = 1.47f,
		animationSpec = infiniteRepeatable(
			animation = tween(470, easing = LinearEasing),
			repeatMode = RepeatMode.Reverse
		)
	)

	Card(
		modifier = Modifier
			.fillMaxWidth()
			.padding(12.dp, 4.dp),
		colors = CardDefaults.cardColors(
			containerColor = Color.DeleteContainer,
			contentColor = Color.DeleteContent,
		),
	) {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.padding(16.dp)
		) {
			Row(
				modifier = Modifier.fillMaxWidth()
			) {
				Icon(
					painter = painterResource(id = R.drawable.ic_exclamation),
					contentDescription = "Experimental",
					modifier = Modifier
						.requiredSize(24.dp)
						.graphicsLayer {
							scaleX = scale
							scaleY = scale
						}
				)
				Spacer(modifier = Modifier.width(12.dp))
				Text(
					text = "Experimental",
					style = MaterialTheme.typography.titleMedium,
					fontWeight = FontWeight.Bold
				)
			}
			Spacer(modifier = Modifier.height(12.dp))
			Text(
				text = "This feature is experimental and may not work as expected",
				style = MaterialTheme.typography.bodyMedium,
			)

			Spacer(modifier = Modifier.height(24.dp))

			Button(
				colors = ButtonDefaults.buttonColors(
					containerColor = Color.DeleteContent,
					contentColor = Color.DeleteContainer,
				),
				onClick = { context.startActivity(Intent(context, ReportActivity::class.java)) },
				modifier = Modifier.fillMaxWidth(),
			) {
				Text(text = "Report a bug")
			}
		}
	}
}
