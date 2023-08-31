package com.syncodec.graphite.presentation.explorer.atlas.composable

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.google.android.gms.maps.model.LatLngBounds
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.ChapterObjectLite
import com.syncodec.graphite.di.model.NoteObjectLite
import com.syncodec.graphite.presentation.explorer.composable.ExplorerScreen
import io.realm.kotlin.types.RealmUUID


@Preview
@Composable
fun AtlasScreen(
	chapterObject: ChapterObjectLite? = null,
	chapterFilteredNoteList: List<NoteObjectLite> = listOf(),
	contextFilteredNoteList: List<NoteObjectLite> = listOf(),
	onUpdateCameraBound: (LatLngBounds?) -> Unit = {},
	onExploreChapter: (RealmUUID?) -> Unit = {},
) {
	ExplorerScreen(
		screenTitle = stringResource(id = R.string.atlas),
		bottomSheetTitle = stringResource(id = R.string.in_visible_region),
		currentChapter = chapterObject,
		noteList = contextFilteredNoteList,
		onExploreChapter = onExploreChapter,
	) {
		AtlasView2(
			noteList = chapterFilteredNoteList,
			modifier = Modifier.fillMaxSize(),
			onClickNote = {},
			onUpdateCameraBound = onUpdateCameraBound,
		)
	}
}
