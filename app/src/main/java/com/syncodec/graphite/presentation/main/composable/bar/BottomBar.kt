package com.syncodec.graphite.presentation.main.composable.bar

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.syncodec.graphite.presentation.main.MainComponent


@Composable
fun BottomBar(
	currentRoute: MainComponent = MainComponent.Home,
	onNavigation: (MainComponent) -> Unit = {},
) {
	val screens = remember {
		listOf(
			MainComponent.Home,
			MainComponent.Calendar,
			MainComponent.Atlas,
		)
	}
	NavigationBar(
		tonalElevation = 0.dp,
		containerColor = Color.Black,
		modifier = Modifier.fillMaxWidth()
	) {
		screens.forEach { screen ->
			NavigationBarItem(
				icon = {
//					https://issuetracker.google.com/issues/316327367
//					Color should change colors. Remove tint when fixed.
					Icon(
						painter = painterResource(id = if (screen == currentRoute) screen.iconFilled else screen.icon),
						contentDescription = stringResource(id = screen.title),
						tint = if (screen == currentRoute) Color.Black else Color.White,
						modifier = Modifier
							.requiredSize(22.dp)
							.padding(2.dp)
					)
				},
				label = {
					Text(
						text = stringResource(id = screen.title),
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
				selected = currentRoute == screen,
				onClick = { onNavigation(screen) },
			)
		}
	}
}
