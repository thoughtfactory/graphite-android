package com.syncodec.graphite.service.syncInator

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.local.DeletedAttachment
import com.syncodec.graphite.di.model.serializer.RealmUUIDSerializer
import com.syncodec.graphite.di.repository.LockableRepo
import com.syncodec.graphite.di.repository.Repository
import com.syncodec.graphite.utils.dataStore.SyncDataStoreInstance
import io.realm.kotlin.types.RealmUUID
import io.realm.kotlin.types.TypedRealmObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.koin.android.ext.android.inject
import kotlin.reflect.KFunction1


abstract class SyncInatorService : LifecycleService() {

	//	Don't make these private or lateinit
	val lockableRepo: LockableRepo by inject()
	val syncDataStoreInstance: SyncDataStoreInstance by inject()

	var syncProvider: String? = null

	var isUnbounded = false
	val isAutoSyncEnabled: MutableStateFlow<Boolean> = MutableStateFlow(false)

	val syncStat = MutableStateFlow<SyncStat>(SyncStat.Init)

	var repository: Repository? = null

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
		repository = lockableRepo.silentlyDecryptRepository()
		lifecycleScope.launch(Dispatchers.IO) {
			syncDataStoreInstance.isAutoSyncEnabledFlow.collectLatest {
				isAutoSyncEnabled.tryEmit(it)
			}
		}
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

	abstract fun syncNow()

	abstract fun forceSync()

	companion object {

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

		/**
		 * Remote and local operation
		 *
		 * [Operation.UpSync.Upsert]
		 *
		 * [Operation.UpSync.Delete]
		 *
		 * [Operation.DownSync.Upsert]
		 *
		 * [Operation.DownSync.Delete]
		 *
		 * [Operation.NoOp.Skip]
		 *
		 * [Operation.NoOp.Conflict]
		 */
		sealed class Operation {

			/**
			 * @param path Destination where to up sync on Dropbox
			 */
			sealed class UpSync(open val realmUUID: RealmUUID, open val path: String, open val modifiedTimestamp: Long) : Operation() {

				/**
				 * @param realmUUID id of the object
				 * @param path Destination where to up sync on Dropbox
				 */
				data class Upsert(override val realmUUID: RealmUUID, override val path : String, override val modifiedTimestamp : Long) : UpSync(realmUUID = realmUUID, path = path, modifiedTimestamp = modifiedTimestamp)

				/**
				 * @param path Destination what to delete on Dropbox
				 */
				data class Delete(override val realmUUID: RealmUUID, override val path : String, override val modifiedTimestamp : Long) : UpSync(realmUUID = realmUUID, path = path, modifiedTimestamp = modifiedTimestamp)
			}

			/**
			 * @param realmUUID id of the object
			 */
			sealed class DownSync(open val realmUUID: RealmUUID) : Operation() {

				/**
				 * @param path Source from where to down sync
				 */
				data class Upsert(override val realmUUID: RealmUUID, val path: String) : DownSync(realmUUID = realmUUID)

				/**
				 * @param realmUUID id of the object to delete
				 */
				data class Delete(override val realmUUID: RealmUUID) : DownSync(realmUUID = realmUUID)
			}

			/**
			 * @param realmUUID id of the object
			 * @param path remote path of the object
			 */
			sealed class NoOp(open val realmUUID: RealmUUID, open val path: String) : Operation() {

				/**
				 * Do nothing
				 */
				data class Skip(override val realmUUID: RealmUUID, override val path: String) : NoOp(realmUUID = realmUUID, path = path)

				/**
				 * Do nothing. No action as of now
				 */
				data class Conflict(override val realmUUID: RealmUUID, override val path: String) : NoOp(realmUUID = realmUUID, path = path)
			}
		}
	}
}
