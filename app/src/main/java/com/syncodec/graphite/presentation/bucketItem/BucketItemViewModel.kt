package com.syncodec.graphite.presentation.bucketItem

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.android.gms.common.util.Base64Utils
import com.syncodec.graphite.di.Repository
import com.syncodec.graphite.di.model.BucketItemObject
import com.syncodec.graphite.di.model.BucketItemState
import com.syncodec.graphite.di.model.BucketType
import com.syncodec.graphite.di.network.BookData
import com.syncodec.graphite.di.network.MovieData
import com.syncodec.graphite.di.network.ShowType
import com.syncodec.graphite.utils.Status
import com.syncodec.graphite.utils.toByteArray
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.Serializable


class BucketItemViewModel : ViewModel() {

	private val objectMapper = jsonMapper { addModule(kotlinModule()) }.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

	var isNew: MutableState<Boolean> = mutableStateOf(false)
	var isSaved: MutableState<Boolean> = mutableStateOf(false)
	var bucketId: MutableState<String?> = mutableStateOf(null)
	var bucketType: MutableState<BucketType?> = mutableStateOf(null)
	var bucketItemId: MutableState<String?> = mutableStateOf(null)

	var status: MutableState<Status> = mutableStateOf(Status.INIT)

	var thumbnail: MutableState<Bitmap?> = mutableStateOf(null)

	//	Book data
	var bookKey: MutableState<String?> = mutableStateOf(null)
	var bookTitle: MutableState<String?> = mutableStateOf(null)
	var coverI: MutableState<String?> = mutableStateOf(null)
	var authorList: SnapshotStateList<String?> = mutableStateListOf()
	var firstPublishedYear: MutableState<Int?> = mutableStateOf(null)
	var description: MutableState<String?> = mutableStateOf(null)

	//	Movie data
	var movieId: MutableState<String?> = mutableStateOf(null)
	val adult: MutableState<Boolean> = mutableStateOf(false)
	val backdropPath: MutableState<String?> = mutableStateOf(null)
	val genreIds: SnapshotStateList<Int?> = mutableStateListOf()
	val movieTitle: MutableState<String?> = mutableStateOf(null)
	val movieTagline: MutableState<String?> = mutableStateOf(null)
	val originalLanguage: MutableState<String?> = mutableStateOf(null)
	val originalTitle: MutableState<String?> = mutableStateOf(null)
	val overview: MutableState<String?> = mutableStateOf(null)
	val popularity: MutableState<Double?> = mutableStateOf(null)
	val posterPath: MutableState<String?> = mutableStateOf(null)
	val releaseDate: MutableState<String?> = mutableStateOf(null)
	val runtime: MutableState<Int?> = mutableStateOf(null)
	val video: MutableState<Boolean> = mutableStateOf(false)
	val voteAverage: MutableState<Double?> = mutableStateOf(null)
	val voteCount: MutableState<Int?> = mutableStateOf(null)

	var state: MutableState<BucketItemState> = mutableStateOf(BucketItemState.ALPHA)
	var isFavourite: MutableState<Boolean> = mutableStateOf(false)
	var isLocked: MutableState<Boolean> = mutableStateOf(false)

	fun initNewData(bucketId: String, bucketType: BucketType, itemKey: String, extraData: Serializable?, showType: ShowType?) {
		isNew.value = true
		this.bucketId.value = bucketId
		this.bucketType.value = bucketType

		when (bucketType) {
			BucketType.TODO -> null
			BucketType.BOOK -> {
				this.bookKey.value = itemKey
				retrieveBookData(key = itemKey, extraData)
			}
			BucketType.SHOW -> {
				this.movieId.value = itemKey
				when (showType) {
					ShowType.MOVIE -> retrieveMovieData(itemKey, extraData)
					ShowType.TV -> retrieveTvData(itemKey, extraData)
				}
			}
			BucketType.LINK -> null
		}
	}

