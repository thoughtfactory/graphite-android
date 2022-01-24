package com.syncodec.momento.database.notebook

data class Chapter(
	val chapterList: List<Chapter>,
	val noteList: List<Note>,
) {
}
