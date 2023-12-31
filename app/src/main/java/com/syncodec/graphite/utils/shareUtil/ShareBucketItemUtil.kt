package com.syncodec.graphite.utils.shareUtil

import com.syncodec.graphite.di.model.local.BucketItemData
import com.syncodec.graphite.di.model.local.BucketItemObject
import com.syncodec.graphite.di.model.local.BucketItemState


object ShareBucketItemUtil {

	fun getTodoItemShareText(bucketItemList: List<BucketItemObject>): String {
		return bucketItemList
			.map { "${getCheckString(it.state)} ${it.title}" }
			.fold("") { acc, s -> "$acc$s\n" }
	}

	fun getBookItemShareText(bucketItemList: List<BucketItemObject>): String {
		return bucketItemList
			.map { "${it.title} - ${it.getBucketItemData<BucketItemData.BookData.OpenLibraryBookData>()?.getURL()}" }
			.fold("") { acc, s -> "$acc$s\n" }
	}

	fun getShowItemShareText(bucketItemList: List<BucketItemObject>): String {
		return bucketItemList
			.map { "${it.title} - ${it.getBucketItemData<BucketItemData.BookData.OpenLibraryBookData>()?.getURL()}" }
			.fold("") { acc, s -> "$acc$s\n" }
	}

	fun getLinkItemShareText(bucketItemList: List<BucketItemObject>): String {
		return bucketItemList
			.map { "${it.title} - ${it.getBucketItemData<BucketItemData.LinkData>()?.getURL()}" }
			.fold("") { acc, s -> "$acc$s\n" }
	}

	private fun getCheckString(state: String): String {
		return when (state) {
			BucketItemState.ALPHA.name -> "[]"
			BucketItemState.BETA.name -> "[-]"
			BucketItemState.GAMMA.name -> "[x]"
			else -> "[]"
		}
	}
}
