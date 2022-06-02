package com.syncodec.graphite.alice

import java.io.UnsupportedEncodingException
import java.security.GeneralSecurityException
import java.security.SecureRandom
import java.security.spec.KeySpec
import java.util.*
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec


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
			return Base64.getEncoder().withoutPadding().encodeToString(bytes)
		}

		private fun fromBase64(base64: String): ByteArray {
			return Base64.getDecoder().decode(base64)
		}
	}
}
