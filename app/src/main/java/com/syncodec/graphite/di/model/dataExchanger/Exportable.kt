package com.syncodec.graphite.di.model.dataExchanger

import com.google.errorprone.annotations.Keep
import com.syncodec.graphite.di.model.local.BaseObject
import com.syncodec.graphite.di.model.local.BucketItemObject
import com.syncodec.graphite.di.model.local.BucketItemState
import com.syncodec.graphite.di.model.local.BucketObject
import com.syncodec.graphite.di.model.local.ChapterObject
import com.syncodec.graphite.di.model.local.LatLng
import com.syncodec.graphite.di.model.local.NoteObject
import com.syncodec.graphite.di.model.local.TagObject
import com.syncodec.graphite.di.model.serializer.RealmUUIDNullableSerializer
import com.syncodec.graphite.di.model.serializer.RealmUUIDSerializer
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.RealmUUID
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Keep
@Serializable
sealed class Exportable {

	abstract fun toObject(): RealmObject

	@Keep
	@Serializable
	data class ExportBaseObject(
		@Serializable(with = RealmUUIDNullableSerializer::class)
		@SerialName("default_chapter_id") val defaultChapterId: RealmUUID? = null,
		@SerialName("notebook_id_order_list") val notebookIdOrderList: List<@Serializable(with = RealmUUIDSerializer::class) RealmUUID> = listOf(),
		@SerialName("bucket_id_order_list") val bucketIdOrderList: List<@Serializable(with = RealmUUIDSerializer::class) RealmUUID> = listOf(),
		@SerialName("schema_version") val schemaVersion: Int = SCHEME_VERSION,
	) : Exportable() {
		override fun toObject(): BaseObject = BaseObject().apply {
			this.defaultChapterId = this@ExportBaseObject.defaultChapterId
			this.notebookIdOrderList.addAll(this@ExportBaseObject.notebookIdOrderList)
			this.bucketIdOrderList.addAll(this@ExportBaseObject.bucketIdOrderList)
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
		@SerialName("id") val id: RealmUUID,
		@SerialName("created_timestamp") val createdTimestamp: Long,
		@SerialName("modified_timestamp") val modifiedTimestamp: Long,
		@SerialName("user_timestamp") val userTimestamp: Long,
		@SerialName("title") val title: String? = null,
		@SerialName("lat_lng") val latLng: LatLng? = null,
		@SerialName("address") val address: String? = null,
		@SerialName("content") val content: String? = null,
		@SerialName("is_favourite") val isFavourite: Boolean = false,
		@SerialName("is_locked") val isLocked: Boolean = false,
		@Serializable(with = RealmUUIDNullableSerializer::class)
		@SerialName("parent_id") val parentId: RealmUUID? = null,
		@SerialName("schema_version") val schemaVersion: Int = SCHEME_VERSION,
	) : Exportable() {
		override fun toObject(): NoteObject = NoteObject().apply {
			this.id = this@ExportNoteObject.id
			this.createdTimestamp = this@ExportNoteObject.createdTimestamp
			this.modifiedTimestamp = this@ExportNoteObject.modifiedTimestamp
			this.userTimestamp = this@ExportNoteObject.userTimestamp
			this.title = this@ExportNoteObject.title
			this.setLatLng(this@ExportNoteObject.latLng)
			this.address = this@ExportNoteObject.address
			this.content = this@ExportNoteObject.content
			this.isFavourite = this@ExportNoteObject.isFavourite
			this.isLocked = this@ExportNoteObject.isLocked
			this.parentId = this@ExportNoteObject.parentId
		}

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
		@SerialName("id") val id: RealmUUID,
		@SerialName("created_timestamp") val createdTimestamp: Long,
		@SerialName("modified_timestamp") val modifiedTimestamp: Long,
		@SerialName("title") val title: String? = null,
		@SerialName("description") val description: String? = null,
		@SerialName("color") val color: Int? = null,
		@SerialName("thumbnail") val thumbnail: String? = null,
		@SerialName("is_favourite") val isFavourite: Boolean = false,
		@SerialName("is_locked") val isLocked: Boolean = false,
		@Serializable(with = RealmUUIDNullableSerializer::class)
		@SerialName("parent_id") val parentId: RealmUUID? = null,
		@SerialName("schema_version") val schemaVersion: Int = SCHEME_VERSION,
	) : Exportable() {
		override fun toObject(): ChapterObject = ChapterObject().apply {
			this.id = this@ExportChapterObject.id
			this.createdTimestamp = this@ExportChapterObject.createdTimestamp
			this.modifiedTimestamp = this@ExportChapterObject.modifiedTimestamp
			this.title = this@ExportChapterObject.title
			this.description = this@ExportChapterObject.description
			this.color = this@ExportChapterObject.color
			this.thumbnail = this@ExportChapterObject.thumbnail
			this.isFavourite = this@ExportChapterObject.isFavourite
			this.isLocked = this@ExportChapterObject.isLocked
			this.parentId = this@ExportChapterObject.parentId
		}

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
		@SerialName("id") val id: RealmUUID,
		@SerialName("created_timestamp") val createdTimestamp: Long,
		@SerialName("modified_timestamp") val modifiedTimestamp: Long,
		@SerialName("bucket_type") val bucketType: String,
		@SerialName("title") val title: String? = null,
		@SerialName("state") val state: String = BucketItemState.ALPHA.name,
		@SerialName("thumbnail") val thumbnail: String? = null,
		@SerialName("is_favourite") val isFavourite: Boolean = false,
		@SerialName("is_locked") val isLocked: Boolean = false,
		@Serializable(with = RealmUUIDNullableSerializer::class)
		@SerialName("parent_id") val parentId: RealmUUID? = null,
		@SerialName("key") val key: String? = null,
		@SerialName("data") val data: String? = null,
		@SerialName("schema_version") val schemaVersion: Int = SCHEME_VERSION,
	) : Exportable() {
		override fun toObject(): BucketItemObject = BucketItemObject().apply {
			this.id = this@ExportBucketItemObject.id
			this.createdTimestamp = this@ExportBucketItemObject.createdTimestamp
			this.modifiedTimestamp = this@ExportBucketItemObject.modifiedTimestamp
			this.bucketType = this@ExportBucketItemObject.bucketType
			this.title = this@ExportBucketItemObject.title
			this.state = this@ExportBucketItemObject.state
			this.thumbnail = this@ExportBucketItemObject.thumbnail
			this.isFavourite = this@ExportBucketItemObject.isFavourite
			this.isLocked = this@ExportBucketItemObject.isLocked
			this.parentId = this@ExportBucketItemObject.parentId
			this.key = this@ExportBucketItemObject.key
			this.data = this@ExportBucketItemObject.data
		}

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
		@SerialName("id") val id: RealmUUID,
		@SerialName("created_timestamp") val createdTimestamp: Long,
		@SerialName("modified_timestamp") val modifiedTimestamp: Long,
		@SerialName("bucket_type") val bucketType: String,
		@SerialName("title") val title: String? = null,
		@SerialName("description") val description: String? = null,
		@SerialName("is_favourite") val isFavourite: Boolean = false,
		@SerialName("is_locked") val isLocked: Boolean = false,
		@SerialName("bucket_item_order_list") val bucketItemOrderList: List<@Serializable(with = RealmUUIDSerializer::class) RealmUUID> = listOf(),
		@SerialName("schema_version") val schemaVersion: Int = SCHEME_VERSION,
	) : Exportable() {
		override fun toObject(): BucketObject = BucketObject().apply {
			this.id = this@ExportBucketObject.id
			this.createdTimestamp = this@ExportBucketObject.createdTimestamp
			this.modifiedTimestamp = this@ExportBucketObject.modifiedTimestamp
			this.bucketType = this@ExportBucketObject.bucketType
			this.title = this@ExportBucketObject.title
			this.description = this@ExportBucketObject.description
			this.isFavourite = this@ExportBucketObject.isFavourite
			this.isLocked = this@ExportBucketObject.isLocked
			this.bucketItemOrderList.addAll(this@ExportBucketObject.bucketItemOrderList)
		}

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
		@SerialName("id") val id: RealmUUID,
		@SerialName("tag") val tag: String,
		@SerialName("color") val color: Int,
		@SerialName("modified_timestamp") val modifiedTimestamp: Long,
		@SerialName("object_id_list") val objectIdList: List<@Serializable(with = RealmUUIDSerializer::class) RealmUUID> = listOf(),
		@SerialName("schema_version") val schemaVersion: Int = SCHEME_VERSION,
	) : Exportable() {
		override fun toObject(): TagObject = TagObject().apply {
			this.id = this@ExportTagObject.id
			this.tag = this@ExportTagObject.tag
			this.color = this@ExportTagObject.color
			this.modifiedTimestamp = this@ExportTagObject.modifiedTimestamp
			this.objectIdList.addAll(this@ExportTagObject.objectIdList)
		}

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

	companion object {
		const val SCHEME_VERSION = 3

		fun fromObject(inputObject: RealmObject): Exportable {
			return when (inputObject) {
				is BaseObject -> ExportBaseObject.fromObject(inputObject = inputObject)
				is NoteObject -> ExportNoteObject.fromObject(inputObject = inputObject)
				is ChapterObject -> ExportChapterObject.fromObject(inputObject = inputObject)
				is BucketItemObject -> ExportBucketItemObject.fromObject(inputObject = inputObject)
				is BucketObject -> ExportBucketObject.fromObject(inputObject = inputObject)
				is TagObject -> ExportTagObject.fromObject(inputObject = inputObject)
				else -> throw IllegalArgumentException("Unknown object type")
			}
		}
	}
}
