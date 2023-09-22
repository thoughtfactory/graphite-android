package com.syncodec.graphite.di.repository

import android.content.Context
import android.graphics.Bitmap
import android.os.FileObserver
import android.util.Log
import androidx.annotation.WorkerThread
import com.jakewharton.processphoenix.ProcessPhoenix
import com.syncodec.graphite.BuildConfig
import com.syncodec.graphite.di.model.BaseObject
import com.syncodec.graphite.di.model.BucketItemObject
import com.syncodec.graphite.di.model.BucketObject
import com.syncodec.graphite.di.model.BucketObjectLite
import com.syncodec.graphite.di.model.ChapterObject
import com.syncodec.graphite.di.model.ChapterObjectLite
import com.syncodec.graphite.di.model.DeletedAttachment
import com.syncodec.graphite.di.model.DeletedObject
import com.syncodec.graphite.di.model.NoteObject
import com.syncodec.graphite.di.model.NoteObjectLite
import com.syncodec.graphite.di.model.TagObject
import com.syncodec.graphite.di.model.TagObjectLite
import com.syncodec.graphite.di.repository.AttachmentRepository.Companion.attachmentDirPath
import com.syncodec.graphite.di.repository.group.RealmObjectGroup
import com.syncodec.graphite.di.repository.group.RealmObjectGroupList
import com.syncodec.graphite.presentation.settings.composable.viewModel.LocalBackupViewModel
import com.syncodec.graphite.service.syncInator.SyncInatorService
import com.syncodec.graphite.utils.RecursiveFileObserver
import com.syncodec.graphite.utils.SortBy
import com.syncodec.graphite.utils.SortOn
import com.syncodec.graphite.utils.alice.AliceRequestResult
import com.syncodec.graphite.utils.alice.getSecretData
import com.syncodec.graphite.utils.alice.putSecretData
import com.syncodec.graphite.utils.archiveUtil.CompressUtil
import com.syncodec.graphite.utils.copyInDirectory
import com.syncodec.graphite.utils.dataStore.DataStoreInstance
import com.syncodec.graphite.utils.encodeBase64
import com.syncodec.graphite.utils.scaleBitmap
import com.syncodec.graphite.utils.timeStampToPrettyDay
import io.realm.kotlin.MutableRealm
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.ext.query
import io.realm.kotlin.ext.toRealmList
import io.realm.kotlin.notifications.ResultsChange
import io.realm.kotlin.query.RealmResults
import io.realm.kotlin.schema.RealmSchema
import io.realm.kotlin.types.BaseRealmObject
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.RealmUUID
import io.realm.kotlin.types.TypedRealmObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.internal.closeQuietly
import java.io.File
import java.io.InputStream
import java.security.SecureRandom
import java.time.Instant
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1


class LockableRepo {

	private var repository: Repository? = null

	val repositoryStatusFlow: MutableStateFlow<Repository.Companion.RepositoryStatus> = MutableStateFlow(Repository.Companion.RepositoryStatus.Init)

	fun initRepository(context: Context, dataStoreInstance: DataStoreInstance, repository: Repository) {
		this.repository = repository
		repository.initRepository(context = context, dataStoreInstance = dataStoreInstance)
	}

	fun decryptRepository() {
		repository?.let { repository1 ->
			repository1.decryptRepository()
			repositoryStatusFlow.tryEmit(Repository.Companion.RepositoryStatus.Success(repository1))
		}
	}

}

class Repository {

	lateinit var context: Context
	lateinit var dataStoreInstance: DataStoreInstance
	val attachmentRepository = AttachmentRepository()

	private val sortByFlow: MutableStateFlow<SortBy?> = MutableStateFlow(null)
	private val sortOnFlow: MutableStateFlow<SortOn?> = MutableStateFlow(null)

	/**
	 * The state of the repository.This is used to determine if the repository is ready to be used. Use repository when [repositoryState] is [RepositoryState.Success].
	 */
	val repositoryState: MutableStateFlow<RepositoryState> = MutableStateFlow(RepositoryState.Init)

	var realm: Realm? = null
	private val _isUnlocked: MutableStateFlow<Boolean> = MutableStateFlow(false)
	val isUnlocked: StateFlow<Boolean> = _isUnlocked

	fun initRepository(context: Context, dataStoreInstance: DataStoreInstance) {
		this.context = context
		this.dataStoreInstance = dataStoreInstance
		initializeFilter()
	}

	fun lockRepo() {
		this._isUnlocked.tryEmit(false)
	}

	fun unlockRepo() {
		this._isUnlocked.tryEmit(true)
	}

	fun decryptRepository() {
		try {
			var key: ByteArray
			context.getSecretData("realmKey").let {
				if (it.result == AliceRequestResult.KEY_NOT_FOUND) {
					val realmKey = ByteArray(Realm.ENCRYPTION_KEY_LENGTH)
					SecureRandom().nextBytes(realmKey)
					context.putSecretData("realmKey", realmKey)
					key = context.getSecretData("realmKey").data!!
				} else {
					key = it.data!!
				}
			}

			if (BuildConfig.DEBUG) {
				key.joinToString(separator = "") { eachByte -> "%02x".format(eachByte) }.let {
					Log.d("npr71", "Realm Key: $it")
				}
			}

			val realmConfiguration = RealmConfiguration
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
				.encryptionKey(key)
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

			realm = Realm.open(realmConfiguration)
			repositoryState.value = RepositoryState.Success
			attachmentRepository.initRepository(context)
		} catch (e: Exception) {
			repositoryState.tryEmit(RepositoryState.Error)
//			e.printStackTrace()
		}
	}

	private fun initializeFilter() {
		CoroutineScope(Dispatchers.IO).apply {
			launch { dataStoreInstance.getSortBy.collectLatest { this@Repository.sortByFlow.tryEmit(it) } }
			launch { dataStoreInstance.getSortOn.collectLatest { this@Repository.sortOnFlow.tryEmit(it) } }
		}
	}

	fun getRealmSchema(): Pair<RealmSchema, Long>? {
		realm?.let {
			return Pair(it.schema(), it.schemaVersion())
		} ?: return null
	}

	/**
	 * Checks if the given [id] of [clazz] type exists in realm.
	 * Can have following extra:
	 *  *   [BaseObject]
	 *  *   [NoteObject]
	 *  *   [ChapterObject]
	 *  *   [BucketItemObject]
	 *  *   [BucketObject]
	 *  *   [TagObject]
	 */
	fun exists(clazz: KClass<out RealmObject>, id: RealmUUID?): Boolean {
		return if (clazz == BaseObject::class) {
			(realm?.query(BaseObject::class)?.count()?.find() ?: 0L) > 0
		} else {
			if (id == null) {
				(realm?.query(BaseObject::class)?.count()?.find() ?: 0L) > 0
			} else {
				(realm?.query(clazz, "id = $id")?.count()?.find() ?: 0L) > 0
			}
		}
	}

	suspend fun putDefaultChapterId(id: RealmUUID) {
		if (realm == null) throw RealmNotInitializedException()
		realm?.write {
			val baseObject = this.query(BaseObject::class).first().find()
			baseObject?.let { findLatest(it)?.defaultChapterId = id }
				?: BaseObject().also { _baseObject ->
					_baseObject.defaultChapterId = id
					copyToRealm(_baseObject)
				}
		}
	}

