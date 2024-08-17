package com.syncodec.graphite.di.repo

import android.content.Context
import android.util.Log
import com.syncodec.graphite.di.model.BaseObject
import com.syncodec.graphite.di.model.BucketItemObject
import com.syncodec.graphite.di.model.BucketObject
import com.syncodec.graphite.di.model.ChapterObject
import com.syncodec.graphite.di.model.DeletedAttachment
import com.syncodec.graphite.di.model.DeletedObject
import com.syncodec.graphite.di.model.NoteObject
import com.syncodec.graphite.di.model.TagObject
import com.syncodec.graphite.di.repository.RealmMigrator
import com.syncodec.graphite.di.repository.repository.Repository.Companion.SCHEMA_VERSION
import com.syncodec.graphite.utils.DataStoreInstance
import com.syncodec.graphite.utils.SortOn
import com.syncodec.graphite.utils.SortOrder
import com.syncodec.graphite.utils.alice.AliceRequest2
import com.syncodec.graphite.utils.alice.getSecretData2
import com.syncodec.graphite.utils.alice.putSecretData2
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.ext.query
import io.realm.kotlin.notifications.ResultsChange
import io.realm.kotlin.query.Sort
import io.realm.kotlin.types.RealmUUID
import io.realm.kotlin.types.TypedRealmObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flattenConcat
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.launch
import java.security.SecureRandom
import java.time.Instant


class WrappedRepo(context: Context, dataStoreInstance: DataStoreInstance) {

    private val _repoFlow: MutableStateFlow<Repo> = MutableStateFlow(Repo.EncryptedRepo(context = context, dataStoreInstance = dataStoreInstance))
    val repoFlow: StateFlow<Repo> = _repoFlow.asStateFlow()

    init {
        decryptRepo()
        if (repoFlow.value is Repo.EncryptedRepo) Log.e(TAG, "Repo encrypted")
    }

    fun decryptRepo() = _repoFlow.tryEmit(_repoFlow.value.decryptRepo())

    fun encryptRepo() = _repoFlow.tryEmit(_repoFlow.value.encryptRepo())

    fun lockRepo() = (repoFlow.value as? Repo.DecryptedRepo)?.lockRepo()

