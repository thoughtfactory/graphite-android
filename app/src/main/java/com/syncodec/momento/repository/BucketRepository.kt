package com.syncodec.momento.repository

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.syncodec.momento.Momento
import com.syncodec.momento.bucketComponent.modalBottomSheet.ShowType
import com.syncodec.momento.database.UserDatabase
import com.syncodec.momento.database.bucket.*
import com.syncodec.momento.konstant.Secret
import com.syncodec.momento.miscellaneous.generatePrimaryKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONObject
import javax.inject.Singleton

@Singleton
class BucketRepository(val momento: Momento) {

	private val objectMapper: ObjectMapper = ObjectMapper().registerModule(KotlinModule())

	val bucketDbTableDao: BucketDbTableDao = UserDatabase.getInstance(momento).bucketDbTableDao
	val bucketItemDbTableDao: BucketItemDbTableDao = UserDatabase.getInstance(momento).bucketItemDbTableDao

	var bucketList: LiveData<List<BucketDbEntry>> = bucketDbTableDao.getAllAsLiveData()

	fun getBucketItemListAsFlow(bucketKey: String): Flow<List<BucketItemDbEntry>> {
		return bucketItemDbTableDao.getFromBucketAsFlow(bucketKey = bucketKey)
	}

	suspend fun getAllBucketDbEntry(bucketKey: String): List<BucketItemDbEntry> {
		return bucketItemDbTableDao.getAllBucketItem(bucketKey = bucketKey)
	}

	fun getBucket(bucketKey: String): Flow<BucketDbEntry?> {
		return bucketDbTableDao.getAsFlow(key = bucketKey)
	}

	fun getBucketItem(bucketKey: String, bucketItemKey: String): Pair<Flow<BucketItemDbEntry?>, String?> {
		return Pair(
			bucketItemDbTableDao.getAsLiveData(key = bucketItemKey),
			momento.getBucketItemData(bucketKey = bucketKey, bucketItemKey = bucketItemKey)
		)
	}

	suspend fun deleteBucketItem(bucketKey: String, keyList: List<String>) {
		keyList.forEach {
			bucketItemDbTableDao.delete(it)
			momento.deleteBucketItem(bucketKey = bucketKey, bucketItemKey = it)
		}
	}

	suspend fun putNewBucket(bucketType: BucketItemType.Type, title: String) {
		withContext(Dispatchers.IO) {
			val currentTimestamp = System.currentTimeMillis()
			BucketDbEntry(
				key = generatePrimaryKey(),
				bucketType = bucketType.ordinal
			).apply {
				this.createdTimestamp = currentTimestamp
				this.modifiedTimestamp = currentTimestamp
				this.title = title
				this.contentThumbnail = null
				this.isArchived = false
				this.isFavourite = false
				this.isLocked = false

				bucketDbTableDao.insert(bucketDbEntry = this)
			}
		}
	}

	fun putBucket(bucketDbEntry: BucketDbEntry) = bucketDbTableDao.update(bucketDbEntry = bucketDbEntry)

	fun putBucketItem(
		bucketItemDbEntry: BucketItemDbEntry,
		thoughtList: List<String>,
		data: Any?
	) {
		bucketItemDbTableDao.insert(bucketItemDbEntry = bucketItemDbEntry)

		momento.putBucketItemData(
			bucketKey = bucketItemDbEntry.bucketKey,
			bucketItemKey = bucketItemDbEntry.key,
			jsonString = objectMapper.writeValueAsString(data),
		)

		momento.putThought(
			bucketKey = bucketItemDbEntry.bucketKey,
			bucketItemKey = bucketItemDbEntry.key,
			thoughtList = thoughtList
		)
	}

	suspend fun updateBucketItem(bucketItemDbEntry: BucketItemDbEntry) =
		withContext(Dispatchers.IO) { bucketItemDbTableDao.update(bucketItemDbEntry = bucketItemDbEntry) }

	suspend fun putThought(
		bucketKey: String,
		bucketItemKey: String,
		thoughtList: List<String>
	) {
		withContext(Dispatchers.IO) {
			momento.putThought(
				bucketKey = bucketKey,
				bucketItemKey = bucketItemKey,
				thoughtList = thoughtList
			)
		}
	}

	fun getThought(
		bucketKey: String,
		bucketItemKey: String
	): List<String> {
		return momento.getThought(bucketKey = bucketKey, bucketItemKey = bucketItemKey)
	}

	fun getBucketItemThumbnail(
		bucketKey: String,
		bucketItemKey: String
	): String? {
		return momento.getBucketItemThumbnail(
			bucketKey = bucketKey,
			bucketItemKey = bucketItemKey
		)
	}

	//	note    Why tvData is here? So that it value can be updated after downloading data
	var tvData: MutableState<TvData?> = mutableStateOf(null)
	fun downloadTvData(id: String) {
		val requestQueue = Volley.newRequestQueue(momento.applicationContext)
		val tvDataUrl =
			"https://api.themoviedb.org/3/tv/$id?api_key=daf55a105cab241cd55fa75413656db4&language=en-US${Secret.TMDB_KEY}&language=en-US&query="

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

	var movieData: MutableState<MovieData?> = mutableStateOf(null)
	fun downloadMovieData(id: String) {
		val requestQueue = Volley.newRequestQueue(momento.applicationContext)
		val movieDataUrl =
			"https://api.themoviedb.org/3/movie/$id?api_key=daf55a105cab241cd55fa75413656db4&language=en-US${Secret.TMDB_KEY}&language=en-US&query="

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

	companion object {
		private var INSTANCE: BucketRepository? = null

		fun getInstance(momento: Momento): BucketRepository {
			synchronized(lock = this) {
				var instance = INSTANCE
				if (instance == null) {
					instance = BucketRepository(momento = momento)
					INSTANCE = instance
				}
				return instance
			}
		}
	}
}
