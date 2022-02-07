package com.syncodec.momento.repository

import android.app.Application
import androidx.lifecycle.LiveData
import com.syncodec.momento.database.UserDatabase
import com.syncodec.momento.database.diary.Note
import com.syncodec.momento.database.diary.DiaryDbEntry
import com.syncodec.momento.database.diary.DiaryTableDao


class DiaryRepository(val application: Application) {
    private var diaryTableDao: DiaryTableDao = UserDatabase.getInstance(application).diaryTableDao

    val diaryDbEntryListLiveData: LiveData<List<DiaryDbEntry>> = diaryTableDao.getAllAsLiveData()

    suspend fun saveDiary(note: Note) {
        DiaryDbEntry(
            primaryKey = note.primaryKey,
            timezoneOffset = note.timezoneOffset
        ).apply {
            this.createdTimestamp = note.createdTimestamp
            this.modifiedTimestamp = note.modifiedTimestamp
            this.userTimestamp = note.userTimestamp
            this.contentThumbnail = note.contentThumbnail
            this.latitude = note.location?.latitude
            this.latitude = note.location?.longitude
            this.address = note.address

           insert(this)
        }

    }

    suspend fun insert(diaryDbEntry: DiaryDbEntry) {
        diaryTableDao.insert(diaryDbEntry)
    }

    suspend fun delete(primaryKey: String) {
        diaryTableDao.delete(primaryKey)
    }

    suspend fun deleteAll() {
        diaryTableDao.deleteAll()
    }
}