	/**
	 * Get the default chapterId stored in the [BaseObject] as flow. Flow should not be null.
	 * @author pushpull
	 * @since 2.2.0
	 * @throws [RealmNotInitializedException] if realm is not initialized.
	 */
	fun getDefaultChapterIdAsFlow(): Flow<RealmUUID?> {
		realm.let { realm ->
			return if (realm == null) throw RealmNotInitializedException()
			else realm.query(BaseObject::class).first().asFlow().map { it.obj?.defaultChapterId }
		}
	}

	/**
	 * Get the default chapterId stored in the [BaseObject]. This should not be null.
	 * @author pushpull
	 * @since 2.2.0
	 * @throws [RealmNotInitializedException] if realm is not initialized.
	 */
	fun getDefaultChapterId(): RealmUUID? {
		realm.let { realm ->
			return if (realm == null) throw RealmNotInitializedException()
			else realm.query(BaseObject::class).first().find()?.defaultChapterId
		}
	}

	/**
	 * Get BaseObject from realm
	 * @author pushpull
	 * @since 2.2.0
	 * @return BaseObject or null. Mostly not null because baseObject is created when realm is initialized.
	 * @throws [RealmNotInitializedException] if realm is not initialized.
	 */
	fun getBaseObject(): BaseObject? {
		realm.let { realm ->
			return if (realm == null) throw RealmNotInitializedException()
			else realm.query(BaseObject::class).first().find()
		}
	}

	/**
	 * Get BaseObject from realm as flow
	 * @author pushpull
	 * @since 2.2.0
	 * @return Flow of BaseObject. Mostly not null because baseObject is created when realm is initialized.
	 * @throws [RealmNotInitializedException] if realm is not initialized.
	 */
	fun getBaseObjectAsFlow(): Flow<BaseObject?> {
		realm.let { realm ->
			return if (realm == null) throw RealmNotInitializedException()
			else realm.query(BaseObject::class).first().asFlow().map { it.obj }
		}
	}

	fun downSyncBaseObject(baseObject: BaseObject, modifyTimestampAuto: Boolean = true) {
		realm?.writeBlocking {
			val storedBaseObject = getBaseObject()
			storedBaseObject?.let {
				findLatest(it)?.let { latestBaseObject ->
					latestBaseObject.modifiedTimestamp = if (modifyTimestampAuto) Instant.now().toEpochMilli() else latestBaseObject.modifiedTimestamp
					latestBaseObject.defaultChapterId = baseObject.defaultChapterId
					latestBaseObject.notebookIdOrderList = baseObject.notebookIdOrderList
					latestBaseObject.bucketIdOrderList = baseObject.bucketIdOrderList
				} ?: copyToRealm(baseObject)
			} ?: copyToRealm(baseObject)
		}
	}

	fun downSyncBaseObjectSuspended(baseObject: BaseObject, modifyTimestampAuto: Boolean = true) {
		CoroutineScope(Dispatchers.Default).launch { downSyncBaseObject(baseObject, modifyTimestampAuto) }
	}

	fun putChapter(chapterObject: ChapterObject, modifyTimestampAuto: Boolean = true) {
		realm?.writeBlocking {
			val storedChapterObject = getChapterFromId(chapterObject.id)
			storedChapterObject?.let {
				findLatest(it)?.let { latestChapterObject ->
					latestChapterObject.modifiedTimestamp = if (modifyTimestampAuto) Instant.now().toEpochMilli() else latestChapterObject.modifiedTimestamp
					latestChapterObject.title = chapterObject.title
					latestChapterObject.description = chapterObject.description
					latestChapterObject.color = chapterObject.color
					latestChapterObject.thumbnail = chapterObject.thumbnail
					latestChapterObject.isFavourite = chapterObject.isFavourite
					latestChapterObject.isLocked = chapterObject.isLocked
					latestChapterObject.parentId = chapterObject.parentId
				} ?: copyToRealm(chapterObject)
			} ?: copyToRealm(chapterObject)
		}
	}

	fun putChapterSuspended(chapterObject: ChapterObject, modifyTimestampAuto: Boolean = true) {
		CoroutineScope(Dispatchers.Default).launch { putChapter(chapterObject, modifyTimestampAuto) }
	}

	fun reorderNotebookList(idOrderList: List<RealmUUID>) {
		realm?.writeBlocking {
			val storedBaseObject = getBaseObject()
			storedBaseObject?.let {
				findLatest(it)?.let { latestBaseObject ->
					latestBaseObject.notebookIdOrderList = idOrderList.toRealmList()
				}
			}
		}
	}

	fun reorderNotebookListSuspended(idOrderList: List<RealmUUID>) {
		CoroutineScope(Dispatchers.Default).launch { reorderBucketList(idOrderList) }
	}

	fun getChapterFromIdAsFlow(id: RealmUUID?): Flow<ChapterObject?> {
		realm.let { realm ->
			return if (realm == null) throw RealmNotInitializedException()
			else realm.query(ChapterObject::class, "id == $0 ", id).first().asFlow().map { it.obj }
		}
	}

	/**
	 * Get all chapters as a list
	 * @author pushpull
	 * @since 2.0.0
	 * @throws [RealmNotInitializedException] if realm is not initialized
	 */
	fun getAllChapter(includeLocked: Boolean): List<ChapterObject> {
		realm.let { realm1 ->
			return if (realm1 == null) throw RealmNotInitializedException()
			else if (includeLocked) realm1.query<ChapterObject>().find().map { it } else realm1.query<ChapterObject>("isLocked == $0", false).find().map { it }
		}
	}

	/**
	 * Get all chapters as a flow of list
	 * @author pushpull
	 * @since 2.0.0
	 * @throws [RealmNotInitializedException] if realm is not initialized
	 */
	fun getAllChapterAsFlow(): Flow<List<ChapterObject>> {
		realm.let { realm ->
			return if (realm == null) throw RealmNotInitializedException()
			else realm.query(ChapterObject::class).asFlow().map { it.list.map { it } }
		}
	}

	/**
	 * Get chapter from its id
	 * @author pushpull
	 * @since 2.0.0
	 * @param id [RealmUUID] of the chapter. If null, no exceptions will be thrown but result will also be null.
	 * @return ChapterObject with the given id. If no chapter with the given id exists, null will be returned.
	 * @throws [RealmNotInitializedException] if realm is not initialized.
	 */
	fun getChapterFromId(id: RealmUUID?): ChapterObject? {
		realm.let { realm ->
			return if (realm == null) throw RealmNotInitializedException()
			else realm.query(ChapterObject::class, "id == $0 ", id).first().find()
		}
	}

	inline fun <reified T : TypedRealmObject> getAllObjectOfType(includeLocked: Boolean): List<T> {
		realm.let { realm ->
			return if (realm == null) throw RealmNotInitializedException()
			else if (includeLocked) realm.query<T>().find().map { it } else realm.query<T>("isLocked == $0", false).find().map { it }
		}
	}

	inline fun <reified T : TypedRealmObject> getObjectFromId(id: RealmUUID?): T? {
		realm.let { realm ->
			return if (realm == null) throw RealmNotInitializedException()
			else realm.query(T::class, "id == $0 ", id).first().find()
		}
	}

	suspend inline fun <reified T : TypedRealmObject> setObjectFromId(id: RealmUUID?, crossinline write: T.() -> Unit) {
		realm.let { realm ->
			if (realm == null) throw RealmNotInitializedException()
			realm.write {
				query(T::class, "id == $0 ", id).first().find()?.write()
			}
		}
	}

	inline fun <reified T : TypedRealmObject> setObjectFromIdSuspended(id: RealmUUID?, crossinline insert: MutableRealm.() -> Unit = {}, crossinline update: T.() -> Unit) {
		realm.let { realm ->
			if (realm == null) throw RealmNotInitializedException()
			CoroutineScope(Dispatchers.Default).launch {
				realm.write {
					query(T::class, "id == $0 ", id).first().find()?.apply {
						update()
					} ?: insert()
				}
			}
		}
	}

