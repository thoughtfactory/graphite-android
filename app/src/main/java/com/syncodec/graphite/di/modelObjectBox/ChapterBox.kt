package com.syncodec.graphite.di.modelObjectBox

import com.syncodec.graphite.di.modelObjectBox.converter.ZonedDateTimeConverter
import com.syncodec.graphite.di.modelObjectBox.customObject.Thumbnail
import com.syncodec.graphite.di.modelObjectBox.customObject.ThumbnailConverter
import io.objectbox.annotation.Convert
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import kotlinx.serialization.InternalSerializationApi
import java.time.ZonedDateTime


@OptIn(InternalSerializationApi::class)
@Entity
data class ChapterBox(
    @Id var id: Long = 0,

    @Convert(converter = ZonedDateTimeConverter::class, dbType = String::class) var createdTimestamp: ZonedDateTime? = ZonedDateTime.now(),
    @Convert(converter = ZonedDateTimeConverter::class, dbType = String::class) var modifiedTimestamp: ZonedDateTime? = ZonedDateTime.now(),

    var title: String? = null,
    var description: String? = null,

    @Convert(converter = ThumbnailConverter::class, dbType = String::class) var thumbnail: Thumbnail? = null,

    var isFavourite: Boolean = false,
    var isLocked: Boolean = false,
)
