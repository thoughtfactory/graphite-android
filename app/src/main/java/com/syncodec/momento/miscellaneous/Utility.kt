package com.syncodec.momento.miscellaneous

import android.content.Context
import android.net.Uri
import android.text.format.DateFormat
import androidx.core.content.FileProvider
import com.syncodec.momento.BuildConfig
import com.syncodec.momento.konstant.MediaType
import java.io.File
import java.io.IOException
import java.util.*
import kotlin.random.Random

fun timeStampToPrettyDay(timestamp: Long): String {
	return DateFormat.format("dd MMM, yyyy", timestamp).toString()
}

fun timeStampToPrettyFull(timestamp: Long): String {
	return DateFormat.format("EEE dd MMM, yyyy, HH:mm aa", timestamp).toString()
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
fun createTempFile(mediaType: MediaType): File {
	return File.createTempFile("${mediaType.name.lowercase(Locale.getDefault())}_", "_${generatePrimaryKey(8)}")
}

@Throws(IOException::class)
fun createTempFileToExpose(context: Context, mediaType: MediaType): Uri {
	return FileProvider.getUriForFile(
		context,
		"${BuildConfig.APPLICATION_ID}.provider",
		createTempFile(mediaType)
	)
}