	fun loadAndViewData(bucketId: String, bucketType: BucketType, bucketItemId: String) {
		this.bucketId.value = bucketId
		this.bucketType.value = bucketType
		this.bucketItemId.value = bucketItemId

		viewModelScope.launch(Dispatchers.IO) {
			Repository.getBucketItemAsFlow(id = bucketItemId).collect {
				val bucketItem = it
				if (bucketItem == null) {
					status.value = Status.ERROR
				} else {
					try {
						state.value = BucketItemState.valueOf(bucketItem.state)
					} catch (e: Exception) {
						state.value = BucketItemState.ALPHA
					}

					try {
						thumbnail.value = bucketItem.thumbnail?.let { it1 ->
							val thumbnailByteArray = Base64Utils.decode(it1)
							BitmapFactory.decodeByteArray(thumbnailByteArray, 0, thumbnailByteArray.size)
						}
					} catch (e: Exception) {
						thumbnail.value = null
					}

					when(bucketType) {
						BucketType.TODO -> null
						BucketType.BOOK -> loadAndViewBookData(bucketItem = bucketItem)
						BucketType.SHOW -> loadAndViewShowData(bucketItem = bucketItem)
						BucketType.LINK -> null
					}
				}
			}
		}
	}

	private fun loadAndViewBookData(bucketItem: BucketItemObject) {
		if (bucketItem.item == null) {
			status.value = Status.ERROR
		} else {
			try {
				val bookData = objectMapper.readValue<BookData>(bucketItem.item!!)
				bookKey.value = bookData.key
				bookTitle.value = bookData.title
				coverI.value = bookData.coverI
				authorList.clear()
				authorList.addAll(bookData.authorList ?: listOf())
				firstPublishedYear.value = bookData.firstPublishedYear
				description.value = bookData.description

				status.value = Status.LOADED
			} catch (e: Exception) {
				status.value = Status.ERROR
				e.printStackTrace()
			}
		}
	}

	private fun loadAndViewShowData(bucketItem: BucketItemObject) {
		if (bucketItem.item == null) {
			status.value = Status.ERROR
		} else {
			try {
				val showData = objectMapper.readValue<MovieData>(bucketItem.item!!)
				movieId.value = showData.id
				adult.value = showData.adult
				backdropPath.value = showData.backdropPath
				genreIds.clear()
				genreIds.addAll(showData.genreIds ?: listOf())
				movieTitle.value = showData.title
				movieTagline.value = showData.tagline
				originalLanguage.value = showData.originalLanguage
				originalTitle.value = showData.originalTitle
				overview.value = showData.overview
				popularity.value = showData.popularity
				posterPath.value = showData.posterPath
				releaseDate.value = showData.releaseDate
				runtime.value = showData.runtime
				video.value = showData.video
				voteAverage.value = showData.voteAverage
				voteCount.value = showData.voteCount

				status.value = Status.LOADED
			} catch (e: Exception) {
				status.value = Status.ERROR
				e.printStackTrace()
			}
		}
	}

	fun putBucketItem() {
		when {
			this@BucketItemViewModel.bucketId.value == null -> null
			this@BucketItemViewModel.bucketType.value == null -> null
			else -> when (bucketType.value) {
				BucketType.TODO -> null
				BucketType.BOOK -> putBookBucketItem()
				BucketType.SHOW -> putMovieBucketItem()
				BucketType.LINK -> null
//			    TODO Show error message if else
				else -> null
			}
		}
	}

	private fun putBookBucketItem() {
		CoroutineScope(Dispatchers.IO).launch {
			when {
				this@BucketItemViewModel.bookKey.value.isNullOrBlank() -> null
				this@BucketItemViewModel.bookTitle.value.isNullOrBlank() -> null
				else -> BucketItemObject().apply {
					try {
						val bookData = BookData(
							key = this@BucketItemViewModel.bookKey.value,
							title = this@BucketItemViewModel.bookTitle.value,
							coverI = this@BucketItemViewModel.coverI.value,
							authorList = this@BucketItemViewModel.authorList,
							firstPublishedYear = this@BucketItemViewModel.firstPublishedYear.value,
							description = this@BucketItemViewModel.description.value
						)

						this.bucketType = this@BucketItemViewModel.bucketType.value!!.name
						this.title = this@BucketItemViewModel.bookTitle.value!!
						this.state = this@BucketItemViewModel.state.value.name
						this.thumbnail = this@BucketItemViewModel.thumbnail.value?.toByteArray()?.let { Base64Utils.encode(it) }
						this.item = objectMapper.writeValueAsString(bookData)
						this.isFavourite = this@BucketItemViewModel.isFavourite.value
						this.isLocked = this@BucketItemViewModel.isLocked.value

						bucketId.value?.let { Repository.putBucketItem(it, this) }
						this@BucketItemViewModel.isSaved.value = true
					} catch (e: Exception) {
//						TODO Show error message
						e.printStackTrace()
					}
				}
			}
		}
	}

