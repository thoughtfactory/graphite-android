package com.syncodec.graphite.noteComponent

import android.annotation.SuppressLint
import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationTokenSource
import com.syncodec.graphite.Graphite
import com.syncodec.graphite.database.attachment.AttachmentDbEntry
import com.syncodec.graphite.database.note.NoteDbEntry
import com.syncodec.graphite.konstant.Status
import com.syncodec.graphite.miscellaneous.generatePrimaryKey
import com.syncodec.graphite.miscellaneous.getResizedBitmap
import com.syncodec.graphite.miscellaneous.locationAddressFilter
import com.syncodec.graphite.miscellaneous.logger
import com.syncodec.graphite.repository.AttachmentRepository
import com.syncodec.graphite.repository.NoteRepository
import com.syncodec.graphite.repository.TagRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.IOException
import java.util.*


class NoteViewModel(application: Application) : AndroidViewModel(application) {

	private val noteRepository: NoteRepository =
		NoteRepository.getInstance(graphite = application as Graphite)
	private val attachmentRepository: AttachmentRepository =
		AttachmentRepository.getInstance(graphite = application as Graphite)
	private val tagRepository: TagRepository =
		TagRepository.getInstance(graphite = application as Graphite)

	val noteKeyList: SnapshotStateList<String> = mutableStateListOf()

	val status: MutableState<Status> = mutableStateOf(Status.INIT)

	var isNew: Boolean? = null
	var viewerKey: MutableState<String?> = mutableStateOf(null)

	lateinit var notebookKey: String
	lateinit var chapterPath: MutableList<String>
	val noteDbEntry = mutableStateOf<NoteDbEntry?>(null)
	val noteContent = mutableStateOf<JSONObject?>(null)
	val knotDbEntry = MutableStateFlow<NoteDbEntry?>(null)
	val attachmentMap: SnapshotStateMap<String, Pair<AttachmentDbEntry, Uri>> = mutableStateMapOf()
	val tagList = tagRepository.tagList
	val connectedTag: SnapshotStateList<String> = mutableStateListOf()

	lateinit var activityState: NoteActivity.ActivityState

	fun createNewNote(title: String?) {
		NoteDbEntry(
			key = generatePrimaryKey(),
			timezone = TimeZone.getDefault().id,
			notebookKey = this.notebookKey,
			chapterPath = this.chapterPath,
		).apply {
			this.createdTimestamp = System.currentTimeMillis()
			this.modifiedTimestamp = this.createdTimestamp
			this.userTimestamp = this.createdTimestamp
			this.title = title

			this@NoteViewModel.noteDbEntry.value = this
			emitNote()
		}
	}

	fun emitNote() = viewModelScope.launch(Dispatchers.IO) { knotDbEntry.emit(noteDbEntry.value) }

	fun insertAttachment(uri: Uri) {
		val key = generatePrimaryKey()
		val mimeType = getApplication<Graphite>().contentResolver.getType(uri)

		noteDbEntry.value?.key?.let {
			AttachmentDbEntry(
				key = key,
				createdTimestamp = System.currentTimeMillis(),
				noteKey = it,
				mimeType = mimeType,
				chapterPath = chapterPath,
				notebookKey = notebookKey
			).apply { attachmentMap[key] = Pair(this, uri) }
		}
	}

	fun putNote() {
		viewModelScope.launch(Dispatchers.IO) {
			this@NoteViewModel.noteDbEntry.value?.let {
				it.modifiedTimestamp = System.currentTimeMillis()
				var bitmap: Bitmap? = null
				attachmentMap.forEach { (_, data) ->
					when (data.first.mimeType?.split("/")?.first()) {
						"image" -> {
							bitmap = BitmapFactory.decodeStream(
								getApplication<Graphite>().contentResolver.openInputStream(data.second)
							)
							return@forEach
						}
						"video" -> {
							return@forEach
						}
					}
				}

				noteDbEntry.value!!.attachmentKeyList.clear()
				noteDbEntry.value!!.attachmentKeyList.addAll(attachmentMap.keys.toMutableList())

				it.attachmentThumbnail = bitmap?.let { it1 -> getResizedBitmap(it1, 256) }

				noteRepository.putNote(noteDbEntry = it, noteContent = noteContent.value)
				tagRepository.connectTag(key = noteDbEntry.value!!.key, connectedTag)
				attachmentRepository.putAttachment(attachmentList = attachmentMap.values.toList(), noteKey = noteDbEntry.value!!.key)
				activityState.isSaving.value = false
				activityState.isSaved.value = true
			}
		}
	}

