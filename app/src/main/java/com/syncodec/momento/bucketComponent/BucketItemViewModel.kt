package com.syncodec.momento.bucketComponent

import android.app.Application
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.syncodec.momento.Momento
import com.syncodec.momento.bucketComponent.modalBottomSheet.BookData
import com.syncodec.momento.bucketComponent.modalBottomSheet.MovieData
import com.syncodec.momento.bucketComponent.screen.MovieCharacterData
import com.syncodec.momento.database.bucket.Bucket
import com.syncodec.momento.database.bucket.BucketDbEntry
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


	lateinit var bucketItemType: BucketItemType.Type
	lateinit var bucketKey: String
	var bucketItemKey: String? = null
	lateinit var bucketItemDataJson: JSONObject

	lateinit var bucketItemActivityState: BucketItemActivity.BucketItemActivityState

	var contentList = mutableStateListOf<String>()
	var thumbnail: ByteArray? by mutableStateOf(null)
	var extraData: String by mutableStateOf( objectMapper.writeValueAsString(mutableListOf<String>()) )

	var bucketItem by mutableStateOf(
		BucketItem(
			primaryKey = "",
			bucketKey = "",
			itemType = BucketItemType.Type.TODO,
			createdTimestamp = -1
		)
	)

	private val _status: MutableState<Int> = mutableStateOf(0)
	val status: State<Int> get() = _status

	fun generateNewBucketItem() {
		val currentTimestamp = System.currentTimeMillis()

		bucketItem = BucketItem(
			primaryKey = generatePrimaryKey(),
			bucketKey = this.bucketKey,
			itemType = this.bucketItemType,
			createdTimestamp = currentTimestamp
		).apply {
			this.modifiedTimestamp = currentTimestamp
			this.title = bucketItemDataJson.getString("title")
			this.innerContent = bucketItemDataJson.toString()
			this.contentList = this@BucketItemViewModel.contentList
			this.thumbnail = this@BucketItemViewModel.thumbnail
		}

		bucketItemKey = bucketItem.bucketKey

		viewModelScope.launch {
			withContext(Dispatchers.IO) {
				bucketRepository.createNewBucketItem(
					bucketItem = bucketItem
				)
				withContext(Dispatchers.Main) {
					_status.value = 1
				}
				when(bucketItemType) {
					BucketItemType.Type.TODO -> ""
					BucketItemType.Type.BOOKS -> getBookData()
					BucketItemType.Type.MOVIES -> getMovieData()
					BucketItemType.Type.TVSHOWS -> TODO()
					BucketItemType.Type.MEDIA -> TODO()
					BucketItemType.Type.LINKS -> TODO()
				}

			}
		}
	}

	fun updateBucketItem() {
		viewModelScope.launch {
			withContext(Dispatchers.IO) {
				bucketRepository.putBucketItem(
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

			if (bucketItem.innerContent!=null) {
				extraData = when(bucketItemType) {
					BucketItemType.Type.TODO -> ""
					BucketItemType.Type.BOOKS -> ""
					BucketItemType.Type.MOVIES -> objectMapper.writeValueAsString(objectMapper.readValue<MovieData>(bucketItem.innerContent!!).characterDataList)
					BucketItemType.Type.TVSHOWS -> objectMapper.writeValueAsString(objectMapper.readValue<MovieData>(bucketItem.innerContent!!).characterDataList)
					BucketItemType.Type.MEDIA -> objectMapper.writeValueAsString(objectMapper.readValue<MovieData>(bucketItem.innerContent!!).characterDataList)
					BucketItemType.Type.LINKS -> objectMapper.writeValueAsString(objectMapper.readValue<MovieData>(bucketItem.innerContent!!).characterDataList)
				}
			}

			this@BucketItemViewModel.thumbnail = bucketItem.thumbnail

			withContext(Dispatchers.Main) {
				_status.value = 1
			}
		}
	}

	private fun getMovieData() {
		viewModelScope.launch {
			withContext(Dispatchers.IO) {
				val movieData: MovieData = objectMapper.readValue(bucketItemDataJson.toString())
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
				movieData.characterDataList = movieCharacterDataList
				bucketItem.innerContent = objectMapper.writeValueAsString(movieData)
				extraData = objectMapper.writeValueAsString(movieData.characterDataList)
				updateBucketItem()

				val thumbnailUrl = "https://image.tmdb.org/t/p/w500${movieData.posterPath}"
				val contentThumbnail = (getApplication<Application>() as Momento).downloadBucketItemThumbnail(
					thumbnailUrl = thumbnailUrl,
				)
				withContext(Dispatchers.Main) {
					this@BucketItemViewModel.thumbnail = contentThumbnail
					bucketItem.thumbnail = contentThumbnail
				}
				updateBucketItem()
			}
		}
	}

	private fun getBookData() {
		viewModelScope.launch {
			val bookData: BookData = objectMapper.readValue(bucketItemDataJson.toString())

			withContext(Dispatchers.IO) {
				val thumbnailUrl = "https://covers.openlibrary.org/b/id/${bookData.coverI}-L.jpg"
				val contentThumbnail = (getApplication<Application>() as Momento).downloadBucketItemThumbnail(
					thumbnailUrl = thumbnailUrl,
				)
				withContext(Dispatchers.Main) {
					this@BucketItemViewModel.thumbnail = contentThumbnail
					bucketItem.thumbnail = contentThumbnail
				}
				updateBucketItem()
			}
		}
	}
}
