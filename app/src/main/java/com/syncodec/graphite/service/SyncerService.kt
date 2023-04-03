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
		sealed class SyncStatus() {
			object Init : SyncStatus()
			data class Idle(val syncedTimestamp : Long) : SyncStatus()
			object Loading : SyncStatus()
			object Locked : SyncStatus()
			object Connected : SyncStatus()
			object Disconnected : SyncStatus()
			data class Syncing(val sessionId : String = RealmUUID.random().toString()) : SyncStatus()
			object Paused : SyncStatus()
			data class Failed(val reason : String) : SyncStatus()
		}

		sealed class DriveState() {
			object Available : DriveState()
			object Locked : DriveState()
			object LockExpired : DriveState()
			data class Unknown(val exception : Exception) : DriveState()
		}

		sealed class UnlockResult() {
			object Success : UnlockResult()
			object Failed : UnlockResult()
			object LockExpired : UnlockResult()
		}

		data class ObjectMetadata(
			val modifiedTimestamp : Long,
			val hash : String,
			val isDeleted : Boolean
		)

		data class AttachmentMetadata(
			val parentId : RealmUUID,
			val name : String,
			val isDeleted : Boolean,
		)

		sealed class Operation {
			object Create : Operation()
			object Update : Operation()
			class Delete(val objectType : String? = null) : Operation()
		}

		sealed class DownloadResult {
			data class Success(val byteArray : ByteArray) : DownloadResult() {
				override fun equals(other : Any?) : Boolean {
					if (this === other) return true
					if (other !is Success) return false

					if (! byteArray.contentEquals(other.byteArray)) return false

					return true
				}

				override fun hashCode() : Int {
					return byteArray.contentHashCode()
				}
			}

			object UnknownError : DownloadResult()
			object NetworkError : DownloadResult()
		}

		sealed class UpSyncResult {
			object Success : UpSyncResult()
			object UnknownError : UpSyncResult()
			object NetworkError : UpSyncResult()
		}

		sealed class RectifyResult {
			object Success : RectifyResult()
			object UnknownError : RectifyResult()
			object NetworkError : RectifyResult()
		}
	}
}
