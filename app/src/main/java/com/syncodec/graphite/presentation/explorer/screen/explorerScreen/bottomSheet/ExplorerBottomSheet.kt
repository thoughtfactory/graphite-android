package com.syncodec.graphite.presentation.explorer.screen.explorerScreen.bottomSheet

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.BottomSheetScaffoldState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.di.model.NoteObjectLite
import com.syncodec.graphite.di.model.TagObject
import com.syncodec.graphite.presentation.notebook.screen.buildingBlock.NoteCardDefaults
import com.syncodec.graphite.presentation.notebook.screen.buildingBlock.noteList
import io.realm.kotlin.types.RealmUUID


@OptIn(ExperimentalMaterialApi::class)
@Preview
@Composable
fun ExplorerBottomSheet(
	scaffoldState : BottomSheetScaffoldState = rememberBottomSheetScaffoldState(),
	title : String = "Title",
	headerBackgroundColor : Color? = null,
	noteList : List<NoteObjectLite> = listOf(),
	tagList : List<TagObject> = listOf(),
	selectedIdList : List<RealmUUID> = listOf(),
	onClickNote : (NoteObjectLite) -> Unit = {},
	onLongClickNote : (NoteObjectLite) -> Unit = {},
) {

	Column(
		modifier = Modifier.fillMaxWidth()
	) {
		val noteCardColors =
			if (isSystemInDarkTheme()) NoteCardDefaults.noteCardColorsOnSurface(backgroundColor = headerBackgroundColor ?: MaterialTheme.colorScheme.surface)
			else NoteCardDefaults.noteCardColors(backgroundColor = headerBackgroundColor ?: MaterialTheme.colorScheme.surface)

		LazyColumn(
			modifier = Modifier
		) {
			noteList(
				noteList = noteList,
				tagList = tagList,
//				selectedIdList = selectedIdList,
				headerTitle = title,
				headerMinHeight = 48.dp,
				headerBackgroundColor = headerBackgroundColor,
				noteCardColors = noteCardColors,
				headerEnabled = false,
				onClick = onClickNote,
				onLongClick = onLongClickNote,
			)

			if (noteList.isNotEmpty()) item { Spacer(modifier = Modifier.height(32.dp)) }
		}
	}
}
