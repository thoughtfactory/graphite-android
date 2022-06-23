package com.syncodec.graphite.settingsComponent.miscellaneous

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import com.syncodec.graphite.R


@Composable
fun TopBar(
	title: String,
	scrollBehavior: TopAppBarScrollBehavior,
	onBack: () -> Unit
) {
	Bar(
		title = title,
		scrollBehavior = scrollBehavior
	) { onBack() }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun Bar(
	title: String,
	scrollBehavior: TopAppBarScrollBehavior,
	onBack: () -> Unit
) {
	LargeTopAppBar(
		navigationIcon = {
			IconButton(onClick = { onBack() }) {
				Icon(
					painter = painterResource(id = R.drawable.ic_back),
					contentDescription = "Back",
					tint = MaterialTheme.colorScheme.onBackground,
				)
			}
		},
		title = {
				AnimatedContent(targetState = title) {
					Text(text = it)
				}
		},
		colors = TopAppBarDefaults.largeTopAppBarColors(
			containerColor = MaterialTheme.colorScheme.background,
		),
		scrollBehavior = scrollBehavior,
	)
}
