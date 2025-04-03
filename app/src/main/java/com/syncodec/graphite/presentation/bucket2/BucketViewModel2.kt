package com.syncodec.graphite.presentation.bucket2

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syncodec.graphite.di.modelObjectBox.BucketBoxDecrypted
import com.syncodec.graphite.di.modelObjectBox.BucketBoxEncrypted
import com.syncodec.graphite.di.modelObjectBox.BucketItemBoxDecrypted
import com.syncodec.graphite.di.modelObjectBox.BucketItemBoxEncrypted
import com.syncodec.graphite.di.modelObjectBox.encryptable.encrypt
import com.syncodec.graphite.di.secureRepository.BoxRepository
import com.syncodec.graphite.utils.DataLoader
import com.syncodec.graphite.utils.alice2.Alice2
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import kotlin.random.Random
import kotlin.time.measureTime
import kotlin.uuid.ExperimentalUuidApi


@KoinViewModel
class BucketViewModel2(
    private val boxRepository: BoxRepository,
    private val alice2: Alice2,
) : ViewModel() {

    private val _bucketBoxDataFlow: MutableStateFlow<DataLoader<BucketBoxDecrypted>> = MutableStateFlow(value = DataLoader.Init())
    val bucketBoxDataFlow: StateFlow<DataLoader<BucketBoxDecrypted>> = this._bucketBoxDataFlow.asStateFlow()

    private val _bucketItemBoxOrderedDataFlow: MutableStateFlow<DataLoader<BucketItemBoxOrderedData>> = MutableStateFlow(value = DataLoader.Init())
    val bucketItemBoxOrderedDataFlow: StateFlow<DataLoader<BucketItemBoxOrderedData>> = this._bucketItemBoxOrderedDataFlow

    fun loadData(bucketId: Long) {
        viewModelScope.launch(context = Dispatchers.Default) {
            try {
                boxRepository.getBucketBoxAsFlow(id = bucketId).collectLatest { bucketBoxCache ->
                    val bucketBox = bucketBoxCache?.decrypt(alice2)
                    if (bucketBox == null) this@BucketViewModel2._bucketBoxDataFlow.tryEmit(value = DataLoader.NoData())
                    else this@BucketViewModel2._bucketBoxDataFlow.tryEmit(value = DataLoader.Loaded(data = bucketBox))
                }
            } catch (e: Exception) {
                this@BucketViewModel2._bucketBoxDataFlow.tryEmit(value = DataLoader.Error(message = "Error loading data", exception = e))
            }
        }

        viewModelScope.launch(context = Dispatchers.IO) {
            boxRepository
                .getBucketItemBoxListAsFlow(parentId = bucketId)
                .combine(flow = bucketBoxDataFlow) { bucketItemBoxList, bucketBoxData ->

                    val bucketItemBoxIdMap = bucketItemBoxList.associateBy { it.enc.id }

                    var a: BucketItemBoxOrderedData
                    val m = measureTime {
                        val allIdSet = bucketItemBoxList.map { it.enc.id }.toSet()
                        val savedIdList = (bucketBoxData as? DataLoader.Loaded)?.data?.sortedIdList ?: listOf()

                        val allOrderedIdList = (savedIdList + allIdSet.minus(elements = savedIdList)).filter { it in allIdSet }
                        val stateGroupId = allOrderedIdList.groupBy { id -> bucketItemBoxIdMap[id]?.enc?.state?.decrypt(alice2) }

                        a = BucketItemBoxOrderedData(
                            bucketItemBoxMap = bucketItemBoxIdMap,
                            allOrderedIdList = allOrderedIdList,
                            alphaOrderedIdList = stateGroupId[BucketItemBoxDecrypted.State.Alpha] ?: listOf(),
                            betaOrderedIdList = stateGroupId[BucketItemBoxDecrypted.State.Beta] ?: listOf(),
                            gammaOrderedIdList = stateGroupId[BucketItemBoxDecrypted.State.Gamma] ?: listOf(),
                        )
                    }
                    a
                }.flowOn(context = Dispatchers.IO)
                .collectLatest { bucketItemBoxOrderedData ->
                    if (bucketItemBoxOrderedData.bucketItemBoxMap.isEmpty()) this@BucketViewModel2._bucketItemBoxOrderedDataFlow.tryEmit(value = DataLoader.NoData())
                    else this@BucketViewModel2._bucketItemBoxOrderedDataFlow.tryEmit(value = DataLoader.Loaded(data = bucketItemBoxOrderedData, hash = Random.nextInt()))
                }
        }
    }

    fun updateBucketBox(bucketBox: BucketBoxDecrypted) = boxRepository.putBucketBox(bucketBox = bucketBox)

    @OptIn(ExperimentalUuidApi::class)
    fun putBucketItemBox(bucketItemBox: BucketItemBoxDecrypted, parent: BucketBoxDecrypted) = boxRepository.putBucketItemBox(bucketItemBox = bucketItemBox.copy(parent = parent))

    fun updateBucketItemBox(bucketItemBox: BucketItemBoxDecrypted) = boxRepository.putBucketItemBox(bucketItemBox = bucketItemBox)

    fun updateBucketItemBoxState(bucketItemBox: BucketItemBoxDecrypted, newState: BucketItemBoxDecrypted.State) = boxRepository.updateBucketItemBoxState(bucketItemBox = bucketItemBox, newState = newState)

    @OptIn(ExperimentalUuidApi::class)
    fun updateBucketItemBoxState(itemIdList: List<Long>, newState: BucketItemBoxDecrypted.State) {
        viewModelScope.launch(context = Dispatchers.Default) {
            val bucketItemBoxList = (bucketItemBoxOrderedDataFlow.value as? DataLoader.Loaded)
                ?.data
                ?.bucketItemBoxMap
                ?.filterKeys { it in itemIdList }
                ?.values
                ?.map { it.decrypt(alice2) }?: return@launch
            bucketItemBoxList
                .mapNotNull { it?.copy(state = newState) }
                .forEach { boxRepository.putBucketItemBox(bucketItemBox = it) }
        }
    }

    fun onUpdateBucketItemOrder(bucketBox: BucketBoxDecrypted, bucketItemBoxIdOrder: List<Long>) {
        boxRepository.onUpdateBucketItemOrder(bucketBox = bucketBox, bucketItemBoxIdOrder = bucketItemBoxIdOrder)
    }

    @OptIn(ExperimentalUuidApi::class)
    fun onToggleFavouriteBucketBox(newState: Boolean) {
        val bucketBox = (this.bucketBoxDataFlow.value as? DataLoader.Loaded)?.data?.copy(isFavourite = newState)
        boxRepository.putBucketBox(bucketBox = bucketBox ?: return)
    }

    @OptIn(ExperimentalUuidApi::class)
    fun onToggleLockBucketBox(newState: Boolean) {
        val bucketBox = (this.bucketBoxDataFlow.value as? DataLoader.Loaded)?.data?.copy(isLocked = newState)
        boxRepository.putBucketBox(bucketBox = bucketBox ?: return)
    }

    @OptIn(ExperimentalUuidApi::class)
    fun onToggleFavouriteBucketItemBox(itemIdList: List<Long>) {
        viewModelScope.launch(context = Dispatchers.Default) {
            val bucketItemBoxList = (bucketItemBoxOrderedDataFlow.value as? DataLoader.Loaded)
                ?.data
                ?.bucketItemBoxMap
                ?.filterKeys { it in itemIdList }
                ?.values
                ?.map { it.decrypt(alice2) }?: return@launch
            val isAllFavourite = bucketItemBoxList.all { it?.isFavourite == true }
            bucketItemBoxList
                .mapNotNull { it?.copy(isFavourite = !isAllFavourite) }
                .forEach { boxRepository.putBucketItemBox(bucketItemBox = it) }
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun onToggleLockBucketItemBox(itemIdList: List<Long>) {
        viewModelScope.launch(context = Dispatchers.Default) {
            val bucketItemBoxList = (bucketItemBoxOrderedDataFlow.value as? DataLoader.Loaded)
                ?.data
                ?.bucketItemBoxMap
                ?.filterKeys { it in itemIdList }
                ?.values
                ?.map { it.decrypt(alice2) }?: return@launch
            val isAllLocked = bucketItemBoxList.all { it?.isLocked == true }
            bucketItemBoxList
                .mapNotNull { it?.copy(isLocked = !isAllLocked) }
                .forEach { boxRepository.putBucketItemBox(bucketItemBox = it) }
        }
    }

    fun deleteBucketItemBox(idList: List<Long>) = boxRepository.deleteBucketItemBox(idList = idList)

    @OptIn(ExperimentalUuidApi::class)
    fun addTestData(bucketType: BucketBoxEncrypted.BucketType? = null, bucketBox: BucketBoxDecrypted) {
        viewModelScope.launch(context = Dispatchers.Default) {
            (bucketItemBoxOrderedDataFlow.value as? DataLoader.Loaded)?.data?.bucketItemBoxMap?.toList()?.firstOrNull()?.second?.let { bucketItemBox ->
                repeat(times = 50) {
                    boxRepository.putBucketItemBox(bucketItemBox = bucketItemBox.decrypt(alice2)?.copy(id = 0, parent = bucketBox)!!)
                }
            }
        }
    }

    data class BucketItemBoxOrderedData(
        val bucketItemBoxMap: Map<Long, BoxRepository.Companion.CacheValue<BucketItemBoxEncrypted, BucketItemBoxDecrypted>>,
        val allOrderedIdList: List<Long> = listOf(),
        val alphaOrderedIdList: List<Long> = listOf(),
        val betaOrderedIdList: List<Long> = listOf(),
        val gammaOrderedIdList: List<Long> = listOf(),
        val rand: Int = Random.nextInt()
    )

    companion object {
        const val TAG = "BucketViewModel2"
    }
}