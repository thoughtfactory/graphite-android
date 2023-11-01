package com.syncodec.graphite.presentation.note

import android.net.Uri
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syncodec.graphite.di.model.local.ChapterObjectLite
import com.syncodec.graphite.di.model.local.LatLng
import com.syncodec.graphite.di.model.local.NoteObject
import com.syncodec.graphite.di.model.local.TagObject
import com.syncodec.graphite.di.repository.LockableRepo
import com.syncodec.graphite.di.repository.Repository
import com.syncodec.graphite.presentation.note.kitKat.KitKatFormat
import com.syncodec.graphite.utils.Location.getLocation
import com.syncodec.graphite.utils.LocationData
import com.syncodec.graphite.utils.dataStore.DataStoreInstance
import io.realm.kotlin.UpdatePolicy
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.android.annotation.KoinViewModel
import java.io.File


@KoinViewModel
class NoteViewModel(private val lockableRepo: LockableRepo, private val dataStoreInstance : DataStoreInstance) : ViewModel() {

	private val _repository: MutableStateFlow<Repository?> = MutableStateFlow(null)

	private val _noteId: MutableStateFlow<RealmUUID?> = MutableStateFlow(null)

	private val _noteObject: MutableStateFlow<NoteObject?> = MutableStateFlow(null)
	val noteObject: StateFlow<NoteObject?> = _noteObject

	private val _locationData: MutableStateFlow<LocationData> = MutableStateFlow(LocationData.Init)
	val locationData: StateFlow<LocationData> = _locationData

	private val _parentChapter: MutableStateFlow<ChapterObjectLite?> = MutableStateFlow(null)
	val parentChapter: StateFlow<ChapterObjectLite?> = _parentChapter

	//	Attachments
	private val _attachmentList: MutableStateFlow<List<AttachmentState>> = MutableStateFlow(listOf())
	val attachmentList: StateFlow<List<AttachmentState>> = _attachmentList

	//	Tags
	private val _allTagsList: MutableStateFlow<List<TagObject>> = MutableStateFlow(listOf())
	val allTagsList: StateFlow<List<TagObject>> = _allTagsList

	private val _tagStateMap: MutableStateFlow<Map<TagObject, TagObjectState>> = MutableStateFlow(mapOf())
	val tagStateMap: StateFlow<Map<TagObject, TagObjectState>> = _tagStateMap

	val isEditing: MutableStateFlow<Boolean?> = MutableStateFlow(null)
	private var kitKatFormat: KitKatFormat = KitKatFormat()

	init {
		viewModelScope.launch(Dispatchers.Default) {
			lockableRepo.repositoryStatusFlow.collectLatest { repositoryStatus ->
				if (repositoryStatus is Repository.Companion.RepositoryStatus.Success) _repository.tryEmit(repositoryStatus.repository)
			}
		}

		viewModelScope.launch(Dispatchers.Default) {
			combine(_repository, _noteId) { repository1, noteId1 -> Pair(repository1, noteId1) }.collectLatest { (repository1, noteId1) ->
				repository1?.let { repository2 ->
					noteId1?.let { launch { observeNote(repository = repository2, noteId = it) } }
					launch { observeChapter(repository = repository2) }
					launch { observeAllTags(repository = repository2) }
				}
				launch { observeConnectedTags() }
			}
		}
	}

	private suspend fun observeNote(repository: Repository, noteId: RealmUUID) {
		repository.getObjectFromIdAsFlow<NoteObject>(id = noteId).collectLatest { noteObject1 ->
			this@NoteViewModel._noteObject.tryEmit(noteObject1)
			setLocation(latLng = noteObject1?.getLatLng(), address = noteObject1?.address)
			noteObject1?.id?.let { this@NoteViewModel._attachmentList.tryEmit(repository.attachmentRepository.getAttachmentFromNote(parentId = it).map { AttachmentState.Saved(file = it) }) }
		}
	}

