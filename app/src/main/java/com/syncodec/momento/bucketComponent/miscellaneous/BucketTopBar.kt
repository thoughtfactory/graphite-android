package com.syncodec.momento.bucketComponent.miscellaneous

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.syncodec.momento.bucketComponent.BucketViewModel
import compose.icons.TablerIcons
import compose.icons.tablericons.ChevronLeft
import compose.icons.tablericons.Dots
import compose.icons.tablericons.InfoCircle

@Composable
fun BucketTopBar() {
	val activity = LocalContext.current as? Activity

	val viewModel: BucketViewModel = viewModel()
	val status: Int by viewModel.status

	Box(
		modifier = Modifier
			.fillMaxWidth()
			.height(64.dp)
			.background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = (1 - viewModel.bucketActivityState.collapsingToolbarScaffoldState.toolbarState.progress)))
	) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier
				.fillMaxWidth()
				.fillMaxHeight()
				.padding(8.dp)
		) {
			IconButton(
				onClick = { activity?.finish() },
			) {
				Icon(
					imageVector = TablerIcons.ChevronLeft,
					contentDescription = "Back",
					tint = MaterialTheme.colorScheme.onPrimaryContainer,
				)
			}

			Text(
				text = if (status != 1) "" else viewModel.bucket.title,
				color = MaterialTheme.colorScheme.onPrimaryContainer,
				style = MaterialTheme.typography.titleMedium,
				modifier = Modifier
					.padding(12.dp, 8.dp, 8.dp, 8.dp)
					.graphicsLayer {
						this.alpha = 1 - viewModel.bucketActivityState.collapsingToolbarScaffoldState.toolbarState.progress
					},
			)

			Spacer(modifier = Modifier.weight(1f))

			IconButton(
				onClick = {
				},
			) {
				Icon(
					imageVector = TablerIcons.InfoCircle,
					contentDescription = "Metadata",
					tint = MaterialTheme.colorScheme.onPrimaryContainer,
				)
			}

			IconButton(
				onClick = {
				},
			) {
				Icon(
					imageVector = TablerIcons.Dots,
					contentDescription = "Menu",
					tint = MaterialTheme.colorScheme.onPrimaryContainer,
				)
			}
		}
	}

	Row(
		verticalAlignment = Alignment.CenterVertically,
		modifier = Modifier
			.fillMaxWidth()
			.height(64.dp)
			.padding(8.dp)
			.zIndex(1f)
	) {
		IconButton(
			onClick = { activity?.finish() },
		) {
			Icon(
				imageVector = TablerIcons.ChevronLeft,
				contentDescription = null,
				tint = MaterialTheme.colorScheme.onBackground,
			)
		}

	}
}