    fun unlockRepo() {
        repoFlow.value.let { repo ->
            Log.d(TAG, "repo : ${repo::class}")
            (repo as? Repo.DecryptedRepo)?.unlockRepo()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun <T> emptyOrDataFlow(operation: Repo.DecryptedRepo.() -> Flow<T>): Flow<T> = _repoFlow.transform<Repo, Flow<T>> { repo ->
        Log.d(TAG, "emptyOrDataFlow : repo : ${repo::class.simpleName}")
        when (repo) {
            is Repo.ErrorRepo -> emit(emptyFlow())
            is Repo.EncryptedRepo -> emit(emptyFlow())
            is Repo.DecryptedRepo -> emit(repo.operation())
        }
    }.flattenConcat()

    companion object {
        const val TAG = "WrappedRepo"
    }
}


sealed class Repo {

    abstract val context: Context
    abstract val dataStoreInstance: DataStoreInstance

    abstract fun decryptRepo(): Repo


    abstract fun encryptRepo(): Repo


    class ErrorRepo(
        override val context: Context,
        override val dataStoreInstance: DataStoreInstance
    ) : Repo() {

        override fun decryptRepo(): Repo = retry().encryptRepo()

        override fun encryptRepo(): Repo = retry().encryptRepo()

        fun retry(): Repo = EncryptedRepo(context = context, dataStoreInstance = dataStoreInstance).initialize()
    }

    class EncryptedRepo(
        override val context: Context,
        override val dataStoreInstance: DataStoreInstance
    ) : Repo() {

        override fun decryptRepo(): Repo = initialize()

        override fun encryptRepo(): Repo = this

        fun initialize(): Repo {

            val keyAliceRequest = context.getSecretData2("realmKey")
            val realmKey = when (keyAliceRequest) {
                is AliceRequest2.KeyNotFound -> return createNewRepo()
                is AliceRequest2.KeyStoreNotInitialized -> return createNewRepo()
                is AliceRequest2.Error -> return ErrorRepo(context = context, dataStoreInstance = dataStoreInstance)
                is AliceRequest2.Success -> keyAliceRequest.data
            }

            return DecryptedRepo(context = context, realm = Realm.open(getRealmConfiguration(realmKey = realmKey)), dataStoreInstance = dataStoreInstance)
        }

        private fun createNewRepo(): Repo {
            val realmKey = ByteArray(Realm.ENCRYPTION_KEY_LENGTH)
            SecureRandom().nextBytes(realmKey)
            context.putSecretData2("realmKey", realmKey)
            val keyAliceRequest = context.getSecretData2("realmKey")
            val _realmKey = when (keyAliceRequest) {
                is AliceRequest2.Success -> keyAliceRequest.data
                else -> return ErrorRepo(context = context, dataStoreInstance = dataStoreInstance)
            }
            return DecryptedRepo(context = context, realm = Realm.open(getRealmConfiguration(realmKey = _realmKey)), dataStoreInstance = dataStoreInstance)
        }

        private fun getRealmConfiguration(realmKey: ByteArray): RealmConfiguration = RealmConfiguration
            .Builder(
                setOf(
                    BaseObject::class,
                    ChapterObject::class,
                    NoteObject::class,
                    BucketObject::class,
                    BucketItemObject::class,
                    TagObject::class,
                    DeletedObject::class,
                    DeletedAttachment::class,
                )
            )
            .encryptionKey(realmKey)
            .initialData {
                ChapterObject().also { chapterObject ->
                    chapterObject.title = "Diary"
                    chapterObject.description = "Default diary. Every notes will be saved in this notebook by default"

                    copyToRealm(chapterObject)

                    BaseObject().also { baseObject ->
                        baseObject.defaultChapterId = chapterObject.id

                        copyToRealm(baseObject)
                    }
                }
            }
            .schemaVersion(SCHEMA_VERSION)
            .migration(RealmMigrator())
            .build()
    }


    class DecryptedRepo(
        override val context: Context,
        override val dataStoreInstance: DataStoreInstance,
        val realm: Realm
    ) : Repo() {

        override fun decryptRepo(): Repo = this

        override fun encryptRepo(): Repo = EncryptedRepo(context = context, dataStoreInstance = dataStoreInstance)

        private val _isRepoOpen = MutableStateFlow(false)
        val isRepoOpen = _isRepoOpen.asStateFlow()

        private val sortOrderFlow = dataStoreInstance.getSortOrder.stateIn(scope = CoroutineScope(Dispatchers.Default), started = SharingStarted.Lazily, initialValue = null)
        private val sortOnFlow = dataStoreInstance.getSortOn.stateIn(scope = CoroutineScope(Dispatchers.Default), started = SharingStarted.Lazily, initialValue = null)

        fun lockRepo() {
            _isRepoOpen.tryEmit(false)
        }

        fun unlockRepo() {
            _isRepoOpen.tryEmit(true)
        }

        fun withQueryFilterFlow() = combine(
            isRepoOpen,
            sortOnFlow,
            sortOrderFlow
        ) {
            QueryFilter(
                isRepoOpen = it[0] as Boolean,
                sortOn = it[1] as SortOn?,
                sortOrder = it[2] as SortOrder?
            )
        }

        fun withQueryFilter() = QueryFilter(
            isRepoOpen = isRepoOpen.value,
            sortOn = sortOnFlow.value,
            sortOrder = sortOrderFlow.value
        )

        fun getDefaultChapterId(): RealmUUID? = realm.query(BaseObject::class).first().find()?.defaultChapterId

        fun getDefaultChapterIdAsFlow(): Flow<RealmUUID?> = realm.query(BaseObject::class).first().asFlow().map { it.obj?.defaultChapterId }

        fun getAllNotesFromChapter(chapterId: RealmUUID, includeLocked: Boolean) =
            if (includeLocked) realm.query(NoteObject::class, "parentId == $0", chapterId).first().find()
            else realm.query(NoteObject::class, "parentId == $0 AND isLocked == $1", chapterId, false).first().find()

        @OptIn(ExperimentalCoroutinesApi::class)
        fun getAllNotesFromChapterAsFlow(chapterId: RealmUUID, sorted: Boolean = false) = withQueryFilterFlow()
            .transform { queryFilter ->
                val realmQuery = if (queryFilter.isRepoOpen) realm.query(NoteObject::class, "parentId == $0", chapterId)
                else realm.query(NoteObject::class, "parentId == $0 AND isLocked == $1", chapterId, false)

                if (sorted) emit(realmQuery.sort(property = queryFilter.getRealmSortOnProperty(), sortOrder = queryFilter.getRealmSortOrder()).asFlow())
                else emit(realmQuery.asFlow())
            }.flattenConcat()

        @OptIn(ExperimentalCoroutinesApi::class)
        fun getNotesFromDefaultChapterAsFlow() = getDefaultChapterIdAsFlow()
            .transform { chapterId ->
                Log.d(TAG, "getNotesFromDefaultChapterAsFlow : chapterId : ${chapterId}")
                if (chapterId != null) emit(getAllNotesFromChapterAsFlow(chapterId = chapterId, sorted = false))
                else emit(emptyFlow())
            }.flattenConcat()

        inline fun <reified T : TypedRealmObject> getObjectFromId(id: RealmUUID?, includeLocked: Boolean): T? =
            if (includeLocked) realm.query(T::class, "id == $0 ", id).first().find()
            else realm.query(T::class, "id == $0 AND isLocked == $1", id, false).first().find()

        inline fun <reified T : TypedRealmObject> getObjectFromId(idList: List<RealmUUID>, includeLocked: Boolean): List<T> =
            if (includeLocked) realm.query<T>("id IN $0 ", idList).find().map { it }
            else realm.query<T>("id IN $0 AND isLocked == $1", idList, false).find().map { it }

        fun putNote(noteObject: NoteObject, modifyTimestampAuto: Boolean = true) = realm.writeBlocking {
            val storedNoteObject = getObjectFromId<NoteObject>(id = noteObject.id, includeLocked = true)
            storedNoteObject?.let {
                findLatest(it)?.let { latestNoteObject ->
                    latestNoteObject.createdTimestamp = noteObject.createdTimestamp
                    latestNoteObject.modifiedTimestamp = if (modifyTimestampAuto) Instant.now().toEpochMilli() else noteObject.modifiedTimestamp
                    latestNoteObject.userTimestamp = noteObject.userTimestamp
                    latestNoteObject.title = noteObject.title
                    latestNoteObject.color = noteObject.color
                    latestNoteObject.latLng = noteObject.latLng
                    latestNoteObject.address = noteObject.address
                    latestNoteObject.content = noteObject.content
                    latestNoteObject.isFavourite = noteObject.isFavourite
                    latestNoteObject.isLocked = noteObject.isLocked
                    latestNoteObject.parentId = noteObject.parentId
                } ?: copyToRealm(noteObject)
            } ?: copyToRealm(noteObject)
        }

        fun putNoteInDefaultSuspended(noteObject: NoteObject, modifyTimestampAuto: Boolean = true) = CoroutineScope(Dispatchers.Default).launch {
            Log.d(TAG, "putNoteInDefaultSuspended")
            noteObject.parentId = getDefaultChapterId()
            putNote(noteObject, modifyTimestampAuto)
        }

        fun applyGroupOnProperty() {

        }

        data class QueryFilter(
            val isRepoOpen: Boolean,
            val sortOn: SortOn?,
            val sortOrder: SortOrder?
        ) {
            fun getRealmSortOrder() = if (sortOrder == SortOrder.Ascending) Sort.ASCENDING else Sort.DESCENDING
            fun getRealmSortOnProperty(): String = when (sortOn) {
                SortOn.Title -> "title"
                SortOn.CreatedTimestamp -> "createdTimestamp"
                SortOn.ModifiedTimestamp -> "modifiedTimestamp"
                SortOn.UserTimestamp -> "userTimestamp"
                SortOn.Custom -> "title"
                else -> "createdTimestamp"
            }
        }

        companion object {
            const val TAG = "DecryptedRepo"
        }
    }
}
