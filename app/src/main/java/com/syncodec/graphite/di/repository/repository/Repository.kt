package com.syncodec.graphite.di.repository.repository

import android.content.Context
import android.graphics.Bitmap
import android.os.FileObserver
import android.util.Log
import com.jakewharton.processphoenix.ProcessPhoenix
import com.syncodec.graphite.BuildConfig
import com.syncodec.graphite.di.model.BaseObject
import com.syncodec.graphite.di.model.BucketItemObject
import com.syncodec.graphite.di.model.BucketObject
import com.syncodec.graphite.di.model.ChapterObject
import com.syncodec.graphite.di.model.ChapterObjectLite
import com.syncodec.graphite.di.model.DeletedAttachment
import com.syncodec.graphite.di.model.DeletedObject
import com.syncodec.graphite.di.model.NoteObject
import com.syncodec.graphite.di.model.NoteObjectLite
import com.syncodec.graphite.di.model.TagObject
import com.syncodec.graphite.di.repository.AttachmentRepository
import com.syncodec.graphite.di.repository.AttachmentRepository.Companion.attachmentDirPath
import com.syncodec.graphite.di.repository.RealmMigrator
import com.syncodec.graphite.service.syncInator.SyncInatorService
import com.syncodec.graphite.utils.RecursiveFileObserver
import com.syncodec.graphite.utils.alice.AliceRequestResult
import com.syncodec.graphite.utils.alice.getSecretData
import com.syncodec.graphite.utils.alice.putSecretData
import com.syncodec.graphite.utils.compress7z
import com.syncodec.graphite.utils.copyInDirectory
import com.syncodec.graphite.utils.encodeBase64
import com.syncodec.graphite.utils.extract7z
import com.syncodec.graphite.utils.scaleBitmap
import io.realm.kotlin.MutableRealm
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.ext.toRealmList
import io.realm.kotlin.notifications.ResultsChange
import io.realm.kotlin.query.RealmResults
import io.realm.kotlin.schema.RealmSchema
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.apache.commons.compress.archivers.sevenz.SevenZFile
import org.apache.commons.compress.archivers.sevenz.SevenZOutputFile
import java.io.File
import java.io.InputStream
import java.security.SecureRandom
import java.time.Instant
import kotlin.reflect.KClass


class Repository {

	lateinit var context: Context
	val attachmentRepository = AttachmentRepository()

	/**
	 * The state of the repository.This is used to determine if the repository is ready to be used. Use repository when [repositoryState] is [RepositoryState.Success].
	 */
	val repositoryState: MutableStateFlow<RepositoryState> = MutableStateFlow(RepositoryState.Init)

	var realm: Realm? = null

