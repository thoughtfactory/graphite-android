package com.syncodec.graphite.presentation.main.composable.bar

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.syncodec.graphite.R


sealed class BottomNavigationItem(val route: String, val icon: Int, val iconFilled: Int, val title: String) {
	data object Home : BottomNavigationItem(route = "home", icon = R.drawable.ic_fa_home, iconFilled = R.drawable.ic_fa_home_solid, title = "Home")
	data object Calendar : BottomNavigationItem(route = "calendar", icon = R.drawable.ic_fa_calendar, iconFilled = R.drawable.ic_fa_calendar_solid, title = "Calendar")
	data object Atlas : BottomNavigationItem(route = "atlas", icon = R.drawable.ic_fa_atlas, iconFilled = R.drawable.ic_fa_atlas_solid, title = "Atlas")
}

@Composable
fun BottomBar(
	currentRoute: String?,
	onNavigation: (BottomNavigationItem) -> Unit
) {
	val screens = listOf(
		BottomNavigationItem.Home,
		BottomNavigationItem.Calendar,
		BottomNavigationItem.Atlas,
	)

	NavigationBar(
		tonalElevation = 0.dp,
		containerColor = Color.Black,
		modifier = Modifier.fillMaxWidth()
	) {
		screens.forEach { screen ->
			NavigationBarItem(
				onClick = { onNavigation(screen) },
				icon = {
					Icon(
						painter = painterResource(id = if (screen.route == currentRoute) screen.iconFilled else screen.icon),
						contentDescription = screen.title,
						modifier = Modifier
							.requiredSize(22.dp)
							.padding(2.dp)
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
					selectedIconColor = Color.Black,
					selectedTextColor = Color.White,
					indicatorColor = Color.White,
					unselectedIconColor = Color.White,
					unselectedTextColor = Color.White,
				),
				selected = currentRoute == screen.route,
				interactionSource = remember { MutableInteractionSource() },
				modifier = Modifier,
			)
		}
	}
}
