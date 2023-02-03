package com.syncodec.graphite.presentation.main.composable.buildingBlock

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.ui.IconButtonSize


@Preview
@Composable
fun NoteFloatingActionButton(
	isExpanded: Boolean = true,
	onClick: () -> Unit = {},
) {
	ExtendedFloatingActionButton(
		text = {
			Text(text = "New Note")
		},
		icon = {
			Icon(
				painter = painterResource(id = R.drawable.ic_pencil),
				contentDescription = "New note",
				modifier = Modifier.requiredSize(IconButtonSize)
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
		text = {
			Text(text = "New List")
		},
		icon = {
			Icon(
				painter = painterResource(id = R.drawable.ic_bucket),
				contentDescription = "New bucket",
				modifier = Modifier.requiredSize(IconButtonSize)
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
		text = {
			Text(text = "New Notebook")
		},
		icon = {
			Icon(
				painter = painterResource(id = R.drawable.ic_notebook),
				contentDescription = "New notebook",
				modifier = Modifier.requiredSize(IconButtonSize)
			)
		},
		expanded = isExpanded,
		onClick = onClick
	)
}
