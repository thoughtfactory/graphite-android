package com.syncodec.graphite.notebookComponent.miscellaneous

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.miscellaneous.ThemeUtils.Companion.tone


@Composable
fun Breadcrumb(
	chapterNamePath: SnapshotStateList<String>,
	onAction: (Int) -> Unit
) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.height(24.dp)
			.horizontalScroll(state = rememberScrollState()),
		verticalAlignment = Alignment.CenterVertically
	) {
		Spacer(modifier = Modifier.width(12.dp))

		Crumb(crumb = "/") { onAction(chapterNamePath.size) }
		chapterNamePath.forEachIndexed { index, crumb ->
			Crumb(crumb = crumb) { onAction(chapterNamePath.size - index - 1) }
		}
	}
}

@Composable
private fun Crumb(
	crumb: String,
	onAction: () -> Unit
) {
	var showCrumb by remember { mutableStateOf(false) }
	LaunchedEffect(key1 = null) { showCrumb = true }

	AnimatedVisibility(
		visible = showCrumb,
		enter = slideInHorizontally(animationSpec = tween(durationMillis = 400)) +
				expandHorizontally(animationSpec = tween(durationMillis = 400))
	) {
		Row(
			modifier = Modifier.height(24.dp),
			verticalAlignment = Alignment.CenterVertically
		) {
			Box(
				modifier = Modifier
					.fillMaxHeight()
					.clip(RoundedCornerShape(50))
					.background(MaterialTheme.colorScheme.surface.tone(isSystemInDarkTheme(), 2))
					.clickable { onAction() },
				contentAlignment = Alignment.Center
			) {
				Text(
					text = crumb,
					style = MaterialTheme.typography.bodyLarge,
					color = MaterialTheme.colorScheme.onSurface.tone(isSystemInDarkTheme(), 2),
					textAlign = TextAlign.Center,
					modifier = Modifier.padding(16.dp, 0.dp)
				)
			}

			Icon(
				painter = painterResource(id = R.drawable.ic_chevron_right),
				contentDescription = null,
				modifier = Modifier.requiredSize(16.dp),
				tint = MaterialTheme.colorScheme.onSurface
			)
		}
	}
}
