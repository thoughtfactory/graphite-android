package com.syncodec.graphite.utils.alice

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.security.InvalidAlgorithmParameterException
import java.security.InvalidKeyException
import java.security.KeyStore
import java.security.KeyStoreException
import java.security.NoSuchAlgorithmException
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec


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

fun Context.putData(key : String, value : ByteArray) {
	val keyStore = KeyStore.getInstance("AndroidKeyStore")
	keyStore.load(null)
	var secretKey = keyStore.getKey("grey_alice", null) as SecretKey?
	if (secretKey == null) generateSecretKey()
	secretKey = keyStore.getKey("grey_alice", null) as SecretKey?

	if (secretKey!=null) {
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
		getSharedPreferences("alice", Context.MODE_PRIVATE).edit()
			.putString("${key}_iv_and_encrypted_key", Base64.encodeToString(initializationVectorAndEncryptedKey, Base64.NO_WRAP))
			.apply()
	}
}

fun Context.getData(key: String): AliceRequest {
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
		return AliceRequest(result = AliceRequestResult.KEY_NOT_FOUND,)
	}
}

fun Context.generateSecretKey() {
	val keyStore = KeyStore.getInstance("AndroidKeyStore")
	keyStore.load(null)

	val cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/" + KeyProperties.BLOCK_MODE_CBC + "/" + KeyProperties.ENCRYPTION_PADDING_PKCS7)
	val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")

	val keySpec = KeyGenParameterSpec.Builder("grey_alice", KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
		.setBlockModes(KeyProperties.BLOCK_MODE_CBC)
		.setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
		.setUserAuthenticationRequired(false)
//			.setUserAuthenticationValidityDurationSeconds(300)
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
