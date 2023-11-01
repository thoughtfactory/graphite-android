package com.syncodec.graphite.presentation.bucketItem.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.syncodec.graphite.BuildConfig
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.local.BucketType
import com.syncodec.graphite.presentation.bucketItem.viewModel.BookBucketItemViewModel
import com.syncodec.graphite.presentation.bucketItem.composable.screen.BookBucketItemScreen
import com.syncodec.graphite.presentation.base.BaseComposable
import com.syncodec.graphite.utils.Extra
import com.syncodec.graphite.utils.shareUtil.ShareBucketItemUtil
import io.realm.kotlin.types.RealmUUID
import org.koin.androidx.viewmodel.ext.android.viewModel


class BookBucketItemActivity : ComponentActivity() {

	private val viewModel: BookBucketItemViewModel by viewModel()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		unloadIntent()

		setContent {
			BaseComposable {

				val isNew by viewModel.isNew.collectAsState()
				val bucketItemObject by viewModel.bucketItemObject.collectAsState()

				BookBucketItemScreen(
					isNew = isNew == true,
					bucketItemObject = bucketItemObject,
					onClickSave = viewModel::putBucketItem,
					onClickFavourite = viewModel::onClickFavourite,
					onClickLock = viewModel::onClickLock,
					onUpdateState = viewModel::onChangeState,
					onClickShare = { bucketItemObject?.let { share(ShareBucketItemUtil.getBookItemShareText(bucketItemList = listOf(it))) } },
					onConfirmDelete = { viewModel.delete(); finish() },
				)
			}
		}
	}

	private fun share(shareText: String) {
		Intent(Intent.ACTION_SEND).apply {
			type = "text/html"
			putExtra(Intent.EXTRA_TEXT, shareText)
			addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

			if (resolveActivity(packageManager) != null) startActivity(Intent.createChooser(this, "Share using"))
			else Toast.makeText(this@BookBucketItemActivity, "No app found on your device which can perform this action", Toast.LENGTH_SHORT).show()
		}
	}

	private fun unloadIntent() {
		val hasIsNew = intent.hasExtra(Extra.Companion.Extra.IsNew.name)
		val hasBucketId = intent.hasExtra(Extra.Companion.Extra.BUCKET_ID.name)
		val hasBucketType = intent.hasExtra(Extra.Companion.Extra.BUCKET_TYPE.name)

		if (!hasIsNew || !hasBucketId || !hasBucketType) {
			if (BuildConfig.DEBUG) Log.e("npr71", "hasIsNew : $hasIsNew : hasBucketId : $hasBucketId : $hasBucketType")
			errorReadingData()
		} else {
			val isNew = intent.getBooleanExtra(Extra.Companion.Extra.IsNew.name, false)
			val bucketId = intent.getByteArrayExtra(Extra.Companion.Extra.BUCKET_ID.name)?.let { RealmUUID.from(it) }
			val bucketItemId = intent.getByteArrayExtra(Extra.Companion.Extra.BUCKET_ITEM_ID.name)?.let { RealmUUID.from(it) }
			val bucketType = intent.getStringExtra(Extra.Companion.Extra.BUCKET_TYPE.name)
			val bookId = intent.getStringExtra(Extra.Companion.Extra.BOOK_ID.name)

			when {
				bucketId == null -> {
					if (BuildConfig.DEBUG) Log.e("npr71", "bucketId : null")
					errorReadingData()
				}

				bucketType != BucketType.BOOK.name -> {
					if (BuildConfig.DEBUG) Log.e("npr71", "bucketType != BucketType.BOOK.name")
					errorReadingData()
				}

				bookId != null -> viewModel.initBucketItem(bookId = bookId, parentId = bucketId)
				(bucketItemId != null) -> viewModel.readBucketItem(bucketItemId, parentId = bucketId)
				else -> {
					if (BuildConfig.DEBUG) Log.e("npr71", "unknown error")
					errorReadingData()
				}
			}
		}
	}

	private fun errorReadingData() {
		Toast.makeText(this, getText(R.string.toast_error_loading_data), Toast.LENGTH_SHORT).show()
		finish()
	}
}