	private fun putMovieBucketItem() {
		CoroutineScope(Dispatchers.IO).launch {
			try {
				when {
					this@BucketItemViewModel.movieId.value.isNullOrBlank() -> null
					this@BucketItemViewModel.movieTitle.value.isNullOrBlank() -> null
					else -> BucketItemObject().apply {
						val movieData = MovieData(
							adult = this@BucketItemViewModel.adult.value,
							backdropPath = this@BucketItemViewModel.backdropPath.value,
							genreIds = this@BucketItemViewModel.genreIds,
							id = this@BucketItemViewModel.movieId.value,
							originalLanguage = this@BucketItemViewModel.originalLanguage.value,
							originalTitle = this@BucketItemViewModel.originalTitle.value,
							overview = this@BucketItemViewModel.overview.value,
							popularity = this@BucketItemViewModel.popularity.value,
							posterPath = this@BucketItemViewModel.posterPath.value,
							releaseDate = this@BucketItemViewModel.releaseDate.value,
							runtime = this@BucketItemViewModel.runtime.value,
							tagline = this@BucketItemViewModel.movieTagline.value,
							title = this@BucketItemViewModel.movieTitle.value,
							video = this@BucketItemViewModel.video.value,
							voteAverage = this@BucketItemViewModel.voteAverage.value,
							voteCount = this@BucketItemViewModel.voteCount.value
						)

						this.bucketType = this@BucketItemViewModel.bucketType.value!!.name
						this.title = this@BucketItemViewModel.movieTitle.value!!
						this.state = this@BucketItemViewModel.state.value.name
						this.thumbnail = this@BucketItemViewModel.thumbnail.value?.toByteArray()?.let { Base64Utils.encode(it) }
						this.item = objectMapper.writeValueAsString(movieData)
						this.isFavourite = this@BucketItemViewModel.isFavourite.value
						this.isLocked = this@BucketItemViewModel.isLocked.value

						bucketId.value?.let { Repository.putBucketItem(it, this) }

						this@BucketItemViewModel.isSaved.value = true
					}
				}
			} catch (e: Exception) {
//				TODO Show error message
				e.printStackTrace()
			}
		}
	}

	private fun retrieveBookData(key: String, extraData: Serializable?) {
		if (extraData == null) {
			status.value = Status.ERROR
		} else {
			try {
				val bookData = extraData as BookData
				bookTitle.value = bookData.title
				coverI.value = bookData.coverI
				authorList.clear()
				authorList.addAll(bookData.authorList ?: listOf())
				firstPublishedYear.value = bookData.firstPublishedYear

				status.value = Status.LOADED

				viewModelScope.launch(Dispatchers.IO) {
					try {
						Repository.openLibraryApi.retrieveDataFromKey(key) { response ->
							if (response == null) {
//								TODO Show msg
							} else {
								response.body?.string()?.let {
									val jsonObject = JSONObject(it)
									description.value =
										jsonObject.getJSONObject("description").getString("value")
								}
							}
						}
					} catch (e: Exception) {
//						TODO    Show description not retrieved message
						e.printStackTrace()
					}
					try {
						getBookThumbnail(coverI = coverI.value)
					} catch (e: Exception) {
//						TODO    Show thumbnail not retrieved message
						e.printStackTrace()
					}
				}
			} catch (e: Exception) {
				e.printStackTrace()
				status.value = Status.ERROR
			}
		}
	}

