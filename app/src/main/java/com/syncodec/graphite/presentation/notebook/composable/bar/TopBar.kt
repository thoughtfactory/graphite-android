package com.syncodec.graphite.presentation.notebook.composable.bar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.BucketType
import com.syncodec.graphite.presentation.bucket.BucketActivity
import com.syncodec.graphite.presentation.custom.button.MenuButton
import com.syncodec.graphite.presentation.notebook.NotebookActivity


@Composable
fun TopBar(
	title: String,
	onClickMenu: () -> Unit
) {
	Column(
		modifier = Modifier
			.fillMaxWidth()
			.background(MaterialTheme.colorScheme.background)
	) {
		Bar(
			title = title,
			onClickMenu = onClickMenu
		)
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Bar(
	title: String,
	onClickMenu: () -> Unit,
) {
	val activity: NotebookActivity = LocalContext.current as NotebookActivity

	SmallTopAppBar(
		navigationIcon = {
			MenuButton(
				icon = R.drawable.ic_back,
				contentDescription = "Back"
			) { activity.finish() }
		},
		title = {
			Text(
				text = title,
				color = MaterialTheme.colorScheme.onSurface,
			)
		},
		actions = {
			MenuButton(
				icon = R.drawable.ic_menu,
				contentDescription = "Menu",
				onClick = onClickMenu
			)
		},
		colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
	)
}
