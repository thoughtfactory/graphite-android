package com.syncodec.graphite.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.syncodec.graphite.BuildConfig
import com.syncodec.graphite.utils.alice.Alice
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch


val Context.dataStore : DataStore<Preferences> by preferencesDataStore("dataStore")

class DataStoreInstance(private val context : Context) {
	companion object {
		private val IS_FIRST_TIME = booleanPreferencesKey("is_first_time")
		private val STORED_VERSION = intPreferencesKey("stored_version")
		private val PREFERENCE_SUPER_EXPIRY_TIME = stringPreferencesKey("super_expiry_time")
		private val PREFERENCE_FOLLOW_SYSTEM_DARK_THEME = booleanPreferencesKey("follow_system_dark_theme")
		private val PREFERENCE_FORCE_DARK_THEME = booleanPreferencesKey("force_dark_theme")
		private val PREFERENCE_TINT_FAVORITE = booleanPreferencesKey("tint_favorite")
		private val PREFERENCE_GEOLOCATION = booleanPreferencesKey("geolocation")
		private val PREFERENCE_YEAR_PROGRESS = booleanPreferencesKey("year_progress")
		private val PREFERENCE_NOTE_FROM_NOTIFICATION = booleanPreferencesKey("note_from_notification")
		private val PREFERENCE_SORT_ON = intPreferencesKey("sort_on")
		private val PREFERENCE_SORT_BY = intPreferencesKey("sort_by")
		private val PREFERENCE_VIEW_TYPE = intPreferencesKey("view_type")
		private val PREFERENCE_USE_BIOMETRIC = booleanPreferencesKey("use_biometric")
		private val PREFERENCE_IS_SYNC_ENABLED = booleanPreferencesKey("is_sync_enabled")
		private val PREFERENCE_DROPBOX_REFRESH_TOKEN = stringPreferencesKey("dropbox_refresh_token")

		private val PREFERENCE_SHOW_WHATS_NEW_CARD = intPreferencesKey("show_whats_new_card")
	}

	val getIsFirstTime : Flow<Boolean> =
		context.dataStore.data.map { preferences -> preferences[IS_FIRST_TIME] ?: true }

	fun putIsFirstTime(isFirstTime : Boolean) = CoroutineScope(Dispatchers.IO).launch {
		context.dataStore.edit { pref -> pref[IS_FIRST_TIME] = isFirstTime }
	}

	val storedVersion : Flow<Int> = context.dataStore.data.map { preferences -> preferences[STORED_VERSION] ?: 0 }

	val getSuperExpiryTime = context.dataStore.data.map { preferences ->
		try {
			preferences[PREFERENCE_SUPER_EXPIRY_TIME]?.let { Alice.decrypt(it, "V0&776*t^nr@!C&18mTFJnHO@9Y0yGM7") ?: "" } ?: ""
		} catch (exception : Exception) {
			exception.printStackTrace()
			""
		}
	}

	fun putSuperExpiryTime(expiryTime : Long) =
		CoroutineScope(Dispatchers.IO).launch {
			try {
				Alice.encrypt(expiryTime.toString(), "V0&776*t^nr@!C&18mTFJnHO@9Y0yGM7")?.apply {
					context.dataStore.edit { pref -> pref[PREFERENCE_SUPER_EXPIRY_TIME] = this }
				}
			} catch (exception : Exception) {
				exception.printStackTrace()
			}
		}


	val getFollowSystemDarkTheme : Flow<Boolean> = context.dataStore.data.map { preferences -> preferences[PREFERENCE_FOLLOW_SYSTEM_DARK_THEME] ?: true }

	fun putFollowSystemDarkTheme(followSystemDarkTheme : Boolean) = CoroutineScope(Dispatchers.IO).launch {
		context.dataStore.edit { pref -> pref[PREFERENCE_FOLLOW_SYSTEM_DARK_THEME] = followSystemDarkTheme }
	}

	val getForceDarkTheme : Flow<Boolean> = context.dataStore.data.map { preferences -> preferences[PREFERENCE_FORCE_DARK_THEME] ?: false }

	fun putForceDarkTheme(forceDarkTheme : Boolean) = CoroutineScope(Dispatchers.IO).launch {
		context.dataStore.edit { pref -> pref[PREFERENCE_FORCE_DARK_THEME] = forceDarkTheme }
	}

	val getTintFavorite : Flow<Boolean> = context.dataStore.data.map { preferences -> preferences[PREFERENCE_TINT_FAVORITE] ?: true }

	fun putTintFavorite(tintFavorite : Boolean) = CoroutineScope(Dispatchers.IO).launch {
		context.dataStore.edit { pref -> pref[PREFERENCE_TINT_FAVORITE] = tintFavorite }
	}

	val getGeolocation : Flow<Boolean> = context.dataStore.data.map { preferences -> preferences[PREFERENCE_GEOLOCATION] ?: true }

