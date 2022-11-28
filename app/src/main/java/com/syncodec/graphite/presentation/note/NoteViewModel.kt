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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.syncodec.graphite.BaseApplication
import com.syncodec.graphite.di.model.ChapterObject
import com.syncodec.graphite.di.model.ChapterObjectLite
import com.syncodec.graphite.di.model.LatLng
import com.syncodec.graphite.di.model.NoteObject
import com.syncodec.graphite.di.model.TagObject
import com.syncodec.graphite.di.repository.RealmNotInitializedException
import com.syncodec.graphite.di.repository.Repository2
import com.syncodec.graphite.di.repository.RepositoryState
import com.syncodec.graphite.presentation.note.util.reverseGeocode
import com.syncodec.graphite.utils.AttachmentType
import com.syncodec.graphite.utils.DataStoreInstance
import com.syncodec.graphite.utils.LocationState
import com.syncodec.graphite.utils.copyInputStreamToOutputStream
import com.syncodec.graphite.utils.encodeBase64
import com.syncodec.graphite.utils.locationAddressFilter
import com.syncodec.graphite.utils.type
import dagger.hilt.android.lifecycle.HiltViewModel
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.time.Clock
import javax.inject.Inject


@HiltViewModel
class NoteViewModel @Inject constructor(private val repository2 : Repository2) : ViewModel() {

	val repositoryState = repository2.repositoryState

	val isNew : MutableState<Boolean?> = mutableStateOf(null)
	val isViewing : MutableState<Boolean?> = mutableStateOf(null)
	val isOperationPending : MutableState<Boolean> = mutableStateOf(false)
	val locationSnackbarHostState = SnackbarHostState()

	val noteIdList : SnapshotStateList<RealmUUID> = mutableStateListOf()
	val noteId : MutableState<RealmUUID?> = mutableStateOf(null)
	val noteObject : MutableState<NoteObject?> = mutableStateOf(null)
	val parentChapterId : MutableState<RealmUUID?> = mutableStateOf(null)
	val parentChapterObject : MutableState<ChapterObject?> = mutableStateOf(null)

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

	val attachmentListStored : SnapshotStateList<Pair<File?, Uri?>> = mutableStateListOf()
	val attachmentListBuffer : SnapshotStateList<Pair<File?, Uri?>> = mutableStateListOf()

	val locationState : MutableState<LocationState> = mutableStateOf(LocationState.INIT)
	var locationCoroutine : CoroutineScope? = null
	var locationCancellationSource : CancellationTokenSource? = null

	val tagList : SnapshotStateList<TagObject> = mutableStateListOf()
	val tagListBuffer : SnapshotStateList<TagObject> = mutableStateListOf()

	val selectChapterList : SnapshotStateList<ChapterObject> = mutableStateListOf()
	val selectParentChapter : MutableState<ChapterObject?> = mutableStateOf(null)
	val selectChapterPath : SnapshotStateList<ChapterObjectLite> = mutableStateListOf()

	val showLocationPermissionDialog : MutableState<Boolean> = mutableStateOf(false)
	val showLocationPickerDialog : MutableState<Boolean> = mutableStateOf(false)
	val showNotificationPermissionDialog : MutableState<Boolean> = mutableStateOf(false)
	val showChapterSelectionDialog : MutableState<Boolean> = mutableStateOf(false)
	val showDiscardDialog : MutableState<Boolean> = mutableStateOf(false)
	val showDeleteDialog : MutableState<Boolean> = mutableStateOf(false)

	var chapterCoroutine : CoroutineScope? = null

	init {
		viewModelScope.launch(Dispatchers.Default) {
			when (repositoryState.value) {
				RepositoryState.INIT -> null
				RepositoryState.LOCKED -> null
				RepositoryState.LOADING -> null
				RepositoryState.SUCCESS -> {
					viewModelScope.launch(Dispatchers.Default) {
						if (repositoryState.value != RepositoryState.SUCCESS) this.cancel()
						repository2.getAllTagAsFlow().collect {
							withContext(Dispatchers.Main) {
								tagList.clear()
								tagList.addAll(it)
							}
						}
					}
				}

				RepositoryState.ERROR -> null
			}
		}
	}

	fun singleRead(noteId : RealmUUID) {
		viewModelScope.launch(Dispatchers.Default) {
			noteIdList.add(noteId)
			getNote(id = noteId)
		}
	}

