package com.syncodec.graphite.presentation.settings.composable.dialog.exportData

import androidx.lifecycle.ViewModel
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.syncodec.graphite.di.model.BucketItemObject
import com.syncodec.graphite.di.model.BucketObject
import com.syncodec.graphite.di.model.ChapterObject
import com.syncodec.graphite.di.model.NoteObject
import com.syncodec.graphite.di.model.TagObject
import com.syncodec.graphite.di.repository.KoinRepository
import com.syncodec.graphite.di.repository.RealmUUIDDeserializer
import io.realm.kotlin.schema.RealmSchema
import io.realm.kotlin.types.RealmUUID
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry
import org.apache.commons.compress.archivers.sevenz.SevenZOutputFile
import org.json.JSONObject
import org.koin.android.annotation.KoinViewModel
import java.io.File
import java.io.IOException


@KoinViewModel
class ExportDataViewModel(private val repository : KoinRepository) : ViewModel() {

	private val objectMapper = jsonMapper {
		addModule(
			kotlinModule().addDeserializer(
				RealmUUID::class.java,
				RealmUUIDDeserializer()
			)
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
		val bucketList = if (isBucketSelected) repository.getAllBucket() else null
		val bucketObjectList = if (isBucketSelected) repository.getAllBucketItem() else null
		val chapterList = if(isNotesSelected) repository.getAllChapter() else null
		val noteList = if(isNotesSelected) repository.getAllNote() else null
		val tagList = if(isTagSelected) repository.getAllTag() else null

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
			val realmSchema: Pair<RealmSchema, Long>? = null,
			val baseObject : String? = null,
			val bucketList: List<BucketObject>? = null,
			val bucketItemList: List<BucketItemObject>? = null,
			val chapterList: List<ChapterObject>? = null,
			val noteList: List<NoteObject>? = null,
			val tagList: List<TagObject>? = null,
		)
	}
}
