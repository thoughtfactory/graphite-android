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
import com.syncodec.graphite.di.repository.Repository2
import com.syncodec.graphite.di.repository.RepositoryState
import com.syncodec.graphite.utils.Extra
import com.syncodec.graphite.utils.Status
import com.syncodec.graphite.utils.decodeBase64ToBitmap
import com.syncodec.graphite.utils.encodeBase64
import com.syncodec.graphite.utils.serializable
import dagger.hilt.android.lifecycle.HiltViewModel
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class BucketItemViewModel @Inject constructor(private val repository2 : Repository2) : ViewModel() {

	val objectMapper = jsonMapper { addModule(kotlinModule()) }.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

	val repositoryState = repository2.repositoryState
	val status : MutableState<Status> = mutableStateOf(Status.INIT)

	val isNew = mutableStateOf<Boolean?>(null)
	val bucketId = mutableStateOf<RealmUUID?>(null)

	val objectId : MutableState<RealmUUID?> = mutableStateOf(null)
	val createdTimestamp : MutableState<Long?> = mutableStateOf(null)
	val modifiedTimestamp : MutableState<Long?> = mutableStateOf(null)
	val bucketType : MutableState<BucketType?> = mutableStateOf(null)
	val title : MutableState<String?> = mutableStateOf(null)
	val state : MutableState<BucketItemState?> = mutableStateOf(null)
	val thumbnail : MutableState<Bitmap?> = mutableStateOf(null)
	val isFavourite : MutableState<Boolean?> = mutableStateOf(null)
	val isLocked : MutableState<Boolean?> = mutableStateOf(null)

	//  Book
	var bookKey : MutableState<String?> = mutableStateOf(null)
	var bookTitle : MutableState<String?> = mutableStateOf(null)
	var bookCoverI : MutableState<String?> = mutableStateOf(null)
	var bookAuthorList : SnapshotStateList<String> = mutableStateListOf()
	var bookDescription : MutableState<String?> = mutableStateOf(null)
	var bookPageCount : MutableState<Int?> = mutableStateOf(null)
	var bookFirstPublishYear : MutableState<String?> = mutableStateOf(null)

	val showType : MutableState<ShowType?> = mutableStateOf(null)

	//  Movie
	val movieId : MutableState<String?> = mutableStateOf(null)
	val movieAdult : MutableState<Boolean?> = mutableStateOf(false)
	val movieGenres : SnapshotStateList<Genre> = mutableStateListOf()
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
	val tvGenres : SnapshotStateList<Genre> = mutableStateListOf()
	val tvHomepage : MutableState<String?> = mutableStateOf(null)
	val tvNumberOfSeasons : MutableState<Int?> = mutableStateOf(null)
	val tvNumberOfEpisodes : MutableState<Int?> = mutableStateOf(null)
	val tvOriginalLanguage : MutableState<String?> = mutableStateOf(null)
	val tvName : MutableState<String?> = mutableStateOf(null)
	val tvOriginalName : MutableState<String?> = mutableStateOf(null)
	val tvOverview : MutableState<String?> = mutableStateOf(null)
	val tvPosterPath : MutableState<String?> = mutableStateOf(null)
	val tvTagline : MutableState<String?> = mutableStateOf(null)

	fun initData(bucketId : RealmUUID, bucketType : BucketType, intent : Intent) {
		isNew.value = true

		this.bucketId.value = bucketId
		this.bucketType.value = bucketType

		viewModelScope.launch(Dispatchers.IO) {
			when (bucketType) {
				BucketType.TODO -> null
				BucketType.BOOK -> {
					val hasBookId = intent.hasExtra(Extra.Companion.Constant.BOOK_ID.name)
					val hasExtraData = intent.hasExtra(Extra.Companion.Constant.BUCKET_EXTRA_DATA.name)

					if (hasBookId && hasExtraData) {
						val bookId = intent.getStringExtra(Extra.Companion.Constant.BOOK_ID.name)
						val bookData = intent.serializable<BookData>(Extra.Companion.Constant.BUCKET_EXTRA_DATA.name)

						loadBook(bookData)
						OpenLibraryApi.retrieveDescriptionFromKey(key = bookId) { description ->
							this.launch(Dispatchers.Main) {
								bookDescription.value = description
							}
						}
						getBookThumbnail(coverI = bookData?.coverI) {
							thumbnail.value = it
						}
					}
				}

				BucketType.SHOW -> {
					val hasMovieId = intent.hasExtra(Extra.Companion.Constant.MOVIE_ID.name)
					val hasTvId = intent.hasExtra(Extra.Companion.Constant.TV_ID.name)

					if (hasMovieId) {
						val movieId = intent.getStringExtra(Extra.Companion.Constant.MOVIE_ID.name)
						if (movieId != null) {
							viewModelScope.launch(Dispatchers.IO) {
								try {
									TMDbApi.retrieveMovieDataFromId(id = movieId) { response ->
										if (response == null) {
//									        TODO Show msg
											status.value = Status.ERROR
										} else {
											response.body?.string()?.let {
												val movieData : MovieData = objectMapper.readValue(it, MovieData::class.java)
												loadMovie(movieData = movieData)
												getShowThumbnail(url = movieData.posterPath) { thumbnail.value = it }
											}
										}
									}
								} catch (e : Exception) {
//				                    TODO    Show description not retrieved message
									e.printStackTrace()
									status.value = Status.ERROR
								}
							}
						} else {
//					        TODO Show error
							status.value = Status.ERROR
						}

					} else if (hasTvId) {
						val tvId = intent.getStringExtra(Extra.Companion.Constant.TV_ID.name)
						if (tvId != null) {
							viewModelScope.launch(Dispatchers.IO) {
								try {
									TMDbApi.retrieveTvDataFromId(id = tvId) { response ->
										if (response == null) {
//									        TODO Show msg
											status.value = Status.ERROR
										} else {
											response.body?.string()?.let {
												val tvData : TvData = objectMapper.readValue(it, TvData::class.java)
												loadTv(tvData = tvData)
												getShowThumbnail(url = tvData.posterPath) { thumbnail.value = it }
											}
										}
									}
								} catch (e : Exception) {
//				                    TODO    Show description not retrieved message
									e.printStackTrace()
									status.value = Status.ERROR
								}
							}
						} else {
//					        TODO Show error
							status.value = Status.ERROR
						}
					}
				}

				BucketType.LINK -> null
				BucketType.UNKNOWN -> null
			}
		}
	}

	fun loadData(bucketItemId : RealmUUID, bucketId : RealmUUID) {
		viewModelScope.launch(Dispatchers.IO) {
			this@BucketItemViewModel.bucketId.value = bucketId
			when (repositoryState.value) {
				RepositoryState.INIT -> null
				RepositoryState.LOCKED -> null
				RepositoryState.LOADING -> null
				RepositoryState.SUCCESS -> onRepositoryStateSuccess(bucketItemId)
				RepositoryState.ERROR -> null
			}
		}
	}

	private fun onRepositoryStateSuccess(bucketItemId : RealmUUID) {
		viewModelScope.launch(Dispatchers.IO) {
			if (repositoryState.value != RepositoryState.SUCCESS) this.cancel()

			isNew.value = false
			repository2.getBucketItemAsFlow(id = bucketItemId).collect {
				this@BucketItemViewModel.objectId.value = it?.id
				this@BucketItemViewModel.createdTimestamp.value = it?.createdTimestamp
				this@BucketItemViewModel.modifiedTimestamp.value = it?.modifiedTimestamp
				this@BucketItemViewModel.bucketType.value = try {
					BucketType.valueOf(it?.bucketType ?: BucketType.UNKNOWN.name)
				} catch (e : Exception) {
					BucketType.UNKNOWN
				}
				this@BucketItemViewModel.title.value = it?.title
				this@BucketItemViewModel.state.value = BucketItemState.valueOf(it?.state ?: BucketItemState.ALPHA.name)
				this@BucketItemViewModel.thumbnail.value = it?.thumbnail?.decodeBase64ToBitmap()
				this@BucketItemViewModel.isFavourite.value = it?.isFavourite
				this@BucketItemViewModel.isLocked.value = it?.isLocked

				when (this@BucketItemViewModel.bucketType.value) {
					BucketType.TODO -> null
					BucketType.BOOK -> loadBook(bookData = it?.getBookData())
					BucketType.SHOW -> it?.getShowData()?.let { (type, tvData, movieData) ->
						when (type) {
							ShowType.MOVIE -> loadMovie(movieData = movieData)
							ShowType.TV -> loadTv(tvData = tvData)
							null -> null
						}
					}
					BucketType.LINK -> null
					BucketType.UNKNOWN -> null
					else -> null
				}
			}
		}
	}

	private fun loadBook(bookData : BookData?) {
		viewModelScope.launch(Dispatchers.Main) {
			if (bookData == null) {
//			    TODO Show error
				status.value = Status.ERROR
			} else {
				this@BucketItemViewModel.bookKey.value = bookData.key
				this@BucketItemViewModel.bookTitle.value = bookData.title
				this@BucketItemViewModel.bookCoverI.value = bookData.coverI
				this@BucketItemViewModel.bookAuthorList.clear()
				this@BucketItemViewModel.bookAuthorList.addAll(bookData.authorList?.filterNotNull() ?: listOf())
				this@BucketItemViewModel.bookDescription.value = bookData.description
				this@BucketItemViewModel.bookPageCount.value = bookData.numberOfPages
				this@BucketItemViewModel.bookFirstPublishYear.value = bookData.firstPublishYear

				this@BucketItemViewModel.title.value = bookData.title

				status.value = Status.LOADED
			}
		}
	}

	private fun loadMovie(movieData : MovieData?) {
		viewModelScope.launch(Dispatchers.Main) {
			if (movieData == null) {
//			    TODO Show error
				status.value = Status.ERROR
			} else {
				this@BucketItemViewModel.showType.value = ShowType.MOVIE

				this@BucketItemViewModel.movieId.value = movieData.id
				this@BucketItemViewModel.movieAdult.value = movieData.adult
				this@BucketItemViewModel.movieGenres.clear(); movieGenres.addAll(movieData.genres?.filterNotNull() ?: listOf())
				this@BucketItemViewModel.movieHomepage.value = movieData.homepage
				this@BucketItemViewModel.movieImdbId.value = movieData.imdbId
				this@BucketItemViewModel.movieOriginalLanguage.value = movieData.originalLanguage
				this@BucketItemViewModel.movieOriginalTitle.value = movieData.originalTitle
				this@BucketItemViewModel.movieOverview.value = movieData.overview
				this@BucketItemViewModel.moviePosterPath.value = movieData.posterPath
				this@BucketItemViewModel.movieReleaseDate.value = movieData.releaseDate
				this@BucketItemViewModel.movieRuntime.value = movieData.runtime
				this@BucketItemViewModel.movieTitle.value = movieData.title
				this@BucketItemViewModel.movieTagline.value = movieData.tagline

				this@BucketItemViewModel.title.value = movieData.title

				status.value = Status.LOADED
			}
		}
	}

	private fun loadTv(tvData : TvData?) {
		viewModelScope.launch(Dispatchers.Main) {
			if (tvData == null) {
//			    TODO Show error
				status.value = Status.ERROR
			} else {
				this@BucketItemViewModel.showType.value = ShowType.TV

				this@BucketItemViewModel.tvId.value = tvData.id
				this@BucketItemViewModel.tvAdult.value = tvData.adult
				this@BucketItemViewModel.tvFirstAirDate.value = tvData.firstAirDate
				this@BucketItemViewModel.tvGenres.clear(); tvGenres.addAll(tvData.genres?.filterNotNull() ?: listOf())
				this@BucketItemViewModel.tvHomepage.value = tvData.homepage
				this@BucketItemViewModel.tvNumberOfSeasons.value = tvData.numberOfSeasons
				this@BucketItemViewModel.tvNumberOfEpisodes.value = tvData.numberOfEpisodes
				this@BucketItemViewModel.tvOriginalLanguage.value = tvData.originalLanguage
				this@BucketItemViewModel.tvName.value = tvData.name
				this@BucketItemViewModel.tvOriginalName.value = tvData.originalName
				this@BucketItemViewModel.tvOverview.value = tvData.overview
				this@BucketItemViewModel.tvPosterPath.value = tvData.posterPath
				this@BucketItemViewModel.tvTagline.value = tvData.tagline

				this@BucketItemViewModel.title.value = tvData.name

				status.value = Status.LOADED
			}
		}
	}

	fun putBucketItem() {
		CoroutineScope(Dispatchers.IO).launch {
			try {
				BucketItemObject().apply {
					if (this@BucketItemViewModel.objectId.value != null) this.id = this@BucketItemViewModel.objectId.value ?: RealmUUID.random()
					this.modifiedTimestamp = System.currentTimeMillis()
					this.bucketType = this@BucketItemViewModel.bucketType.value?.name ?: BucketType.UNKNOWN.name
					this.title = this@BucketItemViewModel.title.value
					this.state = this@BucketItemViewModel.state.value?.name ?: BucketItemState.ALPHA.name
					this.thumbnail = this@BucketItemViewModel.thumbnail.value?.encodeBase64()
					this.isFavourite = this@BucketItemViewModel.isFavourite.value ?: false
					this.isLocked = this@BucketItemViewModel.isLocked.value ?: false
					this.parentId = this@BucketItemViewModel.bucketId.value

					when (this@BucketItemViewModel.bucketType.value) {
						BucketType.TODO -> null
						BucketType.BOOK -> {
							this.key = bookKey.value
							this.data = BookData(
								key = bookKey.value,
								title = bookTitle.value,
								coverI = bookCoverI.value,
								authorList = bookAuthorList,
								firstPublishYear = bookFirstPublishYear.value,
								numberOfPages = bookPageCount.value,
								description = bookDescription.value,
							).toJsonString()
						}

						BucketType.SHOW -> {
							this.key = when(showType.value) {
								ShowType.MOVIE -> movieId.value
								ShowType.TV -> tvId.value
								else -> null
							}
							this.data = when(showType.value) {
								ShowType.MOVIE -> ShowData(
									type = ShowType.MOVIE,
									movieData = MovieData(
										adult = this@BucketItemViewModel.movieAdult.value,
										genres = this@BucketItemViewModel.movieGenres.toList(),
										homepage = this@BucketItemViewModel.movieHomepage.value,
										id = this@BucketItemViewModel.movieId.value,
										imdbId = this@BucketItemViewModel.movieImdbId.value,
										originalLanguage = this@BucketItemViewModel.movieOriginalLanguage.value,
										originalTitle = this@BucketItemViewModel.movieOriginalTitle.value,
										overview = this@BucketItemViewModel.movieOverview.value,
										posterPath = this@BucketItemViewModel.moviePosterPath.value,
										releaseDate = this@BucketItemViewModel.movieReleaseDate.value,
										runtime = this@BucketItemViewModel.movieRuntime.value,
										tagline = this@BucketItemViewModel.movieTagline.value,
										title = this@BucketItemViewModel.movieTitle.value
									),
								).toJsonString()
								ShowType.TV -> ShowData(
									type = ShowType.TV,
									tvData = TvData(
										adult = this@BucketItemViewModel.tvAdult.value,
										firstAirDate = this@BucketItemViewModel.tvFirstAirDate.value,
										genres = this@BucketItemViewModel.tvGenres.toList(),
										homepage = this@BucketItemViewModel.tvHomepage.value,
										id = this@BucketItemViewModel.tvId.value,
										name = this@BucketItemViewModel.tvName.value,
										numberOfEpisodes = this@BucketItemViewModel.tvNumberOfEpisodes.value,
										numberOfSeasons = this@BucketItemViewModel.tvNumberOfSeasons.value,
										originalLanguage = this@BucketItemViewModel.tvOriginalLanguage.value,
										originalName = this@BucketItemViewModel.tvOriginalName.value,
										overview = this@BucketItemViewModel.tvOverview.value,
										posterPath = this@BucketItemViewModel.tvPosterPath.value,
										tagline = this@BucketItemViewModel.tvTagline.value,
									)
								).toJsonString()
								else -> null
							}
						}

						BucketType.LINK -> null
						BucketType.UNKNOWN -> null
						else -> null
					}

					this@BucketItemViewModel.bucketId.value?.let { bucketId ->
						repository2.putBucketItem(bucketId = bucketId, bucketItemObject = this) { _, _ -> onSuccess(this.id, bucketId) }
					}
				}
			} catch (e : Exception) {
				e.printStackTrace()
			}
		}
	}

	private fun onSuccess(bucketItemId : RealmUUID, bucketId : RealmUUID) {
		loadData(bucketItemId, bucketId)
	}

	fun onToggleFavourite() {
		isFavourite.value = isFavourite.value?.not()
		putBucketItem()
	}

	fun onToggleLocked() {
		isLocked.value = isLocked.value?.not()
		putBucketItem()
	}

	fun onToggleState(state : Int) {
		this.state.value = BucketItemState.values()[state]
		putBucketItem()
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
}
