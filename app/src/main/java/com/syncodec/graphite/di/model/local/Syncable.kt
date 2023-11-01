package com.syncodec.graphite.di.model.local


interface Syncable {
	fun toCloudSnapshot(): String
}