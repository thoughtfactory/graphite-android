package com.syncodec.graphite.presentation.main.composable.bar

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.animation.AnimatedText
import com.syncodec.graphite.presentation.common.button.MenuButton
import com.syncodec.graphite.presentation.common.button.MenuButtonDefaults
import com.syncodec.graphite.presentation.common.button.stateButton.StateButton
import com.syncodec.graphite.presentation.common.button.stateButton.StateData
import com.syncodec.graphite.presentation.main.composable.screen.ComponentType
import com.syncodec.graphite.presentation.ui.SyncState
import com.syncodec.graphite.service.syncInator.SyncInatorService
import com.syncodec.graphite.utils.AuthenticatorScreen
import com.syncodec.graphite.utils.LocalAuthenticatorAction
import com.syncodec.graphite.utils.LocalIsAuthenticated


@Preview
@Composable
fun TopBar(
	currentRoute : String? = null,
	componentType : ComponentType = ComponentType.Note,
	isSelecting : Boolean = false,
	selectedSize : Int = 0,
	syncStatus : SyncInatorService.Companion.SyncStatus = SyncInatorService.Companion.SyncStatus.Init,
	onComponentChange : (Int) -> Unit = {},
	onClickFilter : () -> Unit = {},
	onClickMenu : () -> Unit = {},
	onClickCancelSelect : () -> Unit = {},
	onClickCloud : () -> Unit = {},
	onClickSearch : () -> Unit = {},
	onClickDelete : () -> Unit = {},
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

		AnimatedVisibility(
			visible = currentRoute == BottomNavigationItem.Home.route && ! isSelecting,
			enter = expandVertically(tween(300)),
			exit = shrinkVertically(tween(300))
		) {
			ComponentTypeView(
				componentType = componentType,
				onStateChange = onComponentChange,
				onClickFilter = onClickFilter
			)
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
private fun Bar(
	currentRoute : String? = null,
	isSelecting : Boolean = false,
	selectedSize : Int = 0,
	syncStatus : SyncInatorService.Companion.SyncStatus = SyncInatorService.Companion.SyncStatus.Init,
	onClickMenu : () -> Unit = {},
	onClickCancelSelect : () -> Unit = {},
	onClickCloud : () -> Unit = {},
	onClickSearch : () -> Unit = {},
	onClickDelete : () -> Unit = {},
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
					MenuButton(
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
					MenuButton(
						icon = R.drawable.ic_delete,
						colors = MenuButtonDefaults.deleteButtonColors(),
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
						MenuButton(
							icon = R.drawable.ic_menu,
							onClick = onClickMenu,
						)
						MenuButton(
							icon = R.drawable.ic_vault,
							tooltip = "Vault",
							checked = isAuthenticated,
						) { onAuthenticationAction(AuthenticatorScreen.Authenticate) }
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
					MenuButton(
						icon = R.drawable.ic_search,
						onClick = onClickSearch
					)
				},
				colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.background)
			)
		}
	}
}

@Composable
private fun ComponentTypeView(
	componentType : ComponentType = ComponentType.Note,
	onStateChange : (Int) -> Unit = {},
	onClickFilter : () -> Unit = {},
) {
	Column(
		modifier = Modifier.fillMaxWidth(),
		verticalArrangement = Arrangement.Center,
	) {
		Row(
			modifier = Modifier.fillMaxWidth(),
			horizontalArrangement = Arrangement.Center,
			verticalAlignment = Alignment.CenterVertically
		) {
			Spacer(modifier = Modifier.width(12.dp))
			StateButton(
				stateList = listOf(
					StateData(
						title = "Note",
						icon = R.drawable.ic_note,
						stateTint = MaterialTheme.colorScheme.primary
					),
					StateData(
						title = "List",
						icon = R.drawable.ic_bucket,
						stateTint = MaterialTheme.colorScheme.primary
					),
					StateData(
						title = "Notebook",
						icon = R.drawable.ic_notebook,
						stateTint = MaterialTheme.colorScheme.primary
					),
				),
				currentState = componentType.ordinal,
				onChangeState = onStateChange,
				modifier = Modifier
					.height(36.dp)
					.weight(1f)
			)
			MenuButton(
				icon = R.drawable.ic_filter,
				onClick = onClickFilter,
			)

			Spacer(modifier = Modifier.width(4.dp))
		}
		Spacer(modifier = Modifier.height(6.dp))
	}
}

@Composable
private fun CloudButton(
	syncStatus : SyncInatorService.Companion.SyncStatus = SyncInatorService.Companion.SyncStatus.Init,
	onClickSync : () -> Unit
) {
	MenuButton(
		icon = SyncState.syncStatusIcon[syncStatus::class] ?: R.drawable.ic_cloud,
		colors = MenuButtonDefaults.menuButtonColors(
			iconColor = SyncState.getSyncStatusIconColor(syncStatus = syncStatus),
		),
		onClick = onClickSync
	)
}
