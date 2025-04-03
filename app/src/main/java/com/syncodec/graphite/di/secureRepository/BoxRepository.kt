package com.syncodec.graphite.di.secureRepository

import android.content.Context
import android.util.Log
import androidx.annotation.WorkerThread
import com.mayakapps.kache.InMemoryKache
import com.mayakapps.kache.KacheStrategy
import com.syncodec.graphite.BuildConfig
import com.syncodec.graphite.di.modelObjectBox.BucketBoxDecrypted
import com.syncodec.graphite.di.modelObjectBox.BucketBoxEncrypted
import com.syncodec.graphite.di.modelObjectBox.BucketBoxEncrypted_
import com.syncodec.graphite.di.modelObjectBox.BucketItemBoxEncrypted
import com.syncodec.graphite.di.modelObjectBox.BucketItemBoxDecrypted
import com.syncodec.graphite.di.modelObjectBox.BucketItemBoxEncrypted_
import com.syncodec.graphite.di.modelObjectBox.ChapterBox
import com.syncodec.graphite.di.modelObjectBox.DecryptedBox
import com.syncodec.graphite.di.modelObjectBox.EncryptedBox
import com.syncodec.graphite.di.modelObjectBox.MyObjectBox
import com.syncodec.graphite.di.modelObjectBox.customObject.BucketItemData
import com.syncodec.graphite.presentation.ui.authenticator2.AuthController
import com.syncodec.graphite.presentation.ui.authenticator2.AuthState
import com.syncodec.graphite.utils.alice2.Alice2
import dev.whyoleg.cryptography.algorithms.AES
import io.objectbox.BoxStore
import io.objectbox.Property
import io.objectbox.android.Admin
import io.objectbox.kotlin.boxFor
import io.objectbox.kotlin.flow
import io.objectbox.kotlin.query
import io.objectbox.kotlin.toFlow
import io.objectbox.query.QueryBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File
import kotlin.collections.mapNotNull
import kotlin.io.outputStream
import kotlin.jvm.java
import kotlin.uuid.ExperimentalUuidApi


