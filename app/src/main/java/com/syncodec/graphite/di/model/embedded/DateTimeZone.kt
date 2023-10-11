package com.syncodec.graphite.di.model.embedded

import io.realm.kotlin.types.EmbeddedRealmObject
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import kotlin.random.Random


//class DateTimeZone : EmbeddedRealmObject {
//	var date : LocalDate = LocalDate.now()
//	var time : LocalTime = LocalTime.now()
//	var timeZone : ZoneId = ZoneId.systemDefault()
//
//	override fun hashCode(): Int {
//		var result = date.hashCode()
//		result = 31 * result + time.hashCode()
//		result = 31 * result + timeZone.hashCode()
//		return result
//	}
//
//	override fun equals(other: Any?): Boolean {
//		if (this === other) return true
//		if (javaClass != other?.javaClass) return false
//
//		other as DateTimeZone
//
//		if (date != other.date) return false
//		if (time != other.time) return false
//		if (timeZone != other.timeZone) return false
//
//		return true
//	}
//
//	companion object {
//		fun getRandomInstance() : DateTimeZone = DateTimeZone().apply {
//			date = LocalDate.of(2023, Random.nextInt(1, 12), Random.nextInt(1, 28))
//			time = LocalTime.of(Random.nextInt(0, 23), Random.nextInt(0, 59))
//		}
//	}
//}
