package com.syncodec.momento.noteComponent

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
import com.syncodec.momento.database.diary.LocationData
import com.syncodec.momento.database.diary.Note
import com.syncodec.momento.database.diary.WeatherData
import com.syncodec.momento.konstant.Secret
import com.syncodec.momento.konstant.Status
import com.syncodec.momento.miscellaneous.copyInputStreamToOutputStream
import com.syncodec.momento.miscellaneous.generatePrimaryKey
import com.syncodec.momento.miscellaneous.locationAddressFilter
import com.syncodec.momento.repository.AttachmentRepository
import com.syncodec.momento.repository.DiaryRepository
import com.syncodec.momento.repository.NotebookRepository
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

class NoteViewModel(application: Application) : AndroidViewModel(application) {

	val diaryRepository: DiaryRepository = DiaryRepository(momento = application as Momento)
	private val attachmentRepository: AttachmentRepository = AttachmentRepository(momento = application as Momento)
	private val notebookRepository: NotebookRepository = NotebookRepository(momento = application as Momento)

	lateinit var componentType: Momento.Companion.ComponentType

	val status: MutableState<Status> = mutableStateOf(Status.INIT)

	var isViewer: Boolean = true
	var viewerDiaryKey: String? = null

	private val currentTimestamp = System.currentTimeMillis()
	var userTimestamp = mutableStateOf(System.currentTimeMillis())

	var gpsLocation by mutableStateOf<Location?>(null)
	var location by mutableStateOf<Location?>(null)
	var address by mutableStateOf<String?>(null)
	var weatherData by mutableStateOf<WeatherData?>(null)

	var isArchived by mutableStateOf(false)
	var isFavourite by mutableStateOf(false)
	var isLocked by mutableStateOf(false)
	var deletedTimestamp by mutableStateOf(-1L)

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

	lateinit var activityState: NoteActivity.ActivityState

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
		activityState.addressState.value = NoteActivity.AddressState.REMOVED
		location = null
		address = null
	}

	fun saveDiary() {
		viewModelScope.launch {
			note.userTimestamp = userTimestamp.value
			note.location = LocationData(
				latitude = this@NoteViewModel.location?.latitude,
				longitude = this@NoteViewModel.location?.longitude,
				bearing = this@NoteViewModel.location?.bearing,
				altitude = this@NoteViewModel.location?.altitude,
				speed = this@NoteViewModel.location?.speed
			)
			note.address = this@NoteViewModel.address

			note.isArchived = isArchived
			note.isFavourite = isFavourite
			note.isLocked = isLocked

			attachmentRepository.saveAttachmentList(diaryKey = note.primaryKey, attachmentList = attachmentList)
			diaryRepository.saveDiary(
				note = this@NoteViewModel.note,
				attachmentList = attachmentList,
				deletedTimestamp = deletedTimestamp
			)
		}
	}

	fun saveNote() {
		viewModelScope.launch {
			notebookRepository.saveNote(
				note = this@NoteViewModel.note,
				attachmentList = this@NoteViewModel.attachmentList,
				deletedTimestamp = this@NoteViewModel.deletedTimestamp
			)
		}
	}

	private val fusedLocationClient: FusedLocationProviderClient = FusedLocationProviderClient(application.applicationContext)
	private val cancellationToken = CancellationTokenSource().token

	private val addressHandler = Handler(Looper.myLooper()!!)
	private val addressRunnable = kotlinx.coroutines.Runnable {
		if (activityState.addressState.value == NoteActivity.AddressState.LOCATION) {
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
				activityState.addressState.value = NoteActivity.AddressState.LOCATION
				addressHandler.postDelayed(addressRunnable, 10000)

				if (location != null) {
					reverseGeocode(
						latitude = location.latitude,
						longitude = location.longitude,
						onAddressAvailable = { address ->
							viewModelScope.launch {
								withContext(Dispatchers.Main) {
									this@NoteViewModel.address = locationAddressFilter(address = address)
									if (this@NoteViewModel.address != null) {
										activityState.addressState.value = NoteActivity.AddressState.SUCCESS
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
				activityState.addressState.value = NoteActivity.AddressState.ERROR
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
