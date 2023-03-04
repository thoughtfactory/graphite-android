package com.syncodec.graphite.presentation.main.composable.bar

import androidx.compose.animation.*
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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
import com.syncodec.graphite.service.DropboxService
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
	syncStatus : DropboxService.Companion.DropboxSyncStatus = DropboxService.Companion.DropboxSyncStatus.Init,
	onComponentChange : (Int) -> Unit = {},
	onClickFilter : () -> Unit = {},
	onClickMenu : () -> Unit = {},
	onClickCancelSelect : () -> Unit = {},
	onClickCloud : () -> Unit = {},
	onClickSearch : () -> Unit = {},
	onClickDelete : () -> Unit = {},
) {
	val containerColor by animateColorAsState(
		targetValue = when (currentRoute) {
			BottomNavigationItem.Home.route -> MaterialTheme.colorScheme.background
			BottomNavigationItem.Calendar.route -> MaterialTheme.colorScheme.background
			BottomNavigationItem.Atlas.route -> MaterialTheme.colorScheme.background
			else -> MaterialTheme.colorScheme.background
		}
	)

	Column(
		modifier = Modifier
			.fillMaxWidth()
			.background(containerColor)
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
	syncStatus : DropboxService.Companion.DropboxSyncStatus,
	onClickMenu : () -> Unit = {},
	onClickCancelSelect : () -> Unit = {},
	onClickCloud : () -> Unit = {},
	onClickSearch : () -> Unit = {},
	onClickDelete : () -> Unit = {},
) {

	val isAuthenticated = LocalIsAuthenticated.current
	val onAuthenticationAction = LocalAuthenticatorAction.current

	val containerColor by animateColorAsState(
		targetValue = when (currentRoute) {
			"graphite" -> MaterialTheme.colorScheme.background
			"bucket" -> MaterialTheme.colorScheme.background
			"calendar" -> MaterialTheme.colorScheme.background
			"atlas" -> MaterialTheme.colorScheme.background
			else -> MaterialTheme.colorScheme.background
		}
	)

	Crossfade(
		targetState = isSelecting,
		animationSpec = tween(300)
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
				colors = TopAppBarDefaults.topAppBarColors(containerColor = containerColor)
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
//					CloudButton(
//						syncStatus = syncStatus,
//						onClickSync = onClickCloud
//					)
					MenuButton(
						icon = R.drawable.ic_vault,
						tooltip = "Vault",
						checked = isAuthenticated,
					) { onAuthenticationAction(AuthenticatorScreen.Authenticate) }
					MenuButton(
						icon = R.drawable.ic_search,
						onClick = onClickSearch
					)
				},
				colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = containerColor)
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

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun CloudButton(
	syncStatus : DropboxService.Companion.DropboxSyncStatus,
	onClickSync : () -> Unit
) {
	val infiniteTransition = rememberInfiniteTransition()
	val alpha by infiniteTransition.animateFloat(
		initialValue = 1f,
		targetValue = 0.47f,
		animationSpec = infiniteRepeatable(
			animation = tween(710, easing = LinearEasing),
			repeatMode = RepeatMode.Reverse
		)
	)

	AnimatedContent(
		targetState = syncStatus,
		transitionSpec = { fadeIn(tween(300)) with fadeOut(tween(300)) }
	) {
		when (it) {
			is DropboxService.Companion.DropboxSyncStatus.Init -> MenuButton(
				icon = R.drawable.ic_cloud,
				onClick = onClickSync
			)

			is DropboxService.Companion.DropboxSyncStatus.SyncNotConfigured -> MenuButton(
				icon = R.drawable.ic_cloud_dashed,
				onClick = onClickSync
			)

			is DropboxService.Companion.DropboxSyncStatus.SyncDisabled -> MenuButton(
				icon = R.drawable.ic_cloud_disable,
				onClick = onClickSync
			)

			is DropboxService.Companion.DropboxSyncStatus.NoInternet -> MenuButton(
				icon = R.drawable.ic_no_network,
				onClick = onClickSync
			)

			is DropboxService.Companion.DropboxSyncStatus.NotLoggedIn -> MenuButton(
				icon = R.drawable.ic_cloud_disable,
				onClick = onClickSync
			)

			is DropboxService.Companion.DropboxSyncStatus.Loading -> MenuButton(
				icon = R.drawable.ic_cloud,
				onClick = onClickSync
			)

			is DropboxService.Companion.DropboxSyncStatus.Connected -> MenuButton(
				icon = R.drawable.ic_cloud,
				onClick = onClickSync
			)

			is DropboxService.Companion.DropboxSyncStatus.Syncing -> MenuButton(
				icon = R.drawable.ic_cloud_syncing,
				modifier = Modifier.graphicsLayer {
					this.alpha = alpha
				},
				onClick = onClickSync
			)

			is DropboxService.Companion.DropboxSyncStatus.SyncError -> MenuButton(
				icon = R.drawable.ic_cloud_exclamation,
				onClick = onClickSync
			)

			is DropboxService.Companion.DropboxSyncStatus.DriveLocked -> MenuButton(
				icon = R.drawable.ic_cloud_exclamation,
				onClick = onClickSync
			)

			is DropboxService.Companion.DropboxSyncStatus.Idle -> MenuButton(
				icon = R.drawable.ic_cloud,
				onClick = onClickSync
			)
		}
	}
}

val Color.Companion.SyncCheck : Color
	get() = Color(0xFF82AAE3)

val Color.Companion.SyncNotCongifured : Color
	get() = Color(0xFFE94560)
val Color.Companion.SyncDisabled : Color
	get() = Color(0xFFE94560)

val Color.Companion.SyncNoInternet : Color
	get() = Color(0xFFE94560)

val Color.Companion.SyncNotLoggedIn : Color
	get() = Color(0xFFE94560)

val Color.Companion.SyncConnected : Color
	get() = Color(0xFF82AAE3)

val Color.Companion.SyncSyncing : Color
	get() = Color(0xFF82AAE3)

val Color.Companion.SyncError : Color
	get() = Color(0xFFE94560)

val Color.Companion.SyncLocked : Color
	get() = Color(0xFFE94560)
