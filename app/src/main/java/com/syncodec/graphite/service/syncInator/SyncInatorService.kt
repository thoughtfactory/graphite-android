package com.syncodec.graphite.service.syncInator

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.syncodec.graphite.R
import com.syncodec.graphite.di.repository.LockableRepo
import com.syncodec.graphite.di.repository.Repository
import com.syncodec.graphite.utils.dataStore.SyncDataStoreInstance
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject


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

	abstract fun syncNow(forced: Boolean = false)

	companion object {
		/**
		 * Remote and local operation
		 *
		 * [ObjectOperation.UpSync.Upsert]
		 *
		 * [ObjectOperation.UpSync.Delete]
		 *
		 * [ObjectOperation.DownSync.Upsert]
		 *
		 * [ObjectOperation.DownSync.Delete]
		 *
		 * [ObjectOperation.NoOp.Skip]
		 *
		 * [ObjectOperation.NoOp.Conflict]
		 */
		sealed class ObjectOperation {

			/**
			 * @param path Destination where to up sync on Dropbox
			 */
			sealed class UpSync(open val realmUUID: RealmUUID, open val path: String, open val modifiedTimestamp: Long) : ObjectOperation() {

				/**
				 * @param realmUUID id of the object
				 * @param path Destination where to up sync on Dropbox
				 */
				data class Upsert(override val realmUUID: RealmUUID, override val path: String, override val modifiedTimestamp: Long) : UpSync(realmUUID = realmUUID, path = path, modifiedTimestamp = modifiedTimestamp)

				/**
				 * @param path Destination what to delete on Dropbox
				 */
				data class Delete(override val realmUUID: RealmUUID, override val path: String, override val modifiedTimestamp: Long) : UpSync(realmUUID = realmUUID, path = path, modifiedTimestamp = modifiedTimestamp)
			}

			/**
			 * @param realmUUID id of the object
			 */
			sealed class DownSync(open val realmUUID: RealmUUID) : ObjectOperation() {

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
			sealed class NoOp(open val realmUUID: RealmUUID, open val path: String) : ObjectOperation() {

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

		sealed class AttachmentOperation {
			data object Upsert : AttachmentOperation()
			data object Delete : AttachmentOperation()
			data object NoOp : AttachmentOperation()
		}
	}
}
