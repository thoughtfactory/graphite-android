package com.syncodec.graphite.presentation.note.screen.editorScreen

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syncodec.graphite.di.model.ChapterObject
import com.syncodec.graphite.di.model.LatLng
import com.syncodec.graphite.di.model.NoteObject
import com.syncodec.graphite.di.model.TagObject
import com.syncodec.graphite.di.model.TagObjectLite
import com.syncodec.graphite.di.repository.KoinRepository
import com.syncodec.graphite.di.repository.RepositoryState
import com.syncodec.graphite.utils.LocationDataState
import com.syncodec.graphite.utils.decodeBase64ToBitmap
import io.realm.kotlin.types.BacklinksDelegate
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.koin.android.annotation.KoinViewModel


@KoinViewModel
class EditorScreenViewModel(private val repository : KoinRepository) : ViewModel() {

	val repositoryState = repository.repositoryState

	/** Show loading animation and seize new events when set. */
	val isOperationPending = MutableStateFlow(false)

	/** Cancelled and reassigned when [getNote] is called.*/
	private var noteReaderCoroutine : CoroutineScope? = null

	/** Use [isNewNote] to get location. Default value is false because no operation occurs then not net.*/
	val isNewNote = MutableStateFlow(false)

	/** Use [isReady] to show loading screen.*/
	val isReady = MutableStateFlow(false)

	val noteId = MutableStateFlow<RealmUUID?>(null)
	val createdTimestamp : MutableStateFlow<Long?> = MutableStateFlow(null)
	val modifiedTimestamp : MutableStateFlow<Long?> = MutableStateFlow(null)
	val userTimestamp : MutableStateFlow<Long?> = MutableStateFlow(null)
	val title : MutableStateFlow<String?> = MutableStateFlow(null)
	val color : MutableStateFlow<Int?> = MutableStateFlow(null)
	val latLng : MutableStateFlow<LatLng?> = MutableStateFlow(null)
	val address : MutableStateFlow<String?> = MutableStateFlow(null)
	val contentThumbnail : MutableStateFlow<String?> = MutableStateFlow(null)
	val content : MutableStateFlow<String?> = MutableStateFlow(null)
	val thumbnail : MutableStateFlow<Bitmap?> = MutableStateFlow(null)
	val thumbnailType : MutableStateFlow<String?> = MutableStateFlow(null)
	val isFavourite : MutableStateFlow<Boolean?> = MutableStateFlow(null)
	val isLocked : MutableStateFlow<Boolean?> = MutableStateFlow(null)
	val parentChapter : MutableStateFlow<ChapterObject?> = MutableStateFlow(null)

	private val allTagList : MutableStateFlow<List<TagObject>> = MutableStateFlow(listOf())
	val tagList : MutableStateFlow<List<TagObjectLite>> = MutableStateFlow(listOf())
	val tagListSaved : MutableStateFlow<List<TagObjectLite>> = MutableStateFlow(listOf())
	val tagListToAdd : MutableStateFlow<List<TagObjectLite>> = MutableStateFlow(listOf())
	val tagListToRemove : MutableStateFlow<List<TagObjectLite>> = MutableStateFlow(listOf())

	/** Triggers when a new note is saved. Update UI accordingly.*/
	val onNoteSaved = MutableStateFlow(false)

	/** Used for bottom sheet to show the correct view and data.*/
	val locationDataState = MutableStateFlow(LocationDataState.INIT)

	init {
		initObserver()
		loadTags()
	}

	/** Observes [repositoryState] and [noteId] and calls [getNote] when [repositoryState] is [RepositoryState.SUCCESS] and [noteId] is not null.*/
	private fun initObserver() {
		viewModelScope.launch(Dispatchers.Default) {
			combine(repositoryState, noteId) { repositoryState, noteId ->
				repositoryState to noteId
			}.collect { (repositoryState, noteId) ->
				if (repositoryState == RepositoryState.SUCCESS) noteId?.let { getNote(it) }
			}
		}

		viewModelScope.launch(Dispatchers.Default) {
			combine(noteId, allTagList) { noteId, allTagList ->
				noteId to allTagList
			}.collect { (noteId, allTagList) ->
				noteId?.let { realmUUID ->
					val _tagListSaved = mutableListOf<TagObjectLite>()
					allTagList.forEach { tagObject -> if (realmUUID in tagObject.objectIdList) _tagListSaved.add(tagObject.toLite()) }
					this@EditorScreenViewModel.tagListSaved.tryEmit(_tagListSaved)
				}
			}
		}
	}

