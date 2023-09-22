package com.syncodec.graphite.presentation.settings.composable.dialog

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.documentfile.provider.DocumentFile
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.dialog.dialog2.GenericDialog2
import com.syncodec.graphite.presentation.common.dialog.dialog2.GenericDialogDefaults
import com.syncodec.graphite.presentation.settings.composable.viewModel.LocalBackupViewModel
import com.syncodec.graphite.utils.timeStampToPrettyFull


@Preview
@Composable
fun RestoreSnapshotDialog(
	isDialogVisible: Boolean = true,
	onDismissRequest: () -> Unit = {},
	documentFile: DocumentFile? = null,
	snapshotMetadata: LocalBackupViewModel.Companion.SnapshotMetadata? = null,
	onClickRestore: (DocumentFile) -> Unit = {},
) {
	GenericDialog2(
		isDialogVisible = isDialogVisible,
		icon = GenericDialogDefaults.genericDialogWarningIcon(),
		title = stringResource(id = R.string.restore_snapshot),
		contentText = stringResource(id = R.string.restore_snapshot_content),
		primaryButton = GenericDialogDefaults.genericDialogButtonPrimary(text = stringResource(id = R.string.restore), onClick = { documentFile?.let(onClickRestore) }),
		secondaryButton = GenericDialogDefaults.genericDialogButtonDismiss(onClick = onDismissRequest),
		onDismissRequest = onDismissRequest,
		content = snapshotMetadata?.let {
			{
				Text(
					text = it.timestamp.timeStampToPrettyFull(),
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.onBackground,
					fontWeight = FontWeight.Bold
				)
				Spacer(modifier = Modifier.height(12.dp))
				SnapshotDataItemView(title = stringResource(id = R.string.note), count = it.noteCount)
				SnapshotDataItemView(title = stringResource(id = R.string.chapter), count = it.chapterCount)
				SnapshotDataItemView(title = stringResource(id = R.string.bucket), count = it.bucketItemCount)
				SnapshotDataItemView(title = stringResource(id = R.string.bucket_item), count = it.bucketCount)
				SnapshotDataItemView(title = stringResource(id = R.string.tag), count = it.tagCount)
				SnapshotDataItemView(title = stringResource(id = R.string.attachments), count = it.attachmentCount)
			}
		}
	)
}

@Preview
@Composable
private fun SnapshotDataItemView(
	title: String = "Note",
	count: Int = 71
) {
	Row(
		verticalAlignment = Alignment.CenterVertically,
		modifier = Modifier
			.fillMaxWidth()
			.padding(vertical = 4.dp)
	) {
		Text(
			text = title,
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onBackground,
		)
		Spacer(modifier = Modifier.weight(1f))
		Text(
			text = "$count",
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onBackground,
			fontWeight = FontWeight.Bold,
		)
	}
}
