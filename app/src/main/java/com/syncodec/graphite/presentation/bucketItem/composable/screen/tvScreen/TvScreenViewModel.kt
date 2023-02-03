package com.syncodec.graphite.presentation.bucketItem.composable.screen.tvScreen

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.syncodec.graphite.di.network.Genre
import com.syncodec.graphite.di.network.ShowData
import com.syncodec.graphite.di.network.ShowType
import com.syncodec.graphite.di.network.TMDbApi
import com.syncodec.graphite.di.network.TvData
import com.syncodec.graphite.presentation.bucketItem.composable.screen.AbstractBucketScreenViewModel
import com.syncodec.graphite.utils.Quadruple
import com.syncodec.graphite.utils.Status
import com.syncodec.graphite.utils.decodeBase64ToBitmap
import com.syncodec.graphite.utils.encodeBase64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch


class TvScreenViewModel : AbstractBucketScreenViewModel() {

	val status : MutableState<Status> = mutableStateOf(Status.INIT)

	val tvId : MutableStateFlow<String?> = MutableStateFlow(null)
	val tvAdult : MutableStateFlow<Boolean?> = MutableStateFlow(null)
	val tvFirstAirDate : MutableStateFlow<String?> = MutableStateFlow(null)
	val tvGenres : MutableStateFlow<List<Genre>> = MutableStateFlow(listOf())
	val tvHomepage : MutableStateFlow<String?> = MutableStateFlow(null)
	val tvNumberOfSeasons : MutableStateFlow<Int?> = MutableStateFlow(null)
	val tvNumberOfEpisodes : MutableStateFlow<Int?> = MutableStateFlow(null)
	val tvOriginalLanguage : MutableStateFlow<String?> = MutableStateFlow(null)
	val tvName : MutableStateFlow<String?> = MutableStateFlow(null)
	val tvOriginalName : MutableStateFlow<String?> = MutableStateFlow(null)
	val tvOverview : MutableStateFlow<String?> = MutableStateFlow(null)
	val tvPosterPath : MutableStateFlow<String?> = MutableStateFlow(null)
	val tvTagline : MutableStateFlow<String?> = MutableStateFlow(null)

	val thumbnail : MutableStateFlow<Bitmap?> = MutableStateFlow(null)

	val tvData : MutableStateFlow<TvData?> = MutableStateFlow(null)

	init {
		observeData()
	}

	override fun observeData() {
		viewModelScope.launch(Dispatchers.Default) {
			combine(
				tvId,
				tvAdult,
				tvFirstAirDate,
				tvGenres,
				tvHomepage,
				tvNumberOfSeasons,
				tvNumberOfEpisodes,
				tvOriginalLanguage,
				tvName,
				tvOriginalName,
				tvOverview,
				tvPosterPath,
				tvTagline,
			) { flowResult ->
				try {
					val id = flowResult[0] as String?
					val adult = flowResult[1] as Boolean?
					val firstAirDate = flowResult[2] as String?
					val genres = flowResult[3] as List<*>
					val homepage = flowResult[4] as String?
					val numberOfSeasons = flowResult[5] as Int?
					val numberOfEpisodes = flowResult[6] as Int?
					val originalLanguage = flowResult[7] as String?
					val name = flowResult[8] as String?
					val originalName = flowResult[9] as String?
					val overview = flowResult[10] as String?
					val posterPath = flowResult[11] as String?
					val tagline = flowResult[12] as String?

					TvData(
						adult = adult,
						firstAirDate = firstAirDate,
						homepage = homepage,
						genres = genres.map { it as Genre },
						id = id,
						name = name,
						numberOfEpisodes = numberOfEpisodes,
						numberOfSeasons = numberOfSeasons,
						originalLanguage = originalLanguage,
						originalName = originalName,
						overview = overview,
						posterPath = posterPath,
						tagline = tagline,
					)
				} catch (e : Exception) {
					null
				}
			}.collect {
				tvData.tryEmit(it)
			}
		}
	}

	override fun initData(id : String, data : String?) {
		viewModelScope.launch(Dispatchers.IO) {
			TMDbApi.retrieveTvDataFromId(id = id) { response ->
				response?.body?.string()?.let {
					val tvData : TvData = TMDbApi.objectMapper.readValue(it, TvData::class.java)
					loadData(it)
					retrieveThumbnail(data = tvData.posterPath) { thumbnail.value = it }
				} ?: run {
					viewModelScope.launch(Dispatchers.Main) { status.value = Status.ERROR }
				}
			}
		}
	}

	override fun loadData(data : String?, thumbnail : String?) {
		viewModelScope.launch(Dispatchers.Main) {
			TvData(jsonString = data).let {
				this@TvScreenViewModel.tvId.tryEmit(it.id)
				this@TvScreenViewModel.tvAdult.tryEmit(it.adult)
				this@TvScreenViewModel.tvFirstAirDate.tryEmit(it.firstAirDate)
				this@TvScreenViewModel.tvGenres.tryEmit(it.genres?.filterNotNull() ?: listOf())
				this@TvScreenViewModel.tvHomepage.tryEmit(it.homepage)
				this@TvScreenViewModel.tvNumberOfSeasons.tryEmit(it.numberOfSeasons)
				this@TvScreenViewModel.tvNumberOfEpisodes.tryEmit(it.numberOfEpisodes)
				this@TvScreenViewModel.tvOriginalLanguage.tryEmit(it.originalLanguage)
				this@TvScreenViewModel.tvName.tryEmit(it.name)
				this@TvScreenViewModel.tvOriginalName.tryEmit(it.originalName)
				this@TvScreenViewModel.tvOverview.tryEmit(it.overview)
				this@TvScreenViewModel.tvPosterPath.tryEmit(it.posterPath)
				this@TvScreenViewModel.tvTagline.tryEmit(it.tagline)

				if (thumbnail != null) this@TvScreenViewModel.thumbnail.tryEmit(thumbnail.decodeBase64ToBitmap())

				status.value = Status.LOADED
			}
		}
	}

	override fun getData() : Quadruple<String?, String?, String?, String?> {
		val tvData = TvData(
			adult = tvAdult.value,
			firstAirDate = tvFirstAirDate.value,
			homepage = tvHomepage.value,
			genres = tvGenres.value,
			id = tvId.value,
			name = tvName.value,
			numberOfEpisodes = tvNumberOfEpisodes.value,
			numberOfSeasons = tvNumberOfSeasons.value,
			originalLanguage = tvOriginalLanguage.value,
			originalName = tvOriginalName.value,
			overview = tvOverview.value,
			posterPath = tvPosterPath.value,
			tagline = tvTagline.value,
		)

		return Quadruple(
			ShowData(
				type = ShowType.TV,
				tvData = tvData,
				movieData = null,
			).toJsonString(),
			tvId.value,
			thumbnail.value?.encodeBase64(),
			tvName.value,
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
			e.printStackTrace()
		}
	}
}
