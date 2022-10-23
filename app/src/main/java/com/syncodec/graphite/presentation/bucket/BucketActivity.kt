package com.syncodec.graphite.presentation.bucket

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.syncodec.graphite.presentation.bucket.composable.screen.BucketScreen
import com.syncodec.graphite.presentation.ui.BaseContent
import com.syncodec.graphite.utils.Extra
import io.realm.kotlin.types.ObjectId


class BucketActivity: ComponentActivity() {

	private val viewModel by viewModels<BucketViewModel>()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		val hasBucketId = intent.hasExtra(Extra.Companion.Constant.BUCKET_ID.name)
		if (hasBucketId) {
			val bucketId = intent.getStringExtra(Extra.Companion.Constant.BUCKET_ID.name)?.let { ObjectId.from(it) }
			if (bucketId != null) {
				viewModel.loadAndViewData(bucketId)
			} else {
				Log.i("npr71", "Bucket id is null")
				finish()
			}
		} else {
			Log.i("npr71", "Bucket id is not provided")
			finish()
		}

		setContent {
			BaseContent {
				val systemUiController = rememberSystemUiController()
				systemUiController.setStatusBarColor(if (isSystemInDarkTheme()) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onBackground)
				systemUiController.setNavigationBarColor(if (isSystemInDarkTheme()) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onBackground)

				val bucketObject by viewModel.bucketObject

				BucketScreen(bucketObject = bucketObject,)
			}
		}
	}
}
