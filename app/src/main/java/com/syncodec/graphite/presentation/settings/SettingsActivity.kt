package com.syncodec.graphite.presentation.settings

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.syncodec.graphite.presentation.settings.composable.BaseScreen
import com.syncodec.graphite.presentation.settings.composable.SettingsNavigator
import com.syncodec.graphite.presentation.settings.composable.screen.PreferenceScreen
import com.syncodec.graphite.presentation.settings.composable.screen.ThemeScreen
import com.syncodec.graphite.presentation.ui.BaseContent


class SettingsActivity : ComponentActivity() {

	@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)


		setContent {
			BaseContent {
				val systemUiController = rememberSystemUiController()
				systemUiController.setStatusBarColor(MaterialTheme.colorScheme.background)
				systemUiController.setNavigationBarColor(MaterialTheme.colorScheme.background)

				val navController = rememberAnimatedNavController()

				Scaffold(
					topBar = {
//						TopBar(navController = navController, scrollBehavior = scrollBehavior) {}
					}, modifier = Modifier.fillMaxSize()
				) {
					Box(modifier = Modifier.padding(it)) {
						AnimatedNavHost(
							navController = navController,
							startDestination = SettingsNavigator.BASE.name,
						) {
							composable(
								route = SettingsNavigator.BASE.name,
								enterTransition = {
									slideInHorizontally(tween(300), { -it }) +
											fadeIn(tween(300))
								},
								exitTransition = {
									slideOutHorizontally(tween(300), { -it }) +
											fadeOut(tween(300))
								}
							) {
								BaseScreen(navController)
							}
							composable(
								route = SettingsNavigator.PREFERENCE.name,
								enterTransition = {
									slideInHorizontally(tween(300), { it }) +
											fadeIn(tween(300))
								},
								exitTransition = {
									slideOutHorizontally(tween(300), { it }) +
											fadeOut(tween(300))
								}
							) {
								PreferenceScreen(navController)
							}
							composable(
								route = SettingsNavigator.THEME.name,
								enterTransition = {
									slideInHorizontally(tween(300), { -it }) +
											fadeIn(tween(300))
								},
								exitTransition = {
									slideOutHorizontally(tween(300), { -it }) +
											fadeOut(tween(300))
								}
							) {
								ThemeScreen()
							}
						}
					}
				}
			}
		}
	}
}
