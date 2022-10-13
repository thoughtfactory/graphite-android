package com.syncodec.graphite.presentation.note

import android.Manifest
import android.app.Application
import android.content.ContentResolver
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Build
import android.util.Log
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.syncodec.graphite.BaseApplication
import com.syncodec.graphite.di.Repository
import com.syncodec.graphite.di.Repository.getAttachmentFile
import com.syncodec.graphite.di.model.*
import com.syncodec.graphite.presentation.common.printer.Printer
import com.syncodec.graphite.utils.*
import io.realm.kotlin.types.ObjectId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.util.*


class NoteViewModel(application : Application) : AndroidViewModel(application) {

	var isNew : MutableState<Boolean?> = mutableStateOf(null)
	var isViewer : MutableState<Boolean> = mutableStateOf(true)
	var isSaving : MutableState<Boolean> = mutableStateOf(false)

	val showDeleteDialog : MutableState<Boolean> = mutableStateOf(false)
	val showDiscardDialog : MutableState<Boolean> = mutableStateOf(false)
	val showChapterSelectorDialog : MutableState<Boolean> = mutableStateOf(false)
	val showTagDialog : MutableState<Boolean> = mutableStateOf(false)
	val showLocationPermissionRationaleDialog : MutableState<Boolean> = mutableStateOf(false)
	val showSetLocationDialog : MutableState<Boolean> = mutableStateOf(false)
	val showPrintDialog : MutableState<Boolean> = mutableStateOf(false)

	val chapterObject : MutableState<ChapterObject?> = mutableStateOf(null)

	val allChapterList = Repository.getAllChapterAsFlow()
	var newParentChapterObject : MutableState<ChapterObject?> = mutableStateOf(null)

	val noteIdList : SnapshotStateList<ObjectId> = mutableStateListOf()

	var noteId : MutableState<ObjectId?> = mutableStateOf(null)
	var parentChapterId : MutableState<ObjectId?> = mutableStateOf(null)
	val createdTimestamp : MutableState<Long?> = mutableStateOf(null)
	val modifiedTimestamp : MutableState<Long?> = mutableStateOf(null)
	val userTimestamp : MutableState<Long?> = mutableStateOf(null)
	val title : MutableState<String?> = mutableStateOf(null)
	val color : MutableState<Int?> = mutableStateOf(null)
	val latLng : MutableState<LatLng?> = mutableStateOf(null)
	val address : MutableState<String?> = mutableStateOf(null)
	val contentThumbnail : MutableState<String?> = mutableStateOf(null)
	val content : MutableState<String?> = mutableStateOf(null)

	//  Map<Id, Triple<Uri, File, AttachmentObject>>
	val attachmentListStored : SnapshotStateMap<ObjectId, Triple<Uri, File, AttachmentObject>> = mutableStateMapOf()
	val attachmentListNew : SnapshotStateMap<ObjectId, Triple<Uri, File, AttachmentObject>> = mutableStateMapOf()
	val isFavourite : MutableState<Boolean> = mutableStateOf(false)
	val isLocked : MutableState<Boolean> = mutableStateOf(false)

	val tagObjectList = Repository.getAllTagAsFlow()

	var isUserScrollEnabled : MutableState<Boolean> = mutableStateOf(false)
	val locationSnackbarHostState = SnackbarHostState()

	val locationState : MutableState<LocationState> = mutableStateOf(LocationState.INIT)

	var locationCoroutine : CoroutineScope? = null

	fun initNewData(chapterId : ObjectId, filter : Extra.Companion.Filter) {

		viewModelScope.launch(Dispatchers.IO) {

			withContext(Dispatchers.Main) {
				newParentChapterObject.value = Repository.getChapter(chapterId)
			}

			Repository.getChapterAsFlow(chapterId).collectLatest {
				withContext(Dispatchers.Main) {
					chapterObject.value = it
				}
			}
		}

//		TODO Is this use of timestamp current
		this.createdTimestamp.value = System.currentTimeMillis()
		this.modifiedTimestamp.value = System.currentTimeMillis()
		this.userTimestamp.value = System.currentTimeMillis()
		this.parentChapterId.value = chapterId
		this.noteId.value = ObjectId.create()

		getLocation()

		isNew.value = true
		isViewer.value = false
	}

