package com.syncodec.graphite.presentation.settings.composable.screen

import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.settings.SettingsActivity
import com.syncodec.graphite.presentation.settings.composable.buildingBlock.GenericSettingsScreen
import com.syncodec.graphite.presentation.settings.composable.buildingBlock.SettingsButton
import com.syncodec.graphite.presentation.settings.composable.dialog.SettingsDialogType
import com.syncodec.graphite.utils.timeStampToPrettyFull


@Preview
@Composable
fun SnapshotWarehouseScreen() {

	val snapshotList = SettingsActivity.LocalSnapshotList.current

	val scrollState = rememberScrollState()

	val takeSnapshot = SettingsActivity.LocalTakeSnapshot.current
	val getSnapshot = SettingsActivity.LocalGetSnapshot.current

	val openDialog = SettingsActivity.LocalOpenDialog.current

	val refreshState = rememberSwipeRefreshState(isRefreshing = false)

	GenericSettingsScreen(
		title = "Snapshot Warehouse",
		scrollState = scrollState,
		refreshState = refreshState,
		onRefresh = getSnapshot
	) {
		SettingsButton(
			title = "Take Snapshot",
			icon = R.drawable.ic_snapshot,
			subTitle = "Take a snapshot of the current state of the database",
			onClick = takeSnapshot
		)
		snapshotList.forEach {
			val timestamp = it
				.name
				?.substringAfter("snapshot_")
				?.substringBefore(".7z")
				?.let {
					try {
						it.toLong().timeStampToPrettyFull()
					} catch (e : Exception) {
						null
					}
				}
			SettingsButton(
				title = it.name ?: "Unknown",
				icon = R.drawable.ic_snapshot_stored,
				subTitle = if (timestamp == null) "Snapshot taken on unknown date" else "Snapshot taken on $timestamp",
				onClick = { openDialog(SettingsDialogType.RESTORE_SNAPSHOT, it) }
			)
		}
	}
}
