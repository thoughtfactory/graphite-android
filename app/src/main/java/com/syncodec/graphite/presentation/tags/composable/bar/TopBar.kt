package com.syncodec.graphite.presentation.tags.composable.bar

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.syncodec.graphite.presentation.common.button.BackButton


@Preview
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar() {
	TopAppBar(
		navigationIcon = { BackButton() },
		title = { Text(text = "Manage Tags",) },
		colors = TopAppBarDefaults.topAppBarColors(
			containerColor = MaterialTheme.colorScheme.background,
			titleContentColor = MaterialTheme.colorScheme.onBackground,
		)
	)
}