	/**
	 * Sets noteId which will trigger [getNote] to be called on [noteId] value change.
	 * @author pushpull
	 * @since 2.2.0
	 */
	fun loadNote(noteId : RealmUUID ) = this.noteId.tryEmit(noteId)

	private fun loadTags() {
		viewModelScope.launch(Dispatchers.Default) {
			repository.getAllTagAsFlow().collect { tagList ->
				this@EditorScreenViewModel.allTagList.tryEmit(tagList)
				this@EditorScreenViewModel.tagList.tryEmit(tagList.map { it.toLite() })
			}
		}
	}

	fun initNewNote(parentId : RealmUUID) {
		viewModelScope.launch(Dispatchers.Default) {
			repository.getChapterFromId(id = parentId)?.let { parentChapter ->
				NoteObject().let { noteObject ->
					this@EditorScreenViewModel.createdTimestamp.tryEmit(noteObject.createdTimestamp)
					this@EditorScreenViewModel.modifiedTimestamp.tryEmit(noteObject.modifiedTimestamp)
					this@EditorScreenViewModel.userTimestamp.tryEmit(noteObject.userTimestamp)
					this@EditorScreenViewModel.title.tryEmit(noteObject.title)
					this@EditorScreenViewModel.color.tryEmit(noteObject.color)
					this@EditorScreenViewModel.latLng.tryEmit(noteObject.getLatLng())
					this@EditorScreenViewModel.address.tryEmit(noteObject.address)
					this@EditorScreenViewModel.contentThumbnail.tryEmit(noteObject.contentThumbnail)
					this@EditorScreenViewModel.content.tryEmit(noteObject.content)
					this@EditorScreenViewModel.thumbnail.tryEmit(noteObject.thumbnail?.decodeBase64ToBitmap())
					this@EditorScreenViewModel.thumbnailType.tryEmit(noteObject.thumbnailType)
					this@EditorScreenViewModel.isFavourite.tryEmit(noteObject.isFavourite)
					this@EditorScreenViewModel.isLocked.tryEmit(noteObject.isLocked)
					this@EditorScreenViewModel.parentChapter.tryEmit(parentChapter)

					this@EditorScreenViewModel.isReady.tryEmit(true)
					this@EditorScreenViewModel.isNewNote.tryEmit(true)
				}
			} ?: run {
//				TODO    Default chapter not found
			}
		}
	}

	private fun getNote(noteId : RealmUUID) {
		viewModelScope.launch(Dispatchers.Default) {
			noteReaderCoroutine?.cancel()
			noteReaderCoroutine = this
			repository.getNoteFromIdAsFlow(id = noteId).collect { noteObject ->
				noteObject?.let {
					this@EditorScreenViewModel.createdTimestamp.tryEmit(it.createdTimestamp)
					this@EditorScreenViewModel.modifiedTimestamp.tryEmit(it.modifiedTimestamp)
					this@EditorScreenViewModel.userTimestamp.tryEmit(it.userTimestamp)
					this@EditorScreenViewModel.title.tryEmit(it.title)
					this@EditorScreenViewModel.color.tryEmit(it.color)
					this@EditorScreenViewModel.latLng.tryEmit(it.getLatLng())
					this@EditorScreenViewModel.address.tryEmit(it.address)
					this@EditorScreenViewModel.contentThumbnail.tryEmit(it.contentThumbnail)
					this@EditorScreenViewModel.content.tryEmit(it.content)
					this@EditorScreenViewModel.thumbnail.tryEmit(it.thumbnail?.decodeBase64ToBitmap())
					this@EditorScreenViewModel.thumbnailType.tryEmit(it.thumbnailType)
					this@EditorScreenViewModel.isFavourite.tryEmit(it.isFavourite)
					this@EditorScreenViewModel.isLocked.tryEmit(it.isLocked)
					it.parentId?.let { repository.getChapterFromId(id = it).let { parentChapter.tryEmit(it) } }

					loadLocationData(latLng = it.getLatLng(), address = it.address)

					this@EditorScreenViewModel.isReady.tryEmit(true)
				}
			}
		}
	}

