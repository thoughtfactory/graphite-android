package com.syncodec.graphite.presentation.bucketItem

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.lifecycleScope
import com.syncodec.graphite.di.model.BucketType
import com.syncodec.graphite.di.network.BookData
import com.syncodec.graphite.di.network.ShowData
import com.syncodec.graphite.di.network.ShowType
import com.syncodec.graphite.presentation.bucketItem.composable.screen.AbstractBucketScreenViewModel
import com.syncodec.graphite.presentation.bucketItem.composable.screen.BucketItemScreen
import com.syncodec.graphite.presentation.bucketItem.composable.screen.bookScreen.BookScreenViewModel
import com.syncodec.graphite.presentation.bucketItem.composable.screen.movieScreen.MovieScreenViewModel
import com.syncodec.graphite.presentation.bucketItem.composable.screen.tvScreen.TvScreenViewModel
import com.syncodec.graphite.presentation.ui.BaseContent
import com.syncodec.graphite.utils.Extra
import com.syncodec.graphite.utils.decodeBase64ToBitmap
import com.syncodec.graphite.utils.serializable
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel


class BucketItemActivity : ComponentActivity() {

	private val viewModel : BucketItemViewModel by viewModel()
	private lateinit var screenViewModel : AbstractBucketScreenViewModel

	var bucketType = mutableStateOf<BucketType?>(null)

	private var loaderCoroutineScope : CoroutineScope? = null

	override fun onCreate(savedInstanceState : Bundle?) {
		super.onCreate(savedInstanceState)

		initData()

		setContent {
			BaseContent {
				val isNew by viewModel.isNew
				val isFavourite by viewModel.isFavourite
				val isLocked by viewModel.isLocked

				val bucketItemObject by viewModel.bucketItemObject
				val showType by viewModel.showType.collectAsState()
				val bucketType by bucketType

				BucketItemScreen(
					bucketItemObject = bucketItemObject,
					bucketType = bucketType,
					showType = showType,
					isSaved = isNew?.not(),
					isFavourite = isFavourite ?: false,
					isLocked = isLocked ?: false,
					onClickSave = {
						screenViewModel.getData().let { (data, key, thumbnail, title) ->
							viewModel.data.value = data
							viewModel.key.value = key
							viewModel.thumbnail.value = thumbnail
							viewModel.title.value = title
						}
						viewModel.putBucketItem()
					},
					onClickFavourite = {
						if (isNew == true) {
							screenViewModel.getData().let { (bookData, key, thumbnail, title) ->
								viewModel.data.value = bookData
								viewModel.key.value = key
								viewModel.thumbnail.value = thumbnail
								viewModel.title.value = title
							}
						}
						viewModel.onToggleFavourite()
					},
					onClickLock = {
						if (isNew == true) {
							screenViewModel.getData().let { (bookData, key, thumbnail, title) ->
								viewModel.data.value = bookData
								viewModel.key.value = key
								viewModel.thumbnail.value = thumbnail
								viewModel.title.value = title
							}
						}
						viewModel.onToggleLock()
					},
					onChangeState = {
						if (isNew == true) {
							screenViewModel.getData().let { (bookData, key, thumbnail, title) ->
								viewModel.data.value = bookData
								viewModel.key.value = key
								viewModel.thumbnail.value = thumbnail
								viewModel.title.value = title
							}
						}
						viewModel.onChangeState(it)
					},
					onShare = {
						screenViewModel.getData().let { (data, key, thumbnail, title) ->
							val shareData = when (bucketType) {
								BucketType.BOOK -> "I'm reading $title. Find it on https://openlibrary.org$key"
								BucketType.SHOW -> when (showType) {
									ShowType.MOVIE -> "I'm watching $title. Find it on https://www.themoviedb.org/movie/$key"
									ShowType.TV -> "I'm watching $title. Find it on https://www.themoviedb.org/tv/$key"
									else -> ""
								}

								else -> ""
							}
							onShare(shareData)
						}
					},
					onDelete = {
						bucketItemObject?.id?.let { id ->
							viewModel.delete(id) {
								withContext(Dispatchers.Main) {
									Toast.makeText(this@BucketItemActivity, "${bucketItemObject?.title}", Toast.LENGTH_SHORT).show()
								}
								finish()
							}
						}
					},
					onClickBack = { finish() },
				)
			}
		}
	}

	override fun onDestroy() {
		loaderCoroutineScope?.cancel()
		super.onDestroy()
	}

