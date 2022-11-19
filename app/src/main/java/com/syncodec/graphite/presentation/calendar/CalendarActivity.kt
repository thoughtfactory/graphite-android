package com.syncodec.graphite.presentation.calendar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.syncodec.graphite.di.model.NoteObjectLite
import com.syncodec.graphite.presentation.atlas.AtlasActivity
import com.syncodec.graphite.presentation.atlas.composable.screen.AtlasScreen
import com.syncodec.graphite.presentation.attachment.composable.screen.AttachmentScreen
import com.syncodec.graphite.presentation.calendar.composable.screen.CalendarScreen
import com.syncodec.graphite.presentation.ui.BaseContent
import com.syncodec.graphite.utils.Extra
import dagger.hilt.android.AndroidEntryPoint
import io.realm.kotlin.types.RealmUUID


@AndroidEntryPoint
class CalendarActivity : ComponentActivity() {

	private val viewModel by viewModels<CalendarViewModel>()

	override fun onCreate(savedInstanceState : Bundle?) {
		super.onCreate(savedInstanceState)

		val showAll = intent.getBooleanExtra(Extra.Companion.Constant.SHOW_ALL.name, false)
		val hasChapterId = intent.hasExtra(Extra.Companion.Constant.CHAPTER_ID.name)

		if (showAll) {
			viewModel.loadAllData()
		} else if (hasChapterId) {
			val chapterId = intent.getByteArrayExtra(Extra.Companion.Constant.CHAPTER_ID.name)?.let { RealmUUID.from(it) }
			if (chapterId != null) {
				viewModel.loadDataFromChapter(chapterId)
			} else {
				finish()
			}
		} else {
			finish()
		}

		setContent {
			BaseContent {
				val systemUiController = rememberSystemUiController()
				systemUiController.setStatusBarColor(if (isSystemInDarkTheme()) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onBackground)
				systemUiController.setNavigationBarColor(if (isSystemInDarkTheme()) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onBackground)

				val noteList = viewModel.noteList

				CompositionLocalProvider(
					LocalNoteList provides noteList
				) {
					CalendarScreen()
				}
			}
		}
	}

	companion object {
		val LocalNoteList = compositionLocalOf<SnapshotStateList<NoteObjectLite>> { mutableStateListOf() }
	}
}
