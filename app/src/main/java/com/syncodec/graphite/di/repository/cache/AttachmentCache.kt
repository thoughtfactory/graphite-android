package com.syncodec.graphite.di.repository.cache

import android.content.Context
import android.util.Log
import com.syncodec.graphite.di.repository.AttachmentRepository.Companion.attachmentDirPath
import com.syncodec.graphite.presentation.common.attachment.previewer.FilePreviewer
import com.syncodec.graphite.presentation.common.attachment.previewer.PreviewData
import io.realm.kotlin.types.RealmUUID
import java.io.File
import java.util.concurrent.ConcurrentHashMap


object AttachmentCache {

	private val attachmentThumbnailMap: ConcurrentHashMap<RealmUUID, Pair<Int, PreviewData>> = ConcurrentHashMap()

	suspend fun getAttachmentThumbnail(parentId: RealmUUID, hashCode: Int, context: Context): PreviewData? {

		val cachedAttachmentThumbnail = attachmentThumbnailMap[parentId]

		if (cachedAttachmentThumbnail == null || hashCode != cachedAttachmentThumbnail.first) {


			val attachmentDir = File(context.attachmentDirPath(parentId = parentId))
			val previewDataMap = attachmentDir.listFiles()?.map { file -> FilePreviewer.getPreview(file = file, context = context) }

			if (previewDataMap == null) {
//				Log.d("npr71", "getAttachmentThumbnail : $parentId : no attachment")
				return null
			} else {
//				Log.d("npr71", "getAttachmentThumbnail : $parentId : cache not found")

				var newCacheData: PreviewData? = previewDataMap.filterIsInstance<PreviewData.Image>().firstOrNull()
				if (newCacheData != null) {
					attachmentThumbnailMap[parentId] = Pair(hashCode, newCacheData)
					return newCacheData
				}

				newCacheData = previewDataMap.filterIsInstance<PreviewData.Video>().firstOrNull()
				if (newCacheData != null) {
					attachmentThumbnailMap[parentId] = Pair(hashCode, newCacheData)
					return newCacheData
				}

				newCacheData = previewDataMap.filterIsInstance<PreviewData.Pdf>().firstOrNull()
				if (newCacheData != null) {
					attachmentThumbnailMap[parentId] = Pair(hashCode, newCacheData)
					return newCacheData
				}

				newCacheData = previewDataMap.filterIsInstance<PreviewData.Audio>().firstOrNull()
				if (newCacheData != null) {
					attachmentThumbnailMap[parentId] = Pair(hashCode, newCacheData)
					return newCacheData
				}

				newCacheData = previewDataMap.firstOrNull()
				return if (newCacheData != null) {
					attachmentThumbnailMap[parentId] = Pair(hashCode, newCacheData)
					newCacheData
				} else {
					null
				}
			}
		} else {
			Log.d("npr71", "getAttachmentThumbnail : $parentId : cache found")
			return cachedAttachmentThumbnail.second
		}
	}
}
