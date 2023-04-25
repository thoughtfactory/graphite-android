package com.syncodec.graphite.service.syncService

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.widget.Toast
import androidx.lifecycle.LifecycleService
import com.syncodec.graphite.R
import com.syncodec.graphite.di.repository.repository.Repository
import com.syncodec.graphite.utils.DataStoreInstance
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.android.ext.android.inject

abstract class SyncerService : LifecycleService() {
	private fun moveToForeground() {
		val channelId = "SyncService"
		val channelName = "Sync"
		val chan = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW)
		chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
		getSystemService(NotificationManager::class.java).createNotificationChannel(chan)

		val notification : Notification = Notification.Builder(this, channelId)
			.setContentTitle("Graphite")
			.setContentText("Synchronizing with Dropbox. Process will stop automatically when completed.")
			.setSmallIcon(R.drawable.ic_ring)
			.setOngoing(true)
			.build()

		startForeground(71, notification)
	}

	override fun onCreate() {
		super.onCreate()
		dataStoreInstance = DataStoreInstance(applicationContext)
		moveToForeground()
	}

	override fun onStartCommand(intent : Intent?, flags : Int, startId : Int) : Int {
		super.onStartCommand(intent, flags, startId)
		return START_STICKY
	}

	override fun onUnbind(intent : Intent?) : Boolean {
		Toast.makeText(this, "service unbind", Toast.LENGTH_SHORT).show()
		return super.onUnbind(intent)
	}

	override fun onDestroy() {
		super.onDestroy()
		Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show()
	}

	//	Don't make these private or lateinit
	val repository : Repository by inject()

	val syncStatus : MutableStateFlow<SyncStatus> = MutableStateFlow(SyncStatus.Init)
	lateinit var dataStoreInstance : DataStoreInstance

	companion object {
		data class SyncObjectStatus(
			val toUpSyncCount : Int,
			val toDownSyncCount : Int,
			val isSynced : Boolean,
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
			data class Idle(val syncedTimestamp : Long, val isAutoSyncDisabled : Boolean) : SyncStatus()

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
				val sessionId : String = RealmUUID.random().toString(),
				val chapterSyncObjectStatus : SyncObjectStatus? = null,
				val noteSyncObjectStatus : SyncObjectStatus? = null,
				val bucketSyncObjectStatus : SyncObjectStatus? = null,
				val bucketItemSyncObjectStatus : SyncObjectStatus? = null,
				val tagSyncObjectStatus : SyncObjectStatus? = null,
				val attachmentSyncObjectStatus : SyncObjectStatus? = null,
			) : SyncStatus()

			/**
			 * AutoSyncDisabled: Auto sync disabled. Can be triggered to sync but won't resync.
			 */
			object AutoSyncDisabled : SyncStatus()

			/**
			 * Failed: Failed for sync. Can be triggered to sync again.
			 */
			data class Failed(val reason : String) : SyncStatus()

			/**
			 * CredentialError: Failed for sync due to credential error. Connect with Dropbox again.
			 */
			object CredentialError : SyncStatus()
		}
	}
}
