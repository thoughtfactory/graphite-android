package com.syncodec.momento.diaryComponent

import android.app.Application
import android.location.Location
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.syncodec.momento.database.diary.Diary
import com.syncodec.momento.database.diary.DiaryDbEntry
import com.syncodec.momento.database.diary.WeatherData
import com.syncodec.momento.konstant.MediaType
import com.syncodec.momento.miscellaneous.generatePrimaryKey
import com.syncodec.momento.repository.DiaryRepository
import com.syncodec.momento.repository.MediaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

data class TempMediaData(val uri: Uri, val mediaType: MediaType)

class DiaryViewModel(application: Application): AndroidViewModel(application) {

	val diaryRepository: DiaryRepository = DiaryRepository(application)
	val mediaRepository: MediaRepository = MediaRepository(application)

	private val currentTimestamp = System.currentTimeMillis()

	var address by mutableStateOf<String?>(null)
	var weatherData by mutableStateOf<WeatherData?>(null)
	var location by mutableStateOf<Location?>(null)

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
			this.location = this@DiaryViewModel.location
			this.address = this@DiaryViewModel.address
		}
	)

	lateinit var diaryActivityState: DiaryActivity.DiaryActivityState

	var mediaList: MutableList<TempMediaData> = mutableStateListOf()

	fun insertMedia(uri: Uri, mediaType: MediaType) {
		mediaList.add(TempMediaData(uri = uri, mediaType =mediaType))
	}

	fun removeLocationData() {
		location = null
		address = null
	}

	fun saveDiary() {
		viewModelScope.launch {
			withContext(Dispatchers.IO) {
				diaryRepository.saveDiary(this@DiaryViewModel.diary)
			}
		}
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
