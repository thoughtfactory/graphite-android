package com.syncodec.graphite.presentation.settings.composable.dialog

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.dialog.GenericDialog2
import com.syncodec.graphite.presentation.common.dialog.GenericDialogDefaults


@Preview
@Composable
fun ImportingDialog(
	showDialog: Boolean = true,
	processedSize: Int = 0,
	totalSize: Int = 0,
) {
	GenericDialog2(
		isDialogVisible = showDialog,
		icon = GenericDialogDefaults.genericDialogIcon(icon = R.drawable.ic_flat_import),
		title = "Importing",
		content = {
			Text(
				text = "Please wait while your data is being imported.",
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onBackground,
			)
			Spacer(modifier = Modifier.height(8.dp))
			ProcessedView(
				processedSize = processedSize,
				totalSize = totalSize,
			)
		}
	)
}

@Preview
@Composable
private fun ProcessedView(
	processedSize: Int = 0,
	totalSize: Int = 0,
) {
	Row(
		verticalAlignment = Alignment.CenterVertically,
		modifier = Modifier.fillMaxWidth(),
	) {
		LinearProgressIndicator(
			progress = processedSize.toFloat() / (totalSize + 1).toFloat(),
			color = MaterialTheme.colorScheme.primary,
			trackColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.13f),
			strokeCap = StrokeCap.Round,
			modifier = Modifier
				.weight(1f)
				.height(6.dp)
		)
		Spacer(modifier = Modifier.width(12.dp))
		Text(
			text = "$processedSize/$totalSize",
			fontWeight = FontWeight.Bold,
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onBackground,
		)
	}
}
