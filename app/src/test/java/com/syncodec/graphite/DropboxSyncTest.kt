@file:UseSerializers(RealmUUIDSerializer::class)
package com.syncodec.graphite

import com.syncodec.graphite.di.model.serializer.RealmUUIDSerializer
import com.syncodec.graphite.service.syncService.DropboxService
import io.realm.kotlin.types.RealmUUID
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.json.Json
import org.junit.Test


class DropboxSyncTest {
	private val json = Json {
		ignoreUnknownKeys = true
	}

	@Test
	fun test_metadataSerialization() {
		val objectMetadata = DropboxService.Companion.ObjectMetadata(
			modifiedTimestamp = System.currentTimeMillis(),
			hash = "hash",
			isDeleted = false
		)

		val objectMetadataList : Map<RealmUUID, DropboxService.Companion.ObjectMetadata> = mapOf(
			RealmUUID.random() to objectMetadata,
		)

		MapSerializer(RealmUUIDSerializer, DropboxService.Companion.ObjectMetadata.serializer()).let {
			json.encodeToString(it, objectMetadataList).let {
				println(it)
			}
		}
	}
}
