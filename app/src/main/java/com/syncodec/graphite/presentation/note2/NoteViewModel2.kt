package com.syncodec.graphite.presentation.note2

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syncodec.graphite.di.model.ChapterObjectLite
import com.syncodec.graphite.di.model.LatLng
import com.syncodec.graphite.di.model.NoteObject
import com.syncodec.graphite.di.model.TagObject
import com.syncodec.graphite.di.repository.Repository
import com.syncodec.graphite.utils.Location.getLocation
import com.syncodec.graphite.utils.LocationData
import io.realm.kotlin.UpdatePolicy
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.android.annotation.KoinViewModel
import java.io.File


@KoinViewModel
class NoteViewModel2(repositoryStatusStateFlow: MutableStateFlow<Repository.Companion.RepositoryStatus>) : ViewModel() {

	private val _repository: MutableStateFlow<Repository?> = MutableStateFlow(null)

	private val _noteId: MutableStateFlow<RealmUUID?> = MutableStateFlow(null)
//	val noteId: StateFlow<RealmUUID?> = _noteId

	private val _noteObject: MutableStateFlow<NoteObject?> = MutableStateFlow(null)
	val noteObject: StateFlow<NoteObject?> = _noteObject

	private val _locationData: MutableStateFlow<LocationData> = MutableStateFlow(LocationData.Init)
	val locationData: StateFlow<LocationData> = _locationData

//	private val _parentId: MutableStateFlow<RealmUUID?> = MutableStateFlow(null)

	private val _parentChapter: MutableStateFlow<ChapterObjectLite?> = MutableStateFlow(null)
	val parentChapter: StateFlow<ChapterObjectLite?> = _parentChapter

	//	Attachments
	private val _savedFileList: MutableStateFlow<List<File>> = MutableStateFlow(listOf())
	val savedFileList: StateFlow<List<File>> = _savedFileList

	private val _newFileList: MutableStateFlow<List<Uri>> = MutableStateFlow(listOf())
	val newFileList: StateFlow<List<Uri>> = _newFileList

	//	Tags
	private val _allTagsList: MutableStateFlow<List<TagObject>> = MutableStateFlow(listOf())
	val allTagsList: StateFlow<List<TagObject>> = _allTagsList

	private val _tagStateMap: MutableStateFlow<Map<TagObject, TagObjectState>> = MutableStateFlow(mapOf())
	val tagStateMap: StateFlow<Map<TagObject, TagObjectState>> = _tagStateMap

	val isEditing: MutableStateFlow<Boolean?> = MutableStateFlow(null)
	val kitKatFormat: MutableStateFlow<KitKat.Companion.KitKatFormat?> = MutableStateFlow(null)

	init {
		viewModelScope.launch(Dispatchers.Default) {
			repositoryStatusStateFlow.collectLatest { repositoryStatus ->
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
		repository.getNoteFromIdAsFlow(id = noteId).cancellable().collectLatest { noteObject1 ->
			this@NoteViewModel2._noteObject.tryEmit(noteObject1)
			setLocation(latLng = noteObject1?.getLatLng(), address = noteObject1?.address)
			noteObject1?.id?.let { this@NoteViewModel2._savedFileList.tryEmit(repository.attachmentRepository.getAttachmentFromNote(parentId = it)) }
		}
	}

	@OptIn(ExperimentalCoroutinesApi::class)
	private suspend fun observeChapter(repository: Repository?) {
		this._noteObject.transformLatest { emit(repository?.getChapterFromId(it?.parentId)) }.collectLatest {
			this@NoteViewModel2._parentChapter.tryEmit(it?.toLite())
		}
	}

	private suspend fun observeAllTags(repository: Repository?) {
		repository?.getAllTagAsFlow()?.cancellable()?.collectLatest { tagList1 ->
			this@NoteViewModel2._allTagsList.tryEmit(tagList1)
		}
	}

	private suspend fun observeConnectedTags() {
		combine(_noteObject, _allTagsList) { noteObject1, allTagList1 ->
			noteObject1?.let { noteObject2 -> allTagList1.filter { noteObject2.id in it.objectIdList }.associateWith { TagObjectState.Saved } } ?: mapOf()
		}.collectLatest { connectedTagMap ->
			this@NoteViewModel2._tagStateMap.tryEmit(connectedTagMap)
		}
	}

	fun initNote(parentId: RealmUUID) {
		NoteObject().apply {
			this.parentId = parentId
			this@NoteViewModel2._noteObject.tryEmit(this)
		}

		reloadLocation()
	}

	fun getNote(noteId: RealmUUID) {
		this._noteId.tryEmit(noteId)
	}

	fun reloadLocation() {
		viewModelScope.launch(Dispatchers.IO) {
			_repository.value?.context?.let { getLocation(context = it) { _locationData.tryEmit(it) } }
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
		val updatedNewFileList = _newFileList.value.toMutableList().apply { addAll(fileList) }
		_newFileList.tryEmit(updatedNewFileList)
	}

	fun save() {
		_repository.value?.setObjectFromIdSuspended<NoteObject>(
			id = _noteId.value,
			insert = {
				Log.d("npr71", "insert")
				noteObject.value?.apply {
					this.updateModifyTimestamp()
					this.title = kitKatFormat.value?.kitKatTitle
					this.content = kitKatFormat.value?.kitKatContent
					this.latLng = Json.encodeToString(this@NoteViewModel2.locationData.value.getLatLngOrNull())
					this.address = this@NoteViewModel2.locationData.value.getAddressOrNull()
					copyToRealm(this, UpdatePolicy.ALL)
					getNote(noteId = this.id)
				}
			}
		) {
			Log.d("npr71", "update")
			this.updateModifyTimestamp()
			this.title = kitKatFormat.value?.kitKatTitle
			this.content = kitKatFormat.value?.kitKatContent
			this.latLng = Json.encodeToString(this@NoteViewModel2.locationData.value.getLatLngOrNull())
			this.address = this@NoteViewModel2.locationData.value.getAddressOrNull()
		}

		noteObject.value?.id?.let {
			_repository.value?.updateTagConnections(
				id = it,
				tagListToAdd = tagStateMap.value.filterValues { it == TagObjectState.New }.keys.map { it.id },
				tagListToRemove = tagStateMap.value.filterValues { it == TagObjectState.ToRemove }.keys.map { it.id }
			)

			_repository.value?.attachmentRepository?.putAttachment(parentId = it, uriList = newFileList.value, keepName = true)
		}
	}

	fun toggleTag(tagObject: TagObject) {
		val tagState = this._tagStateMap.value[tagObject]
		when (tagState) {
			null -> this._tagStateMap.value.toMutableMap().apply { put(tagObject, TagObjectState.New); this@NoteViewModel2._tagStateMap.tryEmit(toMap()) }
			TagObjectState.Saved -> this._tagStateMap.value.toMutableMap().apply { remove(tagObject); put(tagObject, TagObjectState.ToRemove); this@NoteViewModel2._tagStateMap.tryEmit(toMap()) }
			TagObjectState.New -> this._tagStateMap.value.toMutableMap().apply { remove(tagObject); this@NoteViewModel2._tagStateMap.tryEmit(toMap()) }
			TagObjectState.ToRemove -> this._tagStateMap.value.toMutableMap().apply { remove(tagObject); put(tagObject, TagObjectState.Saved); this@NoteViewModel2._tagStateMap.tryEmit(toMap()) }
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

	companion object {
		sealed class TagObjectState {
			data object Saved : TagObjectState()
			data object New : TagObjectState()
			data object ToRemove : TagObjectState()
		}
	}
}
