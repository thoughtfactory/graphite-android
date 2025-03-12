package com.syncodec.graphite.presentation.main2

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syncodec.graphite.di.modelObjectBox.BucketBox
import com.syncodec.graphite.di.modelObjectBox.BucketBoxEnc
import com.syncodec.graphite.di.modelObjectBox.ChapterBox
import com.syncodec.graphite.di.modelObjectBox.DecryptedBox
import com.syncodec.graphite.di.secureRepository.BoxRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel


@KoinViewModel
class MainViewModel2(private val boxRepository: BoxRepository) : ViewModel() {

    val allChapterBoxListFlow: Flow<List<ChapterBox>> = boxRepository.getAllChapterBoxAsFlow()
    val allBucketBoxListFlow: Flow<List<suspend () -> DecryptedBox?>> = boxRepository.getAllBucketBoxAsFlow()

    init {
        viewModelScope.launch(context = Dispatchers.Default) {
            Log.d("npr71", "chapterBoxSize : LoL")
            allChapterBoxListFlow.collect {
                Log.d("npr71", "chapterBoxSize : ${it.size}")
            }
        }
    }

    fun putChapterBox(chapterBox: ChapterBox) = boxRepository.putChapterBox(chapterBox = chapterBox)

    fun putBucketBox(bucketBox: BucketBox) = boxRepository.putBucketBox(bucketBox = bucketBox)

}