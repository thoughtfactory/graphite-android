package com.syncodec.graphite.di.repository

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
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
import com.syncodec.graphite.utils.RealmUUIDReorderComparator
import com.syncodec.graphite.utils.alice.AliceRequestResult
import com.syncodec.graphite.utils.alice.getSecretData
import com.syncodec.graphite.utils.alice.putSecretData
import com.syncodec.graphite.utils.encodeBase64
import com.syncodec.graphite.utils.scaleBitmap
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.notifications.ResultsChange
import io.realm.kotlin.query.RealmResults
import io.realm.kotlin.schema.RealmSchema
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.File
import java.security.SecureRandom


enum class RepositoryState2 {
	INIT,
	LOCKED,
	LOADING,
	SUCCESS,
	ERROR
}

class KoinRepository {

	/**
	 * The state of the repository.This is used to determine if the repository is ready to be used. Use repository when [repositoryState] is [RepositoryState.SUCCESS].
	 */
	val repositoryState : MutableStateFlow<RepositoryState> = MutableStateFlow(RepositoryState.INIT)

	var realm : Realm? = null

	fun initRealm(context : Context) {
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
			e.printStackTrace()
		}
	}

	val isAuthenticated : MutableStateFlow<Boolean?> = MutableStateFlow(null)
	fun getRealmSchema() : Pair<RealmSchema, Long>? {
		realm?.let {
			return Pair(it.schema(), it.schemaVersion())
		} ?: return null
	}

	fun putDefaultChapterId(id : RealmUUID, callback : (CallbackStatus) -> Unit) {
		CoroutineScope(Dispatchers.Default).launch {
			if (realm == null) {
				callback(CallbackStatus.UNINITIALIZED)
			} else {
				realm?.write {
					val baseObject = this.query(BaseObject::class).first().find()
					if (baseObject == null) {
						BaseObject().also { _baseObject ->
							_baseObject.defaultChapterId = id
							copyToRealm(_baseObject)
						}
						callback(CallbackStatus.ERROR)
					} else {
						baseObject.let { findLatest(it)?.defaultChapterId = id }
						callback(CallbackStatus.SUCCESS)
					}
				}
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
		return if (realm == null) throw RealmNotInitializedException()
		else realm !!.query(BaseObject::class).first().asFlow().map { it.obj?.defaultChapterId }
	}

	/**
	 * Get the default chapterId stored in the [BaseObject]. This should not be null.
	 * @author pushpull
	 * @since 2.2.0
	 * @throws [RealmNotInitializedException] if realm is not initialized.
	 */
	fun getDefaultChapterId() : RealmUUID? {
		return if (realm == null) throw RealmNotInitializedException()
		else realm !!.query(BaseObject::class).first().find()?.defaultChapterId
	}

	/**
	 * Get BaseObject from realm
	 * @author pushpull
	 * @since 2.2.0
	 * @return BaseObject or null. Mostly not null because baseObject is created when realm is initialized.
	 * @throws [RealmNotInitializedException] if realm is not initialized.
	 */
	fun getBaseObject() : BaseObject? {
		return if (realm == null) throw RealmNotInitializedException()
		else realm !!.query(BaseObject::class).first().find()
	}

	/**
	 * Get BaseObject from realm as flow
	 * @author pushpull
	 * @since 2.2.0
	 * @return Flow of BaseObject. Mostly not null because baseObject is created when realm is initialized.
	 * @throws [RealmNotInitializedException] if realm is not initialized.
	 */
	fun getBaseObjectAsFlow() : Flow<BaseObject?> {
		return if (realm == null) throw RealmNotInitializedException()
		else realm !!.query(BaseObject::class).first().asFlow().map { it.obj }
	}

	fun putChapter(parentId : RealmUUID?, chapterObject : ChapterObject, callback : (Boolean, Exception?) -> Unit) {
		CoroutineScope(Dispatchers.Default).launch {
			if (realm == null) {
				callback(false, RealmNotInitializedException())
				return@launch
			}
			try {
				realm !!.write {
					val storedChapterObject = getChapterFromId(chapterObject.id)
					if (storedChapterObject == null) {
						if (parentId == null) {
							copyToRealm(chapterObject)
							callback(true, null)
						} else {
							val parentChapter = getChapterFromId(parentId)
							parentChapter?.let {
								findLatest(it)?.chapterList?.add(chapterObject)
								callback(true, null)
							}
						}
					} else {
						findLatest(storedChapterObject)?.let { latestChapterObject ->
							if (parentId != latestChapterObject.parentId) {
								val oldParentChapter = latestChapterObject.parentId?.let { getChapterFromId(it) }
								val newParentChapter = parentId?.let { getChapterFromId(it) }

								oldParentChapter?.let { findLatest(it)?.chapterList?.remove(latestChapterObject) }
								newParentChapter?.let { findLatest(it)?.chapterList?.add(latestChapterObject) }
							}

							latestChapterObject.modifiedTimestamp = System.currentTimeMillis()
							latestChapterObject.title = chapterObject.title
							latestChapterObject.description = chapterObject.description
							latestChapterObject.color = chapterObject.color
							latestChapterObject.thumbnail = chapterObject.thumbnail
							latestChapterObject.isFavourite = chapterObject.isFavourite
							latestChapterObject.isLocked = chapterObject.isLocked

							latestChapterObject.parentId = chapterObject.parentId

							callback(true, null)
						}
					}
				}
			} catch (e : Exception) {
				callback(false, e)
			}
		}
	}

	suspend fun reorderNotebookList(chapterIdList : List<RealmUUID>, callback : (Boolean, Exception?) -> Unit) {
		CoroutineScope(Dispatchers.Default).launch {
			if (realm == null) {
				callback(false, RealmNotInitializedException())
				return@launch
			}
			try {
				realm !!.write {
					val storedBaseObject = getBaseObject()
					storedBaseObject?.let {
						findLatest(it)
							?.let { latestBaseObject ->
								latestBaseObject.notebookIdOrderList.clear()
								latestBaseObject.notebookIdOrderList.addAll(chapterIdList)
							}
					} ?: run {
						callback(false, Exception())
					}
				}
			} catch (e : Exception) {
				callback(false, e)
			}
		}.join()
	}

	fun getChapterFromIdAsFlow(id : RealmUUID?) : Flow<ChapterObject?> {
		return if (realm == null) throw RealmNotInitializedException()
		else realm !!.query(ChapterObject::class, "id == $0 ", id).first().asFlow().map { it.obj }
	}

	/**
	 * Get all chapters as a list
	 * @author pushpull
	 * @since 2.0.0
	 * @throws [RealmNotInitializedException] if realm is not initialized
	 */
	fun getAllChapter() : List<ChapterObject> {
		return if (realm == null) throw RealmNotInitializedException()
		else realm !!.query(ChapterObject::class).find().map { it }
	}

	/**
	 * Get all chapters as a flow of list
	 * @author pushpull
	 * @since 2.0.0
	 * @throws [RealmNotInitializedException] if realm is not initialized
	 */
	fun getAllChapterAsFlow() : Flow<List<ChapterObject>> {
		return if (realm == null) throw RealmNotInitializedException()
		else realm !!.query(ChapterObject::class).asFlow().map { it.list.map { it } }
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
		return if (realm == null) throw RealmNotInitializedException()
		else realm !!.query(ChapterObject::class, "id == $0 ", id).first().find()
	}

	/**
	 * Get list of [ChapterObject] with [parentId] as flow. If [parentId] is null, Notebooks are flowed.
	 * @author pushpull
	 * @since 2.0.0
	 * @throws [RealmNotInitializedException] if realm is not initialized.
	 */
	fun getChapterWithParentIdAsFlow(parentId : RealmUUID?) : Flow<ResultsChange<ChapterObject>> =
		realm?.query(ChapterObject::class, "parentId = $0", parentId)?.asFlow() ?: throw RealmNotInitializedException()

	/**
	 * @author pushpull
	 * @since 2.0.0
	 * @param parentChapterId Id of the parent chapter. Null if the chapter is a notebook.
	 * @return Pair of the chapter and the child chapters list.
	 * @throws [RealmNotInitializedException] if the realm is not initialized.
	 */
	fun getChapterWithParentId(parentChapterId : RealmUUID?) : Pair<ChapterObject?, List<ChapterObject>> {
		return if (realm == null) throw RealmNotInitializedException()
		else Pair(getChapterFromId(parentChapterId), realm !!.query(ChapterObject::class, "parentId == $0 ", parentChapterId).find().toList())
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
		try {
			val chapterObject = getChapterFromId(id)
			val chapterObjectList = mutableListOf<ChapterObjectLite>()
			if (includeEdge) chapterObject?.toLite()?.let { chapterObjectList.add(it) }
			var parentChapterObject = chapterObject?.parentId?.let { it1 -> getChapterFromId(it1) }
			while (parentChapterObject != null) {
				chapterObjectList.add(parentChapterObject.toLite())
				parentChapterObject = parentChapterObject.parentId?.let { it1 -> getChapterFromId(it1) }
			}
			return chapterObjectList
		} catch (e : Exception) {
			return listOf()
		}
	}

	/**
	 * Saves a note in the database or updates if already present. No need to pass the parent chapter id as it will read from [noteObject].
	 *
	 * To move a note to another chapter, see [moveNoteToChapter].
	 * @author pushpull
	 * @since 2.2.0
	 */
	fun putNote(noteObject : NoteObject, callback : (Boolean, Exception?) -> Unit = { _, _ -> }) {
		CoroutineScope(Dispatchers.Default).launch {
			if (realm == null) {
				callback(false, RealmNotInitializedException())
				return@launch
			}
			try {
				realm !!.write {
					if (noteObject.parentId == null) {
						callback(false, ParentChapterNotFoundException())
					} else {
						val newChapterObject = getChapterFromId(noteObject.parentId !!)
						if (newChapterObject == null) {
							callback(false, ParentChapterNotFoundException())
							return@write
						} else {
							findLatest(newChapterObject)
								?.noteList
								?.let {
									val storedNoteObject = getNoteFromId(noteObject.id)
									val currentChapterObject = storedNoteObject?.parentId?.let { it1 -> getChapterFromId(it1) }
									if (currentChapterObject?.id != newChapterObject.id) {
										currentChapterObject?.let { findLatest(it)?.noteList?.remove(storedNoteObject) }
										it.add(noteObject)
									} else {
										findLatest(storedNoteObject)
											?.let { latestNoteObject ->
												latestNoteObject.modifiedTimestamp = noteObject.modifiedTimestamp
												latestNoteObject.userTimestamp = noteObject.userTimestamp
												latestNoteObject.title = noteObject.title
												latestNoteObject.color = noteObject.color
												latestNoteObject.latLng = noteObject.latLng
												latestNoteObject.address = noteObject.address
												latestNoteObject.contentThumbnail = noteObject.contentThumbnail
												latestNoteObject.content = noteObject.content
												latestNoteObject.thumbnail = noteObject.thumbnail
												latestNoteObject.thumbnailType = noteObject.thumbnailType
												latestNoteObject.isFavourite = noteObject.isFavourite
												latestNoteObject.isLocked = noteObject.isLocked
												latestNoteObject.parentId = noteObject.parentId
												latestNoteObject.googleDriveId = noteObject.googleDriveId
											}
									}
								}

							callback(true, null)
						}
					}
				}
			} catch (e : Exception) {
//				e.printStackTrace()
				callback(false, e)
			}
		}
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
			} ?: throw RealmNotInitializedException()
		}
	}

	/**
	 * Moves an already stored note with [noteId] to another chapter with [chapterId].
	 * @author pushpull
	 * @since 2.2.0
	 */
	fun moveNoteToChapter(noteId : RealmUUID, chapterId : RealmUUID) {
		try {
			if (realm == null) throw RealmNotInitializedException()
			else CoroutineScope(Dispatchers.Default).launch {
				realm !!.write {
					val storedNoteObject = getNoteFromId(noteId)
					val moveToStoredChapterObject = getChapterFromId(chapterId)
//  				val moveFromStoredChapterObject = getChapterFromId(storedNoteObject?.parentChapterId)
					moveToStoredChapterObject?.let {
						if (storedNoteObject != null) {
//							findLatest(it)?.noteList?.add(storedNoteObject)
							findLatest(it)?.title = it.title
						}
					}
				}
			}
		} catch (e : Exception) {

		}
	}

	fun deleteNote(id : RealmUUID, callback : (Boolean, Exception?) -> Unit) {
		if (realm == null) throw RealmNotInitializedException()
		else CoroutineScope(Dispatchers.Default).launch {
			try {
				realm !!.write {
					val noteObject = getNoteFromId(id)
					val chapterObject = noteObject?.parentId?.let { getChapterFromId(it) }

//					noteObject?.id?.let { File("$attachmentDirPath/$it").deleteRecursively() }

					if (chapterObject == null) {
						if (noteObject != null) findLatest(noteObject)?.let { this.delete(it) }
						callback(false, ParentChapterNotFoundException())
						return@write
					} else {
						findLatest(chapterObject)
							?.noteList
							?.removeIf { it.id == id }
						findLatest(noteObject)?.let { this.delete(it) }
						callback(true, null)
					}
				}
			} catch (e : Exception) {
				callback(false, e)
			}
		}
	}

	fun getNoteFromId(id : RealmUUID) : NoteObject? {
		return if (realm == null) throw RealmNotInitializedException()
		else realm !!.query(NoteObject::class, "id == $0 ", id).first().find()
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
		return if (realm == null) throw RealmNotInitializedException()
		else realm !!.query(NoteObject::class, "id == $0 ", id).first().asFlow().map { it.obj }
	}

	fun getAllNoteAsFlow() : Flow<RealmResults<NoteObject>> {
		return if (realm == null) throw RealmNotInitializedException()
		else realm !!.query(NoteObject::class).asFlow().map { it.list }
	}

	fun getAllNoteLiteAsFlow() : Flow<List<NoteObjectLite>> = realm
		?.let { it.query(NoteObject::class).asFlow().map { it.list.map { it.toLite() } } }
		?: throw RealmNotInitializedException()

	/**
	 * Get all notes with [parentId] as flow.
	 * @throws [RealmNotInitializedException] if realm is not initialized
	 */
	fun getNoteWithParentIdAsFlow(parentId : RealmUUID) : Flow<ResultsChange<NoteObject>> =
		realm?.query(NoteObject::class, "parentId = $0", parentId)?.asFlow() ?: throw RealmNotInitializedException()

	/**
	 * Get all notes as a list
	 * @author pushpull
	 * @since 2.0.0
	 * @return List of all NoteObject from realm
	 * @throws [RealmNotInitializedException] if realm is not initialized
	 */
	fun getAllNote() : List<NoteObject> {
		return if (realm == null) throw RealmNotInitializedException()
		else realm !!.query(NoteObject::class).find().map { it }
	}

	fun putBucket(bucketObject : BucketObject, callback : (Boolean, Exception?) -> Unit) {
		CoroutineScope(Dispatchers.Default).launch {
			if (realm == null) {
				callback(false, RealmNotInitializedException())
				return@launch
			}
			try {
				realm !!.write {
					val storedBucketObject = getBucketFromId(bucketObject.id)
					if (storedBucketObject == null) {
						this.copyToRealm(bucketObject)
					} else {
						findLatest(storedBucketObject)
							?.let { latestBucketObject ->
								latestBucketObject.modifiedTimestamp = System.currentTimeMillis()
								latestBucketObject.title = bucketObject.title
								latestBucketObject.description = bucketObject.description
								latestBucketObject.bucketType = bucketObject.bucketType
								latestBucketObject.isFavourite = bucketObject.isFavourite
								latestBucketObject.isLocked = bucketObject.isLocked
							}
					}
				}
			} catch (e : Exception) {
//				e.printStackTrace()
				callback(false, e)
			}
		}
	}

	suspend fun reorderBucketList(bucketIdList : List<RealmUUID>, callback : (Boolean, Exception?) -> Unit) {
		CoroutineScope(Dispatchers.Default).launch {
			if (realm == null) {
				callback(false, RealmNotInitializedException())
				return@launch
			}
			try {
				realm !!.write {
					val storedBaseObject = getBaseObject()
					storedBaseObject?.let {
						findLatest(it)
							?.let { latestBaseObject ->
								latestBaseObject.bucketIdOrderList.clear()
								latestBaseObject.bucketIdOrderList.addAll(bucketIdList)
							}
					} ?: run {
						callback(false, Exception())
					}
				}
			} catch (e : Exception) {
				callback(false, e)
			}
		}
	}

	fun putBucketItem(bucketId : RealmUUID, bucketItemObject : BucketItemObject, callback : (Boolean, Exception?) -> Unit) {
		CoroutineScope(Dispatchers.Default).launch {
			if (realm == null) {
				callback(false, RealmNotInitializedException())
				return@launch
			}
			try {
				realm !!.write {
					getBucketFromId(bucketId)?.let { bucket ->
						val storedBucketItem = bucket.bucketItemList.find { it.id == bucketItemObject.id }

						if (storedBucketItem == null) {
							findLatest(bucket)?.bucketItemList?.add(bucketItemObject)
							callback(true, null)
						} else {
							findLatest(storedBucketItem)?.let { latestBucketItemObject ->
								latestBucketItemObject.modifiedTimestamp = System.currentTimeMillis()
								latestBucketItemObject.title = bucketItemObject.title
								latestBucketItemObject.state = bucketItemObject.state
								latestBucketItemObject.thumbnail = bucketItemObject.thumbnail
								latestBucketItemObject.isFavourite = bucketItemObject.isFavourite
								latestBucketItemObject.isLocked = bucketItemObject.isLocked
								latestBucketItemObject.parentId = bucketItemObject.parentId
								latestBucketItemObject.data = bucketItemObject.data
								latestBucketItemObject.key = bucketItemObject.key
							}
							callback(true, null)
						}
					} ?: callback(false, BucketNotFoundException())
				}
			} catch (e : Exception) {
//				e.printStackTrace()
				callback(false, e)
			}
		}
	}

	fun getAllBucketAsFlow() : Flow<RealmResults<BucketObject>> {
		return if (realm == null) throw RealmNotInitializedException()
		else realm !!.query(BucketObject::class).asFlow().map { it.list }
	}

	/**
	 * Get all buckets as a list
	 * @author pushpull
	 * @since 2.2.0
	 * @return List of all BucketObject
	 * @throws [RealmNotInitializedException] if realm is not initialized.
	 */
	fun getAllBucket() : List<BucketObject> {
		return if (realm == null) throw RealmNotInitializedException()
		else realm !!.query(BucketObject::class).find().map { it }
	}

	fun getBucketAsFlow(id : RealmUUID) : Flow<BucketObject?> {
		return if (realm == null) throw RealmNotInitializedException()
		else realm !!.query(BucketObject::class, "id == $0 ", id).first().asFlow().map { it.obj }
	}

	fun getBucketFromId(id : RealmUUID) : BucketObject? {
		return if (realm == null) throw RealmNotInitializedException()
		else realm !!.query(BucketObject::class, "id == $0 ", id).first().find()
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
		return if (realm == null) throw RealmNotInitializedException()
		else realm !!.query(BucketItemObject::class, "id == $0 ", id).first().asFlow().map { it.obj }
	}

	/**
	 * Returns flow of bucket item with provided parent id as flow.
	 * @author pushpull
	 * @since 2.2.0
	 * @param parentId RealmUUID of the parent bucket.
	 * @return Flow of RealmResults of BucketItemObject with provided parent id.
	 * @throws [RealmNotInitializedException] if realm is not initialized.
	 */
	fun getBucketItemFromParentIdAsFlow(parentId : RealmUUID) : Flow<RealmResults<BucketItemObject>> {
		return if (realm == null) throw RealmNotInitializedException()
		else realm !!.query(BucketItemObject::class, "parentId == $0 ", parentId).asFlow().map { it.list }
	}

	/**
	 * Get all bucket items as a list
	 * @author pushpull
	 * @since 2.2.0
	 * @return List of all BucketItemObject
	 * @throws [RealmNotInitializedException] if realm is not initialized.
	 */
	fun getAllBucketItem() : List<BucketItemObject> {
		return if (realm == null) throw RealmNotInitializedException()
		else realm !!.query(BucketItemObject::class).find().map { it }
	}

	fun getBucketItem(id : RealmUUID) : BucketItemObject? {
		return if (realm == null) throw RealmNotInitializedException()
		else realm !!.query(BucketItemObject::class, "id == $0 ", id).first().find()
	}

	suspend fun reorderBucketItem(bucketId : RealmUUID, bucketItemIdList : List<RealmUUID>, callback : (Boolean, Exception?) -> Unit) {
		CoroutineScope(Dispatchers.Default).launch {
			if (realm == null) {
				callback(false, RealmNotInitializedException())
				return@launch
			}
			try {
				realm !!.write {
					getBucketFromId(bucketId)?.let { bucket ->
						findLatest(bucket)?.let { latestBucket ->
							latestBucket.bucketItemList.sortWith(RealmUUIDReorderComparator(bucketItemIdList, BucketItemObject::id))
							latestBucket.modifiedTimestamp = System.currentTimeMillis()
						}
					}
				}
			} catch (e : Exception) {
				callback(false, e)
			}
		}.join()
	}

	fun putTag(tagObject : TagObject, callback : (Boolean, Exception?) -> Unit) {
		if (realm == null) throw RealmNotInitializedException()
		else {
			CoroutineScope(Dispatchers.Default).launch {
				try {
					realm?.write {
						val storedTagObject = getTagFromId(tagObject.id)
						if (storedTagObject != null) {
							findLatest(storedTagObject)?.let {
								it.tag = tagObject.tag
								it.color = tagObject.color
							}
						} else {
							this@KoinRepository.getAllTag().find { it.tag == tagObject.tag }?.let {
								it.color = tagObject.color
							} ?: copyToRealm(tagObject)
						}

						callback(true, null)
					}
				} catch (e : Exception) {
					callback(false, e)
				}
			}
		}
	}

	fun deleteTag(id : RealmUUID?) {
		if (realm == null) throw RealmNotInitializedException()
		else {
			CoroutineScope(Dispatchers.Default).launch {
				realm?.write {
					val tagObject = getTagFromId(id)
					tagObject?.let { findLatest(it)?.let { this.delete(it) } }
				}
			}
		}
	}

	fun getTagFromId(id : RealmUUID?) : TagObject? {
		return if (realm == null) throw RealmNotInitializedException()
		else realm !!.query(TagObject::class, "id == $0", id).first().find()
	}

	fun getTagFromIdAsFlow(id : RealmUUID) : Flow<TagObject?> {
		return if (realm == null) throw RealmNotInitializedException()
		else realm !!.query(TagObject::class, "id == $0", id).first().asFlow().map { it.obj }
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

	fun connectTag(noteId : RealmUUID, tagIdList : List<RealmUUID>, callback : (Boolean, Exception?) -> Unit) {
		if (realm == null) throw RealmNotInitializedException()
		else {
			CoroutineScope(Dispatchers.Default).launch {
				try {
					realm?.write {
						getAllTag().forEach { findLatest(it)?.objectIdList?.remove(noteId) }
						tagIdList.forEach {
							val tagObject = getTagFromId(it)
							tagObject?.let {
								findLatest(it)?.objectIdList?.add(noteId)
							}
							if (tagObject != null) {
								findLatest(tagObject)?.let {
									if (it.objectIdList.contains(noteId)) it.objectIdList.remove(noteId)
									else {
										it.objectIdList.add(noteId)
									}
								}
							}
						}
						callback(true, null)
					}
				} catch (e : Exception) {
//					e.printStackTrace()
					callback(false, e)
				}
			}
		}
	}

	fun connectTag(noteId : RealmUUID, tagId : RealmUUID) {
		if (realm == null) throw RealmNotInitializedException()
		else {
			CoroutineScope(Dispatchers.Default).launch {
				realm?.write {
					val tagObject = getTagFromId(tagId)
					if (tagObject != null) {
						findLatest(tagObject)?.let {
							if (it.objectIdList.contains(noteId)) it.objectIdList.remove(noteId)
							else it.objectIdList.add(noteId)
						}
					}
				}
			}
		}
	}

	fun getTagFromName(tag : String) : TagObject? {
		return if (realm == null) throw RealmNotInitializedException()
		else realm !!.query(TagObject::class, "tag == $0", tag).first().find()
	}

	fun getTagFromNameAsFlow(tag : String) : Flow<TagObject?> {
		return if (realm == null) throw RealmNotInitializedException()
		else realm !!.query(TagObject::class, "tag == $0", tag).first().asFlow().map { it.obj }
	}

	/**
	 * Get all tags as a flow list and observe changes
	 * @author pushpull
	 * @since 2.0.0
	 * @throws [RealmNotInitializedException] if realm is not initialized
	 */
	fun getAllTagAsFlow() : Flow<RealmResults<TagObject>> {
		return if (realm == null) throw RealmNotInitializedException()
		else realm !!.query(TagObject::class).asFlow().map { it.list }
	}

	/**
	 * Get all tags as a list
	 * @author pushpull
	 * @since 2.0.0
	 * @throws [RealmNotInitializedException] if realm is not initialized
	 */
	fun getAllTag() : List<TagObject> {
		return if (realm == null) throw RealmNotInitializedException()
		else realm !!.query(TagObject::class).find().map { it }
	}

	fun isKeyPresentInBucketItem(key : String?) : Boolean {
		return if (realm == null) throw RealmNotInitializedException()
		else realm !!.query(BucketItemObject::class, "key == $0 ", key).count().find() > 0
	}

	fun delete(objectIdList : List<RealmUUID>) {
		if (realm == null) throw RealmNotInitializedException()
		else {
			CoroutineScope(Dispatchers.Default).launch {
				objectIdList.forEach { delete(it) }
			}
		}
	}

	private suspend fun delete(objectId : RealmUUID) {
		realm?.write {

//			** Delete note
			val noteObject = getNoteFromId(objectId)
			val parentChapterObject = noteObject?.parentId?.let { getChapterFromId(it) }

			if (parentChapterObject != null) {
				findLatest(parentChapterObject)
					?.noteList
					?.removeIf { it.id == objectId }
			}
			noteObject?.id?.let { this@KoinRepository.deleteNote(it) { _, _ -> } }

//			** Delete chapter
			val chapterObject = getChapterFromId(objectId)
			chapterObject?.noteList?.map { it.id }?.let { delete(it) }
			chapterObject?.chapterList?.map { it.id }?.let { delete(it) }
			chapterObject?.let { findLatest(it)?.let { this.delete(it) } }

//			** Delete bucket
			val bucketObject = getBucketFromId(objectId)
			bucketObject?.bucketItemList?.map { it.id }?.let { this@KoinRepository.delete(it) }
			bucketObject?.let { findLatest(it)?.let { this.delete(it) } }

//			** Delete bucket item
			val bucketItemObject = getBucketItem(objectId)
			bucketItemObject?.let { findLatest(it)?.let { this.delete(it) } }
		}
	}

	/**
	 * Clears everything from realm. It does not reinitialize realm with default values. See [initializeRealm].
	 * @author pushpull
	 * @since 2.2.0
	 * @return Callback with true if successful, false if not along with exception
	 * @throws [RealmNotInitializedException] if realm is not initialized.
	 */
	fun clearRealm(callback : (Boolean, Exception?) -> Unit) {
		if (realm == null) throw RealmNotInitializedException()
		else {
			CoroutineScope(Dispatchers.Default).launch {
				try {
					realm !!.write { this.deleteAll() }
					callback(true, null)
				} catch (e : Exception) {
					callback(false, e)
				}
			}
		}
	}

	/**
	 * Initialize realm with default values. It does not clear anything from realm. See [clearRealm].
	 * @author pushpull
	 * @since 2.2.0
	 * @return Callback with true if successful, false if not along with exception
	 * @throws [RealmNotInitializedException] if realm is not initialized.
	 */
	fun initializeRealm(callback : (Boolean, Exception?) -> Unit) {
		if (realm == null) throw RealmNotInitializedException()
		else {
			CoroutineScope(Dispatchers.Default).launch {
				try {
					realm?.write { copyToRealm(BaseObject()) }
					ChapterObject().apply {
						this.title = "Diary"
						this.description = "Default diary. Every notes will be saved in this notebook by default"
						putChapter(parentId = null, chapterObject = this) { isSuccess, exception ->
							if (! isSuccess) {
								callback(false, exception)
							} else {
								putDefaultChapterId(this.id) { callbackStatus ->
									when (callbackStatus) {
										CallbackStatus.SUCCESS -> callback(true, null)
										CallbackStatus.ERROR -> callback(false, null)
										CallbackStatus.UNINITIALIZED -> callback(false, RealmNotInitializedException())
									}
								}
							}
						}
					}
				} catch (e : Exception) {
					callback(false, e)
				}
			}
		}
	}

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
}
