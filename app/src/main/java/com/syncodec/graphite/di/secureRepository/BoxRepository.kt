package com.syncodec.graphite.di.secureRepository

import android.content.Context
import android.graphics.Bitmap
import com.syncodec.graphite.di.modelObjectBox.BucketBox
import com.syncodec.graphite.di.modelObjectBox.BucketBox_
import com.syncodec.graphite.di.modelObjectBox.BucketItemBox
import com.syncodec.graphite.di.modelObjectBox.BucketItemBox_
import com.syncodec.graphite.di.modelObjectBox.ChapterBox
import com.syncodec.graphite.di.modelObjectBox.MyObjectBox
import com.syncodec.graphite.di.modelObjectBox.customObject.BucketItemData
import io.objectbox.BoxStore
import io.objectbox.Property
import io.objectbox.android.Admin
import io.objectbox.kotlin.boxFor
import io.objectbox.kotlin.query
import io.objectbox.kotlin.toFlow
import io.objectbox.query.QueryBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.File
import java.io.OutputStream


class BoxRepository(
    val context: Context
) {

    private lateinit var thumbnailDir: File

    private val store: BoxStore = MyObjectBox.builder()
        .androidContext(context)
        .build()

    init {
        Admin(store).start(context);
        initializeThumbnailDirectory()
    }

    private fun initializeThumbnailDirectory() {
        val filesDir = context.filesDir
        val dataDir = File(filesDir, "data").also { it.mkdirs() }
        this.thumbnailDir = File(dataDir, "thumbnail").also { it.mkdirs() }
    }

    internal inline fun <reified T> putBoxObject(boxObject: T, crossinline callback: (Long?) -> Unit = {}) {
        CoroutineScope(Dispatchers.Default).launch {
            val boxObjectId = if (boxObject != null) store.boxFor(T::class.java).put(boxObject)
            else null
            callback(boxObjectId)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    internal inline fun <reified T> getBoxObjectListAsFlow(block: QueryBuilder<T>.() -> Unit = {}): Flow<List<T>> = store
        .boxFor<T>()
        .query(block = block)
        .subscribe()
        .toFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    internal inline fun <reified T> getBoxObjectAsFlow(id: Long, property: Property<T>) = store
        .boxFor<T>()
        .query { equal(property, id) }
        .subscribe()
        .toFlow()
        .map { it.firstOrNull() }

    fun getAllChapterBoxAsFlow(): Flow<List<ChapterBox>> = getBoxObjectListAsFlow()

    fun getAllBucketBoxAsFlow(): Flow<List<BucketBox>> = getBoxObjectListAsFlow()

    fun getBucketBoxAsFlow(id: Long) = getBoxObjectAsFlow<BucketBox>(id = id, property = BucketBox_.id)

    fun getBucketItemBoxListAsFlow(parentId: Long): Flow<List<BucketItemBox>> = getBoxObjectListAsFlow { equal(BucketItemBox_.parentId, parentId) }

    fun getBucketItemBoxAsFlow(id: Long) = getBoxObjectAsFlow<BucketItemBox>(id = id, property = BucketItemBox_.id)

    fun putChapterBox(chapterBox: ChapterBox) = putBoxObject(boxObject = chapterBox)

    fun putBucketBox(bucketBox: BucketBox) = putBoxObject(boxObject = bucketBox)

    fun putBucketItemBox(bucketItemBox: BucketItemBox, parent: BucketBox, callback: (Long?) -> Unit = {})  {
        bucketItemBox.apply { this.parent.target = parent }
        putBoxObject(boxObject = bucketItemBox, callback = callback)
    }

    fun putBucketItemBoxThumbnail(bucketItemBoxId: Long, thumbnail: Bitmap?)  {
        thumbnail ?: return
        val thumbnailFile = File(thumbnailDir, "${bucketItemBoxId}.png")
        thumbnailFile.createNewFile()

        val thumbnailFileOutputStream = thumbnailFile.outputStream()
        thumbnail.compress(Bitmap.CompressFormat.PNG, 100, thumbnailFileOutputStream)
    }

    suspend fun putBucketItemBoxThumbnail(bucketItemBoxId: Long, saveTo: suspend (OutputStream) -> Unit)  {
        val thumbnailFile = File(thumbnailDir, "${bucketItemBoxId}.png")
        thumbnailFile.createNewFile()

        saveTo(thumbnailFile.outputStream())
    }

    fun getBucketItemBoxThumbnail(bucketItemBoxId: Long) : File? {
        val thumbnailFile = File(thumbnailDir, "${bucketItemBoxId}.png")
        return if (thumbnailFile.exists()) thumbnailFile else null
    }

    fun updateBucketItemBoxState(bucketItemBox: BucketItemBox, newState: BucketItemData.State) {
        val bucketItemBox = bucketItemBox.apply { this.bucketItemData = this.bucketItemData?.copyWithState(newState = newState) }
        putBoxObject(boxObject = bucketItemBox)
    }

    fun onUpdateBucketItemOrder(bucketBox: BucketBox, bucketItemBoxIdOrder: List<Long>) = putBoxObject(boxObject = bucketBox.copy(sortedIdList = bucketItemBoxIdOrder))
}
