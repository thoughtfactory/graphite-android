package com.syncodec.graphite.di.modelObjectBox

import com.syncodec.graphite.di.modelObjectBox.converter.ZonedDateTimeConverter
import com.syncodec.graphite.di.modelObjectBox.customObject.Location
import com.syncodec.graphite.di.modelObjectBox.customObject.LocationConverter
import io.objectbox.annotation.Convert
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import kotlinx.serialization.InternalSerializationApi
import java.time.ZonedDateTime


@OptIn(InternalSerializationApi::class)
@Entity
data class NoteBox(
    @Id var id: Long = 0,

    @Convert(converter = ZonedDateTimeConverter::class, dbType = String::class) var createdTimestamp: ZonedDateTime = ZonedDateTime.now(),
    @Convert(converter = ZonedDateTimeConverter::class, dbType = String::class) var modifiedTimestamp: ZonedDateTime = ZonedDateTime.now(),
    @Convert(converter = ZonedDateTimeConverter::class, dbType = String::class) var userTimestamp: ZonedDateTime = ZonedDateTime.now(),

    var title: String? = null,

    @Convert(converter = LocationConverter::class, dbType = String::class) var location: Location?,

    var isFavourite: Boolean = false,
    var isLocked: Boolean = false
)
