package com.syncodec.graphite.di.modelObjectBox

import com.syncodec.graphite.di.modelObjectBox.customObject.Thumbnail
import com.syncodec.graphite.di.modelObjectBox.customObject.ThumbnailConverter
import com.syncodec.graphite.di.modelObjectBox.customObject.Timestamp
import com.syncodec.graphite.di.modelObjectBox.customObject.TimestampConverter
import io.objectbox.annotation.Convert
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import kotlinx.serialization.InternalSerializationApi


@OptIn(InternalSerializationApi::class)
@Entity
data class ChapterBox(
    @Id var id: Long = 0,

    @Convert(converter = TimestampConverter::class, dbType = String::class) var createdTimestamp: Timestamp? = Timestamp(),
//    @Convert(converter = TimestampConverter::class, dbType = String::class) var modifiedTimestamp: Timestamp = Timestamp(),

    var title: String? = null,
    var description: String? = null,

//    @Convert(converter = ThumbnailConverter::class, dbType = String::class) var thumbnail: Thumbnail? = null,

    var isFavourite: Boolean = false,
    var isLocked: Boolean = false
)
