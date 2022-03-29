package com.syncodec.momento.settings.miscellaneous

import android.app.Activity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.syncodec.momento.settings.SettingsActivity
import compose.icons.TablerIcons
import compose.icons.tablericons.ArrowBack


@OptIn(ExperimentalMaterialApi::class, ExperimentalAnimationApi::class)
@Composable
fun TopBar(currentPath: List<SettingsActivity.Companion.Path>, ) {
	val activity = LocalContext.current as? Activity

	Box(
		modifier = Modifier
			.fillMaxWidth()
			.height(64.dp)
			.background(MaterialTheme.colorScheme.background)
	) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.Start,
			modifier = Modifier
				.fillMaxWidth()
				.fillMaxHeight()
				.padding(8.dp)
		) {
			IconButton(onClick = { activity?.onBackPressed() }) {
				Icon(
					imageVector = TablerIcons.ArrowBack,
					contentDescription = "Back",
					tint = MaterialTheme.colorScheme.onBackground,
				)
			}

			Spacer(modifier = Modifier.width(8.dp))


			Title(title = SettingsActivity.PathMap[currentPath.last()]!!)
		}
	}
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun Title(title: String) {
	AnimatedContent(targetState = title) {
		Text(
			text = it,
			style = MaterialTheme.typography.titleMedium,
			fontWeight = FontWeight.Bold,
			color = MaterialTheme.colorScheme.onBackground
		)
	}
}
