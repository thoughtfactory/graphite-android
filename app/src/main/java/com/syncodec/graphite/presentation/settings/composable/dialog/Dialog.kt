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

	val attachmentCount = SettingsActivity.attachmentCount.current
	val attachmentProcessed = SettingsActivity.attachmentProcessed.current
	val bucketItemCount = SettingsActivity.bucketItemCount.current
	val bucketItemProcessed = SettingsActivity.bucketItemProcessed.current
	val bucketCount = SettingsActivity.bucketCount.current
	val bucketProcessed = SettingsActivity.bucketProcessed.current
	val chapterCount = SettingsActivity.chapterCount.current
	val chapterProcessed = SettingsActivity.chapterProcessed.current
	val noteCount = SettingsActivity.noteCount.current
	val noteProcessed = SettingsActivity.noteProcessed.current
	val tagCount = SettingsActivity.tagCount.current
	val tagProcessed = SettingsActivity.tagProcessed.current
	val packageCount = SettingsActivity.packageCount.current
	val packageProcessed = SettingsActivity.packageProcessed.current

	val showTakeSnapshotDialog = SettingsActivity.showTakeSnapshotDialog.current
	val showRestoreSnapshotDialog = SettingsActivity.showRestoreSnapshotDialog.current
	val showRestoringSnapshotDialog = SettingsActivity.showRestoringSnapshotDialog.current
	val showDeleteAccountDialog = SettingsActivity.showDeleteAccountDialog.current

	val restoreSnapshot = SettingsActivity.restoreSnapshot.current
	val deleteAccount = SettingsActivity.deleteAccount.current

	val closeDialog = SettingsActivity.closeDialog.current

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

	DeleteAccountDialog(
		showDialog = showDeleteAccountDialog,
		onDelete = deleteAccount
	) { closeDialog(SettingsDialogType.DELETE_ACCOUNT) }
}
