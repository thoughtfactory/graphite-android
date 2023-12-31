package com.syncodec.graphite.presentation.settings.composable.dialog

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.documentfile.provider.DocumentFile
import com.syncodec.graphite.BuildConfig
import com.syncodec.graphite.R
import com.syncodec.graphite.di.cloud.dropbox.DropboxApi
import com.syncodec.graphite.di.snapshot.SnapshotInator
import com.syncodec.graphite.presentation.common.LoadingView
import com.syncodec.graphite.presentation.common.dialog.dialog2.GenericAlertDialog2
import com.syncodec.graphite.presentation.common.dialog.dialog2.GenericDialogDefaults


@Preview
@Composable
fun RestoreSnapshotDialog(
	isDialogVisible: Boolean = true,
	onDismissRequest: () -> Unit = {},
	documentFile: DocumentFile? = null,
	snapshotMetadata: SnapshotInator.Companion.SnapshotMetadata? = null,
	onClickRestore: (DocumentFile) -> Unit = {},
) {
	GenericAlertDialog2(
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
					text = it.timeStampPretty ?: stringResource(R.string.created_time_unknown),
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
fun RestoreSnapshotDialog(
	isDialogVisible: Boolean = true,
	onDismissRequest: () -> Unit = {},
	remoteSnapshot: DropboxApi.Companion.RemoteSnapshot? = null,
	snapshotMetadata: SnapshotInator.Companion.SnapshotMetadata? = null,
	snapshotByteArray: ByteArray? = null,
	onClickRestore: (ByteArray) -> Unit = {},
) {
	val context = LocalContext.current
	val uriHandler = LocalUriHandler.current

	GenericAlertDialog2(
		isDialogVisible = isDialogVisible,
		icon = GenericDialogDefaults.genericDialogWarningIcon(),
		title = stringResource(id = R.string.restore_snapshot),
		contentText = stringResource(id = R.string.restore_snapshot_content),
		primaryButton = GenericDialogDefaults.genericDialogButtonPrimary(text = stringResource(id = R.string.restore), onClick = { snapshotByteArray?.let(onClickRestore) }),
		secondaryButton = GenericDialogDefaults.genericDialogButtonDismiss(onClick = onDismissRequest),
		tertiaryButton = GenericDialogDefaults.genericDialogButtonSecondary(
			text = stringResource(id = R.string.open_in_dropbox),
			onClick = {
				try {
					remoteSnapshot?.pathLower?.let { uriHandler.openUri(DropboxApi.getDropboxFilePath(it)) }
				} catch (e: Exception) {
					if (BuildConfig.DEBUG) e.printStackTrace()
					Toast.makeText(context, context.getText(R.string.toast_error_opening_externally), Toast.LENGTH_SHORT).show()
				}
			}
		),
		isPrimaryButtonEnabled = snapshotByteArray != null,
		isTertiaryButtonEnabled = remoteSnapshot != null,
		onDismissRequest = onDismissRequest,
	) {
		snapshotMetadata?.let {
			Spacer(modifier = Modifier.height(12.dp))
			Text(
				text = it.timeStampPretty ?: stringResource(R.string.created_time_unknown),
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
		} ?: LoadingView(modifier = Modifier.fillMaxWidth())
	}
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
