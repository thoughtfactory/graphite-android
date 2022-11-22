package com.syncodec.graphite.presentation.main.composable.bar

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.LocalCompositionIsSelected
import com.syncodec.graphite.presentation.common.LocalCompositionOnSelect
import com.syncodec.graphite.presentation.common.LocalCompositionSelectedObjectIdList
import com.syncodec.graphite.presentation.common.button.MenuButton
import com.syncodec.graphite.presentation.common.button.stateButton.StateButton
import com.syncodec.graphite.presentation.common.button.stateButton.StateData
import com.syncodec.graphite.presentation.main.composable.LocalCompositionOpenBottomSheet
import com.syncodec.graphite.presentation.main.composable.LocalCompositionOpenDialog
import com.syncodec.graphite.presentation.main.composable.bottomSheet.MainBottomSheetType
import com.syncodec.graphite.presentation.main.composable.dialog.MainDialogType
import com.syncodec.graphite.presentation.main.composable.screen.ComponentType
import com.syncodec.graphite.presentation.ui.DeleteContainer
import com.syncodec.graphite.utils.Authenticator
import com.syncodec.graphite.utils.LocalAuthenticatorAction
import com.syncodec.graphite.utils.LocalVaultIsOpened


@Composable
fun TopBar(
	currentRoute : String?,
	componentType : ComponentType,
	onComponentChange : (Int) -> Unit,
	onClickSearch : () -> Unit
) {
	val containerColor by animateColorAsState(
		targetValue = when (currentRoute) {
			BottomNavigationItem.Home.route -> MaterialTheme.colorScheme.background
			BottomNavigationItem.Calendar.route -> MaterialTheme.colorScheme.background
			BottomNavigationItem.Atlas.route -> MaterialTheme.colorScheme.background
			else -> MaterialTheme.colorScheme.background
		}
	)

	val isSelected = LocalCompositionIsSelected.current

	Column(
		modifier = Modifier
			.fillMaxWidth()
			.background(containerColor)
	) {
		Bar(
			currentRoute = currentRoute,
			onClickSearch = onClickSearch
		)

		AnimatedVisibility(
			visible = currentRoute == BottomNavigationItem.Home.route && ! isSelected,
			enter = expandVertically(tween(300)),
			exit = shrinkVertically(tween(300))
		) {
			ComponentType(
				componentType = componentType,
			) { onComponentChange(it) }
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Bar(
	currentRoute : String?,
	onClickSearch : () -> Unit,
) {
	val openSheet = LocalCompositionOpenBottomSheet.current

	val isSelected = LocalCompositionIsSelected.current
	val selectedObjectIdList = LocalCompositionSelectedObjectIdList.current

	val onSelect = LocalCompositionOnSelect.current

	val isVaultOpened = LocalVaultIsOpened.current

	val openDialog = LocalCompositionOpenDialog.current

	val containerColor by animateColorAsState(
		targetValue = when (currentRoute) {
			"graphite" -> MaterialTheme.colorScheme.background
			"bucket" -> MaterialTheme.colorScheme.background
			"calendar" -> MaterialTheme.colorScheme.background
			"atlas" -> MaterialTheme.colorScheme.background
			else -> MaterialTheme.colorScheme.background
		}
	)

	val onAuthenticatorAction = LocalAuthenticatorAction.current

	Crossfade(
		targetState = isSelected,
		animationSpec = tween(300)
	) {
		if (it) {
			TopAppBar(
				navigationIcon = {
					MenuButton(
						icon = R.drawable.ic_close,
						tint = MaterialTheme.colorScheme.onBackground,
					) {
						onSelect(false)
						selectedObjectIdList.clear()
					}
				},
				title = {
					Text(
						text = if (selectedObjectIdList.isEmpty()) "No items selected" else if (selectedObjectIdList.size == 1) "1 item selected" else "${selectedObjectIdList.size} items selected",
						color = MaterialTheme.colorScheme.onBackground
					)
				},
				actions = {
					IconButton(
						onClick = { openDialog(MainDialogType.DELETE) }
					) {
						Icon(
							painter = painterResource(id = R.drawable.ic_delete),
							contentDescription = "Delete items",
							tint = Color.DeleteContainer
						)
					}
				},
				colors = TopAppBarDefaults.topAppBarColors(containerColor = containerColor)
			)
		} else {
			CenterAlignedTopAppBar(
				navigationIcon = {
					MenuButton(
						icon = R.drawable.ic_menu,
						tint = MaterialTheme.colorScheme.onBackground,
					) { openSheet(MainBottomSheetType.MENU) }
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
					MenuButton(
						icon = R.drawable.ic_vault,
						tint = if (isVaultOpened) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground,
						containerColor = if (isVaultOpened) MaterialTheme.colorScheme.primary else Color.Transparent,
						onClick = { onAuthenticatorAction(Authenticator.AUTHENTICATE) }
					)
					MenuButton(
						icon = R.drawable.ic_search,
						tint = MaterialTheme.colorScheme.onBackground,
						onClick = onClickSearch
					)
				},
				colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = containerColor)
			)
		}
	}
}

@Composable
private fun ComponentType(
	componentType : ComponentType,
	onStateChange : (Int) -> Unit
) {
	val openSheet = LocalCompositionOpenBottomSheet.current

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
						title = "Bucket",
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
				onStateChange = onStateChange,
				modifier = Modifier
					.height(36.dp)
					.weight(1f)
			)
			MenuButton(
				icon = R.drawable.ic_filter,
				tint = MaterialTheme.colorScheme.onBackground,
			) { openSheet(MainBottomSheetType.FILTER) }

			Spacer(modifier = Modifier.width(4.dp))
		}
		Spacer(modifier = Modifier.height(6.dp))
	}
}
