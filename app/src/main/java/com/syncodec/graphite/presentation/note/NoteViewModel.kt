package com.syncodec.graphite.presentation.note

import android.Manifest
import android.app.Application
import android.content.ContentResolver
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Build
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.syncodec.graphite.BaseApplication
import com.syncodec.graphite.di.Repository
import com.syncodec.graphite.di.Repository.getAttachmentFile
import com.syncodec.graphite.di.model.AttachmentObject
import com.syncodec.graphite.di.model.ChapterObject
import com.syncodec.graphite.di.model.LatLng
import com.syncodec.graphite.di.model.NoteObject
import com.syncodec.graphite.utils.*
import io.realm.kotlin.types.ObjectId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.util.*


class NoteViewModel(application: Application) : AndroidViewModel(application) {

	var isNew: MutableState<Boolean?> = mutableStateOf(null)
	var isViewer: MutableState<Boolean> = mutableStateOf(true)
	var isSaving: MutableState<Boolean> = mutableStateOf(false)

	val showDeleteDialog: MutableState<Boolean> = mutableStateOf(false)
	var showDiscardDialog: MutableState<Boolean> = mutableStateOf(false)

	val status: MutableState<Status> = mutableStateOf(Status.INIT)

	val chapterObject: MutableState<ChapterObject?> = mutableStateOf(null)
	val noteIdList: SnapshotStateList<ObjectId> = mutableStateListOf()

	var noteId: MutableState<ObjectId?> = mutableStateOf(null)
	var parentChapterId: MutableState<ObjectId?> = mutableStateOf(null)
	val createdTimestamp: MutableState<Long?> = mutableStateOf(null)
	val modifiedTimestamp: MutableState<Long?> = mutableStateOf(null)
	val userTimestamp: MutableState<Long?> = mutableStateOf(null)
	val title: MutableState<String?> = mutableStateOf(null)
	val color: MutableState<Int?> = mutableStateOf(null)
	val latLng: MutableState<LatLng?> = mutableStateOf(null)
	val address: MutableState<String?> = mutableStateOf(null)
	val contentThumbnail: MutableState<String?> = mutableStateOf(null)
	val content: MutableState<String?> = mutableStateOf(null)

	//  Map<UId, Triple<Uri, File, AttachmentObject>>
	val attachmentListStored: SnapshotStateMap<ObjectId, Triple<Uri, File, AttachmentObject>> = mutableStateMapOf()
	val attachmentListVisible: SnapshotStateMap<ObjectId, Triple<Uri, File, AttachmentObject>> = mutableStateMapOf()
	val isFavourite: MutableState<Boolean> = mutableStateOf(false)
	val isLocked: MutableState<Boolean> = mutableStateOf(false)

	var isUserScrollEnabled: MutableState<Boolean> = mutableStateOf(false)
	val locationSnackbarHostState = SnackbarHostState()

	val locationState: MutableState<LocationState> = mutableStateOf(LocationState.INIT)

	fun initNewData(chapterId: ObjectId, filter: Extra.Companion.Filter) {

		viewModelScope.launch(Dispatchers.IO) {
			Repository.getChapterAsFlow(chapterId).collectLatest {
				chapterObject.value = it
			}
		}

//		TODO Is this use of timestamp current
		this.createdTimestamp.value = System.currentTimeMillis()
		this.modifiedTimestamp.value = System.currentTimeMillis()
		this.userTimestamp.value = System.currentTimeMillis()
		this.parentChapterId.value = chapterId

		getLocation()

		isNew.value = true
		isViewer.value = false
	}

	fun loadAndViewData(chapterUId: ObjectId, noteId: ObjectId, filter: Extra.Companion.Filter) {
		isNew.value = false
		when (filter) {
			Extra.Companion.Filter.READ_CHAPTER -> {
				viewModelScope.launch(Dispatchers.IO) {
					isUserScrollEnabled.value = true

					Repository
						.getChapterAsFlow(id = chapterUId)
						.map {
							viewModelScope.launch(Dispatchers.Main) {
								chapterObject.value = it
							}
							it?.noteList?.sortedBy { -it.userTimestamp }?.map { it.id }
						}
						.collect {
							viewModelScope.launch(Dispatchers.Main) {
								noteIdList.clear()
								it?.let { noteIdList.addAll(it) }
							}
						}
				}
			}
		}
		this.noteId.value = noteId
		isViewer.value = true
	}