	fun loadNote(key: String) {
		viewModelScope.launch(Dispatchers.IO) {
			status.value = Status.LOADING
			noteRepository.getNote(key = key).apply {
				noteDbEntry.value = first
				noteContent.value = second
				emitNote()
				status.value = Status.LOADED
			}
		}
	}

	fun loadAttachment(key: String) {
		viewModelScope.launch(Dispatchers.IO) {
			status.value = Status.LOADING
			attachmentRepository.getAttachment(noteKey = key).forEach {
				logger("key : ${it.key}")
				attachmentMap[it.key] = Pair(it, attachmentRepository.getAttachmentUri(it.key))
			}
			status.value = Status.LOADED
		}
	}

	fun openNotebook() {
		viewModelScope.launch(Dispatchers.IO) {
			noteRepository.openNotebookChapterAsFlow(
				notebookKey = notebookKey,
				chapterPath = chapterPath
			).collect {
				noteKeyList.clear()
				noteKeyList.addAll(it)
			}
		}
	}

	fun addTag(tag: String) =
		viewModelScope.launch(Dispatchers.IO) { tagRepository.putTag(tag = tag) }

	fun connectTag(tag: String) =
		if (tag in connectedTag) connectedTag.remove(tag) else connectedTag.add(tag)


	private val fusedLocationClient: FusedLocationProviderClient =
		FusedLocationProviderClient(application.applicationContext)
	private val cancellationToken = CancellationTokenSource().token

	private val addressHandler = Handler(Looper.myLooper()!!)
	private val addressRunnable = kotlinx.coroutines.Runnable {
		if (activityState.addressState.value == NoteActivity.AddressState.LOCATION) {
			Log.i("npr71", "show coordinate card")
		}
	}

	@SuppressLint("MissingPermission")
	fun getLocation() {
		fusedLocationClient.getCurrentLocation(
			LocationRequest.PRIORITY_HIGH_ACCURACY, cancellationToken
		)
			.addOnSuccessListener { location: Location? ->
				if (location == null)
					this.knotDbEntry.value?.latLng = null
				else
					this.knotDbEntry.value?.latLng = LatLng(location.latitude, location.longitude)

				activityState.addressState.value = NoteActivity.AddressState.LOCATION
				addressHandler.postDelayed(addressRunnable, 10000)

				if (location != null) {
					reverseGeocode(
						latitude = location.latitude,
						longitude = location.longitude,
						onAddressAvailable = { address ->
							viewModelScope.launch(Dispatchers.Main) {
								this@NoteViewModel.knotDbEntry.value?.address =
									locationAddressFilter(address = address)
								if (this@NoteViewModel.knotDbEntry.value?.address != null) activityState.addressState.value =
									NoteActivity.AddressState.SUCCESS
							}
						},
						onIoException = {
							Log.i(
								"Diary Activity",
								"Reverse Geocode : IO Exception : Maybe network unavailable"
							)
						},
						onException = {
							Log.e("Diary Activity", "Reverse Geocode : Exception")
						}
					)
				}
			}
			.addOnFailureListener {
				Log.i("npr71", "location failed...")
				activityState.addressState.value = NoteActivity.AddressState.ERROR
			}
	}

	fun removeLocationData() {
		activityState.addressState.value = NoteActivity.AddressState.REMOVED
		knotDbEntry.value?.latLng = null
		knotDbEntry.value?.address = null
	}

	fun reverseGeocode(
		latitude: Double,
		longitude: Double,
		onAddressAvailable: (Address?) -> Unit,
		onIoException: () -> Unit,
		onException: () -> Unit
	) {
		viewModelScope.launch(Dispatchers.IO) {
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