@OptIn(ExperimentalUuidApi::class, ExperimentalStdlibApi::class)
class BoxRepository(
    val context: Context,
    val alice2: Alice2,
    val authController: AuthController
) {
    private lateinit var thumbnailDir: File

    private val store: BoxStore = MyObjectBox.builder()
        .androidContext(context)
        .build()

    init {
        Admin(store).start(context)
        initializeThumbnailDirectory()

        if (BuildConfig.DEBUG) {
            CoroutineScope(Dispatchers.Default).launch {
                Log.w(TAG, alice2.key?.encodeToByteArray(AES.Key.Format.RAW)?.toHexString(HexFormat.UpperCase).toString())
            }
        }
    }

    val kache = InMemoryKache<CacheKey, CacheValue<EncryptedBox, DecryptedBox>>(maxSize = 200 * 1024 * 1024) {  // 5 MB
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

    @WorkerThread
    private suspend inline fun <reified E : EncryptedBox> putBoxObjectBlocking(boxObject: E, crossinline callback: (Long?) -> Unit = {}) {
        val boxObjectId = store.boxFor(E::class.java).put(boxObject)
        callback(boxObjectId)
    }

    private inline fun <reified E : EncryptedBox, reified D : DecryptedBox> putBoxObject(boxObject: D, crossinline callback: (Long?) -> Unit = {}) {
        CoroutineScope(Dispatchers.Default).launch { putBoxObjectBlocking<E, D>(boxObject = boxObject, callback = callback) }
    }

    private inline fun <reified E : EncryptedBox> putBoxObject(boxObject: E, crossinline callback: (Long?) -> Unit = {}) {
        CoroutineScope(Dispatchers.Default).launch { putBoxObjectBlocking<E>(boxObject = boxObject, callback = callback) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    internal inline fun <reified T> getBoxObjectListAsFlow(block: QueryBuilder<T>.() -> Unit = {}): Flow<List<T>> = store
        .boxFor<T>()
        .query(block = block)
        .subscribe()
        .toFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    internal inline fun <reified E : EncryptedBox, D : DecryptedBox> getBoxObjectListAsFlow2(block: QueryBuilder<E>.() -> Unit = {}): Flow<List<CacheValue<E, D>>> = store
        .boxFor<E>()
        .query(block = block)
        .flow()
        .combine(authController.authStateFlow) { boxObjectList, authState ->
            boxObjectList.filter { if (authState == AuthState.Authenticated) true else it.isLocked?.decrypt(alice2) == false }
        }
        .mapNotNull { encryptedBoxList ->
            encryptedBoxList.map { encryptedBox ->
                val cacheKey = CacheKey(encryptedBox.id, E::class.simpleName)
                val cachedData = kache.get(cacheKey) as? CacheValue<E, D>

                if (cachedData?.encryptedHash == encryptedBox.hashCode()) cachedData
                else {
                    val cacheValue = CacheValue<E, D>(enc = encryptedBox, encryptedHash = encryptedBox.hashCode())
                    kache.put(cacheKey) { cacheValue }
                    cacheValue
                }
            }
        }
        .flowOn(Dispatchers.Default)

    @OptIn(ExperimentalCoroutinesApi::class)
    internal inline fun <reified E : EncryptedBox, D : DecryptedBox> getBoxObjectList2(block: QueryBuilder<E>.() -> Unit = {}) = store
        .boxFor<E>()
        .query(block = block)
        .find()
        .mapNotNull { encryptedBox ->
            runBlocking {
                if (authController.authStateFlow.value != AuthState.Authenticated && encryptedBox.isLocked?.decrypt(alice2) == true) return@runBlocking null
                val cacheKey = CacheKey(encryptedBox.id, E::class.simpleName)
                val cachedData = kache.get(cacheKey)
                val cacheValue = CacheValue<E, D>(enc = encryptedBox, encryptedHash = encryptedBox.hashCode())
                if (cachedData?.encryptedHash == encryptedBox.hashCode()) cachedData
                else kache.put(cacheKey) { cacheValue }
                cacheValue
            }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    internal inline fun <reified T> getBoxObjectList(block: QueryBuilder<T>.() -> Unit = {}) = store
        .boxFor<T>()
        .query(block = block)
        .find()
        .toList()

    internal inline fun <reified E : EncryptedBox> countBoxObjectList(block: QueryBuilder<E>.() -> Unit = {}) = store
        .boxFor<E>()
        .query(block = block)
        .count()

    @OptIn(ExperimentalCoroutinesApi::class)
    internal inline fun <reified E : EncryptedBox, D : DecryptedBox> getBoxObjectAsFlow(id: Long, property: Property<E>): Flow<CacheValue<E, D>?> = store
        .boxFor<E>()
        .query { equal(property, id) }
        .flow()
        .map { encryptedBoxList ->
            val encryptedBox = encryptedBoxList.firstOrNull()
            encryptedBox ?: return@map null
            val cacheKey = CacheKey(encryptedBox.id, E::class.simpleName)
            val cachedData = kache.get(cacheKey) as? CacheValue<E, D>

            if (cachedData?.encryptedHash == encryptedBox.hashCode()) cachedData
            else {
                val cacheValue = CacheValue<E, D>(enc = encryptedBox, encryptedHash = encryptedBox.hashCode())
                kache.put(cacheKey) { cacheValue }
                cacheValue
            }
        }

    private inline fun <reified E : EncryptedBox> deleteBoxObject(id: Long) {
        CoroutineScope(context = Dispatchers.Default).launch {
            store.boxFor<E>().remove(id)
        }
    }

    fun getAllChapterBoxAsFlow(): Flow<List<ChapterBox>> = getBoxObjectListAsFlow()

    //    ////  Bucket Box
    fun getAllBucketBoxAsFlow() = getBoxObjectListAsFlow2<BucketBoxEncrypted, BucketBoxDecrypted>()

    fun getBucketBoxAsFlow(id: Long) = getBoxObjectAsFlow<BucketBoxEncrypted, BucketBoxDecrypted>(id = id, property = BucketBoxEncrypted_.id)

    //    ////  BucketItem Box
    fun getBucketItemBoxListAsFlow(parentId: Long): Flow<List<CacheValue<BucketItemBoxEncrypted, BucketItemBoxDecrypted>>> = getBoxObjectListAsFlow2<BucketItemBoxEncrypted, BucketItemBoxDecrypted> {
        equal(BucketItemBoxEncrypted_.parentId, parentId)
    }

    fun getBucketItemBoxAsFlow(id: Long) = getBoxObjectAsFlow<BucketItemBoxEncrypted, BucketItemBoxDecrypted>(id = id, property = BucketItemBoxEncrypted_.id)

    //    ////
//    fun putChapterBox(chapterBox: ChapterBox) = putBoxObject(boxObject = chapterBox)
    fun putChapterBox(chapterBox: ChapterBox) {}

    fun putBucketBox(bucketBox: BucketBoxDecrypted) = putBoxObject<BucketBoxEncrypted, BucketBoxDecrypted>(boxObject = bucketBox.modifyDateTime())

    fun putBucketItemBox(bucketItemBox: BucketItemBoxDecrypted, callback: (Long?) -> Unit = {}) = putBoxObject<BucketItemBoxEncrypted, BucketItemBoxDecrypted>(boxObject = bucketItemBox.modifyDateTime(), callback = callback)

    fun putBucketItemBox(bucketItemBox: BucketItemBoxEncrypted, callback: (Long?) -> Unit = {}) = putBoxObject<BucketItemBoxEncrypted>(boxObject = bucketItemBox, callback = callback)

    fun moveBucketItemBox(parent: BucketBoxEncrypted, bucketItemIdList: List<Long>, callback: () -> Unit = {}) {
        CoroutineScope(context = Dispatchers.Default).launch {
            val bucketItemList = getBoxObjectList2<BucketItemBoxEncrypted, BucketItemBoxDecrypted> {
                `in`(BucketItemBoxEncrypted_.id, bucketItemIdList.toLongArray())
            }.map { it.enc }
            bucketItemList.map { it.parent.target = parent }
            bucketItemList.forEach { putBucketItemBox(bucketItemBox = it) }
            callback()
        }
    }

    fun putBucketItemBoxThumbnail(thumbnailFile: BucketItemData.Companion.Thumbnail.File?, thumbnailPlainByteArray: ByteArray) {
        thumbnailFile ?: return

        val encryptedByteArray = alice2.encrypt(thumbnailPlainByteArray) ?: return

        val file = thumbnailFile.getFile(context)
        if (file != null) {
            try {
                file.outputStream().write(encryptedByteArray)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getBucketItemBoxThumbnail(bucketItemBoxId: Long): File? {
        val thumbnailFile = File(thumbnailDir, "${bucketItemBoxId}.png")
        return if (thumbnailFile.exists()) thumbnailFile else null
    }

    fun updateBucketItemBoxState(bucketItemBox: BucketItemBoxDecrypted, newState: BucketItemBoxDecrypted.State) {
        val bucketItemBox = bucketItemBox.copy(state = newState)
        putBucketItemBox(bucketItemBox = bucketItemBox)
    }

    fun onUpdateBucketItemOrder(bucketBox: BucketBoxDecrypted, bucketItemBoxIdOrder: List<Long>) = putBucketBox(bucketBox = bucketBox.copy(sortedIdList = bucketItemBoxIdOrder))

    fun deleteBucketItemBox(idList: List<Long>) { idList.forEach { deleteBoxObject<BucketItemBoxEncrypted>(id = it) } }

    companion object {

        const val TAG = "BoxRepository"

        data class CacheKey(val id: Long, val type: String?)
        class CacheValue<out E : EncryptedBox, out D : DecryptedBox>(val enc: E, val encryptedHash: Int) {

            private var dec: D? = null

            @WorkerThread
            suspend fun decrypt(alice2: Alice2): D? {
                if (dec == null) dec = enc.decrypt(alice2) as D?
                return dec
            }

            fun decryptBlocking(alice2: Alice2): D? {
                if (dec == null) dec = enc.decrypt(alice2) as D?
                return dec
            }
        }
    }
}


