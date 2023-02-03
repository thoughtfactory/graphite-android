package com.syncodec.graphite.presentation.bucketItem.composable.screen

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import com.syncodec.graphite.utils.Quadruple

abstract class AbstractBucketScreenViewModel: ViewModel() {
	abstract fun observeData()
	abstract fun  initData(id : String, data : String?)
	abstract fun  loadData(data : String?, thumbnail: String? = null)
	abstract fun getData() : Quadruple<String?, String?, String?, String?>
	abstract fun retrieveThumbnail(data : String?, onSuccess : (Bitmap) -> Unit)
}
