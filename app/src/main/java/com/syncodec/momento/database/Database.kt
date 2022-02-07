package com.syncodec.momento.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.syncodec.momento.database.bucket.BucketDbEntry
import com.syncodec.momento.database.bucket.BucketTableDao
import com.syncodec.momento.database.diary.DiaryDbEntry
import com.syncodec.momento.database.diary.DiaryTableDao
import com.syncodec.momento.database.attachment.Attachment
import com.syncodec.momento.database.attachment.AttachmentTableDao
import com.syncodec.momento.database.notebook.NotebookDbEntry
import com.syncodec.momento.database.notebook.NotebookTableDao
import javax.inject.Singleton

@Singleton
@Database(entities = [DiaryDbEntry::class, BucketDbEntry::class, NotebookDbEntry::class, Attachment::class], version = 1, exportSchema = false)
abstract class UserDatabase : RoomDatabase() {

	abstract val diaryTableDao: DiaryTableDao
	abstract val attachmentTableDao: AttachmentTableDao
	abstract val bucketTableDao: BucketTableDao
	abstract val notebookTableDao: NotebookTableDao

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
