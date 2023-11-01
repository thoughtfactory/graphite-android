package com.syncodec.graphite.di.cloud

import java.io.File


abstract class CloudConnection {
	abstract suspend fun disconnect()
	abstract suspend fun refreshConnection() : Boolean
	abstract suspend fun uploadSnapshot(snapshotFile : File) : Boolean
	abstract suspend fun scanSnapshot()
	abstract suspend fun downloadSnapshot(fileId : String) : ByteArray?
}