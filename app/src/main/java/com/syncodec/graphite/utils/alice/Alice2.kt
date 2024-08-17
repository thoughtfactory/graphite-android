package com.syncodec.graphite.utils.alice

import android.content.Context
import android.security.keystore.KeyProperties
import android.util.Base64
import com.syncodec.graphite.di.repository.repository.Repository
import com.syncodec.graphite.utils.alice.AliceRequest2
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.security.InvalidKeyException
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

sealed class AliceRequest2 {
    object KeyStoreNotInitialized : AliceRequest2()
    object KeyNotFound : AliceRequest2()
    data class Error(val exception: Exception? = null) : AliceRequest2()
    data class Success(val data: ByteArray) : AliceRequest2()
}

fun Context.putSecretData2(
    key: String,
    value: ByteArray
) {
    val keyStore = KeyStore.getInstance("AndroidKeyStore")
    keyStore.load(null)

    var secretKey = keyStore.getKey("grey_alice", null) as SecretKey?
    if (secretKey == null) generateSecretKey()
    secretKey = keyStore.getKey("grey_alice", null) as SecretKey?

    if (secretKey != null) {
        val cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/" + KeyProperties.BLOCK_MODE_CBC + "/" + KeyProperties.ENCRYPTION_PADDING_PKCS7)

        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val encryptedKeyForRealm: ByteArray = cipher.doFinal(value)
        val initializationVector: ByteArray = cipher.iv
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

fun Context.putSecretData2(key: String, value: String) = putSecretData(key, value.encodeToByteArray())

fun Context.getSecretData2(key: String): AliceRequest2 {
    val keyStore: KeyStore = KeyStore.getInstance("AndroidKeyStore")
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
        val cipher: Cipher = try {
            Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/" + KeyProperties.BLOCK_MODE_CBC + "/" + KeyProperties.ENCRYPTION_PADDING_PKCS7)
        } catch (e: Exception) {
            return AliceRequest2.Error(exception = e)
        }
        val decryptedKey: ByteArray = try {
            val secretKey = keyStore.getKey("grey_alice", null) as SecretKey
            val initializationVectorSpec = IvParameterSpec(initializationVector)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, initializationVectorSpec)
            cipher.doFinal(encryptedKey)
        } catch (e: InvalidKeyException) {
            return AliceRequest2.Error(exception = e)
        } catch (e: Exception) {
            return AliceRequest2.Error(exception = e)
        }
        return AliceRequest2.Success(data = decryptedKey)
    } else {
        return AliceRequest2.KeyNotFound
    }
}
