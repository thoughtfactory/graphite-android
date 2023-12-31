package com.syncodec.graphite.di.model.local

import com.syncodec.graphite.BuildConfig
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure


@Serializable
sealed class BucketItemData {

	abstract val key: String?
	abstract var title: String?
	abstract var description: String?

	@Serializable
	sealed class BookData : BucketItemData() {
		@Serializable
		data class OpenLibraryBookData(
			@SerialName("key") override var key: String,
			@SerialName("title") override var title: String? = null,
			@SerialName("description") override var description: String? = null,
			@SerialName("cover_i") var coverI: Int? = null,
			@SerialName("author_name") var authorList: List<String> = listOf(),
			@SerialName("first_publish_year") var firstPublishYear: Int? = null,
			@SerialName("number_of_pages_median") var numberOfPages: Int? = null,
		) : BookData()
	}

	@Serializable
	sealed class ShowData : BucketItemData() {
		@Serializable
		sealed class TMDbData : ShowData() {
			@Serializable
			data class TMDbMovieData(
				@SerialName("id") val id: Int,
				@SerialName("key") override val key: String = id.toString(),
				@SerialName("title") override var title: String? = null,
				@SerialName("overview") override var description: String? = null,
				@SerialName("imdb_id") var imdbId: String? = null,
				@SerialName("adult") var adult: Boolean? = null,
				@SerialName("genres") var genres: List<@Serializable(with = TMDbGenreSerializer::class) Int> = listOf(),
				@SerialName("homepage") var homepage: String? = null,
				@SerialName("original_language") var originalLanguage: String? = null,
				@SerialName("original_title") var originalTitle: String? = null,
				@SerialName("poster_path") var posterPath: String? = null,
				@SerialName("release_date") var releaseDate: String? = null,
				@SerialName("runtime") var runtime: Int? = null,
				@SerialName("tagline") var tagline: String? = null,
			) : TMDbData()

			@Serializable
			data class TMDbTvData(
				@SerialName("id") val id: Int,
				@SerialName("key") override val key: String = id.toString(),
				@SerialName("name") override var title: String? = null,
				@SerialName("overview") override var description: String? = null,
				@SerialName("adult") var adult: Boolean? = null,
				@SerialName("first_air_date") var firstAirDate: String? = null,
				@SerialName("homepage") var homepage: String? = null,
				@SerialName("genres") var genres: List<@Serializable(with = TMDbGenreSerializer::class) Int> = listOf(),
				@SerialName("number_of_episodes") var numberOfEpisodes: Int? = null,
				@SerialName("number_of_seasons") var numberOfSeasons: Int? = null,
				@SerialName("original_language") var originalLanguage: String? = null,
				@SerialName("original_name") var originalName: String? = null,
				@SerialName("poster_path") var posterPath: String? = null,
				@SerialName("tagline") var tagline: String? = null,
			) : TMDbData()

			fun getGenreList(): List<String> = when (this) {
				is TMDbMovieData -> movieGenreMap.filter { this.genres.contains(it.key) }.map { it.value }
				is TMDbTvData -> tvGenreMap.filter { this.genres.contains(it.key) }.map { it.value }
			}

			companion object {
				val movieGenreMap = mapOf(
					28 to "Action",
					12 to "Adventure",
					16 to "Animation",
					35 to "Comedy",
					80 to "Crime",
					99 to "Documentary",
					18 to "Drama",
					10751 to "Family",
					14 to "Fantasy",
					36 to "History",
					27 to "Horror",
					10402 to "Music",
					9648 to "Mystery",
					10749 to "Romance",
					878 to "Science Fiction",
					10770 to "TV Movie",
					53 to "Thriller",
					10752 to "War",
					37 to "Western",
				)

				val tvGenreMap = mapOf(
					10759 to "Action & Adventure",
					16 to "Animation",
					35 to "Comedy",
					80 to "Crime",
					99 to "Documentary",
					18 to "Drama",
					10751 to "Family",
					10762 to "Kids",
					9648 to "Mystery",
					10763 to "News",
					10764 to "Reality",
					10765 to "Sci-Fi & Fantasy",
					10766 to "Soap",
					10767 to "Talk",
					10768 to "War & Politics",
					37 to "Western",
				)
			}
		}
	}

