package com.syncodec.graphite.di.repository.cache

import io.realm.kotlin.types.RealmUUID


object NoteCache {

	val noteContentThumbnailMap : MutableMap<RealmUUID, Pair<Int, String?>> = mutableMapOf()

}
