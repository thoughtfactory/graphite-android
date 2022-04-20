package com.syncodec.momento.bucketItemComponent

import android.app.Application
import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import coil.ImageLoader
import coil.request.ImageRequest
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.syncodec.momento.Momento
import com.syncodec.momento.bucketComponent.modalBottomSheet.BookData
import com.syncodec.momento.bucketComponent.modalBottomSheet.ShowData
import com.syncodec.momento.bucketComponent.modalBottomSheet.ShowType
import com.syncodec.momento.database.bucketItem.*
import com.syncodec.momento.konstant.Konstant
import com.syncodec.momento.konstant.Secret
import com.syncodec.momento.konstant.Status
import com.syncodec.momento.miscellaneous.generatePrimaryKey
import com.syncodec.momento.repository.BucketRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import kotlin.properties.Delegates

class BucketItemViewModel(application: Application) : AndroidViewModel(application) {

	private val objectMapper: ObjectMapper = ObjectMapper().registerModule(KotlinModule())

	private val bucketRepository: BucketRepository =
		BucketRepository.getInstance(momento = application as Momento)
	lateinit var activityState: BucketItemActivity.ActivityState

	var isNew by Delegates.notNull<Boolean>()

	val status: MutableState<Status> = mutableStateOf(Status.INIT)

	lateinit var bucketItemType: BucketItemType
	lateinit var bucketKey: String
	var bucketItemKey = mutableStateOf<String?>(null)

	var bucketItemDbEntry = mutableStateOf<BucketItemDbEntry?>(null)
	val bucketItemDbEntryFlow = MutableStateFlow<BucketItemDbEntry?>(null)
	var bucketItem = mutableStateOf<BucketItem?>(null)

	var bookData = mutableStateOf<BookData?>(null)
	var showData = mutableStateOf<ShowData?>(null)
	val tvData = mutableStateOf<TvData?>(null)
	val movieData = mutableStateOf<MovieData?>(null)

	var thumbnail = mutableStateOf<Bitmap?>(null)
	var thoughtList: SnapshotStateList<String> = mutableStateListOf()

	fun emitBucketItemDbEntry() =
		viewModelScope.launch(Dispatchers.IO) { bucketItemDbEntryFlow.emit(bucketItemDbEntry.value) }

	fun putItem() {
		viewModelScope.launch(Dispatchers.IO) {
			when (bucketItemType) {
				BucketItemType.TODO -> null
				BucketItemType.BOOKS -> {
					bucketItemKey.value = bucketItemDbEntry.value!!.key
					bucketItemDbEntry.value!!.apply {
						this.title = bookData.value?.title
						this.thumbnail = this@BucketItemViewModel.thumbnail.value
						BucketItem(
							thoughtList = thoughtList,
							extra = objectMapper.writeValueAsString(bookData.value)
						).also { this.data = it }

						bucketRepository.putBucketItem(bucketItemDbEntry = this)
						getFromDatabase()
					}
				}
				BucketItemType.SHOWS -> {
					bucketItemKey.value = bucketItemDbEntry.value!!.key
					bucketItemDbEntry.value!!.apply {
						this.title = showData.value?.title
						this.thumbnail = this@BucketItemViewModel.thumbnail.value
						BucketItem(
							thoughtList = thoughtList,
							extra = when (showData.value!!.showType) {
								ShowType.TV -> objectMapper.writeValueAsString(tvData.value)
								ShowType.MOVIE -> objectMapper.writeValueAsString(movieData.value)
							}
						).also { this.data = it }

						bucketRepository.putBucketItem(bucketItemDbEntry = this)
						getFromDatabase()
					}
				}
			}

			emitBucketItemDbEntry()
		}
	}

