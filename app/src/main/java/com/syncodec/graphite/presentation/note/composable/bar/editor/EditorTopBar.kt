package com.syncodec.graphite.presentation.note.composable.bar.editor

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.local.ChapterObjectLite
import com.syncodec.graphite.presentation.common.button.BackButton
import com.syncodec.graphite.presentation.note.composable.bar.buildingBlock.ChapterSelector


@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun EditorTopBar(
	parentChapter: ChapterObjectLite? = ChapterObjectLite.getRandomInstance(),
	onClickSave: () -> Unit = {},
	onClickBack: () -> Unit = {},
	onClickSelectChapter: () -> Unit = {},
) {
	CenterAlignedTopAppBar(
		navigationIcon = {
			BackButton(onClick = onClickBack)
		},
		title = { ChapterSelector(parentChapter = parentChapter, onClickSelectChapter = onClickSelectChapter) },
		actions = {
			Button(
				shape = MaterialTheme.shapes.medium,
				onClick = onClickSave,
			) {
				Text(text = stringResource(id = R.string.save))
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

