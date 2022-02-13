package com.syncodec.momento.diaryComponent

import android.annotation.SuppressLint
import android.app.Application
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.tasks.CancellationTokenSource
import com.syncodec.momento.Momento
import com.syncodec.momento.database.diary.Note
import com.syncodec.momento.database.diary.WeatherData
import com.syncodec.momento.konstant.Secret
import com.syncodec.momento.konstant.Status
import com.syncodec.momento.miscellaneous.copyInputStreamToOutputStream
import com.syncodec.momento.miscellaneous.generatePrimaryKey
import com.syncodec.momento.miscellaneous.locationAddressFilter
import com.syncodec.momento.repository.AttachmentRepository
import com.syncodec.momento.repository.DiaryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

data class TempAttachmentData(
	val primaryKey: String,
	val uri: Uri,
	val mimeType: String?,
	var file: File? = null
)

class DiaryViewModel(application: Application) : AndroidViewModel(application) {

	val diaryRepository: DiaryRepository = DiaryRepository(application)
	val attachmentRepository: AttachmentRepository = AttachmentRepository(application as Momento)

	private val _status: MutableState<Status> = mutableStateOf(Status.SUCCESS)
	val status: State<Status> get() = _status

	private val currentTimestamp = System.currentTimeMillis()

	var gpsLocation by mutableStateOf<Location?>(null)
	var location by mutableStateOf<Location?>(null)
	var address by mutableStateOf<String?>(null)
	var weatherData by mutableStateOf<WeatherData?>(null)

	var note: Note by mutableStateOf(
		Note(
			primaryKey = generatePrimaryKey(),
			timezoneOffset = TimeZone
				.getDefault()
				.getOffset(currentTimestamp)
		).apply {
			this.createdTimestamp = currentTimestamp
			this.modifiedTimestamp = currentTimestamp
			this.userTimestamp = currentTimestamp
		}
	)

	lateinit var diaryActivityState: DiaryActivity.DiaryActivityState

	var attachmentList: MutableList<TempAttachmentData> = mutableStateListOf()

	fun insertAttachment(
		uri: Uri,
		mimeType: String?
	) {
		val tempAttachmentData = TempAttachmentData(
			primaryKey = generatePrimaryKey(),
			uri = uri,
			mimeType = mimeType
		)

		val tempFile = com.syncodec.momento.miscellaneous.createTempFile(
			primaryKey = tempAttachmentData.primaryKey,
			mimeType = tempAttachmentData.mimeType
		)

		tempAttachmentData.file = tempFile

		val inputStream = getApplication<Momento>().contentResolver.openInputStream(tempAttachmentData.uri)
		val outputStream = FileOutputStream(tempFile)

		if (inputStream != null) {
			copyInputStreamToOutputStream(inputStream = inputStream, outputStream = outputStream)
		}

		attachmentList.add(tempAttachmentData)
	}

	fun removeLocationData() {
		diaryActivityState.addressState.value = DiaryActivity.AddressState.REMOVED
		location = null
		address = null
	}

	fun saveDiary() {
		viewModelScope.launch {
			note.location = this@DiaryViewModel.location
			note.address = this@DiaryViewModel.address

			attachmentRepository.saveAttachmentList(diaryKey = note.primaryKey, attachmentList = attachmentList)
			diaryRepository.saveDiary(
				note = this@DiaryViewModel.note,
				attachmentList = attachmentList
			)
		}
	}

	private val fusedLocationClient: FusedLocationProviderClient = FusedLocationProviderClient(application.applicationContext)
	private val cancellationToken = CancellationTokenSource().token

	private val addressHandler = Handler(Looper.myLooper()!!)
	private val addressRunnable = kotlinx.coroutines.Runnable {
		if (diaryActivityState.addressState.value == DiaryActivity.AddressState.LOCATION) {
			Log.i("npr71", "show coordinate card")
		}
	}

	@SuppressLint("MissingPermission")
	fun getLocation() {
		fusedLocationClient.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, cancellationToken)
			.addOnSuccessListener { location: Location? ->
				Log.i("npr71", "location success...")
				this.gpsLocation = location
				this.location = location
				diaryActivityState.addressState.value = DiaryActivity.AddressState.LOCATION
				addressHandler.postDelayed(addressRunnable, 10000)

				if (location != null) {
					reverseGeocode(
						latitude = location.latitude,
						longitude = location.longitude,
						onAddressAvailable = { address ->
							viewModelScope.launch {
								withContext(Dispatchers.Main) {
									this@DiaryViewModel.address = locationAddressFilter(address = address)
									if (this@DiaryViewModel.address != null) {
										diaryActivityState.addressState.value = DiaryActivity.AddressState.SUCCESS
									}
								}
							}
						},
						onIoException = {
							Log.i("Diary Activity", "Reverse Geocode : IO Exception : Maybe network unavailable")
						},
						onException = {
							Log.e("Diary Activity", "Reverse Geocode : Exception")
						}
					)
				}

				if (location != null) {
					viewModelScope.launch {
						withContext(Dispatchers.IO) {
							val weatherRequestUrl =
								"https://api.openweathermap.org/data/2.5/weather?lat=${location.latitude}&lon=${location.latitude}&appid=${Secret.OPEN_WEATHER_KEY}"
							val weatherRequestQueue = Volley.newRequestQueue(getApplication())
							val stringRequest = StringRequest(
								Request.Method.GET,
								weatherRequestUrl,
								{ requestResult ->
									val jsonObject = JSONObject(requestResult)
									val weatherList = jsonObject.getJSONArray("weather")
									if (weatherList.length() > 0) {
										val weather = JSONObject(weatherList.get(0).toString())
										val main = jsonObject.getJSONObject("main")
										WeatherData(
											icon = weather.getString("icon"),
											description = weather.getString("description"),
											temperature = main.getDouble("temp")
										).apply { weatherData = this }
									}
								},
								{
								}
							)
							weatherRequestQueue.add(stringRequest)
						}
					}
				}
			}
			.addOnFailureListener {
				Log.i("npr71", "location failed...")
				diaryActivityState.addressState.value = DiaryActivity.AddressState.ERROR
			}
	}

	fun reverseGeocode(
		latitude: Double,
		longitude: Double,
		onAddressAvailable: (Address?) -> Unit,
		onIoException: () -> Unit,
		onException: () -> Unit
	) {
		viewModelScope.launch {
			withContext(Dispatchers.IO) {
				try {
					val geocoder = Geocoder(getApplication(), Locale.getDefault())
					val addressList = geocoder.getFromLocation(latitude, longitude, 1)
					if (addressList.isNotEmpty()) {
						val address = addressList.first()
						onAddressAvailable(address)
					} else {
						onAddressAvailable(null)
					}
				} catch (exception: IOException) {
					onIoException()
				} catch (exception: Exception) {
					onException()
				}
			}
		}
	}


}
