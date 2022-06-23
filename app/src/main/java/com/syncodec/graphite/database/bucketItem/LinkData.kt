package com.syncodec.graphite.database.bucketItem

import android.graphics.Bitmap
import androidx.annotation.Keep

@Keep
data class LinkData(
	val title: String?,
	val description: String?,
	val image: Bitmap?,
	val url: String?,
	val originalUrl: String?,
	val type: String?,
	val siteName: String?
)
