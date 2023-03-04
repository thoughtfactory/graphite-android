package com.syncodec.graphite.presentation.bucketItem.composable.screen.movieScreen

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.syncodec.graphite.di.network.Genre
import com.syncodec.graphite.di.network.MovieData
import com.syncodec.graphite.di.network.ShowData
import com.syncodec.graphite.di.network.ShowType
import com.syncodec.graphite.di.network.TMDbApi
import com.syncodec.graphite.di.network.TMDbApi.objectMapper
import com.syncodec.graphite.presentation.bucketItem.composable.screen.AbstractBucketScreenViewModel
import com.syncodec.graphite.utils.ContentStatus
import com.syncodec.graphite.utils.Quadruple
import com.syncodec.graphite.utils.Status
import com.syncodec.graphite.utils.decodeBase64ToBitmap
import com.syncodec.graphite.utils.encodeBase64
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch


class MovieScreenViewModel : AbstractBucketScreenViewModel() {

	val movieId : MutableStateFlow<String?> = MutableStateFlow(null)
	val movieAdult : MutableStateFlow<Boolean?> = MutableStateFlow(null)
	val movieGenres : MutableStateFlow<List<Genre>> = MutableStateFlow(listOf())
	val movieHomepage : MutableStateFlow<String?> = MutableStateFlow(null)
	val movieImdbId : MutableStateFlow<String?> = MutableStateFlow(null)
	val movieOriginalLanguage : MutableStateFlow<String?> = MutableStateFlow(null)
	val movieOriginalTitle : MutableStateFlow<String?> = MutableStateFlow(null)
	val movieOverview : MutableStateFlow<String?> = MutableStateFlow(null)
	val moviePosterPath : MutableStateFlow<String?> = MutableStateFlow(null)
	val movieReleaseDate : MutableStateFlow<String?> = MutableStateFlow(null)
	val movieRuntime : MutableStateFlow<Int?> = MutableStateFlow(null)
	val movieTitle : MutableStateFlow<String?> = MutableStateFlow(null)
	val movieTagline : MutableStateFlow<String?> = MutableStateFlow(null)

	val thumbnailContentStatus : MutableStateFlow<ContentStatus<Bitmap?>> = MutableStateFlow(ContentStatus.Init)

	val movieData : MutableStateFlow<MovieData?> = MutableStateFlow(null)

	init {
		observeData()
	}

	override fun observeData() {
		viewModelScope.launch(Dispatchers.Default) {
			combine(
				movieId,
				movieAdult,
				movieGenres,
				movieHomepage,
				movieImdbId,
				movieOriginalLanguage,
				movieOriginalTitle,
				movieOverview,
				moviePosterPath,
				movieReleaseDate,
				movieRuntime,
				movieTitle,
				movieTagline,
			) { flowResult ->
				try {
					val id = flowResult[0] as String?
					val adult = flowResult[1] as Boolean?
					val genres = flowResult[2] as List<*>
					val homepage = flowResult[3] as String?
					val imdbId = flowResult[4] as String?
					val originalLanguage = flowResult[5] as String?
					val originalTitle = flowResult[6] as String?
					val overview = flowResult[7] as String?
					val posterPath = flowResult[8] as String?
					val releaseDate = flowResult[9] as String?
					val runtime = flowResult[10] as Int?
					val title = flowResult[11] as String?
					val tagline = flowResult[12] as String?

					MovieData(
						adult = adult,
						genres = genres.map { it as Genre },
						homepage = homepage,
						id = id,
						imdbId = imdbId,
						originalLanguage = originalLanguage,
						originalTitle = originalTitle,
						overview = overview,
						posterPath = posterPath,
						releaseDate = releaseDate,
						runtime = runtime,
						tagline = tagline,
						title = title,
					)
				} catch (e : Exception) {
					null
				}
			}.collect {
				movieData.tryEmit(it)
			}
		}
	}

	override fun initData(id : String, data : String?) {
		viewModelScope.launch(Dispatchers.IO) {
			TMDbApi.retrieveMovieDataFromId(id = id) { response ->
				response?.body?.string()?.let {
					val movieData : MovieData = objectMapper.readValue(it, MovieData::class.java)
					loadData(it)
					retrieveThumbnail(data = movieData.posterPath) { loadThumbnail(it) }
				}
			}
		}
	}

	override fun loadData(data : String?) {
		viewModelScope.launch(Dispatchers.Main) {
			MovieData(jsonString = data).let {
				this@MovieScreenViewModel.movieId.tryEmit(it.id)
				this@MovieScreenViewModel.movieAdult.tryEmit(it.adult)
				this@MovieScreenViewModel.movieGenres.tryEmit(it.genres?.filterNotNull() ?: listOf())
				this@MovieScreenViewModel.movieHomepage.tryEmit(it.homepage)
				this@MovieScreenViewModel.movieImdbId.tryEmit(it.imdbId)
				this@MovieScreenViewModel.movieOriginalLanguage.tryEmit(it.originalLanguage)
				this@MovieScreenViewModel.movieOriginalTitle.tryEmit(it.originalTitle)
				this@MovieScreenViewModel.movieOverview.tryEmit(it.overview)
				this@MovieScreenViewModel.moviePosterPath.tryEmit(it.posterPath)
				this@MovieScreenViewModel.movieReleaseDate.tryEmit(it.releaseDate)
				this@MovieScreenViewModel.movieRuntime.tryEmit(it.runtime)
				this@MovieScreenViewModel.movieTitle.tryEmit(it.title)
				this@MovieScreenViewModel.movieTagline.tryEmit(it.tagline)
			}
		}
	}

	override fun loadThumbnail(thumbnail : Bitmap?) {
		thumbnail?.let {
			this@MovieScreenViewModel.thumbnailContentStatus.tryEmit(ContentStatus.Loaded(it))
		} ?: this@MovieScreenViewModel.thumbnailContentStatus.tryEmit(ContentStatus.LoadedEmpty)
	}

	override fun getData() : Quadruple<String?, String?, String?, String?> {
		val movieData = MovieData(
			adult = movieAdult.value,
			genres = movieGenres.value,
			homepage = movieHomepage.value,
			id = movieId.value,
			imdbId = movieImdbId.value,
			originalLanguage = movieOriginalLanguage.value,
			originalTitle = movieOriginalTitle.value,
			overview = movieOverview.value,
			posterPath = moviePosterPath.value,
			releaseDate = movieReleaseDate.value,
			runtime = movieRuntime.value,
			tagline = movieTagline.value,
			title = movieTitle.value,
		)
		return Quadruple(
			ShowData(
				type = ShowType.MOVIE,
				tvData = null,
				movieData = movieData,
			).toJsonString(),
			movieId.value,
			thumbnailContentStatus.value.dataOrNull?.encodeBase64(),
			movieTitle.value,
		)
	}

	override fun retrieveThumbnail(data : String?, onSuccess : (Bitmap) -> Unit) {
		try {
			viewModelScope.launch(Dispatchers.IO) {
				TMDbApi.retrieveShowPoster(posterPath = data) {
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
//			e.printStackTrace()
		}
	}
}
