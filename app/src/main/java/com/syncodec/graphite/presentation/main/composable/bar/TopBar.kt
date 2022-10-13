package com.syncodec.graphite.presentation.main.composable.bar

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.button.MenuButton
import com.syncodec.graphite.presentation.common.button.stateButton.StateButton
import com.syncodec.graphite.presentation.common.button.stateButton.StateData
import com.syncodec.graphite.presentation.main.composable.screen.ComponentType


@Composable
fun TopBar(
	currentRoute: String?,
	componentType: ComponentType,
	onComponentChange: (Int) -> Unit,
	onClickOpenMenu: () -> Unit,
	onClickOpenFilter: () -> Unit,
	onClickSearch: () -> Unit
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
			isSelected = false,
			selectedItemSize = 0,
			currentRoute = currentRoute,
			onClickOpenMenu = onClickOpenMenu,
			onClickOpenFilter = onClickOpenFilter,
			onClickSearch = onClickSearch
		)

		AnimatedVisibility(
			visible = currentRoute == BottomNavigationItem.Home.route,
			enter = expandVertically(tween(300)),
			exit = shrinkVertically(tween(300))
		) {
			ComponentType(
				showComponentChooser = true,
				componentType = componentType,
			) { onComponentChange(it) }
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Bar(
	isSelected: Boolean,
	selectedItemSize: Int,
	currentRoute: String?,
	onClickOpenMenu: () -> Unit,
	onClickOpenFilter: () -> Unit,
	onClickSearch: () -> Unit,
) {
	val containerColor by animateColorAsState(
		targetValue = when (currentRoute) {
			"graphite" -> MaterialTheme.colorScheme.background
			"bucket" -> MaterialTheme.colorScheme.background
			"calendar" -> MaterialTheme.colorScheme.background
			"atlas" -> MaterialTheme.colorScheme.background
			else -> MaterialTheme.colorScheme.background
		}
	)

	Crossfade(targetState = isSelected) {
		if (it) {
			TopAppBar(
				title = {
					Text(
						text = if (selectedItemSize == 0) "Select items to delete" else if (selectedItemSize == 1) "1 item selected" else "$selectedItemSize items selected",
						modifier = Modifier,
						style = MaterialTheme.typography.titleMedium,
						color = MaterialTheme.colorScheme.onBackground
					)
				},
				actions = {
					IconButton(onClick = {  }) {
						Icon(
							painter = painterResource(id = R.drawable.ic_delete),
							contentDescription = "Delete items",
							tint = Color(0xFFF05945)
						)
					}
				},
				colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = containerColor)
			)
		} else {
			CenterAlignedTopAppBar(
				navigationIcon = {
					MenuButton(
						icon = R.drawable.ic_menu,
						tint = MaterialTheme.colorScheme.onBackground,
						onClick = onClickOpenMenu
					)
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
						icon = R.drawable.ic_filter,
						tint = MaterialTheme.colorScheme.onBackground,
						onClick = onClickOpenFilter
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
	showComponentChooser: Boolean,
	componentType: ComponentType,
	onStateChange: (Int) -> Unit
) {
	AnimatedVisibility(
		visible = showComponentChooser,
		enter = expandVertically(tween(600)) + fadeIn(tween(300)),
		exit = shrinkVertically(tween(600)) + fadeOut(tween(300)),
		modifier = Modifier
			.fillMaxWidth()
			.padding(12.dp, 0.dp),
	) {
		Column(
			modifier = Modifier.fillMaxWidth()
		) {
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
				modifier = Modifier.height(36.dp),
				onStateChange = onStateChange
			)
			Spacer(modifier = Modifier.height(6.dp))
		}
	}
}
