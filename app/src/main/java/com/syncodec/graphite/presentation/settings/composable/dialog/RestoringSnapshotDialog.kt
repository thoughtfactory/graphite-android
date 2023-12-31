package com.syncodec.graphite.presentation.settings.composable.dialog

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.dialog.dialog2.GenericAlertDialog2
import com.syncodec.graphite.presentation.common.dialog.dialog2.GenericDialog2
import com.syncodec.graphite.presentation.common.dialog.dialog2.GenericDialogDefaults


@Preview
@Composable
fun RestoringSnapshotDialog(
	isDialogVisible: Boolean = true,
) {
	GenericDialog2(
		isDialogVisible = isDialogVisible,
		icon = GenericDialogDefaults.genericDialogIcon(icon = R.drawable.ic_fa_restore),
		title = stringResource(id = R.string.restoring_snapshot),
	) {
		LinearProgressIndicator(
			modifier = Modifier.fillMaxWidth()
		)
	}
}