	fun initRepository(context: Context) {
		this.context = context
		attachmentRepository.initRepository(context)
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
		} catch (e: Exception) {
			repositoryState.tryEmit(RepositoryState.Error)
//			e.printStackTrace()
		}
	}

	val isAuthenticated: MutableStateFlow<Boolean?> = MutableStateFlow(null)
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
	fun getAllChapter(): List<ChapterObject> {
		realm.let { realm ->
			return if (realm == null) throw RealmNotInitializedException()
			else realm.query(ChapterObject::class).find()
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


	/**
	 * Get list of [ChapterObject] with [parentId] as flow. If [parentId] is null, Notebooks are flowed.
	 * @author pushpull
	 * @since 2.0.0
	 * @throws [RealmNotInitializedException] if realm is not initialized.
	 */
	fun getChapterWithParentIdAsFlow(parentId: RealmUUID?): Flow<ResultsChange<ChapterObject>> {
		realm.let { realm ->
			return if (realm == null) throw RealmNotInitializedException()
			else realm.query(ChapterObject::class, "parentId = $0", parentId).asFlow()
		}
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
					latestNoteObject.contentThumbnail = noteObject.contentThumbnail
					latestNoteObject.content = noteObject.content
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
		realm.let { realm ->
			return if (realm == null) throw RealmNotInitializedException()
			else realm.let { it.query(NoteObject::class).asFlow().map { it.list.map { it.toLite() } } }
		}
	}

	/**
	 * Get all notes with [parentId] as flow.
	 * @throws [RealmNotInitializedException] if realm is not initialized
	 */
	fun getNoteWithParentIdAsFlow(parentId: RealmUUID): Flow<ResultsChange<NoteObject>> {
		realm.let { realm ->
			return if (realm == null) throw RealmNotInitializedException()
			else realm.query(NoteObject::class, "parentId = $0", parentId).asFlow()
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
	fun getAllNote(): List<NoteObject> {
		realm.let { realm ->
			return if (realm == null) throw RealmNotInitializedException()
			else realm.query(NoteObject::class).find().map { it }
		}
	}

	fun putBucket(bucketObject: BucketObject, modifyTimestampAuto: Boolean = true, googleDriveId: String? = null,) {
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

	fun putBucketSuspended(bucketObject: BucketObject, modifyTimestampAuto: Boolean = true, googleDriveId: String? = null,) {
		CoroutineScope(Dispatchers.Default).launch { putBucket(bucketObject, modifyTimestampAuto, googleDriveId,) }
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

	fun putBucketItem(bucketItemObject: BucketItemObject, modifyTimestampAuto: Boolean = true, googleDriveId: String? = null,) {
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

	fun putBucketItemSuspended(bucketItemObject: BucketItemObject, modifyTimestampAuto: Boolean = true, googleDriveId: String? = null,) {
		CoroutineScope(Dispatchers.Default).launch { putBucketItem(bucketItemObject, modifyTimestampAuto) }
	}

	fun reorderBucketItemList(parentId: RealmUUID, idOrderList: List<RealmUUID>) {
		realm?.writeBlocking {
			val storedBucketObject = getBucketFromId(parentId)
			storedBucketObject?.let {
				findLatest(it)?.let { latestBucketObject ->
					latestBucketObject.bucketItemOrderList = idOrderList.toRealmList()
				}
			}
		}
	}

	fun reorderBucketItemListSuspended(parentId: RealmUUID, idOrderList: List<RealmUUID>) {
		CoroutineScope(Dispatchers.Default).launch { reorderBucketItemList(parentId, idOrderList) }
	}

	fun getAllBucketAsFlow(): Flow<RealmResults<BucketObject>> {
		realm.let { realm ->
			return if (realm == null) throw RealmNotInitializedException()
			else realm.query(BucketObject::class).asFlow().map { it.list }
		}
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

	fun getBucketItemWithParentId(parentId: RealmUUID): List<BucketItemObject> {
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
	fun getAllTagAsFlow(): Flow<RealmResults<TagObject>> {
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
	 * Clears everything from realm. It does not reinitialize realm with default values. See [initializeRealm].
	 * @author pushpull
	 * @since 2.2.0
	 * @return Callback with true if successful, false if not along with exception
	 * @throws [RealmNotInitializedException] if realm is not initialized.
	 */
	fun clearRealm(callback: (Boolean, Exception?) -> Unit) = realm?.let {
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
	 * Initialize realm with default values. It does not clear anything from realm. See [clearRealm].
	 * @author pushpull
	 * @since 2.2.0
	 * @return Callback with true if successful, false if not along with exception
	 * @throws [RealmNotInitializedException] if realm is not initialized.
	 */
	fun initializeRealm(callback: (Boolean, Exception?) -> Unit) = realm?.let {
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

	fun restoreRealmSnapshot(context: Context, name: String, path: String, callback: (Boolean, Exception?) -> Unit) {
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

		private fun getImportSnapshotDir() = File(context.cacheDir, "importSnapshot").also {
			it.deleteRecursively()
			it.mkdirs()
		}

		fun generate(callback: (File) -> Unit) {
			val snapshotDir = File(context.cacheDir, "snapshot").also {
				it.deleteRecursively()
				it.mkdirs()
			}
			val fileName = "graphite_snapshot_${Instant.now().toEpochMilli()}"
			val currentSnapshotDir = File(snapshotDir, fileName).also {
				it.mkdirs()
			}

			val observer = RecursiveFileObserver(
				mPath = currentSnapshotDir.path,
				mask = FileObserver.CLOSE_WRITE,
				mListener = object : RecursiveFileObserver.EventListener {
					override fun onEvent(event: Int, file: File?) {
						if (event == FileObserver.CLOSE_WRITE && file == File(currentSnapshotDir, "$fileName.realm")) {
							val attachmentFolder = File(currentSnapshotDir, "attachment").also { it.mkdirs() }
							copyInDirectory(File(context.attachmentDirPath()), attachmentFolder)

							val sevenZFile = File(snapshotDir, "${fileName}.7z")
							val sevenZOutput = SevenZOutputFile(sevenZFile)
							compress7z(currentSnapshotDir, sevenZOutput) { progress, total -> }

							callback(sevenZFile)
						}
					}
				}
			)

			observer.startWatching()
			getRealmSnapshot("$fileName.realm", currentSnapshotDir.path)
		}

		fun restore(inputStream: InputStream, callback: (Boolean) -> Unit) {
			val importSnapshotDir = getImportSnapshotDir()

			val sevenZImportFile = File(importSnapshotDir, "graphite_snapshot.7z")
			sevenZImportFile.outputStream().use { outputStream ->
				inputStream.copyTo(outputStream)
				outputStream.close()
			}
			inputStream.close()

			val sevenZFile = SevenZFile(sevenZImportFile)
			val snapshotDir = File(importSnapshotDir, "snapshot")
			extract7z(sevenZFile, snapshotDir) { progress, total -> }

			// Delete attachment folder
			attachmentRepository.deleteAll()

			snapshotDir.listFiles()?.firstOrNull { it.name.endsWith(".realm") }?.let {
				restoreRealmSnapshot(context, it.name, snapshotDir.path) { isSuccess, exception ->
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

		const val SCHEMA_VERSION = 4L

		enum class RepositoryState {
			Init,
			Locked,
			Loading,
			Success,
			Error,
		}

		sealed class RealmSnapshotCopyStatus {
			object Success : RealmSnapshotCopyStatus()
			object Error : RealmSnapshotCopyStatus()
			data class InProgress(val processed: Int, val total: Int) : RealmSnapshotCopyStatus()
		}

		class RealmNotInitializedException : Exception("Realm not initialized")
	}
}
