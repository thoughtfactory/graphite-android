package com.syncodec.momento.miscellaneous

import android.content.Context
import android.location.Address
import android.net.Uri
import android.text.format.DateFormat
import androidx.core.content.FileProvider
import com.syncodec.momento.BuildConfig
import com.syncodec.momento.konstant.AttachmentType
import java.io.File
import java.io.IOException
import java.util.*
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.random.Random

fun timeStampToPrettyDay(timestamp: Long): String {
	return DateFormat.format("dd MMM, yyyy EEE", timestamp).toString()
}

fun timeStampToPrettyFull(timestamp: Long): String {
	return DateFormat.format("EEE dd MMM, yyyy, HH:mm aa", timestamp).toString()
}

fun timeStampToTime(timestamp: Long): String {
	return DateFormat.format("HH:mm aa", timestamp).toString()
}

fun generatePrimaryKey(keyLength: Int = 20): String {
	val chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray()
	val stringBuilder = StringBuilder(20)
	for (i in 0 until keyLength) {
		val c = chars[Random.nextInt(chars.size)]
		stringBuilder.append(c)
	}
	return stringBuilder.toString()
}

@Throws(IOException::class)
fun createTempFile(attachmentType: AttachmentType): File {
	return File.createTempFile("${attachmentType.name.lowercase(Locale.getDefault())}_", "_${generatePrimaryKey(8)}")
}

@Throws(IOException::class)
fun createTempFileToExpose(context: Context, attachmentType: AttachmentType): Uri {
	return FileProvider.getUriForFile(
		context,
		"${BuildConfig.APPLICATION_ID}.provider",
		createTempFile(attachmentType)
	)
}

fun locationAddressFilter(address: Address?): String? {
	return if (address == null) {
		null
	} else {
		(if (address.featureName != null) "${address.featureName}, " else "") +
				(if (address.thoroughfare != null) "${address.thoroughfare}, " else "") +
				(if (address.locality != null) "${address.locality}, " else "") +
				(if (address.subAdminArea != null) "${address.subAdminArea}, " else "") +
				(if (address.adminArea != null) "${address.adminArea}, " else "") +
				(if (address.postalCode != null) "${address.postalCode}, " else "") +
				if (address.countryName != null) address.countryName else ""
	}
}

fun Double.roundTo(numFractionDigits: Int): Double {
	val factor = 10.0.pow(numFractionDigits.toDouble())
	return (this * factor).roundToInt() / factor
}