	inline fun <reified T : TypedRealmObject> setMultiObjectFromIdSuspended(idList: Set<RealmUUID>, crossinline write: T.() -> Unit) {
		realm.let { realm ->
			if (realm == null) throw RealmNotInitializedException()
			CoroutineScope(Dispatchers.Default).launch {
				idList.forEach { id ->
					realm.write { query(T::class, "id == $0 ", id).first().find()?.write() }
				}
			}
		}
	}

	/**
	 * Get list of [ChapterObject] with [parentId] as flow. If [parentId] is null, Notebooks are flowed.
	 * @author pushpull
	 * @since 2.0.0
	 * @throws [RealmNotInitializedException] if realm is not initialized.
	 */
	fun getChapterWithParentIdAsFlow(parentId: RealmUUID?): Flow<List<ChapterObject>> {
		realm.let { realm ->
			return if (realm == null) throw RealmNotInitializedException()
			else realm.query(ChapterObject::class, "parentId = $0", parentId)
				.asFlow()
				.extractList()
				.filterLocked(isLockedGetter = ChapterObject::isLocked)
				.applySortOnBy(
					idGetter = ChapterObject::id,
					titleGetter = ChapterObject::title,
					timestampGetter = ChapterObject::createdTimestamp,
					modifiedTimestampGetter = ChapterObject::modifiedTimestamp,
					customOrderFlow = getNotebookOrderAsFlow(),
				)
		}
	}

	fun getNotebookAsFlow(): Flow<List<ChapterObject>> {
		realm.let { realm ->
			return if (realm == null) throw RealmNotInitializedException()
			else realm.query(ChapterObject::class, "parentId = $0", null)
				.asFlow()
				.extractList()
				.filterLocked(isLockedGetter = ChapterObject::isLocked)
				.applySortOnBy(
					idGetter = ChapterObject::id,
					titleGetter = ChapterObject::title,
					timestampGetter = ChapterObject::createdTimestamp,
					modifiedTimestampGetter = ChapterObject::modifiedTimestamp,
					customOrderFlow = getNotebookOrderAsFlow(),
				)
		}
	}

	private fun getNotebookOrderAsFlow(): Flow<List<RealmUUID>> {
		return getBaseObjectAsFlow().map { it?.notebookIdOrderList ?: listOf() }
	}

	/**
	 * @author pushpull
	 * @since 2.0.0
	 * @param parentChapterId Id of the parent chapter. Null if the chapter is a notebook.
	 * @return Pair of the chapter and the child chapters list.
	 * @throws [RealmNotInitializedException] if the realm is not initialized.
	 */
	fun getChapterWithParentId(parentChapterId: RealmUUID?): RealmResults<ChapterObject> {
		realm.let { realm ->
			return if (realm == null) throw RealmNotInitializedException()
			else realm.query(ChapterObject::class, "parentId == $0 ", parentChapterId).find()
		}
	}

	/**
	 * Get the path of the chapter within the tree.
	 * @author pushpull
	 * @since 2.0.0
	 * @param id RealmUUID of the current chapter. Null if the chapter is a notebook.
	 * @param includeEdge If true, the current chapter will be included in the path.
	 * @param callback Callback with the path list and exception if thrown.
	 */
	fun getChapterPath(id: RealmUUID?, includeEdge: Boolean = false): List<ChapterObjectLite> {
		return try {
			val chapterObject = getChapterFromId(id)
			val chapterObjectList = mutableListOf<ChapterObjectLite>()
			if (includeEdge) chapterObject?.toLite()?.let { chapterObjectList.add(it) }
			var parentChapterObject = chapterObject?.parentId?.let { it1 -> getChapterFromId(it1) }
			while (parentChapterObject != null) {
				chapterObjectList.add(parentChapterObject.toLite())
				parentChapterObject = parentChapterObject.parentId?.let { it1 -> getChapterFromId(it1) }
			}
			chapterObjectList
		} catch (e: Exception) {
			listOf()
		}
	}

