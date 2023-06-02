package com.syncodec.graphite.service.syncInator

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import androidx.lifecycle.LifecycleService
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.DeletedAttachment
import com.syncodec.graphite.di.model.serializer.RealmUUIDSerializer
import com.syncodec.graphite.di.repository.repository.Repository
import com.syncodec.graphite.service.syncInator.SyncInatorService.Companion.SyncStatus.AutoSyncDisabled
import com.syncodec.graphite.service.syncInator.SyncInatorService.Companion.SyncStatus.Connected
import com.syncodec.graphite.service.syncInator.SyncInatorService.Companion.SyncStatus.CredentialError
import com.syncodec.graphite.service.syncInator.SyncInatorService.Companion.SyncStatus.Failed
import com.syncodec.graphite.service.syncInator.SyncInatorService.Companion.SyncStatus.Idle
import com.syncodec.graphite.service.syncInator.SyncInatorService.Companion.SyncStatus.Init
import com.syncodec.graphite.service.syncInator.SyncInatorService.Companion.SyncStatus.Locked
import com.syncodec.graphite.service.syncInator.SyncInatorService.Companion.SyncStatus.Syncing
import com.syncodec.graphite.utils.dataStore.SyncDataStoreInstance
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.Serializable
import org.koin.android.ext.android.inject

abstract class SyncInatorService : LifecycleService() {

	var syncProvider: String? = null
	private fun moveToForeground() {

		val channelId = "SyncService"
		val channelName = "Sync"
		val chan = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW)
		chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
		getSystemService(NotificationManager::class.java).createNotificationChannel(chan)

		val notification: Notification = Notification.Builder(this, channelId)
			.setContentTitle("Graphite")
			.setContentText("Synchronizing with ${syncProvider ?: "cloud"}. Process will stop automatically when completed.")
			.setSmallIcon(R.drawable.ic_noti_cloud_sync)
			.setOngoing(true)
			.build()

