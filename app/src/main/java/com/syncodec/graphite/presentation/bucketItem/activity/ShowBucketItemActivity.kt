package com.syncodec.graphite.presentation.bucketItem.activity

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.BucketType
import com.syncodec.graphite.di.network.ShowType
import com.syncodec.graphite.presentation.bucketItem.composable.screen.MovieBucketItemScreen
import com.syncodec.graphite.presentation.bucketItem.composable.screen.TvBucketItemScreen
import com.syncodec.graphite.presentation.bucketItem.viewModel.ShowBucketItemViewModel
import com.syncodec.graphite.presentation.common.LoadingView
import com.syncodec.graphite.presentation.base.BaseComposable
import com.syncodec.graphite.utils.Extra
import io.realm.kotlin.types.RealmUUID
import org.koin.androidx.viewmodel.ext.android.viewModel


class ShowBucketItemActivity : ComponentActivity() {

	private val viewModel: ShowBucketItemViewModel by viewModel()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		unloadIntent()

		setContent {
			BaseComposable {

				val isNew by viewModel.isNew.collectAsState()
				val showType by viewModel.showType.collectAsState()
				val bucketItemObject by viewModel.bucketItemObject.collectAsState()

				when (showType) {
					ShowType.MOVIE -> MovieBucketItemScreen(
						isNew = isNew == true,
						bucketItemObject = bucketItemObject,
						onClickSave = { viewModel.putBucketItem() },
						onClickFavourite = { viewModel.onClickFavourite() },
						onClickLock = { viewModel.onClickLock() },
						onUpdateState = { viewModel.onChangeState(it) }
					)

					ShowType.TV -> TvBucketItemScreen(
						isNew = isNew == true,
						bucketItemObject = bucketItemObject,
						onClickSave = { viewModel.putBucketItem() },
						onClickFavourite = { viewModel.onClickFavourite() },
						onClickLock = { viewModel.onClickLock() },
						onUpdateState = { viewModel.onChangeState(it) }
					)

					else -> LoadingView()
				}
			}
		}
	}

	private fun unloadIntent() {
		val hasIsNew = intent.hasExtra(Extra.Companion.Extra.IsNew.name)
		val hasBucketId = intent.hasExtra(Extra.Companion.Extra.BUCKET_ID.name)
		val hasBucketType = intent.hasExtra(Extra.Companion.Extra.BUCKET_TYPE.name)

		if (!hasIsNew or !hasBucketId or !hasBucketType) {
			errorReadingData()
		} else {
			val isNew = intent.getBooleanExtra(Extra.Companion.Extra.IsNew.name, false)
			val bucketId = intent.getByteArrayExtra(Extra.Companion.Extra.BUCKET_ID.name)?.let { RealmUUID.from(it) }
			val bucketItemId = intent.getByteArrayExtra(Extra.Companion.Extra.BUCKET_ITEM_ID.name)?.let { RealmUUID.from(it) }
			val bucketType = intent.getStringExtra(Extra.Companion.Extra.BUCKET_TYPE.name)
			val tvId = intent.getStringExtra(Extra.Companion.Extra.TV_ID.name)
			val movieId = intent.getStringExtra(Extra.Companion.Extra.MOVIE_ID.name)

			when {
				bucketId == null -> errorReadingData()
				bucketType != BucketType.SHOW.name -> errorReadingData()
				tvId != null -> viewModel.initBucketItem(showId = tvId, parentId = bucketId, showType = ShowType.TV)
				movieId != null -> viewModel.initBucketItem(showId = movieId, parentId = bucketId, showType = ShowType.MOVIE)
				(bucketItemId != null) -> viewModel.readBucketItem(bucketItemId, parentId = bucketId)
				else -> errorReadingData()
			}
		}
	}

	private fun errorReadingData() {
		Toast.makeText(this, getText(R.string.toast_error_loading_data), Toast.LENGTH_SHORT).show()
		finish()
	}
}