package com.syncodec.graphite.presentation.bucketItem

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.syncodec.graphite.di.model.BucketType
import com.syncodec.graphite.presentation.ui.BaseContent
import com.syncodec.graphite.utils.Extra
import io.realm.kotlin.types.ObjectId


class BucketItemActivity: ComponentActivity() {

	val viewModel by viewModels<BucketItemViewModel>()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		val hasIsNew = intent.hasExtra(Extra.Companion.Constant.IS_NEW.name)
		val hasBucketId = intent.hasExtra(Extra.Companion.Constant.BUCKET_ID.name)
		val hasBucketType = intent.hasExtra(Extra.Companion.Constant.BUCKET_TYPE.name)

		if (hasIsNew && hasBucketId && hasBucketType) {
			val isNew = intent.getBooleanExtra(Extra.Companion.Constant.IS_NEW.name, false)
			val bucketId = intent.getStringExtra(Extra.Companion.Constant.BUCKET_ID.name)?.let { ObjectId.from(it) }
			val bucketItemId = intent.getStringExtra(Extra.Companion.Constant.BUCKET_ITEM_ID.name)?.let { ObjectId.from(it) }
			val _bucketType = intent.getStringExtra(Extra.Companion.Constant.BUCKET_TYPE.name)

			if (bucketId == null || _bucketType == null) {
				finish()
			} else {
				try {
					val bucketType = BucketType.valueOf(_bucketType)
					if (bucketType == BucketType.UNKNOWN) {
						finish()
					} else {
						viewModel.initData(isNew = isNew, bucketId = bucketId, bucketItemId = bucketItemId, bucketType = bucketType, intent = intent)
					}
				} catch (e: Exception) {
					e.printStackTrace()
					finish()
				}
			}

		} else {
			finish()
		}

		setContent {
			BaseContent {
				val systemUiController = rememberSystemUiController()
				systemUiController.setStatusBarColor(if (isSystemInDarkTheme()) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onBackground)
				systemUiController.setNavigationBarColor(if (isSystemInDarkTheme()) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onBackground)

				BucketItemScreen(
					viewModel = viewModel
				)
			}
		}
	}
}
