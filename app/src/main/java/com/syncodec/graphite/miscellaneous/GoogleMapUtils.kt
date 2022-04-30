package com.syncodec.graphite.miscellaneous

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng

class GoogleMapUtils {
	companion object {
		fun GoogleMap.isMarkerVisible(markerPosition: LatLng) = projection.visibleRegion.latLngBounds.contains(markerPosition)
	}
}
