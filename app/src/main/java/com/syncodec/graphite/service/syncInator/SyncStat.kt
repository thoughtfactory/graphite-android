package com.syncodec.graphite.service.syncInator

import io.realm.kotlin.types.RealmUUID


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
	data class Idle(val isAutoSyncEnabled : Boolean) : SyncStat()

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
	val isSynced: Boolean = false,
)
