package com.syncodec.graphite.presentation.note

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Address
import android.media.ThumbnailUtils
import android.net.Uri
import android.widget.Toast
import androidx.annotation.MainThread
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.syncodec.graphite.di.model.AttachmentObject
import com.syncodec.graphite.di.model.LatLng
import com.syncodec.graphite.di.model.NoteObject
import com.syncodec.graphite.di.repository.RealmNotInitializedException
import com.syncodec.graphite.di.repository.Repository2
import com.syncodec.graphite.di.repository.RepositoryState
import com.syncodec.graphite.presentation.note.util.reverseGeocode
import com.syncodec.graphite.utils.DataStoreInstance
import com.syncodec.graphite.utils.LocationState
import com.syncodec.graphite.utils.copyInputStreamToOutputStream
import com.syncodec.graphite.utils.locationAddressFilter
import com.syncodec.graphite.utils.toByteArray
import dagger.hilt.android.lifecycle.HiltViewModel
import io.realm.kotlin.types.ObjectId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.util.Base64
import javax.inject.Inject


@HiltViewModel
class NoteViewModel @Inject constructor(private val repository2 : Repository2) : ViewModel() {

	val repositoryState = repository2.repositoryState

	val isNew : MutableState<Boolean?> = mutableStateOf(null)
	val isViewing : MutableState<Boolean?> = mutableStateOf(null)
	val isOperationPending : MutableState<Boolean> = mutableStateOf(false)
	val locationSnackbarHostState = SnackbarHostState()

	val noteIdList : SnapshotStateList<ObjectId> = mutableStateListOf()
	val noteId : MutableState<ObjectId?> = mutableStateOf(null)
	val noteObject : MutableState<NoteObject?> = mutableStateOf(null)
	val parentChapterId : MutableState<ObjectId?> = mutableStateOf(null)

	val createdTimestamp : MutableState<Long?> = mutableStateOf(null)
	val modifiedTimestamp : MutableState<Long?> = mutableStateOf(null)
	val userTimestamp : MutableState<Long?> = mutableStateOf(null)
	val contentThumbnail : MutableState<String?> = mutableStateOf(null)
	val content : MutableState<String?> = mutableStateOf(null)
	val title : MutableState<String?> = mutableStateOf(null)
	val color : MutableState<Int?> = mutableStateOf(null)
	val latLng : MutableState<LatLng?> = mutableStateOf(null)
	val address : MutableState<String?> = mutableStateOf(null)
	val isFavourite : MutableState<Boolean?> = mutableStateOf(null)
	val isLocked : MutableState<Boolean?> = mutableStateOf(null)

	val attachmentListStored : SnapshotStateMap<ObjectId, Triple<AttachmentObject, File?, Uri?>> = mutableStateMapOf()
	val attachmentListBuffer : SnapshotStateMap<ObjectId, Triple<AttachmentObject, File?, Uri?>> = mutableStateMapOf()

	val locationState : MutableState<LocationState> = mutableStateOf(LocationState.INIT)
	var locationCoroutine : CoroutineScope? = null
	var locationCancellationSource : CancellationTokenSource? = null

	val showLocationPermissionDialog : MutableState<Boolean> = mutableStateOf(false)
	val showLocationPickerDialog : MutableState<Boolean> = mutableStateOf(false)
	val showNotificationPermissionDialog : MutableState<Boolean> = mutableStateOf(false)
	val showDiscardDialog : MutableState<Boolean> = mutableStateOf(false)
	val showDeleteDialog : MutableState<Boolean> = mutableStateOf(false)


	fun singleRead(noteId : ObjectId) {
		viewModelScope.launch(Dispatchers.IO) {
			noteIdList.add(noteId)
			getNote(id = noteId)
		}
	}

	fun chapterRead(chapterId : ObjectId, noteId : ObjectId) {
		viewModelScope.launch(Dispatchers.IO) {
			when (repositoryState.value) {
				RepositoryState.INIT -> null
				RepositoryState.LOADING -> null
				RepositoryState.SUCCESS -> {
					viewModelScope.launch(Dispatchers.IO) {
						if (repositoryState.value != RepositoryState.SUCCESS) this.cancel()
						try {
							repository2.getChapterFromIdAsFlow(chapterId).collect {
								withContext(Dispatchers.Main) {
									noteIdList.clear()
									noteIdList.addAll(it?.noteList?.map { it.id } ?: listOf())

									getNote(id = noteId)
								}
							}
						} catch (e : RealmNotInitializedException) {
						} catch (e : Exception) {
						}
					}
				}

				RepositoryState.ERROR -> null
			}
		}

	}

