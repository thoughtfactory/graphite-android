package com.syncodec.graphite.di.cloud.googleDrive

import android.content.Context
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.syncodec.graphite.di.cloud.dropbox.DBox

class GDrive(val context : Context) {
	fun getDrive() : Drive? {
		return try {
			val googleAccount = GoogleSignIn.getLastSignedInAccount(context)
			val credential = GoogleAccountCredential.usingOAuth2(context, listOf(DriveScopes.DRIVE_APPDATA))
			credential.selectedAccount = googleAccount?.account
			Drive
				.Builder(NetHttpTransport(), GsonFactory(), credential)
				.setApplicationName("Graphite")
				.build()
		} catch (exception : Exception) {
			Log.e("GDrive", "getDrive: ${exception.message}")
			null
		}
	}

	fun testConnection(callback : (DBox.Companion.TestConnectionResponse) -> Unit) {
		try {
			val googleAccount = GoogleSignIn.getLastSignedInAccount(context)
			val credential = GoogleAccountCredential.usingOAuth2(context, listOf(DriveScopes.DRIVE_APPDATA))
			credential.selectedAccount = googleAccount?.account
			if (credential.selectedAccount == null) {
				callback(DBox.Companion.TestConnectionResponse.NotLoggedIn)
				return
			} else {
				Drive
					.Builder(NetHttpTransport(), GsonFactory(), credential)
					.setApplicationName("Graphite")
					.build()
					.about()
					.get()
					.setFields("user, storageQuota")
					.execute()
					.let {
						callback(
							DBox.Companion.TestConnectionResponse.Success(
								name = it.user.displayName,
								email = it.user.emailAddress,
								profilePictureUrl = it.user.photoLink,
								spaceUsed = it.storageQuota.usageInDrive,
								spaceTotal = it.storageQuota.limit,
							)
						)
					}
			}
		} catch (exception : Exception) {
			exception.printStackTrace()
			callback(DBox.Companion.TestConnectionResponse.Error(exception, exception.message ?: "Unknown error"))
		}
	}
}