	@OptIn(ExperimentalCoroutinesApi::class)
	private suspend fun observeChapter(repository: Repository?) {
		this._noteObject.transformLatest { emit(repository?.getChapterFromId(it?.parentId)) }.collectLatest {
			this@NoteViewModel._parentChapter.tryEmit(it?.toLite())
		}
	}

	private suspend fun observeAllTags(repository: Repository?) {
		repository?.getAllTagAsFlow()?.collectLatest { tagList1 ->
			this@NoteViewModel._allTagsList.tryEmit(tagList1)
		}
	}

	private suspend fun observeConnectedTags() {
		combine(_noteObject, _allTagsList) { noteObject1, allTagList1 ->
			noteObject1?.let { noteObject2 -> allTagList1.filter { noteObject2.id in it.objectIdList }.associateWith { TagObjectState.Saved } } ?: mapOf()
		}.collectLatest { connectedTagMap ->
			this@NoteViewModel._tagStateMap.tryEmit(connectedTagMap)
		}
	}

	fun initNote(parentId: RealmUUID) {
		NoteObject().apply {
			this.parentId = parentId
			this@NoteViewModel._noteObject.tryEmit(this)
		}

		viewModelScope.launch(Dispatchers.IO) {
			dataStoreInstance.getGeolocation.collectLatest {
				if (it) reloadLocation()
				else _locationData.tryEmit(LocationData.AutoFetchDisabled)
				cancel()
			}
		}
	}

	fun getNote(noteId: RealmUUID) {
		this._noteId.tryEmit(noteId)
	}

	fun reloadLocation() {
		viewModelScope.launch(Dispatchers.IO) {
			getLocation(context = lockableRepo.context) { _locationData.tryEmit(it) }
		}
	}