	fun chapterReadNew(chapterId : ObjectId) {
		viewModelScope.launch(Dispatchers.IO) {
			when (repositoryState.value) {
				RepositoryState.INIT -> null
				RepositoryState.LOADING -> null
				RepositoryState.SUCCESS -> {
					viewModelScope.launch(Dispatchers.IO) {
						if (repositoryState.value != RepositoryState.SUCCESS) this.cancel()
						try {
							repository2.getChapterFromIdAsFlow(chapterId).collect {
								withContext(Dispatchers.Main) {
									noteIdList.clear()
									noteIdList.addAll(it?.noteList?.map { it.id } ?: listOf())
									initNewNote()
									isNew.value = true
									isViewing.value = false
									this@NoteViewModel.parentChapterId.value = chapterId
								}
							}
						} catch (e : RealmNotInitializedException) {
						} catch (e : Exception) {
						}
					}
				}

				RepositoryState.ERROR -> null
			}
		}
	}

	@MainThread
	fun initNewNote() {
		createdTimestamp.value = System.currentTimeMillis()
		modifiedTimestamp.value = System.currentTimeMillis()
		userTimestamp.value = System.currentTimeMillis()
		contentThumbnail.value = null
		content.value = null
		title.value = null
		color.value = null
		latLng.value = null
		address.value = null
		isFavourite.value = false
		isLocked.value = false

		attachmentListStored.clear()
		attachmentListBuffer.clear()

		locationState.value = LocationState.INIT
		getLocation()
	}

	fun getNote(id : ObjectId) {
		viewModelScope.launch(Dispatchers.IO) {
//			val noteObject = repository2.getNoteFromId(id)

			repository2.getNoteFromIdAsFlow(id).cancellable().collect { noteObject ->
				this@NoteViewModel.noteId.value = noteObject?.id
				if (noteObject?.id != this@NoteViewModel.noteId.value) cancel()
				if (noteObject != null) {
					withContext(Dispatchers.Main) {
//						this@NoteViewModel.noteId.value = noteObject.id

						this@NoteViewModel.createdTimestamp.value = noteObject.createdTimestamp
						this@NoteViewModel.modifiedTimestamp.value = noteObject.modifiedTimestamp
						this@NoteViewModel.userTimestamp.value = noteObject.userTimestamp
						this@NoteViewModel.contentThumbnail.value = noteObject.contentThumbnail
						this@NoteViewModel.content.value = noteObject.content
						this@NoteViewModel.title.value = noteObject.title
						this@NoteViewModel.color.value = noteObject.color
						this@NoteViewModel.latLng.value = noteObject.getLatLng()
						this@NoteViewModel.address.value = noteObject.address
						this@NoteViewModel.isFavourite.value = noteObject.isFavourite
						this@NoteViewModel.isLocked.value = noteObject.isLocked

						this@NoteViewModel.parentChapterId.value = noteObject.parentChapterId

						attachmentListStored.clear()
						attachmentListBuffer.clear()
						noteObject.attachmentList.forEach {
							val attachmentObject = repository2.getAttachmentFromId(it.id)
							if (attachmentObject != null) {
								val file = repository2.getAttachmentFile(attachmentObject.id, attachmentObject.extension)
								val uri =
									file?.let { it1 -> FileProvider.getUriForFile(repository2.context, "${repository2.context.packageName}.fileprovider", it1) }
								attachmentListStored[attachmentObject.id] = Triple(attachmentObject, file, uri)
								attachmentListBuffer[attachmentObject.id] = Triple(attachmentObject, file, uri)
							}
						}

						when {
							this@NoteViewModel.latLng.value != null && this@NoteViewModel.address.value != null -> locationState.value = LocationState.SUCCESS
							this@NoteViewModel.latLng.value != null && this@NoteViewModel.address.value == null -> locationState.value =
								LocationState.ONLY_LATLNG

							this@NoteViewModel.latLng.value == null && this@NoteViewModel.address.value != null -> locationState.value =
								LocationState.ONLY_ADDRESS

							else -> locationState.value = LocationState.REMOVED
						}
					}
				}

				isNew.value = false
				isViewing.value = true
			}
		}
	}

	fun editNote() {
		viewModelScope.launch(Dispatchers.IO) {
			this@NoteViewModel.isViewing.value = false
		}
	}

