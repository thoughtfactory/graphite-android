package com.syncodec.graphite.miscellaneous

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class DataStore(private val context: Context) {

	companion object {
		private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("dataStore")
		private val IS_FIRST_TIME = booleanPreferencesKey("isFirstTime")
		private val PREFERENCE_THEME = intPreferencesKey("theme")
		private val PREFERENCE_BACKGROUND = intPreferencesKey("background")
		private val PREFERENCE_TYPOGRAPHY = intPreferencesKey("typography")
		private val PREFERENCE_VAULT_KEY = stringPreferencesKey("vault_key")
		private val PREFERENCE_ACTIVE_COMPONENT = intPreferencesKey("component")
		private val PREFERENCE_DEFAULT_NOTE_KEY = stringPreferencesKey("default_notebook_key")
		private val PREFERENCE_NOTE_SHOW_LOCATION_PERMISSION =
			booleanPreferencesKey("show_location_permission_card")
		private val PREFERENCE_EXPIRY_TIME = longPreferencesKey("expiry_time")
	}

	val getIsFirstTime: Flow<Boolean> =
		context.dataStore.data.map { preferences -> preferences[IS_FIRST_TIME] ?: true }

	fun putIsFirstTime(isFirstTime: Boolean) = CoroutineScope(Dispatchers.IO).launch {
		context.dataStore.edit { pref ->
			pref[IS_FIRST_TIME] = isFirstTime
		}
	}

	val getTheme: Flow<Int> =
		context.dataStore.data.map { preferences -> preferences[PREFERENCE_THEME] ?: 0 }

	fun putTheme(theme: Int) = CoroutineScope(Dispatchers.IO).launch {
		context.dataStore.edit { pref ->
			pref[PREFERENCE_THEME] = theme
		}
	}

	val getBackground: Flow<Int> =
		context.dataStore.data.map { preferences -> preferences[PREFERENCE_BACKGROUND] ?: 0 }

	fun putBackground(background: Int) = CoroutineScope(Dispatchers.IO).launch {
		context.dataStore.edit { pref ->
			pref[PREFERENCE_BACKGROUND] = background
		}
	}

	val getTypography: Flow<Int> =
		context.dataStore.data.map { preferences -> preferences[PREFERENCE_TYPOGRAPHY] ?: 0 }

	fun putTypography(typography: Int) = CoroutineScope(Dispatchers.IO).launch {
		context.dataStore.edit { pref ->
			pref[PREFERENCE_TYPOGRAPHY] = typography
		}
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

						val pbKeySpec = PBEKeySpec("password".toCharArray(), salt, 1324, 256)
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

		val pbKeySpec = PBEKeySpec("7U%%!p28p94o!2B1@4Vqk*3VX!&g0fgP".toCharArray(), salt, 1324, 256)
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

	val getDefaultNotebookKey: Flow<String?> =
		context.dataStore.data.map { preferences -> preferences[PREFERENCE_DEFAULT_NOTE_KEY] }

	fun putDefaultNotebookKey(notebookKey: String) =
		CoroutineScope(Dispatchers.IO).launch {
			context.dataStore.edit { pref ->
				pref[PREFERENCE_DEFAULT_NOTE_KEY] = notebookKey
			}
		}

	val getExpiryTime =
		context.dataStore.data.map { preferences -> preferences[PREFERENCE_EXPIRY_TIME] }
	fun putExpiryTime(expiryTime: Long) =
		CoroutineScope(Dispatchers.IO).launch {
			context.dataStore.edit { pref -> pref[PREFERENCE_EXPIRY_TIME] = expiryTime }
		}
}
