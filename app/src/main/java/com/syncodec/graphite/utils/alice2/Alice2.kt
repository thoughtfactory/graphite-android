package com.syncodec.graphite.utils.alice2

import android.content.Context
import android.util.Base64
import android.util.Log
import androidx.core.content.edit
import com.syncodec.graphite.utils.alice.generateSecretKey
import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.algorithms.AES
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.security.InvalidKeyException
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec


@OptIn(ExperimentalStdlibApi::class)
class Alice2(
    private val context: Context
) {

    private val provider = CryptographyProvider.Default
    private val aesGcm = provider.get(AES.GCM)
    var key: AES.GCM.Key?

    init {
        val dbKeyRequest = context.getSecretData(key = "db_key")
        key = when(dbKeyRequest) {
            is AliceRequest.KeyNotFound -> {
                val keyGenerator = aesGcm.keyGenerator(AES.Key.Size.B256)
                val key: AES.GCM.Key = keyGenerator.generateKeyBlocking()
                context.putSecretData("db_key", key.encodeToByteArrayBlocking(format = AES.Key.Format.RAW))
                key
            }
            is AliceRequest.UnknownError -> null
            is AliceRequest.Success -> {
                val encodedKey: ByteArray = dbKeyRequest.data
                aesGcm.keyDecoder().decodeFromByteArrayBlocking(format = AES.Key.Format.RAW, bytes = encodedKey)
            }
        }
    }

    fun encrypt(data: String): ByteArray? {
        return key?.cipher()?.encryptBlocking(data.encodeToByteArray())
    }

    fun encrypt(data: ByteArray): ByteArray? {
        return key?.cipher()?.encryptBlocking(data)
    }

    fun decrypt(data: ByteArray?): ByteArray? {
        data ?: return null
        return key?.cipher()?.decryptBlocking(data)
    }

    @OptIn(ExperimentalStdlibApi::class)
    fun decrypt(data: String?): String? {
        data ?: return null
        return key?.cipher()?.decryptBlocking(data.hexToByteArray(format = HexFormat.UpperCase))?.decodeToString()
    }

    companion object {

        const val TAG = "Alice2"

        sealed class AliceRequest {
            data object KeyNotFound : AliceRequest()
            data class UnknownError(val message: String?, val exception: Exception) : AliceRequest()
            data class Success(val data: ByteArray) : AliceRequest() {
                override fun equals(other: Any?): Boolean {
                    if (this === other) return true
                    if (other !is Success) return false

                    if (!data.contentEquals(other.data)) return false

                    return true
                }

                override fun hashCode(): Int {
                    return data.contentHashCode()
                }
            }
        }

        fun Context.putSecretData(key: String, value: ByteArray) {
            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)
            var secretKey = keyStore.getKey("grey_alice", null) as SecretKey?
            if (secretKey == null) generateSecretKey()
            secretKey = keyStore.getKey("grey_alice", null) as SecretKey?

            if (secretKey != null) {
                val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")

                cipher.init(Cipher.ENCRYPT_MODE, secretKey)
                val encryptedKeyForRealm: ByteArray = cipher.doFinal(value)
                val initializationVector: ByteArray = cipher.iv
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

        fun Context.getSecretData(key: String): AliceRequest {
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
                    Cipher.getInstance("AES/CBC/PKCS7Padding")
                } catch (e : Exception) {
                    return AliceRequest.UnknownError(message = e.message, exception = e)
                }
                val decryptedKey : ByteArray = try {
                    val secretKey = keyStore.getKey("grey_alice", null) as SecretKey
                    val initializationVectorSpec = IvParameterSpec(initializationVector)
                    cipher.init(Cipher.DECRYPT_MODE, secretKey, initializationVectorSpec)
                    cipher.doFinal(encryptedKey)
                } catch (e : InvalidKeyException) {
                    return AliceRequest.UnknownError(message = e.message, exception = e)
                } catch (e : Exception) {
                    return AliceRequest.UnknownError(message = e.message, exception = e)
                }
                return AliceRequest.Success(data = decryptedKey)
            } else {
                return AliceRequest.KeyNotFound
            }

        }
    }
}