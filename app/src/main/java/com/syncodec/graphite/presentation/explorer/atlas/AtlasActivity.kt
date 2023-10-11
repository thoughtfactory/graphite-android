package com.syncodec.graphite.presentation.explorer.atlas

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.base.BaseComposable
import com.syncodec.graphite.presentation.explorer.atlas.composable.AtlasView2
import com.syncodec.graphite.presentation.explorer.composable.ExplorerScreen
import com.syncodec.graphite.presentation.note.NoteActivity
import com.syncodec.graphite.utils.Extra
import com.syncodec.graphite.utils.xor
import io.realm.kotlin.types.RealmUUID
import org.koin.androidx.viewmodel.ext.android.viewModel


class AtlasActivity : ComponentActivity() {

	val viewModel: AtlasViewModel by viewModel()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		val chapterId = intent.getByteArrayExtra(Extra.Companion.Extra.ChapterId.name)?.let { RealmUUID.from(it) }

		viewModel.loadChapter(chapterId = chapterId)

		setContent {
			BaseComposable {

				val chapterObject by viewModel.currentChapter.collectAsState()
				val chapterFilteredNoteList by viewModel.chapterFilteredNoteList.collectAsState()
				val contextFilteredNoteList by viewModel.contextFilteredNoteList.collectAsState()

				var isSelecting by rememberSaveable { mutableStateOf(false) }
				var selectedIdList: Set<RealmUUID> by remember { mutableStateOf(setOf()) }

				fun onSelect(id: RealmUUID) {
					isSelecting = true
					selectedIdList.toMutableSet().apply {
						xor(id)
						selectedIdList = toSet()
					}
				}

				fun onClickNote(id: RealmUUID) {
					if (isSelecting) {
						onSelect(id)
					} else {
						Intent(this, NoteActivity::class.java).apply {
							putExtra(Extra.Companion.Extra.IsNew.name, false)
							putExtra(Extra.Companion.Extra.NoteId.name, id.bytes)
							putExtra(Extra.Companion.Extra.Filter.name, Extra.Companion.Filter.SingleRead.name)
							startActivity(this)
						}
					}
				}

				BackHandler(enabled = isSelecting) { isSelecting = false; selectedIdList = setOf() }

				ExplorerScreen(
					screenTitle = stringResource(id = R.string.atlas),
					bottomSheetTitle = stringResource(id = R.string.in_visible_region),
					currentChapter = chapterObject,
					noteList = contextFilteredNoteList,
					isSelecting = isSelecting,
					selectedIdList = selectedIdList,
					onSelect = ::onSelect,
					onClickNote = ::onClickNote,
					onClickFavourite = { viewModel.onClickMultiFavourite(idList = selectedIdList) },
					onClickLock = { viewModel.onClickMultiLock(idList = selectedIdList) },
					onUnSelectAll = { selectedIdList = setOf() },
					onExploreChapter = viewModel::loadChapter,
					onConfirmDelete = { viewModel.delete(idList = selectedIdList.toSet()); selectedIdList = setOf() },
				) {
					AtlasView2(
						noteList = chapterFilteredNoteList,
						modifier = Modifier.fillMaxSize(),
						onUpdateCameraBound = viewModel::onUpdateCameraBound,
					)
				}
			}
		}
	}
}
