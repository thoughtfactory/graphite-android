package com.syncodec.momento.mainComponent.miscellaneous

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.*
import androidx.compose.material3.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.google.accompanist.pager.ExperimentalPagerApi
import com.syncodec.momento.MainActivity
import com.syncodec.momento.custom.googleMap.rememberMapViewWithLifecycle
import com.syncodec.momento.mainComponent.MainViewModel
import com.syncodec.momento.mainComponent.screen.*
import compose.icons.TablerIcons
import compose.icons.tablericons.*
import kotlinx.coroutines.InternalCoroutinesApi

open class BottomNavigationItem(var route: String, var icon: ImageVector, var title: String) {
	object Momento : BottomNavigationItem("momento", TablerIcons.Signature, "Momento")
	object Bucket : BottomNavigationItem("bucket", TablerIcons.LayoutCards, "Bucket")
	object Calendar : BottomNavigationItem("calendar", TablerIcons.CalendarEvent, "Calendar")
	object Atlas : BottomNavigationItem("atlas", TablerIcons.Map, "Atlas")
	object Me : BottomNavigationItem("me", TablerIcons.User, "Me")
}

@OptIn(InternalCoroutinesApi::class)
@Composable
fun BottomNavigationBar(
	navController: NavController
) {
	val screens = listOf(
		BottomNavigationItem.Momento,
		BottomNavigationItem.Bucket,
		BottomNavigationItem.Calendar,
		BottomNavigationItem.Atlas,
		BottomNavigationItem.Me
	)

	NavigationBar(
		containerColor = MaterialTheme.colorScheme.surface,
		tonalElevation = 0.dp,
		modifier = Modifier.fillMaxWidth()
	) {
		val navBackStackEntry by navController.currentBackStackEntryAsState()
		val currentRoute = navBackStackEntry?.destination?.route

		screens.forEach { screen ->
			NavigationBarItem(
				onClick = {
					if (currentRoute == screen.route) {
						return@NavigationBarItem
					} else {
						navController.navigate(screen.route) {
							popUpTo(navController.graph.findStartDestination().id) { saveState = true }
							launchSingleTop = true
							restoreState = true
						}
					}
				},
				icon = { Icon(imageVector = screen.icon, contentDescription = screen.title) },
				label = {
					Text(
						text = screen.title,
						textAlign = TextAlign.Center,
						fontWeight = FontWeight.Bold,
						style = MaterialTheme.typography.bodySmall,
						maxLines = 1,
						lineHeight = 12.sp
					)
				},
				colors = NavigationBarItemDefaults.colors(
					selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
					unselectedIconColor = MaterialTheme.colorScheme.onBackground,
					selectedTextColor = MaterialTheme.colorScheme.onBackground,
					unselectedTextColor = MaterialTheme.colorScheme.onBackground
				),
				selected = currentRoute == screen.route,
				interactionSource = remember { MutableInteractionSource() },
				modifier = Modifier,
			)
		}
	}
}

@ExperimentalPagerApi
@ExperimentalFoundationApi
@ExperimentalMaterialApi
@Composable
fun MainNavigation(
	navController: NavHostController,
	viewModelStoreOwner: ViewModelStoreOwner,
	onClick: (MainActivity.Click, Any?) -> Unit
) {
	val mapView = rememberMapViewWithLifecycle()
	val viewModel: MainViewModel = viewModel()

	val vaultState by viewModel.activityState.vaultState
	var showArchived by viewModel.activityState.showArchived
	var showFavourite by viewModel.activityState.showFavourite
	var showLocked by viewModel.activityState.showLocked
	val isSelected by viewModel.activityState.isSelected
	val selectedItemList = viewModel.activityState.selectedItemList

	val noteList = viewModel.defaultNoteList
	val notebookList = viewModel.notebookList
	val bucketMap = viewModel.bucketMap

	val momentoComponentType by viewModel.activityState.momentoComponentType

	NavHost(
		navController = navController,
		startDestination = BottomNavigationItem.Momento.route
	) {
		composable(BottomNavigationItem.Momento.route) {
			CompositionLocalProvider(
				LocalViewModelStoreOwner provides viewModelStoreOwner
			) {
				MomentoScreen(
					noteList = noteList,
					notebookList = notebookList,
					momentoComponentType = momentoComponentType,
					isSelected = isSelected,
					selectedItemList = selectedItemList
				) { click, data -> onClick(click, data) }
			}
		}
		composable(BottomNavigationItem.Bucket.route) {
			CompositionLocalProvider(
				LocalViewModelStoreOwner provides viewModelStoreOwner
			) {
				BucketScreen(
					bucketMap = bucketMap,
					isSelected = isSelected,
					selectedItemList = selectedItemList
				) { click, data -> onClick(click, data) }
			}
		}
		composable(BottomNavigationItem.Calendar.route) {
			CompositionLocalProvider(
				LocalViewModelStoreOwner provides viewModelStoreOwner
			) {
				CalendarScreen(
					noteList = noteList,
					isSelected = isSelected,
					selectedItemList = selectedItemList
				) { click, data -> onClick(click, data) }
			}
		}
		composable(BottomNavigationItem.Atlas.route) {
			CompositionLocalProvider(
				LocalViewModelStoreOwner provides viewModelStoreOwner
			) {
				AtlasScreen(
					mapView = mapView,
					noteList = noteList,
					isSelected = isSelected,
					selectedItemList = selectedItemList
				) { click, data -> onClick(click, data) }
			}
		}
		composable(BottomNavigationItem.Me.route) {
			CompositionLocalProvider(
				LocalViewModelStoreOwner provides viewModelStoreOwner
			) {
				MeScreen(noteList = noteList) { click, data -> onClick(click, data) }
			}
		}
	}
}
