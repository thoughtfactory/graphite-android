package com.syncodec.momento.mainComponent.miscellaneous

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.google.accompanist.pager.ExperimentalPagerApi
import com.syncodec.momento.MainActivity
import com.syncodec.momento.R
import com.syncodec.momento.custom.googleMap.rememberMapViewWithLifecycle
import com.syncodec.momento.mainComponent.MainViewModel
import com.syncodec.momento.mainComponent.screen.AtlasScreen
import com.syncodec.momento.mainComponent.screen.BucketScreen
import com.syncodec.momento.mainComponent.screen.CalendarScreen
import com.syncodec.momento.mainComponent.screen.MomentoScreen
import com.syncodec.momento.miscellaneous.ThemeUtils.Companion.tone
import kotlinx.coroutines.InternalCoroutinesApi


open class BottomNavigationItem(var route: String, var icon: Int, var title: String) {
	object Momento : BottomNavigationItem("momento", R.drawable.ic_write, "Momento")
	object Bucket : BottomNavigationItem("bucket", R.drawable.ic_bucket, "Bucket")
	object Calendar : BottomNavigationItem("calendar", R.drawable.ic_calendar, "Calendar")
	object Atlas : BottomNavigationItem("atlas", R.drawable.ic_atlas, "Atlas")
}

@OptIn(InternalCoroutinesApi::class)
@Composable
fun BottomNavigationBar(
	currentRoute: String?,
	onNavigation: (String) -> Unit
) {
	val screens = listOf(
		BottomNavigationItem.Momento,
		BottomNavigationItem.Bucket,
		BottomNavigationItem.Calendar,
		BottomNavigationItem.Atlas,
	)

	NavigationBar(
		containerColor = MaterialTheme.colorScheme.surface.tone(isSystemInDarkTheme(), 1),
		tonalElevation = 0.dp,
		modifier = Modifier.fillMaxWidth()
	) {
		screens.forEach { screen ->
			NavigationBarItem(
				onClick = { onNavigation(screen.route) },
				icon = {
					Icon(
						painter = painterResource(id = screen.icon),
						contentDescription = screen.title,
						modifier = Modifier.requiredSize(20.dp)
					)
				},
				label = {
					Text(
						text = screen.title,
						textAlign = TextAlign.Center,
						style = MaterialTheme.typography.bodyMedium,
						maxLines = 1,
						lineHeight = 12.sp
					)
				},
				colors = NavigationBarItemDefaults.colors(
					selectedIconColor = MaterialTheme.colorScheme.onPrimary,
					unselectedIconColor = MaterialTheme.colorScheme.onSurface.tone(
						isSystemInDarkTheme(), 1
					),
					selectedTextColor = MaterialTheme.colorScheme.onSurface,
					unselectedTextColor = MaterialTheme.colorScheme.onSurface,
					indicatorColor = MaterialTheme.colorScheme.primary
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
	onAction: (MainActivity.Action, Any?) -> Unit
) {
	val mapView = rememberMapViewWithLifecycle()
	val viewModel: MainViewModel = viewModel()

	val vaultState by viewModel.vaultState
	var showArchived by viewModel.showArchived
	var showFavourite by viewModel.showFavourite
	var showLocked by viewModel.showLocked
	val isSelected by viewModel.isSelected
	val selectedItemList = viewModel.selectedItemList

	val noteMap = viewModel.defaultNoteMap
	val notebookMap = viewModel.notebookMap
	val bucketList = viewModel.bucketList
	val quote by viewModel.quote
	val quoteBg by viewModel.quoteBg

	val componentType by viewModel.componentType
	val bucketFilter = viewModel.bucketFilter

	NavHost(
		navController = navController,
		startDestination = BottomNavigationItem.Momento.route
	) {
		composable(BottomNavigationItem.Momento.route) {
			CompositionLocalProvider(
				LocalViewModelStoreOwner provides viewModelStoreOwner
			) {
				MomentoScreen(
					noteMap = noteMap,
					notebookMap = notebookMap,
					componentType = componentType,
					isSelected = isSelected,
					selectedItemList = selectedItemList,
					quote = quote,
					quoteBg = quoteBg,
					onAction = onAction
				)
			}
		}
		composable(BottomNavigationItem.Bucket.route) {
			CompositionLocalProvider(
				LocalViewModelStoreOwner provides viewModelStoreOwner
			) {
				BucketScreen(
					bucketList = bucketList,
					selectedItemList = selectedItemList,
					bucketFilter = bucketFilter,
					onAction = onAction
				)
			}
		}
		composable(BottomNavigationItem.Calendar.route) {
			CompositionLocalProvider(
				LocalViewModelStoreOwner provides viewModelStoreOwner
			) {
				CalendarScreen(
					noteMap = noteMap,
					selectedItemList = selectedItemList,
					onAction = onAction
				)
			}
		}
		composable(BottomNavigationItem.Atlas.route) {
			CompositionLocalProvider(
				LocalViewModelStoreOwner provides viewModelStoreOwner
			) {
				AtlasScreen(
					mapView = mapView,
					noteMap = noteMap,
					selectedItemList = selectedItemList,
					onAction = onAction
				)
			}
		}
	}
}
