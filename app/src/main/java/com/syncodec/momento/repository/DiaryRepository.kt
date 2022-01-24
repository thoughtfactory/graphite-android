package com.syncodec.momento.repository

import android.app.Application
import androidx.lifecycle.LiveData
import com.syncodec.momento.database.UserDatabase
import com.syncodec.momento.database.diary.DiaryDbEntry
import com.syncodec.momento.database.diary.DiaryTableDao


class DiaryRepository(application: Application) {
    private var diaryTableDao: DiaryTableDao
    init {
        diaryTableDao = UserDatabase.getInstance(application).diaryTableDao
    }

    val diaryDbEntryListLiveData: LiveData<List<DiaryDbEntry>> = diaryTableDao.getAllAsLiveData()
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
