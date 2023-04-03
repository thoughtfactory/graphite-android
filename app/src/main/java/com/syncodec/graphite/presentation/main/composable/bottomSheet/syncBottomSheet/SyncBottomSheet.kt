package com.syncodec.graphite.presentation.main.composable.bottomSheet.syncBottomSheet

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.di.sync.dropbox.DBox
import com.syncodec.graphite.presentation.common.LoadingView
import com.syncodec.graphite.presentation.common.bottomSheet.GenericBottomSheet
import com.syncodec.graphite.presentation.main.composable.bottomSheet.syncBottomSheet.buildingBlock.ConnectedView
import com.syncodec.graphite.presentation.main.composable.bottomSheet.syncBottomSheet.buildingBlock.ErrorView
import com.syncodec.graphite.presentation.main.composable.bottomSheet.syncBottomSheet.buildingBlock.NotLoggedInView
import com.syncodec.graphite.service.DropboxService
import com.syncodec.graphite.service.SyncerService


@Preview
@Composable
fun SyncBottomSheet(
	syncStatus : SyncerService.Companion.SyncStatus = SyncerService.Companion.SyncStatus.Init,
	testConnectionResponse : DBox.Companion.TestConnectionResponse? = null,
	onClickSyncNow : () -> Unit = {},
	onClickForceSync : () -> Unit = {},
) {
	GenericBottomSheet(
		title = "Sync",
		icon = R.drawable.ic_cloud
	) {
		when (testConnectionResponse) {
			is DBox.Companion.TestConnectionResponse.Loading -> LoadingView(
				modifier = Modifier
					.requiredSize(48.dp)
					.padding(8.dp)
			)

			is DBox.Companion.TestConnectionResponse.Success -> ConnectedView(
				email = testConnectionResponse.fullAccount.email,
				name = testConnectionResponse.fullAccount.name?.displayName,
				spaceTotal = testConnectionResponse.spaceUsage.allocation?.individualValue?.allocated,
				spaceUsed = testConnectionResponse.spaceUsage.used,
				onForceSync = onClickForceSync,
				onSync = onClickSyncNow,
			)

			is DBox.Companion.TestConnectionResponse.NotLoggedIn -> NotLoggedInView()
			is DBox.Companion.TestConnectionResponse.Error -> ErrorView()
			else -> LoadingView(
				modifier = Modifier
					.requiredSize(48.dp)
					.padding(8.dp)
			)
		}
	}
}
