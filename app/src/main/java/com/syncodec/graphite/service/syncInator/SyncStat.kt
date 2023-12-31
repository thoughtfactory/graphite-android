package com.syncodec.graphite.service.syncInator

import io.realm.kotlin.types.RealmUUID
import kotlinx.datetime.Instant


/**
 * [Init]: Initial state
 *
 * [Connecting]: When  trying to connect with dropbox
 *
 * [NotConnected]: When not connected to dropbox. Not authenticated
 *
 * [Idle]: When there is no sync going on but connected to dropbox
 *
 * [Locked]: When the sync is locked by another process. Try again after 30 seconds
 *
 * [Syncing]: When the sync is going on
 *
 * [Error.NetworkError]: When there is a network error
 *
 * [Error.CredentialsError]: When there is a credentials error
 *
 * [Error.UnknownError]: When there is an unknown error
 */
sealed class SyncStat {
	/**
	 * [Init]: Initial state
	 */
	data object Init : SyncStat()

	/**
	 * [Connecting]: When  trying to connect with dropbox
	 */
	data object Connecting : SyncStat()

	/**
	 * [NotConnected]: When not connected to dropbox
	 */
	data object NotConnected : SyncStat()

	/**
	 * [Idle]: When there is no sync going on but connected to dropbox
	 * @param isAutoSyncEnabled : Whether auto sync is enabled or not. If enabled, sync will start automatically after 30 seconds
	 */
	data class Idle(val isAutoSyncEnabled : Boolean, val lastSyncedAt : Instant?) : SyncStat()

	/**
	 * [Locked]: When the sync is locked by another process. Try again after 30 seconds
	 */
	data object Locked : SyncStat()

	/**
	 * [Syncing]: When the sync is going on
	 */
	data class Syncing(
		val sessionId: String = RealmUUID.random().toString(),
		val chapterSyncObjectStatus: ObjectStatus = ObjectStatus(),
		val noteSyncObjectStatus: ObjectStatus = ObjectStatus(),
		val bucketSyncObjectStatus: ObjectStatus = ObjectStatus(),
		val bucketItemSyncObjectStatus: ObjectStatus = ObjectStatus(),
		val tagSyncObjectStatus: ObjectStatus = ObjectStatus(),
		val attachmentSyncObjectStatus: ObjectStatus = ObjectStatus(),
	) : SyncStat()


	/**
	 * [Error]: When there is an error while syncing
	 * @param message : Error message
	 */
	sealed class Error(open val message : String? = null): SyncStat() {
		data object CredentialsError : Error(message = "Credentials error")
		data object NetworkError : Error(message = "Network error")
		data class UnknownError(override val message: String? = null) : Error(message = message)
	}
}

data class ObjectStatus(
	val toUpSync: Int = 0,
	val toDownSync: Int = 0,
	val objectSyncStatus: ObjectSyncStatus = ObjectSyncStatus.Waiting,
)

enum class ObjectSyncStatus {
	Waiting,
	Syncing,
	Success,
	Error,
}