	/**
	 * Saves a note in the database or updates if already present. No need to pass the parent chapter id as it will read from [noteObject].
	 *
	 * @author pushpull
	 * @since 2.2.0
	 */
	fun putNote(noteObject: NoteObject, modifyTimestampAuto: Boolean = true) {
		realm?.writeBlocking {
			val storedNoteObject = getNoteFromId(noteObject.id)
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
					latestNoteObject.content2 = noteObject.content2
					latestNoteObject.thumbnail = noteObject.thumbnail
					latestNoteObject.isFavourite = noteObject.isFavourite
					latestNoteObject.isLocked = noteObject.isLocked
					latestNoteObject.parentId = noteObject.parentId
				} ?: copyToRealm(noteObject)
			} ?: copyToRealm(noteObject)
		}
	}

	fun putNoteSuspended(noteObject: NoteObject, modifyTimestampAuto: Boolean = true) {
		CoroutineScope(Dispatchers.Default).launch { putNote(noteObject, modifyTimestampAuto) }
	}

	fun putThumbnailInNote(noteId: RealmUUID, thumbnail: Bitmap) {
		CoroutineScope(Dispatchers.Default).launch {
			realm?.write {
				this@Repository.getNoteFromId(id = noteId)?.let { noteObject ->
					thumbnailCompressorLoop@ for (i in 1 until 10) {
						val thumbnailString = thumbnail.scaleBitmap(1024 * 1024 / i).encodeBase64() ?: break@thumbnailCompressorLoop
						if (thumbnailString.length < 4 * 1024 * 1024) {
							this.findLatest(noteObject)?.thumbnail = thumbnailString
							break@thumbnailCompressorLoop
						}
					}
				}
			}
		}
	}

	fun getNoteFromId(id: RealmUUID): NoteObject? {
		realm.let { realm ->
			return if (realm == null) throw RealmNotInitializedException()
			else realm.query(NoteObject::class, "id == $0 ", id).first().find()
		}
	}

	/**
	 * Observes the note with the given [id] and propagates the latest version.
	 *
	 * [id] can be null which can be used to observe if new note is saved.
	 * @author pushpull
	 * @since 2.2.0
	 * @throws [RealmNotInitializedException] if realm is not initialized
	 */
	fun getNoteFromIdAsFlow(id: RealmUUID?): Flow<NoteObject?> {
		realm.let { realm ->
			return if (realm == null) throw RealmNotInitializedException()
			else realm.query(NoteObject::class, "id == $0 ", id).first().asFlow().map { it.obj }
		}
	}

	fun getAllNoteAsFlow(): Flow<RealmResults<NoteObject>> {
		realm.let { realm ->
			return if (realm == null) throw RealmNotInitializedException()
			else realm.query(NoteObject::class).asFlow().map { it.list }
		}
	}

	fun getAllNoteLiteAsFlow(): Flow<List<NoteObjectLite>> {
//		realm!!.query(NoteObject::class).al
		realm.let { realm ->
			return if (realm == null) throw RealmNotInitializedException()
			else realm.let { it.query(NoteObject::class).asFlow().map { it.list.map { it.toLite() } } }
		}
	}

	fun getDefaultNoteLiteMapAsFlow(): Flow<List<NoteObjectLite>> {
		realm.let { realm ->
			return if (realm == null) throw RealmNotInitializedException()
			else realm.let { it.query(NoteObject::class).asFlow().map { it.list.map { it.toLite() } } }
		}
	}

	fun getDefaultNoteLiteMapAsFlow2(): Flow<RealmObjectGroupList<NoteObjectLite>> {
		realm.let { realm ->
			return if (realm == null) throw RealmNotInitializedException()
			else realm.let { realm1 ->
				realm1.query(NoteObject::class)
					.asFlow()
					.toLite { toLite() }
					.combine(getDefaultChapterIdAsFlow()) { noteList1, defaultChapterId1 -> noteList1.filter { it.parentId == defaultChapterId1 } }
					.filterLocked(isLockedGetter = NoteObjectLite::isLocked)
//					.mergeTag(idGetter = NoteObjectLite::id) { noteObjectLite, tagObjectList -> noteObjectLite.copy(tagList = tagObjectList) }
					.applyGroupOn(
						titleGetter = NoteObjectLite::title,
						timestampGetter = NoteObjectLite::userTimestamp,
						modifiedTimestampGetter = NoteObjectLite::modifiedTimestamp,
						customGetter = { it.userTimestamp.toString() }
					)
					.toGroupList()
//					.applySortOnBy(
//						titleGetter = NoteObjectLite::title,
//						timestampGetter = NoteObjectLite::userTimestamp,
//						modifiedTimestampGetter = NoteObjectLite::modifiedTimestamp,
//						customGetter = { noteObjectLite -> noteObjectLite.userTimestamp.timeStampToPrettyDay() },
//					)
			}
		}
	}

	private fun <T : BaseRealmObject> Flow<ResultsChange<T>>.extractList(): Flow<List<T>> {
		return map { it.list.toList() }
	}

	private fun <T : BaseRealmObject, R> Flow<ResultsChange<T>>.toLite(converter: T.() -> R): Flow<List<R>> {
		return map { it.list.toList().map { it.converter() } }
	}

	/**
	 * Filters locked objects from list of objects. Uses [isUnlocked] internally
	 * @author pushpull
	 * @since 3.0.0
	 * @param isLockedGetter Getter of locked property
	 * @sample getDefaultNoteLiteMapAsFlow
	 */
	private fun <T> Flow<List<T>>.filterLocked(isLockedGetter: KProperty1<T, Boolean>): Flow<List<T>> {
		return this.combine(isUnlocked) { objectList, isUnlocked1 -> if (isUnlocked1) objectList else objectList.filter { !isLockedGetter.get(it) } }
	}

	private fun <T> Flow<List<T>>.applyGroupOn(
		titleGetter: KProperty1<T, String?>,
		timestampGetter: KProperty1<T, Long>,
		modifiedTimestampGetter: KProperty1<T, Long>,
		customGetter: (T) -> String,
	): Flow<List<RealmObjectGroup<T>>> {
		return combine(this, sortOnFlow) { objectList1, sortOn1 ->
			objectList1.groupBy {
				sortOnKeySelector(
					t = it,
					sortOn = sortOn1,
					titleGetter = titleGetter,
					timestampGetter = timestampGetter,
					modifiedTimestampGetter = modifiedTimestampGetter,
					customGetter = customGetter,
				)
			}.map {
				RealmObjectGroup(
					title = it.key,
					objectList = it.value
				)
			}
		}
	}

	private fun <T> Flow<List<T>>.applySingleGroupOn(
		customGetter: (T) -> String,
	): Flow<List<RealmObjectGroup<T>>> {
		return this.map {
			listOf(
				RealmObjectGroup(
					title = "",
					objectList = it
				)
			)
		}
	}

	private fun <T> Flow<List<RealmObjectGroup<T>>>.toGroupList(): Flow<RealmObjectGroupList<T>> {
		return map {
			RealmObjectGroupList(
				groupList = it,
				totalSize = it.fold(0) { acc, realmObjectGroup -> acc + realmObjectGroup.objectList.size }
			)
		}
	}

	private fun <T> sortOnKeySelector(
		t: T,
		sortOn: SortOn?,
		titleGetter: KProperty1<T, String?>,
		timestampGetter: KProperty1<T, Long>,
		modifiedTimestampGetter: KProperty1<T, Long>,
		customGetter: (T) -> String,
	): String {
		return when (sortOn) {
			SortOn.Title -> titleGetter(t)?.firstOrNull()?.lowercase() ?: "."
			SortOn.Timestamp -> timestampGetter(t).timeStampToPrettyDay()
			SortOn.Modified -> modifiedTimestampGetter(t).timeStampToPrettyDay()
			else -> customGetter(t)
		}
	}

