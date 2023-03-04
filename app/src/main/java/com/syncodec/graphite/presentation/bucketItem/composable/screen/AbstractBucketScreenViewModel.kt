package com.syncodec.graphite.presentation.bucketItem.composable.screen

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import com.syncodec.graphite.utils.Quadruple
import io.realm.kotlin.types.RealmUUID

abstract class AbstractBucketScreenViewModel: ViewModel() {
	abstract fun observeData()
	abstract fun  initData(id : String, data : String?)
	abstract fun  loadData(data : String?)
	abstract fun  loadThumbnail(thumbnail: Bitmap? = null)
	abstract fun getData() : Quadruple<String?, String?, String?, String?>
	abstract fun retrieveThumbnail(data : String?, onSuccess : (Bitmap) -> Unit)
}
