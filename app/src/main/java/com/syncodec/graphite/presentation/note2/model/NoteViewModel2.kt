package com.syncodec.graphite.presentation.note2.model

import android.util.Log
import androidx.lifecycle.ViewModel
import com.syncodec.graphite.di.model.NoteObject
import com.syncodec.graphite.di.repo.WrappedRepo
import com.syncodec.graphite.utils.DataStoreInstance
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.android.annotation.KoinViewModel


@KoinViewModel
class NoteViewModel2(
    private val wrappedRepo: WrappedRepo,
    private val dataStoreInstance: DataStoreInstance,
) : ViewModel() {

    private val _noteObjectFlow = MutableStateFlow<NoteObject?>(null)
    val noteObjectFlow = _noteObjectFlow.asStateFlow()


    fun initNote(parentId: RealmUUID) {
        Log.d(TAG, "initNote : $parentId")

        val newNoteObject = NoteObject().apply {
            this.parentId = parentId
        }

        this._noteObjectFlow.tryEmit(newNoteObject)

//        fun try get geolocation
    }

    fun readNote() {

    }

    companion object {
        const val TAG = "NoteViewModel2"
    }
}