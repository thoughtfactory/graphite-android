package com.syncodec.graphite.di.repository.koinRepository

import android.content.Context
import android.graphics.Bitmap
import android.os.FileObserver
import android.util.Log
import com.syncodec.graphite.BuildConfig
import com.syncodec.graphite.di.model.BaseObject
import com.syncodec.graphite.di.model.BucketItemObject
import com.syncodec.graphite.di.model.BucketObject
import com.syncodec.graphite.di.model.ChapterObject
import com.syncodec.graphite.di.model.ChapterObjectLite
import com.syncodec.graphite.di.model.NoteObject
import com.syncodec.graphite.di.model.NoteObjectLite
import com.syncodec.graphite.di.model.TagObject
import com.syncodec.graphite.di.repository.AttachmentRepository
import com.syncodec.graphite.di.repository.RealmMigrator
import com.syncodec.graphite.di.repository.RealmNotInitializedException
import com.syncodec.graphite.di.repository.RepositoryState
import com.syncodec.graphite.utils.RecursiveFileObserver
import com.syncodec.graphite.utils.alice.AliceRequestResult
import com.syncodec.graphite.utils.alice.getSecretData
import com.syncodec.graphite.utils.alice.putSecretData
import com.syncodec.graphite.utils.encodeBase64
import com.syncodec.graphite.utils.scaleBitmap
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
import java.io.File
import java.security.SecureRandom
import kotlin.reflect.KClass


class KoinRepository {

	private val attachmentRepository = AttachmentRepository()

	/**
	 * The state of the repository.This is used to determine if the repository is ready to be used. Use repository when [repositoryState] is [RepositoryState.SUCCESS].
	 */
	val repositoryState : MutableStateFlow<RepositoryState> = MutableStateFlow(RepositoryState.INIT)

	var realm : Realm? = null