	fun setUserTimestamp(timestamp : Long) {
		this@NoteViewModel._noteObject.value?.clone()?.let {
			it.userTimestamp = timestamp
			this@NoteViewModel._noteObject.tryEmit(it)
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

	fun addNewAttachment(toAddAttachmentList: List<Uri>) {
		this._attachmentList.value.toMutableList().apply {
			addAll(toAddAttachmentList.map { AttachmentState.New(uri = it) })
			this@NoteViewModel._attachmentList.tryEmit(toList())
		}
	}

	fun toggleAttachment(attachmentState: AttachmentState) {
		when (attachmentState) {
			is AttachmentState.Saved -> this._attachmentList.value.toMutableList().apply {
				remove(attachmentState)
				add(attachmentState.toRemove())
				this@NoteViewModel._attachmentList.tryEmit(toList())
			}

			is AttachmentState.New -> this._attachmentList.value.toMutableList().apply {
				remove(attachmentState)
				this@NoteViewModel._attachmentList.tryEmit(toList())
			}

			is AttachmentState.ToRemove -> this._attachmentList.value.toMutableList().apply {
				remove(attachmentState)
				add(attachmentState.toSave())
				this@NoteViewModel._attachmentList.tryEmit(toList())
			}
		}
	}

	fun save() {
		_repository.value?.let { repository ->
			repository.setObjectFromIdSuspended<NoteObject>(
				id = _noteId.value,
				insert = {
					Log.d("npr71", "insert")
					noteObject.value?.apply {
						this.updateModifyTimestamp()
						this.title = kitKatFormat.kitKatTitle
						this.content = kitKatFormat.kitKatContent?.drop(1)?.dropLast(1)
						Log.d("npr71", "content : ${this.content}")
						this.latLng = Json.encodeToString(this@NoteViewModel.locationData.value.getLatLngOrNull())
						this.address = this@NoteViewModel.locationData.value.getAddressOrNull()
						copyToRealm(this, UpdatePolicy.ALL)
						getNote(noteId = this.id)
					}
				},
				update = {
					Log.d("npr71", "update")
					this.updateModifyTimestamp()
					this@NoteViewModel.noteObject.value?.userTimestamp?.let { this.userTimestamp = it }
					this.title = kitKatFormat.kitKatTitle
					this.content = kitKatFormat.kitKatContent?.drop(1)?.dropLast(1)
					Log.d("npr71", "content : ${this.content}")
					this.latLng = Json.encodeToString(this@NoteViewModel.locationData.value.getLatLngOrNull())
					this.address = this@NoteViewModel.locationData.value.getAddressOrNull()
				}
			)

			noteObject.value?.id?.let { noteId1 ->
				repository.updateTagConnections(
					objectId = noteId1,
					tagListToAdd = tagStateMap.value.filterValues { it == TagObjectState.New }.keys.map { it.id },
					tagListToRemove = tagStateMap.value.filterValues { it == TagObjectState.ToRemove }.keys.map { it.id }
				)

				repository.attachmentRepository.putAttachment(parentId = noteId1, uriList = this.attachmentList.value.filterIsInstance<AttachmentState.New>().map { it.uri })
				repository.attachmentRepository.delete(attachmentList = this.attachmentList.value.filterIsInstance<AttachmentState.ToRemove>().map { it.file }.toSet())
			}
		}

	}

	fun toggleTag(tagObject: TagObject) {
		val tagState = this._tagStateMap.value[tagObject]
		when (tagState) {
			null -> this._tagStateMap.value.toMutableMap().apply { put(tagObject, TagObjectState.New); this@NoteViewModel._tagStateMap.tryEmit(toMap()) }
			TagObjectState.Saved -> this._tagStateMap.value.toMutableMap().apply { remove(tagObject); put(tagObject, TagObjectState.ToRemove); this@NoteViewModel._tagStateMap.tryEmit(toMap()) }
			TagObjectState.New -> this._tagStateMap.value.toMutableMap().apply { remove(tagObject); this@NoteViewModel._tagStateMap.tryEmit(toMap()) }
			TagObjectState.ToRemove -> this._tagStateMap.value.toMutableMap().apply { remove(tagObject); put(tagObject, TagObjectState.Saved); this@NoteViewModel._tagStateMap.tryEmit(toMap()) }
		}
	}

	fun toggleFavourite() {
		_repository.value?.setObjectFromIdSuspended<NoteObject>(id = _noteId.value) {
			this.updateModifyTimestamp()
			this.isFavourite = this.isFavourite.not()
		}
	}

	fun toggleLocked() {
		_repository.value?.setObjectFromIdSuspended<NoteObject>(id = _noteId.value) {
			this.updateModifyTimestamp()
			this.isLocked = this.isLocked.not()
		}
	}

	fun updateParent(parentId: RealmUUID) {
		_repository.value?.setObjectFromIdSuspended<NoteObject>(id = _noteId.value) {
			this.updateModifyTimestamp()
			this.parentId = parentId
		}
	}

	fun putTag(tag: String, color: Color): Boolean {
		return if (allTagsList.value.find { it.tag == tag } != null) {
			false
		}
		else {
			viewModelScope.launch(Dispatchers.Default) {
				TagObject().apply {
					this.tag = tag
					this.color = color.toArgb()

					_repository.value?.putTag(this)
				}
			}
			true
		}
	}

	fun delete() {
		_noteId.value?.let { _repository.value?.deleteSuspended(id = it) }
	}

	fun putKitKatFormat(kitKatFormat: KitKatFormat) {
		this@NoteViewModel.kitKatFormat = kitKatFormat
	}

	fun getKitKatFormat() = this@NoteViewModel.kitKatFormat


	companion object {
		sealed class TagObjectState {
			data object Saved : TagObjectState()
			data object New : TagObjectState()
			data object ToRemove : TagObjectState()
		}

		sealed class AttachmentState(val name : String?) {
			data class Saved(val file: File) : AttachmentState(name = file.name) {
				fun toRemove() = ToRemove(file = file)
			}

			data class New(val uri: Uri) : AttachmentState(name = uri.path)
			data class ToRemove(val file: File) : AttachmentState(name = file.name) {
				fun toSave() = Saved(file = file)
			}
		}
	}
}
