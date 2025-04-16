package com.syncodec.graphite.presentation.bucket2.moveBucketItemScaffold

import androidx.lifecycle.ViewModel
import com.syncodec.graphite.di.modelObjectBox.BucketBoxDecrypted
import com.syncodec.graphite.di.modelObjectBox.BucketBoxEncrypted
import com.syncodec.graphite.di.secureRepository.BoxRepository
import kotlinx.coroutines.flow.Flow
import org.koin.android.annotation.KoinViewModel
import kotlin.uuid.ExperimentalUuidApi


@KoinViewModel
class MoveBucketItemViewModel(
    private val boxRepository: BoxRepository,
) : ViewModel() {

    val allBucketBoxListFlow: Flow<List<BoxRepository.Companion.CacheValue<BucketBoxEncrypted, BucketBoxDecrypted>>> = boxRepository.getAllBucketBoxAsFlow()

    fun putBucketBox(bucketBox: BucketBoxDecrypted) = boxRepository.putBucketBox(bucketBox = bucketBox)

    @OptIn(ExperimentalUuidApi::class)
    fun moveBucketItemBox(bucketBox: BucketBoxEncrypted, bucketItemIdList: List<Long>, callback: () -> Unit = {}) = boxRepository.moveBucketItemBox(parent = bucketBox, bucketItemIdList = bucketItemIdList, callback = callback)
}