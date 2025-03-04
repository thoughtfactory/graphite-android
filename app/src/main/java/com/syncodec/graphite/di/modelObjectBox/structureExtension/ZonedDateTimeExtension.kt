package com.syncodec.graphite.di.modelObjectBox.structureExtension

import java.text.DateFormat
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale


fun ZonedDateTime.toPretty(): String = DateTimeFormatter.ofPattern("dd MMM, yyyy EEE, hh:mm:ss a").format(this) + "\n${this.zone.getDisplayName(TextStyle.FULL, Locale.getDefault())}"
