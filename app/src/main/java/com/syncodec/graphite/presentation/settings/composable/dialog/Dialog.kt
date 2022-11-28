package com.syncodec.graphite.presentation.settings.composable.dialog

import androidx.compose.runtime.Composable
import com.syncodec.graphite.presentation.settings.SettingsActivity


enum class SettingsDialogType {
	TAKE_SNAPSHOT,
	RESTORE_SNAPSHOT,
	RESTORING_SNAPSHOT,
	DELETE_ACCOUNT
}

@Composable
fun SettingsDialog() {

	val attachmentCount = SettingsActivity.LocalAttachmentCount.current
	val attachmentProcessed = SettingsActivity.LocalAttachmentProcessed.current
	val bucketItemCount = SettingsActivity.LocalBucketItemCount.current
	val bucketItemProcessed = SettingsActivity.LocalBucketItemProcessed.current
	val bucketCount = SettingsActivity.LocalBucketCount.current
	val bucketProcessed = SettingsActivity.LocalBucketProcessed.current
	val chapterCount = SettingsActivity.LocalChapterCount.current
	val chapterProcessed = SettingsActivity.LocalChapterProcessed.current
	val noteCount = SettingsActivity.LocalNoteCount.current
	val noteProcessed = SettingsActivity.LocalNoteProcessed.current
	val tagCount = SettingsActivity.LocalTagCount.current
	val tagProcessed = SettingsActivity.LocalTagProcessed.current
	val packageCount = SettingsActivity.LocalPackageCount.current
	val packageProcessed = SettingsActivity.LocalPackageProcessed.current
	val importDataCount = SettingsActivity.LocalImportDataCount.current
	val importDataProcessed = SettingsActivity.LocalImportDataProcessed.current

	val showTakeSnapshotDialog = SettingsActivity.LocalShowTakeSnapshotDialog.current
	val showRestoreSnapshotDialog = SettingsActivity.LocalShowRestoreSnapshotDialog.current
	val showRestoringSnapshotDialog = SettingsActivity.LocalShowRestoringSnapshotDialog.current
	val showImportingDataDialog = SettingsActivity.LocalShowImportingDataDialog.current
	val showImportingJourneyDataDialog = SettingsActivity.LocalShowImportingJourneyDataDialog.current
	val showDeleteAccountDialog = SettingsActivity.LocalShowDeleteAccountDialog.current

	val restoreSnapshot = SettingsActivity.LocalRestoreSnapshot.current
	val deleteAccount = SettingsActivity.LocalDeleteAccount.current

	val closeDialog = SettingsActivity.LocalCloseDialog.current

	TakeSnapshotDialog(
		showDialog = showTakeSnapshotDialog,
		attachmentCount = attachmentCount,
		attachmentProcessed = attachmentProcessed,
		bucketItemCount = bucketItemCount,
		bucketItemProcessed = bucketItemProcessed,
		bucketCount = bucketCount,
		bucketProcessed = bucketProcessed,
		chapterCount = chapterCount,
		chapterProcessed = chapterProcessed,
		noteCount = noteCount,
		noteProcessed = noteProcessed,
		tagCount = tagCount,
		tagProcessed = tagProcessed,
		packageCount = packageCount,
		packageProcessed = packageProcessed,
	)

	RestoreSnapshotDialog(
		showDialog = showRestoreSnapshotDialog,
		onRestore = {
			closeDialog(SettingsDialogType.RESTORE_SNAPSHOT)
			restoreSnapshot()
		}
	) { closeDialog(SettingsDialogType.RESTORE_SNAPSHOT) }

	RestoringSnapshotDialog(
		showDialog = showRestoringSnapshotDialog,
		attachmentCount = attachmentCount,
		attachmentProcessed = attachmentProcessed,
		bucketItemCount = bucketItemCount,
		bucketItemProcessed = bucketItemProcessed,
		bucketCount = bucketCount,
		bucketProcessed = bucketProcessed,
		chapterCount = chapterCount,
		chapterProcessed = chapterProcessed,
		noteCount = noteCount,
		noteProcessed = noteProcessed,
		tagCount = tagCount,
		tagProcessed = tagProcessed,
	)

	ImportingSnapshotDialog(
		showDialog = showImportingDataDialog,
		attachmentCount = attachmentCount,
		attachmentProcessed = attachmentProcessed,
		bucketItemCount = bucketItemCount,
		bucketItemProcessed = bucketItemProcessed,
		bucketCount = bucketCount,
		bucketProcessed = bucketProcessed,
		chapterCount = chapterCount,
		chapterProcessed = chapterProcessed,
		noteCount = noteCount,
		noteProcessed = noteProcessed,
		tagCount = tagCount,
		tagProcessed = tagProcessed,
	)

	ImportingJourneySnapshotDialog(
		showDialog = showImportingJourneyDataDialog,
		importDataCount = importDataCount,
		importDataProcessed = importDataProcessed
	)

	DeleteAccountDialog(
		showDialog = showDeleteAccountDialog,
		onDelete = deleteAccount
	) { closeDialog(SettingsDialogType.DELETE_ACCOUNT) }
}
