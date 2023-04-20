package com.syncodec.graphite.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.widget.Toast
import androidx.lifecycle.LifecycleService
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.syncodec.graphite.R
import com.syncodec.graphite.di.repository.RealmUUIDDeserializer
import com.syncodec.graphite.di.repository.RealmUUIDKeyDeserializer
import com.syncodec.graphite.di.repository.RealmUUIDSerializer
import com.syncodec.graphite.di.repository.koinRepository.KoinRepository
import com.syncodec.graphite.utils.DataStoreInstance
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.android.ext.android.inject

abstract class SyncerService : LifecycleService() {
	val objectMapper = jsonMapper {
		addModule(
			kotlinModule()
				.addSerializer(
					RealmUUID::class.java,
					RealmUUIDSerializer()
				)
				.addDeserializer(
					RealmUUID::class.java,
					RealmUUIDDeserializer()
				)
				.addKeyDeserializer(
					RealmUUID::class.java,
					RealmUUIDKeyDeserializer()
				)
		)
	}.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

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
	val repository : KoinRepository by inject()

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
		*   *   Init: Initial state. Can be triggered to sync. Automatic sync will be triggered.
		*   *   Idle: Synced with Dropbox. Can be triggered to sync again.
		*   *   Locked: Locked for sync. Can be triggered to sync again.
		*   *   Connected: Connected to Dropbox. Cannot be triggered to sync.
		*   *   Syncing: Syncing with Dropbox. Cannot be triggered to sync.
		*   *   Paused: Paused for sync. Can be triggered to sync again.
		*   *   Failed: Failed for sync. Can be triggered to sync again.
		*   *   CredentialError: Failed for sync due to credential error. Connect with Dropbox again.
		 */
		sealed class SyncStatus() {
			/*
			* Init: Initial state. Can be triggered to sync. Automatic sync will be triggered.
			* */
			object Init : SyncStatus()

			/*
			* Idle: Synced with Dropbox. Can be triggered to sync again.
			* */
			data class Idle(val syncedTimestamp : Long) : SyncStatus()

			/*
			* Locked: Locked for sync. Can be triggered to sync again.
			* */
			object Locked : SyncStatus()

			/*
			* Connected: Connected to Dropbox. Cannot be triggered to sync.
			*  */
			object Connected : SyncStatus()

			/*
			* Syncing: Syncing with Dropbox. Cannot be triggered to sync.
			* */
			data class Syncing(
				val sessionId : String = RealmUUID.random().toString(),
				val chapterSyncObjectStatus : SyncObjectStatus? = null,
				val noteSyncObjectStatus : SyncObjectStatus? = null,
				val bucketSyncObjectStatus : SyncObjectStatus? = null,
				val bucketItemSyncObjectStatus : SyncObjectStatus? = null,
				val tagSyncObjectStatus : SyncObjectStatus? = null,
				val attachmentSyncObjectStatus : SyncObjectStatus? = null,
			) : SyncStatus()

			/*
			* Paused: Paused for sync. Can be triggered to sync again.
			* */
			object Paused : SyncStatus()

			/*
			* Failed: Failed for sync. Can be triggered to sync again.
			* */
			data class Failed(val reason : String) : SyncStatus()

			/*
			* CredentialError: Failed for sync due to credential error. Connect with Dropbox again.
			*  */
			object CredentialError : SyncStatus()
		}
	}
}