	fun putNote(data : String?) {
		CoroutineScope(Dispatchers.IO).launch {
			try {
				locationCancellationSource?.cancel()
				locationCancellationSource = null

				val dataObject = JSONObject(data ?: "{}")
				val dataJson = dataObject.optJSONObject("dataJson")
				val dataText = dataObject.optString("dataText")

				NoteObject().apply {
					isOperationPending.value = true

					if (this@NoteViewModel.noteId.value != null) this.id = this@NoteViewModel.noteId.value !!
					this.createdTimestamp = this@NoteViewModel.createdTimestamp.value ?: System.currentTimeMillis()
					this.modifiedTimestamp = this@NoteViewModel.modifiedTimestamp.value ?: System.currentTimeMillis()
					this.userTimestamp = this@NoteViewModel.userTimestamp.value ?: System.currentTimeMillis()
					this.title = this@NoteViewModel.title.value
					this.color = this@NoteViewModel.color.value
					this.setLatLng(
						if (this@NoteViewModel.latLng.value != null) LatLng().apply {
							this.latitude = this@NoteViewModel.latLng.value?.latitude
							this.longitude = this@NoteViewModel.latLng.value?.longitude
						}
						else null
					)
					this.address = this@NoteViewModel.address.value
					this.contentThumbnail = dataText.substring(0, minOf(256, dataText.length))
					this.content = dataJson?.toString()
					this.isFavourite = this@NoteViewModel.isFavourite.value == true
					this.isLocked = this@NoteViewModel.isLocked.value == true

					this.attachmentList.clear()
					this.attachmentList.addAll(attachmentListStored.values.map { it.first })
					val newAttachmentData = putAttachment()
					this.thumbnail = newAttachmentData.second
					this.thumbnailType = newAttachmentData.third

					if (this@NoteViewModel.parentChapterId.value == null) {
//				        TODO Show error
						isOperationPending.value = false
					} else {
						this.parentChapterId = this@NoteViewModel.parentChapterId.value !!
						repository2.putNote(noteObject = this) { _, e ->
							repository2.putAttachment(this.id, newAttachmentData.first){ _, e ->
								attachmentListStored.filterNot { it.key in attachmentListBuffer.keys }.let {
									repository2.deleteAttachment(it.values.map { it.first }) { _, e ->
										chapterRead(this.parentChapterId !!, this.id)
										isOperationPending.value = false
									}
								}
							}
						}
					}
				}
			} catch (e : Exception) {
//				TODO Show error
				isOperationPending.value = false
			}
		}
	}

	fun toggleFavourite() {
		repository2.updateNoteFavourite(id = noteId.value !!)
	}

	fun toggleLock() {
		repository2.updateNoteLock(id = noteId.value !!)
	}

	fun putAttachment(): Triple<List<AttachmentObject>, String?, String?> {
		var thumbnail : String? = null
		var thumbnailType : String? = null

		val attachmentList : MutableList<AttachmentObject> = mutableListOf()

		this@NoteViewModel.attachmentListBuffer.forEach { (id, data) ->
			if (! attachmentListStored.containsKey(id)) {
				saveAttachment(id = id, inputFile = data.second, extension = data.first.extension)
				attachmentList.add(data.first)
			}

			if (thumbnail == null || thumbnailType == null) {
				data.second?.let { getThumbnail(attachmentObject = data.first, file = it) }?.let {
					BitmapFactory.decodeFile(data.second?.absolutePath)?.let {
						val aspectRatio = it.width.toFloat() / it.height.toFloat()
						val _thumbnail = it.let { ThumbnailUtils.extractThumbnail(it, (256 * aspectRatio).toInt(), 256) }

						thumbnail = Base64.getEncoder().encodeToString(_thumbnail.toByteArray())
						thumbnailType = data.first.getType().name
					}
				}
			}
		}

		return Triple(attachmentList, thumbnail, thumbnailType)
	}

	fun discardChanges() {
		viewModelScope.launch(Dispatchers.IO) {
			try {
				if (noteId.value != null) {
					getNote(id = noteId.value !!)
				}
			} catch (e : Exception) {
			}
		}
	}

	fun deleteNote() {
		try {
			isOperationPending.value = true
			if (noteId.value != null) {
				repository2.deleteNote(id = noteId.value !!) { _, e ->
					isOperationPending.value = false
				}
			}
		} catch (e : Exception) {
			isOperationPending.value = false
		}
	}

	fun addAttachmentToBuffer(uriList : List<Uri>) {
		viewModelScope.launch(Dispatchers.IO) {
			repository2.bufferAttachment(uriList).forEach {
				withContext(Dispatchers.Main) {
					attachmentListBuffer[it.key] = it.value
				}
			}
		}
	}

	fun removeAttachmentFromBuffer(attachmentId : ObjectId) {
		attachmentListBuffer.remove(attachmentId)
	}

	private fun saveAttachment(id : ObjectId, inputFile : File?, extension : String?) {
		val file = repository2.getAttachmentFile(id, extension)

		if (file != null) {
			try {
				val inputStream = inputFile?.inputStream()
				val outputStream = file.outputStream()
				if (inputStream != null) {
					copyInputStreamToOutputStream(inputStream, outputStream)
				}
			} catch (e : Exception) {
				e.printStackTrace()
//				TODO Show error message
			}
		}
	}

