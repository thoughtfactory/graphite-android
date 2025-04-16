package com.syncodec.graphite.presentation.bucketItem2

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.syncodec.graphite.R
import com.syncodec.graphite.di.modelObjectBox.BucketBoxEncrypted
import com.syncodec.graphite.presentation.bucketItem2.composable.screen.BucketItemScreen
import com.syncodec.graphite.presentation.ui.BaseComposable2
import com.syncodec.graphite.utils.IntentUtil.BucketItemActivityData
import com.syncodec.graphite.utils.IntentUtil.IntentData
import kotlinx.serialization.json.Json
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.getValue


class BucketItemActivity2 : ComponentActivity() {

    private val json: Json by lazy {
        Json {
            ignoreUnknownKeys = true
            isLenient = true
            classDiscriminator = "klass"
        }
    }


    private val viewModel by viewModel<BucketItemViewModel2>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val bucketType = getIntentData()

        setContent {
            BaseComposable2 {
                BucketItemScreen(bucketType = bucketType)
            }
        }
    }

    private fun getIntentData(): BucketBoxEncrypted.BucketType {
//        val bucketItemActivityData = (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) intent.getSerializableExtra(IntentData.BucketItemActivityData.name, BucketItemActivityData::class.java)
//        else intent.getSerializableExtra(IntentData.BucketItemActivityData.name)) as? BucketItemActivityData

        val bucketItemActivityData = try {
            json.decodeFromString<BucketItemActivityData>(string = intent.getStringExtra(IntentData.BucketItemActivityData.name) ?: "")
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }

        if (bucketItemActivityData == null) {
            Toast.makeText(this, getString(R.string.toast_no_bucket_id), Toast.LENGTH_SHORT).show()
            finish()
            return BucketBoxEncrypted.BucketType.Unknown
        } else {
            when (bucketItemActivityData) {
                is BucketItemActivityData.NewItem.BookItem -> {
                    viewModel.fetchBookData(parentId = bucketItemActivityData.parentId, olBookSearchResult = bucketItemActivityData.olBookSearchResult)
                    return BucketBoxEncrypted.BucketType.Book
                }

                is BucketItemActivityData.NewItem.ShowItem -> {
                    viewModel.fetchShowData(parentId = bucketItemActivityData.parentId, traktShowSearchResult = bucketItemActivityData.traktShowSearchResult)
                    return BucketBoxEncrypted.BucketType.Show
                }

                is BucketItemActivityData.LocalItem -> {
                    viewModel.loadData(bucketItemId = bucketItemActivityData.bucketItemId, parentId = bucketItemActivityData.parentId)
                    return bucketItemActivityData.bucketType
                }
            }
        }
    }
}
