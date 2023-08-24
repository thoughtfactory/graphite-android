package com.syncodec.graphite.presentation.tags.composable.bar

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.button.GenericButton


@Preview
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar() {

	val onBackPressedDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

	TopAppBar(
		navigationIcon = {
			GenericButton(
				icon = R.drawable.ic_back,
				onClick = { onBackPressedDispatcher?.onBackPressed() }
			)
		},
		title = {
			Text(
				text = "Manage Tags",
				color = MaterialTheme.colorScheme.onBackground,
			)
		},
		colors = TopAppBarDefaults.topAppBarColors(
			containerColor = MaterialTheme.colorScheme.background,
			titleContentColor = MaterialTheme.colorScheme.onBackground,
		)
	)
}
