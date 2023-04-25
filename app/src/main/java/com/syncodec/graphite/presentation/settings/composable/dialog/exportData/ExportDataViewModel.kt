package com.syncodec.graphite.presentation.settings.composable.dialog.exportData

import androidx.lifecycle.ViewModel
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ser.std.StdDelegatingSerializer
import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.syncodec.graphite.di.model.BucketItemObject
import com.syncodec.graphite.di.model.BucketObject
import com.syncodec.graphite.di.model.ChapterObject
import com.syncodec.graphite.di.model.NoteObject
import com.syncodec.graphite.di.model.TagObject
import com.syncodec.graphite.di.repository.RealmListConverter
import com.syncodec.graphite.di.repository.RealmUUIDDeserializer
import com.syncodec.graphite.di.repository.RealmUUIDSerializer
import com.syncodec.graphite.di.repository.repository.Repository
import io.realm.kotlin.schema.RealmSchema
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmUUID
import org.json.JSONObject
import org.koin.android.annotation.KoinViewModel


@KoinViewModel
class ExportDataViewModel(val repository : Repository) : ViewModel() {

	val objectMapper = jsonMapper {
		addModule(
			kotlinModule()
				.addSerializer(RealmUUID::class.java, RealmUUIDSerializer())
				.addDeserializer(RealmUUID::class.java, RealmUUIDDeserializer())
				.addSerializer(RealmList::class.java, StdDelegatingSerializer(RealmListConverter()))
		)
	}.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

	fun exportData(
		isNotesSelected : Boolean,
		isBucketSelected : Boolean,
		isTagSelected : Boolean,
	) : ExportObject {

		val realmSchema = repository.getRealmSchema()
		val baseObject = repository
			.getBaseObject()
			?.let {
				val jsonObject = JSONObject()
				jsonObject.put("version", "2")
				jsonObject.put("default_chapter_id", it.defaultChapterId.toString())
				jsonObject.toString()
			}
		val bucketList = if (isBucketSelected) repository.getAllBucket().map { it.clone() } else null
		val bucketObjectList = if (isBucketSelected) repository.getAllBucketItem().map { it.clone() } else null
		val chapterList = if (isNotesSelected) repository.getAllChapter().map { it.clone() } else null
		val noteList = if (isNotesSelected) repository.getAllNote().map { it.clone() } else null
		val tagList = if (isTagSelected) repository.getAllTag().map { it.clone() } else null

		return ExportObject(
			realmSchema = realmSchema,
			baseObject = baseObject,
			bucketList = bucketList,
			bucketItemList = bucketObjectList,
			chapterList = chapterList,
			noteList = noteList,
			tagList = tagList,
		)
	}

	companion object {
		data class ExportObject(
			val realmSchema : Pair<RealmSchema, Long>? = null,
			val baseObject : String? = null,
			val bucketList : List<BucketObject>? = null,
			val bucketItemList : List<BucketItemObject>? = null,
			val chapterList : List<ChapterObject>? = null,
			val noteList : List<NoteObject>? = null,
			val tagList : List<TagObject>? = null,
		)
	}
}
