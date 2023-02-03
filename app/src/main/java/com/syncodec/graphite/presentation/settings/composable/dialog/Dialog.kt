package com.syncodec.graphite.presentation.settings.composable.dialog

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.syncodec.graphite.presentation.settings.SettingsActivity
import com.syncodec.graphite.presentation.settings.composable.dialog.clearData.ClearDataDialog
import com.syncodec.graphite.presentation.settings.composable.dialog.exportData.ExportDataDialog
import com.syncodec.graphite.presentation.settings.composable.dialog.importData.ImportDataDialog


enum class SettingsDialogType {
	IMPORT_DATA,
	EXPORT_DATA,
	CLEAR_DATA,
	NOTIFICATION_PERMISSION,
	DELETE_ACCOUNT
}

@Preview
@Composable
fun SettingsDialog() {

	val showImportDataDialog = SettingsActivity.LocalShowImportDataDialog.current
	val showExportDataDialog = SettingsActivity.LocalShowExportDataDialog.current
	val showClearDataDialog = SettingsActivity.LocalShowClearDataDialog.current

	val closeDialog = SettingsActivity.LocalCloseDialog.current

	ExportDataDialog(showDialog = showExportDataDialog) { closeDialog(SettingsDialogType.EXPORT_DATA) }
	ImportDataDialog(showDialog = showImportDataDialog) { closeDialog(SettingsDialogType.IMPORT_DATA) }
	ClearDataDialog(showDialog = showClearDataDialog) { closeDialog(SettingsDialogType.CLEAR_DATA) }
}


//@Composable
//fun SettingsDialog() {
//	val context = LocalContext.current
//	val dataStoreInstance = remember { DataStoreInstance(context = context) }
//
//	val attachmentCount = SettingsActivity.LocalAttachmentCount.current
//	val attachmentProcessed = SettingsActivity.LocalAttachmentProcessed.current
//	val bucketItemCount = SettingsActivity.LocalBucketItemCount.current
//	val bucketItemProcessed = SettingsActivity.LocalBucketItemProcessed.current
//	val bucketCount = SettingsActivity.LocalBucketCount.current
//	val bucketProcessed = SettingsActivity.LocalBucketProcessed.current
//	val chapterCount = SettingsActivity.LocalChapterCount.current
//	val chapterProcessed = SettingsActivity.LocalChapterProcessed.current
//	val noteCount = SettingsActivity.LocalNoteCount.current
//	val noteProcessed = SettingsActivity.LocalNoteProcessed.current
//	val tagCount = SettingsActivity.LocalTagCount.current
//	val tagProcessed = SettingsActivity.LocalTagProcessed.current
//	val packageCount = SettingsActivity.LocalPackageCount.current
//	val packageProcessed = SettingsActivity.LocalPackageProcessed.current
//	val importDataCount = SettingsActivity.LocalImportDataCount.current
//	val importDataProcessed = SettingsActivity.LocalImportDataProcessed.current
//
//	val showTakeSnapshotDialog = SettingsActivity.LocalShowTakeSnapshotDialog.current
//	val showRestoreSnapshotDialog = SettingsActivity.LocalShowRestoreSnapshotDialog.current
//	val showRestoringSnapshotDialog = SettingsActivity.LocalShowRestoringSnapshotDialog.current
//	val showImportingDataDialog = SettingsActivity.LocalShowImportingDataDialog.current
//	val showImportingJourneyDataDialog = SettingsActivity.LocalShowImportingJourneyDataDialog.current
//	val showNotificationPermissionDialog = SettingsActivity.LocalShowNotificationPermissionDialog.current
//	val showDeleteAccountDialog = SettingsActivity.LocalShowDeleteAccountDialog.current
//
//	val isNoteNotificationEnabled by dataStoreInstance.getNoteFromNotification.collectAsState(initial = null)
//
//	val restoreSnapshot = SettingsActivity.LocalRestoreSnapshot.current
//	val deleteAccount = SettingsActivity.LocalDeleteAccount.current
//
//	val closeDialog = SettingsActivity.LocalCloseDialog.current
//
//	TakeSnapshotDialog(
//		showDialog = showTakeSnapshotDialog,
//		attachmentCount = attachmentCount,
//		attachmentProcessed = attachmentProcessed,
//		bucketItemCount = bucketItemCount,
//		bucketItemProcessed = bucketItemProcessed,
//		bucketCount = bucketCount,
//		bucketProcessed = bucketProcessed,
//		chapterCount = chapterCount,
//		chapterProcessed = chapterProcessed,
//		noteCount = noteCount,
//		noteProcessed = noteProcessed,
//		tagCount = tagCount,
//		tagProcessed = tagProcessed,
//		packageCount = packageCount,
//		packageProcessed = packageProcessed,
//	)
//
//	RestoreSnapshotDialog(
//		showDialog = showRestoreSnapshotDialog,
//		onRestore = {
//			closeDialog(SettingsDialogType.RESTORE_SNAPSHOT)
//			restoreSnapshot()
//		}
//	) { closeDialog(SettingsDialogType.RESTORE_SNAPSHOT) }
//
//	RestoringSnapshotDialog(
//		showDialog = showRestoringSnapshotDialog,
//		attachmentCount = attachmentCount,
//		attachmentProcessed = attachmentProcessed,
//		bucketItemCount = bucketItemCount,
//		bucketItemProcessed = bucketItemProcessed,
//		bucketCount = bucketCount,
//		bucketProcessed = bucketProcessed,
//		chapterCount = chapterCount,
//		chapterProcessed = chapterProcessed,
//		noteCount = noteCount,
//		noteProcessed = noteProcessed,
//		tagCount = tagCount,
//		tagProcessed = tagProcessed,
//	)
//
//	ImportingSnapshotDialog(
//		showDialog = showImportingDataDialog,
//		attachmentCount = attachmentCount,
//		attachmentProcessed = attachmentProcessed,
//		bucketItemCount = bucketItemCount,
//		bucketItemProcessed = bucketItemProcessed,
//		bucketCount = bucketCount,
//		bucketProcessed = bucketProcessed,
//		chapterCount = chapterCount,
//		chapterProcessed = chapterProcessed,
//		noteCount = noteCount,
//		noteProcessed = noteProcessed,
//		tagCount = tagCount,
//		tagProcessed = tagProcessed,
//	)
//
//	ImportingJourneySnapshotDialog(
//		showDialog = showImportingJourneyDataDialog,
//		importDataCount = importDataCount,
//		importDataProcessed = importDataProcessed
//	)
//
//	NotificationPermissionDialog(
//		showDialog = showNotificationPermissionDialog,
//		onDismiss = { closeDialog(SettingsDialogType.NOTIFICATION_PERMISSION) },
//	) {
//		closeDialog(SettingsDialogType.NOTIFICATION_PERMISSION)
//		isNoteNotificationEnabled?.not()?.let {
//			dataStoreInstance.putNoteFromNotification(it)
//			if (it) WriteNoteNotification.showSimpleNotification(context = context) else WriteNoteNotification.cancelNotification(context = context)
//		} ?: dataStoreInstance.putNoteFromNotification(false)
//	}
//
//
//	DeleteAccountDialog(
//		showDialog = showDeleteAccountDialog,
//		onDelete = deleteAccount
//	) { closeDialog(SettingsDialogType.DELETE_ACCOUNT) }
//
//}