	private fun putNote() {
		isOperationPending.tryEmit(true)
		viewModelScope.launch(Dispatchers.Default) {
			NoteObject().apply {
				this@EditorScreenViewModel.noteId.value?.let { this.id = it } ?: run { this@EditorScreenViewModel.noteId.tryEmit(this.id) }
				this@EditorScreenViewModel.createdTimestamp.value?.let { this.createdTimestamp = it }
				this.modifiedTimestamp = System.currentTimeMillis()
				this@EditorScreenViewModel.userTimestamp.value?.let { this.userTimestamp = it }
				this@EditorScreenViewModel.title.value?.let { this.title = it }
				this@EditorScreenViewModel.color.value?.let { this.color = it }
				this@EditorScreenViewModel.latLng.value?.let { this.setLatLng(it) }
				this@EditorScreenViewModel.address.value?.let { this.address = it }
				this@EditorScreenViewModel.contentThumbnail.value?.let { this.contentThumbnail = it }
				this@EditorScreenViewModel.content.value?.let { this.content = it }
				this@EditorScreenViewModel.thumbnailType.value?.let { this.thumbnailType = it }
				this@EditorScreenViewModel.isFavourite.value?.let { this.isFavourite = it }
				this@EditorScreenViewModel.isLocked.value?.let { this.isLocked = it }
				this@EditorScreenViewModel.parentChapter.value?.id?.let { this.parentId = it }

				observeSaveOperation(toSaveNoteObject = this)
				repository.putNote(noteObject = this)
			}
		}
	}

	fun putAttachmentThumbnail(noteId : RealmUUID, thumbnail: Bitmap) {
		repository.putThumbnailInNote(noteId = noteId, thumbnail = thumbnail)
	}

	private fun updateTagConnections(id : RealmUUID) {
		viewModelScope.launch(Dispatchers.Default) {
			val tagListToAdd = this@EditorScreenViewModel.tagListToAdd.value.map { it.id }
			val tagListToRemove = this@EditorScreenViewModel.tagListToRemove.value.map { it.id }
			this@EditorScreenViewModel.tagListToAdd.tryEmit(emptyList())
			this@EditorScreenViewModel.tagListToRemove.tryEmit(emptyList())
			repository.updateTagConnections(id = id, tagListToAdd = tagListToAdd, tagListToRemove = tagListToRemove)
			onNoteSaved.tryEmit(true)
		}
	}

	/** Observes if a new note is saved. Update UI according to [onNoteSaved]*/
	private fun observeSaveOperation(toSaveNoteObject : NoteObject) {
		viewModelScope.launch(Dispatchers.Default) {
			repository.getNoteFromIdAsFlow(id = toSaveNoteObject.id).collect { noteObject ->
				if (noteObject == toSaveNoteObject) {
					updateTagConnections(id = toSaveNoteObject.id)
					cancel()
				}
			}
		}
	}

	/** Used for bottom sheet to show the correct view and data.*/
	private fun loadLocationData(latLng : LatLng?, address : String?) {
		when {
			latLng == null && address == null -> locationDataState.tryEmit(LocationDataState.SUCCESS_NO_LOCATION)
			latLng == null && address == "" -> locationDataState.tryEmit(LocationDataState.SUCCESS_NO_LOCATION)
			latLng == null && address != null -> locationDataState.tryEmit(LocationDataState.SUCCESS_ONLY_ADDRESS)
			latLng != null && address == null -> locationDataState.tryEmit(LocationDataState.SUCCESS_ONLY_LATLNG)
			else -> locationDataState.tryEmit(LocationDataState.SUCCESS)
		}
	}

	fun setUserTimestamp(timestamp : Long) = this.userTimestamp.tryEmit(timestamp)

	fun removeLocation() {
		this.latLng.tryEmit(null)
		this.address.tryEmit(null)
		loadLocationData(latLng = null, address = null)
	}

	fun setLocation(latLng : LatLng?, address : String?) {
		this.latLng.tryEmit(latLng)
		this.address.tryEmit(address)
		loadLocationData(latLng = latLng, address = address)
	}

	fun setContent(data : String?) : Boolean {
		return try {
			val dataObject = JSONObject(data ?: "{}")
			val dataJson = dataObject.optJSONObject("dataJson")
			val dataText = dataObject.optString("dataText")

			return (this.contentThumbnail.tryEmit(dataText.substring(0, minOf(256, dataText.length))) &&
					this.content.tryEmit(dataJson?.toString()) &&
					this.title.tryEmit(dataObject.optString("title"))
					)
		} catch (e : Exception) {
			false
		}
	}

	fun saveNote() = this.putNote()
}
