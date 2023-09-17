package com.syncodec.graphite.di.repository.cache

import com.syncodec.graphite.di.model.KitKatContent
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
				val newNoteThumbnail = noteObject.content?.let { json.decodeFromString<KitKatContent>(it) }?.toTxt()?.take(256)
				noteContentThumbnailMap[noteObject.id] = Pair(this.hashCode(), newNoteThumbnail)
				newNoteThumbnail
			} catch (_ : Exception) {
				null
			}
		} else {
			cachedNoteThumbnail.second
		}
	}
}
