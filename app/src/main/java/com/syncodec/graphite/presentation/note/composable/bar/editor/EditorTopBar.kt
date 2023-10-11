package com.syncodec.graphite.presentation.note.composable.bar.editor

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.button.GenericButton


@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun EditorTopBar(
	onClickSave : () -> Unit = {},
	onClickBack : () -> Unit = {},
) {
	TopAppBar(
		navigationIcon = {
			GenericButton(
				icon = R.drawable.ic_fa_back,
				tooltip = "Back",
				onClick = onClickBack,
			)
		},
		title = {},
		actions = {
			Button(
				shape = MaterialTheme.shapes.medium,
				onClick = onClickSave,
			) {
				Text(text = "Save")
			}
			Spacer(modifier = Modifier.width(4.dp))
		},
		colors = TopAppBarDefaults.topAppBarColors(
			containerColor = MaterialTheme.colorScheme.background,
			navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
			actionIconContentColor = MaterialTheme.colorScheme.onBackground,
		)
	)
}

