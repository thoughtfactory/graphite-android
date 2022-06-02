package com.syncodec.graphite.mainComponent.miscellaneous

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
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
import com.syncodec.graphite.Graphite
import com.syncodec.graphite.R
import com.syncodec.graphite.custom.googleMap.rememberMapViewWithLifecycle
import com.syncodec.graphite.mainComponent.MainActivity
import com.syncodec.graphite.mainComponent.MainViewModel
import com.syncodec.graphite.mainComponent.screen.AtlasScreen
import com.syncodec.graphite.mainComponent.screen.BucketScreen
import com.syncodec.graphite.mainComponent.screen.CalendarScreen
import com.syncodec.graphite.mainComponent.screen.GraphiteScreen
import com.syncodec.graphite.miscellaneous.ThemeUtils.Companion.tone


open class BottomNavigationItem(var route: String, var icon: Int, var title: String) {
	object Graphite : BottomNavigationItem("graphite", R.drawable.ic_icon, "Graphite")
	object Bucket : BottomNavigationItem("bucket", R.drawable.ic_bucket, "Bucket")
	object Calendar : BottomNavigationItem("calendar", R.drawable.ic_calendar, "Calendar")
	object Atlas : BottomNavigationItem("atlas", R.drawable.ic_atlas, "Atlas")
}

@Composable
fun BottomNavigationBar(
	currentRoute: String?,
	onNavigation: (String) -> Unit
) {
	val screens = listOf(
		BottomNavigationItem.Graphite,
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
						fontWeight = FontWeight.Bold,
						maxLines = 1,
						lineHeight = 12.sp
					)
				},
				colors = NavigationBarItemDefaults.colors(
					selectedIconColor = if (screen.route == "graphite") Color.Unspecified else MaterialTheme.colorScheme.onPrimary,
					unselectedIconColor = if (screen.route == "graphite") Color.Unspecified else MaterialTheme.colorScheme.onSurface.tone(
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
	val showArchived by viewModel.showArchived
	val showFavourite by viewModel.showFavourite
	val showLocked by viewModel.showLocked
	val isSelected by viewModel.isSelected
	val selectedItemList = viewModel.selectedItemList

	val noteMap = viewModel.defaultNoteMap
	val notebookListFlow by viewModel.notebookListFlow.collectAsState(initial = listOf())
	val bucketList = viewModel.bucketList
	val quote by viewModel.quote
	val quoteBg by viewModel.quoteBg

	val componentType by viewModel.componentType
	val bucketFilter = viewModel.bucketFilter

//	showArchived	showFavourite	showLocked	isVaultOpen		filter
//
//      FALSE	        FALSE	        FALSE	    FALSE		!arc && !lock
//      FALSE	        FALSE	        FALSE	    TRUE		!arc
//      FALSE	        FALSE	        TRUE	    FALSE		*
//      FALSE	        FALSE	        TRUE	    TRUE		!arc && lock
//      FALSE	        TRUE	        FALSE	    FALSE		!arc && fav && !lock
//      FALSE	        TRUE	        FALSE	    TRUE		!arc && fav
//      FALSE	        TRUE	        TRUE	    FALSE		*
//      FALSE	        TRUE	        TRUE	    TRUE		!arc && fav && lock
//      TRUE	        FALSE	        FALSE	    FALSE		arc && !lock
//      TRUE	        FALSE	        FALSE	    TRUE		arc
//      TRUE	        FALSE	        TRUE	    FALSE		*
//      TRUE	        FALSE	        TRUE	    TRUE		arc && lock
//      TRUE	        TRUE	        FALSE	    FALSE		arc && fav && !lock
//      TRUE	        TRUE	        FALSE	    TRUE		arc && fav
//      TRUE	        TRUE	        TRUE	    FALSE		*
//      TRUE	        TRUE	        TRUE	    TRUE		arc && fav && lock
//


	val filteredNoteMap = noteMap.filter {
		when {
			showArchived && showFavourite && showLocked && vaultState == Graphite.Companion.VaultState.OPENED -> it.value.isArchived && it.value.isFavourite && it.value.isLocked
//			showArchived && showFavourite && showLocked && vaultState != Graphite.Companion.VaultState.OPENED -> it.value.isArchived && it.value.isFavourite && it.value.isLocked
			showArchived && showFavourite && !showLocked && vaultState == Graphite.Companion.VaultState.OPENED -> it.value.isArchived && it.value.isFavourite
			showArchived && showFavourite && !showLocked && vaultState != Graphite.Companion.VaultState.OPENED -> it.value.isArchived && it.value.isFavourite && !it.value.isLocked
			showArchived && !showFavourite && showLocked && vaultState == Graphite.Companion.VaultState.OPENED -> it.value.isArchived && it.value.isLocked
//			showArchived && !showFavourite && showLocked && vaultState != Graphite.Companion.VaultState.OPENED -> it.value.isArchived && it.value.isFavourite && it.value.isLocked
			showArchived && !showFavourite && !showLocked && vaultState == Graphite.Companion.VaultState.OPENED -> it.value.isArchived
			showArchived && !showFavourite && !showLocked && vaultState != Graphite.Companion.VaultState.OPENED -> it.value.isArchived && !it.value.isLocked
			!showArchived && showFavourite && showLocked && vaultState == Graphite.Companion.VaultState.OPENED -> !it.value.isArchived && it.value.isFavourite && it.value.isLocked
//			!showArchived && showFavourite && showLocked && vaultState != Graphite.Companion.VaultState.OPENED -> it.value.isArchived && it.value.isFavourite && it.value.isLocked
			!showArchived && showFavourite && !showLocked && vaultState == Graphite.Companion.VaultState.OPENED -> !it.value.isArchived && it.value.isFavourite
			!showArchived && showFavourite && !showLocked && vaultState != Graphite.Companion.VaultState.OPENED -> !it.value.isArchived && it.value.isFavourite && !it.value.isLocked
			!showArchived && !showFavourite && showLocked && vaultState == Graphite.Companion.VaultState.OPENED -> !it.value.isArchived && it.value.isLocked
//			!showArchived && !showFavourite && showLocked && vaultState != Graphite.Companion.VaultState.OPENED -> it.value.isArchived && it.value.isFavourite && it.value.isLocked
			!showArchived && !showFavourite && !showLocked && vaultState == Graphite.Companion.VaultState.OPENED -> !it.value.isArchived
			!showArchived && !showFavourite && !showLocked && vaultState != Graphite.Companion.VaultState.OPENED -> !it.value.isArchived && !it.value.isLocked
			else -> !it.value.isArchived && !it.value.isLocked
		}
	}

	NavHost(
		navController = navController,
		startDestination = BottomNavigationItem.Graphite.route
	) {
		composable(BottomNavigationItem.Graphite.route) {
			CompositionLocalProvider(
				LocalViewModelStoreOwner provides viewModelStoreOwner
			) {
				GraphiteScreen(
					noteMap = filteredNoteMap,
					notebookListFlow = notebookListFlow,
					componentType = componentType,
					isSelected = isSelected,
					selectedItemList = selectedItemList,
					isFilterActive = showArchived || showFavourite || showLocked,
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
					noteMap = filteredNoteMap,
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
					noteMap = filteredNoteMap,
					selectedItemList = selectedItemList,
					onAction = onAction
				)
			}
		}
	}
}
