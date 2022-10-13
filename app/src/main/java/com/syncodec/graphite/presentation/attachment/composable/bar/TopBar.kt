package com.syncodec.graphite.presentation.attachment.composable.bar

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.button.MenuButton


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
	onClickBack: () -> Unit,
	onClickMenu: () -> Unit
) {
	TopAppBar(
		navigationIcon = {
			MenuButton(
				icon = R.drawable.ic_back,
				onClick = onClickBack,
			)
		},
		title = {
			Text(
				text = "Attachment",
				color = MaterialTheme.colorScheme.onBackground,
				fontWeight = FontWeight.Bold
			)
		},
		actions = {
			MenuButton(
				icon = R.drawable.ic_menu,
				onClick = onClickMenu,
			)
		},
		modifier = Modifier.fillMaxWidth(),
		colors = TopAppBarDefaults.smallTopAppBarColors(
			containerColor = MaterialTheme.colorScheme.background,
			navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
			actionIconContentColor = MaterialTheme.colorScheme.onBackground,
		)
	)
}
