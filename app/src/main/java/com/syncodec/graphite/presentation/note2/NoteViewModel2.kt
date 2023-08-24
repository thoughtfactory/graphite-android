package com.syncodec.graphite.presentation.note2

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syncodec.graphite.di.model.ChapterObject
import com.syncodec.graphite.di.model.ChapterObjectLite
import com.syncodec.graphite.di.model.LatLng
import com.syncodec.graphite.di.model.NoteObject
import com.syncodec.graphite.di.model.TagObject
import com.syncodec.graphite.di.repository.Repository
import com.syncodec.graphite.utils.Location.getLocation
import com.syncodec.graphite.utils.LocationData
import io.realm.kotlin.ext.copyFromRealm
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import java.io.File
import java.time.Instant


@KoinViewModel
class NoteViewModel2(private val repo: Repository, private val repositoryStatusStateFlow: MutableStateFlow<Repository.Companion.RepositoryStatus>) : ViewModel() {

	init {
		viewModelScope.launch(Dispatchers.Default) {
			repositoryStatusStateFlow.collect {
			}
		}
	}

	private val _noteId: MutableStateFlow<RealmUUID?> = MutableStateFlow(null)
	val noteId: StateFlow<RealmUUID?> = _noteId

	private val _createdTimestamp: MutableStateFlow<Long?> = MutableStateFlow(null)
	val createdTimestamp: StateFlow<Long?> = _createdTimestamp

	private val _modifiedTimestamp: MutableStateFlow<Long?> = MutableStateFlow(null)
	val modifiedTimestamp: StateFlow<Long?> = _modifiedTimestamp

	private val _userTimestamp: MutableStateFlow<Long?> = MutableStateFlow(null)
	val userTimestamp: StateFlow<Long?> = _userTimestamp

	private val _title: MutableStateFlow<String?> = MutableStateFlow(null)
	val title: StateFlow<String?> = _title

	private val _locationData: MutableStateFlow<LocationData> = MutableStateFlow(LocationData.Init)
	val locationData: StateFlow<LocationData> = _locationData

	private val _content: MutableStateFlow<String?> = MutableStateFlow(null)
	val content: StateFlow<String?> = _content

	private val _isFavourite: MutableStateFlow<Boolean?> = MutableStateFlow(null)
	val isFavourite: StateFlow<Boolean?> = _isFavourite

	private val _isLocked: MutableStateFlow<Boolean?> = MutableStateFlow(null)
	val isLocked: StateFlow<Boolean?> = _isLocked

	private val _parentId: MutableStateFlow<RealmUUID?> = MutableStateFlow(null)
	val parentId: StateFlow<RealmUUID?> = _parentId

	private val _parentChapter : MutableStateFlow<ChapterObjectLite?> = MutableStateFlow(null)
	val parentChapter : StateFlow<ChapterObjectLite?> = _parentChapter

	//	Attachments
	private val _savedFileList: MutableStateFlow<List<File>> = MutableStateFlow(listOf())
	val savedFileList: StateFlow<List<File>> = _savedFileList

	private val _newFileList: MutableStateFlow<List<Uri>> = MutableStateFlow(listOf())
	val newFileList: StateFlow<List<Uri>> = _newFileList

	//	Tags
	private val _allTagsList: MutableStateFlow<List<TagObject>> = MutableStateFlow(listOf())
	val allTagsList: StateFlow<List<TagObject>> = _allTagsList

	private var putNoteCoroutineScope: CoroutineScope? = null
	private var getNoteCoroutineScope: CoroutineScope? = null
	private var getChapterCoroutineScope: CoroutineScope? = null

	val isEditing: MutableStateFlow<Boolean?> = MutableStateFlow(null)
	val kitKatFormat : MutableStateFlow<KitKat.Companion.KitKatFormat?> = MutableStateFlow(null)

	init {
		viewModelScope.launch(Dispatchers.Default) {
			repositoryStatusStateFlow.collect { repositoryStatus ->
				if (repositoryStatus is Repository.Companion.RepositoryStatus.Success) {
					repositoryStatus.repository.getAllTagAsFlow().collect { _allTagsList.tryEmit(it) }
				}
			}
		}
	}

	fun initNote(parentId: RealmUUID) {
		this._createdTimestamp.tryEmit(Instant.now().toEpochMilli())
		this._modifiedTimestamp.tryEmit(Instant.now().toEpochMilli())
		this._userTimestamp.tryEmit(Instant.now().toEpochMilli())

		this._parentId.tryEmit(parentId)
		getChapter()

		reloadLocation()
	}

	fun getNote(noteId: RealmUUID) {
		getChapter()
		viewModelScope.launch(Dispatchers.Default) {
			getNoteCoroutineScope?.cancel()
			getNoteCoroutineScope = this
			repositoryStatusStateFlow.collect { repositoryStatus ->
				if (repositoryStatus is Repository.Companion.RepositoryStatus.Success) {
					repositoryStatus.repository.getNoteFromIdAsFlow(id = noteId).collect { noteObject ->

//						Load note
						this@NoteViewModel2._noteId.tryEmit(noteObject?.id)
						this@NoteViewModel2._createdTimestamp.tryEmit(noteObject?.createdTimestamp)
						this@NoteViewModel2._modifiedTimestamp.tryEmit(noteObject?.modifiedTimestamp)
						this@NoteViewModel2._userTimestamp.tryEmit(noteObject?.userTimestamp)
						this@NoteViewModel2._title.tryEmit(noteObject?.title)
//						this@NoteViewModel2._userTimestamp.tryEmit(noteObject?.color)
						this@NoteViewModel2.setLocation(latLng = noteObject?.getLatLng(), address = noteObject?.address)
//						this@NoteViewModel2._userTimestamp.tryEmit(noteObject?.contentThumbnail)
						this@NoteViewModel2._content.tryEmit(noteObject?.content)
//						this@NoteViewModel2._userTimestamp.tryEmit(noteObject?.thumbnail)
						this@NoteViewModel2._isFavourite.tryEmit(noteObject?.isFavourite)
						this@NoteViewModel2._isLocked.tryEmit(noteObject?.isLocked)
						this@NoteViewModel2._parentId.tryEmit(noteObject?.parentId)

//						Load attachments
						noteObject?.id?.let {
							_savedFileList.tryEmit(repositoryStatus.repository.attachmentRepository.getAttachmentFromNote(parentId = it))
						}
					}
				}
			}
		}
	}

