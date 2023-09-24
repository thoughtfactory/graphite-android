package com.syncodec.graphite.di.repository.cache

import android.text.Html
import android.text.SpannableStringBuilder
import android.text.Spanned
import com.syncodec.graphite.BuildConfig
import com.syncodec.graphite.di.model.NoteObject
import io.realm.kotlin.types.RealmUUID
import kotlinx.serialization.json.Json


object NoteCache {

	private val json = Json { ignoreUnknownKeys = true }
	private val noteContentThumbnailMap: MutableMap<RealmUUID, Pair<Int, String?>> = mutableMapOf()

	fun getOrSetCache(noteObject: NoteObject): String? {

		val cachedNoteThumbnail = noteContentThumbnailMap[noteObject.id]
		return if (cachedNoteThumbnail == null || cachedNoteThumbnail.first != noteObject.hashCode()) {
			try {
				val newNoteThumbnail = Html.fromHtml(noteObject.content2 ?: "", Html.FROM_HTML_MODE_LEGACY)?.toString()?.take(256)?.replace("\n\n", "\n")?.trimEnd { it == '\n' }
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
}
