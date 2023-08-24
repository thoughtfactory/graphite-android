package com.syncodec.graphite.presentation.common.dialog.whereDialog.composable.bar

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.ChapterObjectLite
import com.syncodec.graphite.presentation.common.button.GenericButton
import com.syncodec.graphite.presentation.notebook.screen.composable.bar.Navigator
import io.realm.kotlin.types.RealmUUID


@Preview
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
	@PreviewParameter(ChapterObjectLiteListPreviewParameter::class) chapterPath : List<ChapterObjectLite>,
	onExploreChapter : (RealmUUID?) -> Unit = {},
	onDismiss : () -> Unit = {},
) {
	Column(
		modifier = Modifier.fillMaxWidth()
	) {
		TopAppBar(
			navigationIcon = {
				GenericButton(
					icon = R.drawable.ic_close,
					onClick = onDismiss
				)
			},
			title = {
				Text(
					text = "Select Chapter",
					fontWeight = FontWeight.Bold
				)
			},
			colors = TopAppBarDefaults.topAppBarColors(
				containerColor = MaterialTheme.colorScheme.background,
				titleContentColor = MaterialTheme.colorScheme.onBackground,
			)
		)

		Navigator(
			chapterPath = chapterPath.reversed(),
			defaultChapterId = null,
			showRoot = true,
			onClickNavigatorChapter = onExploreChapter,
		)
	}
}

private class ChapterObjectLiteListPreviewParameter : PreviewParameterProvider<List<ChapterObjectLite>> {
	override val values = sequenceOf(
		listOf(
			ChapterObjectLite.getRandomInstance(),
			ChapterObjectLite.getRandomInstance(),
			ChapterObjectLite.getRandomInstance(),
			ChapterObjectLite.getRandomInstance(),
		)
	)
}