	private fun initData() {
		val hasIsNew = intent.hasExtra(Extra.Companion.Extra.IsNew.name)
		val hasBucketId = intent.hasExtra(Extra.Companion.Extra.BUCKET_ID.name)
		val hasBucketType = intent.hasExtra(Extra.Companion.Extra.BUCKET_TYPE.name)

		if (hasIsNew && hasBucketId && hasBucketType) {
			val isNew = intent.getBooleanExtra(Extra.Companion.Extra.IsNew.name, false)
			val bucketId = intent.getByteArrayExtra(Extra.Companion.Extra.BUCKET_ID.name)?.let { RealmUUID.from(it) }
			val bucketItemId = intent.getByteArrayExtra(Extra.Companion.Extra.BUCKET_ITEM_ID.name)?.let { RealmUUID.from(it) }
			val bucketType = intent.getStringExtra(Extra.Companion.Extra.BUCKET_TYPE.name)

			if (bucketId == null || bucketType == null) {
				Toast.makeText(this.applicationContext, "Error reading info.", Toast.LENGTH_SHORT).show()
				finish()
			} else {
				try {
					this.bucketType.value = BucketType.values().find { it.name == bucketType }
					when (bucketType) {
						BucketType.BOOK.name -> {
							screenViewModel = viewModels<BookScreenViewModel>().value
							initBookData(isNew = isNew, bucketId = bucketId, bucketItemId = bucketItemId)
						}

						BucketType.SHOW.name -> initShowData(isNew = isNew, bucketId = bucketId, bucketItemId = bucketItemId)
						else -> {
							Toast.makeText(this.applicationContext, "Error reading info.", Toast.LENGTH_SHORT).show()
							finish()
						}
					}
				} catch (e : Exception) {
					Toast.makeText(this.applicationContext, "Error reading info.", Toast.LENGTH_SHORT).show()
					finish()
				}
			}

		} else {
			Toast.makeText(this.applicationContext, "Error reading info.", Toast.LENGTH_SHORT).show()
			finish()
		}
	}

	private fun initBookData(isNew : Boolean, bucketId : RealmUUID, bucketItemId : RealmUUID?) {
		if (isNew) {
			val hasBookId = intent.hasExtra(Extra.Companion.Extra.BOOK_ID.name)
			val hasExtraData = intent.hasExtra(Extra.Companion.Extra.BUCKET_EXTRA_DATA.name)

			if (! hasBookId || ! hasExtraData) {
				Toast.makeText(this.applicationContext, "Error reading info.", Toast.LENGTH_SHORT).show()
				finish()
			} else {
				val bookId = intent.getStringExtra(Extra.Companion.Extra.BOOK_ID.name)
				val bookData = intent.serializable<BookData>(Extra.Companion.Extra.BUCKET_EXTRA_DATA.name)

				if (bookId == null || bookData == null) {
					Toast.makeText(this.applicationContext, "Error reading info.", Toast.LENGTH_SHORT).show()
					finish()
				} else {
					viewModel.initData(bucketId = bucketId, bucketType = BucketType.BOOK)
					screenViewModel.initData(id = bookId, data = bookData.toJsonString())
				}
			}
		} else {
			if (bucketItemId == null) {
				Toast.makeText(this.applicationContext, "Error reading info.", Toast.LENGTH_SHORT).show()
				finish()
			} else {
				viewModel.loadData(bucketItemId = bucketItemId)
				lifecycleScope.launch(Dispatchers.Default) {
					loaderCoroutineScope?.cancel()
					loaderCoroutineScope = this
					combine(viewModel.data, viewModel.thumbnail) { data, thumbnail ->
						Pair(data, thumbnail)
					}.collect { (data, thumbnail) ->
						val bookData = BookData(data)
						viewModel.key.tryEmit(bookData.key)
						screenViewModel.loadData(data = data)
						screenViewModel.loadThumbnail(thumbnail = thumbnail?.decodeBase64ToBitmap())
					}
				}
			}
		}
	}

	private fun initShowData(isNew : Boolean, bucketId : RealmUUID, bucketItemId : RealmUUID?) {
		val hasShowType = intent.hasExtra(Extra.Companion.Extra.SHOW_TYPE.name)

		if (hasShowType) {
			val showType = intent.getStringExtra(Extra.Companion.Extra.SHOW_TYPE.name)
			when (showType) {
				ShowType.MOVIE.name -> {
					screenViewModel = viewModels<MovieScreenViewModel>().value
					initMovieData(isNew = isNew, bucketId = bucketId, bucketItemId = bucketItemId)
				}

				ShowType.TV.name -> {
					screenViewModel = viewModels<TvScreenViewModel>().value
					initTvData(isNew = isNew, bucketId = bucketId, bucketItemId = bucketItemId)
				}

				else -> {
					Toast.makeText(this.applicationContext, "Error reading info.", Toast.LENGTH_SHORT).show()
					finish()
				}
			}
		} else {
			Toast.makeText(this.applicationContext, "Error reading info.", Toast.LENGTH_SHORT).show()
			finish()
		}
	}

