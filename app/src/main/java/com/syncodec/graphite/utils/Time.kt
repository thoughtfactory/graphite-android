package com.syncodec.graphite.utils

import android.text.format.DateFormat
import java.text.SimpleDateFormat
import java.util.*

fun Long.timeStampToPrettyDay(): String = DateFormat.format("dd MMM, yyyy EEE", this).toString()

fun Long.timeStampToPrettyFull(): String = DateFormat.format("EEE dd MMM, yyyy, HH:mm aa", this).toString()

fun Long.timeStampToPrettyFullStop(): String = DateFormat.format("EEE dd MMM, yyyy, HH mm ss aa", this).toString()

fun Long.timeStampToPrettyNumber(): String = DateFormat.format("dd MM yyyy HH mm ss", this).toString()

fun timeStampToTime(timestamp: Long): String = DateFormat.format("HH:mm aa", timestamp).toString()

fun timestampToDate(timestamp: Long): String = DateFormat.format("EEE dd MMM, yyyy", timestamp).toString()

fun entryTimestamp0(timestamp: Long): String = DateFormat.format("EEE dd MMM", timestamp).toString()

fun entryTimestamp1(timestamp: Long): String = DateFormat.format(", yyyy, HH:mm aa", timestamp).toString()

fun noteViewerTimestamp(timestamp: Long): List<String> = listOf(
	DateFormat.format("dd", timestamp).toString(),
	DateFormat.format("E, hh:mm a", timestamp).toString(),
	DateFormat.format("MMMM yyyy", timestamp).toString()
)

fun timestampToCalendarDay(timestamp: Long) : Long {
	val calendar = Calendar.getInstance()
	calendar.timeInMillis = timestamp


	calendar.set(Calendar.MILLISECOND, 0)
	calendar.set(Calendar.SECOND, 0)
	calendar.set(Calendar.MINUTE, 0)
	calendar.set(Calendar.HOUR_OF_DAY, 0)

	return calendar.timeInMillis
}

fun getToday(): Long = timestampToCalendarDay(System.currentTimeMillis())

fun quoteTimestampToKey(timestamp: Long) = SimpleDateFormat("yyyy_MM_dd").format(timestamp)

fun quoteKeyToTimestamp(key: String) : Long? = SimpleDateFormat("yyyy_MM_dd").parse(key)?.time