	private fun retrieveMovieData(id: String, extraData: Serializable?) {
		if (extraData == null) {
			status.value = Status.ERROR
		} else {
			try {
				val movieData = extraData as MovieData
				movieTitle.value = movieData.title
				adult.value = movieData.adult
				backdropPath.value = movieData.backdropPath
				genreIds.clear()
				genreIds.addAll(movieData.genreIds ?: listOf())
				originalLanguage.value = movieData.originalLanguage
				originalTitle.value = movieData.originalTitle
				overview.value = movieData.overview
				popularity.value = movieData.popularity
				posterPath.value = movieData.posterPath
				releaseDate.value = movieData.releaseDate
				movieTitle.value = movieData.title
				video.value = movieData.video
				voteAverage.value = movieData.voteAverage
				voteCount.value = movieData.voteCount

				status.value = Status.LOADED

				viewModelScope.launch(Dispatchers.IO) {
					try {
						Repository.tmDbApi.retrieveMovieDataFromId(id = id) { response ->
							if (response == null) {
//								TODO Show msg
							} else {
								response.body?.string()?.let {
									val jsonObject = JSONObject(it)
									movieTagline.value = jsonObject.optString("tagline")
									runtime.value = jsonObject.optInt("runtime")
								}
							}
						}
					} catch (e: Exception) {
//						TODO    Show description not retrieved message
						e.printStackTrace()
					}
					try {
						getMovieThumbnail(posterPath = movieData.posterPath)
					} catch (e: Exception) {
//						TODO    Show thumbnail not retrieved message
						e.printStackTrace()
					}
				}
			} catch (e: Exception) {
				e.printStackTrace()
				status.value = Status.ERROR
			}
		}
	}

	private fun retrieveTvData(id: String, extraData: Serializable?) {
		if (extraData == null) {
			status.value = Status.ERROR
		} else {
			try {
				val movieData = extraData as MovieData
				movieTitle.value = movieData.title
				adult.value = movieData.adult
				backdropPath.value = movieData.backdropPath
				genreIds.clear()
				genreIds.addAll(movieData.genreIds ?: listOf())
				originalLanguage.value = movieData.originalLanguage
				originalTitle.value = movieData.originalTitle
				overview.value = movieData.overview
				popularity.value = movieData.popularity
				posterPath.value = movieData.posterPath
				releaseDate.value = movieData.releaseDate
				movieTitle.value = movieData.title
				video.value = movieData.video
				voteAverage.value = movieData.voteAverage
				voteCount.value = movieData.voteCount

				status.value = Status.LOADED

				viewModelScope.launch(Dispatchers.IO) {
					try {
						Repository.tmDbApi.retrieveMovieDataFromId(id = id) { response ->
							if (response == null) {
//								TODO Show msg
							} else {
								response.body?.string()?.let {
									val jsonObject = JSONObject(it)
									movieTagline.value = jsonObject.optString("tagline")
									runtime.value = jsonObject.optInt("runtime")
								}
							}
						}
					} catch (e: Exception) {
//						TODO    Show description not retrieved message
						e.printStackTrace()
					}
					try {
						getMovieThumbnail(posterPath = movieData.posterPath)
					} catch (e: Exception) {
//						TODO    Show thumbnail not retrieved message
						e.printStackTrace()
					}
				}
			} catch (e: Exception) {
				e.printStackTrace()
				status.value = Status.ERROR
			}
		}
	}

	private fun getBookThumbnail(coverI: String?) {
		if (coverI != null) {
			viewModelScope.launch(Dispatchers.IO) {
				try {
					Repository.openLibraryApi.retrieveBookCover(coverI = coverI) { response ->
						if (response == null) {
//							TODO Show message
						} else thumbnail.value = BitmapFactory.decodeStream(response.body?.byteStream())
					}
				} catch (e: Exception) {
//					TODO Show message
					e.printStackTrace()
				}
			}
		}
	}

	private fun getMovieThumbnail(posterPath: String?) {
		if (posterPath != null) {
			viewModelScope.launch(Dispatchers.IO) {
				try {
					Repository.tmDbApi.retrieveMoviePoster(posterPath = posterPath) { response ->
						if (response == null) {
//							TODO Show message
						} else thumbnail.value = BitmapFactory.decodeStream(response.body?.byteStream())
					}
				} catch (e: Exception) {
//					TODO Show message
					e.printStackTrace()
				}
			}
		}
	}
}
