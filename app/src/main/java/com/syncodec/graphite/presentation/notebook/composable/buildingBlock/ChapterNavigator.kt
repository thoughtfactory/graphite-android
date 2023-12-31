package com.syncodec.graphite.presentation.notebook.composable.buildingBlock

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.local.ChapterObjectLite
import com.syncodec.graphite.presentation.common.button.GenericButton
import com.syncodec.graphite.presentation.common.button.GenericButtonDefaults
import com.syncodec.graphite.presentation.common.component.chapter.Navigator
import io.realm.kotlin.types.RealmUUID


@Preview
@Composable
fun ChapterNavigator(
	defaultChapterId: RealmUUID? = null,
	chapterPath: List<ChapterObjectLite> = listOf(),
	isChapterListVisible: Boolean = false,
	onLoadChapter: (RealmUUID) -> Unit = {},
	onToggleChapterList: () -> Unit = {},
) {
	Column(
		modifier = Modifier
			.fillMaxWidth()
			.background(MaterialTheme.colorScheme.background)
	) {
		Row {
			Navigator(
				chapterPath = chapterPath.reversed(),
				defaultChapterId = defaultChapterId,
				showRoot = false,
				isVisible = true,
				onClickNavigatorChapter = { it?.let(onLoadChapter) },
				modifier = Modifier.weight(1f)
			)
			Spacer(modifier = Modifier.width(12.dp))

			GenericButton(
				icon = if (isChapterListVisible) R.drawable.ic_fa_minus else R.drawable.ic_fa_arrow_down_duotone,
				colors = GenericButtonDefaults.outlineButtonColors(),
				onClick = onToggleChapterList
			)

			Spacer(modifier = Modifier.width(12.dp))
		}
		Spacer(modifier = Modifier.height(12.dp))
	}
}
