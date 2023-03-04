package com.syncodec.graphite.presentation.notebook

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.syncodec.graphite.presentation.notebook.screen.NotebookScreen
import com.syncodec.graphite.presentation.notebook.screen.NotebookScreenViewModel
import com.syncodec.graphite.presentation.ui.BaseContent
import com.syncodec.graphite.utils.Extra
import io.realm.kotlin.types.RealmUUID
import org.koin.androidx.viewmodel.ext.android.viewModel


class NotebookActivity : ComponentActivity() {

	private val notebookScreenViewModel : NotebookScreenViewModel by viewModel()

	override fun onCreate(savedInstanceState : Bundle?) {
		super.onCreate(savedInstanceState)

		val hasChapterId = intent.hasExtra(Extra.Companion.Extra.ChapterId.name)
		if (hasChapterId) {
			val chapterId = intent.getByteArrayExtra(Extra.Companion.Extra.ChapterId.name)?.let { RealmUUID.from(it) }

			chapterId?.let {
				notebookScreenViewModel.loadChapter(it)
			} ?: finish()
		} else {
			finish()
		}

		setContent {
			BaseContent {
				NotebookScreen()
			}
		}
	}
}
