package com.syncodec.momento.repository

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import com.syncodec.momento.Momento
import com.syncodec.momento.database.UserDatabase
import com.syncodec.momento.database.diary.Diary
import com.syncodec.momento.database.diary.DiaryDbEntry
import com.syncodec.momento.database.diary.DiaryTableDao


class DiaryRepository(val application: Application) {
    private var diaryTableDao: DiaryTableDao = UserDatabase.getInstance(application).diaryTableDao

    val diaryDbEntryListLiveData: LiveData<List<DiaryDbEntry>> = diaryTableDao.getAllAsLiveData()

    suspend fun saveDiary(diary: Diary) {
        DiaryDbEntry(
            primaryKey = diary.primaryKey,
            timezoneOffset = diary.timezoneOffset
        ).apply {
            this.createdTimestamp = diary.createdTimestamp
            this.modifiedTimestamp = diary.modifiedTimestamp
            this.userTimestamp = diary.userTimestamp
            this.latitude = diary.location?.latitude
            this.latitude = diary.location?.longitude
            this.address = diary.address

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