	fun getItem(intent: Intent) {
		status.value = Status.LOADING
		viewModelScope.launch(Dispatchers.IO) {
			if (isNew) {
				try {
					when (bucketItemType) {
						BucketItemType.TODO -> null
						BucketItemType.BOOKS -> {
							bookData.value =
								intent.getSerializableExtra(Konstant.Companion.Konstant.BUCKET_ITEM_DATA.name) as BookData
							getThumbnail()
						}
						BucketItemType.SHOWS -> {
							showData.value =
								intent.getSerializableExtra(Konstant.Companion.Konstant.BUCKET_ITEM_DATA.name) as ShowData
							when (showData.value!!.showType) {
								ShowType.TV -> downloadTvData(id = showData.value!!.id)
								ShowType.MOVIE -> downloadMovieData(id = showData.value!!.id)
							}
							getThumbnail()
						}
					}

					bucketItemDbEntry.value = BucketItemDbEntry(
						key = generatePrimaryKey(),
						bucketKey = bucketKey,
						bucketItemType = bucketItemType,
						createdTimestamp = System.currentTimeMillis()
					).apply {
						this.modifiedTimestamp = this.createdTimestamp
						this.state = BucketItemState.ALPHA
					}

					emitBucketItemDbEntry()
					status.value = Status.LOADED
				} catch (exception: Exception) {
					status.value = Status.ERROR
					exception.printStackTrace()
				}
			} else {
				getFromDatabase()
			}
		}
	}

	fun updateItem() {
		viewModelScope.launch(Dispatchers.IO) {
			when (bucketItemType) {
				BucketItemType.TODO -> {
				}
				BucketItemType.BOOKS -> {
					if (bucketItemDbEntry.value!!.key.isNotEmpty()) {
						if (bucketItemDbEntry.value!!.data == null) {
							putItem()
						} else {
							bucketItemDbEntry.value!!.data!!.thoughtList.clear()
							bucketItemDbEntry.value!!.data!!.thoughtList.addAll(thoughtList)
						}
						bucketRepository.updateBucketItem(bucketItemDbEntry = bucketItemDbEntry.value!!)
					}
				}
				BucketItemType.SHOWS -> {
					if (bucketItemDbEntry.value!!.key.isNotEmpty()) {
						if (bucketItemDbEntry.value!!.data == null) {
							putItem()
						} else {
							bucketItemDbEntry.value!!.data!!.thoughtList.clear()
							bucketItemDbEntry.value!!.data!!.thoughtList.addAll(thoughtList)
						}
						bucketRepository.updateBucketItem(bucketItemDbEntry = bucketItemDbEntry.value!!)
					}
				}
			}

			emitBucketItemDbEntry()
		}
	}

	private suspend fun getFromDatabase() {
		try {
			bucketRepository.getBucketItem(bucketItemKey = bucketItemKey.value!!).collect {
				if (it == null) {
					status.value = Status.ERROR
				} else {
					bucketItemDbEntry.value = it
					thumbnail.value = bucketItemDbEntry.value?.thumbnail
					bucketItem.value = bucketItemDbEntry.value?.data

					when (it.bucketItemType) {
						BucketItemType.TODO -> null
						BucketItemType.BOOKS -> {
							bookData.value =
								bucketItem.value?.extra?.let { objectMapper.readValue(it as String) }
						}
						BucketItemType.SHOWS -> {
							if (bucketItem.value?.extra != null) {
								val jsonObject = JSONObject(bucketItem.value?.extra as String)
								showData.value = when (jsonObject.optString("showType")) {
									"TV" -> {
										tvData.value =
											bucketItem.value?.extra?.let { objectMapper.readValue(it as String) }
										ShowData(
											id = tvData.value!!.id,
											showType = ShowType.TV,
											title = tvData.value!!.name,
											posterPath = tvData.value!!.posterPath,
											releaseDate = tvData.value!!.firstAirDate
										)
									}
									"MOVIE" -> {
										movieData.value =
											bucketItem.value?.extra?.let { objectMapper.readValue(it as String) }
										ShowData(
											id = movieData.value!!.id,
											showType = ShowType.MOVIE,
											title = movieData.value!!.title,
											posterPath = movieData.value!!.posterPath,
											releaseDate = movieData.value!!.releaseDate
										)
									}
									else -> null
								}
							}
						}
					}

					if (!bucketItem.value?.thoughtList.isNullOrEmpty()) {
						thoughtList.clear()
						thoughtList.addAll(bucketItem.value?.thoughtList!!)
					}

					emitBucketItemDbEntry()
					status.value = Status.LOADED
				}
			}

		} catch (exception: Exception) {
			status.value = Status.ERROR
			exception.printStackTrace()
		}
	}

