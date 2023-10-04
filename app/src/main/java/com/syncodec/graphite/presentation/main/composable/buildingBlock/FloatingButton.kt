package com.syncodec.graphite.presentation.main.composable.buildingBlock

import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R


@Preview
@Composable
fun NoteFloatingActionButton(
	isExpanded: Boolean = true,
	onClick: () -> Unit = {},
) {
	ExtendedFloatingActionButton(
		text = { Text(text = stringResource(id = R.string.new_note)) },
		icon = {
			Icon(
				painter = painterResource(id = R.drawable.ic_fa_pen),
				contentDescription = stringResource(id = R.string.new_note),
				modifier = Modifier.requiredSize(16.dp)
			)
		},
		expanded = isExpanded,
		onClick = onClick
	)
}

@Preview
@Composable
fun BucketFloatingActionButton(
	isExpanded: Boolean = true,
	onClick: () -> Unit = {},
) {
	ExtendedFloatingActionButton(
		text = { Text(text = stringResource(id = R.string.new_list)) },
		icon = {
			Icon(
				painter = painterResource(id = R.drawable.ic_fa_bucket_list),
				contentDescription = stringResource(id = R.string.new_list),
				modifier = Modifier.requiredSize(16.dp)
			)
		},
		expanded = isExpanded,
		onClick = onClick
	)
}

@Preview
@Composable
fun NotebookFloatingActionButton(
	isExpanded: Boolean = true,
	onClick: () -> Unit = {},
) {
	ExtendedFloatingActionButton(
		text = { Text(text = stringResource(id = R.string.new_notebook)) },
		icon = {
			Icon(
				painter = painterResource(id = R.drawable.ic_fa_notebook),
				contentDescription = stringResource(id = R.string.new_notebook),
				modifier = Modifier.requiredSize(16.dp)
			)
		},
		expanded = isExpanded,
		onClick = onClick
	)
}
