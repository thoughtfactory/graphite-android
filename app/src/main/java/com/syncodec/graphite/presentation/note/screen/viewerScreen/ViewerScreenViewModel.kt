package com.syncodec.graphite.presentation.note.screen.viewerScreen

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syncodec.graphite.di.model.ChapterObject
import com.syncodec.graphite.di.model.ChapterObjectLite
import com.syncodec.graphite.di.model.LatLng
import com.syncodec.graphite.di.model.NoteObject
import com.syncodec.graphite.di.model.TagObjectLite
import com.syncodec.graphite.di.repository.repository.Repository
import com.syncodec.graphite.utils.decodeBase64ToBitmap
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import java.time.Instant


@KoinViewModel
class ViewerScreenViewModel(private val repository : Repository) : ViewModel() {

	val repositoryState = repository.repositoryState
	val noteId = MutableStateFlow<RealmUUID?>(null)

	/** Show loading animation and seize new events when set. */
	val isOperationPending = MutableStateFlow(false)

	/** Cancelled and reassigned when [setupViewer] is called. This is scoped to the ViewModel.*/
	private var noteReaderCoroutine : CoroutineScope? = null

	/**
	 * Use [isReady] to show loading screen.
	 */
	val isReady = MutableStateFlow(false)

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
	val isFavourite : MutableStateFlow<Boolean?> = MutableStateFlow(null)
	val isLocked : MutableStateFlow<Boolean?> = MutableStateFlow(null)
	val parentId : MutableStateFlow<RealmUUID?> = MutableStateFlow(null)
	val parentChapter : MutableStateFlow<ChapterObject?> = MutableStateFlow(null)

	val tagList : MutableStateFlow<List<TagObjectLite>> = MutableStateFlow(emptyList())

	init {
		initObserver()
	}

	/** Observes [repositoryState] and [noteId] and calls [setupViewer] when [repositoryState] is [RepositoryState.Success] are [noteId] is not null.*/
	private fun initObserver() {
		viewModelScope.launch(Dispatchers.Default) {
			combine(repositoryState, noteId) { repositoryState, noteId ->
				repositoryState to noteId
			}.collect { (repositoryState, noteId) ->
				if (repositoryState == Repository.Companion.RepositoryState.Success) noteId?.let {
					setupViewer(it)
					loadTags(it)
				}
			}
		}
	}

	/**
	 * Sets [noteId] which will trigger [setupViewer] to be called on [noteId] value change.
	 * @author pushpull
	 * @since 2.2.0
	 * @param noteId The note id to set.
	 * @return True if [noteId] was set, false if value transmission is suspended by [MutableStateFlow].
	 */
	fun loadNote(noteId : RealmUUID) = this.noteId.tryEmit(noteId)

	private fun loadTags(noteId : RealmUUID) {
		viewModelScope.launch(Dispatchers.Default) {
			repository.getAllTagAsFlow().collect { tagList ->
				val connectedTagList = mutableListOf<TagObjectLite>()
				tagList.forEach { tagObject -> if (tagObject.objectIdList.contains(noteId)) connectedTagList.add(tagObject.toLite()) }
				this@ViewerScreenViewModel.tagList.tryEmit(connectedTagList)
			}
		}
	}

