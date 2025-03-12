package com.syncodec.graphite.utils.alice

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.io.UnsupportedEncodingException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.security.GeneralSecurityException
import java.security.InvalidKeyException
import java.security.KeyStore
import java.security.KeyStoreException
import java.security.SecureRandom
import java.security.spec.KeySpec
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import androidx.core.content.edit


enum class AliceRequestResult {
	SUCCESS,
	KEYSTORE_NOT_INITIALIZED,
	KEY_NOT_FOUND,
	UNKNOWN_ERROR
}

data class AliceRequest(
	val result : AliceRequestResult,
	val data : ByteArray? = null,
	val error : Exception? = null
)

fun Context.putSecretData(key : String, value : ByteArray) {
	val keyStore = KeyStore.getInstance("AndroidKeyStore")
	keyStore.load(null)
	var secretKey = keyStore.getKey("grey_alice", null) as SecretKey?
	if (secretKey == null) generateSecretKey()
	secretKey = keyStore.getKey("grey_alice", null) as SecretKey?

	if (secretKey != null) {
		val cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/" + KeyProperties.BLOCK_MODE_CBC + "/" + KeyProperties.ENCRYPTION_PADDING_PKCS7)

		cipher.init(Cipher.ENCRYPT_MODE, secretKey)
		val encryptedKeyForRealm : ByteArray = cipher.doFinal(value)
		val initializationVector : ByteArray = cipher.iv
		val initializationVectorAndEncryptedKey = ByteArray(Integer.BYTES + initializationVector.size + encryptedKeyForRealm.size)
		val buffer = ByteBuffer.wrap(initializationVectorAndEncryptedKey)
		buffer.order(ByteOrder.BIG_ENDIAN)
		buffer.putInt(initializationVector.size)
		buffer.put(initializationVector)
		buffer.put(encryptedKeyForRealm)
		getSharedPreferences("alice", Context.MODE_PRIVATE).edit {
            putString("${key}_iv_and_encrypted_key", Base64.encodeToString(initializationVectorAndEncryptedKey, Base64.NO_WRAP))
        }
	}
}

fun Context.putSecretData(key : String, value : String) = putSecretData(key, value.encodeToByteArray())

fun Context.getSecretData(key : String) : AliceRequest {
	val keyStore : KeyStore = KeyStore.getInstance("AndroidKeyStore")
	keyStore.load(null)
	val hasData = getSharedPreferences("alice", Context.MODE_PRIVATE).contains("${key}_iv_and_encrypted_key")
	if (hasData) {
		val initializationVectorAndEncryptedKey = Base64.decode(
			getSharedPreferences("alice", Context.MODE_PRIVATE)
				?.getString("${key}_iv_and_encrypted_key", null), Base64.DEFAULT
		)
		val buffer = ByteBuffer.wrap(initializationVectorAndEncryptedKey)
		buffer.order(ByteOrder.BIG_ENDIAN)
		val initializationVectorLength = buffer.int
		val initializationVector = ByteArray(initializationVectorLength)
		buffer[initializationVector]
		val encryptedKey = ByteArray(initializationVectorAndEncryptedKey.size - Integer.BYTES - initializationVectorLength)
		buffer[encryptedKey]
		val cipher : Cipher = try {
			Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/" + KeyProperties.BLOCK_MODE_CBC + "/" + KeyProperties.ENCRYPTION_PADDING_PKCS7)
		} catch (e : Exception) {
			return AliceRequest(AliceRequestResult.UNKNOWN_ERROR, null, e)
		}
		val decryptedKey : ByteArray = try {
			val secretKey = keyStore.getKey("grey_alice", null) as SecretKey
			val initializationVectorSpec = IvParameterSpec(initializationVector)
			cipher.init(Cipher.DECRYPT_MODE, secretKey, initializationVectorSpec)
			cipher.doFinal(encryptedKey)
		} catch (e : InvalidKeyException) {
			return AliceRequest(AliceRequestResult.UNKNOWN_ERROR, null, e)
		} catch (e : Exception) {
			return AliceRequest(AliceRequestResult.UNKNOWN_ERROR, null, e)
		}
		return AliceRequest(AliceRequestResult.SUCCESS, decryptedKey)
	} else {
		return AliceRequest(result = AliceRequestResult.KEY_NOT_FOUND)
	}
}

fun Context.deleteSecretData(key : String) {
	getSharedPreferences("alice", Context.MODE_PRIVATE).edit { remove("${key}_iv_and_encrypted_key") }
}