	private fun downloadTvData(id: String) {
		val requestQueue = Volley.newRequestQueue(getApplication<Momento>().applicationContext)
		val tvDataUrl =
			"https://api.themoviedb.org/3/tv/$id?api_key=${Secret.TMDB_KEY}&language=en-US&query="

		StringRequest(
			Request.Method.GET,
			tvDataUrl,
			{ requestResult ->
				val tvDataJson = JSONObject(requestResult)
				tvDataJson.apply {
					val episodeRunTimeList: MutableList<Int> = mutableListOf()
					optJSONArray("episode_run_time")?.apply {
						for (i in 0 until length()) {
							episodeRunTimeList.add(getInt(i))
						}
					}

					val genreIdList: MutableList<Int> = mutableListOf()
					optJSONArray("genres")?.apply {
						for (i in 0 until length()) {
							genreIdList.add(getJSONObject(i).getInt("id"))
						}
					}

					TvData(
						adult = optBoolean("adult"),
						backdropPath = optString("backdrop_path"),
						episodeRunTime = episodeRunTimeList,
						firstAirDate = optString("first_air_date"),
						genreIds = genreIdList,
						homepage = optString("homepage"),
						id = optString("id"),
						inProduction = optBoolean("in_production"),
						name = optString("name"),
						noEpisode = optInt("number_of_episodes"),
						noSeason = optInt("number_of_seasons"),
						originalLanguage = optString("original_language"),
						overview = optString("overview"),
						popularity = optDouble("popularity"),
						posterPath = optString("poster_path"),
						showType = ShowType.TV,
						status = optString("statue"),
						tagline = optString("tagline"),
						type = optString("type"),
						voteAverage = optDouble("vote_average"),
						voteCount = optInt("vote_count"),
					).apply { tvData.value = this }
				}
			},
			{
				it.printStackTrace()
			}
		).apply { requestQueue.add(this) }
	}

	private fun downloadMovieData(id: String) {
		val requestQueue = Volley.newRequestQueue(getApplication<Momento>().applicationContext)
		val movieDataUrl =
			"https://api.themoviedb.org/3/movie/$id?api_key=${Secret.TMDB_KEY}&language=en-US&query="

		StringRequest(
			Request.Method.GET,
			movieDataUrl,
			{ requestResult ->
				val movieDataJson = JSONObject(requestResult)
				movieDataJson.apply {
					val genreIdList: MutableList<Int> = mutableListOf()
					optJSONArray("genres")?.apply {
						for (i in 0 until length()) {
							genreIdList.add(getJSONObject(i).getInt("id"))
						}
					}

					MovieData(
						adult = optBoolean("adult"),
						backdropPath = optString("backdrop_path"),
						genreIds = genreIdList,
						homepage = optString("homepage"),
						id = optString("id"),
						imdbId = optString("imdb_id"),
						originalLanguage = optString("original_language"),
						originalTitle = optString("original_title"),
						overview = optString("overview"),
						popularity = optDouble("popularity"),
						posterPath = optString("poster_path"),
						releaseDate = optString("release_date"),
						runtime = optInt("runtime"),
						showType = ShowType.MOVIE,
						status = optString("statue"),
						tagline = optString("tagline"),
						title = optString("title"),
						voteAverage = optDouble("vote_average"),
						voteCount = optInt("vote_count"),
					).apply { movieData.value = this }
				}
			},
			{
				it.printStackTrace()
			}
		).apply { requestQueue.add(this) }
	}

	private suspend fun getThumbnail() {
		if (isNew) {
			when (bucketItemType) {
				BucketItemType.TODO -> null
				BucketItemType.BOOKS -> {
					val request = ImageRequest.Builder(getApplication())
						.data("https://covers.openlibrary.org/b/id/${bookData.value!!.coverI}-M.jpg")
						.target { thumbnail.value = it.toBitmap() }
						.build()

					val imageLoader = ImageLoader.Builder(getApplication()).build()

					imageLoader.execute(request = request)
				}
				BucketItemType.SHOWS -> {
					val request = ImageRequest.Builder(getApplication())
						.data("https://image.tmdb.org/t/p/w500${showData.value!!.posterPath}")
						.target { thumbnail.value = it.toBitmap() }
						.build()

					val imageLoader = ImageLoader.Builder(getApplication()).build()

					imageLoader.execute(request = request)
				}
			}
		} else {
			thumbnail.value = bucketItemDbEntry.value?.thumbnail
		}
	}
}
