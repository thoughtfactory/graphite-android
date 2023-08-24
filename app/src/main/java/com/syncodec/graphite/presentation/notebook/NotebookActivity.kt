package com.syncodec.graphite.presentation.notebook

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.syncodec.graphite.presentation.notebook.composable.NotebookScreen2
import com.syncodec.graphite.presentation.notebook.composable.NotebookScreenViewModel2
import com.syncodec.graphite.presentation.ui.BaseContent
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
			BaseContent {

				val chapterObject by viewModel.currentChapter.collectAsState()
				val chapterList by viewModel.chapterList.collectAsState()
				val noteList by viewModel.noteList.collectAsState()
				val chapterPath by viewModel.currentChapterPath.collectAsState()
				val defaultChapterId by viewModel.defaultChapterId.collectAsState()

				NotebookScreen2(
					chapterObject = chapterObject,
					chapterList = chapterList,
					noteList = noteList,
					chapterPath = chapterPath,
					defaultChapterId = defaultChapterId,
					onLoadChapter = viewModel::loadChapter,
					putNotebook = viewModel::putNotebook
				)
			}
		}
	}
}
