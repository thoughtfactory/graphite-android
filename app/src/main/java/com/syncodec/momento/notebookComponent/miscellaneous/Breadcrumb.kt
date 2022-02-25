package com.syncodec.momento.notebookComponent.miscellaneous

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.syncodec.momento.notebookComponent.ViewModel
import compose.icons.TablerIcons
import compose.icons.tablericons.ChevronRight


@Composable
fun Breadcrumb(
	currentRoute: SnapshotStateList<String>,
) {
	val viewModel: ViewModel = viewModel()

	Row(
		modifier = Modifier
			.fillMaxWidth()
			.height(24.dp)
			.horizontalScroll(
				state = rememberScrollState()
			),
		verticalAlignment = Alignment.CenterVertically
	) {
		Spacer(modifier = Modifier.width(12.dp))

		Crumb(crumb = "/")
		currentRoute.forEach {
			Crumb(crumb = it)
		}
	}
}

@Composable
private fun Crumb(
	crumb: String
) {
	var showCrumb by remember { mutableStateOf(false) }
	LaunchedEffect(
		key1 = !showCrumb
	) {
		showCrumb = true
	}

	AnimatedVisibility(
		visible = showCrumb,
		enter = slideInHorizontally(
			animationSpec = tween(
				durationMillis = 400
			)
		) + expandHorizontally(
			animationSpec = tween(
				durationMillis = 400
			)
		)
	) {
		Row(
			modifier = Modifier
				.height(24.dp),
			verticalAlignment = Alignment.CenterVertically
		) {
			Box(
				modifier = Modifier
					.fillMaxHeight()
					.padding(0.dp, 0.dp)
					.clip(RoundedCornerShape(12.dp))
					.background(MaterialTheme.colorScheme.primary)
					.clickable {
//						viewModel.openNotebook()
					},
				contentAlignment = Alignment.Center
			) {
				Text(
					text = crumb,
					style = MaterialTheme.typography.titleSmall,
					color = MaterialTheme.colorScheme.onPrimary,
					textAlign = TextAlign.Center,
					modifier = Modifier
						.padding(16.dp, 0.dp)
				)
			}

			Icon(
				imageVector = TablerIcons.ChevronRight,
				contentDescription = null,
				modifier = Modifier
					.requiredSize(16.dp),
				tint = MaterialTheme.colorScheme.onPrimaryContainer
			)
		}
	}
}
