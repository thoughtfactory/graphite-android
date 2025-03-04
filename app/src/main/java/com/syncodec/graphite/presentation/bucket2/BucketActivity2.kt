package com.syncodec.graphite.presentation.bucket2

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.syncodec.graphite.presentation.ui.BaseContent
import com.syncodec.graphite.utils.IntentUtil
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.getValue
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.bucket2.composable.screen.BucketScreen
import org.koin.compose.KoinContext


class BucketActivity2 : ComponentActivity() {

    private val viewModel by viewModel<BucketViewModel2>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        getIntentData()

        setContent {
            KoinContext {
                BaseContent {
                    BucketScreen()
                }
            }
        }
    }

    private fun getIntentData() {
        val bucketId = intent.getLongExtra(IntentUtil.IntentKey.BucketId.name, -1)
        if (bucketId == -1L) {
            Toast.makeText(this, getString(R.string.toast_no_bucket_id), Toast.LENGTH_SHORT).show()
            finish()
        } else {
            viewModel.loadData(bucketId = bucketId)
        }
    }
}

