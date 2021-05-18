package com.syncodec.momento.notebookComponent.miscellaneous

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.syncodec.momento.konstant.Status
import com.syncodec.momento.notebookComponent.NotebookActivity
import compose.icons.TablerIcons
import compose.icons.tablericons.ArrowBack
import compose.icons.tablericons.Dots
import compose.icons.tablericons.InfoCircle

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TopBar(
	status: Status,
	title: String,
	chapterRoute: SnapshotStateList<String>,
	onClick: (NotebookActivity.Click, Int) -> Unit
) {
	val activity = LocalContext.current as? Activity

	Column(
		modifier = Modifier
			.fillMaxWidth()
			.background(MaterialTheme.colorScheme.secondaryContainer)
	) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier
				.fillMaxWidth()
				.padding(8.dp, 8.dp, 8.dp, 0.dp)
		) {
			IconButton(
				onClick = { activity?.finish() },
			) {
				Icon(
					imageVector = TablerIcons.ArrowBack,
					contentDescription = "Back",
					tint = MaterialTheme.colorScheme.onSecondaryContainer,
				)
			}

			Text(
				text = if (status != Status.LOADED) "" else title,
				color = MaterialTheme.colorScheme.onSecondaryContainer,
				style = MaterialTheme.typography.titleMedium,
				modifier = Modifier
					.padding(12.dp, 0.dp, 0.dp, 0.dp),
			)

			Spacer(modifier = Modifier.weight(1f))

			IconButton(
				onClick = {
				},
			) {
				Icon(
					imageVector = TablerIcons.InfoCircle,
					contentDescription = "Metadata",
					tint = MaterialTheme.colorScheme.onSecondaryContainer,
				)
			}

			IconButton(
				onClick = {},
			) {
				Icon(
					imageVector = TablerIcons.Dots,
					contentDescription = "Menu",
					tint = MaterialTheme.colorScheme.onSecondaryContainer,
				)
			}
		}

		AnimatedVisibility(visible = status == Status.LOADED) {
			Column(
				modifier = Modifier
					.fillMaxWidth()
			) {
				Breadcrumb(chapterRoute = chapterRoute) { onClick(NotebookActivity.Click.BREAD_CRUMB, it)}
				Spacer(modifier = Modifier.height(8.dp))
			}
		}
	}
}
