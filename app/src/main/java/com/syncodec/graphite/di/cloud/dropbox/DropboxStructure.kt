package com.syncodec.graphite.di.cloud.dropbox

import com.dropbox.core.v2.files.Metadata
import com.google.errorprone.annotations.Keep
import com.syncodec.graphite.BuildConfig
import com.syncodec.graphite.di.model.local.AttachmentIdentity
import com.syncodec.graphite.di.repository.Repository
import com.syncodec.graphite.service.syncInator.SyncInatorService
import kotlinx.serialization.Serializable
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale


/**
 * @param modifiedTimestamp Timestamp of the last modification
 * @param hash Dropbox content hash. Empty string if the object is deleted
 * @param isDeleted True if the object is deleted
 * @see [https://www.dropbox.com/developers/reference/content-hash]
 */
@Keep
@Serializable
data class DropboxObjectMetadata(
	val modifiedTimestamp: Long,
	val hash: String,
	val isDeleted: Boolean
) {
	companion object {
		fun fromMetadata(metadata: Metadata): DropboxObjectMetadata {
			val metadataJsonObject = JSONObject(metadata.toStringMultiline())
			val timestamp = try {
				SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH).parse(metadataJsonObject.optString("client_modified"))?.time ?: 0
			} catch (e: Exception) {
				if (BuildConfig.DEBUG) e.printStackTrace()
				0
			}
			val hash = metadataJsonObject.optString("content_hash")

			return DropboxObjectMetadata(modifiedTimestamp = timestamp, hash = hash, isDeleted = false)
		}
	}
}

@Keep
@Serializable
data class DropboxAttachmentMetadata(
	val attachmentIdentity: AttachmentIdentity,
	val isDeleted: Boolean
)

data class CombinedAttachmentMetadata(
	val attachmentIdentity: AttachmentIdentity,
	val isParentDeleted: Boolean,
	val isRemoteExist: Boolean,
	val isRemoteDeleted: Boolean,
	val isLocalExist: Boolean,
	val isLocalDeleted: Boolean,
) {
//			https://graphite.youtrack.cloud/articles/GRAPHITE-A-29/Attachment-sync

	val attachmentMetadata: DropboxAttachmentMetadata? =
		if (isParentDeleted) DropboxAttachmentMetadata(attachmentIdentity = attachmentIdentity, isDeleted = true)
		else when {
			isRemoteDeleted -> DropboxAttachmentMetadata(attachmentIdentity = attachmentIdentity, isDeleted = true)
			isLocalDeleted -> DropboxAttachmentMetadata(attachmentIdentity = attachmentIdentity, isDeleted = true)
			!isRemoteExist && !isRemoteDeleted && isLocalExist && !isLocalDeleted -> DropboxAttachmentMetadata(attachmentIdentity = attachmentIdentity, isDeleted = false)
			isRemoteExist && !isRemoteDeleted && !isLocalDeleted -> DropboxAttachmentMetadata(attachmentIdentity = attachmentIdentity, isDeleted = false)
			else -> null
		}

	val upSyncOp: SyncInatorService.Companion.AttachmentOperation =
		if (isParentDeleted) when {
			isRemoteExist -> SyncInatorService.Companion.AttachmentOperation.Delete
			else -> SyncInatorService.Companion.AttachmentOperation.NoOp
		} else when {
			isRemoteExist && isRemoteDeleted -> SyncInatorService.Companion.AttachmentOperation.Delete
			isRemoteExist && !isRemoteDeleted && isLocalDeleted -> SyncInatorService.Companion.AttachmentOperation.Delete
			!isRemoteExist && !isRemoteDeleted && isLocalExist && !isLocalDeleted -> SyncInatorService.Companion.AttachmentOperation.Upsert
			else -> SyncInatorService.Companion.AttachmentOperation.NoOp
		}

	val downSyncOp: SyncInatorService.Companion.AttachmentOperation =
		if (isParentDeleted) when {
			isLocalExist -> SyncInatorService.Companion.AttachmentOperation.Delete
			else -> SyncInatorService.Companion.AttachmentOperation.NoOp
		} else when {
			isRemoteExist && !isRemoteDeleted && !isLocalExist && !isLocalDeleted -> SyncInatorService.Companion.AttachmentOperation.Upsert
			isLocalExist && isLocalDeleted -> SyncInatorService.Companion.AttachmentOperation.Delete
			isRemoteDeleted && isLocalExist -> SyncInatorService.Companion.AttachmentOperation.Delete
			else -> SyncInatorService.Companion.AttachmentOperation.NoOp
		}

	val dropboxRemotePath = "${DropboxApi.Companion.DropboxPath.Attachment}/${attachmentIdentity.parentId}/${attachmentIdentity.fileName}"

	fun getFile(repository: Repository): File = repository.attachmentRepository.getAttachment(attachmentIdentity = attachmentIdentity)
}

