package com.syncodec.graphite.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities


class NetworkUtils {
	companion object {
		fun Context.isInternetAvailable(): Boolean {
			var result = false
			val connectivityManager = getSystemService(ConnectivityManager::class.java)
			val networkCapabilities = connectivityManager.activeNetwork ?: return false
			val actNw =
				connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
			result = when {
				actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
				actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
				actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
				else -> false
			}

			return result
		}
	}
}
