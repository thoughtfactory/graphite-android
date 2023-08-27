package com.syncodec.graphite.presentation.common.dialog.whereDialog2.bar

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.ChapterObjectLite
import com.syncodec.graphite.presentation.common.button.CancelButton
import com.syncodec.graphite.presentation.notebook.screen.composable.bar.Navigator
import io.realm.kotlin.types.RealmUUID


@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun TopBar(
	chapterPath : List<ChapterObjectLite> = listOf(),
	onClickChapter : (RealmUUID?) -> Unit = {},
	onClickCancel: () -> Unit = {}
) {
	Column {
		TopAppBar(
			navigationIcon = { CancelButton(onClick = onClickCancel) },
			title = { Text(text = stringResource(id = R.string.select_chapter)) },
			colors = TopAppBarDefaults.topAppBarColors(
				containerColor = MaterialTheme.colorScheme.background,
				navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
				titleContentColor = MaterialTheme.colorScheme.onSurface,
				actionIconContentColor = MaterialTheme.colorScheme.onSurface,
			)
		)

		Navigator(
			chapterPath = chapterPath.reversed(),
			defaultChapterId = null,
			showRoot = true,
			onClickNavigatorChapter = onClickChapter,
		)
	}
}