	@Serializable
	data class LinkData(
		@SerialName("key") override var key: String? = null,
		@SerialName("og_title") override var title: String? = null,
		@SerialName("og_description") override var description: String? = null,
		@SerialName("og_url") var url: String? = null,
		@SerialName("og_site_name") var siteName: String? = null,
		@SerialName("og_type") var type: String? = null,
		@SerialName("og_image") var imagePath: String? = null,
	) : BucketItemData()

	fun getYear(): String? = try {
		when (this) {
			is BookData.OpenLibraryBookData -> this.firstPublishYear?.toString()
			is ShowData.TMDbData.TMDbMovieData -> this.releaseDate?.take(4)
			is ShowData.TMDbData.TMDbTvData -> this.firstAirDate?.take(4)
			is LinkData -> null
		}
	} catch (e: Exception) {
		if (BuildConfig.DEBUG) e.printStackTrace()
		null
	}

	fun getThumbnailPath(): String? = when (this) {
		is BookData.OpenLibraryBookData -> this.coverI?.let { "https://covers.openlibrary.org/b/id/${it}M.jpg" }
		is ShowData.TMDbData.TMDbMovieData -> this.posterPath?.let { "https://image.tmdb.org/t/p/w500$it" }
		is ShowData.TMDbData.TMDbTvData -> this.posterPath?.let { "https://image.tmdb.org/t/p/w500$it" }
		is LinkData -> this.imagePath
	}

	fun getURL(): String? = this.key?.let {
		when (this) {
			is BookData.OpenLibraryBookData -> "$OPEN_LIBRARY_BASE_URL$it"      //  key is an OpenLibrary Id. It already contains / slash  -> /works/OL5819456W
			is ShowData.TMDbData.TMDbMovieData -> "$TMDB_MOVIE_BASE_URL/$it"
			is ShowData.TMDbData.TMDbTvData -> "$TMDB_TV_BASE_URL/$it"
			is LinkData -> this.url
		}
	}

	companion object {
		private const val OPEN_LIBRARY_BASE_URL = "https://openlibrary.org"
		private const val TMDB_MOVIE_BASE_URL = "https://www.themoviedb.org/movie"
		private const val TMDB_TV_BASE_URL = "https://www.themoviedb.org/tv"
	}
}

object TMDbGenreSerializer : KSerializer<Int> {
	private val detailDescriptor: SerialDescriptor = buildClassSerialDescriptor("TMDbGenre") {
		element("id", PrimitiveSerialDescriptor("TMDbGenre", PrimitiveKind.INT))
		element("name", PrimitiveSerialDescriptor("TMDbGenre", PrimitiveKind.STRING))
	}
	override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("TMDbGenre", PrimitiveKind.STRING)
	override fun serialize(encoder: Encoder, value: Int) {
		encoder.encodeInt(value)
	}

	override fun deserialize(decoder: Decoder): Int {
		return try {
			decoder.decodeInt()
		} catch (e: Exception) {
			try {
				decoder.decodeStructure(detailDescriptor) {
					var id: Int? = null
					loop@ while (true) {
						when (val index = decodeElementIndex(detailDescriptor)) {
							CompositeDecoder.DECODE_DONE -> break@loop
							0 -> id = decodeIntElement(detailDescriptor, 0)
							1 -> decodeStringElement(detailDescriptor, 1)
							else -> throw SerializationException("Unknown index $index")
						}
					}
					id ?: throw SerializationException("Missing field 'id'")
				}
			} catch (e: Exception) {
				0
			}
		}
	}
}