	fun putGeolocation(geolocation : Boolean) = CoroutineScope(Dispatchers.IO).launch {
		context.dataStore.edit { pref -> pref[PREFERENCE_GEOLOCATION] = geolocation }
	}

	val getYearProgress : Flow<Boolean> = context.dataStore.data.map { preferences -> preferences[PREFERENCE_YEAR_PROGRESS] ?: true }

	fun putYearProgress(yearProgress : Boolean) = CoroutineScope(Dispatchers.IO).launch {
		context.dataStore.edit { pref -> pref[PREFERENCE_YEAR_PROGRESS] = yearProgress }
	}

	val getNoteFromNotification : Flow<Boolean> = context.dataStore.data.map { preferences -> preferences[PREFERENCE_NOTE_FROM_NOTIFICATION] ?: false }

	fun putNoteFromNotification(noteFromNotification : Boolean) = CoroutineScope(Dispatchers.IO).launch {
		context.dataStore.edit { pref -> pref[PREFERENCE_NOTE_FROM_NOTIFICATION] = noteFromNotification }
	}

	val getSortOn : Flow<SortOn> = context.dataStore.data.map { preferences ->
		when (preferences[PREFERENCE_SORT_ON] ?: 1) {
			0 -> SortOn.TITLE
			1 -> SortOn.TIMESTAMP
			2 -> SortOn.MODIFIED
//			3 -> SortOn.PRIORITY
//			4 -> SortOn.COMPLETED
//			5 -> SortOn.DUE
//			6 -> SortOn.CREATED
//			7 -> SortOn.DONE
			8 -> SortOn.CUSTOM
			else -> SortOn.TIMESTAMP
		}
	}

	fun putSortOn(sortOn : SortOn) = CoroutineScope(Dispatchers.IO).launch {
		context.dataStore.edit { pref -> pref[PREFERENCE_SORT_ON] = sortOn.ordinal }
	}

	val getSortBy : Flow<SortBy> = context.dataStore.data.map { preferences ->
		when (preferences[PREFERENCE_SORT_BY] ?: 1) {
			0 -> SortBy.ASCENDING
			1 -> SortBy.DESCENDING
			else -> SortBy.DESCENDING
		}
	}

	fun putSortBy(sortBy : SortBy) = CoroutineScope(Dispatchers.IO).launch {
		context.dataStore.edit { pref -> pref[PREFERENCE_SORT_BY] = sortBy.ordinal }
	}

	val getViewType : Flow<ViewType> = context.dataStore.data.map { preferences ->
		when (preferences[PREFERENCE_VIEW_TYPE] ?: 0) {
			0 -> ViewType.LIST
			1 -> ViewType.GRID
			else -> ViewType.LIST
		}
	}

	fun putViewType(viewType : ViewType) = CoroutineScope(Dispatchers.IO).launch {
		context.dataStore.edit { pref -> pref[PREFERENCE_VIEW_TYPE] = viewType.ordinal }
	}

	fun getUseBiometric() : Flow<Boolean> = context.dataStore.data.map { preferences -> preferences[PREFERENCE_USE_BIOMETRIC] ?: false }

	fun putUseBiometric(useBiometric : Boolean) = CoroutineScope(Dispatchers.IO).launch {
		context.dataStore.edit { pref -> pref[PREFERENCE_USE_BIOMETRIC] = useBiometric }
	}

	fun getIsSyncEnabled() : Flow<Boolean> = context.dataStore.data.map { preferences -> preferences[PREFERENCE_IS_SYNC_ENABLED] ?: true }

	fun setIsSyncEnabled(isSyncEnabled : Boolean) = CoroutineScope(Dispatchers.IO).launch {
		context.dataStore.edit { pref -> pref[PREFERENCE_IS_SYNC_ENABLED] = isSyncEnabled }
	}

	fun getDropboxRefreshToken() : Flow<String> = context.dataStore.data.map { preferences -> preferences[PREFERENCE_DROPBOX_REFRESH_TOKEN] ?: "" }

	fun putDropboxRefreshToken(refreshToken : String) = CoroutineScope(Dispatchers.IO).launch {
		context.dataStore.edit { pref -> pref[PREFERENCE_DROPBOX_REFRESH_TOKEN] = refreshToken }
	}

	val getShowWhatsNewCard : Flow<Boolean> = context.dataStore.data.map { preferences -> (preferences[PREFERENCE_SHOW_WHATS_NEW_CARD] ?: 0) != BuildConfig.VERSION_CODE }

	fun putShowWhatsNewCard(showWhatsNewCard : Boolean) = CoroutineScope(Dispatchers.IO).launch {
		context.dataStore.edit { pref -> pref[PREFERENCE_SHOW_WHATS_NEW_CARD] = BuildConfig.VERSION_CODE }
	}

	fun clearDatastore() = CoroutineScope(Dispatchers.IO).launch {
		context.dataStore.edit { pref -> pref.clear() }
	}
}
