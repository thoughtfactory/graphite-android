package com.syncodec.graphite.presentation.attachment.composable.bar

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.syncodec.graphite.presentation.common.button.BackButton


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar() {
	TopAppBar(
		navigationIcon = {
			BackButton()
		},
		title = {
			Text(
				text = "Attachment",
			)
		},
		modifier = Modifier.fillMaxWidth(),
		colors = TopAppBarDefaults.topAppBarColors(
			containerColor = MaterialTheme.colorScheme.background,
			navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
			actionIconContentColor = MaterialTheme.colorScheme.onBackground,
		)
	)
}
