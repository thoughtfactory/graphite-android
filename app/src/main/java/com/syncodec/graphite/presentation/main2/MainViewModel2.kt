package com.syncodec.graphite.presentation.main2

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syncodec.graphite.di.modelObjectBox.ChapterBox
import com.syncodec.graphite.di.repository.repository.Repository
import com.syncodec.graphite.di.secureRepository.BoxRepository
import com.syncodec.graphite.di.sync.dropbox.DBox
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel


@KoinViewModel
class MainViewModel2(private val boxRepository: BoxRepository) : ViewModel() {

    val allChapterBoxListFlow = boxRepository.getAllChapterAsFlow()

    init {
        viewModelScope.launch(Dispatchers.Default) {
            Log.d("npr71", "chapterBoxSize : LoL")
            allChapterBoxListFlow.collect {
                Log.d("npr71", "chapterBoxSize : ${it.size}")
            }
        }
    }

    fun putChapter(chapterBox: ChapterBox) {
        boxRepository.putChapter(chapterBox)
    }

}