	private fun getChapter() {
		viewModelScope.launch(Dispatchers.Default) {
			getChapterCoroutineScope?.cancel()
			getChapterCoroutineScope = this
			repositoryStatusStateFlow.collect { repositoryStatus ->
				this@NoteViewModel2.parentId.collect {
					if (repositoryStatus is Repository.Companion.RepositoryStatus.Success) {
						this@NoteViewModel2._parentChapter.tryEmit(repositoryStatus.repository.getChapterFromId(id = it)?.toLite())
					}
				}
			}
		}
	}

	fun reloadLocation() {
		viewModelScope.launch(Dispatchers.IO) {
			getLocation(context = repo.context) { _locationData.tryEmit(it) }
		}
	}

	fun setLocation(latLng: LatLng?, address: String?) {
		when {
			latLng == null && address == null -> _locationData.tryEmit(LocationData.Removed)
			latLng == null && address != null -> _locationData.tryEmit(LocationData.SuccessOnlyAddress(address = address))
			latLng != null && address == null -> _locationData.tryEmit(LocationData.SuccessOnlyLatLng(latLng = latLng))
			latLng != null && address != null -> _locationData.tryEmit(LocationData.Success(latLng = latLng, address = address))
		}
	}

	fun addNewFileToBuffer(fileList: List<Uri>) {
		val updatedNewFileList = _newFileList.value.toMutableList().apply {
			addAll(fileList)
		}
		_newFileList.tryEmit(updatedNewFileList)
	}

	fun save(kitKatFormat: KitKat.Companion.KitKatFormat) {
		viewModelScope.launch(Dispatchers.Default) {
			putNoteCoroutineScope?.cancel()
			putNoteCoroutineScope = this
			repositoryStatusStateFlow.collect { repositoryStatus ->
				if (repositoryStatus is Repository.Companion.RepositoryStatus.Success) {
					NoteObject().apply {
						this@NoteViewModel2.noteId.value?.let { this.id = it } ?: run { this@NoteViewModel2._noteId.tryEmit(this.id) }
						this.modifiedTimestamp = Instant.now().toEpochMilli()
						this@NoteViewModel2.createdTimestamp.value?.let { this.createdTimestamp = it }
						this@NoteViewModel2.userTimestamp.value?.let { this.userTimestamp = it }
						this.title = kitKatFormat.kitKatTitle
//						this@NoteViewModel2.color.value?.let { this.color = it }
						this@NoteViewModel2.locationData.value.let {
							this.setLatLng(it.getLatLngOrNull())
							this.address = it.getAddressOrNull()
						}
						this.content = kitKatFormat.kitKatContent
//						this@NoteViewModel2.thumbnail.value?.let { this.thumbnail = it }
						this@NoteViewModel2.isFavourite.value?.let { this.isFavourite = it }
						this@NoteViewModel2.isLocked.value?.let { this.isLocked = it }
						this@NoteViewModel2.parentId.value?.let { this.parentId = it }

						repositoryStatus.repository.putNote(noteObject = this, modifyTimestampAuto = false)

						repositoryStatus.repository.attachmentRepository.putAttachment(parentId = this.id, uriList = newFileList.value, keepName = true)

						getNote(this.id)
						cancel()
					}
				}
			}
		}
	}

	fun toggleFavourite() {
		viewModelScope.launch(Dispatchers.Default) {
			putNoteCoroutineScope?.cancel()
			putNoteCoroutineScope = this
			repositoryStatusStateFlow.collect { repositoryStatus ->
				this@NoteViewModel2.noteId.value?.let {noteId1 ->
					if (repositoryStatus is Repository.Companion.RepositoryStatus.Success) {
						repositoryStatus.repository.getNoteFromId(id = noteId1)?.copyFromRealm()?.let { noteObject ->
							noteObject.isFavourite = !noteObject.isFavourite
							repositoryStatus.repository.putNote(noteObject)
						}
					}
				}
			}
		}
	}

	fun toggleLocked() {
		viewModelScope.launch(Dispatchers.Default) {
			putNoteCoroutineScope?.cancel()
			putNoteCoroutineScope = this
			repositoryStatusStateFlow.collect { repositoryStatus ->
				this@NoteViewModel2.noteId.value?.let {noteId1 ->
					if (repositoryStatus is Repository.Companion.RepositoryStatus.Success) {
						repositoryStatus.repository.getNoteFromId(id = noteId1)?.copyFromRealm()?.let { noteObject ->
							noteObject.isLocked = !noteObject.isLocked
							repositoryStatus.repository.putNote(noteObject)
						}
					}
				}
			}
		}
	}
}