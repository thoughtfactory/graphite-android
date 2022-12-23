package com.syncodec.graphite.presentation.dropbox.composable.bar

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.button.MenuButton


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
	onClickBack : () -> Unit,
) {

	TopAppBar(
		modifier = Modifier.fillMaxWidth(),
		navigationIcon = {
			MenuButton(
				icon = R.drawable.ic_back,
				tint = MaterialTheme.colorScheme.onBackground,
				onClick = onClickBack
			)
		},
		title = {
			Text(
				text = "Dropbox",
				color = MaterialTheme.colorScheme.onBackground,
				fontWeight = FontWeight.Bold,
				modifier = Modifier.graphicsLayer { this.alpha = alpha }
			)
		},
		colors = TopAppBarDefaults.topAppBarColors(
			containerColor = MaterialTheme.colorScheme.background,
			navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
			titleContentColor = MaterialTheme.colorScheme.onBackground,
		)
	)
}
