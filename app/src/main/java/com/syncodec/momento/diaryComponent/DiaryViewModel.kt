package com.syncodec.momento.diaryComponent

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.syncodec.momento.database.diary.Diary
import com.syncodec.momento.konstant.MediaType
import com.syncodec.momento.miscellaneous.generatePrimaryKey
import com.syncodec.momento.repository.DiaryRepository
import com.syncodec.momento.repository.MediaRepository
import java.util.*

data class TempMediaData(val uri: Uri, val mediaType: MediaType)

class DiaryViewModel(application: Application): AndroidViewModel(application) {

	val diaryRepository: DiaryRepository = DiaryRepository(application)
	val mediaRepository: MediaRepository = MediaRepository(application)

	private val currentTimestamp = System.currentTimeMillis()

//	var diary: MutableLiveData<Diary> = MutableLiveData(
//		Diary(
//			primaryKey = generatePrimaryKey(),
//			timezoneOffset = TimeZone
//				.getDefault()
//				.getOffset(currentTimestamp)
//		).apply {
//			this.modifiedTimestamp = currentTimestamp
//			this.userTimestamp = currentTimestamp
//		}
//	)

	var diary: Diary by mutableStateOf(
		Diary(
			primaryKey = generatePrimaryKey(),
			timezoneOffset = TimeZone
				.getDefault()
				.getOffset(currentTimestamp)
		).apply {
			this.createdTimestamp = currentTimestamp
			this.modifiedTimestamp = currentTimestamp
			this.userTimestamp = currentTimestamp
		}
	)

	lateinit var diaryActivityState: DiaryActivity.DiaryActivityState

	var mediaList: MutableList<TempMediaData> = mutableStateListOf()

	fun insertMedia(uri: Uri, mediaType: MediaType) {
		mediaList.add(TempMediaData(uri = uri, mediaType =mediaType))
	}

	fun removeLocationData() {
		diary.location = null
		diary.address = null
	}

//	fun insertAttachment(uri: Uri, mediaType: MediaType) {
//		viewModelScope.launch {
//			withContext(Dispatchers.IO) {
//				val currentTimestamp = System.currentTimeMillis()
//				Media(
//					primaryKey = generatePrimaryKey(Konstant.MEDIA_PRIMARY_KEY_LENGTH),
//					createdTimestamp = currentTimestamp,
//					timezoneOffset = TimeZone
//						.getDefault()
//						.getOffset(currentTimestamp),
//					mediaType = mediaType.name
//				).apply {
//					mediaList.add(this)
//				}
//			}
//		}
//	}
}
