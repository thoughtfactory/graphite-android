package com.syncodec.graphite.di.modelObjectBox

import com.syncodec.graphite.di.modelObjectBox.customObject.Location
import com.syncodec.graphite.di.modelObjectBox.customObject.LocationConverter
import com.syncodec.graphite.di.modelObjectBox.customObject.Timestamp
import com.syncodec.graphite.di.modelObjectBox.customObject.TimestampConverter
import io.objectbox.annotation.Convert
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import kotlinx.serialization.InternalSerializationApi


@OptIn(InternalSerializationApi::class)
@Entity
data class NoteBox(
    @Id var id: Long = 0,

    @Convert(converter = TimestampConverter::class, dbType = String::class) var createdTimestamp: Timestamp = Timestamp(),
    @Convert(converter = TimestampConverter::class, dbType = String::class) var modifiedTimestamp: Timestamp = Timestamp(),
    @Convert(converter = TimestampConverter::class, dbType = String::class) var userTimestamp: Timestamp = Timestamp(),

    var title: String? = null,

    @Convert(converter = LocationConverter::class, dbType = String::class) var location: Location?,

    var isFavourite: Boolean = false,
    var isLocked: Boolean = false
)
