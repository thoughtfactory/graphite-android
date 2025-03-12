package com.syncodec.graphite.di.secureRepository

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.annotation.WorkerThread
import com.mayakapps.kache.InMemoryKache
import com.mayakapps.kache.KacheStrategy
import com.syncodec.graphite.di.modelObjectBox.BucketBox
import com.syncodec.graphite.di.modelObjectBox.BucketBoxEnc
import com.syncodec.graphite.di.modelObjectBox.BucketBoxEnc_
import com.syncodec.graphite.di.modelObjectBox.BucketItemBox
import com.syncodec.graphite.di.modelObjectBox.BucketItemBox_
import com.syncodec.graphite.di.modelObjectBox.ChapterBox
import com.syncodec.graphite.di.modelObjectBox.DecryptedBox
import com.syncodec.graphite.di.modelObjectBox.EncryptedBox
import com.syncodec.graphite.di.modelObjectBox.MyObjectBox
import com.syncodec.graphite.di.modelObjectBox.customObject.BucketItemData
import com.syncodec.graphite.utils.alice2.Alice2
import io.objectbox.BoxStore
import io.objectbox.Property
import io.objectbox.android.Admin
import io.objectbox.kotlin.boxFor
import io.objectbox.kotlin.flow
import io.objectbox.kotlin.query
import io.objectbox.kotlin.toFlow
import io.objectbox.query.QueryBuilder
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.OutputStream
import java.util.concurrent.CompletableFuture
import kotlin.jvm.java
import kotlin.time.TimeSource


