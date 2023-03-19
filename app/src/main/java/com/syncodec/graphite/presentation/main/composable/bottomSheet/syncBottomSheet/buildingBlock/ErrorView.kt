package com.syncodec.graphite.presentation.main.composable.bottomSheet.syncBottomSheet.buildingBlock

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.info.InfoCard
import com.syncodec.graphite.presentation.common.info.InfoCardDefaults
import com.syncodec.graphite.presentation.sync.dropbox.DropboxSyncActivity
import com.syncodec.graphite.utils.NetworkUtils.Companion.isInternetAvailable


@Preview
@Composable
fun ErrorView() {
	val context = LocalContext.current

	val isInternetAvailable = context.isInternetAvailable()

	InfoCard(
		title = if (isInternetAvailable) "Error connecting with Dropbox" else "No internet connection",
		description = if (isInternetAvailable) "Ensure you have a working internet connection and try again." else "It seems you are not connected to internet. Please connect to internet and try again.",
		icon = R.drawable.ic_warning,
		colors = InfoCardDefaults.warningCardColors(),
		buttonText = "Manage",
		onClickButton = { context.startActivity(Intent(context, DropboxSyncActivity::class.java)) },
	)
}
