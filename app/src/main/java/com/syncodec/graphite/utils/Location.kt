package com.syncodec.graphite.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Build
import androidx.annotation.WorkerThread
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.syncodec.graphite.BuildConfig
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.LatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import java.io.IOException
import java.util.Locale


fun locationAddressFilter(address: Address?): String? {
	return if (address == null) {
		null
	} else {
		(if (address.featureName != null) "${address.featureName}, " else "") +
				(if (address.thoroughfare != null) "${address.thoroughfare}, " else "") +
				(if (address.locality != null) "${address.locality}, " else "") +
				(if (address.subAdminArea != null) "${address.subAdminArea}, " else "") +
				(if (address.adminArea != null) "${address.adminArea}, " else "") +
				(if (address.postalCode != null) "${address.postalCode}, " else "") +
				if (address.countryName != null) address.countryName else ""
	}
}

object Location {
	private var locationCoroutine: CoroutineScope? = null
	private var locationCancellationSource: CancellationTokenSource? = null

	fun CoroutineScope.getLocation(context: Context, callback: (LocationData) -> Unit) {
		locationCancellationSource?.cancel()
		locationCoroutine?.cancel()
		locationCancellationSource = CancellationTokenSource()
		locationCoroutine = this

		val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
		if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
			ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
		) {
			callback(LocationData.NoPermission)
		} else {
			fusedLocationClient
				.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, locationCancellationSource?.token)
				.addOnSuccessListener { location ->
					callback(LocationData.SuccessOnlyLatLng(LatLng(location.latitude, location.longitude)))
					try {
						reverseGeocode(
							context = context,
							latitude = location.latitude,
							longitude = location.longitude,
							onAddressAvailable = { address ->
								locationAddressFilter(address = address)?.let { callback(LocationData.Success(LatLng(location.latitude, location.longitude), it)) }
							},
							onIoException = {},
							onException = {}
						)
					} catch (_: Exception) {
						callback(LocationData.Error(message = "Unknown error"))
					}
				}
				.addOnFailureListener {
					callback(LocationData.Error(message = "Unknown error"))
				}
		}
	}

	@WorkerThread
	fun reverseGeocode(
		context: Context,
		latitude: Double,
		longitude: Double,
		onAddressAvailable: (Address?) -> Unit,
		onIoException: () -> Unit = {},
		onException: () -> Unit = {}
	) {
		try {
			val geocoder = Geocoder(context, Locale.getDefault())
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
				geocoder.getFromLocation(latitude, longitude, 1) { addresses ->
					onAddressAvailable(addresses.getOrNull(0))
				}
			} else {
//			    Deprecation is handled in upper block
				val addresses = geocoder.getFromLocation(latitude, longitude, 1)
				onAddressAvailable(addresses?.firstOrNull())
			}

		} catch (e: IOException) {
			if (BuildConfig.DEBUG) e.printStackTrace()
			onIoException()
		} catch (e: Exception) {
			if (BuildConfig.DEBUG) e.printStackTrace()
			onException()
		}
	}

	fun getMapStyle(isDarkTheme: Boolean): Int = if (isDarkTheme) R.raw.map_style_night_1 else R.raw.map_style_day_1
}
