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

			attachmentDir.listFiles()?.forEach { file ->
				when(val filePreview = FilePreviewer.getPreview(file = file, context = context)) {
					is PreviewData.Image -> {
						attachmentThumbnailMap[parentId] = Pair(hashCode, filePreview)
						Log.d("npr71", "file : ${file.name} : image")
						return filePreview
					}
					is PreviewData.Video -> {
						attachmentThumbnailMap[parentId] = Pair(hashCode, filePreview)
						Log.d("npr71", "file : ${file.name} : video")
						return filePreview
					}
					is PreviewData.Pdf -> {
						attachmentThumbnailMap[parentId] = Pair(hashCode, filePreview)
						Log.d("npr71", "file : ${file.name} : pdf")
						return filePreview
					}
					is PreviewData.Audio -> {
						attachmentThumbnailMap[parentId] = Pair(hashCode, filePreview)
						Log.d("npr71", "file : ${file.name} : audio")
						return filePreview
					}
					else -> Unit
				}
				Log.d("npr71", "file : ${file.name} : else")
			}

			return null
		} else {
			Log.d("npr71", "getAttachmentThumbnail : $parentId : cache found : ${cachedAttachmentThumbnail.second::class.simpleName}")
			return cachedAttachmentThumbnail.second
		}
	}
}
