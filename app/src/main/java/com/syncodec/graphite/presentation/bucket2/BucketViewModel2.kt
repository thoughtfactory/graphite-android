package com.syncodec.graphite.presentation.bucket2

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syncodec.graphite.di.modelObjectBox.BucketBox
import com.syncodec.graphite.di.modelObjectBox.BucketItemBox
import com.syncodec.graphite.di.modelObjectBox.customObject.BucketItemData
import com.syncodec.graphite.di.secureRepository.BoxRepository
import com.syncodec.graphite.utils.DataLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import kotlin.random.Random


@KoinViewModel
class BucketViewModel2(private val boxRepository: BoxRepository) : ViewModel() {

    private val _bucketBoxDataFlow: MutableStateFlow<DataLoader<BucketBox>> = MutableStateFlow(value = DataLoader.Init)
    val bucketBoxDataFlow: StateFlow<DataLoader<BucketBox>> = this._bucketBoxDataFlow.asStateFlow()

    private val _bucketItemBoxListDataFlow: MutableStateFlow<DataLoader<BucketItemBoxGroup>> = MutableStateFlow(value = DataLoader.Init)
    val bucketItemBoxGroupDataFlow: StateFlow<DataLoader<BucketItemBoxGroup>> = this._bucketItemBoxListDataFlow.asStateFlow()

    fun loadData(bucketId: Long) {
        viewModelScope.launch(context = Dispatchers.Default) {
            try {
                boxRepository.getBucketBoxAsFlow(id = bucketId).collectLatest { bucketBox ->
                    if (bucketBox == null) this@BucketViewModel2._bucketBoxDataFlow.tryEmit(value = DataLoader.NoData)
                    else this@BucketViewModel2._bucketBoxDataFlow.tryEmit(value = DataLoader.Loaded(data = bucketBox))
                }
            } catch (e: Exception) {
                this@BucketViewModel2._bucketBoxDataFlow.tryEmit(value = DataLoader.Error(message = "Error loading data", exception = e))
            }
        }

        viewModelScope.launch(context = Dispatchers.Default) {
            try {

                combine(flow = bucketBoxDataFlow, flow2 = boxRepository.getBucketItemBoxListAsFlow(parentId = bucketId)) { bucketBoxData, bucketItemBoxList ->
                    val sortedIdList = (bucketBoxData as? DataLoader.Loaded)?.data?.sortedIdList ?: listOf()
                    val bucketItemBoxMap = bucketItemBoxList.associateBy { it.id }
                    sortedIdList.mapNotNull { bucketItemBoxMap[it] } + bucketItemBoxMap.keys.minus(elements = sortedIdList).mapNotNull { bucketItemBoxMap[it] }
                }.collectLatest { bucketItemBoxList ->
                    val bucketItemBoxGroup = BucketItemBoxGroup(
                        alpha = bucketItemBoxList.filter { it.bucketItemData?.state == BucketItemData.State.Alpha },
                        beta = bucketItemBoxList.filter { it.bucketItemData?.state == BucketItemData.State.Beta },
                        gamma = bucketItemBoxList.filter { it.bucketItemData?.state == BucketItemData.State.Gamma },
                        other = bucketItemBoxList.filter { it.bucketItemData?.state == null },
                        allOrdered = bucketItemBoxList
                    )
                    if (bucketItemBoxList.isEmpty()) this@BucketViewModel2._bucketItemBoxListDataFlow.tryEmit(value = DataLoader.NoData)
                    else this@BucketViewModel2._bucketItemBoxListDataFlow.tryEmit(value = DataLoader.Loaded(data = bucketItemBoxGroup, hash = Random.nextInt()))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                this@BucketViewModel2._bucketItemBoxListDataFlow.tryEmit(value = DataLoader.Error(message = "Error loading data", exception = e))
            }
        }
    }

    fun updateBucketBox(bucketBox: BucketBox) = boxRepository.putBucketBox(bucketBox = bucketBox)

    fun putBucketItemBox(bucketItemBox: BucketItemBox, parent: BucketBox) = boxRepository.putBucketItemBox(bucketItemBox = bucketItemBox, parent = parent)

    fun updateBucketItemBoxState(bucketItemBox: BucketItemBox, newState: BucketItemData.State) = boxRepository.updateBucketItemBoxState(bucketItemBox = bucketItemBox, newState = newState)

    fun onUpdateBucketItemOrder(bucketBox: BucketBox, bucketItemBoxIdOrder: List<Long>) = boxRepository.onUpdateBucketItemOrder(bucketBox = bucketBox, bucketItemBoxIdOrder = bucketItemBoxIdOrder)

    fun onToggleLock(newState: Boolean) {
        val bucketBox = (this.bucketBoxDataFlow.value as? DataLoader.Loaded)?.data?.apply { this.isLocked = newState }
        boxRepository.putBucketBox(bucketBox = bucketBox ?: return)
    }

    fun onToggleFavourite(newState: Boolean) {
        val bucketBox = (this.bucketBoxDataFlow.value as? DataLoader.Loaded)?.data?.apply { this.isFavourite = newState }
        boxRepository.putBucketBox(bucketBox = bucketBox ?: return)
    }

    fun addTestData(bucketType: BucketBox.BucketType?, bucketBox: BucketBox) {
        viewModelScope.launch(context = Dispatchers.Default) {
            repeat(times = 50) {
                boxRepository.putBucketItemBox(BucketItemBox.randomLink, parent = bucketBox)
            }
        }
    }

    data class BucketItemBoxGroup(
        val alpha: List<BucketItemBox>,
        val beta: List<BucketItemBox>,
        val gamma: List<BucketItemBox>,
        val other: List<BucketItemBox>,
        val allOrdered: List<BucketItemBox>
    ) {
        val all
            get() = alpha + beta + gamma + other
    }
}