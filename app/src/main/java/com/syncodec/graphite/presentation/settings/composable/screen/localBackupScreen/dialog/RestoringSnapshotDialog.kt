package com.syncodec.graphite.presentation.settings.composable.screen.localBackupScreen.dialog

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.syncodec.graphite.presentation.common.dialog.GenericDialog


@Preview
@Composable
fun RestoringSnapshotDialog(
	showDialog : Boolean = true,
) {
	GenericDialog(
		showDialog = showDialog,
		title = "Restoring Snapshot",
		onDismissRequest = { /*TODO*/ }
	) {
		LinearProgressIndicator(
			color = MaterialTheme.colorScheme.onBackground,
			trackColor = MaterialTheme.colorScheme.background,
			modifier = Modifier.fillMaxWidth(),
		)
	}
}
