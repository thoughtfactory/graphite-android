package com.syncodec.graphite.utils

import android.location.Address


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