	fun chapterRead(chapterId : RealmUUID, noteId : RealmUUID) {
		viewModelScope.launch(Dispatchers.Default) {
			when (repositoryState.value) {
				RepositoryState.INIT -> null
				RepositoryState.LOCKED -> null
				RepositoryState.LOADING -> null
				RepositoryState.SUCCESS -> {
					viewModelScope.launch(Dispatchers.Default) {
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

	fun chapterReadNew(chapterId : RealmUUID) {
		viewModelScope.launch(Dispatchers.Default) {
			when (repositoryState.value) {
				RepositoryState.INIT -> null
				RepositoryState.LOCKED -> null
				RepositoryState.LOADING -> null
				RepositoryState.SUCCESS -> {
					viewModelScope.launch(Dispatchers.Default) {
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
		userTimestamp.value = Clock.systemDefaultZone().millis()
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

		viewModelScope.launch(Dispatchers.IO) {
			val getGeolocation = DataStoreInstance(context = repository2.context)
				.getGeolocation
				.first()

			val isPro = BaseApplication.isPro.value

			when {
				isPro && getGeolocation -> getLocation()
				isPro && ! getGeolocation -> withContext(Dispatchers.Main) { locationState.value = LocationState.DISABLED }
				else -> withContext(Dispatchers.Main) { locationState.value = LocationState.NOT_PRO }
			}
		}
	}

	fun getNote(id : RealmUUID, retry : Int = 10) {
		viewModelScope.launch(Dispatchers.Default) {
			repository2.isAuthenticated.tryEmit(true)
			try {
				repository2.getNoteFromIdAsFlow(id).cancellable().collect { noteObject ->
					this@NoteViewModel.noteId.value = noteObject?.id
					if (noteObject?.id != this@NoteViewModel.noteId.value) cancel()
					if (noteObject != null) {
						withContext(Dispatchers.Main) {
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

							this@NoteViewModel.parentChapterId.value = noteObject.parentId
							getChapter()

							attachmentListStored.clear()
							attachmentListBuffer.clear()

							repository2.readAttachmentFromNoteId(noteObject.id).forEach {
								attachmentListStored.add(it)
								attachmentListBuffer.add(it)
							}

							when {
								this@NoteViewModel.latLng.value != null && this@NoteViewModel.address.value != null -> locationState.value =
									LocationState.SUCCESS

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
					isOperationPending.value = false
				}
			} catch (e : RealmNotInitializedException) {
				if (retry > 0) {
					repository2.isAuthenticated.tryEmit(true)
					delay(470)
					getNote(id, retry - 1)
				} else {
					Toast.makeText(repository2.context, "Error reading data", Toast.LENGTH_SHORT).show()
				}
			}
		}
		viewModelScope.launch(Dispatchers.Default) {
			withContext(Dispatchers.Main) { tagListBuffer.clear() }
			tagList.forEach {
				if (it.objectIdList.contains(id)) {
					withContext(Dispatchers.Main) {
						if (! tagListBuffer.contains(it)) tagListBuffer.add(it)
					}
				}
			}
		}
	}

	fun getChapter() {
		viewModelScope.launch(Dispatchers.Default) {

			chapterCoroutine?.cancel()
			chapterCoroutine = this

			parentChapterId.value?.let {
				repository2.getChapterFromIdAsFlow(it).cancellable().collect { chapterObject ->
					parentChapterObject.value = chapterObject
					selectParentChapter.value = chapterObject
				}
			}
		}
	}

	fun editNote() {
		viewModelScope.launch(Dispatchers.Default) {
			this@NoteViewModel.isViewing.value = false
		}
	}

	fun putNote() {
		if (! isOperationPending.value) {
			CoroutineScope(Dispatchers.Default).launch {
				try {
					locationCancellationSource?.cancel()
					locationCancellationSource = null

					NoteObject().apply {
						isOperationPending.value = true

						if (this@NoteViewModel.noteId.value != null) this.id = this@NoteViewModel.noteId.value !!
						this.createdTimestamp = this@NoteViewModel.createdTimestamp.value ?: System.currentTimeMillis()
						this.modifiedTimestamp = this@NoteViewModel.modifiedTimestamp.value ?: System.currentTimeMillis()
						this.userTimestamp = this@NoteViewModel.userTimestamp.value ?: System.currentTimeMillis()
						this.title = this@NoteViewModel.title.value
						this.color = this@NoteViewModel.color.value
						if (this@NoteViewModel.latLng.value != null) LatLng().apply {
							this.latitude = this@NoteViewModel.latLng.value?.latitude
							this.longitude = this@NoteViewModel.latLng.value?.longitude
						}.let { this.setLatLng(it) }
						this.address = this@NoteViewModel.address.value
						this.contentThumbnail = this@NoteViewModel.contentThumbnail.value
						this.content = this@NoteViewModel.content.value
						this.isFavourite = this@NoteViewModel.isFavourite.value == true
						this.isLocked = this@NoteViewModel.isLocked.value == true

						val (thumbnail, thumbnailType) = putAttachment(this.id)
						this.thumbnail = thumbnail
						this.thumbnailType = thumbnailType

						if (this@NoteViewModel.parentChapterId.value == null) {
//				        TODO Show error
							isOperationPending.value = false
						} else {
							this.parentId = this@NoteViewModel.parentChapterId.value !!
							repository2.putNote(noteObject = this) { _, e ->
								repository2.connectTag(this.id, tagListBuffer.map { it.id }) { _, e ->
									chapterRead(this.parentId !!, this.id)
									viewModelScope.launch(Dispatchers.Main) {
										Toast.makeText(repository2.context, "Note saved", Toast.LENGTH_SHORT).show()
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
	}

	fun setContent(data : String?) {
		val dataObject = JSONObject(data ?: "{}")
		val dataJson = dataObject.optJSONObject("dataJson")
		val dataText = dataObject.optString("dataText")

		this.contentThumbnail.value = dataText.substring(0, minOf(256, dataText.length))
		this.content.value = dataJson?.toString()
		this.title.value = dataObject.optString("title")
	}

	fun toggleFavourite() {
		this.isFavourite.value = this.isFavourite.value?.not()
		this.putNote()
	}

	fun toggleLock() {
		this.isLocked.value = this.isLocked.value?.not()
		this.putNote()
	}

	fun putAttachment(noteId : RealmUUID): Pair<String?, String?> {
		var thumbnail : String? = null
		var thumbnailType : String? = null

		this@NoteViewModel.attachmentListStored.forEach { (file, uri) ->
			if (attachmentListBuffer.find { it.first == file && it.second == uri } == null) file?.delete()
		}

		this@NoteViewModel.attachmentListBuffer.forEach { (file, uri) ->
			if (attachmentListStored.find { it.first == file && it.second == uri } == null) {
				saveAttachment(noteId = noteId, inputFile = file)
			}

			if (thumbnail == null || thumbnailType == null) {
				file?.let { getThumbnail(file = file) }?.let {
					thumbnail = it
					thumbnailType = file.type()
				}
			}
		}

		return Pair(thumbnail, thumbnailType)
	}

	fun discardChanges(callback : (Boolean) -> Unit) {
		viewModelScope.launch(Dispatchers.Default) {
			try {
				if (isNew.value == false) {
					if (noteId.value != null) getNote(id = noteId.value !!)
					callback(false)
				} else callback(true)
			} catch (e : Exception) {
				callback(false)
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
		viewModelScope.launch(Dispatchers.Default) {
			withContext(Dispatchers.Main) {
				attachmentListBuffer.addAll(repository2.bufferAttachment(uriList))
			}
		}
	}

	fun removeAttachmentFromBuffer(file : File?, uri : Uri?) = attachmentListBuffer.removeIf { it.first == file && it.second == uri }

	private fun saveAttachment(noteId : RealmUUID, inputFile : File?) : String? {
		val file = repository2.getNewAttachmentFile(noteId, name = inputFile?.name?.replace(Regex("attachment_[0-9]*_"), "") ?: RealmUUID.random().toString())

		return if (file != null) {
			try {
				val inputStream = inputFile?.inputStream()
				val outputStream = file.outputStream()
				if (inputStream != null) copyInputStreamToOutputStream(inputStream, outputStream)
				file.name
			} catch (e : Exception) {
				e.printStackTrace()
//				TODO Show error message
				null
			}
		} else null
	}

	private fun getThumbnail(file : File) : String? {
		val type = file.type()

		if (type == AttachmentType.IMAGE.name.lowercase()) {
			BitmapFactory.decodeFile(file.absolutePath).let {
				val aspectRatio = it.width.toFloat() / it.height.toFloat()
				val thumbnail = ThumbnailUtils.extractThumbnail(it, (256 * aspectRatio).toInt(), 256)
				return thumbnail.encodeBase64()
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

			withContext(Dispatchers.Main) { locationState.value = LocationState.LOADING }

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

	fun getSelectChapter(parentChapterId : RealmUUID?) {
		viewModelScope.launch(Dispatchers.Default) {
			repository2.getChapterWithParentId(parentChapterId = parentChapterId).let {
				withContext(Dispatchers.Main) {
					selectChapterList.clear()
					selectChapterList.addAll(it.second)
				}
			}
			repository2.getChapterFromId(id = parentChapterId).let {
				repository2.getParentChapterList(id = it?.id, true) { list, _ ->
					viewModelScope.launch(Dispatchers.Main) {
						selectChapterPath.clear()
						list?.let { selectChapterPath.addAll(it) }
					}
				}
			}
		}
	}

	fun moveNoteToChapter(chapterId : RealmUUID) {
		this@NoteViewModel.parentChapterId.value = chapterId
		if (isViewing.value == true) this@NoteViewModel.putNote()
	}

	fun onConnectTag(tagObject : TagObject) {
		if (isViewing.value == true) {
//			noteId.value?.let { repository2.connectTag(noteId = it, tagId = tagObject.id) }
		} else {
			if (tagListBuffer.contains(tagObject)) tagListBuffer.remove(tagObject)
			else tagListBuffer.add(tagObject)
		}
	}

	fun setUserTimestamp(timestamp : Long) {
		this.userTimestamp.value = timestamp
	}

	fun onSetTitle(title : String?) {
		this.title.value = title
	}

	override fun onCleared() {
		super.onCleared()

		this.locationCoroutine?.cancel()
		this.viewModelScope.cancel()
	}

	fun exportPdf(data : String?) {
	}
}
