package com.syncodec.momento.mainComponent.miscellaneous

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
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
	BottomNavigation(
		backgroundColor = MaterialTheme.colorScheme.background,
		elevation = 0.dp
	) {
		val navBackStackEntry by navController.currentBackStackEntryAsState()
		val currentRoute = navBackStackEntry?.destination?.route

		items.forEach { item ->
			BottomNavigationItem(
				icon = {
					Icon(
						imageVector = item.icon,
						tint = if (currentRoute == item.route) MaterialTheme.colorScheme.primary else Color.DarkGray,
						contentDescription = null
					)
				},
				label = {
					Text(
						text = item.title,
						fontWeight = FontWeight.Bold,
						color = if (currentRoute == item.route) MaterialTheme.colorScheme.primary else Color.DarkGray,
						textAlign = TextAlign.Center,
						style = MaterialTheme.typography.bodySmall,
						maxLines = 1
					)
				},
				selectedContentColor = Color.White,
				unselectedContentColor = Color.White.copy(0.4f),
				alwaysShowLabel = true,
				selected = currentRoute == item.route,
				onClick = {
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
