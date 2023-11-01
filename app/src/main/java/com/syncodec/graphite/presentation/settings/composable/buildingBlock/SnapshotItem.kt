package com.syncodec.graphite.presentation.settings.composable.buildingBlock

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.documentfile.provider.DocumentFile
import com.syncodec.graphite.R
import com.syncodec.graphite.di.cloud.dropbox.DropboxApi
import com.syncodec.graphite.di.snapshot.SnapshotInator
import java.io.File


@Preview
@Composable
fun SnapshotItem(
	snapshotMetadata: SnapshotInator.Companion.SnapshotMetadata? = null,
	documentFile: DocumentFile = DocumentFile.fromFile(File("")),
	onClick: () -> Unit = {}
) {
	SettingsButton(
		title = documentFile.name ?: snapshotMetadata?.timeStampPretty ?: "Snapshot",
		subTitle = "${stringResource(id = R.string.created_on)} ${snapshotMetadata?.timeStampPretty ?: "?"}",
		leadingIcon = SettingsButtonDefaults.settingsButtonLeadingIcon(icon = R.drawable.ic_fa_restore),
		onClick = onClick,
	)
}

@Preview
@Composable
fun RemoteSnapshot(
	remoteSnapshot: DropboxApi.Companion.RemoteSnapshot = DropboxApi.Companion.RemoteSnapshot("", "", "", ""),
	onClick: () -> Unit = {},
) {
	SettingsButton(
		title = remoteSnapshot.name,
		subTitle = "${stringResource(id = R.string.created_on)} ${remoteSnapshot.timestampPretty}",
		leadingIcon = SettingsButtonDefaults.settingsButtonLeadingIcon(icon = R.drawable.ic_fa_restore),
		onClick = onClick,
	)
}
