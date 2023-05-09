package com.syncodec.graphite.utils.dataStore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch


class SyncDataStoreInstance(private val context : Context) {
	companion object {
		private val PREFERENCE_IS_AUTO_SYNC_ENABLED = booleanPreferencesKey("is_auto_sync_enabled")
		private val PREFERENCE_SYNC_PROVIDER = stringPreferencesKey("sync_provider")
		private val PREFERENCE_LAST_SYNC_TIME = longPreferencesKey("last_sync_time")

		enum class SyncProvider {
			Dropbox,
			GoogleDrive,
			Unknown,
			NotConfigured,
		}
	}

	val isAutoSyncEnabledFlow : Flow<Boolean>
		get() = context.dataStore.data.map { preferences -> preferences[PREFERENCE_IS_AUTO_SYNC_ENABLED] ?: false }

	fun setIsAutoSyncEnabled(isAutoSyncEnabled : Boolean) {
		CoroutineScope(Dispatchers.IO).launch {
			context.dataStore.edit { pref -> pref[Companion.PREFERENCE_IS_AUTO_SYNC_ENABLED] = isAutoSyncEnabled }
		}
	}

	val syncProvider : Flow<SyncProvider>
		get() = context.dataStore.data.map { preferences ->
			val provider = preferences[PREFERENCE_SYNC_PROVIDER] ?: SyncProvider.NotConfigured.name
			SyncProvider.values().find { it.name == provider } ?: SyncProvider.Unknown
		}

	fun setSyncProvider(syncProvider : SyncProvider) {
		CoroutineScope(Dispatchers.IO).launch {
			context.dataStore.edit { pref -> pref[PREFERENCE_SYNC_PROVIDER] = syncProvider.name }
		}
	}

	val lastSyncTimeFlow : Flow<Long?>
		get() = context.dataStore.data.map { preferences -> preferences[PREFERENCE_LAST_SYNC_TIME]}

	fun setLastSyncTime(lastSyncTime : Long?) {
		CoroutineScope(Dispatchers.IO).launch {
			if (lastSyncTime == null) context.dataStore.edit { pref -> pref.remove(PREFERENCE_LAST_SYNC_TIME) }
			else context.dataStore.edit { pref -> pref[PREFERENCE_LAST_SYNC_TIME] = lastSyncTime }
		}
	}
}
