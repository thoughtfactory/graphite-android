package com.syncodec.graphite.noteComponent

import android.annotation.SuppressLint
import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.widget.Toast
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
import com.syncodec.graphite.database.tag.TagDbEntry
import com.syncodec.graphite.database.tag.TagKeyDbEntry
import com.syncodec.graphite.konstant.Status
import com.syncodec.graphite.miscellaneous.FileUtils.Companion.getFileExtension
import com.syncodec.graphite.miscellaneous.generatePrimaryKey
import com.syncodec.graphite.miscellaneous.getResizedBitmap
import com.syncodec.graphite.miscellaneous.locationAddressFilter
import com.syncodec.graphite.repository.AttachmentRepository
import com.syncodec.graphite.repository.NoteRepository
import com.syncodec.graphite.repository.TagRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
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

	lateinit var activityState: NoteActivity.ActivityState

	val noteKeyList: SnapshotStateList<String> = mutableStateListOf()

	val status: MutableState<Status> = mutableStateOf(Status.INIT)

	var isNew: Boolean? = null
	var viewerKey: MutableState<String?> = mutableStateOf(null)

	var showArchived: Boolean = false
	val showFavourite: Boolean = false
	var showLocked: Boolean = false

	lateinit var notebookKey: String
	lateinit var chapterPath: MutableList<String>
	val noteDbEntry = mutableStateOf<NoteDbEntry?>(null)
	val noteContent = mutableStateOf<JSONObject?>(null)
	val attachmentMap: SnapshotStateMap<String, Pair<AttachmentDbEntry, Uri?>> = mutableStateMapOf()
	val tagList: SnapshotStateList<TagDbEntry> = mutableStateListOf()
	val tagKeyList: SnapshotStateList<TagKeyDbEntry> = mutableStateListOf()
	val connectedTag: SnapshotStateList<String> = mutableStateListOf()

	var createdTimestamp: MutableState<Long> = mutableStateOf(0)
	var modifiedTimestamp: MutableState<Long> = mutableStateOf(0)
	var userTimestamp: MutableState<Long> = mutableStateOf(0)
	var title: MutableState<String?> = mutableStateOf(null)
	var latLng: MutableState<LatLng?> = mutableStateOf(null)
	var address: MutableState<String?> = mutableStateOf(null)
	var mood: MutableState<Int> = mutableStateOf(0)
	var isFavourite: MutableState<Boolean> = mutableStateOf(false)
	var isArchived: MutableState<Boolean> = mutableStateOf(false)
	var isLocked: MutableState<Boolean> = mutableStateOf(false)

	init {
		viewModelScope.launch(Dispatchers.IO) {
			tagRepository.tagList.collectLatest {
				tagList.clear()
				tagList.addAll(it)
			}
		}

		viewModelScope.launch(Dispatchers.IO) {
			tagRepository.tagKeyList.collectLatest {
				tagKeyList.clear()
				tagKeyList.addAll(it)
			}
		}
	}

	fun createNewNote(title: String?) {
		this.createdTimestamp.value = System.currentTimeMillis()
		this.modifiedTimestamp.value = this.createdTimestamp.value
		this.userTimestamp.value = this.createdTimestamp.value
		this.title.value = title
		NoteDbEntry(
			key = generatePrimaryKey(),
			timezone = TimeZone.getDefault().id,
			notebookKey = this.notebookKey,
			chapterPath = this.chapterPath,
		).apply {
			this@NoteViewModel.noteDbEntry.value = this
		}
	}

	fun insertAttachment(uri: Uri) {
		val key = generatePrimaryKey()
		val mimeType = getApplication<Graphite>().contentResolver.getType(uri)
		val extension = getApplication<Graphite>().getFileExtension(uri)

		noteDbEntry.value?.key?.let {
			AttachmentDbEntry(
				key = key,
				createdTimestamp = System.currentTimeMillis(),
				noteKey = it,
				mimeType = mimeType,
				extension = extension,
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
								data.second?.let { it1 ->
									getApplication<Graphite>().contentResolver.openInputStream(
										it1
									)
								}
							)
							return@forEach
						}
						"video" -> {
							return@forEach
						}
					}
				}

				noteDbEntry.value?.attachmentKeyList?.clear()
				noteDbEntry.value?.attachmentKeyList?.addAll(attachmentMap.keys.toMutableList())

				noteDbEntry.value?.createdTimestamp = createdTimestamp.value
				noteDbEntry.value?.modifiedTimestamp = modifiedTimestamp.value
				noteDbEntry.value?.userTimestamp = userTimestamp.value
				noteDbEntry.value?.title = title.value
				noteDbEntry.value?.latLng = latLng.value
				noteDbEntry.value?.address = address.value
				noteDbEntry.value?.mood = mood.value
				noteDbEntry.value?.isFavourite = isFavourite.value
				noteDbEntry.value?.isArchived = isArchived.value
				noteDbEntry.value?.isLocked = isLocked.value

				it.attachmentThumbnail = bitmap?.let { it1 -> getResizedBitmap(it1, 256) }

				noteRepository.putNote(noteDbEntry = it, noteContent = noteContent.value)
				tagRepository.connectTag(key = noteDbEntry.value!!.key, connectedTag)
				attachmentRepository.putAttachment(
					attachmentList = attachmentMap.values.toList(),
					noteKey = noteDbEntry.value!!.key
				)
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

				connectedTag.clear()
				tagKeyList.forEach { if (it.key == first?.key) connectedTag.add(it.tag) }

				noteDbEntry.value?.also {
					createdTimestamp.value = it.createdTimestamp
					modifiedTimestamp.value = it.modifiedTimestamp
					userTimestamp.value = it.userTimestamp
					title.value = it.title
					latLng.value = it.latLng
					address.value = it.address
					mood.value = it.mood
					isFavourite.value = it.isFavourite
					isArchived.value = it.isArchived
					isLocked.value = it.isLocked
				}

				status.value = Status.LOADED
			}
		}
	}

	fun loadAttachment(key: String) {
		viewModelScope.launch(Dispatchers.IO) {
			status.value = Status.LOADING
			attachmentRepository.getAttachment(noteKey = key).forEach {
				attachmentMap[it.key] =
					Pair(it, attachmentRepository.getAttachmentUri(it.key, it.extension))
			}
			status.value = Status.LOADED
		}
	}

	fun openNotebook() {
		viewModelScope.launch(Dispatchers.IO) {
			noteRepository.openNotebookChapterAsFlow(
				notebookKey = notebookKey,
				chapterPath = chapterPath,
				showArchived,
				showLocked
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

	@SuppressLint("MissingPermission")
	fun getLocation() {
		fusedLocationClient.getCurrentLocation(
			LocationRequest.PRIORITY_HIGH_ACCURACY, cancellationToken
		)
			.addOnSuccessListener { location: Location? ->
				if (location == null)
					this.latLng.value = null
				else
					this.latLng.value = LatLng(location.latitude, location.longitude)

				activityState.addressState.value = NoteActivity.AddressState.LOCATION

				if (location != null) {
					reverseGeocode(
						latitude = location.latitude,
						longitude = location.longitude,
						onAddressAvailable = { address ->
							viewModelScope.launch(Dispatchers.Main) {
								this@NoteViewModel.address.value =
									locationAddressFilter(address = address)
								if (this@NoteViewModel.address.value != null) activityState.addressState.value =
									NoteActivity.AddressState.SUCCESS
							}
						},
						onIoException = {
							CoroutineScope(Dispatchers.Main).launch {
								Toast.makeText(
									getApplication(),
									"Error getting location",
									Toast.LENGTH_SHORT
								).show()
							}
						},
						onException = {
							CoroutineScope(Dispatchers.Main).launch {
								Toast.makeText(
									getApplication(),
									"Error getting location",
									Toast.LENGTH_SHORT
								).show()
							}
						}
					)
				}
			}
			.addOnFailureListener {
				activityState.addressState.value = NoteActivity.AddressState.ERROR
			}
	}

	fun removeLocationData() {
		activityState.addressState.value = NoteActivity.AddressState.REMOVED
		latLng.value = null
		address.value = null
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
					val address = addressList.firstOrNull()
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

	fun printNote(htmlContent: String) =
		getApplication<Graphite>().printNote(
			htmlContent = htmlContent,
			key = noteDbEntry.value?.key,
			timestamp = noteDbEntry.value?.userTimestamp ?: 0,
			address = noteDbEntry.value?.address,
			latLng = noteDbEntry.value?.latLng
		)

	fun delete() {
		viewModelScope.launch(Dispatchers.IO) {
			if (noteDbEntry.value != null) {
				noteDbEntry.value?.attachmentKeyList?.let { attachmentRepository.delete(it) }
				noteDbEntry.value?.key?.let { noteRepository.deleteNote(keyList = listOf(it)) }
			}
		}
	}
}