//	private fun <T> Flow<List<RealmObjectGroup<T>>>.applySortOnBy(
//		titleGetter: KProperty1<T, String?>,
//		timestampGetter: KProperty1<T, Long>,
//		modifiedTimestampGetter: KProperty1<T, Long>,
//		customGetter: (T) -> String,
//	): Flow<List<RealmObjectGroup<T>>> {
//		return this.combine(sortOnFlow) { realmObjectGroupList1, sortOn1 ->
//			realmObjectGroupList1.sortedBy { it.title }.map { realmObjectGroup ->
//				realmObjectGroup.copy(
//					objectList = realmObjectGroup.objectList.sortedBy { t ->
//						sortOnKeySelector(
//							t = t,
//							sortOn = sortOn1,
//							titleGetter = titleGetter,
//							timestampGetter = timestampGetter,
//							modifiedTimestampGetter = modifiedTimestampGetter,
//							customGetter = customGetter,
//						)
//					}
//				)
//			}
//		}.combine(sortByFlow) { realmObjectGroupList1, sortBy1 ->
//			when (sortBy1) {
//				SortBy.Ascending -> realmObjectGroupList1
//				SortBy.Descending -> realmObjectGroupList1.map { it.copy(objectList = it.objectList.reversed()) }.reversed()
//				else -> realmObjectGroupList1
//			}
//		}
//	}

	private fun <T> Flow<RealmObjectGroupList<T>>.applySortOnBy(
		titleGetter: KProperty1<T, String?>,
		timestampGetter: KProperty1<T, Long>,
		modifiedTimestampGetter: KProperty1<T, Long>,
		customGetter: (T) -> String,
	): Flow<RealmObjectGroupList<T>> {
		return this.combine(sortOnFlow) { realmObjectGroup1, sortOn1 ->
			realmObjectGroup1.copy(
				groupList = realmObjectGroup1.groupList.sortedBy { it.title }.map { realmObjectGroup ->
					realmObjectGroup.copy(
						objectList = realmObjectGroup.objectList.sortedBy { t ->
							sortOnKeySelector(
								t = t,
								sortOn = sortOn1,
								titleGetter = titleGetter,
								timestampGetter = timestampGetter,
								modifiedTimestampGetter = modifiedTimestampGetter,
								customGetter = customGetter,
							)
						}
					)
				}
			)
		}.combine(sortByFlow) { realmObjectGroupList1, sortBy1 ->
			when (sortBy1) {
				SortBy.Ascending -> realmObjectGroupList1
				SortBy.Descending -> realmObjectGroupList1.copy(groupList = realmObjectGroupList1.groupList.map { it.copy(objectList = it.objectList.reversed()) }.reversed())
				else -> realmObjectGroupList1
			}
		}
	}

	private fun <T> Flow<List<T>>.applySortOnBy(
		idGetter: KProperty1<T, RealmUUID>,
		titleGetter: KProperty1<T, String?>,
		timestampGetter: KProperty1<T, Long>,
		modifiedTimestampGetter: KProperty1<T, Long>,
		customOrderFlow: Flow<List<RealmUUID>>? = null
	): Flow<List<T>> {
		if (customOrderFlow == null) {
			return combine(this, sortOnFlow, sortByFlow) { realmObjectList1, sortOn1, sortBy1 ->
				realmObjectList1.sortedBy { t ->
					sortOnKeySelector(
						t = t,
						sortOn = sortOn1,
						titleGetter = titleGetter,
						timestampGetter = timestampGetter,
						modifiedTimestampGetter = modifiedTimestampGetter,
						customGetter = { "" }
					)
				}.let {
					when (sortBy1) {
						SortBy.Ascending -> it
						SortBy.Descending -> it.reversed()
						else -> it
					}
				}
			}
		} else {
			return combine(this, sortOnFlow, customOrderFlow, sortByFlow) { realmObjectList1, sortOn1, customOrder1, sortBy1 ->
				if (sortOn1 == SortOn.Custom) {
					realmObjectList1.sortedBy { customOrder1.indexOf(idGetter(it)) }
				} else {
					realmObjectList1.sortedBy { t ->
						sortOnKeySelector(
							t = t,
							sortOn = sortOn1,
							titleGetter = titleGetter,
							timestampGetter = timestampGetter,
							modifiedTimestampGetter = modifiedTimestampGetter,
							customGetter = { "" }
						)
					}.let {
						when (sortBy1) {
							SortBy.Ascending -> it
							SortBy.Descending -> it.reversed()
							else -> it
						}
					}
				}
			}
		}
	}


	private fun <T> Flow<List<T>>.mergeTag(
		idGetter: KProperty1<T, RealmUUID>,
		merge: (T, List<TagObjectLite>) -> T
	): Flow<List<T>> {
		return this.combine(getAllTagAsFlow()) { objectList1, tagList1 ->
			objectList1.map { realmObject: T ->
				val containedTagList = tagList1.filter { it.objectIdList.contains(idGetter(realmObject)) }.map { it.toLite() }
				merge(realmObject, containedTagList)
			}
		}
	}

	/**
	 * Get all notes with [parentId] as flow.
	 * @throws [RealmNotInitializedException] if realm is not initialized
	 */
	fun getNoteWithParentIdAsFlow(parentId: RealmUUID?): Flow<List<NoteObjectLite>> {
		realm.let { realm ->
			return if (realm == null) throw RealmNotInitializedException()
			else realm.query(NoteObject::class, "parentId = $0", parentId)
				.asFlow()
				.toLite { toLite() }
				.filterLocked(isLockedGetter = NoteObjectLite::isLocked)
				.applySortOnBy(
					idGetter = NoteObjectLite::id,
					titleGetter = NoteObjectLite::title,
					timestampGetter = NoteObjectLite::createdTimestamp,
					modifiedTimestampGetter = NoteObjectLite::modifiedTimestamp,
					customOrderFlow = getNotebookOrderAsFlow(),
				)
		}
	}

	fun getNoteWithParentId(parentId: RealmUUID): RealmResults<NoteObject> {
		realm.let { realm ->
			return if (realm == null) throw RealmNotInitializedException()
			else realm.query(NoteObject::class, "parentId = $0", parentId).find()
		}
	}

	/**
	 * Get all notes as a list
	 * @author pushpull
	 * @since 2.0.0
	 * @return List of all NoteObject from realm
	 * @throws [RealmNotInitializedException] if realm is not initialized
	 */
	fun getAllNote(includeLocked: Boolean = false): List<NoteObject> {
		realm.let { realm1 ->
			return if (realm1 == null) throw RealmNotInitializedException()
			else if (includeLocked) realm1.query<NoteObject>().find().map { it } else realm1.query<NoteObject>("isLocked == $0", false).find().map { it }
		}
	}

	fun putBucket(bucketObject: BucketObject, modifyTimestampAuto: Boolean = true, googleDriveId: String? = null) {
		realm?.writeBlocking {
			val storedBucketObject = getBucketFromId(bucketObject.id)
			storedBucketObject?.let {
				findLatest(it)?.let { latestBucketItemObject ->
					latestBucketItemObject.createdTimestamp = bucketObject.createdTimestamp
					latestBucketItemObject.modifiedTimestamp = if (modifyTimestampAuto) Instant.now().toEpochMilli() else bucketObject.modifiedTimestamp
					latestBucketItemObject.title = bucketObject.title
					latestBucketItemObject.description = bucketObject.description
					latestBucketItemObject.bucketType = bucketObject.bucketType
					latestBucketItemObject.isFavourite = bucketObject.isFavourite
					latestBucketItemObject.isLocked = bucketObject.isLocked
					latestBucketItemObject.googleDriveId = googleDriveId ?: latestBucketItemObject.googleDriveId
				} ?: copyToRealm(bucketObject)
			} ?: copyToRealm(bucketObject)
		}
	}

	fun putBucketSuspended(bucketObject: BucketObject, modifyTimestampAuto: Boolean = true, googleDriveId: String? = null) {
		CoroutineScope(Dispatchers.Default).launch { putBucket(bucketObject, modifyTimestampAuto, googleDriveId) }
	}

	fun reorderBucketList(idOrderList: List<RealmUUID>) {
		realm?.writeBlocking {
			val storedBaseObject = getBaseObject()
			storedBaseObject?.let {
				findLatest(it)?.let { latestBaseObject ->
					latestBaseObject.bucketIdOrderList = idOrderList.toRealmList()
				}
			}
		}
	}

	fun reorderBucketListSuspended(idOrderList: List<RealmUUID>) {
		CoroutineScope(Dispatchers.Default).launch { reorderBucketList(idOrderList) }
	}

	fun putBucketItem(bucketItemObject: BucketItemObject, modifyTimestampAuto: Boolean = true, googleDriveId: String? = null) {
		realm?.writeBlocking {
			val storedBucketItemObject = getBucketItemFromId(bucketItemObject.id)
			storedBucketItemObject?.let {
				findLatest(it)?.let { latestBucketItemObject ->
					latestBucketItemObject.createdTimestamp = bucketItemObject.createdTimestamp
					latestBucketItemObject.modifiedTimestamp = if (modifyTimestampAuto) Instant.now().toEpochMilli() else bucketItemObject.modifiedTimestamp
					latestBucketItemObject.bucketType = bucketItemObject.bucketType
					latestBucketItemObject.title = bucketItemObject.title
					latestBucketItemObject.state = bucketItemObject.state
					latestBucketItemObject.thumbnail = bucketItemObject.thumbnail
					latestBucketItemObject.isFavourite = bucketItemObject.isFavourite
					latestBucketItemObject.isLocked = bucketItemObject.isLocked
					latestBucketItemObject.parentId = bucketItemObject.parentId
					latestBucketItemObject.key = bucketItemObject.key
					latestBucketItemObject.data = bucketItemObject.data
					latestBucketItemObject.googleDriveId = googleDriveId ?: latestBucketItemObject.googleDriveId
				} ?: copyToRealm(bucketItemObject)
			} ?: copyToRealm(bucketItemObject)
		}
	}

	fun putBucketItemSuspended(bucketItemObject: BucketItemObject, modifyTimestampAuto: Boolean = true, googleDriveId: String? = null) {
		CoroutineScope(Dispatchers.Default).launch { putBucketItem(bucketItemObject, modifyTimestampAuto) }
	}

	fun reorderBucketItemList(parentId: RealmUUID, idOrderList: List<RealmUUID>) {
		setObjectFromIdSuspended<BucketObject>(id = parentId) {
			this.bucketItemOrderList = idOrderList.toRealmList()
		}
	}

	fun getAllBucketAsFlow(): Flow<List<BucketObject>> {
		realm.let { realm ->
			return if (realm == null) throw RealmNotInitializedException()
			else realm.query(BucketObject::class).asFlow().map { it.list }.combine(isUnlocked) { bucketList, isAuthenticated ->
				if (isAuthenticated) bucketList else bucketList.filter { !it.isLocked }
			}
		}
	}

	fun getAllBucketLiteAsFlow(): Flow<List<BucketObjectLite>> {
		realm.let { realm ->
			return if (realm == null) throw RealmNotInitializedException()
			else realm.query(BucketObject::class)
				.asFlow()
				.toLite { toLite() }
				.filterLocked(isLockedGetter = BucketObjectLite::isLocked)
				.mergeBucketSize()
				.applySortOnBy(
					idGetter = BucketObjectLite::id,
					titleGetter = BucketObjectLite::title,
					timestampGetter = BucketObjectLite::createdTimestamp,
					modifiedTimestampGetter = BucketObjectLite::modifiedTimestamp,
					customOrderFlow = getBucketOrderAsFlow(),
				)
		}
	}

	private fun Flow<List<BucketObjectLite>>.mergeBucketSize(): Flow<List<BucketObjectLite>> {
		return combine(getAllBucketSizeAsFlow()) { bucketList1, bucketSizeMap1 ->
			bucketList1.map { it.copy(bucketItemCount = bucketSizeMap1[it.id] ?: 0) }
		}
	}

	private fun getBucketOrderAsFlow(): Flow<List<RealmUUID>> {
		return getBaseObjectAsFlow().map { it?.bucketIdOrderList ?: listOf() }
	}

	/**
	 * Get all buckets as a list
	 * @author pushpull
	 * @since 2.2.0
	 * @return List of all BucketObject
	 * @throws [RealmNotInitializedException] if realm is not initialized.
	 */
	fun getAllBucket(): List<BucketObject> {
		realm.let { realm ->
			return if (realm == null) throw RealmNotInitializedException()
			else realm.query(BucketObject::class).find().map { it }
		}
	}

	fun getBucketAsFlow(id: RealmUUID): Flow<BucketObject?> {
		realm.let { realm ->
			return if (realm == null) throw RealmNotInitializedException()
			else realm.query(BucketObject::class, "id == $0 ", id).first().asFlow().map { it.obj }
		}
	}

	fun getBucketFromId(id: RealmUUID): BucketObject? {
		realm.let { realm ->
			return if (realm == null) throw RealmNotInitializedException()
			else realm.query(BucketObject::class, "id == $0 ", id).first().find()
		}
	}

	fun getAllBucketSizeAsFlow(): Flow<Map<RealmUUID?, Int>> {
		realm.let { realm ->
			return if (realm == null) throw RealmNotInitializedException()
			else realm.query(BucketItemObject::class).asFlow().combine(isUnlocked) { bucketItemList1, isAuthenticated1 ->
				if (isAuthenticated1) bucketItemList1.list else bucketItemList1.list.filter { !it.isLocked }
			}.map { it.groupBy { it.parentId }.mapValues { it.value.size } }
		}
	}

	/**
	 * Returns flow of bucket item with provided id as flow
	 * @author pushpull
	 * @since 2.2.0
	 * @param id (RealmUUID) of the bucket item. Can be null but then it will return null.
	 * @return Flow of BucketItemObject with provided id.
	 * @throws [RealmNotInitializedException] if realm is not initialized.
	 */
	fun getBucketItemAsFlow(id: RealmUUID?): Flow<BucketItemObject?> {
		realm.let { realm ->
			return if (realm == null) throw RealmNotInitializedException()
			else realm.query(BucketItemObject::class, "id == $0 ", id).first().asFlow().map { it.obj }
		}
	}

	/**
	 * Returns flow of bucket item with provided parent id as flow.
	 * @author pushpull
	 * @since 2.2.0
	 * @param parentId RealmUUID of the parent bucket.
	 * @return Flow of RealmResults of BucketItemObject with provided parent id.
	 * @throws [RealmNotInitializedException] if realm is not initialized.
	 */
	fun getBucketItemWithParentIdAsFlow(parentId: RealmUUID): Flow<RealmResults<BucketItemObject>> {
		realm.let { realm ->
			return if (realm == null) throw RealmNotInitializedException()
			else realm.query(BucketItemObject::class, "parentId == $0 ", parentId).asFlow().map { it.list }
		}
	}

	fun getBucketItemWithParentId(parentId: RealmUUID?): List<BucketItemObject> {
		realm.let { realm ->
			return if (realm == null) throw RealmNotInitializedException()
			else realm.query(BucketItemObject::class, "parentId == $0 ", parentId).find().map { it }
		}
	}

	/**
	 * Get all bucket items as a list
	 * @author pushpull
	 * @since 2.2.0
	 * @return List of all BucketItemObject
	 * @throws [RealmNotInitializedException] if realm is not initialized.
	 */
	fun getAllBucketItem(): List<BucketItemObject> {
		realm.let { realm ->
			return if (realm == null) throw RealmNotInitializedException()
			else realm.query(BucketItemObject::class).find().map { it }
		}
	}

	fun getAllBucketItemAsFlow(): Flow<RealmResults<BucketItemObject>> {
		realm.let { realm ->
			return if (realm == null) throw RealmNotInitializedException()
			else realm.query(BucketItemObject::class).asFlow().map { it.list }
		}
	}

	fun getBucketItemFromId(id: RealmUUID): BucketItemObject? {
		realm.let { realm ->
			return if (realm == null) throw RealmNotInitializedException()
			else realm.query(BucketItemObject::class, "id == $0 ", id).first().find()
		}
	}

	fun putTag(tagObject: TagObject, modifyTimestampAuto: Boolean = true, googleDriveId: String? = null) {
		realm?.writeBlocking {
			val storedTagObject = getTagFromId(tagObject.id)
			storedTagObject?.let {
				findLatest(it)?.let { latestTagObject ->
					latestTagObject.modifiedTimestamp = if (modifyTimestampAuto) Instant.now().toEpochMilli() else latestTagObject.modifiedTimestamp
					latestTagObject.tag = tagObject.tag
					latestTagObject.color = tagObject.color
					latestTagObject.googleDriveId = googleDriveId ?: latestTagObject.googleDriveId
				} ?: copyToRealm(tagObject)
			} ?: copyToRealm(tagObject)
		}
	}

	fun putTagSuspended(tagObject: TagObject, modifyTimestampAuto: Boolean = true, googleDriveId: String? = null) {
		CoroutineScope(Dispatchers.Default).launch { putTag(tagObject, modifyTimestampAuto, googleDriveId) }
	}

	fun getTagFromId(id: RealmUUID?): TagObject? {
		realm.let { realm ->
			return if (realm == null) throw RealmNotInitializedException()
			else realm.query(TagObject::class, "id == $0", id).first().find()
		}
	}

	fun updateTagConnections(id: RealmUUID, tagListToAdd: List<RealmUUID>, tagListToRemove: List<RealmUUID>) {
		CoroutineScope(Dispatchers.Default).launch {
			realm?.write {
				tagListToRemove.forEach {
					getTagFromId(it)?.let { tagObject -> findLatest(tagObject)?.objectIdList?.remove(id) }
				}
				tagListToAdd.forEach {
					getTagFromId(it)?.let { tagObject -> findLatest(tagObject)?.objectIdList?.add(id) }
				}
			} ?: throw RealmNotInitializedException()
		}
	}

	/**
	 * Get all tags as a flow list and observe changes
	 * @author pushpull
	 * @since 2.0.0
	 * @throws [RealmNotInitializedException] if realm is not initialized
	 */
	fun getAllTagAsFlow(): Flow<List<TagObject>> {
		realm.let { realm ->
			return if (realm == null) throw RealmNotInitializedException()
			else realm.query(TagObject::class).asFlow().map { it.list }
		}
	}

	/**
	 * Get all tags as a list
	 * @author pushpull
	 * @since 2.0.0
	 * @throws [RealmNotInitializedException] if realm is not initialized
	 */
	fun getAllTag(): List<TagObject> {
		realm.let { realm ->
			return if (realm == null) throw RealmNotInitializedException()
			else realm.query(TagObject::class).find().map { it }
		}
	}

	fun deleteAttachment(attachmentList: List<File>, keepHistory: Boolean = true) {
		if (keepHistory) attachmentList
			.map {
				DeletedAttachment()
					.apply {
						it.parentFile?.name?.let { it1 -> RealmUUID.Companion.from(it1) }?.let { this.parentId = it }
						this.fileName = it.name
					}
			}.let { deletedFileList ->
				getBaseObject()?.let { realm?.writeBlocking { findLatest(it)?.deletedAttachmentSet?.addAll(deletedFileList) } }
			}
		attachmentRepository.delete(attachmentList)
	}

	/**
	 * DownSync attachment to delete and preserve history(optional but recommended)
	 *
	 * @author pushpull
	 * @since 2.4.0
	 * @param id [SyncInatorService.Companion.AttachmentMetadata.Companion.AttachmentIdentity]
	 * @param keepHistory if true, id will be added to [BaseObject.deletedAttachmentSet]
	 */
	fun deleteAttachment(attachmentIdentity: SyncInatorService.Companion.AttachmentMetadata.Companion.AttachmentIdentity, keepHistory: Boolean = true) {
		if (keepHistory) attachmentIdentity
			.let {
				DeletedAttachment()
					.apply {
						this.parentId = it.parentId
						this.fileName = it.fileName
					}
			}.let { deletedFile ->
				getBaseObject()?.let { CoroutineScope(Dispatchers.Default).launch { realm?.write { findLatest(it)?.deletedAttachmentSet?.add(deletedFile) } } }
			}
		attachmentRepository.delete(attachmentIdentity.parentId, attachmentIdentity.fileName)
	}

	/**
	 * Deletes [NoteObject], [ChapterObject], [BucketItemObject], [BucketObject] or [TagObject]
	 *
	 * [NoteObject] will delete all associated [AttachmentObject]
	 *
	 * [ChapterObject] will delete all [NoteObject]s with [ChapterObject.id] as [NoteObject.parentId] and [ChapterObject]s with [ChapterObject.parentId] as [ChapterObject.id]
	 *
	 * [BucketObject] will delete all [BucketItemObject]s with [BucketObject.id] as [BucketItemObject.parentId]
	 *
	 * [TagObject] will not delete any associated objects
	 * @author pushpull
	 * @since 2.3.0
	 * @param id [RealmUUID] of [RealmObject]
	 * @param keepHistory if true, id will be added to [BaseObject.deletedObjectSet]
	 */
	private fun delete(id: RealmUUID, keepHistory: Boolean) {
		getNoteFromId(id)?.let {
			deleteAttachment(attachmentRepository.getAttachmentFromNote(it.id), keepHistory)
			attachmentRepository.delete(it.id)
			realm?.writeBlocking {
				if (keepHistory) updateDeleteHistory(id, NoteObject::class.simpleName)
				findLatest(it)?.let { delete(it) }
			}
		}
		getChapterFromId(id)?.let {
			delete(getChapterWithParentId(id).map { it.id }, keepHistory)
			delete(getNoteWithParentId(id).map { it.id }, keepHistory)
			realm?.writeBlocking {
				if (keepHistory) updateDeleteHistory(id, ChapterObject::class.simpleName)
				findLatest(it)?.let { delete(it) }
			}
		}
		getBucketItemFromId(id)?.let {
			realm?.writeBlocking {
				if (keepHistory) updateDeleteHistory(id, BucketItemObject::class.simpleName)
				findLatest(it)?.let { delete(it) }
			}
		}
		getBucketFromId(id)?.let {
			delete(getBucketItemWithParentId(id).map { it.id }, keepHistory)
			realm?.writeBlocking {
				if (keepHistory) updateDeleteHistory(id, BucketObject::class.simpleName)
				findLatest(it)?.let { delete(it) }
			}
		}
		getTagFromId(id)?.let {
			realm?.writeBlocking {
				if (keepHistory) updateDeleteHistory(id, TagObject::class.simpleName)
				findLatest(it)?.let { delete(it) }
			}
		}
	}

	private fun MutableRealm.updateDeleteHistory(id: RealmUUID, objectType: String?) {
		getBaseObject()?.let { baseObject ->
			DeletedObject().apply {
				this.id = id
				this.deletedTimestamp = Instant.now().toEpochMilli()
				this.objectType = objectType
				findLatest(baseObject)?.deletedObjectSet?.add(this@apply)
			}
		}
	}

	private fun delete(idList: Collection<RealmUUID>, keepHistory: Boolean = true) = idList.forEach { delete(it, keepHistory) }

	fun deleteSuspended(id: RealmUUID, keepHistory: Boolean = true, callback: suspend () -> Unit = {}) {
		CoroutineScope(Dispatchers.Default).launch {
			delete(id, keepHistory)
			callback()
		}
	}

	fun deleteSuspended(idList: Collection<RealmUUID>, keepHistory: Boolean = true, callback: suspend () -> Unit = {}) {
		CoroutineScope(Dispatchers.Default).launch {
			delete(idList, keepHistory)
			callback()
		}
	}

	/**
	 * Clears everything from realm. It does not reinitialize realm with default values. See [initializeRealmSuspended].
	 * @author pushpull
	 * @since 2.2.0
	 * @return Callback with true if successful, false if not along with exception
	 * @throws [RealmNotInitializedException] if realm is not initialized.
	 */
	fun clearRealmSuspended(callback: (Boolean, Exception?) -> Unit) = realm?.let {
		CoroutineScope(Dispatchers.Default).launch {
			try {
				attachmentRepository.deleteAll()
				it.writeBlocking { deleteAll() }
				callback(true, null)
			} catch (e: Exception) {
				callback(false, e)
			}
		}
	} ?: callback(false, RealmNotInitializedException())

	/**
	 * Clears everything from realm and reinitialize realm with default values. See [initializeRealmSuspended].
	 * @author pushpull
	 * @since 3.0.0
	 * @return true if successful, false if not
	 */
	suspend fun resetRealm(): Boolean {
		return try {
			attachmentRepository.deleteAll()
			realm?.write {
				deleteAll()
				val chapterObject = ChapterObject().apply {
					this.title = "Diary"
					this.description = "Default diary. Every notes will be saved in this notebook by default"
				}
				val baseObject = BaseObject().apply {
					this.defaultChapterId = chapterObject.id
				}
				copyToRealm(chapterObject)
				copyToRealm(baseObject)
			}
			true
		} catch (e: Exception) {
			if (BuildConfig.DEBUG) e.printStackTrace()
			false
		}
	}

	/**
	 * Initialize realm with default values. It does not clear anything from realm. See [clearRealmSuspended].
	 * @author pushpull
	 * @since 2.2.0
	 * @return Callback with true if successful, false if not along with exception
	 * @throws [RealmNotInitializedException] if realm is not initialized.
	 */
	fun initializeRealmSuspended(callback: (Boolean, Exception?) -> Unit) = realm?.let {
		CoroutineScope(Dispatchers.Default).launch {
			try {
				it.write { copyToRealm(BaseObject()) }
				ChapterObject().apply {
					this.title = "Diary"
					this.description = "Default diary. Every notes will be saved in this notebook by default"
					putChapter(chapterObject = this)
					putDefaultChapterId(this.id)
					callback(true, null)
				}
			} catch (e: Exception) {
				callback(false, e)
			}
		}
	} ?: callback(false, RealmNotInitializedException())

	fun getRealmSnapshot(name: String, path: String) {
		CoroutineScope(Dispatchers.Default).launch {
			val realmConfiguration = RealmConfiguration
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
				.schemaVersion(SCHEMA_VERSION)
				.directory(path)
				.name(name)
				.migration(RealmMigrator())
				.build()
			realm?.writeCopyTo(realmConfiguration)
		}
	}

	fun restoreRealmSnapshot(name: String, path: String, callback: (Boolean, Exception?) -> Unit) {
		CoroutineScope(Dispatchers.Default).launch {
			try {
				val realmConfiguration = RealmConfiguration
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
					.schemaVersion(SCHEMA_VERSION)
					.directory(path)
					.name(name)
					.migration(RealmMigrator())
					.build()

				val snapshotRealm = Realm.open(realmConfiguration)

				var key: ByteArray
				context.getSecretData("realmKey").let {
					key = if (it.result == AliceRequestResult.KEY_NOT_FOUND) {
						val realmKey = ByteArray(Realm.ENCRYPTION_KEY_LENGTH)
						SecureRandom().nextBytes(realmKey)
						context.putSecretData("realmKey", realmKey)
						context.getSecretData("realmKey").data!!
					} else {
						it.data!!
					}
				}

				realm?.close()
				File(context.filesDir, "default.realm").delete()

				val observer = RecursiveFileObserver(
					mPath = context.filesDir.path,
					mask = FileObserver.CLOSE_WRITE,
					mListener = object : RecursiveFileObserver.EventListener {
						override fun onEvent(event: Int, file: File?) {
							if (file?.name == "default.realm" && event == FileObserver.CLOSE_WRITE) callback(true, null)
						}
					}
				)
				observer.startWatching()

				val realmConfiguration2 = RealmConfiguration
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
					.encryptionKey(key)
					.schemaVersion(SCHEMA_VERSION)
					.migration(RealmMigrator())
					.build()

				snapshotRealm.writeCopyTo(realmConfiguration2)
				snapshotRealm.close()
			} catch (e: Exception) {
//				e.printStackTrace()
				callback(false, e)
			}
		}
	}

	val snapshot = Snapshot()

	inner class Snapshot {

		private fun getTmpImportSnapshotDir() = File(context.cacheDir, "importSnapshot").also {
			it.deleteRecursively()
			it.mkdirs()
		}

		@WorkerThread
		fun generate(callback: (File) -> Unit) {
			val snapshotDir = File(context.cacheDir, "snapshot").also {
				it.deleteRecursively()
				it.mkdirs()
			}
			val fileName = "graphite_snapshot_${Instant.now().toEpochMilli()}"
			val currentSnapshotDir = File(snapshotDir, fileName).also {
				it.mkdirs()
			}

			val snapshotMetadata = LocalBackupViewModel.Companion.SnapshotMetadata(
				timestamp = Instant.now().toEpochMilli(),
				noteCount = getAllObjectOfType<NoteObject>(includeLocked = true).size,
				chapterCount = getAllObjectOfType<ChapterObject>(includeLocked = true).size,
				bucketItemCount = getAllObjectOfType<BucketItemObject>(includeLocked = true).size,
				bucketCount = getAllObjectOfType<BucketObject>(includeLocked = true).size,
				tagCount = getAllObjectOfType<TagObject>(includeLocked = true).size,
				attachmentCount = attachmentRepository.countTotalAttachment(),
			)

			val observer = RecursiveFileObserver(
				mPath = currentSnapshotDir.path,
				mask = FileObserver.CLOSE_WRITE,
				mListener = object : RecursiveFileObserver.EventListener {
					override fun onEvent(event: Int, file: File?) {
						if (event == FileObserver.CLOSE_WRITE && file == File(currentSnapshotDir, "$fileName.realm")) {

							val attachmentFolder = File(currentSnapshotDir, "attachment").also { it.mkdirs() }
							val snapshotMetadataFile = File(currentSnapshotDir, "metadata.json")
							snapshotMetadataFile.createNewFile()
							snapshotMetadataFile.writeText(Json.encodeToString(snapshotMetadata))

							copyInDirectory(File(context.attachmentDirPath()), attachmentFolder)

							val zipFile = File(snapshotDir, "${fileName}.zip")
							CompressUtil.Zip.createZipFile(currentSnapshotDir, zipFile)
							callback(zipFile)
						}
					}
				}
			)

			observer.startWatching()
			getRealmSnapshot("$fileName.realm", currentSnapshotDir.path)
		}

		fun restore(inputStream: InputStream, is7z : Boolean = false, callback: (Boolean) -> Unit) {
			val importSnapshotDir = getTmpImportSnapshotDir()

			val snapshotDir = if (is7z) {
				val sevenZImportFile = File(importSnapshotDir, "graphite_snapshot.7z")
				sevenZImportFile.outputStream().use { outputStream ->
					inputStream.copyTo(outputStream)
					inputStream.closeQuietly()
				}

				val snapshotDir1 = File(importSnapshotDir, "snapshot")
				CompressUtil.SevenZ.extractSevenZFile(inputFile = sevenZImportFile, outputFile = snapshotDir1)
				snapshotDir1
			} else {
				val zipImportFile = File(importSnapshotDir, "graphite_snapshot.zip")
				zipImportFile.outputStream().use { outputStream ->
					inputStream.copyTo(outputStream)
					inputStream.closeQuietly()
				}

				val snapshotDir1 = File(importSnapshotDir, "snapshot")
				CompressUtil.Zip.extractZipFile(inputFile = zipImportFile, outputFile = snapshotDir1)
				snapshotDir1
			}

			// Delete attachment folder
			attachmentRepository.deleteAll()

			snapshotDir.listFiles()?.firstOrNull { it.name.endsWith(".realm") }?.let { realmFile ->
				restoreRealmSnapshot(realmFile.name, snapshotDir.path) { isSuccess, exception ->
					if (BuildConfig.DEBUG) exception?.printStackTrace()
					if (isSuccess) {
						snapshotDir.listFiles()?.firstOrNull { it.name == "attachment" }?.let { attachmentDir ->
							attachmentRepository.importAttachmentFromGraphite(attachmentDir)
							ProcessPhoenix.triggerRebirth(context)
						} ?: kotlin.run {
//							Trigger rebirth if attachment folder is not found
							ProcessPhoenix.triggerRebirth(context)
						}
					} else callback(false)
				}
			}
		}
	}

	companion object {

		const val SCHEMA_VERSION = 5L

		sealed class RepositoryStatus {
			data object Init : RepositoryStatus()
			data object Loading : RepositoryStatus()
			data object Locked : RepositoryStatus()
			data class Success(val repository: Repository) : RepositoryStatus()
			data object Error : RepositoryStatus()
		}

		enum class RepositoryState {
			Init,
			Locked,
			Loading,
			Success,
			Error,
		}

		sealed class RealmSnapshotCopyStatus {
			data object Success : RealmSnapshotCopyStatus()
			data object Error : RealmSnapshotCopyStatus()
			data class InProgress(val processed: Int, val total: Int) : RealmSnapshotCopyStatus()
		}

		class RealmNotInitializedException : Exception("Realm not initialized")
	}
}
