package com.syncodec.graphite.custom

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.syncodec.graphite.R


@Composable
fun SplashScreen() {
	val systemUiController = rememberSystemUiController()
	systemUiController.setStatusBarColor(MaterialTheme.colorScheme.surface)
	val configuration = LocalConfiguration.current
	val screenWidth = configuration.screenWidthDp.dp

	Box(
		contentAlignment = Alignment.Center,
		modifier = Modifier
			.fillMaxSize()
			.background(MaterialTheme.colorScheme.surface),
	) {
		Image(
			painter = painterResource(id = R.drawable.il_login_background),
			contentDescription = null,
			modifier = Modifier.requiredSize(screenWidth / 2)
		)
	}
}
