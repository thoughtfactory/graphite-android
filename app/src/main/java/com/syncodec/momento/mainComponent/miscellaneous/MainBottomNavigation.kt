package com.syncodec.momento.mainComponent.miscellaneous

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.*
import androidx.compose.material3.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.google.accompanist.pager.ExperimentalPagerApi
import com.syncodec.momento.custom.googleMap.rememberMapViewWithLifecycle
import com.syncodec.momento.mainComponent.screen.*
import compose.icons.TablerIcons
import compose.icons.tablericons.*

open class BottomNavigationItem(var route: String, var icon: ImageVector, var title: String) {
	object Momento : BottomNavigationItem("momento", TablerIcons.Signature, "Momento")
	object Bucket : BottomNavigationItem("bucket", TablerIcons.LayoutCards, "Bucket")
	object Calendar : BottomNavigationItem("calendar", TablerIcons.CalendarEvent, "Calendar")
	object Atlas : BottomNavigationItem("atlas", TablerIcons.Map, "Atlas")
	object Me : BottomNavigationItem("me", TablerIcons.User, "Me")
}

@Composable
fun BottomNavigationBar(
	navController: NavController
) {
	val items = listOf(
		BottomNavigationItem.Momento,
		BottomNavigationItem.Bucket,
		BottomNavigationItem.Calendar,
		BottomNavigationItem.Atlas,
		BottomNavigationItem.Me
	)

	NavigationBar(
		modifier = Modifier.fillMaxWidth(),
		containerColor = MaterialTheme.colorScheme.background,
	) {
		val navBackStackEntry by navController.currentBackStackEntryAsState()
		val currentRoute = navBackStackEntry?.destination?.route

		items.forEach { item ->
			NavigationBarItem(
				selected = currentRoute == item.route,
				onClick = {
					if (item.route != currentRoute) {
						navController.navigate(item.route) {
							navController.graph.startDestinationRoute?.let { route ->
								popUpTo(route) {
									saveState = true
								}
							}
							launchSingleTop = true
							restoreState = true
						}
					}
				},
				icon = { Icon(imageVector = item.icon, contentDescription = item.title) },
				modifier = Modifier,
				label = {
					Text(
						text = item.title,
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
				)
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
) {
	val mapView = rememberMapViewWithLifecycle()

	NavHost(
		navController = navController,
		startDestination = BottomNavigationItem.Momento.route
	) {
		composable(BottomNavigationItem.Momento.route) {
			CompositionLocalProvider(
				LocalViewModelStoreOwner provides viewModelStoreOwner
			) {
				MomentoScreen()
			}
		}
		composable(BottomNavigationItem.Bucket.route) {
			CompositionLocalProvider(
				LocalViewModelStoreOwner provides viewModelStoreOwner
			) {
				BucketScreen()
			}
		}
		composable(BottomNavigationItem.Calendar.route) {
			CompositionLocalProvider(
				LocalViewModelStoreOwner provides viewModelStoreOwner
			) {
				CalendarScreen()
			}
		}
		composable(BottomNavigationItem.Atlas.route) {
			CompositionLocalProvider(
				LocalViewModelStoreOwner provides viewModelStoreOwner
			) {
				AtlasScreen(mapView = mapView)
			}
		}
		composable(BottomNavigationItem.Me.route) {
			CompositionLocalProvider(
				LocalViewModelStoreOwner provides viewModelStoreOwner
			) {
				MeScreen()
			}
		}
	}
}
