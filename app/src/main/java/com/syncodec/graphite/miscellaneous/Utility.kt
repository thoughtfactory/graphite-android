package com.syncodec.graphite.miscellaneous

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Address
import android.util.Base64
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import java.io.*
import java.util.*
import kotlin.math.pow
import kotlin.math.roundToInt

fun generatePrimaryKey(): String = UUID.randomUUID().toString()

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

fun Bitmap.bitmapToBase64String(): String? {
	val outputStream = ByteArrayOutputStream()
	this.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
	return Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
}

@Throws(IllegalArgumentException::class)
fun String.base64stringToBitmap(): Bitmap? {
	val decodedBytes: ByteArray = Base64.decode(this.substring(this.indexOf(",") + 1), Base64.DEFAULT)
	return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
}

fun getResizedBitmap(image: Bitmap, maxSize: Int): Bitmap? {
	var width = image.width
	var height = image.height
	val bitmapRatio = width.toFloat() / height.toFloat()
	if (bitmapRatio > 1) {
		width = maxSize
		height = (width / bitmapRatio).toInt()
	} else {
		height = maxSize
		width = (height * bitmapRatio).toInt()
	}
	return Bitmap.createScaledBitmap(image, width, height, true)
}

@Throws(IOException::class)
fun getStringFromInputStream(stream: InputStream?): String {
	var n = 0
	val buffer = CharArray(1024 * 4)
	val reader = InputStreamReader(stream, "UTF8")
	val writer = StringWriter()
	while (-1 != reader.read(buffer).also { n = it }) writer.write(buffer, 0, n)
	return writer.toString()
}

fun Color.toHexString(): String {
	return String.format("#%06X", (0xFFFFFF and this.toArgb()))
}

fun filterData(
	showArchived: Boolean,
	isArchived: Boolean,
	showFavourite: Boolean,
	isFavourite: Boolean,
	showLocked: Boolean,
	isLocked: Boolean,
): Boolean {
	return when {
		showArchived and showFavourite and showLocked -> isArchived and isFavourite and isLocked
		showArchived and showFavourite and !showLocked -> isArchived and isFavourite and !isLocked
		showArchived and !showFavourite and showLocked -> isArchived and !isFavourite and isLocked
		showArchived and !showFavourite and !showLocked -> isArchived and !isFavourite and !isLocked
		!showArchived and showFavourite and showLocked -> !isArchived and isFavourite and isLocked
		!showArchived and showFavourite and !showLocked -> !isArchived and isFavourite and !isLocked
		!showArchived and !showFavourite and showLocked -> !isArchived and !isFavourite and isLocked
		!showArchived and !showFavourite and !showLocked -> !isArchived and !isLocked
		else -> false
	}
}