	fun initRepository(context : Context) {
		attachmentRepository.initRepository(context)
		try {
			var key : ByteArray
			context.getSecretData("realmKey").let {
				if (it.result == AliceRequestResult.KEY_NOT_FOUND) {
					val realmKey = ByteArray(Realm.ENCRYPTION_KEY_LENGTH)
					SecureRandom().nextBytes(realmKey)
					context.putSecretData("realmKey", realmKey)
					key = context.getSecretData("realmKey").data !!
				} else {
					key = it.data !!
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
						TagObject::class
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
				.schemaVersion(2)
				.migration(RealmMigrator())
				.build()

			realm = Realm.open(realmConfiguration)
			repositoryState.value = RepositoryState.SUCCESS
		} catch (e : Exception) {
			repositoryState.tryEmit(RepositoryState.ERROR)
//			e.printStackTrace()
		}
	}

	val isAuthenticated : MutableStateFlow<Boolean?> = MutableStateFlow(null)
	fun getRealmSchema() : Pair<RealmSchema, Long>? {
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
	fun exists(clazz : KClass<out RealmObject>, id : RealmUUID?) : Boolean {
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

	suspend fun putDefaultChapterId(id : RealmUUID) {
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
	fun getDefaultChapterIdAsFlow() : Flow<RealmUUID?> {
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
	fun getDefaultChapterId() : RealmUUID? {
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
	fun getBaseObject() : BaseObject? {
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
	fun getBaseObjectAsFlow() : Flow<BaseObject?> {
		realm.let { realm ->
			return if (realm == null) throw RealmNotInitializedException()
			else realm.query(BaseObject::class).first().asFlow().map { it.obj }
		}
	}

	fun putChapter(chapterObject : ChapterObject, modifyTimestampAuto : Boolean = true) {
		realm?.writeBlocking {
			val storedChapterObject = getChapterFromId(chapterObject.id)
			storedChapterObject?.let {
				findLatest(it)?.let { latestChapterObject ->
					latestChapterObject.modifiedTimestamp = if (modifyTimestampAuto) System.currentTimeMillis() else latestChapterObject.modifiedTimestamp
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

	fun putChapterSuspended(chapterObject : ChapterObject, modifyTimestampAuto : Boolean = true) {
		CoroutineScope(Dispatchers.Default).launch { putChapter(chapterObject, modifyTimestampAuto) }
	}

	fun reorderNotebookList(idOrderList : List<RealmUUID>) {
		realm?.writeBlocking {
			val storedBaseObject = getBaseObject()
			storedBaseObject?.let {
				findLatest(it)?.let { latestBaseObject ->
					latestBaseObject.notebookIdOrderList = idOrderList.toRealmList()
				}
			}
		}
	}

	fun reorderNotebookListSuspended(idOrderList : List<RealmUUID>) {
		CoroutineScope(Dispatchers.Default).launch { reorderBucketList(idOrderList) }
	}

	fun getChapterFromIdAsFlow(id : RealmUUID?) : Flow<ChapterObject?> {
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
	fun getAllChapter() : List<ChapterObject> {
		realm.let { realm ->
			return if (realm == null) throw RealmNotInitializedException()
			else realm.query(ChapterObject::class).find().map { it }
		}
	}

	/**
	 * Get all chapters as a flow of list
	 * @author pushpull
	 * @since 2.0.0
	 * @throws [RealmNotInitializedException] if realm is not initialized
	 */
	fun getAllChapterAsFlow() : Flow<List<ChapterObject>> {
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
	fun getChapterFromId(id : RealmUUID?) : ChapterObject? {
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
	fun getChapterWithParentIdAsFlow(parentId : RealmUUID?) : Flow<ResultsChange<ChapterObject>> {
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
	fun getChapterWithParentId(parentChapterId : RealmUUID?) : Pair<ChapterObject?, List<ChapterObject>> {
		realm.let { realm ->
			return if (realm == null) throw RealmNotInitializedException()
			else realm.query(ChapterObject::class, "parentId == $0 ", parentChapterId).find().toList().let {
				Pair(getChapterFromId(parentChapterId), it)
			}
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
	fun getChapterPath(id : RealmUUID?, includeEdge : Boolean = false) : List<ChapterObjectLite> {
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
		} catch (e : Exception) {
			listOf()
		}
	}

	/**
	 * Saves a note in the database or updates if already present. No need to pass the parent chapter id as it will read from [noteObject].
	 *
	 * @author pushpull
	 * @since 2.2.0
	 */
	fun putNote(noteObject : NoteObject, modifyTimestampAuto : Boolean = true) {
		realm?.writeBlocking {
			val storedNoteObject = getNoteFromId(noteObject.id)
			storedNoteObject?.let {
				findLatest(it)?.let { storedNoteObject ->
					storedNoteObject.createdTimestamp = noteObject.createdTimestamp
					storedNoteObject.modifiedTimestamp = if (modifyTimestampAuto) System.currentTimeMillis() else noteObject.modifiedTimestamp
					storedNoteObject.userTimestamp = noteObject.userTimestamp
					storedNoteObject.title = noteObject.title
					storedNoteObject.color = noteObject.color
					storedNoteObject.latLng = noteObject.latLng
					storedNoteObject.address = noteObject.address
					storedNoteObject.contentThumbnail = noteObject.contentThumbnail
					storedNoteObject.content = noteObject.content
					storedNoteObject.thumbnail = noteObject.thumbnail
					storedNoteObject.thumbnailType = noteObject.thumbnailType
					storedNoteObject.isFavourite = noteObject.isFavourite
					storedNoteObject.isLocked = noteObject.isLocked
					storedNoteObject.parentId = noteObject.parentId
				} ?: copyToRealm(noteObject)
			} ?: copyToRealm(noteObject)
		}
	}

	fun putNoteSuspended(noteObject : NoteObject, modifyTimestampAuto : Boolean = true) {
		CoroutineScope(Dispatchers.Default).launch { putNote(noteObject, modifyTimestampAuto) }
	}

	fun putThumbnailInNote(noteId : RealmUUID, thumbnail : Bitmap) {
		CoroutineScope(Dispatchers.Default).launch {
			realm?.write {
				this@KoinRepository.getNoteFromId(id = noteId)?.let { noteObject ->
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

	fun getNoteFromId(id : RealmUUID) : NoteObject? {
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
	fun getNoteFromIdAsFlow(id : RealmUUID?) : Flow<NoteObject?> {
		realm.let { realm ->
			return if (realm == null) throw RealmNotInitializedException()
			else realm.query(NoteObject::class, "id == $0 ", id).first().asFlow().map { it.obj }
		}
	}

	fun getAllNoteAsFlow() : Flow<RealmResults<NoteObject>> {
		realm.let { realm ->
			return if (realm == null) throw RealmNotInitializedException()
			else realm.query(NoteObject::class).asFlow().map { it.list }
		}
	}

	fun getAllNoteLiteAsFlow() : Flow<List<NoteObjectLite>> {
		realm.let { realm ->
			return if (realm == null) throw RealmNotInitializedException()
			else realm.let { it.query(NoteObject::class).asFlow().map { it.list.map { it.toLite() } } }
		}
	}

	/**
	 * Get all notes with [parentId] as flow.
	 * @throws [RealmNotInitializedException] if realm is not initialized
	 */
	fun getNoteWithParentIdAsFlow(parentId : RealmUUID) : Flow<ResultsChange<NoteObject>> {
		realm.let { realm ->
			return if (realm == null) throw RealmNotInitializedException()
			else realm.query(NoteObject::class, "parentId = $0", parentId).asFlow()
		}
	}

	fun getNoteWithParentId(parentId : RealmUUID) : RealmResults<NoteObject> {
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
	fun getAllNote() : List<NoteObject> {
		realm.let { realm ->
			return if (realm == null) throw RealmNotInitializedException()
			else realm.query(NoteObject::class).find().map { it }
		}
	}

	fun putBucket(bucketObject : BucketObject, modifyTimestampAuto : Boolean = true) {
		realm?.writeBlocking {
			val storedBucketObject = getBucketFromId(bucketObject.id)
			storedBucketObject?.let {
				findLatest(it)?.let { latestBucketItemObject ->
					latestBucketItemObject.createdTimestamp = bucketObject.createdTimestamp
					latestBucketItemObject.modifiedTimestamp = if (modifyTimestampAuto) System.currentTimeMillis() else bucketObject.modifiedTimestamp
					latestBucketItemObject.title = bucketObject.title
					latestBucketItemObject.description = bucketObject.description
					latestBucketItemObject.bucketType = bucketObject.bucketType
					latestBucketItemObject.isFavourite = bucketObject.isFavourite
					latestBucketItemObject.isLocked = bucketObject.isLocked
				} ?: copyToRealm(bucketObject)
			} ?: copyToRealm(bucketObject)
		}
	}

	fun putBucketSuspended(bucketObject : BucketObject, modifyTimestampAuto : Boolean = true) {
		CoroutineScope(Dispatchers.Default).launch { putBucket(bucketObject, modifyTimestampAuto) }
	}

	fun reorderBucketList(idOrderList : List<RealmUUID>) {
		realm?.writeBlocking {
			val storedBaseObject = getBaseObject()
			storedBaseObject?.let {
				findLatest(it)?.let { latestBaseObject ->
					latestBaseObject.bucketIdOrderList = idOrderList.toRealmList()
				}
			}
		}
	}

	fun reorderBucketListSuspended(idOrderList : List<RealmUUID>) {
		CoroutineScope(Dispatchers.Default).launch { reorderBucketList(idOrderList) }
	}

	fun putBucketItem(bucketItemObject : BucketItemObject, modifyTimestampAuto : Boolean = true) {
		realm?.writeBlocking {
			val storedBucketItemObject = getBucketItemFromId(bucketItemObject.id)
			storedBucketItemObject?.let {
				findLatest(it)?.let { latestBucketItemObject ->
					latestBucketItemObject.createdTimestamp = bucketItemObject.createdTimestamp
					latestBucketItemObject.modifiedTimestamp = if (modifyTimestampAuto) System.currentTimeMillis() else bucketItemObject.modifiedTimestamp
					latestBucketItemObject.bucketType = bucketItemObject.bucketType
					latestBucketItemObject.title = bucketItemObject.title
					latestBucketItemObject.state = bucketItemObject.state
					latestBucketItemObject.thumbnail = bucketItemObject.thumbnail
					latestBucketItemObject.isFavourite = bucketItemObject.isFavourite
					latestBucketItemObject.isLocked = bucketItemObject.isLocked
					latestBucketItemObject.parentId = bucketItemObject.parentId
					latestBucketItemObject.key = bucketItemObject.key
					latestBucketItemObject.data = bucketItemObject.data
				} ?: copyToRealm(bucketItemObject)
			} ?: copyToRealm(bucketItemObject)
		}
	}

	fun putBucketItemSuspended(bucketItemObject : BucketItemObject, modifyTimestampAuto : Boolean = true) {
		CoroutineScope(Dispatchers.Default).launch { putBucketItem(bucketItemObject, modifyTimestampAuto) }
	}

	fun reorderBucketItemList(parentId : RealmUUID, idOrderList : List<RealmUUID>) {
		realm?.writeBlocking {
			val storedBucketObject = getBucketFromId(parentId)
			storedBucketObject?.let {
				findLatest(it)?.let { latestBucketObject ->
					latestBucketObject.bucketItemOrderList = idOrderList.toRealmList()
				}
			}
		}
	}

	fun reorderBucketItemListSuspended(parentId : RealmUUID, idOrderList : List<RealmUUID>) {
		CoroutineScope(Dispatchers.Default).launch { reorderBucketItemList(parentId, idOrderList) }
	}

	fun getAllBucketAsFlow() : Flow<RealmResults<BucketObject>> {
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
	fun getAllBucket() : List<BucketObject> {
		realm.let { realm ->
			return if (realm == null) throw RealmNotInitializedException()
			else realm.query(BucketObject::class).find().map { it }
		}
	}

	fun getBucketAsFlow(id : RealmUUID) : Flow<BucketObject?> {
		realm.let { realm ->
			return if (realm == null) throw RealmNotInitializedException()
			else realm.query(BucketObject::class, "id == $0 ", id).first().asFlow().map { it.obj }
		}
	}

	fun getBucketFromId(id : RealmUUID) : BucketObject? {
		realm.let { realm ->
			return if (realm == null) throw RealmNotInitializedException()
			else realm.query(BucketObject::class, "id == $0 ", id).first().find()
		}
	}

	/**
	 * Returns flow of bucket item with provided id as flow
	 * @author pushpull
	 * @since 2.2.0
	 * @param RealmUUID of the bucket item. Can be null but then it will return null.
	 * @return Flow of BucketItemObject with provided id.
	 * @throws [RealmNotInitializedException] if realm is not initialized.
	 */
	fun getBucketItemAsFlow(id : RealmUUID?) : Flow<BucketItemObject?> {
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
	fun getBucketItemWithParentIdAsFlow(parentId : RealmUUID) : Flow<RealmResults<BucketItemObject>> {
		realm.let { realm ->
			return if (realm == null) throw RealmNotInitializedException()
			else realm.query(BucketItemObject::class, "parentId == $0 ", parentId).asFlow().map { it.list }
		}
	}

	fun getBucketItemWithParentId(parentId : RealmUUID) : List<BucketItemObject> {
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
	fun getAllBucketItem() : List<BucketItemObject> {
		realm.let { realm ->
			return if (realm == null) throw RealmNotInitializedException()
			else realm.query(BucketItemObject::class).find().map { it }
		}
	}

	fun getAllBucketItemAsFlow() : Flow<RealmResults<BucketItemObject>> {
		realm.let { realm ->
			return if (realm == null) throw RealmNotInitializedException()
			else realm.query(BucketItemObject::class).asFlow().map { it.list }
		}
	}

	fun getBucketItemFromId(id : RealmUUID) : BucketItemObject? {
		realm.let { realm ->
			return if (realm == null) throw RealmNotInitializedException()
			else realm.query(BucketItemObject::class, "id == $0 ", id).first().find()
		}
	}

	fun putTag(tagObject : TagObject, modifyTimestampAuto : Boolean = true) {
		realm?.writeBlocking {
			val storedTagObject = getTagFromId(tagObject.id)
			storedTagObject?.let {
				findLatest(it)?.let { latestChapterObject ->
					latestChapterObject.modifiedTimestamp = if (modifyTimestampAuto) System.currentTimeMillis() else latestChapterObject.modifiedTimestamp
					latestChapterObject.tag = tagObject.tag
					latestChapterObject.color = tagObject.color
				} ?: copyToRealm(tagObject)
			} ?: copyToRealm(tagObject)
		}
	}

	fun putTagSuspended(tagObject : TagObject, modifyTimestampAuto : Boolean = true) {
		CoroutineScope(Dispatchers.Default).launch { putTag(tagObject, modifyTimestampAuto) }
	}

	fun getTagFromId(id : RealmUUID?) : TagObject? {
		realm.let { realm ->
			return if (realm == null) throw RealmNotInitializedException()
			else realm.query(TagObject::class, "id == $0", id).first().find()
		}
	}

	fun updateTagConnections(id : RealmUUID, tagListToAdd : List<RealmUUID>, tagListToRemove : List<RealmUUID>) {
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
	fun getAllTagAsFlow() : Flow<RealmResults<TagObject>> {
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
	fun getAllTag() : List<TagObject> {
		realm.let { realm ->
			return if (realm == null) throw RealmNotInitializedException()
			else realm.query(TagObject::class).find().map { it }
		}
	}

	private fun delete(id : RealmUUID) {
		getNoteFromId(id)?.let {
			attachmentRepository.delete(it.id)
			realm?.writeBlocking { findLatest(it)?.let { delete(it) } }
		}
		getChapterFromId(id)?.let {
			delete(getNoteWithParentId(id).map { it.id })
			realm?.writeBlocking { findLatest(it)?.let { delete(it) } }
		}
		getBucketItemFromId(id)?.let { realm?.writeBlocking { findLatest(it)?.let { delete(it) } } }
		getBucketFromId(id)?.let {
			delete(getBucketItemWithParentId(id).map { it.id })
			realm?.writeBlocking { findLatest(it)?.let { delete(it) } }
		}
		getTagFromId(id)?.let { realm?.writeBlocking { findLatest(it)?.let { delete(it) } } }
	}

	private fun delete(idList : List<RealmUUID>) = idList.forEach { delete(it) }

	fun deleteSuspended(id : RealmUUID, callback : suspend () -> Unit = {}) {
		CoroutineScope(Dispatchers.Default).launch {
			delete(id)
			callback()
		}
	}

	fun deleteSuspended(idList : List<RealmUUID>, callback : suspend () -> Unit = {}) {
		CoroutineScope(Dispatchers.Default).launch {
			delete(idList)
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
	fun clearRealm(callback : (Boolean, Exception?) -> Unit) = realm?.let {
		CoroutineScope(Dispatchers.Default).launch {
			try {
				attachmentRepository.deleteAll()
				it.writeBlocking { deleteAll() }
				callback(true, null)
			} catch (e : Exception) {
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
	fun initializeRealm(callback : (Boolean, Exception?) -> Unit) = realm?.let {
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
			} catch (e : Exception) {
				callback(false, e)
			}
		}
	} ?: callback(false, RealmNotInitializedException())

	fun getRealmSnapshot(name : String, path : String) {
		CoroutineScope(Dispatchers.Default).launch {
			val realmConfiguration = RealmConfiguration
				.Builder(
					setOf(
						BaseObject::class,
						ChapterObject::class,
						NoteObject::class,
						BucketObject::class,
						BucketItemObject::class,
						TagObject::class
					)
				)
				.schemaVersion(2)
				.directory(path)
				.name(name)
				.migration(RealmMigrator())
				.build()

			realm?.writeCopyTo(realmConfiguration)
		}
	}

	fun restoreRealmSnapshot(context : Context, name : String, path : String, callback : (Boolean, Exception?) -> Unit) {
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
							TagObject::class
						)
					)
					.schemaVersion(2)
					.directory(path)
					.name(name)
					.migration(RealmMigrator())
					.build()

				val snapshotRealm = Realm.open(realmConfiguration)

				var key : ByteArray
				context.getSecretData("realmKey").let {
					key = if (it.result == AliceRequestResult.KEY_NOT_FOUND) {
						val realmKey = ByteArray(Realm.ENCRYPTION_KEY_LENGTH)
						SecureRandom().nextBytes(realmKey)
						context.putSecretData("realmKey", realmKey)
						context.getSecretData("realmKey").data !!
					} else {
						it.data !!
					}
				}

				realm?.close()
				File(context.filesDir, "default.realm").delete()

				val observer = RecursiveFileObserver(
					mPath = context.filesDir.path,
					mask = FileObserver.CLOSE_WRITE,
					mListener = object : RecursiveFileObserver.EventListener {
						override fun onEvent(event : Int, file : File?) {
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
							TagObject::class
						)
					)
					.encryptionKey(key)
					.schemaVersion(2)
					.migration(RealmMigrator())
					.build()

				snapshotRealm.writeCopyTo(realmConfiguration2)
				snapshotRealm.close()
			} catch (e : Exception) {
//				e.printStackTrace()
				callback(false, e)
			}
		}
	}

	companion object {
		sealed class RealmSnapshotCopyStatus {
			object Success : RealmSnapshotCopyStatus()
			object Error : RealmSnapshotCopyStatus()
			data class InProgress(val processed : Int, val total : Int) : RealmSnapshotCopyStatus()
		}

		class ParentDoesNotExistException : Exception("Parent does not exist")
	}
}