	private fun getThumbnail(attachmentObject : AttachmentObject, file : File) : Pair<String?, String?>? {
		if (attachmentObject.getType() == AttachmentObject.Companion.Type.IMAGE) {
			BitmapFactory.decodeFile(file.absolutePath)?.let {

				val aspectRatio = it.width.toFloat() / it.height.toFloat()
				val thumbnail = it.let { ThumbnailUtils.extractThumbnail(it, (256 * aspectRatio).toInt(), 256) }

				val thumbnailString = Base64.getEncoder().encodeToString(thumbnail.toByteArray())
				val thumbnailType = attachmentObject.getType().name

				return Pair(thumbnailType, thumbnailString)
			}
		}
		return null
	}

	fun getLocation(showRationale : Boolean = false) {
		viewModelScope.launch(Dispatchers.IO) {
			locationCoroutine?.cancel()
			locationCancellationSource?.cancel()
			locationCancellationSource = CancellationTokenSource()
			locationCoroutine = this
			if (repositoryState.value != RepositoryState.SUCCESS) this.cancel()

			val context = repository2.context

			val dataStoreInstance = DataStoreInstance(context = context)
			dataStoreInstance.getGeolocation.collect {
				if (! it) {
					withContext(Dispatchers.Main) { locationState.value = LocationState.DISABLED }
					this.cancel()
				} else {
					withContext(Dispatchers.Main) {
						locationState.value = LocationState.LOADING
					}

					if (context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
						context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
					) {
						withContext(Dispatchers.Main) { locationState.value = LocationState.NO_PERMISSION }
						showLocationPermissionDialog.value = showRationale
					} else {
						val fusedLocationClient : FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

						try {
							fusedLocationClient
								.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, locationCancellationSource?.token)
								.addOnSuccessListener {
									latLng.value = LatLng(it.latitude, it.longitude)
									context.reverseGeocode(
										latitude = it.latitude,
										longitude = it.longitude,
										onAddressAvailable = {
											if (it == null) {
												Toast.makeText(repository2.context, "Error getting address", Toast.LENGTH_SHORT).show()
											} else {
												onReceiveAddress(address = it)
											}
										},
										onIoException = {
											this.launch(Dispatchers.Main) {
												locationSnackbarHostState.showSnackbar(
													message = "Lat : ${latLng.value?.latitude}\nLng : ${latLng.value?.longitude}",
													duration = SnackbarDuration.Short
												)
												locationState.value = LocationState.ONLY_LATLNG
											}
										},
										onException = {
											this.launch(Dispatchers.Main) {
												locationSnackbarHostState.showSnackbar(
													message = "Lat : ${latLng.value?.latitude}\nLng : ${latLng.value?.longitude}",
													duration = SnackbarDuration.Short
												)
												locationState.value = LocationState.ONLY_LATLNG
											}
										}
									)
								}
								.addOnFailureListener { this.launch(Dispatchers.Main) { locationState.value = LocationState.KNOWN_ERROR } }
						} catch (e : Exception) {
							e.printStackTrace()
							withContext(Dispatchers.Main) { locationState.value = LocationState.KNOWN_ERROR }
							this.cancel()
						}
					}
				}
			}
		}
	}

	fun setLocation(latLng : LatLng, address : String?) {
		this.latLng.value = latLng
		this.address.value = address

		if (address == null) locationState.value = LocationState.ONLY_LATLNG
		else locationState.value = LocationState.SUCCESS
	}

	private fun onReceiveAddress(address : Address?) {
		CoroutineScope(Dispatchers.Main).launch {
			this@NoteViewModel.address.value = locationAddressFilter(address = address)

			if (this@NoteViewModel.address.value == null) {
				locationSnackbarHostState.showSnackbar(
					message = "Lat : ${this@NoteViewModel.latLng.value?.latitude}\nLng : ${this@NoteViewModel.latLng.value?.longitude}",
					duration = SnackbarDuration.Short
				)
				locationState.value = LocationState.ONLY_LATLNG
			} else {
				locationSnackbarHostState.showSnackbar(
					message = this@NoteViewModel.address.value ?: "Lat : ${latLng.value?.latitude}\nLng : ${latLng.value?.longitude}",
					duration = SnackbarDuration.Short
				)
				locationState.value = LocationState.SUCCESS
			}
		}
	}

	fun onRemoveLocation() {
		viewModelScope.launch(Dispatchers.Main) {
			latLng.value = null
			address.value = null
			locationState.value = LocationState.REMOVED
		}
	}

	override fun onCleared() {
		super.onCleared()

		this.locationCoroutine?.cancel()
		this.viewModelScope.cancel()
	}
}