	fun loadAndViewData(chapterId : ObjectId, noteId : ObjectId, filter : Extra.Companion.Filter) {
		isNew.value = false
		when (filter) {
			Extra.Companion.Filter.SINGLE_READ -> {
				viewModelScope.launch(Dispatchers.IO) {
					withContext(Dispatchers.Main) {
						newParentChapterObject.value = Repository.getChapter(chapterId)
					}

					viewModelScope.launch(Dispatchers.IO) {
						Repository.getChapterAsFlow(chapterId).collectLatest {
							withContext(Dispatchers.Main) { chapterObject.value = it }
						}
					}

					withContext(Dispatchers.Main) {
						noteIdList.clear()
						noteIdList.add(noteId)
					}
				}
			}
			Extra.Companion.Filter.READ_CHAPTER -> {
				viewModelScope.launch(Dispatchers.IO) {
					isUserScrollEnabled.value = true

					withContext(Dispatchers.Main) {
						newParentChapterObject.value = Repository.getChapter(chapterId)
					}

					Repository
						.getChapterAsFlow(id = chapterId)
						.map {
							viewModelScope.launch(Dispatchers.Main) { chapterObject.value = it }
							it?.noteList?.sortedBy { - it.userTimestamp }?.map { it.id }
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

	fun updateNewChapterObject(chapterId : ObjectId) {
		viewModelScope.launch(Dispatchers.IO) {
			withContext(Dispatchers.Main) {
				newParentChapterObject.value = Repository.getChapter(chapterId)
			}
		}
	}

	fun bufferAttachment(uriList : List<Uri>) {
		uriList.forEach { uri ->
			var extension : String? = null
			val name = getApplication<BaseApplication>().getFileName(uri)
			try {
				extension =
					if (uri.scheme.equals(ContentResolver.SCHEME_CONTENT))
						MimeTypeMap.getSingleton().getExtensionFromMimeType(getApplication<BaseApplication>().applicationContext.contentResolver.getType(uri))
					else
						MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(uri.path?.let { File(it) }).toString())
			} catch (e : Exception) {
//		    	TODO Show error message
				e.printStackTrace()
			} finally {
				AttachmentObject().apply {
					this.name = name ?: this.id.toString()
					this.extension = extension
					this.mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)

					this.parentNoteId = this@NoteViewModel.noteId.value

					val uriAndFile = createTempFileToExpose(getApplication(), this.id.toString(), this.extension)
					val inputStream = getApplication<BaseApplication>().contentResolver.openInputStream(uri)
					val outputStream = getApplication<BaseApplication>().contentResolver.openOutputStream(uriAndFile.first)

					if (inputStream != null && outputStream != null) copyInputStreamToOutputStream(inputStream, outputStream)

					inputStream?.close()
					outputStream?.close()

					attachmentListNew[this.id] = Triple(uriAndFile.first, uriAndFile.second, this)
				}
			}
		}
	}

	fun putNote(data : String) {
		CoroutineScope(Dispatchers.IO).launch {
			try {
				locationCoroutine?.cancel()

				val dataObject = JSONObject(data)
				val dataJson = dataObject.getJSONObject("dataJson")
				val dataText = dataObject.getString("dataText")

				if (this@NoteViewModel.createdTimestamp.value == null ||
					this@NoteViewModel.modifiedTimestamp.value == null ||
					this@NoteViewModel.userTimestamp.value == null ||
					this@NoteViewModel.chapterObject.value == null
				) {
//			    TODO    Show msg
				} else {
					NoteObject().apply {
						withContext(Dispatchers.Main) { isSaving.value = true }

						if (noteId.value != null) this.id = noteId.value !!
						this.createdTimestamp = this@NoteViewModel.createdTimestamp.value !!
						this.modifiedTimestamp = this@NoteViewModel.modifiedTimestamp.value !!
						this.userTimestamp = this@NoteViewModel.userTimestamp.value !!
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
						this.content = dataJson.toString()
						this.isFavourite = this@NoteViewModel.isFavourite.value
						this.isLocked = this@NoteViewModel.isLocked.value

						this.attachmentList.clear()
						this@NoteViewModel.attachmentListNew.forEach { (id, data) ->
							if (! data.third.isSaved) {
								saveAttachment(id, data.second, data.third.extension)
							}
							this.attachmentList.add(data.third)

							if (this.thumbnail == null || this.thumbnailType == null) {
								if (data.third.getType() == AttachmentObject.Companion.Type.IMAGE) {
									BitmapFactory.decodeFile(data.second.absolutePath)?.let {

										val aspectRatio = it.width.toFloat() / it.height.toFloat()
										val thumbnail = it.let { ThumbnailUtils.extractThumbnail(it, (256 * aspectRatio).toInt(), 256) }

										this.thumbnail = Base64.getEncoder().encodeToString(thumbnail.toByteArray())
										this.thumbnailType = data.third.getType().name
									}
								}
							}
						}

						if (chapterObject.value != null) {
							Repository.putNote(chapterObject.value !!.id, this) {
								loadAndViewData(chapterObject.value !!.id, this.id, Extra.Companion.Filter.READ_CHAPTER)
								isSaving.value = false
							}
						}

						this@NoteViewModel.noteId.value = this.id
						isViewer.value = true
					}
				}
			} catch (e : Exception) {
//		    	TODO Show error message
				isSaving.value = false
				e.printStackTrace()
			}
		}
	}

	private fun saveAttachment(id : ObjectId, second : File, extension : String?) {
		val file = getApplication<BaseApplication>().getAttachmentFile(id, extension)

		if (file != null) {
			try {
				val inputStream = second.inputStream()
				val outputStream = file.outputStream()
				copyInputStreamToOutputStream(inputStream, outputStream)
			} catch (e : Exception) {
				e.printStackTrace()
//				TODO Show error message
			}
		}
	}

	private fun deleteAttachment(id : ObjectId, file : File) {
		try {
			Repository.deleteAttachment(id = id)
			file.delete()
		} catch (e : Exception) {
			e.printStackTrace()
//			TODO Show error message
		}
	}

	fun editNote() {
		isViewer.value = false
	}

	fun updateNote(data : String? = null) {
		CoroutineScope(Dispatchers.IO).launch {
			try {
				locationCoroutine?.cancel()
				isUserScrollEnabled.value = false

//				Only update if the entry already exists
				if (this@NoteViewModel.noteId.value != null) {
					NoteObject().apply {
						withContext(Dispatchers.Main) { isSaving.value = true }

						if (noteId.value != null) {
							this.id = noteId.value !!

							this.createdTimestamp = this@NoteViewModel.createdTimestamp.value !!
							this.modifiedTimestamp = this@NoteViewModel.modifiedTimestamp.value !!
							this.userTimestamp = this@NoteViewModel.userTimestamp.value !!
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

							if (data != null) {
								try {
									val dataObject = JSONObject(data)
									val dataJson = dataObject.getJSONObject("dataJson")
									val dataText = dataObject.getString("dataText")
									this.contentThumbnail = dataText.substring(0, minOf(256, dataText.length))
									this.content = dataJson.toString()
								} catch (e : Exception) {
									e.printStackTrace()
								}
							} else {
								this.contentThumbnail = this@NoteViewModel.contentThumbnail.value
								this.content = this@NoteViewModel.content.value
							}
							this.attachmentList.clear()

							attachmentListStored.forEach { (id, data) ->
								if (id !in attachmentListNew.keys) deleteAttachment(id, data.second)
							}

							this@NoteViewModel.attachmentListNew.forEach { (id, data) ->
								if (!data.third.isSaved) {
									saveAttachment(id, data.second, data.third.extension)
									data.third.isSaved = true
								}
								this.attachmentList.add(data.third)

								if (this.thumbnail.isNullOrBlank() || this.thumbnailType == null) {
									if (data.third.getType() == AttachmentObject.Companion.Type.IMAGE) {
										BitmapFactory.decodeFile(data.second.absolutePath)?.let {
											val aspectRatio = it.width.toFloat() / it.height.toFloat()
											val thumbnail = it.let { ThumbnailUtils.extractThumbnail(it, (256 * aspectRatio).toInt(), 256) }

											this.thumbnail = Base64.getEncoder().encodeToString(thumbnail.toByteArray())
											this.thumbnailType = data.third.getType().name
										}
									}
								}
							}
							this.isFavourite = this@NoteViewModel.isFavourite.value
							this.isLocked = this@NoteViewModel.isLocked.value

							if (chapterObject.value != null) {
								Repository.putNote(chapterObject.value !!.id, this) {
									Toast.makeText(getApplication(), "Note updated", Toast.LENGTH_SHORT).show()
									loadAndViewData(chapterObject.value !!.id, this.id, Extra.Companion.Filter.READ_CHAPTER)
									isSaving.value = false
								}
							}

							withContext(Dispatchers.Main) { isViewer.value = true }
						} else {
//							TODO Show error message
							withContext(Dispatchers.Main) { isViewer.value = true }
						}
					}
				}

				isUserScrollEnabled.value = true
			} catch (e : Exception) {
//				TODO Show error message
				isUserScrollEnabled.value = true
				e.printStackTrace()
			}
		}
	}

	fun getNote(id : ObjectId) {
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
			this.latLng.value = noteObject.getLatLng()
			this.address.value = noteObject.address
			this.contentThumbnail.value = noteObject.contentThumbnail
			this.content.value = noteObject.content
			this.attachmentListNew.clear()
			noteObject.attachmentList.forEach { attachmentObject ->
				val file = getApplication<BaseApplication>().getAttachmentFile(attachmentObject.id, attachmentObject.extension)

				file?.let { it1 ->
					val uri = FileProvider.getUriForFile(getApplication<BaseApplication>(), "com.syncodec.fileprovider", it1)
					attachmentListStored[attachmentObject.id] = Triple(uri, file, attachmentObject.clone().apply { this.isSaved = true })
					attachmentListNew[attachmentObject.id] = Triple(uri, file, attachmentObject.clone().apply { this.isSaved = true })
				}
			}

			this.isFavourite.value = noteObject.isFavourite
			this.isLocked.value = noteObject.isLocked

			if (this.latLng.value != null && this.address.value != null) {
				this.locationState.value = LocationState.SUCCESS
			}

			when {
				this.latLng.value != null && this.address.value != null -> this.locationState.value = LocationState.SUCCESS
				this.latLng.value != null && this.address.value == null -> this.locationState.value = LocationState.ONLY_ADDRESS
				this.latLng.value == null && this.address.value != null -> this.locationState.value = LocationState.ONLY_LATLNG
				else -> this.locationState.value = LocationState.REMOVED
			}
		}
	}

	fun deleteNote() {
		noteId.value?.let { Repository.deleteNote(it){} }
		showDeleteDialog.value = false
	}

	private val fusedLocationClient : FusedLocationProviderClient = FusedLocationProviderClient(application.applicationContext)
	private val cancellationToken = CancellationTokenSource().token

	fun getLocation(
		tryShowRationale : Boolean = false,
	) {
		locationState.value = LocationState.INIT

		viewModelScope.launch(Dispatchers.IO) {

			locationCoroutine?.cancel()
			locationCoroutine = this

			if (getApplication<BaseApplication>().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
				getApplication<BaseApplication>().checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
			) {
				withContext(Dispatchers.Main) { locationState.value = LocationState.NO_PERMISSION }
				showLocationPermissionRationaleDialog.value = tryShowRationale

				return@launch
			}

			fusedLocationClient
				.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cancellationToken)
				.addOnSuccessListener { location : Location? ->
					if (location == null) {
						this@NoteViewModel.latLng.value = null
						if (isViewer.value) updateNote()
						locationState.value = LocationState.ERROR
						Toast.makeText(getApplication(), "Error getting location", Toast.LENGTH_SHORT).show()
					} else {
						onReceiveLocation(latitude = location.latitude, longitude = location.longitude)

						reverseGeocode(
							latitude = location.latitude,
							longitude = location.longitude,
							onAddressAvailable = { address ->
								if (address == null) {
									Toast.makeText(getApplication(), "Error getting address", Toast.LENGTH_SHORT).show()
								} else {
									onReceiveAddress(address = locationAddressFilter(address = address))
								}
							},
							onIoException = {
								CoroutineScope(Dispatchers.Main).launch {
									locationSnackbarHostState.showSnackbar(
										message = "Lat : ${latLng.value?.latitude}\nLng : ${latLng.value?.longitude}",
										duration = SnackbarDuration.Short
									)
									locationState.value = LocationState.ONLY_LATLNG
								}
							},
							onException = {
								CoroutineScope(Dispatchers.Main).launch {
									locationSnackbarHostState.showSnackbar(
										message = "Lat : ${latLng.value?.latitude}\nLng : ${latLng.value?.longitude}",
										duration = SnackbarDuration.Short
									)
									locationState.value = LocationState.ONLY_LATLNG
								}
							}
						)
					}
				}
				.addOnFailureListener {
					viewModelScope.launch(Dispatchers.Main) {
						locationSnackbarHostState.showSnackbar(
							message = "Error getting location",
							duration = SnackbarDuration.Short
						)
					}
				}
		}
	}

	fun onReceiveLocation(
		latitude : Double,
		longitude : Double,
	) {
		this@NoteViewModel.latLng.value = LatLng(latitude, longitude)
		if (isViewer.value) updateNote()
		locationState.value = LocationState.LATLNG
	}

	fun onReceiveAddress(
		address : String?,
	) {
		viewModelScope.launch(Dispatchers.Main) {
			this@NoteViewModel.address.value = address
			if (isViewer.value) updateNote()

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


	fun removeLocation() {
		latLng.value = null
		address.value = null
		locationState.value = LocationState.REMOVED
		if (isViewer.value) updateNote()
	}

	fun reverseGeocode(
		latitude : Double,
		longitude : Double,
		onAddressAvailable : (Address?) -> Unit,
		onIoException : () -> Unit,
		onException : () -> Unit
	) {
		viewModelScope.launch(Dispatchers.IO) {
			try {
				val geocoder = Geocoder(getApplication(), Locale.getDefault())
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
					geocoder.getFromLocation(latitude, longitude, 1) { addresses ->
						onAddressAvailable(addresses.getOrNull(0))
					}
				} else {
//					Deprecation is handled in upper block
					val addresses = geocoder.getFromLocation(latitude, longitude, 1)
					onAddressAvailable(addresses?.firstOrNull())
				}

			} catch (exception : IOException) {
				onIoException()
			} catch (exception : Exception) {
				onException()
			}
		}
	}

	fun onUpdateFavorite() {
		isFavourite.value = ! isFavourite.value
		if (isViewer.value) updateNote()
	}

	fun onUpdateLock() {
		isLocked.value = ! isLocked.value
		if (isViewer.value) updateNote()
	}

	fun updateTitle(title : String?) {
		this.title.value = title
		if (isViewer.value) updateNote()
	}

	fun putTag(tag : String, color : Color) {
		TagObject().apply {
			this.tag = tag
			this.color = color.toArgb()
			Repository.putTag(tagObject = this)
		}
	}

	fun updateTagConnection(tagObjectId : ObjectId) {
		Repository.updateTagConnection(tagObjectId = tagObjectId, objectId = noteId.value)
	}

	fun printNote(data : String) {
		showPrintDialog.value = true
		viewModelScope.launch(Dispatchers.Main) {
			val printer = Printer(getApplication())
			printer.createWebPrintJob(data)
		}
	}
}
