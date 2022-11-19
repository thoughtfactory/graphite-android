package com.syncodec.graphite.presentation.calendar

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syncodec.graphite.di.model.ChapterObject
import com.syncodec.graphite.di.model.NoteObjectLite
import com.syncodec.graphite.di.repository.RealmNotInitializedException
import com.syncodec.graphite.di.repository.Repository2
import com.syncodec.graphite.di.repository.RepositoryState
import dagger.hilt.android.lifecycle.HiltViewModel
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


@HiltViewModel
class CalendarViewModel  @Inject constructor(private val repository2 : Repository2) : ViewModel() {

	val repositoryState = repository2.repositoryState

	private val chapterObject : MutableState<ChapterObject?> = mutableStateOf(null)
	val noteList : SnapshotStateList<NoteObjectLite> = mutableStateListOf()

	fun loadAllData() {
		viewModelScope.launch(Dispatchers.IO) {
			repositoryState.collect {
				when (it) {
					RepositoryState.INIT -> Log.d("CalendarViewModel", "Init")
					RepositoryState.LOCKED -> null
					RepositoryState.LOADING -> Log.d("CalendarViewModel", "Loading")
					RepositoryState.SUCCESS -> {
						viewModelScope.launch {
							if (repositoryState.value != RepositoryState.SUCCESS) this.cancel()
							try {
								repository2.getAllNoteLiteAsFlow().collect {
									withContext(Dispatchers.Main) {
										noteList.clear()
										noteList.addAll(it)
									}
								}
							} catch (e : RealmNotInitializedException) {
							} catch (e : Exception) {
							}
						}
					}

					RepositoryState.ERROR -> Log.d("AttachmentViewModel", "Error")
				}
			}
		}
	}

	fun loadDataFromChapter(RealmUUID:RealmUUID) {
		viewModelScope.launch(Dispatchers.IO) {
			repositoryState.collect {
				when (it) {
					RepositoryState.INIT -> Log.d("CalendarViewModel", "Init")
					RepositoryState.LOCKED -> null
					RepositoryState.LOADING -> Log.d("CalendarViewModel", "Loading")
					RepositoryState.SUCCESS -> {
						viewModelScope.launch {
							if (repositoryState.value != RepositoryState.SUCCESS) this.cancel()
							try {
								repository2.getChapterFromIdAsFlow(RealmUUID).collect {
									withContext(Dispatchers.Main) { chapterObject.value = it }
									it?.noteList?.map { it.toLite() }?.let {
										withContext(Dispatchers.Main) {
											noteList.clear()
											noteList.addAll(it)
										}
									}
								}
							} catch (e : RealmNotInitializedException) {
							} catch (e : Exception) {
							}
						}
					}

					RepositoryState.ERROR -> Log.d("CalendarViewModel", "Error")
				}
			}
		}
	}
}