	fun bufferAttachment(uriList: List<Uri>) {
		uriList.forEach { uri ->
			var extension: String? = null
			val name = getApplication<BaseApplication>().getFileName(uri)
			try {
				extension =
					if (uri.scheme.equals(ContentResolver.SCHEME_CONTENT))
						MimeTypeMap.getSingleton().getExtensionFromMimeType(getApplication<BaseApplication>().applicationContext.contentResolver.getType(uri))
					else
						MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(uri.path?.let { File(it) }).toString())
			} catch (e: Exception) {
//		    	TODO Show error message
				e.printStackTrace()
			} finally {
				AttachmentObject().apply {
					this.name = name ?: this.id.toString()
					this.extension = extension
					this.mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)

					val uriAndFile = createTempFileToExpose(getApplication(), this.id.toString(), this.extension)
					val inputStream = getApplication<BaseApplication>().contentResolver.openInputStream(uri)
					val outputStream = getApplication<BaseApplication>().contentResolver.openOutputStream(uriAndFile.first)

					if (inputStream != null && outputStream != null) copyInputStreamToOutputStream(inputStream, outputStream)

					inputStream?.close()
					outputStream?.close()

					attachmentListVisible[this.id] = Triple(uriAndFile.first, uriAndFile.second, this)
				}
			}
		}
	}

	fun putNote(data: String) {
		val dataObject = JSONObject(data)
		val dataJson = dataObject.getJSONObject("dataJson")
		val dataText = dataObject.getString("dataText")

		if (this.createdTimestamp.value == null ||
			this.modifiedTimestamp.value == null ||
			this.userTimestamp.value == null ||
			this.chapterObject.value == null
		) {
//			TODO    Show msg
		} else {
			CoroutineScope(Dispatchers.IO).launch {
				NoteObject().apply {
					if (noteId.value != null) this.id = noteId.value!!
					this.createdTimestamp = this@NoteViewModel.createdTimestamp.value!!
					this.modifiedTimestamp = this@NoteViewModel.modifiedTimestamp.value!!
					this.userTimestamp = this@NoteViewModel.userTimestamp.value!!
					this.title = this@NoteViewModel.title.value
					this.color = this@NoteViewModel.color.value
					this.latLng =
						if (this@NoteViewModel.latLng.value != null) LatLng().apply {
							this.latitude = this@NoteViewModel.latLng.value?.latitude
							this.longitude = this@NoteViewModel.latLng.value?.longitude
						}
						else null
					this.address = this@NoteViewModel.address.value
					this.contentThumbnail = dataText.substring(0, minOf(256, dataText.length))
					this.content = dataJson.toString()
					this.isFavourite = this@NoteViewModel.isFavourite.value
					this.isLocked = this@NoteViewModel.isLocked.value

					this.attachmentList.clear()
					this@NoteViewModel.attachmentListVisible.forEach { (id, data) ->
						if (!data.third.isSaved) {
							saveAttachment(id, data.second, data.third.extension)
						}
						this.attachmentList.add(data.third)

						if (this.thumbnail == null || this.thumbnailType == null) {
							if (data.third.getType() == AttachmentObject.Companion.Type.IMAGE) {
								BitmapFactory.decodeFile(data.second.absolutePath)?.let {
									this.thumbnail = Base64.getEncoder().encodeToString(it.toByteArray())
									this.thumbnailType = data.third.getType().name
								}
							}
						}
					}

					if (chapterObject.value != null) {
						Repository.putNote(chapterObject.value!!.id, this) {
							loadAndViewData(chapterObject.value!!.id, this.id, Extra.Companion.Filter.READ_CHAPTER)
						}
					}

					this@NoteViewModel.noteId.value = this.id
					isViewer.value = true
				}
			}
		}
	}

	private fun saveAttachment(id: ObjectId, second: File, extension: String?) {
		val file = getApplication<BaseApplication>().getAttachmentFile(id, extension)

		if (file != null) {
			try {
				val inputStream = second.inputStream()
				val outputStream = file.outputStream()
				copyInputStreamToOutputStream(inputStream, outputStream)
			} catch (e: Exception) {
				e.printStackTrace()
//				TODO Show error message
			}
		}
	}

	fun editNote() {
		isViewer.value = false
	}

	fun updateNote(data: String? = null, action: () -> Unit) {
		viewModelScope.launch(Dispatchers.IO) {
			isUserScrollEnabled.value = false
			action.invoke()
			try {
//				Only update if the entry already exists
				if (this@NoteViewModel.noteId.value != null) {
					NoteObject().apply {
						if (noteId.value != null) {
							this.id = noteId.value!!

							this.createdTimestamp = this@NoteViewModel.createdTimestamp.value!!
							this.modifiedTimestamp = this@NoteViewModel.modifiedTimestamp.value!!
							this.userTimestamp = this@NoteViewModel.userTimestamp.value!!
							this.title = this@NoteViewModel.title.value
							this.color = this@NoteViewModel.color.value
							this.latLng =
								if (this@NoteViewModel.latLng.value != null) LatLng().apply {
									this.latitude = this@NoteViewModel.latLng.value?.latitude
									this.longitude = this@NoteViewModel.latLng.value?.longitude
								}
								else null
							this.address = this@NoteViewModel.address.value

							if (data != null) {
								try {
									val dataObject = JSONObject(data)
									val dataJson = dataObject.getJSONObject("dataJson")
									val dataText = dataObject.getString("dataText")
									this.contentThumbnail = dataText.substring(0, minOf(256, dataText.length))
									this.content = dataJson.toString()
								} catch (e: Exception) {
									e.printStackTrace()
								}
							} else {
								this.contentThumbnail = this@NoteViewModel.contentThumbnail.value
								this.content = this@NoteViewModel.content.value
							}
							this.attachmentList.clear()
							this@NoteViewModel.attachmentListVisible.forEach { (id, data) ->
								if (!data.third.isSaved) {
									saveAttachment(id, data.second, data.third.extension)
								}
								this.attachmentList.add(data.third)
							}
							this.isFavourite = this@NoteViewModel.isFavourite.value
							this.isLocked = this@NoteViewModel.isLocked.value

							if (chapterObject.value != null) {
								Repository.putNote(chapterObject.value!!.id, this) {
									loadAndViewData(chapterObject.value!!.id, this.id, Extra.Companion.Filter.READ_CHAPTER)
								}
							}

//  				    	this@NoteViewModel.noteId.value = id
							isViewer.value = true
						} else {
//							TODO Show error message
							isViewer.value = true
						}
					}
				}

				isUserScrollEnabled.value = true
			} catch (e: Exception) {
//				TODO Show error message
				isUserScrollEnabled.value = true
				e.printStackTrace()
			}
		}
	}

	fun getNote(id: ObjectId) {
		noteId.value = id
		val noteObject = Repository.getNote(id = id)
		if (noteObject == null || noteObject.id != this@NoteViewModel.noteId.value) {
//			TODO Show error message
		} else {
			this.parentChapterId.value = noteObject.parentChapterId
			this.createdTimestamp.value = noteObject.createdTimestamp
			this.modifiedTimestamp.value = noteObject.modifiedTimestamp
			this.userTimestamp.value = noteObject.userTimestamp
			this.title.value = noteObject.title
			this.color.value = noteObject.color
			this.latLng.value = noteObject.latLng
			this.address.value = noteObject.address
			this.contentThumbnail.value = noteObject.contentThumbnail
			this.content.value = noteObject.content
			this.attachmentListVisible.clear()
			noteObject.attachmentList.forEach { attachmentObject ->
				val file = getApplication<BaseApplication>().getAttachmentFile(attachmentObject.id, attachmentObject.extension)

				file?.let { it1 ->
					val uri = FileProvider.getUriForFile(
						getApplication<BaseApplication>(),
						"com.syncodec.fileprovider",
						it1
					)
					attachmentListVisible[attachmentObject.id] = Triple(uri, file, attachmentObject.clone().apply { this.isSaved = true })
				}
			}

			this.isFavourite.value = noteObject.isFavourite
			this.isLocked.value = noteObject.isLocked
		}
	}

	fun deleteNote() {
		noteId.value?.let { Repository.deleteNote(it) }
		showDeleteDialog.value = false
	}

	private val fusedLocationClient: FusedLocationProviderClient = FusedLocationProviderClient(application.applicationContext)
	private val cancellationToken = CancellationTokenSource().token

	fun getLocation() {
		locationState.value = LocationState.INIT

		viewModelScope.launch(Dispatchers.IO) {

			if (getApplication<BaseApplication>().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
				getApplication<BaseApplication>().checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
			) {
				withContext(Dispatchers.Main) {
					locationState.value = LocationState.NO_PERMISSION
				}
				// TODO: Consider calling
				//    ActivityCompat#requestPermissions
				// here to request the missing permissions, and then overriding
				//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
				//                                          int[] grantResults)
				// to handle the case where the user grants the permission. See the documentation
				// for ActivityCompat#requestPermissions for more details.
				return@launch
			}
			fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cancellationToken)
				.addOnSuccessListener { location: Location? ->
					if (location == null) {
						this@NoteViewModel.latLng.value = null
						locationState.value = LocationState.ERROR
						Toast.makeText(getApplication(), "Error getting location", Toast.LENGTH_SHORT).show()
					} else {
						this@NoteViewModel.latLng.value = LatLng(location.latitude, location.longitude)
						locationState.value = LocationState.LATLNG
					}

//					activityState.addressState.value = NoteActivity.AddressState.LOCATION

					if (location != null) {
						reverseGeocode(
							latitude = location.latitude,
							longitude = location.longitude,
							onAddressAvailable = { address ->
								viewModelScope.launch(Dispatchers.Main) {
									this@NoteViewModel.address.value = locationAddressFilter(address = address)

									if (this@NoteViewModel.address.value != null) {
										locationSnackbarHostState.showSnackbar(
											message = this@NoteViewModel.address.value ?: "Lat : ${latLng.value?.latitude}\nLng : ${latLng.value?.longitude}",
											duration = SnackbarDuration.Short
										)
										locationState.value = LocationState.ADDRESS
									} else {
										locationSnackbarHostState.showSnackbar(
											message = "Lat : ${latLng.value?.latitude}\nLng : ${latLng.value?.longitude}",
											duration = SnackbarDuration.Short
										)
										locationState.value = LocationState.LATLNG_NO_ADDRESS
									}
								}
							},
							onIoException = {
								CoroutineScope(Dispatchers.Main).launch {
									locationSnackbarHostState.showSnackbar(
										message = "Lat : ${latLng.value?.latitude}\nLng : ${latLng.value?.longitude}",
										duration = SnackbarDuration.Short
									)
									locationState.value = LocationState.LATLNG_NO_ADDRESS
								}
							},
							onException = {
								CoroutineScope(Dispatchers.Main).launch {
									locationSnackbarHostState.showSnackbar(
										message = "Lat : ${latLng.value?.latitude}\nLng : ${latLng.value?.longitude}",
										duration = SnackbarDuration.Short
									)
									locationState.value = LocationState.LATLNG_NO_ADDRESS
								}
							}
						)
					}
				}
				.addOnFailureListener {
//				activityState.addressState.value = NoteActivity.AddressState.ERROR
					viewModelScope.launch(Dispatchers.Main) {
						locationSnackbarHostState.showSnackbar(
							message = "Error getting location",
							duration = SnackbarDuration.Short
						)
					}
				}
		}
	}


	fun removeLocation() {
		latLng.value = null
		address.value = null
		locationState.value = LocationState.REMOVED
	}

	private fun reverseGeocode(
		latitude: Double,
		longitude: Double,
		onAddressAvailable: (Address?) -> Unit,
		onIoException: () -> Unit,
		onException: () -> Unit
	) {
		viewModelScope.launch(Dispatchers.IO) {
			try {
				val geocoder = Geocoder(getApplication(), Locale.getDefault())
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
					geocoder.getFromLocation(latitude, longitude, 1) { addresses ->
						onAddressAvailable(addresses.getOrNull(0))
					}
				} else {
					val addresses = geocoder.getFromLocation(latitude, longitude, 1)
					onAddressAvailable(addresses?.firstOrNull())
				}

			} catch (exception: IOException) {
				onIoException()
			} catch (exception: Exception) {
				onException()
			}
		}
	}

	fun onUpdateFavorite() {
		if (isViewer.value) {
			updateNote { isFavourite.value = !isFavourite.value }
		} else {
			isFavourite.value = !isFavourite.value
		}
	}

	fun onUpdateLock() {
		if (isViewer.value) {
			updateNote { isLocked.value = !isLocked.value }
		} else {
			isLocked.value = !isLocked.value
		}
	}

	fun updateTitle(title: String?) {
		if (isViewer.value) {
			updateNote { this.title.value = title }
		} else {
			this.title.value = title
		}
	}
}
