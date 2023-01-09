package com.syncodec.graphite.presentation.tags.composable.bar

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.button.MenuButton


@Preview
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
	onBackPressed : () -> Unit = {}
) {
	TopAppBar(
		navigationIcon = {
			MenuButton(
				icon = R.drawable.ic_back,
				tint = MaterialTheme.colorScheme.onBackground,
				onClick = onBackPressed
			)
		},
		title = {
			Text(
				text = "Manage Tags",
				color = MaterialTheme.colorScheme.onBackground,
				fontWeight = FontWeight.Bold,
			)
		},
		colors = TopAppBarDefaults.topAppBarColors(
			containerColor = MaterialTheme.colorScheme.background,
			titleContentColor = MaterialTheme.colorScheme.onBackground,
		)
	)
}
