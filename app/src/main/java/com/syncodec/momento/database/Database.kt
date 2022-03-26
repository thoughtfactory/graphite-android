package com.syncodec.momento.database

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.room.*
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.android.gms.maps.model.LatLng
import com.syncodec.momento.database.attachment.AttachmentDbEntry
import com.syncodec.momento.database.attachment.AttachmentTableDao
import com.syncodec.momento.database.bucket.*
import com.syncodec.momento.database.bucketItem.BucketItemDbEntry
import com.syncodec.momento.database.bucketItem.BucketItemDbTableDao
import com.syncodec.momento.database.bucketItem.BucketItemState
import com.syncodec.momento.database.bucketItem.BucketItemType
import com.syncodec.momento.database.chapter.ChapterDbEntry
import com.syncodec.momento.database.chapter.ChapterTableDao
import com.syncodec.momento.database.note.LocationData
import com.syncodec.momento.database.note.NoteDbEntry
import com.syncodec.momento.database.note.NoteTableDao
import com.syncodec.momento.database.notebook.NotebookDbEntry
import com.syncodec.momento.database.notebook.NotebookTableDao
import com.syncodec.momento.database.tag.TagDbEntry
import com.syncodec.momento.database.tag.TagDbTableDao
import com.syncodec.momento.database.tag.TagKeyDbEntry
import com.syncodec.momento.database.tag.TagKeyDbTableDao
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import javax.inject.Singleton


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
		return value?.let { objectMapper.writeValueAsString(it) }
	}

	@TypeConverter
	fun fromDataToLatLng(data: String?): LatLng? {
		return if (data != null) {
			val jsonObject = JSONObject(data)
			LatLng(jsonObject.getDouble("latitude"), jsonObject.getDouble("longitude"))
		} else null
	}

	@TypeConverter
	fun fromLocationDataToData(value: LocationData?): String? {
		return value?.let { objectMapper.writeValueAsString(it) }
	}

	@TypeConverter
	fun fromDataToLocationData(data: String?): LocationData? {
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
	fun fromTagListToData(value: MutableList<TagDbEntry>): String = objectMapper.writeValueAsString(value)

	@TypeConverter
	fun fromDataToTaList(value: String?): MutableList<TagDbEntry> = value?.let { objectMapper.readValue(it) } ?: mutableListOf()
}

@Singleton
@Database(
	entities = [
		NoteDbEntry::class,
		ChapterDbEntry::class,
		NotebookDbEntry::class,
		BucketDbEntry::class,
		BucketItemDbEntry::class,
		AttachmentDbEntry::class,
		TagDbEntry::class,
		TagKeyDbEntry::class
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
