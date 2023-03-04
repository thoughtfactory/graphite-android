package com.syncodec.graphite.presentation.bucket

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.syncodec.graphite.di.model.BucketItemObject
import com.syncodec.graphite.presentation.bucket.composable.bottomSheet.BucketBottomSheetViewModel
import com.syncodec.graphite.presentation.bucket.composable.screen.BucketScreen
import com.syncodec.graphite.presentation.bucket.composable.screen.BucketScreenCommonViewModel
import com.syncodec.graphite.presentation.ui.BaseContent
import com.syncodec.graphite.utils.Extra
import io.realm.kotlin.types.RealmUUID
import org.koin.androidx.viewmodel.ext.android.viewModel


class BucketActivity : ComponentActivity() {

	private val viewModel by viewModel<BucketViewModel>()
	private val bucketScreenCommonViewModel by viewModel<BucketScreenCommonViewModel>()
	private val bucketBottomSheetViewModel by viewModel<BucketBottomSheetViewModel>()

	override fun onCreate(savedInstanceState : Bundle?) {
		super.onCreate(savedInstanceState)

		val hasBucketId = intent.hasExtra(Extra.Companion.Extra.BUCKET_ID.name)
		if (hasBucketId) {
			val bucketId = intent.getByteArrayExtra(Extra.Companion.Extra.BUCKET_ID.name)?.let { RealmUUID.from(it) }
			bucketId?.let { realmUUID ->
				viewModel.loadAndViewData(realmUUID)
				bucketScreenCommonViewModel.initBucket(realmUUID)
				bucketBottomSheetViewModel.initBucket(realmUUID)
			} ?: run {
				Toast.makeText(this, "Error loading bucket. No id specified.", Toast.LENGTH_SHORT).show()
				finish()
			}
		} else {
			Toast.makeText(this, "Error loading bucket. No id specified.", Toast.LENGTH_SHORT).show()
			finish()
		}

		setContent {
			BaseContent {
				BucketScreen(afterDeleteBucket = { finish() })
			}
		}
	}

	private fun onShare(bucketItemObjectList : List<BucketItemObject>, shareAll : Boolean) {

//		val baseUrl = when (bucketObject.bucketType) {
//			BucketType.TODO.name -> ""
//			BucketType.BOOK.name -> " - https://openlibrary.org"
//			BucketType.SHOW.name -> " - https://www.themoviedb.org/"
//			BucketType.LINK.name -> ""
//			BucketType.UNKNOWN.name -> ""
//			else -> ""
//		}
//
//		var shareText = ""
//		bucketItemObjectList.filter { if (shareAll) true else it.id in selectedRealmUUIDList }.forEach {
//			val connector = when (it.getShowData()?.type) {
//				ShowType.TV -> "tv/"
//				ShowType.MOVIE -> "movie/"
//				else -> ""
//			}
//			shareText += "${it.title}$baseUrl$connector${if (bucketObject.bucketType == BucketType.TODO.name) "" else it.key}\n"
//		}
//
//		Intent(Intent.ACTION_SEND).apply {
//			type = "text/html"
//			putExtra(Intent.EXTRA_SUBJECT, bucketObject.title ?: bucketObject.bucketType)
////			putExtra(Intent.EXTRA_TEXT, Html.fromHtml(shareText, Html.FROM_HTML_SEPARATOR_LINE_BREAK_LIST))
//			putExtra(Intent.EXTRA_TEXT, shareText)
//
//			if (resolveActivity(this@BucketActivity.packageManager) != null) startActivity(Intent.createChooser(this, "Share using"))
//			else Toast.makeText(this@BucketActivity, "No app found on your device which can perform this action", Toast.LENGTH_SHORT).show()
//		}
	}
}
