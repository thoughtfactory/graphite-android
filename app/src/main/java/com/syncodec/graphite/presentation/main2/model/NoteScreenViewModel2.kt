package com.syncodec.graphite.presentation.main2.model

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syncodec.graphite.di.model.NoteObject
import com.syncodec.graphite.di.repo.WrappedRepo
import io.realm.kotlin.query.RealmResults
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel


@KoinViewModel
class NoteScreenViewModel2(
    private val wrappedRepo: WrappedRepo,
) : ViewModel() {

    val defaultChapterIdFlow = wrappedRepo.emptyOrDataFlow { getDefaultChapterIdAsFlow() }.flowOn(Dispatchers.Default)

    @OptIn(ExperimentalCoroutinesApi::class)
    val noteListFlow: MutableStateFlow<List<NoteObject>> = MutableStateFlow(listOf())

    init {
        Log.d(TAG, "init")
        viewModelScope.launch(Dispatchers.Default) {
            wrappedRepo.emptyOrDataFlow { getNotesFromDefaultChapterAsFlow() }.collectLatest {
                this@NoteScreenViewModel2.noteListFlow.tryEmit(it.list)
            }
        }
    }

    companion object {
        const val TAG = "NoteScreenViewModel2"
    }
}
