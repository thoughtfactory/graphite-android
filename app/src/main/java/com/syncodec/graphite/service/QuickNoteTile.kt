package com.syncodec.graphite.service

import android.content.Intent
import android.service.quicksettings.TileService
import com.syncodec.graphite.di.repository.Repository2
import com.syncodec.graphite.di.repository.RepositoryState
import com.syncodec.graphite.presentation.note.NoteActivity
import com.syncodec.graphite.utils.Extra
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch


class QuickNoteTile: TileService() {

	override fun onClick() {
		super.onClick()

		quickNote()
	}

	private fun quickNote() {
		val repository2 = Repository2(this)
		repository2.isAuthenticated.value = true

		CoroutineScope(Dispatchers.Default).launch {
			repository2.repositoryState.collect {
				if (it == RepositoryState.SUCCESS) {
					repository2.getDefaultChapterId().collect {
						it?.let {
							Intent(this@QuickNoteTile, NoteActivity::class.java).apply {
								putExtra(Extra.Companion.Constant.IS_NEW.name, true)
								putExtra(Extra.Companion.Constant.CHAPTER_ID.name, it.bytes)
								putExtra(Extra.Companion.Constant.FILTER.name, Extra.Companion.Filter.READ_CHAPTER.name)

								flags = Intent.FLAG_ACTIVITY_NEW_TASK

								startActivityAndCollapse(this)
							}

							this.cancel()
						}
					}
				}
			}
		}
	}
}
