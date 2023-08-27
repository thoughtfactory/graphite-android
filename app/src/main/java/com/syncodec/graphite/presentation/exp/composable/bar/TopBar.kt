package com.syncodec.graphite.presentation.exp.composable.bar

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.syncodec.graphite.presentation.common.button.BackButton


@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun TopBar(
	title : String = "Explorer",
) {
	TopAppBar(
		navigationIcon = { BackButton() },
		title = { Text(text = title) },
		colors = TopAppBarDefaults.topAppBarColors(
			containerColor = MaterialTheme.colorScheme.background,
			navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
			titleContentColor = MaterialTheme.colorScheme.onBackground,
		)
	)
}
