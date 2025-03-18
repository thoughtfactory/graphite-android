package com.syncodec.graphite.presentation.bucket2

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syncodec.graphite.di.modelObjectBox.BucketBox
import com.syncodec.graphite.di.modelObjectBox.BucketItemBox
import com.syncodec.graphite.di.modelObjectBox.BucketItemBoxPlain
import com.syncodec.graphite.di.modelObjectBox.customObject.BucketItemData
import com.syncodec.graphite.di.modelObjectBox.encryptable.EncryptedBoolean
import com.syncodec.graphite.di.secureRepository.BoxRepository
import com.syncodec.graphite.utils.DataLoader
import com.syncodec.graphite.utils.alice2.Alice2
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.annotation.KoinViewModel
import kotlin.random.Random
import kotlin.time.measureTime


@KoinViewModel
class BucketViewModel2(
    private val boxRepository: BoxRepository,
    private val alice2: Alice2,
) : ViewModel() {

    private val _bucketBoxDataFlow: MutableStateFlow<DataLoader<BucketBox>> = MutableStateFlow(value = DataLoader.Init)
    val bucketBoxDataFlow: StateFlow<DataLoader<BucketBox>> = this._bucketBoxDataFlow.asStateFlow()

    private val _bucketItemBoxListDataFlow: MutableStateFlow<DataLoader<List<BucketItemBoxPlain>>> = MutableStateFlow(value = DataLoader.Init)
    val bucketItemBoxListDataFlow: StateFlow<DataLoader<List<BucketItemBoxPlain>>> = this._bucketItemBoxListDataFlow.asStateFlow()

    private val _bucketItemBoxListOrderDataFlow: MutableStateFlow<DataLoader<List<Long>>>  = MutableStateFlow(value = DataLoader.Init)
    val bucketItemBoxListOrderDataFlow: StateFlow<DataLoader<List<Long>>>  = this._bucketItemBoxListOrderDataFlow.asStateFlow()

    private val _bucketItemBoxGroupDataFlow: MutableStateFlow<DataLoader<BucketItemBoxGroup>> = MutableStateFlow(value = DataLoader.Init)
    val bucketItemBoxGroupDataFlow: StateFlow<DataLoader<BucketItemBoxGroup>> = this._bucketItemBoxGroupDataFlow.asStateFlow()

    private val _bucketItemBoxOrderedDataFlow: MutableStateFlow<DataLoader<BucketItemBoxOrderedData>> = MutableStateFlow(value = DataLoader.Init)
    val bucketItemBoxOrderedDataFlow: StateFlow<DataLoader<BucketItemBoxOrderedData>> = this._bucketItemBoxOrderedDataFlow.asStateFlow()

    fun loadData(bucketId: Long) {
        viewModelScope.launch(context = Dispatchers.IO) {
            try {
                boxRepository.getBucketBoxAsFlow(id = bucketId).collectLatest { bucketBox ->
                    if (bucketBox == null) this@BucketViewModel2._bucketBoxDataFlow.tryEmit(value = DataLoader.NoData)
                    else this@BucketViewModel2._bucketBoxDataFlow.tryEmit(value = DataLoader.Loaded(data = bucketBox))

                    withContext(Dispatchers.Default) {
                        TODO()
                        this@BucketViewModel2._bucketItemBoxListOrderDataFlow.tryEmit(value = DataLoader.Loaded(data = bucketBox?.sortedIdList?.decrypt(alice2) ?: listOf()))
                    }
                }
            } catch (e: Exception) {
                this@BucketViewModel2._bucketBoxDataFlow.tryEmit(value = DataLoader.Error(message = "Error loading data", exception = e))
            }
        }

        viewModelScope.launch(context = Dispatchers.IO) {
            boxRepository
                .getBucketItemBoxListAsFlow(parentId = bucketId)
                .collectLatest { bucketItemBoxList ->
                    withContext(Dispatchers.Default) {
                        this@BucketViewModel2._bucketItemBoxListDataFlow.tryEmit(value = DataLoader.Loaded(data = bucketItemBoxList.map { it.decrypt(alice2) }))
                    }
                }
        }

        viewModelScope.launch(context = Dispatchers.IO) {
            boxRepository
                .getBucketItemBoxListAsFlow(parentId = bucketId)
                .combine(flow = bucketBoxDataFlow) { bucketItemBoxList, bucketBoxData ->

                    var a: BucketItemBoxOrderedData
                    val m = measureTime {
                        val allIdSet = bucketItemBoxList.map { it.id }.toSet()
                        val savedIdList = (bucketBoxData as? DataLoader.Loaded)?.data?.sortedIdList?.decrypt(alice2) ?: listOf()

                        val orderedIdList = savedIdList + allIdSet.minus(savedIdList)

                        a = BucketItemBoxOrderedData(orderedIdList = orderedIdList, bucketItemBoxMap = bucketItemBoxList.associate { it.id to it.decrypt(alice2) })
                    }
                    Log.d(TAG, m.toString())
                    a
                }.flowOn(Dispatchers.IO)
                .collectLatest { bucketItemBoxOrderedData ->
                    if (bucketItemBoxOrderedData.bucketItemBoxMap.isEmpty()) this@BucketViewModel2._bucketItemBoxOrderedDataFlow.tryEmit(value = DataLoader.NoData)
                    else this@BucketViewModel2._bucketItemBoxOrderedDataFlow.tryEmit(value = DataLoader.Loaded(data = bucketItemBoxOrderedData, hash = Random.nextInt()))
                }
        }

//        viewModelScope.launch(context = Dispatchers.Default) {
//            try {
//                combine(flow = bucketBoxDataFlow, flow2 = boxRepository.getBucketItemBoxListAsFlow(parentId = bucketId)) { bucketBoxData, bucketItemBoxList ->
//                    val sortedIdList = (bucketBoxData as? DataLoader.Loaded)?.data?.sortedIdList?.decrypt(alice2) ?: listOf()
//                    val bucketItemBoxMap = bucketItemBoxList.associateBy { it.id }
//                    sortedIdList.mapNotNull { bucketItemBoxMap[it] } + bucketItemBoxMap.keys.minus(elements = sortedIdList).mapNotNull { bucketItemBoxMap[it] }
//                }.collectLatest { bucketItemBoxList ->
//                    val bucketItemBoxGroup = BucketItemBoxGroup(
//                        alpha = bucketItemBoxList.filter { it.bucketItemData?.decrypt(alice2)?.state == BucketItemData.State.Alpha },
//                        beta = bucketItemBoxList.filter { it.bucketItemData?.decrypt(alice2)?.state == BucketItemData.State.Beta },
//                        gamma = bucketItemBoxList.filter { it.bucketItemData?.decrypt(alice2)?.state == BucketItemData.State.Gamma },
//                        other = bucketItemBoxList.filter { it.bucketItemData?.decrypt(alice2)?.state == null },
//                        allOrdered = bucketItemBoxList
//                    )
//                    if (bucketItemBoxList.isEmpty()) this@BucketViewModel2._bucketItemBoxGroupDataFlow.tryEmit(value = DataLoader.NoData)
//                    else this@BucketViewModel2._bucketItemBoxGroupDataFlow.tryEmit(value = DataLoader.Loaded(data = bucketItemBoxGroup, hash = Random.nextInt()))
//                }
//            } catch (e: Exception) {
//                e.printStackTrace()
//                this@BucketViewModel2._bucketItemBoxGroupDataFlow.tryEmit(value = DataLoader.Error(message = "Error loading data", exception = e))
//            }
//        }
    }

    fun updateBucketBox(bucketBox: BucketBox) = boxRepository.putBucketBox(bucketBox = bucketBox)

    fun putBucketItemBox(bucketItemBox: BucketItemBox, parent: BucketBox) = boxRepository.putBucketItemBox(bucketItemBox = bucketItemBox, parent = parent)

    fun updateBucketItemBoxState(bucketItemBox: BucketItemBoxPlain, newState: BucketItemData.State) {
        viewModelScope.launch(Dispatchers.Default) {
            boxRepository.updateBucketItemBoxState(bucketItemBox = bucketItemBox.encrypt(alice2), newState = newState)
        }
    }

    fun onUpdateBucketItemOrder(bucketBox: BucketBox, bucketItemBoxIdOrder: List<Long>) {
        viewModelScope.launch(Dispatchers.Default) {
            boxRepository.onUpdateBucketItemOrder(bucketBox = bucketBox, bucketItemBoxIdOrder = bucketItemBoxIdOrder)
        }
    }

    fun onToggleLock(newState: Boolean) {
        val bucketBox = (this.bucketBoxDataFlow.value as? DataLoader.Loaded)?.data?.apply { this.isLocked = EncryptedBoolean.fromBoolean(newState, alice2) }
        boxRepository.putBucketBox(bucketBox = bucketBox ?: return)
    }

    fun onToggleFavourite(newState: Boolean) {
        val bucketBox = (this.bucketBoxDataFlow.value as? DataLoader.Loaded)?.data?.apply { this.isFavourite = EncryptedBoolean.fromBoolean(newState, alice2) }
        boxRepository.putBucketBox(bucketBox = bucketBox ?: return)
    }

    fun addTestData(bucketType: BucketBox.BucketType?, bucketBox: BucketBox) {
        viewModelScope.launch(context = Dispatchers.Default) {
            repeat(times = 50) {
                boxRepository.putBucketItemBox(BucketItemBox.randomBook(alice2), parent = bucketBox)
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

    data class BucketItemBoxOrderedData(
        val bucketItemBoxMap: Map<Long, BucketItemBoxPlain>,
        val orderedIdList: List<Long>
    )

    companion object {
        const val TAG = "BucketViewModel2"
    }
}