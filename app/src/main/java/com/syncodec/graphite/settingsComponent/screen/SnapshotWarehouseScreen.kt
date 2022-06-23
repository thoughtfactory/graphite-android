package com.syncodec.graphite.settingsComponent.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.syncodec.graphite.R
import com.syncodec.graphite.database.snapshot.Snapshot
import com.syncodec.graphite.settingsComponent.SettingsActivity
import com.syncodec.graphite.settingsComponent.miscellaneous.SettingButton


@Composable
fun SnapshotWarehouseScreen(
	snapshotList: List<Snapshot>,
	isRefreshing: Boolean,
	onAction: (SettingsActivity.Action, Any?) -> Unit
) {
	SwipeRefresh(
		state = rememberSwipeRefreshState(isRefreshing),
		onRefresh = { onAction(SettingsActivity.Action.REFRESH_SNAPSHOT, null) },
	) {
		LazyColumn(
			modifier = Modifier.fillMaxSize(),
			horizontalAlignment = Alignment.CenterHorizontally
		) {
			item {
				SettingButton(
					title = "Take a Snapshot",
					subTitle = "Take a new snapshot of current state of data",
					leadingIcon = R.drawable.ic_timer,
				) { onAction(SettingsActivity.Action.TAKE_SNAPSHOT, null) }
			}

			item {
				Spacer(
					modifier = Modifier
						.fillMaxWidth(0.71f)
						.height(1.dp)
						.background(MaterialTheme.colorScheme.onBackground.copy(0.47f))
				)
			}

			snapshotList.forEach { snapshot ->
				item {
					SettingButton(
						title = snapshot.title,
						leadingIcon = R.drawable.ic_history,
					) {
						onAction(SettingsActivity.Action.OPEN_SNAPSHOT, snapshot)
					}
				}
			}
		}
	}
}
