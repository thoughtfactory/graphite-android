package com.syncodec.graphite.presentation.atlas.composable.bar

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.button.MenuButton


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
	onBackPressed : () -> Unit,
) {
	TopAppBar(
		navigationIcon = {
			MenuButton(
				icon = R.drawable.ic_back,
				contentDescription = "Back",
				tint = MaterialTheme.colorScheme.onBackground,
				onClick = onBackPressed
			)
		},
		title = {
			Text(
				text = "Atlas",
				color = MaterialTheme.colorScheme.onBackground,
				fontWeight = FontWeight.Bold
			)
		},
		colors = TopAppBarDefaults.smallTopAppBarColors(
			containerColor = MaterialTheme.colorScheme.background,
			navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
			titleContentColor = MaterialTheme.colorScheme.onSurface,
			actionIconContentColor = MaterialTheme.colorScheme.onSurface,
		)
	)
}
