package com.syncodec.graphite.presentation.note.util

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import androidx.annotation.WorkerThread
import java.io.IOException
import java.util.Locale


@WorkerThread
fun Context.reverseGeocode(
	latitude : Double,
	longitude : Double,
	onAddressAvailable : (Address?) -> Unit,
	onIoException : () -> Unit = {},
	onException : () -> Unit = {}
) {
	try {
		val geocoder = Geocoder(this, Locale.getDefault())
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
			geocoder.getFromLocation(latitude, longitude, 1) { addresses ->
				onAddressAvailable(addresses.getOrNull(0))
			}
		} else {
//				Deprecation is handled in upper block
			val addresses = geocoder.getFromLocation(latitude, longitude, 1)
			onAddressAvailable(addresses?.firstOrNull())
		}

	} catch (exception : IOException) {
		onIoException()
	} catch (exception : Exception) {
		onException()
	}
}
