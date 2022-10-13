package com.syncodec.graphite.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.nio.charset.StandardCharsets
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec


val Context.dataStore: DataStore<Preferences> by preferencesDataStore("dataStore")

class DataStoreInstance(private val context: Context) {
	companion object {
		private val IS_FIRST_TIME = booleanPreferencesKey("is_first_time")
		private val STORED_VERSION = intPreferencesKey("stored_version")
		private val PREFERENCE_SHOW_RELEASE_NOTES = booleanPreferencesKey("show_release_notes")
		private val PREFERENCE_THEME = intPreferencesKey("theme")
		private val PREFERENCE_BACKGROUND = intPreferencesKey("background")
		private val PREFERENCE_TYPOGRAPHY = intPreferencesKey("typography")
		private val PREFERENCE_FOLLOW_SYSTEM_DARK_THEME = booleanPreferencesKey("follow_system_dark_theme")
		private val PREFERENCE_FORCE_DARK_THEME = booleanPreferencesKey("force_dark_theme")
		private val PREFERENCE_TINT_FAVORITE = booleanPreferencesKey("tint_favorite")
		private val PREFERENCE_GEOLOCATION = booleanPreferencesKey("geolocation")
		private val PREFERENCE_YEAR_PROGRESS = booleanPreferencesKey("year_progress")
		private val PREFERENCE_LANGUAGE = stringPreferencesKey("language")
		private val PREFERENCE_VAULT_KEY = stringPreferencesKey("vault_key")
		private val PREFERENCE_USE_BIOMETRIC = booleanPreferencesKey("use_biometric")
		private val PREFERENCE_ACTIVE_COMPONENT = intPreferencesKey("component")
		private val PREFERENCE_DEFAULT_NOTE_KEY = stringPreferencesKey("default_notebook_key")
		private val PREFERENCE_NOTE_SHOW_LOCATION_PERMISSION =
			booleanPreferencesKey("show_location_permission_card")
		private val PREFERENCE_SUPER_EXPIRY_TIME = stringPreferencesKey("super_expiry_time")
		private val PREFERENCE_EXPIRY_TIME = stringPreferencesKey("expiry_time")
	}

	val getIsFirstTime: Flow<Boolean> =
		context.dataStore.data.map { preferences -> preferences[IS_FIRST_TIME] ?: true }

	fun putIsFirstTime(isFirstTime: Boolean) = CoroutineScope(Dispatchers.IO).launch {
		context.dataStore.edit { pref -> pref[IS_FIRST_TIME] = isFirstTime }
	}

	val storedVersion: Flow<Int> =
		context.dataStore.data.map { preferences -> preferences[STORED_VERSION] ?: 0 }

	fun storeVersion(version: Int) = CoroutineScope(Dispatchers.IO).launch {
		context.dataStore.edit { pref -> pref[STORED_VERSION] = version }
	}

	val showReleaseNotes: Flow<Boolean> =
		context.dataStore.data.map { preferences ->
			preferences[PREFERENCE_SHOW_RELEASE_NOTES] ?: true
		}

	fun setShowReleaseNotes(showReleaseNotes: Boolean) = CoroutineScope(Dispatchers.IO).launch {
		context.dataStore.edit { pref -> pref[PREFERENCE_SHOW_RELEASE_NOTES] = showReleaseNotes }
	}

	val getTheme: Flow<Int> =
		context.dataStore.data.map { preferences -> preferences[PREFERENCE_THEME] ?: 0 }

	fun putTheme(theme: Int) = CoroutineScope(Dispatchers.IO).launch {
		context.dataStore.edit { pref -> pref[PREFERENCE_THEME] = theme }
	}

	val getBackground: Flow<Int> =
		context.dataStore.data.map { preferences -> preferences[PREFERENCE_BACKGROUND] ?: 0 }

	fun putBackground(background: Int) = CoroutineScope(Dispatchers.IO).launch {
		context.dataStore.edit { pref -> pref[PREFERENCE_BACKGROUND] = background }
	}

	val getTypography: Flow<Int> =
		context.dataStore.data.map { preferences -> preferences[PREFERENCE_TYPOGRAPHY] ?: 0 }

	fun putTypography(typography: Int) = CoroutineScope(Dispatchers.IO).launch {
		context.dataStore.edit { pref -> pref[PREFERENCE_TYPOGRAPHY] = typography }
	}

	val getFollowSystemDarkTheme: Flow<Boolean> = context.dataStore.data.map { preferences -> preferences[PREFERENCE_FOLLOW_SYSTEM_DARK_THEME] ?: true }

	fun putFollowSystemDarkTheme(followSystemDarkTheme: Boolean) = CoroutineScope(Dispatchers.IO).launch {
		context.dataStore.edit { pref -> pref[PREFERENCE_FOLLOW_SYSTEM_DARK_THEME] = followSystemDarkTheme }
	}

	val getForceDarkTheme: Flow<Boolean> = context.dataStore.data.map { preferences -> preferences[PREFERENCE_FORCE_DARK_THEME] ?: false }

	fun putForceDarkTheme(forceDarkTheme: Boolean) = CoroutineScope(Dispatchers.IO).launch {
		context.dataStore.edit { pref -> pref[PREFERENCE_FORCE_DARK_THEME] = forceDarkTheme }
	}

	val getTintFavorite: Flow<Boolean> = context.dataStore.data.map { preferences -> preferences[PREFERENCE_TINT_FAVORITE] ?: true }

	fun putTintFavorite(tintFavorite: Boolean) = CoroutineScope(Dispatchers.IO).launch {
		context.dataStore.edit { pref -> pref[PREFERENCE_TINT_FAVORITE] = tintFavorite }
	}

