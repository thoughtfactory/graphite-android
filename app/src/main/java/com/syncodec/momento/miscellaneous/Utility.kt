package com.syncodec.momento.miscellaneous

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Address
import android.net.Uri
import android.text.format.DateFormat
import android.util.Base64
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.FileProvider
import com.syncodec.momento.BuildConfig
import java.io.*
import java.net.URL
import java.net.URLConnection
import java.util.*
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1

fun logger(msg: String) = Log.i("npr71", msg)

fun timeStampToPrettyDay(timestamp: Long): String = DateFormat.format("dd MMM, yyyy EEE", timestamp).toString()

fun timeStampToPrettyFull(timestamp: Long): String = DateFormat.format("EEE dd MMM, yyyy, HH:mm aa", timestamp).toString()

fun timeStampToTime(timestamp: Long): String = DateFormat.format("HH:mm aa", timestamp).toString()

fun timestampToDate(timestamp: Long): String = DateFormat.format("EEE dd MMM, yyyy", timestamp).toString()

fun entryTimestamp0(timestamp: Long): String = DateFormat.format("EEE dd MMM", timestamp).toString()
fun entryTimestamp1(timestamp: Long): String = DateFormat.format(", yyyy, HH:mm aa", timestamp).toString()

fun noteViewerTimestamp(timestamp: Long): List<String> = listOf(
	DateFormat.format("dd", timestamp).toString(),
	DateFormat.format("E, hh:mm a", timestamp).toString(),
	DateFormat.format("MMMM yyyy", timestamp).toString()
)

fun generatePrimaryKey(): String = UUID.randomUUID().toString()

@Throws(IOException::class)
fun createTempFile(primaryKey: String, mimeType: String?): File = File.createTempFile("attachment_", "_$primaryKey")

@Throws(IOException::class)
fun createTempFileToExpose(context: Context, primaryKey: String, mimeType: String): Uri = FileProvider.getUriForFile(
	context,
	"${BuildConfig.APPLICATION_ID}.provider",
	createTempFile(primaryKey = primaryKey, mimeType = mimeType)
)


fun copyInputStreamToOutputStream(inputStream: FileInputStream, outputStream: FileOutputStream) = try {
	val buf = ByteArray(1024)
	var len: Int
	while (inputStream.read(buf).also { len = it } > 0) {
		outputStream.write(buf, 0, len)
	}
	outputStream.close()
	inputStream.close()
} catch (e: Exception) {
	e.printStackTrace()
}


fun copyInputStreamToOutputStream(inputStream: InputStream, outputStream: FileOutputStream) = try {
	val buf = ByteArray(1024)
	var len: Int
	while (inputStream.read(buf).also { len = it } > 0) {
		outputStream.write(buf, 0, len)
	}
	outputStream.close()
	inputStream.close()
} catch (e: Exception) {
	e.printStackTrace()
}

fun copyInputStreamToOutputStream(inputStream: InputStream, outputStream: OutputStream) = try {
	val buf = ByteArray(1024)
	var len: Int
	while (inputStream.read(buf).also { len = it } > 0) {
		outputStream.write(buf, 0, len)
	}
	outputStream.close()
	inputStream.close()
} catch (e: Exception) {
	e.printStackTrace()
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

inline fun <reified T, Y> MutableList<T>.listOfField(property: KMutableProperty1<T, Y?>): MutableList<Y> {
	val yy = ArrayList<Y>()
	this.forEach { t: T -> yy.add(property.get(t) as Y) }
	return yy
}

inline fun <reified T, Y> MutableList<T>.listOfField(property: KProperty1<T, Y?>): MutableList<Y> {
	val yy = ArrayList<Y>()
	this.forEach { t: T -> yy.add(property.get(t) as Y) }
	return yy
}

@JvmName("listOfFieldT")
inline fun <reified T, Y> List<T>.listOfField(property: KMutableProperty1<T, Y?>): List<Y> {
	val yy = ArrayList<Y>()
	this.forEach { t: T -> yy.add(property.get(t) as Y) }
	return yy
}

@JvmName("listOfFieldT")
inline fun <reified T, Y> List<T>.listOfField(property: KProperty1<T, Y?>): List<Y> {
	val yy = ArrayList<Y>()
	this.forEach { t: T -> yy.add(property.get(t) as Y) }
	return yy
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

fun downloadImage(
	thumbnailUrl: String,
): Bitmap? {
	val url = URL(thumbnailUrl)
	val connection: URLConnection = url.openConnection()
	connection.connect()

	val input: InputStream = BufferedInputStream(
		url.openStream(),
		8192
	)

	val output = ByteArrayOutputStream()
	val data = ByteArray(1024)

	var total: Long = 0
	var count = 0

	while (input.read(data).also { count = it } != -1) {
		total += count
		output.write(data, 0, count)
	}
	output.flush()
	output.close()
	input.close()

	val byteArray = output.toByteArray()
	return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
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
