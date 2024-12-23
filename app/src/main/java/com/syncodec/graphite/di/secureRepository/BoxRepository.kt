package com.syncodec.graphite.di.secureRepository

import android.R.attr.order
import android.content.Context
import com.google.android.gms.common.internal.Objects.equal
import com.syncodec.graphite.di.modelObjectBox.ChapterBox
import com.syncodec.graphite.di.modelObjectBox.ChapterBox_
import com.syncodec.graphite.di.modelObjectBox.MyObjectBox
import io.objectbox.Box
import io.objectbox.BoxStore
import io.objectbox.Property
import io.objectbox.android.Admin
import io.objectbox.kotlin.boxFor
import io.objectbox.kotlin.query
import io.objectbox.kotlin.toFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch


class BoxRepository(
    private val context: Context
) {

    private val store: BoxStore = MyObjectBox.builder()
        .androidContext(context)
        .build()

    init {
        Admin(store).start(context);
    }

    fun putChapter2(chapterBox: ChapterBox) {
        store
            .callInTxAsync(
                {
                    store
                        .boxFor(ChapterBox::class.java)
                        .put(chapterBox)
                },
                { r, e ->
                    e?.printStackTrace()
                }
            )
    }

    fun putChapter(
        chapterBox: ChapterBox,
    ) {
        CoroutineScope(Dispatchers.Default).launch {
            store.boxFor(ChapterBox::class.java).put(chapterBox)
        }
    }


    @OptIn(ExperimentalCoroutinesApi::class)
    fun getAllChapterAsFlow(): Flow<List<ChapterBox>> {
        val chapterBox = store.boxFor<ChapterBox>()

        return chapterBox
            .query{
                greater(ChapterBox_.id, 0L)
            }
            .subscribe()
            .toFlow()
    }

}
