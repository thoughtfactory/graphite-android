package com.syncodec.graphite.presentation.notebook

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.syncodec.graphite.presentation.notebook.composable.NotebookScreen
import com.syncodec.graphite.presentation.notebook.composable.NotebookScreenViewModel2
import com.syncodec.graphite.presentation.base.BaseComposable
import com.syncodec.graphite.utils.Extra
import io.realm.kotlin.types.RealmUUID
import org.koin.androidx.viewmodel.ext.android.viewModel


class NotebookActivity : ComponentActivity() {

	private val viewModel: NotebookScreenViewModel2 by viewModel()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		val hasChapterId = intent.hasExtra(Extra.Companion.Extra.ChapterId.name)
		if (hasChapterId) {
			val chapterId = intent.getByteArrayExtra(Extra.Companion.Extra.ChapterId.name)?.let { RealmUUID.from(it) }

			chapterId?.let {
				viewModel.loadChapter(it)
			} ?: finish()
		} else {
			finish()
		}

		setContent {
			BaseComposable {

				val chapterObject by viewModel.currentChapter.collectAsState()
				val chapterList by viewModel.chapterList.collectAsState()
				val noteGroupList by viewModel.noteGroupList.collectAsState()
				val tagList by viewModel.tagList.collectAsState()
				val chapterNoteItemCount by viewModel.chapterNoteItemCount.collectAsState()
				val chapterChapterItemCount by viewModel.chapterChapterItemCount.collectAsState()
				val chapterPath by viewModel.currentChapterPath.collectAsState()
				val defaultChapterId by viewModel.defaultChapterId.collectAsState()

				NotebookScreen(
					chapterObject = chapterObject,
					chapterList = chapterList,
					noteGroupList = noteGroupList,
					tagList = tagList,
					chapterNoteItemCount = chapterNoteItemCount,
					chapterChapterItemCount = chapterChapterItemCount,
					chapterPath = chapterPath,
					defaultChapterId = defaultChapterId,
					onLoadChapter = viewModel::loadChapter,
					onClickFavourite = viewModel::toggleFavourite,
					onClickMultiFavourite = viewModel::toggleFavourite,
					onClickLock = viewModel::toggleLock,
					onClickMultiLock = viewModel::toggleLock,
					onClickSetDefault = viewModel::setDefaultChapter,
					putChapter = viewModel::putChapter,
					onConfirmDelete = viewModel::delete
				)
			}
		}
	}
}
