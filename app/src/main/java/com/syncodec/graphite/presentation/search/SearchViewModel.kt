package com.syncodec.graphite.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syncodec.graphite.di.model.ChapterObjectLite
import com.syncodec.graphite.di.model.KitKatContent
import com.syncodec.graphite.di.model.NoteObject
import com.syncodec.graphite.di.model.NoteObjectLite
import com.syncodec.graphite.di.model.TagObject
import com.syncodec.graphite.di.repository.Repository
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel


@KoinViewModel
class SearchViewModel(repositoryStateFlow: MutableStateFlow<Repository.Companion.RepositoryStatus>) : ViewModel() {

	private val _repository: MutableStateFlow<Repository?> = MutableStateFlow(null)

	private val _noteMap: MutableStateFlow<Map<RealmUUID, NoteObject>> = MutableStateFlow(mapOf())

	private val _tagList: MutableStateFlow<Set<TagObject>> = MutableStateFlow(setOf())
	val tagList: StateFlow<Set<TagObject>> = _tagList

	private val _noteAttachmentCountMap: MutableStateFlow<Map<RealmUUID, Int>> = MutableStateFlow(mapOf())

	private val _filterChapterObject: MutableStateFlow<ChapterObjectLite?> = MutableStateFlow(null)
	val filterChapterObject: StateFlow<ChapterObjectLite?> = _filterChapterObject

	private val _filteredNoteList: MutableStateFlow<Set<NoteObjectLite>> = MutableStateFlow(setOf())
	val filteredNoteList: StateFlow<Set<NoteObjectLite>> = _filteredNoteList

	private val _noteFilterList: MutableStateFlow<Set<NoteFilter>> = MutableStateFlow(setOf())
	val noteFilterList: StateFlow<Set<NoteFilter>> = _noteFilterList

	private val _noteQueryCache: MutableMap<RealmUUID, Pair<String, Int>> = mutableMapOf()

	init {
		viewModelScope.launch(Dispatchers.Default) {
			repositoryStateFlow.collectLatest { repositoryStatus ->
				if (repositoryStatus is Repository.Companion.RepositoryStatus.Success) _repository.tryEmit(repositoryStatus.repository)
			}
		}

		viewModelScope.launch(Dispatchers.Default) {
			this@SearchViewModel._repository.collectLatest { repository1 ->
				launch {
					repository1?.getAllNoteAsFlow()?.collectLatest { noteList ->
						this@SearchViewModel._noteMap.tryEmit(noteList.associateBy { it.id })
					}
				}
				launch {
					repository1?.getAllTagAsFlow()?.collectLatest { tagList ->
						this@SearchViewModel._tagList.tryEmit(tagList.toSet())
					}
				}
			}
		}

		startFilter()
	}

	private fun startFilter() {
		viewModelScope.launch(Dispatchers.Default) {
			combine(
				_noteMap,
				_tagList,
				_filterChapterObject,
				_noteFilterList
			) { noteMap1, tagList1, filterChapterObject1, noteFilterList1 ->
				noteMap1
					.filterValues { noteObject -> filterChapterObject1 == null || filterChapterObject1.id == noteObject.parentId }
					.filterValues { noteObject -> noteFilterList1.all { applyNoteFilter(noteObject = noteObject, noteFilter = it, tagList = tagList1) } }
					.mapValues { it.value.toLite() }
					.values
			}.collectLatest { noteList ->
				this@SearchViewModel._filteredNoteList.tryEmit(noteList.toSet())
			}
		}
	}

	private fun applyNoteFilter(noteObject: NoteObject, noteFilter: NoteFilter, tagList : Set<TagObject>): Boolean {
		return when(noteFilter) {
			is NoteFilter.Favourite -> noteObject.isFavourite
			is NoteFilter.Locked -> noteObject.isLocked
			is NoteFilter.WithAttachment -> true
			is NoteFilter.Query -> checkIfQueryInNote(word = noteFilter.word, noteObject = noteObject)
			is NoteFilter.Tag -> tagList.find { it == noteFilter.tagObject }?.objectIdList?.contains(noteObject.id) ?: true
		}
	}

	private fun checkIfQueryInNote(word: String, noteObject: NoteObject): Boolean {
		val cachedData = _noteQueryCache[noteObject.id]
		return if (cachedData?.first == word && cachedData.second == noteObject.hashCode()) {
			true
		}
		else {
			try {
				val kitKatContent = KitKatContent.fromString(noteObject.content)
				kitKatContent?.toTxt()?.lowercase()?.contains(word.lowercase()) ?: false
			} catch (e: Exception) {
				e.printStackTrace()
				false
			}
		}
	}

	fun addFilter(noteFilter: NoteFilter) {
		this.noteFilterList.value.toMutableSet().apply {
			add(noteFilter)
			this@SearchViewModel._noteFilterList.tryEmit(toSet())
		}
	}

	fun removeFilter(noteFilter: NoteFilter) {
		this.noteFilterList.value.toMutableSet().apply {
			remove(noteFilter)
			this@SearchViewModel._noteFilterList.tryEmit(toSet())
		}
	}

	fun removeAllFilter() {
		this._noteFilterList.tryEmit(setOf())
	}

	companion object {
		sealed class NoteFilter {
			data object Favourite : NoteFilter()
			data object Locked : NoteFilter()
			data object WithAttachment : NoteFilter()
			data class Query(val word: String) : NoteFilter()
			data class Tag(val tagObject: TagObject) : NoteFilter()
		}
	}
}
