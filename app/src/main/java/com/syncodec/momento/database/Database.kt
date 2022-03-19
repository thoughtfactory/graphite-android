package com.syncodec.momento.database

import android.content.Context
import androidx.room.*
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.android.gms.maps.model.LatLng
import com.syncodec.momento.database.attachment.Attachment
import com.syncodec.momento.database.attachment.AttachmentTableDao
import com.syncodec.momento.database.bucket.*
import com.syncodec.momento.database.chapter.ChapterDbEntry
import com.syncodec.momento.database.chapter.ChapterTableDao
import com.syncodec.momento.database.note.Note
import com.syncodec.momento.database.note.NoteDbEntry
import com.syncodec.momento.database.note.NoteTableDao
import com.syncodec.momento.database.notebook.NotebookDbEntry
import com.syncodec.momento.database.notebook.NotebookTableDao
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
		return data?.let { objectMapper.readValue(it) }
	}

	@TypeConverter
	fun fromBucketItemTypeToData(value: BucketItemType.Type): Int {
		return value.ordinal
	}

	@TypeConverter
	fun fromDataToBucketItemType(data: Int): BucketItemType.Type {
		return BucketItemType.Type.values()[data]
	}

	@TypeConverter
	fun fromBucketItemStateToData(value: BucketItemState): Int {
		return value.ordinal
	}

	@TypeConverter
	fun fromDataToBucketItemState(data: Int): BucketItemState {
		return BucketItemState.values()[data]
	}
}

@Singleton
@Database(
	entities = [
		NoteDbEntry::class,
		ChapterDbEntry::class,
		NotebookDbEntry::class,
		BucketDbEntry::class,
		BucketItemDbEntry::class,
		Attachment::class
	],
	version = 1,
	exportSchema = false
)
@TypeConverters(Converters::class)
abstract class UserDatabase : RoomDatabase() {

	abstract val noteTableDao: NoteTableDao
	abstract val chapterTableDao: ChapterTableDao
	abstract val notebookTableDao: NotebookTableDao
	abstract val attachmentTableDao: AttachmentTableDao
	abstract val bucketDbTableDao: BucketDbTableDao
	abstract val bucketItemDbTableDao: BucketItemDbTableDao

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
