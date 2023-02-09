package com.syncodec.graphite.presentation.note

import androidx.lifecycle.ViewModel
import com.syncodec.graphite.di.repository.koinRepository.KoinRepository
import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.android.annotation.KoinViewModel


@KoinViewModel
class NoteViewModel(private val repository : KoinRepository) : ViewModel() {

	val isEditing = MutableStateFlow<Boolean?>(null)

}
