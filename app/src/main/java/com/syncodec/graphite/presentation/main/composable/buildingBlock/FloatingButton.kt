package com.syncodec.graphite.presentation.main.composable.buildingBlock

import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import com.syncodec.graphite.R


@Composable
fun NoteFloatingActionButton(
	isExpanded: Boolean,
	onClick: () -> Unit,
) {
	ExtendedFloatingActionButton(
		text = {
			Text(
				text = "New Note",
				style = MaterialTheme.typography.bodyMedium
			)
		},
		icon = {
			Icon(
				painter = painterResource(id = R.drawable.ic_pencil),
				contentDescription = "New note",
			)
		},
		expanded = isExpanded,
		onClick = onClick
	)
}

@Composable
fun BucketFloatingActionButton(
	isExpanded: Boolean,
	onClick: () -> Unit,
) {
	ExtendedFloatingActionButton(
		text = {
			Text(
				text = "New Bucket",
				style = MaterialTheme.typography.bodyMedium
			)
		},
		icon = {
			Icon(
				painter = painterResource(id = R.drawable.ic_bucket),
				contentDescription = "New bucket",
			)
		},
		expanded = isExpanded,
		onClick = onClick
	)
}

@Composable
fun NotebookFloatingActionButton(
	isExpanded: Boolean,
	onClick: () -> Unit,
) {
	ExtendedFloatingActionButton(
		text = {
			Text(
				text = "New Notebook",
				style = MaterialTheme.typography.bodyMedium
			)
		},
		icon = {
			Icon(
				painter = painterResource(id = R.drawable.ic_notebook),
				contentDescription = "New notebook",
			)
		},
		expanded = isExpanded,
		onClick = onClick
	)
}
