package com.syncodec.graphite.presentation.main.composable.bar

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.LoadingView
import com.syncodec.graphite.presentation.explorer.screen.explorerScreen.ExplorerScreen
import com.syncodec.graphite.presentation.main.composable.bottomSheet.MainBottomSheetType
import com.syncodec.graphite.presentation.main.composable.screen.ComponentType
import com.syncodec.graphite.presentation.main.composable.screen.HomeScreen
import com.syncodec.graphite.presentation.ui.IconButtonSize
import com.syncodec.graphite.utils.Extra
import com.syncodec.graphite.utils.tone
import io.realm.kotlin.types.RealmUUID


open class BottomNavigationItem(val route : String, val icon : Int, val title : String) {
	object Home : BottomNavigationItem("home", R.drawable.ic_home, "Home")
	object Calendar : BottomNavigationItem("calendar", R.drawable.ic_calendar, "Calendar")
	object Atlas : BottomNavigationItem("atlas", R.drawable.ic_atlas, "Atlas")
}

@Composable
fun BottomNavigationBar(
	currentRoute : String?,
	onNavigation : (BottomNavigationItem) -> Unit
) {
	val screens = listOf(
		BottomNavigationItem.Home,
		BottomNavigationItem.Calendar,
		BottomNavigationItem.Atlas,
	)

	NavigationBar(
		tonalElevation = 8.dp,
		modifier = Modifier.fillMaxWidth()
	) {
		screens.forEach { screen ->
			NavigationBarItem(
				onClick = { onNavigation(screen) },
				icon = {
					Icon(
						painter = painterResource(id = screen.icon),
						contentDescription = screen.title,
						modifier = Modifier.requiredSize(IconButtonSize)
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
					selectedIconColor = MaterialTheme.colorScheme.onPrimary,
					unselectedIconColor = MaterialTheme.colorScheme.onSurface.tone(isSystemInDarkTheme(), 1),
					selectedTextColor = MaterialTheme.colorScheme.onSurface.tone(isSystemInDarkTheme(), 1),
					unselectedTextColor = MaterialTheme.colorScheme.onSurface.tone(isSystemInDarkTheme(), 1),
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
	currentRoute : BottomNavigationItem = BottomNavigationItem.Home,
	componentType : ComponentType = ComponentType.Note,
	isSelecting : Boolean = false,
	onSelect : (RealmUUID) -> Unit = {},
	selectedIdList : List<RealmUUID> = listOf(),
	openSheet : (MainBottomSheetType) -> Unit = {},
) {
	Crossfade(
		targetState = currentRoute,
	) {
		when (it) {
			BottomNavigationItem.Home -> HomeScreen(
				componentType = componentType,
				isSelecting = isSelecting,
				onSelect = onSelect,
				selectedIdList = selectedIdList,
				onClickNewList = { openSheet(MainBottomSheetType.Bucket) },
				onClickNewNotebook = { openSheet(MainBottomSheetType.Notebook) },
			)

			BottomNavigationItem.Calendar -> ExplorerScreen(
				explorerType = Extra.Companion.ExplorerType.Calendar,
				searchInDefaultChapter = true,
				isStatic = true,
				isSelecting = isSelecting,
				onSelect = onSelect,
				selectedIdList = selectedIdList,
			)

			BottomNavigationItem.Atlas -> ExplorerScreen(
				explorerType = Extra.Companion.ExplorerType.Atlas,
				searchInDefaultChapter = true,
				isStatic = true,
				isSelecting = isSelecting,
				onSelect = onSelect,
				selectedIdList = selectedIdList,
			)

			null -> LoadingView()
		}
	}
}
