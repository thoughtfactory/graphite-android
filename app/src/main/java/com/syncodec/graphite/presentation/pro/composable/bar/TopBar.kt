package com.syncodec.graphite.presentation.pro.composable.bar

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.button.GenericButton


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar() {
	val backPressedDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

	TopAppBar(
		navigationIcon = { GenericButton(icon = R.drawable.ic_close) { backPressedDispatcher?.onBackPressed() } },
		title = {},
		colors = TopAppBarDefaults.topAppBarColors(
			containerColor = MaterialTheme.colorScheme.background,
			navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
			titleContentColor = MaterialTheme.colorScheme.onBackground,
			actionIconContentColor = MaterialTheme.colorScheme.onBackground
		)
	)
}
