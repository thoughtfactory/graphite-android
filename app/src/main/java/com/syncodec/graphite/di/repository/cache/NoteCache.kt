package com.syncodec.graphite.di.repository.cache

import android.text.Html
import com.syncodec.graphite.BuildConfig
import com.syncodec.graphite.di.model.NoteObject
import io.realm.kotlin.types.RealmUUID
import java.util.concurrent.ConcurrentHashMap


object NoteCache {

	private val noteContentThumbnailMap: ConcurrentHashMap<RealmUUID, Pair<Int, String?>> = ConcurrentHashMap()

	fun getOrSetCache(noteObject: NoteObject): String? {

		val cachedNoteThumbnail = noteContentThumbnailMap[noteObject.id]
		return if (cachedNoteThumbnail == null || cachedNoteThumbnail.first != noteObject.hashCode()) {
			try {
				val newNoteThumbnail = Html.fromHtml(noteObject.content2 ?: "", Html.FROM_HTML_MODE_LEGACY)?.toString()?.take(CONTENT_THUMBNAIL_SIZE)?.replace("\n\n", "\n")?.trimEnd { it == '\n' }
				noteContentThumbnailMap[noteObject.id] = Pair(this.hashCode(), newNoteThumbnail)
				newNoteThumbnail
			} catch (e: Exception) {
				if (BuildConfig.DEBUG) e.printStackTrace()
				null
			}
		} else {
			cachedNoteThumbnail.second
		}
	}

	private const val CONTENT_THUMBNAIL_SIZE = 512
}