fun generateSecretKey() {
	val keyStore = KeyStore.getInstance("AndroidKeyStore")
	keyStore.load(null)

	val cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/" + KeyProperties.BLOCK_MODE_CBC + "/" + KeyProperties.ENCRYPTION_PADDING_PKCS7)
	val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")

	val keySpec = KeyGenParameterSpec.Builder("grey_alice", KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
		.setBlockModes(KeyProperties.BLOCK_MODE_CBC)
		.setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
//		.setUserAuthenticationRequired(true)
		.build()

	keyGenerator.init(keySpec)

	keyGenerator.generateKey()
}


fun Context.deleteKey(keyAlias : String) {
	val keyStore : KeyStore
	try {
		keyStore = KeyStore.getInstance("AndroidKeyStore")
		keyStore.load(null)
	} catch (e : Exception) {
		throw e
	}
	try {
		keyStore.deleteEntry(keyAlias)
	} catch (e : KeyStoreException) {
		throw e
	}
	getSharedPreferences("key", Context.MODE_PRIVATE).edit()
		.remove("${keyAlias}_iv_and_encrypted_key")
		.apply()
}

fun Context.hasKeyAlias(keyAlias : String) : Boolean {
	val keyStore : KeyStore
	try {
		keyStore = KeyStore.getInstance("AndroidKeyStore")
		keyStore.load(null)
	} catch (e : Exception) {
		throw e
	}
	return keyStore.containsAlias(keyAlias)
}

fun Context.hasKey(keyAlias : String) : Boolean {
	return getSharedPreferences("key", Context.MODE_PRIVATE)?.getString("${keyAlias}_iv_and_encrypted_key", null) != null
}

class Alice {
	companion object {
		private const val ITERATION_COUNT = 35423
		private const val KEY_LENGTH = 256
		private const val PBKDF2_DERIVATION_ALGORITHM = "PBKDF2WithHmacSHA1"
		private const val CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding"
		private const val PKCS5_SALT_LENGTH = 32
		private const val DELIMITER = "]"

		private val random: SecureRandom = SecureRandom()

		fun encrypt(plaintext: String, password: String): String? {
			val salt = generateSalt()
			val key: SecretKey = deriveKey(password, salt)
			return try {
				val cipher: Cipher = Cipher.getInstance(CIPHER_ALGORITHM)
				val iv = generateIv(cipher.blockSize)
				val ivParams = IvParameterSpec(iv)
				cipher.init(Cipher.ENCRYPT_MODE, key, ivParams)
				val cipherText: ByteArray = cipher.doFinal(plaintext.toByteArray(charset("UTF-8")))
				String.format("%s%s%s%s%s", toBase64(salt), DELIMITER, toBase64(iv), DELIMITER, toBase64(cipherText))
			} catch (e: GeneralSecurityException) {
				throw RuntimeException(e)
			} catch (e: UnsupportedEncodingException) {
				throw RuntimeException(e)
			}
		}

		fun encrypt(byteArray : ByteArray, password: String): String? {
			val salt = generateSalt()
			val key: SecretKey = deriveKey(password, salt)
			return try {
				val cipher: Cipher = Cipher.getInstance(CIPHER_ALGORITHM)
				val iv = generateIv(cipher.blockSize)
				val ivParams = IvParameterSpec(iv)
				cipher.init(Cipher.ENCRYPT_MODE, key, ivParams)
				val cipherText: ByteArray = cipher.doFinal(byteArray)
				String.format("%s%s%s%s%s", toBase64(salt), DELIMITER, toBase64(iv), DELIMITER, toBase64(cipherText))
			} catch (e: GeneralSecurityException) {
				throw RuntimeException(e)
			} catch (e: UnsupportedEncodingException) {
				throw RuntimeException(e)
			}
		}

		fun decrypt(ciphertext: String, password: String): String? {
			val fields = ciphertext.split(DELIMITER).toTypedArray()
			require(fields.size == 3) { "Invalid encrypted text format" }
			val salt = fromBase64(fields[0])
			val iv = fromBase64(fields[1])
			val cipherBytes = fromBase64(fields[2])
			val key: SecretKey = deriveKey(password, salt)

			return try {
				val cipher: Cipher = Cipher.getInstance(CIPHER_ALGORITHM)
				val ivParams = IvParameterSpec(iv)
				cipher.init(Cipher.DECRYPT_MODE, key, ivParams)
				val plaintext: ByteArray = cipher.doFinal(cipherBytes)
				String(plaintext, Charsets.UTF_8)
			} catch (e: GeneralSecurityException) {
				throw RuntimeException(e)
			} catch (e: UnsupportedEncodingException) {
				throw RuntimeException(e)
			}
		}

		private fun generateSalt(): ByteArray {
			val b = ByteArray(PKCS5_SALT_LENGTH)
			random.nextBytes(b)
			return b
		}

		private fun generateIv(length: Int): ByteArray {
			val b = ByteArray(length)
			random.nextBytes(b)
			return b
		}

		private fun deriveKey(password: String, salt: ByteArray?): SecretKey {
			return try {
				val keySpec: KeySpec = PBEKeySpec(password.toCharArray(), salt, ITERATION_COUNT, KEY_LENGTH)
				val keyFactory: SecretKeyFactory = SecretKeyFactory.getInstance(PBKDF2_DERIVATION_ALGORITHM)
				val keyBytes: ByteArray = keyFactory.generateSecret(keySpec).encoded
				SecretKeySpec(keyBytes, "AES")
			} catch (e: GeneralSecurityException) {
				throw RuntimeException(e)
			}
		}

		private fun toBase64(bytes: ByteArray): String? {
			return java.util.Base64.getEncoder().withoutPadding().encodeToString(bytes)
		}

		private fun fromBase64(base64: String): ByteArray {
			return java.util.Base64.getDecoder().decode(base64)
		}
	}
}
