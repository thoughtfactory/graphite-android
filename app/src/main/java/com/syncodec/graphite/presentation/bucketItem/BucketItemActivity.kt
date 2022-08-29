package com.syncodec.graphite.presentation.bucketItem

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.syncodec.graphite.di.model.BucketType
import com.syncodec.graphite.di.network.ShowType
import com.syncodec.graphite.presentation.ui.BaseContent
import com.syncodec.graphite.utils.Extra

class BucketItemActivity : ComponentActivity() {

	val viewModel by viewModels<BucketItemViewModel>()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		val hasIsNew = intent.hasExtra(Extra.Companion.Constant.IS_NEW.name)
		val hasBucketId = intent.hasExtra(Extra.Companion.Constant.BUCKET_ID.name)
		val hasBucketType = intent.hasExtra(Extra.Companion.Constant.BUCKET_TYPE.name)

		if (hasIsNew && hasBucketId && hasBucketType) {
			val isNew = intent.getBooleanExtra(Extra.Companion.Constant.IS_NEW.name, false)
			val bucketUId = intent.getStringExtra(Extra.Companion.Constant.BUCKET_ID.name)
			val _bucketType = intent.getStringExtra(Extra.Companion.Constant.BUCKET_TYPE.name)

			if (_bucketType == null || bucketUId == null) {
//				TODO Show error message
				finish()
			} else {
				try {
					when (val bucketType = BucketType.valueOf(_bucketType)) {
						BucketType.TODO -> null
						BucketType.BOOK -> if (isNew) initNewBookData(bucketUId, bucketType) else loadData(bucketUId, bucketType)
						BucketType.SHOW -> if (isNew) initNewShowData(bucketUId, bucketType) else loadData(bucketUId, bucketType)
						BucketType.LINK -> null
					}
				} catch (e: Exception) {
//					TODO Show error message
					Toast.makeText(this, "Error getting bucket type", Toast.LENGTH_SHORT).show()
					finish()
				}
			}
		} else {
//			TODO Show error
			Toast.makeText(this, "Bucket type or bucket id not found", Toast.LENGTH_SHORT).show()
			finish()
		}

		setContent {
			BaseContent {
				val systemUiController = rememberSystemUiController()
				systemUiController.setStatusBarColor(MaterialTheme.colorScheme.surface)
				systemUiController.setNavigationBarColor(MaterialTheme.colorScheme.background)

				BucketItemScreen()
			}
		}
	}

	private fun initNewBookData(bucketId: String, bucketType: BucketType) {
		try {
			val hasBookKey = intent.hasExtra(Extra.Companion.Constant.BOOK_KEY.name)
			val hasExtraData = intent.hasExtra(Extra.Companion.Constant.EXTRA_DATA.name)
			if (hasBookKey && hasExtraData) {
				val bookKey = intent.getStringExtra(Extra.Companion.Constant.BOOK_KEY.name)
				val extraData = intent.getSerializableExtra(Extra.Companion.Constant.EXTRA_DATA.name)
				if (bookKey == null) {
//				    TODO Show error message
					Toast.makeText(this, "Book key is null", Toast.LENGTH_SHORT).show()
					finish()
				} else {
					viewModel.initNewData(bucketId, bucketType, bookKey, extraData, null)
				}
			} else {
//			    TODO Show error
				Toast.makeText(this, "Book key or extra data is null", Toast.LENGTH_SHORT).show()
				finish()
			}
		} catch (e: Exception) {
//			TODO Show error
			Toast.makeText(this, "Unknown error", Toast.LENGTH_SHORT).show()
			finish()
		}
	}

	private fun initNewShowData(bucketId: String, bucketType: BucketType) {
		try {
			val hasMovieId = intent.hasExtra(Extra.Companion.Constant.MOVIE_UID.name)
			val hasExtraData = intent.hasExtra(Extra.Companion.Constant.EXTRA_DATA.name)
			val hasShowType = intent.hasExtra(Extra.Companion.Constant.SHOW_TYPE.name)

			if (hasMovieId && hasExtraData && hasShowType) {
				val movieId = intent.getStringExtra(Extra.Companion.Constant.MOVIE_UID.name)
				val extraData = intent.getSerializableExtra(Extra.Companion.Constant.EXTRA_DATA.name)
				val _showType = intent.getIntExtra(Extra.Companion.Constant.SHOW_TYPE.name, -1)

				if (movieId == null || _showType < 0) {
//				    TODO Show error message
					Toast.makeText(this, "Movie id is null", Toast.LENGTH_SHORT).show()
					finish()
				} else {
					val showType = ShowType.values()[_showType]
					viewModel.initNewData(bucketId, bucketType, movieId, extraData, showType)
				}
			} else {
//			    TODO Show error
				Toast.makeText(this, "Movie id or extra data is null", Toast.LENGTH_SHORT).show()
				finish()
			}
		} catch (e: Exception) {
//			TODO Show error
			Toast.makeText(this, "Unknown error", Toast.LENGTH_SHORT).show()
			finish()
		}
	}

	private fun loadData(bucketId: String, bucketType: BucketType) {
		val hasBucketItemId = intent.hasExtra(Extra.Companion.Constant.BUCKET_ITEM_ID.name)
		if (hasBucketItemId) {
			val bucketItemId = intent.getStringExtra(Extra.Companion.Constant.BUCKET_ITEM_ID.name)
			if (bucketItemId.isNullOrBlank()) {
//				TODO Show error message
				Toast.makeText(this, "Bucket item id is null", Toast.LENGTH_SHORT).show()
				finish()
			} else {
				viewModel.loadAndViewData(bucketId, bucketType, bucketItemId)
			}
		} else {
//			TODO Show error message
			Toast.makeText(this, "Bucket item id not found", Toast.LENGTH_SHORT).show()
			finish()
		}
	}
}
