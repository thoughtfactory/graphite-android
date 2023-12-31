package com.syncodec.graphite.di.model.local.ext

import com.syncodec.graphite.di.cloud.dropbox.DropboxObjectMetadata


interface Syncable {
	fun toCloudSnapshot(): String
	fun toObjectMetadata(): DropboxObjectMetadata
}