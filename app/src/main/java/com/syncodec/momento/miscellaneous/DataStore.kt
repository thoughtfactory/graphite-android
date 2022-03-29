package com.syncodec.momento.miscellaneous

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class DataStore(private val context: Context) {

	companion object {
		private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("dataStore")
		private val PREFERENCE_THEME = intPreferencesKey("theme")
		private val PREFERENCE_TYPOGRAPHY = intPreferencesKey("typography")
		private val PREFERENCE_VAULT_KEY = stringPreferencesKey("vault_key")
		private val PREFERENCE_ACTIVE_COMPONENT = intPreferencesKey("component")
		private val PREFERENCE_DEFAULT_NOTE_KEY = stringPreferencesKey("default_notebook_key")
		private val PREFERENCE_NOTE_SHOW_LOCATION_PERMISSION = booleanPreferencesKey("show_location_permission_card")
	}

	val getTheme: Flow<Int> = context.dataStore.data.map { preferences -> preferences[PREFERENCE_THEME] ?: 0 }
	fun putTheme(theme: Int) = CoroutineScope(Dispatchers.IO).launch { context.dataStore.edit { pref -> pref[PREFERENCE_THEME] = theme } }

	val getTypography: Flow<Int> = context.dataStore.data.map { preferences -> preferences[PREFERENCE_TYPOGRAPHY] ?: 0 }
	fun putTypography(typography: Int) = CoroutineScope(Dispatchers.IO).launch { context.dataStore.edit { pref -> pref[PREFERENCE_TYPOGRAPHY] = typography } }

	val getPasscode: Flow<String?> = context.dataStore.data.map { preferences -> preferences[PREFERENCE_VAULT_KEY] ?: "" }
	fun putPasscode(code: String) = CoroutineScope(Dispatchers.IO).launch { context.dataStore.edit { pref -> pref[PREFERENCE_VAULT_KEY] = code } }

	val getDefaultNotebookKey: Flow<String?> = context.dataStore.data.map { preferences -> preferences[PREFERENCE_DEFAULT_NOTE_KEY] }
	fun putDefaultNotebookKey(notebookKey: String) =
		CoroutineScope(Dispatchers.IO).launch { context.dataStore.edit { pref -> pref[PREFERENCE_DEFAULT_NOTE_KEY] = notebookKey } }
}
