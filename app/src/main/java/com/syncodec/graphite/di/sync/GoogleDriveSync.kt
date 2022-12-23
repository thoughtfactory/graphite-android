package com.syncodec.graphite.di.sync

//import android.content.Context
//import android.content.Intent
//import android.util.Log
//import com.google.android.gms.auth.api.signin.GoogleSignIn
//import com.google.android.gms.auth.api.signin.GoogleSignInAccount
//import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
//import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
//import com.google.api.client.http.javanet.NetHttpTransport
//import com.google.api.client.json.gson.GsonFactory
//import com.google.api.services.drive.Drive
//import com.google.api.services.drive.DriveScopes
//import com.google.api.services.drive.model.About.StorageQuota
//import com.google.firebase.auth.FirebaseAuth
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch


//data class GoogleDriveResult(
//	val onSuccess : (storageQuota : StorageQuota) -> Unit,
//	val onError : (e : Exception) -> Unit
//)
//
//class GoogleDriveSync(val context : Context) {
//
//	fun testConnection(googleDriveResult : GoogleDriveResult) {
//		try {
//			val auth = FirebaseAuth.getInstance()
//
//			GoogleAccountCredential
//				.usingOAuth2(context, listOf(DriveScopes.DRIVE_APPDATA))
//				.setSelectedAccountName(auth.currentUser?.email)
//				.allAccounts.let {
//					it.forEach {
//						Log.d("npr71", "testConnection: ${it.name}")
//					}
//				}
//
//			Log.i("npr71", "scopes : ${GoogleSignIn.getLastSignedInAccount(context)?.grantedScopes}")
//
//
////			val drive = Drive
////				.Builder(
////					NetHttpTransport(),
////					GsonFactory.getDefaultInstance(),
////
////				)
////				.setApplicationName("Graphite")
////				.build()
////			val drive = Drive
////				.Builder(
////					NetHttpTransport(),
////					GsonFactory.getDefaultInstance(),
////					GoogleAccountCredential
////						.usingOAuth2(context, listOf(DriveScopes.DRIVE_APPDATA))
////						.setSelectedAccountName(auth.currentUser !!.email)
////				)
////				.setApplicationName("Graphite")
////				.build()
//
////			CoroutineScope(Dispatchers.IO).launch {
////				try {
////					drive
////						.about()
////						.get()
////						.apply {
////							fields = "storageQuota"
////							execute()
////								.storageQuota
////								.let {
////									googleDriveResult.onSuccess(it)
////								}
////						}
////				} catch (e: UserRecoverableAuthIOException) {
////					e.intent.apply {
////						flags = Intent.FLAG_ACTIVITY_NEW_TASK
////						context.startActivity(this)
////					}
////				}
////				catch (e : Exception) {
////					e.printStackTrace()
////					googleDriveResult.onError(e)
////				}
////			}
//		} catch (e : Exception) {
//			e.printStackTrace()
//			googleDriveResult.onError(e)
//			return
//		}
//	}
//}
