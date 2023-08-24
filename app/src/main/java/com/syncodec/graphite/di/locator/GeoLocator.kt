package com.syncodec.graphite.di.locator

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Build
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource


class Locator(private val context: Context) {

	private val fusedLocationClient: FusedLocationProviderClient by lazy { LocationServices.getFusedLocationProviderClient(context) }
	private var locationCancellationSource: CancellationTokenSource? = null
	private val geocoder: Geocoder by lazy { Geocoder(context) }

	fun getCurrentLocation(withAddress: Boolean = false, callback: (LocationState) -> Unit) {
		if (MOCK_LOCATION) {
			callback(LocationState.Location(lat = LATITUDE, lng = LONGITUDE, address = ADDRESS))
			return
		}

		callback(LocationState.Loading)

		locationCancellationSource?.cancel()
		locationCancellationSource = CancellationTokenSource()

		if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
			ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
		) {
			callback(LocationState.NoPermission)
			return
		} else {
			fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, locationCancellationSource?.token)
				.addOnSuccessListener { location ->
					try {
						if (withAddress) {
							getAddress(lat = location.latitude, lng = location.longitude) {
								it?.also { callback(LocationState.Location(lat = location.latitude, lng = location.longitude, address = it)) } ?: callback(LocationState.OnlyLatLng(lat = location.latitude, lng = location.longitude))
								return@getAddress
							}
						} else {
							callback(LocationState.OnlyLatLng(lat = location.latitude, lng = location.longitude))
							return@addOnSuccessListener
						}
					} catch (_: Exception) {
						callback(LocationState.OnlyLatLng(lat = location.latitude, lng = location.longitude))
						return@addOnSuccessListener
					}
				}
				.addOnFailureListener {
					it.printStackTrace()
					callback(LocationState.UnknownError())
					return@addOnFailureListener
				}
		}
	}

	fun getAddress(lat: Double, lng: Double, callback: (String?) -> Unit) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
			geocoder.getFromLocation(lat, lng, 1) {
				callback(locationAddressFilter(it.firstOrNull()))
			}
		} else {
			val address = geocoder.getFromLocation(lat, lng, 1)
			callback(locationAddressFilter(address?.firstOrNull()))
		}
	}

	companion object {

		const val MOCK_LOCATION = true
		const val LATITUDE = 23.129107997475042
		const val LONGITUDE = 72.54507396851794
		const val ADDRESS = "Sarkhej - Gandhinagar Hwy, Gota, Ahmedabad, Gujarat 382481, India"

		sealed class LocationState {
			object Init : LocationState()
			object NoPermission : LocationState()
			object Loading : LocationState()
			data class OnlyLatLng(val lat: Double, val lng: Double) : LocationState()
			data class OnlyAddress(val address: String) : LocationState()
			data class Location(val lat: Double, val lng: Double, val address: String) : LocationState()
			object GpsUnavailable : LocationState()
			data class UnknownError(val retry: Int? = null) : LocationState()
		}

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
	}
}