		startForeground(71, notification)
	}

	override fun onCreate() {
		super.onCreate()
		syncDataStoreInstance = SyncDataStoreInstance(this)
	}

	override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
		super.onStartCommand(intent, flags, startId)
		syncProvider = intent?.getStringExtra("syncProvider")
		moveToForeground()
		return START_STICKY
	}

	override fun onUnbind(intent: Intent?): Boolean {
		return super.onUnbind(intent)
	}

	override fun onDestroy() {
		super.onDestroy()
	}

	//	Don't make these private or lateinit
	val repository: Repository by inject()

	val syncStatus: MutableStateFlow<SyncStatus> = MutableStateFlow(SyncStatus.Init)
	lateinit var syncDataStoreInstance: SyncDataStoreInstance

	companion object {
		data class SyncObjectStatus(
			val toUpSyncCount: Int,
			val toDownSyncCount: Int,
			val isSynced: Boolean,
		)

		/**
		 *   Possible Sync Status
		 *   *   [Init]: Initial state. Can be triggered to sync. Automatic sync will be triggered.
		 *   *   [Idle]: Synced with Dropbox. Can be triggered to sync again.
		 *   *   [Locked]: Locked for sync. Can be triggered to sync again.
		 *   *   [Connected]: Connected to Dropbox. Cannot be triggered to sync.
		 *   *   [Syncing]: Syncing with Dropbox. Cannot be triggered to sync.
		 *   *   [AutoSyncDisabled]: Auto sync disabled. Can be triggered to sync but won't resync.
		 *   *   [Failed]: Failed for sync. Can be triggered to sync again.
		 *   *   [CredentialError]: Failed for sync due to credential error. Connect with Dropbox again.
		 */
		sealed class SyncStatus() {
			/**
			 * Init: Initial state. Can be triggered to sync. Automatic sync will be triggered.
			 */
			object Init : SyncStatus()

			/**
			 * Idle: Synced with Dropbox. Can be triggered to sync again.
			 */
			data class Idle(val syncedTimestamp: Long, val isAutoSyncDisabled: Boolean) : SyncStatus()

			/**
			 * Locked: Locked for sync. Can be triggered to sync again.
			 */
			object Locked : SyncStatus()

			/**
			 * Connected: Connected to Dropbox. Cannot be triggered to sync.
			 */
			object Connected : SyncStatus()

			/**
			 * Syncing: Syncing with Dropbox. Cannot be triggered to sync.
			 */
			data class Syncing(
				val sessionId: String = RealmUUID.random().toString(),
				val chapterSyncObjectStatus: SyncObjectStatus? = null,
				val noteSyncObjectStatus: SyncObjectStatus? = null,
				val bucketSyncObjectStatus: SyncObjectStatus? = null,
				val bucketItemSyncObjectStatus: SyncObjectStatus? = null,
				val tagSyncObjectStatus: SyncObjectStatus? = null,
				val attachmentSyncObjectStatus: SyncObjectStatus? = null,
			) : SyncStatus()

			/**
			 * AutoSyncDisabled: Auto sync disabled. Can be triggered to sync but won't resync.
			 */
			object AutoSyncDisabled : SyncStatus()

			/**
			 * Failed: Failed for sync. Can be triggered to sync again.
			 */
			data class Failed(val reason: String) : SyncStatus()

			/**
			 * CredentialError: Failed for sync due to credential error. Connect with Dropbox again.
			 */
			object CredentialError : SyncStatus()
		}

		@Serializable
		data class ObjectMetadata(
			val modifiedTimestamp: Long,
			val hash: String,
			val isDeleted: Boolean
		)

		/**
		 * Specially used to calculate difference between state of local and remote attachment and take action accordingly.
		 *
		 * @author pushpull
		 * @since 2.4.0
		 * @param fileName [AttachmentMetadata.fileName] Exact name of the attachment as stored in storage. Represented by [java.io.File]
		 * @param parentId [AttachmentMetadata.parentId] Unique identifier of the parent object. Represented by [RealmUUID] but stored as a folder with [parentId] as name in storage.
		 * @param isDeleted [AttachmentMetadata.isDeleted] Whether the attachment is deleted or not.
		 */
		@Serializable
		data class AttachmentMetadata(
			val fileName: String,
			@Serializable(with = RealmUUIDSerializer::class)
			val parentId: RealmUUID,
			val isDeleted: Boolean,
		) {
			fun asDeleted(): AttachmentMetadata = AttachmentMetadata(fileName, parentId, true)

			fun toDeletedAttachment(): DeletedAttachment =
				DeletedAttachment().apply { this.fileName = this@AttachmentMetadata.fileName; this.parentId = this@AttachmentMetadata.parentId }

			fun toAttachmentIdentity(): AttachmentIdentity = AttachmentIdentity(fileName, parentId)

			override fun hashCode(): Int {
				var result = fileName.hashCode()
				result = 31 * result + parentId.hashCode()
				result = 31 * result + isDeleted.hashCode()
				return result
			}

			override fun equals(other: Any?): Boolean {
				if (this === other) return true
				if (other !is AttachmentMetadata) return false

				if (fileName != other.fileName) return false
				if (parentId != other.parentId) return false
				if (isDeleted != other.isDeleted) return false

				return true
			}

			companion object {
				/**
				 * Uniquely identifies an attachment using fileName and parentId.
				 * This is same as [DeletedAttachment]. Did some fuck up and forget that exactly same class already exists.
				 * Don't want to delete this class because it's name represents exactly what it's supposed to do and can't
				 * delete or rename [DeletedAttachment] because that will require migration as it is a [io.realm.kotlin.types.RealmObject]
				 *
				 * @author pushpull
				 * @since 2.4.0
				 * @param fileName [AttachmentMetadata.fileName] Exact name of the attachment as stored in storage. Represented by [java.io.File]
				 * @param parentId [AttachmentMetadata.parentId] Unique identifier of the parent object. Represented by [RealmUUID] but stored as a folder with [parentId] as name in storage.
				 */
				data class AttachmentIdentity(val fileName: String, val parentId: RealmUUID)
			}
		}

		sealed class Operation {
			object Upsert : Operation()
			class Delete : Operation()
		}
	}
}
