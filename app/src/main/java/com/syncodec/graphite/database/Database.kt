package com.syncodec.graphite.database

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.room.*
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.android.gms.maps.model.LatLng
import com.syncodec.graphite.database.attachment.AttachmentDbEntry
import com.syncodec.graphite.database.attachment.AttachmentTableDao
import com.syncodec.graphite.database.bucket.BucketDbEntry
import com.syncodec.graphite.database.bucket.BucketDbTableDao
import com.syncodec.graphite.database.bucketItem.*
import com.syncodec.graphite.database.chapter.ChapterDbEntry
import com.syncodec.graphite.database.chapter.ChapterTableDao
import com.syncodec.graphite.database.note.NoteDbEntry
import com.syncodec.graphite.database.note.NoteTableDao
import com.syncodec.graphite.database.notebook.NotebookDbEntry
import com.syncodec.graphite.database.notebook.NotebookTableDao
import com.syncodec.graphite.database.quote.QuoteDbEntry
import com.syncodec.graphite.database.quote.QuoteTableDao
import com.syncodec.graphite.database.tag.TagDbEntry
import com.syncodec.graphite.database.tag.TagDbTableDao
import com.syncodec.graphite.database.tag.TagKeyDbEntry
import com.syncodec.graphite.database.tag.TagKeyDbTableDao
import org.json.JSONObject
import java.io.ByteArrayOutputStream


class Converters {
	private val objectMapper: ObjectMapper = ObjectMapper().registerModule(KotlinModule())

	@TypeConverter
	fun fromChapterPathToData(value: MutableList<String>?): String? {
		return value?.let { objectMapper.writeValueAsString(it) }
	}

	@TypeConverter
	fun fromDataToChapterPath(data: String?): MutableList<String>? {
		return data?.let { objectMapper.readValue(it) }
	}

	@TypeConverter
	fun fromLatLngToData(value: LatLng?): String? {
		JSONObject().apply {
			return if (value != null) {
				put("latitude" , value.latitude)
				put("longitude" , value.longitude)
				toString()
			} else null
		}
	}

	@TypeConverter
	fun fromDataToLatLng(data: String?): LatLng? {
		return if (data != null) {
			try {
				val jsonObject = JSONObject(data)
				LatLng(jsonObject.getDouble("latitude"), jsonObject.getDouble("longitude"))
			} catch (exception : Exception) {
				null
			}
		} else null
	}

	@TypeConverter
	fun fromBucketItemToData(value: BucketItem?): ByteArray? {
		return value?.let { objectMapper.writeValueAsBytes(it) }
	}

	@TypeConverter
	fun fromDataToBucketItem(data: ByteArray?): BucketItem? {
		return data?.let { objectMapper.readValue(it) }
	}

	@TypeConverter
	fun fromBucketItemTypeToData(value: BucketItemType): Int {
		return value.ordinal
	}

	@TypeConverter
	fun fromDataToBucketItemType(data: Int): BucketItemType {
		return BucketItemType.values()[data]
	}

	@TypeConverter
	fun fromBucketItemStateToData(value: BucketItemState): Int {
		return value.ordinal
	}

	@TypeConverter
	fun fromDataToBucketItemState(data: Int): BucketItemState {
		return BucketItemState.values()[data]
	}

	@TypeConverter
	fun fromBitmapToData(value: Bitmap?): ByteArray? {
		return if (value != null) {
			val stream = ByteArrayOutputStream()
			value.compress(Bitmap.CompressFormat.PNG, 100, stream)
			val byteArray: ByteArray = stream.toByteArray()
			byteArray
		} else {
			null
		}
	}

	@TypeConverter
	fun fromDataToBitmap(data: ByteArray?): Bitmap? {
		return if (data == null) {
			null
		} else {
			BitmapFactory.decodeByteArray(data, 0, data.size)
		}
	}

	@TypeConverter
	fun fromJsonObjectToData(value: JSONObject?): ByteArray? {
		return objectMapper.writeValueAsBytes(value?.toString())
	}

	@TypeConverter
	fun fromDataToJsonObject(data: ByteArray?): JSONObject? {
		return if (data == null) {
			null
		} else {
			objectMapper.readValue(data)
		}
	}
}

@Database(
	entities = [
		NoteDbEntry::class,
		ChapterDbEntry::class,
		NotebookDbEntry::class,
		BucketDbEntry::class,
		BucketItemDbEntry::class,
		AttachmentDbEntry::class,
		TagDbEntry::class,
		TagKeyDbEntry::class,
		QuoteDbEntry::class
	],
	version = 1,
	exportSchema = false
)
@TypeConverters(Converters::class)
abstract class UserDatabase : RoomDatabase() {

	abstract val noteTableDao: NoteTableDao
	abstract val chapterTableDao: ChapterTableDao
	abstract val notebookTableDao: NotebookTableDao
	abstract val bucketDbTableDao: BucketDbTableDao
	abstract val bucketItemDbTableDao: BucketItemDbTableDao
	abstract val attachmentTableDao: AttachmentTableDao
	abstract val tagDbTableDao: TagDbTableDao
	abstract val tagKeyDbTableDao: TagKeyDbTableDao
	abstract val quoteTableDao: QuoteTableDao

	companion object {
		@Volatile
		private var INSTANCE: UserDatabase? = null

		fun getInstance(context: Context): UserDatabase {
			synchronized(lock = this) {
				var instance = INSTANCE
				if (instance == null) {
					instance = Room.databaseBuilder(
						context,
						UserDatabase::class.java,
						"user_database"
					)
//						TODO
//                      !!!   Will destruct and reconstruct database when version changes
						.fallbackToDestructiveMigration()
						.build()
					INSTANCE = instance
				}
				return instance
			}
		}
	}
}
