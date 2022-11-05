package com.syncodec.graphite.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.google.android.gms.common.util.Base64Utils
import java.nio.charset.StandardCharsets
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import kotlin.math.pow
import kotlin.math.roundToInt


fun String.addEmptyLines(lines: Int) = this + "\n".repeat(lines)

fun Double.roundTo(numFractionDigits: Int): Double {
	val factor = 10.0.pow(numFractionDigits.toDouble())
	return (this * factor).roundToInt() / factor
}

fun String.containsAnyOfIgnoreCase(keywords: List<String>): Boolean {
	for (keyword in keywords) {
		if (this.contains(keyword, true)) return true
	}
	return false
}

fun String.decodeBase64ToBitmap(): Bitmap? {
	return try {
		Base64Utils.decode(this).let { BitmapFactory.decodeByteArray(it, 0, it.size)}
	} catch (e: Exception) {
		null
	}
}

fun Bitmap.encodeBase64(): String? {
	return try {
		Base64Utils.encode(this.toByteArray())
	} catch (e: Exception) {
		null
	}
}

@Deprecated("Remove in next version")
fun String.encrypt(key: String = "cJj1w1^x00#r!37#tM@46tM1q1d*&Cm"): String? {
	try {
		val random = SecureRandom()
		val salt = ByteArray(256)
		random.nextBytes(salt)

		val pbKeySpec =
			PBEKeySpec(key.toCharArray(), salt, 1324, 256)
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
	} catch (exception : Exception) {
		return null
	}
}

@Deprecated("Remove in next version")
fun String.decrypt(key: String = "cJj1w1^x00#r!37#tM@46tM1q1d*&Cm"): String? {
	return try {
		if (length > 256 + 16) {
			val salt = substring(0, 256).toByteArray(StandardCharsets.ISO_8859_1)
			val iv =
				substring(256, 256 + 16).toByteArray(StandardCharsets.ISO_8859_1)
			val cipherText =
				substring(256 + 16).toByteArray(StandardCharsets.ISO_8859_1)

			val pbKeySpec = PBEKeySpec(key.toCharArray(), salt, 1324, 256)
			val secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
			val keyBytes = secretKeyFactory.generateSecret(pbKeySpec).encoded
			val keySpec = SecretKeySpec(keyBytes, "AES")

			val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
			val ivSpec = IvParameterSpec(iv)
			cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)
			val decrypted = cipher.doFinal(cipherText)

			decrypted.toString(StandardCharsets.ISO_8859_1)
		} else {
			null
		}
	} catch (exception: Exception) {
		null
	}
}

fun ByteArray.toHex(): String = joinToString(separator = "") { eachByte -> "%02x".format(eachByte) }

inline fun <reified T : Enum<T>> safeValueOf(type: String?): T? {
	return type?.let { java.lang.Enum.valueOf(T::class.java, it) }
}
