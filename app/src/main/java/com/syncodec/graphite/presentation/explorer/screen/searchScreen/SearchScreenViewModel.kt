package com.syncodec.graphite.presentation.explorer.screen.searchScreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syncodec.graphite.di.model.ChapterObjectLite
import com.syncodec.graphite.di.model.NoteObject
import com.syncodec.graphite.di.model.NoteObjectLite
import com.syncodec.graphite.di.model.TagObject
import com.syncodec.graphite.di.repository.repository.Repository
import com.syncodec.graphite.utils.LoaderStatus
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel


@KoinViewModel
class SearchScreenViewModel(private val repository : Repository) : ViewModel() {

	val repositoryState = repository.repositoryState
	val loaderStatus : MutableStateFlow<LoaderStatus> = MutableStateFlow(LoaderStatus.Init)

	val tagList : MutableStateFlow<List<TagObject>> = MutableStateFlow(listOf())
	private val noteList : MutableStateFlow<List<NoteObject>> = MutableStateFlow(listOf())
	val filteredNoteList : MutableStateFlow<List<NoteObjectLite>> = MutableStateFlow(listOf())

	val searchFilterType : MutableStateFlow<SearchFilterType> = MutableStateFlow(SearchFilterType.None)

	val searchInChapter : MutableStateFlow<ChapterObjectLite?> = MutableStateFlow(null)

	init {
		observeFilter()

		viewModelScope.launch(Dispatchers.Default) {
			repositoryState.collect {
				if (it == Repository.Companion.RepositoryState.Success) {
					viewModelScope.launch(Dispatchers.Default) {
						repository.getAllTagAsFlow().collect { tagList ->
							this@SearchScreenViewModel.tagList.tryEmit(tagList)
						}
					}
					viewModelScope.launch(Dispatchers.Default) {
						repository.getAllNoteAsFlow().collect { noteList ->
							this@SearchScreenViewModel.noteList.tryEmit(noteList)
						}
					}
				}
			}
		}
	}

	private fun observeFilter() {
		viewModelScope.launch(Dispatchers.Default) {
			combine(
				noteList,
				searchFilterType,
				searchInChapter
			) { noteList, searchFilterType, searchInChapter ->
				filterNoteList(
					noteList = noteList,
					searchFilterType = searchFilterType,
					searchInChapter = searchInChapter
				)
			}.collect()
		}
	}

	private fun filterNoteList(
		noteList : List<NoteObject>,
		searchFilterType : SearchFilterType,
		searchInChapter : ChapterObjectLite? = null
	) {
		this@SearchScreenViewModel.loaderStatus.tryEmit(LoaderStatus.Loading)

		when (searchFilterType) {
			is SearchFilterType.None -> this@SearchScreenViewModel.loaderStatus.tryEmit(LoaderStatus.Init)
			is SearchFilterType.Favourite -> noteList.filter { it.isFavourite && if (searchInChapter != null) it.parentId == searchInChapter.id else true }
				.map { it.toLite() }.let {
					this@SearchScreenViewModel.filteredNoteList.tryEmit(it)
					if (it.isEmpty()) this@SearchScreenViewModel.loaderStatus.tryEmit(LoaderStatus.LoadedEmpty)
					else this@SearchScreenViewModel.loaderStatus.tryEmit(LoaderStatus.Loaded)
				}

			is SearchFilterType.WithAttachment -> noteList.filter { repository.attachmentRepository.haveAttachment(parentId = it.id) && if (searchInChapter != null) it.parentId == searchInChapter.id else true }
				.map { it.toLite() }.let {
					this@SearchScreenViewModel.filteredNoteList.tryEmit(it)
					if (it.isEmpty()) this@SearchScreenViewModel.loaderStatus.tryEmit(LoaderStatus.LoadedEmpty)
					else this@SearchScreenViewModel.loaderStatus.tryEmit(LoaderStatus.Loaded)
				}

			is SearchFilterType.Locked -> noteList.filter { it.isLocked && if (searchInChapter != null) it.parentId == searchInChapter.id else true }
				.map { it.toLite() }.let {
					this@SearchScreenViewModel.filteredNoteList.tryEmit(it)
					if (it.isEmpty()) this@SearchScreenViewModel.loaderStatus.tryEmit(LoaderStatus.LoadedEmpty)
					else this@SearchScreenViewModel.loaderStatus.tryEmit(LoaderStatus.Loaded)
				}

			is SearchFilterType.Tag -> noteList.filter { it.id in searchFilterType.tag.objectIdList && if (searchInChapter != null) it.parentId == searchInChapter.id else true }
				.map { it.toLite() }.let {
					this@SearchScreenViewModel.filteredNoteList.tryEmit(it)
					if (it.isEmpty()) this@SearchScreenViewModel.loaderStatus.tryEmit(LoaderStatus.LoadedEmpty)
					else this@SearchScreenViewModel.loaderStatus.tryEmit(LoaderStatus.Loaded)
				}

			is SearchFilterType.Query -> noteList.filter {
				(it.title?.contains(searchFilterType.query, true) ?: false) ||
						(it.content?.contains(searchFilterType.query, true) ?: false) &&
						if (searchInChapter != null) it.parentId == searchInChapter.id else true
			}
				.map { it.toLite() }.let {
					this@SearchScreenViewModel.filteredNoteList.tryEmit(it)
					if (it.isEmpty()) this@SearchScreenViewModel.loaderStatus.tryEmit(LoaderStatus.LoadedEmpty)
					else this@SearchScreenViewModel.loaderStatus.tryEmit(LoaderStatus.Loaded)
				}
		}
	}

	fun setSearchInChapter(chapter : ChapterObjectLite?) = searchInChapter.tryEmit(chapter)

	fun clearFilter() = searchFilterType.tryEmit(SearchFilterType.None)

	fun filterFavourite() = searchFilterType.tryEmit(SearchFilterType.Favourite)

	fun filterWithAttachment() = searchFilterType.tryEmit(SearchFilterType.WithAttachment)

	fun filterLocked() = searchFilterType.tryEmit(SearchFilterType.Locked)

	fun filterTag(tag : TagObject) = searchFilterType.tryEmit(SearchFilterType.Tag(tag))

	fun filterTag(tagId : RealmUUID) {
		viewModelScope.launch(Dispatchers.Default) {
			repository.getTagFromId(tagId)?.let { filterTag(it) }
		}
	}

	fun filterQuery(query : String) = searchFilterType.tryEmit(SearchFilterType.Query(query))

	fun delete(idList : List<RealmUUID>) {
		repository.deleteSuspended(idList)
	}

	companion object {
		sealed class SearchFilterType {
			object None : SearchFilterType()
			object Favourite : SearchFilterType()
			object WithAttachment : SearchFilterType()
			object Locked : SearchFilterType()
			data class Tag(val tag : TagObject) : SearchFilterType()
			data class Query(val query : String) : SearchFilterType()
		}

	}
}
