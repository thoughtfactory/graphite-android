package com.syncodec.momento.bucketComponent

import android.app.Application
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.syncodec.momento.Momento
import com.syncodec.momento.bucketComponent.modalBottomSheet.MovieData
import com.syncodec.momento.bucketComponent.screen.MovieCharacterData
import com.syncodec.momento.database.bucket.BucketItem
import com.syncodec.momento.database.bucket.BucketItemType
import com.syncodec.momento.konstant.Secret
import com.syncodec.momento.miscellaneous.generatePrimaryKey
import com.syncodec.momento.repository.BucketRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject


class BucketItemViewModel(application: Application) : AndroidViewModel(application) {

	private val objectMapper: ObjectMapper = ObjectMapper().registerModule(KotlinModule())

	val bucketRepository: BucketRepository = BucketRepository(application)


	lateinit var bucketItemType: BucketItemType
	lateinit var bucketKey: String
	var bucketItemKey: String? = null
	lateinit var bucketItemDataJson: JSONObject

	lateinit var bucketItemActivityState: BucketItemActivity.BucketItemActivityState

	var isContentThumbnailAvailable: Boolean? by mutableStateOf(null)
	var contentThumbnailPath: String? = null

	var contentList = mutableStateListOf<String>()
	var movieCharactersData: List<MovieCharacterData> by mutableStateOf(listOf())


	var bucketItem by mutableStateOf(
		BucketItem(
			primaryKey = "",
			bucketKey = "",
			itemType = BucketItemType.TODO,
			createdTimestamp = -1
		)
	)

	private val _status: MutableState<Int> = mutableStateOf(0)
	val status: State<Int> get() = _status

	fun generateNewBucketItem() {
		val currentTimestamp = System.currentTimeMillis()
		val movieData: MovieData = objectMapper.readValue(bucketItemDataJson.toString())

		bucketItem = BucketItem(
			primaryKey = generatePrimaryKey(),
			bucketKey = this.bucketKey,
			itemType = this.bucketItemType,
			createdTimestamp = currentTimestamp
		).apply {
			this.modifiedTimestamp = currentTimestamp
			this.title = movieData.title
			this.innerContent = bucketItemDataJson.toString()
			this.contentList = this@BucketItemViewModel.contentList
			this.isContentThumbnailAvailable = this@BucketItemViewModel.isContentThumbnailAvailable
		}

		bucketItemKey = bucketItem.bucketKey

		viewModelScope.launch {
			withContext(Dispatchers.IO) {
				bucketRepository.insertBucketItem(
					bucketItem = bucketItem,
					isNewItem = true
				)
				withContext(Dispatchers.Main) {
					_status.value = 1
				}
				getMovieData()
			}
		}
	}

	fun updateBucketItem() {
		viewModelScope.launch {
			withContext(Dispatchers.IO) {
				bucketRepository.insertBucketItem(
					bucketItem = bucketItem
				)
			}
		}
	}

	fun readBucketItem() {
		viewModelScope.launch {
			bucketItem = bucketRepository.readBucketItem(
				bucketKey = bucketKey,
				bucketItemKey = bucketItemKey!!
			)
			contentList = bucketItem.contentList.toMutableStateList()
			bucketItem.contentList = contentList

			val movieData: MovieData = objectMapper.readValue(bucketItem.innerContent!!)
			movieCharactersData = movieData.characterDataList

			contentThumbnailPath = "${(getApplication<Application>() as Momento).BUCKET_DIR}/bucket_${bucketKey}/bucket_item_thumbnail_${bucketItemKey}.jpg"
			this@BucketItemViewModel.isContentThumbnailAvailable = true
			bucketItem.isContentThumbnailAvailable = true

			withContext(Dispatchers.Main) {
				_status.value = 1
			}
		}
	}

	private fun getMovieData() {
		CoroutineScope(Dispatchers.IO).launch {
			val movieData: MovieData = objectMapper.readValue(bucketItemDataJson.toString())
			if (movieData.characterDataList.isEmpty()) {
				val requestUrl = "https://api.themoviedb.org/3/movie/${movieData.id}/credits?api_key=${Secret.TMDB_KEY}&language=en-US"

				val movieDataString = (getApplication<Application>() as Momento).downloadMovieData(requestUrl = requestUrl)
				val jsonObject = JSONObject(movieDataString)

				val cast = jsonObject.getJSONArray("cast")
				val castLength = cast.length()
				val movieCharacterDataList: MutableList<MovieCharacterData> = mutableListOf()
				for (i in 0 until castLength) {
					val movieCharacterData = objectMapper.readValue<MovieCharacterData>(cast.get(i).toString())
					movieCharacterDataList.add(movieCharacterData)
				}

				val crew = jsonObject.getJSONArray("crew")
				val crewLength = crew.length()
				for (i in 0 until crewLength) {
					val crewData = crew.getJSONObject(i)
					val job = crewData.getString("job")

					if (job == "Director") {
						val movieCharacterData = MovieCharacterData(
							id = crewData.getString("id"),
							name = crewData.getString("name"),
							character = crewData.getString("job")
						)

						movieCharacterDataList.add(movieCharacterData)
					}
				}
				movieCharactersData = movieCharacterDataList
				movieData.characterDataList = movieCharacterDataList
				bucketItem.innerContent = objectMapper.writeValueAsString(movieData)
				updateBucketItem()

				val thumbnailUrl = "https://image.tmdb.org/t/p/w500${movieData.posterPath}"
				val contentThumbnail = (getApplication<Application>() as Momento).downloadBucketItemThumbnail(
					thumbnailUrl = thumbnailUrl,
					bucketKey = bucketItem.bucketKey,
					bucketItemKey = bucketItem.primaryKey
				)

				withContext(Dispatchers.Main) {
					this@BucketItemViewModel.contentThumbnailPath = contentThumbnail
					this@BucketItemViewModel.isContentThumbnailAvailable = true
					bucketItem.isContentThumbnailAvailable = true
				}
				updateBucketItem()

			} else {
				movieCharactersData = movieData.characterDataList
			}
		}
	}
}
