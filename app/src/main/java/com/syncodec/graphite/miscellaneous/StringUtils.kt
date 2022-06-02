package com.syncodec.graphite.miscellaneous

import java.nio.charset.StandardCharsets
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

class StringUtils {
	companion object {
		fun String.addEmptyLines(lines: Int) = this + "\n".repeat(lines)

		fun String.containsAnyOfIgnoreCase(keywords: List<String>): Boolean {
			for (keyword in keywords) {
				if (this.contains(keyword, true)) return true
			}
			return false
		}

		fun String.encrypt(): String {
			val random = SecureRandom()
			val salt = ByteArray(256)
			random.nextBytes(salt)

			val pbKeySpec =
				PBEKeySpec("cJj1w1^x00#r!37#tM@46tM1q1d*&Cm".toCharArray(), salt, 1324, 256)
			val secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
			val keyBytes = secretKeyFactory.generateSecret(pbKeySpec).encoded
			val keySpec = SecretKeySpec(keyBytes, "AES")

			val ivRandom = SecureRandom()
			val iv = ByteArray(16)
			ivRandom.nextBytes(iv)
			val ivSpec = IvParameterSpec(iv)

			val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
			cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec)
			val encrypted = cipher.doFinal(toByteArray())

			return salt.toString(StandardCharsets.ISO_8859_1) +
					iv.toString(StandardCharsets.ISO_8859_1) +
					encrypted.toString(StandardCharsets.ISO_8859_1)
		}

		fun String.decrypt(): String {
			return try {
				if (length > 256 + 16) {
					val salt = substring(0, 256).toByteArray(StandardCharsets.ISO_8859_1)
					val iv =
						substring(256, 256 + 16).toByteArray(StandardCharsets.ISO_8859_1)
					val cipherText =
						substring(256 + 16).toByteArray(StandardCharsets.ISO_8859_1)

					val pbKeySpec = PBEKeySpec("cJj1w1^x00#r!37#tM@46tM1q1d*&Cm".toCharArray(), salt, 1324, 256)
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
	}
}
