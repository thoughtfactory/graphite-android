package com.syncodec.graphite.di.sync


class DropboxMetadata() {
	val noteMetadata: MutableMap<String, DropboxInnerMetadata> = mutableMapOf()
	val chapterMetadata: MutableMap<String, DropboxInnerMetadata> = mutableMapOf()
}

data class DropboxInnerMetadata(
	val modifiedTimestamp : Long,
	val hash: String,
	val isDeleted: Boolean
)
