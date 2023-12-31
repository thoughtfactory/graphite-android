package com.syncodec.graphite.presentation.bucketItem.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.local.BucketItemData
import com.syncodec.graphite.di.model.local.BucketType
import com.syncodec.graphite.presentation.base.BaseComposable
import com.syncodec.graphite.presentation.bucketItem.composable.screen.MovieBucketItemScreen
import com.syncodec.graphite.presentation.bucketItem.composable.screen.TvBucketItemScreen
import com.syncodec.graphite.presentation.bucketItem.viewModel.ShowBucketItemViewModel
import com.syncodec.graphite.utils.Extra
import com.syncodec.graphite.utils.shareUtil.ShareBucketItemUtil
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
				val bucketItemObject by viewModel.bucketItemObject.collectAsState()
				val tmdbData by remember(bucketItemObject?.bucketItemDataJson) { derivedStateOf { bucketItemObject?.getBucketItemData<BucketItemData.ShowData.TMDbData>() } }

				tmdbData?.let { tmdbData1 ->
					when (tmdbData1) {
						is BucketItemData.ShowData.TMDbData.TMDbMovieData -> MovieBucketItemScreen(
							isNew = isNew == true,
							bucketItemObject = bucketItemObject,
							movieData = tmdbData1,
							onClickSave = viewModel::putBucketItem,
							onClickFavourite = viewModel::onClickFavourite,
							onClickLock = viewModel::onClickLock,
							onUpdateState = viewModel::onChangeState,
							onClickShare = { bucketItemObject?.let { share(ShareBucketItemUtil.getShowItemShareText(bucketItemList = listOf(it))) } },
							onConfirmDelete = { viewModel.delete(); finish() },
						)

						is BucketItemData.ShowData.TMDbData.TMDbTvData -> TvBucketItemScreen(
							isNew = isNew == true,
							bucketItemObject = bucketItemObject,
							tvData = tmdbData1,
							onClickSave = viewModel::putBucketItem,
							onClickFavourite = viewModel::onClickFavourite,
							onClickLock = viewModel::onClickLock,
							onUpdateState = viewModel::onChangeState,
							onClickShare = { bucketItemObject?.let { share(ShareBucketItemUtil.getShowItemShareText(bucketItemList = listOf(it))) } },
							onConfirmDelete = { viewModel.delete(); finish() },
						)
					}
				}
			}
		}
	}

	private fun share(shareText: String) {
		Intent(Intent.ACTION_SEND).apply {
			type = "text/html"
			putExtra(Intent.EXTRA_TEXT, shareText)
			addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

			if (resolveActivity(packageManager) != null) startActivity(Intent.createChooser(this, "Share using"))
			else Toast.makeText(this@ShowBucketItemActivity, "No app found on your device which can perform this action", Toast.LENGTH_SHORT).show()
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
				tvId != null -> viewModel.initBucketItem(showId = tvId, parentId = bucketId, tmdbDataType = BucketItemData.ShowData.TMDbData.TMDbTvData::class.simpleName)
				movieId != null -> viewModel.initBucketItem(showId = movieId, parentId = bucketId, tmdbDataType = BucketItemData.ShowData.TMDbData.TMDbMovieData::class.simpleName)
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