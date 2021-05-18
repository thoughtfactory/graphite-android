package com.syncodec.momento.repository

import android.graphics.Bitmap
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.syncodec.momento.Momento
import com.syncodec.momento.bucketComponent.modalBottomSheet.BookData
import com.syncodec.momento.bucketComponent.modalBottomSheet.ShowType
import com.syncodec.momento.database.UserDatabase
import com.syncodec.momento.database.bucket.*
import com.syncodec.momento.konstant.Secret
import com.syncodec.momento.miscellaneous.downloadImage
import com.syncodec.momento.miscellaneous.generatePrimaryKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONObject
import javax.inject.Singleton

@Singleton
class BucketRepository(val momento: Momento) {

	private val objectMapper: ObjectMapper = ObjectMapper().registerModule(KotlinModule())

	private val bucketDbTableDao: BucketDbTableDao = UserDatabase.getInstance(momento).bucketDbTableDao
	private val bucketItemTableDao: BucketItemTableDao = UserDatabase.getInstance(momento).bucketItemTableDao

	fun getBucketListAsLiveData(): LiveData<List<BucketDbEntry>> {
		return bucketDbTableDao.getAllAsLiveData()
	}

	fun getBucketItemListAsLiveData(bucketKey: String): Flow<List<BucketItemDbEntry>> {
		return bucketItemTableDao.getAllAsLiveData(bucketKey = bucketKey)
	}

	suspend fun getAllBucketDbEntry(bucketKey: String): List<BucketItemDbEntry> {
		return bucketItemTableDao.getAllBucketItem(bucketKey = bucketKey)
	}

	suspend fun getBucket(bucketKey: String): BucketDbEntry? {
		return bucketDbTableDao.get(primaryKey = bucketKey)
	}

	suspend fun getBucketItem(bucketItemKey: String): BucketItemDbEntry? {
		return bucketItemTableDao.get(primaryKey = bucketItemKey)
	}

	suspend fun deleteBucketItem(bucketKey: String, keyList: List<String>) {
		keyList.forEach {
			bucketItemTableDao.delete(it)
			momento.deleteBucketItem(bucketKey = bucketKey, bucketItemKey = it)
		}
	}

	suspend fun putBucket(bucketType: BucketItemType.Type, title: String) {
		withContext(Dispatchers.IO) {
			val currentTimestamp = System.currentTimeMillis()
			BucketDbEntry(
				primaryKey = generatePrimaryKey(),
				bucketType = bucketType.ordinal
			).apply {
				this.createdTimestamp = currentTimestamp
				this.modifiedTimestamp = currentTimestamp
				this.title = title
				this.containerSize = 0
				this.contentThumbnail = null
				this.isArchived = false
				this.isFavourite = false
				this.isLocked = false

				bucketDbTableDao.insert(bucketDbEntry = this)
			}
		}
	}

	fun putBucketItem(
		bucketKey: String,
		bucketItemKey: String?,
		bucketItemType: BucketItemType.Type,
		title: String,
		state: Int,
		thoughtList: List<String>,
		data: Any
	): String {
		val currentTimestamp = System.currentTimeMillis()

		val bucketItemDbEntry = BucketItemDbEntry(
			key = bucketItemKey ?: generatePrimaryKey(),
			bucketKey = bucketKey,
			bucketItemType = bucketItemType.ordinal,
			createdTimestamp = currentTimestamp
		).apply {
			this.modifiedTimestamp = currentTimestamp
			this.title = title
			this.state = state
			this.isFavourite = false
			this.isArchived = false
			this.isLocked = false
		}
		bucketItemTableDao.insert(bucketItemDbEntry = bucketItemDbEntry)

		val thumbnail: Bitmap? = when (bucketItemType) {
			BucketItemType.Type.TODO -> null
			BucketItemType.Type.BOOKS -> {
				try {
					data as BookData
					if (data.coverI != null) {
						downloadImage(thumbnailUrl = "https://covers.openlibrary.org/b/id/${data.coverI}-M.jpg")
					} else null
				} catch (exception: Exception) {
					null
				}
			}
			BucketItemType.Type.SHOWS -> {
				try {
					data as TvData
					if (data.posterPath != null) {
						downloadImage(thumbnailUrl = "https://image.tmdb.org/t/p/w500${data.posterPath}")
					} else null
				} catch (exception: Exception) {
					null
				}
			}
			BucketItemType.Type.MEDIA -> null
			BucketItemType.Type.LINKS -> null
		}

		momento.putBucketItemData(
			bucketKey = bucketKey,
			bucketItemKey = bucketItemDbEntry.key,
			jsonString = objectMapper.writeValueAsString(data),
			thumbnail = thumbnail
		)

		momento.putThought(
			bucketKey = bucketKey,
			bucketItemKey = bucketItemDbEntry.key,
			thoughtList = thoughtList
		)

		return bucketItemDbEntry.key
	}

	suspend fun getBucketItemData(
		bucketKey: String,
		bucketItemKey: String
	): Pair<BucketItemDbEntry?, String> {
		return Pair(
			getBucketItem(bucketItemKey = bucketItemKey), momento.getBucketItemData(
				bucketKey = bucketKey,
				bucketItemKey = bucketItemKey
			)
		)
	}

	suspend fun updateBucketItem(
		bucketItemDbEntry: BucketItemDbEntry
	) {
		withContext(Dispatchers.IO) {
			bucketItemTableDao.insert(bucketItemDbEntry = bucketItemDbEntry)
		}
	}

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
					val creatorDataList: MutableList<CreatorData> = mutableListOf()
					optJSONArray("created_by")?.apply {
						for (i in 0 until length()) {
							optJSONObject(i).apply {
								CreatorData(
									id = optString("id"),
									creditId = optString("credit_id"),
									name = optString("name")
								).apply { creatorDataList.add(this) }
							}
						}
					}

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

					val seasonDataList: MutableList<SeasonData> = mutableListOf()
					optJSONArray("seasons")?.apply {
						for (i in 0 until length()) {
							optJSONObject(i).apply {
								SeasonData(
									airDate = optString("air_date"),
									noEpisode = optInt("episode_count"),
									id = optString("id"),
									name = optString("name"),
									overview = optString("overview"),
									posterPath = optString("poster_path"),
									seasonNo = optInt("season_number"),
								).apply { seasonDataList.add(this) }
							}
						}
					}

					TvData(
						adult = optBoolean("adult"),
						backdropPath = optString("backdrop_path"),
						creatorDataList = creatorDataList,
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
//						seasonDataList = seasonDataList,
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
