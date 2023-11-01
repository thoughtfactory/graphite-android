package com.syncodec.graphite.di.repository.cache

import android.text.Html
import com.syncodec.graphite.BuildConfig
import com.syncodec.graphite.di.model.local.KitKatContent
import com.syncodec.graphite.di.model.local.NoteObject
import io.realm.kotlin.types.RealmUUID
import java.util.concurrent.ConcurrentHashMap


object NoteCache {

	private val noteContentThumbnailMap: ConcurrentHashMap<RealmUUID, Pair<Int, String?>> = ConcurrentHashMap()

	fun getOrSetCache(noteObject: NoteObject): String? {

		val cachedNoteThumbnail = noteContentThumbnailMap[noteObject.id]
		return if (cachedNoteThumbnail == null || cachedNoteThumbnail.first != noteObject.hashCode()) {
			try {
				val newNoteThumbnailJson = if (noteObject.content?.startsWith("{") == true) getThumbnailFromJson(noteObject.content) else null
				val newNoteThumbnail = (newNoteThumbnailJson ?: getThumbnailFromHtml(noteObject.content))?.take(CONTENT_THUMBNAIL_SIZE)
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

	private fun getThumbnailFromHtml(content : String?) : String? {
		return try {
			val newNoteThumbnailHtml = Html.fromHtml(content ?: "", Html.FROM_HTML_MODE_LEGACY)?.toString()?.replace("\n\n", "\n")?.trimEnd { it == '\n' }
			if (newNoteThumbnailHtml.isNullOrEmpty()) null else newNoteThumbnailHtml
		} catch (e: Exception) {
			if (BuildConfig.DEBUG) e.printStackTrace()
			null
		}
	}

	private fun getThumbnailFromJson(content : String?) : String? {
		return try {
			val newNoteThumbnailJson = KitKatContent.fromString(content ?: "")?.toTxt()?.replace("\n\n", "\n")?.trimEnd { it == '\n' }
			if (newNoteThumbnailJson.isNullOrEmpty()) null else newNoteThumbnailJson
		} catch (e: Exception) {
			if (BuildConfig.DEBUG) e.printStackTrace()
			null
		}
	}

	private const val CONTENT_THUMBNAIL_SIZE = 512
}