	val getGeolocation: Flow<Boolean> = context.dataStore.data.map { preferences -> preferences[PREFERENCE_GEOLOCATION] ?: true }

	fun putGeolocation(geolocation: Boolean) = CoroutineScope(Dispatchers.IO).launch {
		context.dataStore.edit { pref -> pref[PREFERENCE_GEOLOCATION] = geolocation }
	}

	val getYearProgress: Flow<Boolean> = context.dataStore.data.map { preferences -> preferences[PREFERENCE_YEAR_PROGRESS] ?: true }

	fun putYearProgress(yearProgress: Boolean) = CoroutineScope(Dispatchers.IO).launch {
		context.dataStore.edit { pref -> pref[PREFERENCE_YEAR_PROGRESS] = yearProgress }
	}

	val getPasscode =
		context.dataStore.data.map { preferences -> preferences[PREFERENCE_VAULT_KEY] ?: "" }
			.map { data ->
				try {
					if (data.length > 256 + 16) {
						val salt = data.substring(0, 256).toByteArray(StandardCharsets.ISO_8859_1)
						val iv =
							data.substring(256, 256 + 16).toByteArray(StandardCharsets.ISO_8859_1)
						val cipherText =
							data.substring(256 + 16).toByteArray(StandardCharsets.ISO_8859_1)

						val pbKeySpec = PBEKeySpec(
							"7U%%!p28p94o!2B1@4Vqk*3VX!&g0fgP".toCharArray(),
							salt,
							1324,
							256
						)
						val secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
						val keyBytes = secretKeyFactory.generateSecret(pbKeySpec).encoded
						val keySpec = SecretKeySpec(keyBytes, "AES")

						val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
						val ivSpec = IvParameterSpec(iv)
						cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)
						val decrypted = cipher.doFinal(cipherText)

						decrypted.toString(StandardCharsets.ISO_8859_1)
					} else {
						""
					}
				} catch (exception: Exception) {
					""
				}
			}

	fun putPasscode(code: String) = CoroutineScope(Dispatchers.IO).launch {
		val random = SecureRandom()
		val salt = ByteArray(256)
		random.nextBytes(salt)

		val pbKeySpec =
			PBEKeySpec("7U%%!p28p94o!2B1@4Vqk*3VX!&g0fgP".toCharArray(), salt, 1324, 256)
		val secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
		val keyBytes = secretKeyFactory.generateSecret(pbKeySpec).encoded
		val keySpec = SecretKeySpec(keyBytes, "AES")

		val ivRandom = SecureRandom()
		val iv = ByteArray(16)
		ivRandom.nextBytes(iv)
		val ivSpec = IvParameterSpec(iv)

		val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
		cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec)
		val encrypted = cipher.doFinal(code.toByteArray())

		val data =
			salt.toString(StandardCharsets.ISO_8859_1) +
					iv.toString(StandardCharsets.ISO_8859_1) +
					encrypted.toString(StandardCharsets.ISO_8859_1)

		context.dataStore.edit { pref -> pref[PREFERENCE_VAULT_KEY] = data }
	}

	fun getUseBiometric(): Flow<Boolean> = context.dataStore.data.map { preferences -> preferences[PREFERENCE_USE_BIOMETRIC] ?: false }

	fun putUseBiometric(useBiometric: Boolean) = CoroutineScope(Dispatchers.IO).launch {
		context.dataStore.edit { pref -> pref[PREFERENCE_USE_BIOMETRIC] = useBiometric }
	}

	val getDefaultNotebookKey: Flow<String?> =
		context.dataStore.data.map { preferences -> preferences[PREFERENCE_DEFAULT_NOTE_KEY] }

	fun putDefaultNotebookKey(notebookKey: String) =
		CoroutineScope(Dispatchers.IO).launch {
			context.dataStore.edit { pref -> pref[PREFERENCE_DEFAULT_NOTE_KEY] = notebookKey }
		}

	val getSuperExpiryTime =
		context.dataStore.data.map { preferences ->
			try {
				preferences[PREFERENCE_SUPER_EXPIRY_TIME]?.decrypt("V0&776*t^nr@!C&18mTFJnHO@9Y0yGM7")
			} catch (exception: Exception) {
				exception.printStackTrace()
				null
			}
		}

	fun putSuperExpiryTime(expiryTime: Long) =
		CoroutineScope(Dispatchers.IO).launch {
			try {
				expiryTime.toString().encrypt("V0&776*t^nr@!C&18mTFJnHO@9Y0yGM7")?.apply {
					context.dataStore.edit { pref -> pref[PREFERENCE_SUPER_EXPIRY_TIME] = this }
				}
			} catch (exception: Exception) {
				exception.printStackTrace()
			}
		}

	val getExpiryTime =
		context.dataStore.data.map { preferences ->
			try {
				preferences[PREFERENCE_EXPIRY_TIME]?.decrypt("V0&776*t^nr@!C&18mTFJnHO@9Y0yGM7")
			} catch (exception: Exception) {
				exception.printStackTrace()
				null
			}
		}

	fun putExpiryTime(expiryTime: Long) =
		CoroutineScope(Dispatchers.IO).launch {
			try {
				expiryTime.toString().encrypt("V0&776*t^nr@!C&18mTFJnHO@9Y0yGM7")?.apply {
					context.dataStore.edit { pref -> pref[PREFERENCE_EXPIRY_TIME] = this }
				}
			} catch (exception: Exception) {
				exception.printStackTrace()
			}
		}

	fun clearDatastore() = CoroutineScope(Dispatchers.IO).launch {
		context.dataStore.edit { pref -> pref.clear() }
	}
}
