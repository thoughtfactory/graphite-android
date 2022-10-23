package com.syncodec.graphite.di.network

import android.util.Log
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.syncodec.graphite.di.model.QuoteObject
import org.json.JSONObject
import java.nio.charset.Charset


class FirebaseStorageApi  {
	private val storage = Firebase.storage("gs://graphite-diary.appspot.com")
	val storageRef = storage.reference
	val quoteDirRef = storageRef.child("server/enQuote")

	fun StorageReference.getQuote(
		onSuccess: (QuoteObject) -> Unit
	) {
		getBytes(16 * 1024)
			.addOnSuccessListener {
				try {
					val jsonObject = JSONObject(it.toString(Charset.defaultCharset()))

					if (jsonObject.optInt("v") == 1) {
						QuoteObject().apply {
							this.date = this@getQuote.name.split(".").firstOrNull()
							this.quote = jsonObject.optString("quote")
							this.author = jsonObject.optString("author")
							this.authorLink = jsonObject.optString("authorLink")
							this.bgLink = jsonObject.optString("bgLink")
							this.bgCred = jsonObject.optString("bgCred")
							this.bgCredLink = jsonObject.optString("bgCredLink")
							this.bgProvider = jsonObject.optString("bgProvider")
							this.bgProviderLink = jsonObject.optString("bgProviderLink")
							this.special = jsonObject.optString("special")
							this.specialLink = jsonObject.optString("specialLink")

							onSuccess(this)
						}
					}

				} catch (e: Exception) {
					e.printStackTrace()
					Log.e("QUOTE", "Error decoding quote data")
				}
			}
	}

	fun StorageReference.getQuoteBg(
		onSuccess: (ByteArray) -> Unit
	) {
		Log.i("npr71", "fetQuoteBg path : ${this.path}")
		getBytes(16 * 1024)
			.addOnSuccessListener {
				try {
					onSuccess(it)
				} catch (e: Exception) {
					e.printStackTrace()
					Log.e("QUOTE", "Error decoding quote bg")
				}
			}
	}

//	note    In yyyy_mm_dd format
	fun getQuote(date: String, onSuccess: (QuoteObject) -> Unit) {
		quoteDirRef
			.child(date)
			.child("$date.json")
			.getQuote(onSuccess)
	}

	fun getQuoteBg(date: String, onSuccess: (ByteArray) -> Unit) {
		quoteDirRef
			.child(date)
			.child("$date.jpg")
			.getQuoteBg(onSuccess)
	}
}
