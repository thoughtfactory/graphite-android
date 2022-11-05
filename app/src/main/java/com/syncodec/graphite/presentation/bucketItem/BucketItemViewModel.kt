package com.syncodec.graphite.presentation.bucketItem

import android.content.Intent
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
import com.syncodec.graphite.di.model.BucketItemObject
import com.syncodec.graphite.di.model.BucketItemState
import com.syncodec.graphite.di.model.BucketType
import com.syncodec.graphite.di.network.BookData
import com.syncodec.graphite.di.network.Genre
import com.syncodec.graphite.di.network.MovieData
import com.syncodec.graphite.di.network.OpenLibraryApi
import com.syncodec.graphite.di.network.ShowData
import com.syncodec.graphite.di.network.ShowType
import com.syncodec.graphite.di.network.TMDbApi
import com.syncodec.graphite.di.network.TvData
import com.syncodec.graphite.di.repository.RealmNotInitializedException
import com.syncodec.graphite.di.repository.Repository2
import com.syncodec.graphite.di.repository.RepositoryState
import com.syncodec.graphite.utils.Extra
import com.syncodec.graphite.utils.Status
import com.syncodec.graphite.utils.decodeBase64ToBitmap
import com.syncodec.graphite.utils.encodeBase64
import com.syncodec.graphite.utils.serializable
import dagger.hilt.android.lifecycle.HiltViewModel
import io.realm.kotlin.types.ObjectId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class BucketItemViewModel @Inject constructor(private val repository2 : Repository2) : ViewModel() {

	val repositoryState = repository2.repositoryState

	val objectMapper = jsonMapper { addModule(kotlinModule()) }.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

	var isNew : MutableState<Boolean?> = mutableStateOf(null)
	var bucketId : MutableState<ObjectId?> = mutableStateOf(null)
	var bucketType : MutableState<BucketType?> = mutableStateOf(null)


	//	common
	var bucketItemObject : MutableState<BucketItemObject?> = mutableStateOf(null)
	val state : MutableState<BucketItemState?> = mutableStateOf(null)
	val showType : MutableState<ShowType?> = mutableStateOf(null)
	val thumbnail : MutableState<Bitmap?> = mutableStateOf(null)
	val isFavourite : MutableState<Boolean?> = mutableStateOf(null)
	val isLocked : MutableState<Boolean?> = mutableStateOf(null)


	//  Book
	var bookKey : MutableState<String?> = mutableStateOf(null)
	var bookTitle : MutableState<String?> = mutableStateOf(null)
	var bookCoverI : MutableState<String?> = mutableStateOf(null)
	var bookAuthorList : SnapshotStateList<String?> = mutableStateListOf()
	var bookDescription : MutableState<String?> = mutableStateOf(null)
	var bookPageCount : MutableState<Int?> = mutableStateOf(null)
	var bookFirstPublishYear : MutableState<String?> = mutableStateOf(null)

	//  Movie
	var movieId : MutableState<String?> = mutableStateOf(null)
	val movieAdult : MutableState<Boolean?> = mutableStateOf(false)
	val movieGenres : SnapshotStateList<Genre?> = mutableStateListOf()
	val movieHomepage : MutableState<String?> = mutableStateOf(null)
	val movieImdbId : MutableState<String?> = mutableStateOf(null)
	val movieOriginalLanguage : MutableState<String?> = mutableStateOf(null)
	val movieOriginalTitle : MutableState<String?> = mutableStateOf(null)
	val movieOverview : MutableState<String?> = mutableStateOf(null)
	val moviePosterPath : MutableState<String?> = mutableStateOf(null)
	val movieReleaseDate : MutableState<String?> = mutableStateOf(null)
	val movieRuntime : MutableState<Int?> = mutableStateOf(null)
	val movieTitle : MutableState<String?> = mutableStateOf(null)
	val movieTagline : MutableState<String?> = mutableStateOf(null)

	//	Tv
	val tvId : MutableState<String?> = mutableStateOf(null)
	val tvAdult : MutableState<Boolean?> = mutableStateOf(null)
	val tvFirstAirDate : MutableState<String?> = mutableStateOf(null)
	val tvGenres : SnapshotStateList<Genre?> = mutableStateListOf()
	val tvHomepage : MutableState<String?> = mutableStateOf(null)
	val tvNumberOfSeasons : MutableState<Int?> = mutableStateOf(null)
	val tvNumberOfEpisodes : MutableState<Int?> = mutableStateOf(null)
	val tvOriginalLanguage : MutableState<String?> = mutableStateOf(null)
	val tvName : MutableState<String?> = mutableStateOf(null)
	val tvOriginalName : MutableState<String?> = mutableStateOf(null)
	val tvOverview : MutableState<String?> = mutableStateOf(null)
	val tvPosterPath : MutableState<String?> = mutableStateOf(null)
	val tvTagline : MutableState<String?> = mutableStateOf(null)

	val status : MutableState<Status> = mutableStateOf(Status.INIT)

	fun initData(isNew : Boolean, bucketId : ObjectId, bucketItemId : ObjectId?, bucketType : BucketType, intent : Intent) {

		status.value = Status.LOADING

		this.isNew.value = isNew
		this.bucketId.value = bucketId
		this.bucketType.value = bucketType

		viewModelScope.launch(Dispatchers.IO) {
			when (repositoryState.value) {
				RepositoryState.INIT -> null
				RepositoryState.LOADING -> null
				RepositoryState.SUCCESS -> onRepositoryStateSuccess(bucketItemId, intent)
				RepositoryState.ERROR -> null
			}
		}
	}

	private fun onRepositoryStateSuccess(bucketItemId : ObjectId?, intent : Intent) {
		when (this.isNew.value) {
			true -> initBucketItem(intent = intent)
			false -> if (bucketItemId == null) {
				status.value = Status.ERROR
			} else {
				loadBucketItem(id = bucketItemId)
			}

			null -> null
		}
	}

	private fun initBucketItem(intent : Intent) {
		bucketItemObject.value = BucketItemObject().apply {
			this.bucketType = this@BucketItemViewModel.bucketType.value?.name ?: BucketType.UNKNOWN.name
			this.state = BucketItemState.ALPHA.name
			this.isFavourite = false
			this.isLocked = false
		}

		when (bucketType.value) {
			BucketType.TODO -> null
			BucketType.BOOK -> initNewBookItem(intent = intent)
			BucketType.SHOW -> initNewShowItem(intent = intent)
			BucketType.LINK -> null
			BucketType.UNKNOWN -> null
			null -> null
		}
	}

	private fun loadBucketItem(id : ObjectId) {
		viewModelScope.launch(Dispatchers.IO) {

			if (repositoryState.value != RepositoryState.SUCCESS) {
				status.value = Status.ERROR
				this.cancel()
			}

			try {
				repository2.getBucketItemAsFlow(id = id).collect {
					bucketItemObject.value = it
					thumbnail.value = it?.thumbnail?.decodeBase64ToBitmap()
					try {
						state.value = BucketItemState.valueOf(it?.state ?: BucketItemState.ALPHA.name)
					} catch (e : Exception) {
						state.value = BucketItemState.ALPHA
					}
					isFavourite.value = it?.isFavourite
					isLocked.value = it?.isLocked

					when (bucketType.value) {
						BucketType.TODO -> null
						BucketType.BOOK -> initBookItem(bookData = it?.getBookData())
						BucketType.SHOW -> initShowItem(showData = it?.getShowData())
						BucketType.LINK -> null
						BucketType.UNKNOWN -> null
						null -> null
					}
				}
			} catch (e : RealmNotInitializedException) {
				status.value = Status.ERROR
			} catch (e : Exception) {
				status.value = Status.ERROR
			}
		}
	}

	private fun initNewBookItem(intent : Intent) {
		val hasBookId = intent.hasExtra(Extra.Companion.Constant.BOOK_ID.name)
		val hasExtraData = intent.hasExtra(Extra.Companion.Constant.BUCKET_EXTRA_DATA.name)

		if (hasBookId && hasExtraData) {
			val bookId = intent.getStringExtra(Extra.Companion.Constant.BOOK_ID.name)
			val bookData = intent.serializable<BookData>(Extra.Companion.Constant.BUCKET_EXTRA_DATA.name)

			if (bookId != null) {
				viewModelScope.launch(Dispatchers.IO) {
					try {
						initBookItem(bookData = bookData)
						OpenLibraryApi.retrieveDescriptionFromKey(key = bookId) { description ->
							bookDescription.value = description
						}
						getBookThumbnail(coverI = bookData?.coverI) { thumbnail.value = it }
					} catch (e : Exception) {
						status.value = Status.ERROR
					}
				}
			}
		} else {
			status.value = Status.ERROR
		}
	}

	private fun initBookItem(bookData : BookData?) {
		if (bookData == null) {
			status.value = Status.ERROR
			return
		} else {
			bookKey.value = bookData.key
			bookTitle.value = bookData.title
			bookCoverI.value = bookData.coverI
			if (bookData.description != null) bookDescription.value = bookData.description
			bookAuthorList.clear()
			bookAuthorList.addAll(bookData.authorList?.map { it } ?: listOf())
			bookPageCount.value = bookData.numberOfPages
			bookFirstPublishYear.value = bookData.firstPublishYear

			status.value = Status.LOADED
		}
	}

	private fun initNewShowItem(intent : Intent) {

		val hasMovieId = intent.hasExtra(Extra.Companion.Constant.MOVIE_ID.name)
		val hasTvId = intent.hasExtra(Extra.Companion.Constant.TV_ID.name)

		when {
			hasMovieId -> {
				val movieId = intent.getStringExtra(Extra.Companion.Constant.MOVIE_ID.name)
				if (movieId != null) {
					viewModelScope.launch(Dispatchers.IO) {
						try {
							TMDbApi.retrieveMovieDataFromId(id = movieId) { response ->
								if (response == null) {
//									TODO Show msg
									status.value = Status.ERROR
								} else {
									response.body?.string()?.let {
										val movieData : MovieData = objectMapper.readValue(it)
										initMovieData(movieData = movieData)
										getShowThumbnail(url = movieData.posterPath) { thumbnail.value = it }
									}
								}
							}
						} catch (e : Exception) {
//				            TODO    Show description not retrieved message
							e.printStackTrace()
							status.value = Status.ERROR
						}
					}
				} else {
//					TODO Show error
					status.value = Status.ERROR
				}
			}

			hasTvId -> {
				val tvId = intent.getStringExtra(Extra.Companion.Constant.TV_ID.name)
				if (tvId != null) {
					viewModelScope.launch(Dispatchers.IO) {
						try {
							TMDbApi.retrieveTvDataFromId(id = tvId) { response ->
								if (response == null) {
//                                  TODO Show msg
									status.value = Status.ERROR
								} else {
									response.body?.string()?.let {
										val tvData : TvData = objectMapper.readValue(it)
										initTvData(tvData = tvData)
										getShowThumbnail(url = tvData.posterPath) { thumbnail.value = it }
									}
								}
							}
						} catch (e : Exception) {
//				            TODO    Show description not retrieved message
							e.printStackTrace()
							status.value = Status.ERROR
						}
					}
				}
			}

			else -> {
//				TODO Show error
				status.value = Status.ERROR
			}
		}
	}

	fun initShowItem(showData : ShowData?) {
		if (showData == null) {
//			TODO Show error
			status.value = Status.ERROR
		} else {
			when (showData.type) {
				ShowType.MOVIE -> initMovieData(movieData = showData.movieData)
				ShowType.TV -> initTvData(tvData = showData.tvData)
				else -> {
//					TODO Show error
					status.value = Status.ERROR
				}
			}
		}
	}

	private fun initMovieData(movieData : MovieData?) {
		if (movieData == null) {
//			TODO Show error
			status.value = Status.ERROR
		} else {
			movieId.value = movieData.id
			movieAdult.value = movieData.adult
			movieGenres.clear(); movieGenres.addAll(movieData.genres ?: listOf())
			movieHomepage.value = movieData.homepage
			movieImdbId.value = movieData.imdbId
			movieOriginalLanguage.value = movieData.originalLanguage
			movieOriginalTitle.value = movieData.originalTitle
			movieOverview.value = movieData.overview
			moviePosterPath.value = movieData.posterPath
			movieReleaseDate.value = movieData.releaseDate
			movieRuntime.value = movieData.runtime
			movieTitle.value = movieData.title
			movieTagline.value = movieData.tagline

			status.value = Status.LOADED
		}
	}

	private fun initTvData(tvData : TvData?) {
		if (tvData == null) {
//          TODO Show error
			status.value = Status.ERROR
		} else {
			tvId.value = tvData.id
			tvAdult.value = tvData.adult
			tvFirstAirDate.value = tvData.firstAirDate
			tvGenres.clear(); tvGenres.addAll(tvData.genres ?: listOf())
			tvHomepage.value = tvData.homepage
			tvNumberOfSeasons.value = tvData.numberOfSeasons
			tvNumberOfEpisodes.value = tvData.numberOfEpisodes
			tvOriginalLanguage.value = tvData.originalLanguage
			tvOriginalName.value = tvData.originalName
			tvOverview.value = tvData.overview
			tvPosterPath.value = tvData.posterPath
			tvTagline.value = tvData.tagline
			tvName.value = tvData.name

			status.value = Status.LOADED
		}
	}

	fun putBucketItem() {
		when (bucketType.value) {
			BucketType.BOOK -> putBook {
				isNew.value = false
				bucketItemObject.value?.let {
					loadBucketItem(id = it.id)
				}
			}
			BucketType.SHOW -> putShow { isNew.value = false }
			else -> return
		}
	}

	fun updateBucketItem() {
//		Repository.updateBucketItem(
//			id = bucketItemObject.value?.id ?: return,
//			state = state.value ?: return,
//			isFavourite = isFavourite.value ?: return,
//			isLocked = isLocked.value ?: return
//		)
	}

	fun putBook(onSuccess : () -> Unit) {
		if (bucketItemObject.value == null || bucketId.value == null) {
//			Show error
		} else {
			bucketItemObject.value?.thumbnail = thumbnail.value?.encodeBase64()
			bucketItemObject.value?.title = this.bookTitle.value

			bucketItemObject.value?.isFavourite = isFavourite.value ?: false
			bucketItemObject.value?.isLocked = isLocked.value ?: false

			bucketItemObject.value?.data = BookData(
				key = bookKey.value,
				title = bookTitle.value,
				coverI = bookCoverI.value,
				authorList = bookAuthorList,
				firstPublishYear = bookFirstPublishYear.value,
				numberOfPages = bookPageCount.value,
				description = bookDescription.value,
			).toJsonString()

			try {
				bucketItemObject.value?.let { it1 ->
					bucketId.value?.let { it2 ->
						repository2.putBucketItem(it2, it1) { _, e ->
							onSuccess()
						}
					}
				}
			} catch (e : Exception) {
//				TODO Show error
				e.printStackTrace()
			}
		}
	}

	fun putShow(onSuccess : () -> Unit) {
		if (bucketItemObject.value == null || bucketId.value == null) {
//			Show error
		} else {
			bucketItemObject.value?.data = if (tvId.value != null) {
				bucketItemObject.value?.thumbnail = thumbnail.value?.encodeBase64()
				bucketItemObject.value?.title = this.tvName.value
				ShowData(
					type = ShowType.TV,
					tvData = TvData(
						id = this.tvId.value,
						adult = this.tvAdult.value,
						firstAirDate = this.tvFirstAirDate.value,
						genres = this.tvGenres.toList(),
						homepage = this.tvHomepage.value,
						numberOfSeasons = this.tvNumberOfSeasons.value,
						numberOfEpisodes = this.tvNumberOfEpisodes.value,
						originalLanguage = this.tvOriginalLanguage.value,
						originalName = this.tvOriginalName.value,
						overview = this.tvOverview.value,
						posterPath = this.tvPosterPath.value,
						tagline = this.tvTagline.value,
						name = this.tvName.value
					),
				).toJsonString()
			} else if (movieId.value != null) {
				bucketItemObject.value?.thumbnail = thumbnail.value?.encodeBase64()
				bucketItemObject.value?.title = this.movieTitle.value
				ShowData(
					type = ShowType.MOVIE,
					movieData = MovieData(
						id = this.movieId.value,
						adult = this.movieAdult.value,
						genres = this.movieGenres.toList(),
						homepage = this.movieHomepage.value,
						imdbId = this.movieImdbId.value,
						originalLanguage = this.movieOriginalLanguage.value,
						originalTitle = this.movieOriginalTitle.value,
						overview = this.movieOverview.value,
						posterPath = this.moviePosterPath.value,
						releaseDate = this.movieReleaseDate.value,
						runtime = this.movieRuntime.value,
						title = this.movieTitle.value,
						tagline = this.movieTagline.value
					)
				).toJsonString()
			} else {
//				TODO Show error
				return
			}

			try {
				TODO()
//				Repository.putBucketItem(bucketId = bucketId.value ?: return, bucketItemObject = bucketItemObject.value ?: return, onSuccess = onSuccess)
			} catch (e : Exception) {
//				TODO Show error
				e.printStackTrace()
			}
		}
	}

	fun getBookThumbnail(coverI : String?, onSuccess : (Bitmap) -> Unit) {
		try {
			viewModelScope.launch(Dispatchers.IO) {
				OpenLibraryApi.retrieveBookCover(coverI = coverI) {
					it?.body?.byteStream()?.let { inputStream ->
						val bitmap = BitmapFactory.decodeStream(inputStream)
						this.launch(Dispatchers.Main) {
							onSuccess(bitmap)
						}
					} ?: run {
//				        TODO Show error
					}
				}
			}
		} catch (e : Exception) {
//			TODO Show error
			e.printStackTrace()
		}
	}

	fun getShowThumbnail(url : String?, onSuccess : (Bitmap) -> Unit) {
		try {
			viewModelScope.launch(Dispatchers.IO) {
				TMDbApi.retrieveShowPoster(posterPath = url) {
					it?.body?.byteStream()?.let { inputStream ->
						val bitmap = BitmapFactory.decodeStream(inputStream)
						this.launch(Dispatchers.Main) { onSuccess(bitmap) }
					} ?: run {
//	        			TODO Show error
					}
				}
			}
		} catch (e : Exception) {
//			TODO Show error
			e.printStackTrace()
		}
	}

	fun onChangeState(state : Int) {
		this.state.value = BucketItemState.values().getOrElse(state) { BucketItemState.ALPHA }
		if (isNew.value == false) updateBucketItem() else putBucketItem()
	}

	fun onClickLock() {
		isLocked.value = ! (isLocked.value ?: return)
//		if (isNew.value == false) updateBucketItem() else putBucketItem()
		putBucketItem()
	}

	fun onClickFavourite() {
		isFavourite.value = ! (isFavourite.value ?: return)
//		if (isNew.value == false) updateBucketItem() else putBucketItem()
		putBucketItem()
	}
}
