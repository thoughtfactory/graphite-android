package com.syncodec.momento.database.note

data class LocationData(
	var latitude: Double? = null,
	var longitude: Double? = null,
	var bearing: Float? = null,
	var altitude: Double? = null,
	var speed: Float? = null
)
