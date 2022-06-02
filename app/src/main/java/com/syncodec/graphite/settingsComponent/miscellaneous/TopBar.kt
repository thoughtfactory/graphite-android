package com.syncodec.graphite.settingsComponent.miscellaneous

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.settingsComponent.SettingsActivity


@Composable
fun TopBar(
	currentPath: List<SettingsActivity.Companion.Path>,
	onBack: () -> Unit
) {
	Bar(
		title = SettingsActivity.PathNameMap[currentPath.last()]!!,
		icon = SettingsActivity.PathIconMap[currentPath.last()]!!,
		currentPath = currentPath
	) { onBack() }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun Bar(
	title: String,
	icon: Int,
	currentPath: List<SettingsActivity.Companion.Path>,
	onBack: () -> Unit
) {
	SmallTopAppBar(
		navigationIcon = {
			IconButton(onClick = { onBack() }) {
				Icon(
					painter = painterResource(id = R.drawable.ic_back),
					contentDescription = "Back",
					tint = MaterialTheme.colorScheme.onBackground,
					modifier = Modifier
						.requiredSize(32.dp)
						.padding(4.dp)
				)
			}
		},
		title = {
			AnimatedContent(
				targetState = title,
				transitionSpec = {
					fadeIn(tween(300)) + scaleIn(tween(300)) with
							fadeOut(tween(300)) + scaleOut(tween(300))
				}
			) {
				Text(
					text = it,
					style = MaterialTheme.typography.titleMedium,
					fontWeight = FontWeight.Bold,
					color = MaterialTheme.colorScheme.onBackground
				)
			}
		},
		actions = {
			AnimatedContent(
				targetState = icon,
				transitionSpec = {
					fadeIn(tween(300)) + scaleIn(tween(300)) with
							fadeOut(tween(300)) + scaleOut(tween(300))
				}
			) {
				Icon(
					painter = painterResource(id = it),
					contentDescription = title,
					tint = if (currentPath.lastOrNull() == SettingsActivity.Companion.Path.GRAPHITE) Color.Unspecified else MaterialTheme.colorScheme.onBackground,
					modifier = Modifier
						.requiredSize(32.dp)
						.padding(4.dp)
				)
			}
			Spacer(modifier = Modifier.width(8.dp))
		},
		colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = MaterialTheme.colorScheme.background)
	)
}
