package com.syncodec.graphite

import com.syncodec.graphite.di.model.serializer.RealmUUIDNullableSerializer
import com.syncodec.graphite.service.syncInator.SyncInatorService
import com.syncodec.graphite.utils.filterNotNull
import io.realm.kotlin.types.RealmUUID
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.json.Json
import org.junit.Test
import java.time.Instant


class DropboxSyncTest {
	private val json = Json {
		ignoreUnknownKeys = true
	}

	@Test
	fun test_metadataSerialization() {
//		val objectMetadata = SyncInatorService.Companion.ObjectMetadata(
//			modifiedTimestamp = Instant.now().toEpochMilli(),
//			hash = "hash",
//			isDeleted = false
//		)
//
//		val objectMetadataList : Map<RealmUUID, SyncInatorService.Companion.ObjectMetadata> = mapOf(
//			RealmUUID.random() to objectMetadata,
//		)
//
//		MapSerializer(RealmUUIDNullableSerializer, SyncInatorService.Companion.ObjectMetadata.serializer()).let {
//			json.encodeToString(it, objectMetadataList).let {
//				println(it)
//			}
//		}
	}

	@Test
	fun test_notNull() {
		val alphaMap = mapOf(
			"alpha" to "alpha",
			"beta" to "beta",
			null to "null",
			"null" to null,
		)

		alphaMap.filterNotNull().forEach {
			println(it)
		}
	}
}