class BoxRepository(
    val context: Context,
    val alice2: Alice2
) {
    private lateinit var thumbnailDir: File

    private val store: BoxStore = MyObjectBox.builder()
        .androidContext(context)
        .build()

    init {
        Admin(store).start(context);
        initializeThumbnailDirectory()
    }

    val cacheMap: MutableMap<CacheKey, CacheValue> = mutableMapOf()
    val kache = InMemoryKache<CacheKey, CacheValue>(maxSize = 200 * 1024 * 1024) {  // 5 MB
        strategy = KacheStrategy.LRU
    }


    private fun initializeThumbnailDirectory() {
        val filesDir = context.filesDir
        val dataDir = File(filesDir, "data").also { it.mkdirs() }
        this.thumbnailDir = File(dataDir, "thumbnail").also { it.mkdirs() }
    }

    @WorkerThread
    private suspend inline fun <reified E : EncryptedBox, reified D : DecryptedBox> putBoxObjectBlocking(boxObject: D, crossinline callback: (Long?) -> Unit = {}) {
        val encryptedBox = boxObject.encrypt(alice2) as E
        val boxObjectId = store.boxFor(E::class.java).put(encryptedBox)
        callback(boxObjectId)
    }

    private inline fun <reified E : EncryptedBox, reified D : DecryptedBox> putBoxObject(boxObject: D, crossinline callback: (Long?) -> Unit = {}) {
        CoroutineScope(Dispatchers.Default).launch { putBoxObjectBlocking<E, D>(boxObject = boxObject, callback = callback) }
    }

    private suspend inline fun <reified T> putBoxObjectBlocking(boxObject: T, crossinline callback: (Long?) -> Unit = {}) {
        val boxObjectId = if (boxObject != null) store.boxFor(T::class.java).put(boxObject)
        else null
        callback(boxObjectId)
    }

    private inline fun <reified T> putBoxObject(boxObject: T, crossinline callback: (Long?) -> Unit = {}) {
        CoroutineScope(Dispatchers.Default).launch { putBoxObjectBlocking(boxObject = boxObject, callback = callback) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    internal inline fun <reified T> getBoxObjectListAsFlow(block: QueryBuilder<T>.() -> Unit = {}): Flow<List<T>> = store
        .boxFor<T>()
        .query(block = block)
        .subscribe()
        .toFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    internal inline fun <reified E : EncryptedBox, D : DecryptedBox> getBoxObjectListAsFlow2(block: QueryBuilder<E>.() -> Unit = {}) = store
        .boxFor<E>()
        .query(block = block)
        .flow()
        .map { encryptedBoxList ->
            encryptedBoxList.map {encryptedBox ->
                val cacheKey = CacheKey(encryptedBox.id, E::class.simpleName)
                futureCompletable {
                    val cachedData = kache.get(cacheKey)
                    if (cachedData?.encryptedHash == encryptedBox.hashCode()) cachedData.data
                    else kache.put(cacheKey) { CacheValue(data = encryptedBox.decrypt(alice2 = alice2) as D, encryptedHash = encryptedBox.hashCode()) }?.data
                }
            }
        }
        .flowOn(Dispatchers.Default)

    @OptIn(ExperimentalCoroutinesApi::class)
    internal inline fun <reified T : EncryptedBox, D : DecryptedBox> getBoxObjectList2(cache: Boolean, block: QueryBuilder<T>.() -> Unit = {}) = store
        .boxFor<T>()
        .query(block = block)
        .find()
        .mapNotNull { t ->
            val cacheKey = CacheKey(t.id, T::class.simpleName)
            futureCompletable {
//                kache.getOrPut(cacheKey) {
//                    missCount += 1
//                    CacheValue(data = t.decrypt(alice2 = alice2) as D, encryptedHash = 0)
//                }

                Log.d(TAG, "id : ${t.id} : newHash : ${t.hashCode()} : cacheHash : ${kache.get(cacheKey)?.encryptedHash}")

                val cachedData = kache.get(cacheKey)
                if (cachedData?.encryptedHash == t.hashCode()) cachedData.data
                else kache.put(cacheKey) { CacheValue(data = t.decrypt(alice2 = alice2) as D, encryptedHash = t.hashCode()) }?.data
            }
        }

    fun <T> futureCompletable(func: suspend () -> T): suspend () -> T {
        return func
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    internal inline fun <reified T> getBoxObjectList(block: QueryBuilder<T>.() -> Unit = {}) = store
        .boxFor<T>()
        .query(block = block)
        .find()
        .toList()

    internal inline fun <reified T> countBoxObjectList(block: QueryBuilder<T>.() -> Unit = {}) = store
        .boxFor<T>()
        .query(block = block)
        .count()

    @OptIn(ExperimentalCoroutinesApi::class)
    internal inline fun <reified T> getBoxObjectAsFlow(id: Long, property: Property<T>) = store
        .boxFor<T>()
        .query { equal(property, id) }
        .subscribe()
        .toFlow()
        .map { it.firstOrNull() }

    fun getAllChapterBoxAsFlow(): Flow<List<ChapterBox>> = getBoxObjectListAsFlow()

    //    ////  Bucket Box
    fun getAllBucketBoxAsFlow() = getBoxObjectListAsFlow2<BucketBoxEnc, BucketBox>()

    fun getAllBucketBox() = getBoxObjectList2<BucketBoxEnc, BucketBox>(cache = true)

    fun getBucketBoxAsFlow(id: Long) = getBoxObjectAsFlow<BucketBoxEnc>(id = id, property = BucketBoxEnc_.id).map { it?.decrypt(alice2) }

    fun countBucketBox() = countBoxObjectList<BucketBoxEnc>()

    //    ////  BucketItem Box
    fun getBucketItemBoxListAsFlow(parentId: Long): Flow<List<BucketItemBox>> = getBoxObjectListAsFlow { equal(BucketItemBox_.parentId, parentId) }

    fun getBucketItemBoxAsFlow(id: Long) = getBoxObjectAsFlow<BucketItemBox>(id = id, property = BucketItemBox_.id)

    fun getAllBucketItemBox() = getBoxObjectList<BucketItemBox>()

    fun countBucketItemBox() = countBoxObjectList<BucketItemBox>()

    //    ////
    fun putChapterBox(chapterBox: ChapterBox) = putBoxObject(boxObject = chapterBox)

    fun putBucketBox(bucketBox: BucketBox) = putBoxObject<BucketBoxEnc, BucketBox>(boxObject = bucketBox)

    suspend fun putBucketBoxBlocking(bucketBox: BucketBox) = putBoxObjectBlocking<BucketBoxEnc, BucketBox>(boxObject = bucketBox)

    fun putBucketItemBox(bucketItemBox: BucketItemBox, parent: BucketBox, callback: (Long?) -> Unit = {}) {
//        bucketItemBox.apply { this.parent.target = parent }
        putBoxObject(boxObject = bucketItemBox, callback = callback)
    }

    suspend fun putBucketItemBoxBlocking(bucketItemBox: BucketItemBox, parent: BucketBox, callback: (Long?) -> Unit = {}) {
//        bucketItemBox.apply { this.parent.target = parent }
        putBoxObjectBlocking(boxObject = bucketItemBox, callback = callback)
    }

    fun putBucketItemBoxThumbnail(bucketItemBoxId: Long, thumbnail: Bitmap?) {
        thumbnail ?: return
        val thumbnailFile = File(thumbnailDir, "${bucketItemBoxId}.png")
        thumbnailFile.createNewFile()

        val thumbnailFileOutputStream = thumbnailFile.outputStream()
        thumbnail.compress(Bitmap.CompressFormat.PNG, 100, thumbnailFileOutputStream)
    }

    suspend fun putBucketItemBoxThumbnail(bucketItemBoxId: Long, saveTo: suspend (OutputStream) -> Unit) {
        val thumbnailFile = File(thumbnailDir, "${bucketItemBoxId}.png")
        thumbnailFile.createNewFile()

        saveTo(thumbnailFile.outputStream())
    }

    fun getBucketItemBoxThumbnail(bucketItemBoxId: Long): File? {
        val thumbnailFile = File(thumbnailDir, "${bucketItemBoxId}.png")
        return if (thumbnailFile.exists()) thumbnailFile else null
    }

    fun updateBucketItemBoxState(bucketItemBox: BucketItemBox, newState: BucketItemData.State) {
        val bucketItemBox = bucketItemBox.apply { this.bucketItemData = this.bucketItemData?.copyWithState(newState = newState) }
        putBoxObject(boxObject = bucketItemBox)
    }

    //    fun onUpdateBucketItemOrder(bucketBox: BucketBox, bucketItemBoxIdOrder: List<Long>) = putBoxObject(boxObject = bucketBox.copy(sortedIdList = bucketItemBoxIdOrder))
    fun onUpdateBucketItemOrder(bucketBox: BucketBox, bucketItemBoxIdOrder: List<Long>) = Unit

    companion object {

        const val TAG = "BoxRepository"

        data class CacheKey(val id: Long, val type: String?)
        data class CacheValue(val data: DecryptedBox, val encryptedHash: Int)
    }
}


