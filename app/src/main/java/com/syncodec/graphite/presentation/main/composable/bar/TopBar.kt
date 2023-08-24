package com.syncodec.graphite.presentation.main.composable.bar

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.animation.AnimatedText
import com.syncodec.graphite.presentation.common.button.GenericButton
import com.syncodec.graphite.presentation.common.button.GenericButtonDefaults
import com.syncodec.graphite.presentation.common.button.MenuButton
import com.syncodec.graphite.presentation.common.button.VaultButton
import com.syncodec.graphite.presentation.ui.SyncState
import com.syncodec.graphite.service.syncInator.SyncInatorService
import com.syncodec.graphite.utils.LocalAuthenticatorAction
import com.syncodec.graphite.utils.LocalIsAuthenticated


@Preview
@Composable
fun TopBar(
	currentRoute: String? = null,
	isSelecting: Boolean = false,
	selectedSize: Int = 0,
	syncStatus: SyncInatorService.Companion.SyncStatus = SyncInatorService.Companion.SyncStatus.Init,
	onClickMenu: () -> Unit = {},
	onClickCancelSelect: () -> Unit = {},
	onClickCloud: () -> Unit = {},
	onClickSearch: () -> Unit = {},
	onClickDelete: () -> Unit = {},
) {
	Column(
		modifier = Modifier
			.fillMaxWidth()
			.background(MaterialTheme.colorScheme.background)
	) {
		Bar(
			currentRoute = currentRoute,
			isSelecting = isSelecting,
			selectedSize = selectedSize,
			syncStatus = syncStatus,
			onClickMenu = onClickMenu,
			onClickCancelSelect = onClickCancelSelect,
			onClickCloud = onClickCloud,
			onClickSearch = onClickSearch,
			onClickDelete = onClickDelete,
		)
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Bar(
	currentRoute: String? = null,
	isSelecting: Boolean = false,
	selectedSize: Int = 0,
	syncStatus: SyncInatorService.Companion.SyncStatus = SyncInatorService.Companion.SyncStatus.Init,
	onClickMenu: () -> Unit = {},
	onClickCancelSelect: () -> Unit = {},
	onClickCloud: () -> Unit = {},
	onClickSearch: () -> Unit = {},
	onClickDelete: () -> Unit = {},
) {

	val isAuthenticated = LocalIsAuthenticated.current
	val onAuthenticationAction = LocalAuthenticatorAction.current

	Crossfade(
		targetState = isSelecting,
		animationSpec = tween(300),
		label = "isSelecting"
	) {
		if (it) {
			TopAppBar(
				navigationIcon = {
					GenericButton(
						icon = R.drawable.ic_close,
						onClick = onClickCancelSelect,
					)
				},
				title = {
					AnimatedText(
						text = if (selectedSize == 0) "No items selected" else if (selectedSize == 1) "1 item selected" else "${selectedSize} items selected",
						color = MaterialTheme.colorScheme.onBackground,
					)
				},
				actions = {
					GenericButton(
						icon = R.drawable.ic_delete,
						colors = GenericButtonDefaults.deleteButtonColors(),
						onClick = onClickDelete
					)
				},
				colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
			)
		} else {
			CenterAlignedTopAppBar(
				navigationIcon = {
					Row(
						modifier = Modifier
					) {
						MenuButton(onClick = onClickMenu,)
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
					CloudButton(
						syncStatus = syncStatus,
						onClickSync = onClickCloud
					)
					GenericButton(
						icon = R.drawable.ic_fa_search,
						onClick = onClickSearch
					)
				},
				colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.background)
			)
		}
	}
}

@Composable
private fun CloudButton(
	syncStatus: SyncInatorService.Companion.SyncStatus = SyncInatorService.Companion.SyncStatus.Init,
	onClickSync: () -> Unit
) {
	GenericButton(
		icon = SyncState.syncStatusIcon[syncStatus::class] ?: R.drawable.ic_cloud,
		colors = GenericButtonDefaults.genericButtonColors(iconColor = SyncState.getSyncStatusIconColor(syncStatus = syncStatus),),
		onClick = onClickSync
	)
}
