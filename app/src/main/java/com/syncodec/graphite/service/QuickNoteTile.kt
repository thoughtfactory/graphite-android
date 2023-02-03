package com.syncodec.graphite.service

import android.service.quicksettings.TileService


class QuickNoteTile: TileService() {

	override fun onClick() {
		super.onClick()

		quickNote()
	}

	private fun quickNote() {
//		val repository2 = Repository2(this)
//		repository2.isAuthenticated.value = true
//
//		CoroutineScope(Dispatchers.Default).launch {
//			repository2.repositoryState.collect {
//				if (it == RepositoryState.SUCCESS) {
//					repository2.getDefaultChapterId().collect {
//						it?.let {
//							Intent(this@QuickNoteTile, NoteActivity::class.java).apply {
//								putExtra(Extra.Companion.Constant.IsNew.name, true)
//								putExtra(Extra.Companion.Constant.CHAPTER_ID.name, it.bytes)
//								putExtra(Extra.Companion.Constant.Filter.name, Extra.Companion.Filter.READ_CHAPTER.name)
//
//								flags = Intent.FLAG_ACTIVITY_NEW_TASK
//
//								startActivityAndCollapse(this)
//							}
//
//							this.cancel()
//						}
//					}
//				}
//			}
//		}
	}
}
