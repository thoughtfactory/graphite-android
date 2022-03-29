package com.syncodec.momento.database.note

import com.google.android.gms.maps.model.LatLng

data class LocationData(
	var latitude: Double? = null,
	var longitude: Double? = null,
	var bearing: Float? = null,
	var altitude: Double? = null,
	var speed: Float? = null
)

fun locationDataToLatLng(locationData: LocationData?): LatLng? {
	return if (locationData == null) null else {
		if (locationData.latitude !=null && locationData.longitude!=null) {
			LatLng(locationData.latitude!!, locationData.longitude!!)
		} else null
	}
}
