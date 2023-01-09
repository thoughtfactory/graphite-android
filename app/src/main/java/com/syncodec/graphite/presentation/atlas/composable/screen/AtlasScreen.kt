package com.syncodec.graphite.presentation.atlas.composable.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.syncodec.graphite.di.model.ChapterObjectLite
import com.syncodec.graphite.di.model.NoteObjectLite
import com.syncodec.graphite.presentation.atlas.composable.bar.BottomBar
import com.syncodec.graphite.presentation.atlas.composable.bar.TopBar
import com.syncodec.graphite.presentation.atlas.composable.dialog.AtlasDialog
import com.syncodec.graphite.utils.LocalVaultIsOpened
import io.realm.kotlin.types.RealmUUID


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AtlasScreen(
	parentChapter : ChapterObjectLite?,
	noteList : List<NoteObjectLite>,
	onClickNote: (RealmUUID) -> Unit,
	onLongClickNote : (RealmUUID) -> Unit,
	onBackPressed : () -> Unit,
) {
	val isVaultOpened = LocalVaultIsOpened.current

	Scaffold(
		topBar = { TopBar(onBackPressed = onBackPressed) },
		bottomBar = { BottomBar(parentChapter = parentChapter) }
	) {
		Box(
			modifier = Modifier
				.fillMaxSize()
				.padding(it)
		) {
			com.syncodec.graphite.presentation.main.composable.screen.AtlasScreen(
				currentRoute = com.syncodec.graphite.presentation.main.composable.bar.BottomNavigationItem.Atlas,
				noteList = noteList.filter { if (it.isLocked) isVaultOpened else true },
				onClickNote = onClickNote,
				onLongClickNote = onLongClickNote,
			)
		}
	}

	AtlasDialog(parentChapter = parentChapter)
}