	private fun setupViewer(noteId : RealmUUID) {
		viewModelScope.launch(Dispatchers.Default) {
			isReady.tryEmit(false)
			noteReaderCoroutine?.cancel()
			noteReaderCoroutine = this
			repository.getNoteFromIdAsFlow(id = noteId).collect { noteObject ->
				noteObject?.let {
					this@ViewerScreenViewModel.createdTimestamp.tryEmit(it.createdTimestamp)
					this@ViewerScreenViewModel.modifiedTimestamp.tryEmit(it.modifiedTimestamp)
					this@ViewerScreenViewModel.userTimestamp.tryEmit(it.userTimestamp)
					this@ViewerScreenViewModel.title.tryEmit(it.title)
					this@ViewerScreenViewModel.color.tryEmit(it.color)
					this@ViewerScreenViewModel.latLng.tryEmit(it.getLatLng())
					this@ViewerScreenViewModel.address.tryEmit(it.address)
					this@ViewerScreenViewModel.contentThumbnail.tryEmit(it.contentThumbnail)
					this@ViewerScreenViewModel.content.tryEmit(it.content)
					this@ViewerScreenViewModel.thumbnail.tryEmit(it.thumbnail?.decodeBase64ToBitmap())
					this@ViewerScreenViewModel.isFavourite.tryEmit(it.isFavourite)
					this@ViewerScreenViewModel.isLocked.tryEmit(it.isLocked)
					this@ViewerScreenViewModel.parentId.tryEmit(it.parentId)
					it.parentId?.let { repository.getChapterFromId(id = it).let { parentChapter.tryEmit(it) } }

					this@ViewerScreenViewModel.isReady.tryEmit(true)
				}
			}
		}
	}

	private fun putNote() {
		isOperationPending.tryEmit(true)
		viewModelScope.launch(Dispatchers.Default) {
			NoteObject().apply {
				this@ViewerScreenViewModel.noteId.value?.let { this.id = it } ?: run { this@ViewerScreenViewModel.noteId.tryEmit(this.id) }
				this@ViewerScreenViewModel.createdTimestamp.value?.let { this.createdTimestamp = it }
				this.modifiedTimestamp = Instant.now().toEpochMilli()
				this@ViewerScreenViewModel.userTimestamp.value?.let { this.userTimestamp = it }
				this@ViewerScreenViewModel.title.value?.let { this.title = it }
				this@ViewerScreenViewModel.color.value?.let { this.color = it }
				this@ViewerScreenViewModel.latLng.value?.let { this.setLatLng(it) }
				this@ViewerScreenViewModel.address.value?.let { this.address = it }
				this@ViewerScreenViewModel.contentThumbnail.value?.let { this.contentThumbnail = it }
				this@ViewerScreenViewModel.content.value?.let { this.content = it }
//				this@EditorScreenViewModel.thumbnail.value?.let { this.thumbnail = it }
				this@ViewerScreenViewModel.isFavourite.value?.let { this.isFavourite = it }
				this@ViewerScreenViewModel.isLocked.value?.let { this.isLocked = it }
				this@ViewerScreenViewModel.parentChapter.value?.id?.let { this.parentId = it }

				observeSaveOperation(toSaveNoteObject = this)
				repository.putNote(noteObject = this)
			}
		}
	}

	/** Observes if a new note is saved.*/
	private fun observeSaveOperation(toSaveNoteObject : NoteObject) {
		viewModelScope.launch(Dispatchers.Default) {
			repository.getNoteFromIdAsFlow(id = toSaveNoteObject.id).collect { noteObject ->
				isOperationPending.tryEmit(false)
				if (noteObject == toSaveNoteObject) cancel()
			}
		}
	}

	fun toggleFavourite() {
		this.isFavourite.tryEmit(this.isFavourite.value?.not() ?: true)
		putNote()
	}

	fun toggleLock() {
		this.isLocked.tryEmit(this.isLocked.value?.not() ?: true)
		putNote()
	}

	fun setUserTimestamp(timestamp : Long) {
		this.userTimestamp.tryEmit(timestamp)
		putNote()
	}

	fun setParentChapter(chapterObjectLite : ChapterObjectLite) {
		viewModelScope.launch(Dispatchers.Default) {
			repository.getChapterFromId(id = chapterObjectLite.id).let {
				this@ViewerScreenViewModel.parentChapter.tryEmit(it)
				putNote()
			}
		}
	}

	fun deleteNote(id : RealmUUID, callback : suspend () -> Unit) {
		isOperationPending.tryEmit(true)
		repository.deleteSuspended(id = id, callback = callback)
	}
}
