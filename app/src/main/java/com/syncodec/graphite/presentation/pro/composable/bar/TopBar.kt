package com.syncodec.graphite.presentation.pro.composable.bar

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.button.MenuButton
import com.syncodec.graphite.presentation.pro.ProActivity


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar() {

	val onBackPressed = ProActivity.onBackPressed.current

	TopAppBar(
		navigationIcon = {
			MenuButton(
				icon = R.drawable.ic_close,
				tint = MaterialTheme.colorScheme.onBackground,
				onClick = onBackPressed
			)
		},
		title = {

		},
		colors = TopAppBarDefaults.smallTopAppBarColors(
			containerColor = MaterialTheme.colorScheme.background,
			navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
			titleContentColor = MaterialTheme.colorScheme.onBackground,
			actionIconContentColor = MaterialTheme.colorScheme.onBackground
		)
	)

}