	private fun initMovieData(isNew : Boolean, bucketId : RealmUUID, bucketItemId : RealmUUID?) {
		if (isNew) {
			val hasMovieId = intent.hasExtra(Extra.Companion.Extra.MOVIE_ID.name)

			if (hasMovieId) {
				val movieId = intent.getStringExtra(Extra.Companion.Extra.MOVIE_ID.name)

				if (movieId == null) {
					Toast.makeText(this.applicationContext, "Error reading info.", Toast.LENGTH_SHORT).show()
					finish()
				} else {
					viewModel.initData(bucketId = bucketId, bucketType = BucketType.SHOW, showType = ShowType.MOVIE)
					screenViewModel.initData(id = movieId, data = null)
				}
			} else {
				Toast.makeText(this.applicationContext, "Error reading info.", Toast.LENGTH_SHORT).show()
				finish()
			}
		} else {
			if (bucketItemId == null) {
				Toast.makeText(this.applicationContext, "Error reading info.", Toast.LENGTH_SHORT).show()
				finish()
			} else {
				viewModel.loadData(bucketItemId = bucketItemId, showType = ShowType.MOVIE)
				lifecycleScope.launch(Dispatchers.Default) {
					loaderCoroutineScope?.cancel()
					loaderCoroutineScope = this
					combine(viewModel.data, viewModel.thumbnail) { data, thumbnail ->
						Pair(data, thumbnail)
					}.collect { (data, thumbnail) ->
						val showData = ShowData(data)
						viewModel.key.tryEmit(showData.movieData?.id)
						screenViewModel.loadData(data = showData.movieData?.toJsonString())
						screenViewModel.loadThumbnail(thumbnail = thumbnail?.decodeBase64ToBitmap())
					}
				}
			}
		}
	}

	private fun initTvData(isNew : Boolean, bucketId : RealmUUID, bucketItemId : RealmUUID?) {
		if (isNew) {
			val hasTvId = intent.hasExtra(Extra.Companion.Extra.TV_ID.name)

			if (hasTvId) {
				val tvId = intent.getStringExtra(Extra.Companion.Extra.TV_ID.name)

				if (tvId == null) {
					Toast.makeText(this.applicationContext, "Error reading info.", Toast.LENGTH_SHORT).show()
					finish()
				} else {
					viewModel.initData(bucketId = bucketId, bucketType = BucketType.SHOW, showType = ShowType.TV)
					screenViewModel.initData(id = tvId, data = null)
				}
			} else {
				Toast.makeText(this.applicationContext, "Error reading info.", Toast.LENGTH_SHORT).show()
				finish()
			}
		} else {
			if (bucketItemId == null) {
				Toast.makeText(this.applicationContext, "Error reading info.", Toast.LENGTH_SHORT).show()
				finish()
			} else {
				viewModel.loadData(bucketItemId = bucketItemId, showType = ShowType.TV)
				lifecycleScope.launch(Dispatchers.Default) {
					loaderCoroutineScope?.cancel()
					loaderCoroutineScope = this
					combine(viewModel.data, viewModel.thumbnail) { data, thumbnail ->
						Pair(data, thumbnail)
					}.collect { (data, thumbnail) ->
						val showData = ShowData(data)
						viewModel.key.tryEmit(showData.tvData?.id)
						screenViewModel.loadData(data = showData.tvData?.toJsonString())
						screenViewModel.loadThumbnail(thumbnail = thumbnail?.decodeBase64ToBitmap())
					}
				}
			}
		}
	}

	private fun onShare(data : String) {
		Intent(Intent.ACTION_SEND).apply {
			type = "text/html"
			putExtra(Intent.EXTRA_SUBJECT, data)
//			putExtra(Intent.EXTRA_TEXT, Html.fromHtml(shareText, Html.FROM_HTML_SEPARATOR_LINE_BREAK_LIST))
			putExtra(Intent.EXTRA_TEXT, data)

			if (resolveActivity(this@BucketItemActivity.packageManager) != null) startActivity(Intent.createChooser(this, "Share using"))
			else Toast.makeText(this@BucketItemActivity, "No app found on your phone which can perform this action", Toast.LENGTH_SHORT).show()
		}
	}
}
