package com.syncodec.graphite.di.model

import androidx.annotation.Keep
import io.realm.kotlin.types.EmbeddedRealmObject
import java.time.Instant
import java.util.TimeZone


@Keep
class TimestampObject() : EmbeddedRealmObject {

    var timestampMilliUtc: Long = System.currentTimeMillis()
    var timeZone = TimeZone.getDefault().id

    override fun hashCode(): Int {
        var result = timestampMilliUtc.hashCode()
        result = 31 * result + timeZone.hashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is com.syncodec.graphite.di.model.TimestampObject) return false

        if (timestampMilliUtc != other.timestampMilliUtc) return false
        if (timeZone != other.timeZone) return false

        return true
    }
}
