package com.syncodec.graphite.presentation.main.composable.bar

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.button.GenericButton
import com.syncodec.graphite.presentation.common.button.GenericButtonDefaults
import com.syncodec.graphite.presentation.common.button.MenuButton
import com.syncodec.graphite.presentation.common.button.SearchButton
import com.syncodec.graphite.presentation.common.button.VaultButton
import com.syncodec.graphite.presentation.base.SyncState
import com.syncodec.graphite.service.syncInator.SyncInatorService


@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun TopBar(
	onClickMenu: () -> Unit = {},
	onClickCloud: () -> Unit = {},
	onClickSearch: () -> Unit = {},
) {
	CenterAlignedTopAppBar(
		navigationIcon = {
			Row(
				modifier = Modifier
			) {
				MenuButton(onClick = onClickMenu)
				VaultButton()
			}
		},
		title = {
			Text(
				text = "GRAPHITE",
				modifier = Modifier,
				fontFamily = FontFamily(Font(R.font.graduate_regular, FontWeight.Normal)),
				fontWeight = FontWeight.Bold,
				fontSize = 24.sp,
				lineHeight = 28.sp,
				letterSpacing = 2.sp,
				color = MaterialTheme.colorScheme.primary
			)
		},
		actions = {
//			CloudButton(
//				syncStatus = syncStatus,
//				onClickSync = onClickCloud
//			)
			SearchButton(onClick = onClickSearch)
		},
		colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.background)
	)
}

//@Composable
//private fun CloudButton(
//	syncStatus: SyncInatorService.Companion.SyncStatus = SyncInatorService.Companion.SyncStatus.Init,
//	onClickSync: () -> Unit
//) {
//	GenericButton(
//		icon = SyncState.syncStatusIcon[syncStatus::class] ?: R.drawable.ic_cloud,
//		colors = GenericButtonDefaults.genericButtonColors(iconColor = SyncState.getSyncStatusIconColor(syncStatus = syncStatus),),
//		onClick = onClickSync
//	)
//}
