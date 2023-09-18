package com.syncodec.graphite.di.model.exporter.schema3

import com.google.errorprone.annotations.Keep
import com.syncodec.graphite.di.model.BaseObject
import com.syncodec.graphite.di.model.BucketItemObject
import com.syncodec.graphite.di.model.BucketItemState
import com.syncodec.graphite.di.model.BucketObject
import com.syncodec.graphite.di.model.ChapterObject
import com.syncodec.graphite.di.model.LatLng
import com.syncodec.graphite.di.model.NoteObject
import com.syncodec.graphite.di.model.TagObject
import com.syncodec.graphite.di.model.serializer.RealmUUIDNullableSerializer
import com.syncodec.graphite.di.model.serializer.RealmUUIDSerializer
import io.realm.kotlin.types.RealmUUID
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer


@Keep
@Serializable
data class ExportBaseObject(
	@Serializable(with = RealmUUIDNullableSerializer::class)
	val defaultChapterId: RealmUUID? = null,
	val notebookIdOrderList: List<@Serializable(with = RealmUUIDSerializer::class) RealmUUID> = listOf(),
	val bucketIdOrderList: List<@Serializable(with = RealmUUIDSerializer::class) RealmUUID> = listOf(),
	val schemaVersion: Int = SCHEME_VERSION,
) {
	init {
		ListSerializer(RealmUUIDSerializer)
	}

	companion object {
		fun fromObject(inputObject: BaseObject) = ExportBaseObject(
			defaultChapterId = inputObject.defaultChapterId,
			notebookIdOrderList = inputObject.notebookIdOrderList,
			bucketIdOrderList = inputObject.bucketIdOrderList,
		)
	}
}

@Keep
@Serializable
data class ExportNoteObject(
	@Serializable(with = RealmUUIDSerializer::class)
	val id: RealmUUID,
	val createdTimestamp: Long,
	val modifiedTimestamp: Long,
	val userTimestamp: Long,
	val title: String? = null,
	val latLng: LatLng? = null,
	val address: String? = null,
	val content: String? = null,
	val isFavourite: Boolean = false,
	val isLocked: Boolean = false,
	@Serializable(with = RealmUUIDNullableSerializer::class)
	val parentId: RealmUUID? = null,
	val schemaVersion: Int = SCHEME_VERSION,
) {
	companion object {
		fun fromObject(inputObject: NoteObject) = ExportNoteObject(
			id = inputObject.id,
			createdTimestamp = inputObject.createdTimestamp,
			modifiedTimestamp = inputObject.modifiedTimestamp,
			userTimestamp = inputObject.userTimestamp,
			title = inputObject.title,
			latLng = inputObject.getLatLng(),
			address = inputObject.address,
			content = inputObject.content,
			isFavourite = inputObject.isFavourite,
			isLocked = inputObject.isLocked,
			parentId = inputObject.parentId,
		)
	}
}

@Keep
@Serializable
data class ExportChapterObject(
	@Serializable(with = RealmUUIDSerializer::class)
	val id: RealmUUID,
	val createdTimestamp: Long,
	val modifiedTimestamp: Long,
	val title: String? = null,
	val description: String? = null,
	val color: Int? = null,
	val thumbnail: String? = null,
	val isFavourite: Boolean = false,
	val isLocked: Boolean = false,
	@Serializable(with = RealmUUIDNullableSerializer::class)
	val parentId: RealmUUID? = null,
	val schemaVersion: Int = SCHEME_VERSION,
) {
	companion object {
		fun fromObject(inputObject: ChapterObject) = ExportChapterObject(
			id = inputObject.id,
			createdTimestamp = inputObject.createdTimestamp,
			modifiedTimestamp = inputObject.modifiedTimestamp,
			title = inputObject.title,
			description = inputObject.description,
			color = inputObject.color,
			thumbnail = inputObject.thumbnail,
			isFavourite = inputObject.isFavourite,
			isLocked = inputObject.isLocked,
			parentId = inputObject.parentId
		)
	}

}

@Keep
@Serializable
data class ExportBucketItemObject(
	@Serializable(with = RealmUUIDSerializer::class)
	val id: RealmUUID,
	val createdTimestamp: Long,
	val modifiedTimestamp: Long,
	val bucketType: String,
	val title: String? = null,
	val state: String = BucketItemState.ALPHA.name,
	val thumbnail: String? = null,
	val isFavourite: Boolean = false,
	val isLocked: Boolean = false,
	@Serializable(with = RealmUUIDNullableSerializer::class)
	val parentId: RealmUUID? = null,
	val key: String? = null,
	val data: String? = null,
	val schemaVersion: Int = SCHEME_VERSION,
) {
	companion object {
		fun fromObject(inputObject: BucketItemObject) = ExportBucketItemObject(
			id = inputObject.id,
			createdTimestamp = inputObject.createdTimestamp,
			modifiedTimestamp = inputObject.modifiedTimestamp,
			bucketType = inputObject.bucketType,
			title = inputObject.title,
			state = inputObject.state,
			thumbnail = inputObject.thumbnail,
			isFavourite = inputObject.isFavourite,
			isLocked = inputObject.isLocked,
			parentId = inputObject.parentId,
			key = inputObject.key,
			data = inputObject.data,
		)
	}
}

@Keep
@Serializable
data class ExportBucketObject(
	@Serializable(with = RealmUUIDSerializer::class)
	val id: RealmUUID,
	val createdTimestamp: Long,
	val modifiedTimestamp: Long,
	val bucketType: String,
	val title: String? = null,
	val description: String? = null,
	val isFavourite: Boolean = false,
	val isLocked: Boolean = false,
	val bucketItemOrderList: List<@Serializable(with = RealmUUIDSerializer::class) RealmUUID> = listOf(),
	val schemaVersion: Int = SCHEME_VERSION,
) {
	companion object {
		fun fromObject(inputObject: BucketObject) = ExportBucketObject(
			id = inputObject.id,
			createdTimestamp = inputObject.createdTimestamp,
			modifiedTimestamp = inputObject.modifiedTimestamp,
			bucketType = inputObject.bucketType,
			title = inputObject.title,
			description = inputObject.description,
			isFavourite = inputObject.isFavourite,
			isLocked = inputObject.isLocked,
			bucketItemOrderList = inputObject.bucketItemOrderList,
		)
	}
}

@Keep
@Serializable
data class ExportTagObject(
	@Serializable(with = RealmUUIDSerializer::class)
	val id: RealmUUID,
	val tag: String,
	val color: Int,
	val modifiedTimestamp: Long,
	val objectIdList: List<@Serializable(with = RealmUUIDSerializer::class) RealmUUID> = listOf(),
	val schemaVersion: Int = SCHEME_VERSION,
) {
	companion object {
		fun fromObject(inputObject: TagObject) = ExportTagObject(
			id = inputObject.id,
			tag = inputObject.tag,
			color = inputObject.color,
			modifiedTimestamp = inputObject.modifiedTimestamp,
			objectIdList = inputObject.objectIdList,
		)
	}
}

const val SCHEME_VERSION = 3
