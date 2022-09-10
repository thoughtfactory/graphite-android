package com.syncodec.graphite.presentation.notebook

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.syncodec.graphite.presentation.notebook.composable.screen.NotebookScreen
import com.syncodec.graphite.presentation.ui.BaseContent
import com.syncodec.graphite.utils.Extra
import io.realm.kotlin.types.ObjectId


class NotebookActivity : ComponentActivity() {

	private val viewModel by viewModels<NotebookViewModel>()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		val hasChapterId = intent.hasExtra(Extra.Companion.Constant.CHAPTER_ID.name)
		if (hasChapterId) {
			val chapterId = intent.getStringExtra(Extra.Companion.Constant.CHAPTER_ID.name)?.let { ObjectId.from(it) }

			if (chapterId == null) {
//				ERROR ChapterId is null
				Log.d("NotebookActivity", "chapterId is null")
				finish()
			} else {
				viewModel.initNotebook(chapterId = chapterId)
			}
		} else {
//			ERROR ChapterId not found
			Log.i("NotebookActivity", "No chapter id found")
			finish()
		}

		setContent {
			BaseContent {
				val systemUiController = rememberSystemUiController()
				systemUiController.setStatusBarColor(MaterialTheme.colorScheme.primary)
				systemUiController.setNavigationBarColor(MaterialTheme.colorScheme.surface)

				NotebookScreen()
			}
		}
	}

}
