package com.syncodec.graphite.di.repository

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.FileProvider
import com.syncodec.graphite.di.model.AttachmentObject
import com.syncodec.graphite.di.model.BaseObject
import com.syncodec.graphite.di.model.BucketItemObject
import com.syncodec.graphite.di.model.BucketObject
import com.syncodec.graphite.di.model.ChapterObject
import com.syncodec.graphite.di.model.ChapterObjectLite
import com.syncodec.graphite.di.model.NoteObject
import com.syncodec.graphite.di.model.NoteObjectLite
import com.syncodec.graphite.di.model.TagObject
import com.syncodec.graphite.di.network.FirebaseStorageApi
import com.syncodec.graphite.utils.alice.AliceRequestResult
import com.syncodec.graphite.utils.alice.getSecretData
import com.syncodec.graphite.utils.alice.putSecretData
import com.syncodec.graphite.utils.copyInputStreamToOutputStream
import com.syncodec.graphite.utils.createTempAttachmentFileToExpose
import com.syncodec.graphite.utils.getFileName
import com.syncodec.graphite.utils.getRandomColor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.query.RealmResults
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File
import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton


@InstallIn(SingletonComponent::class)
@Module
object RepositoryModule {
	@Singleton
	@Provides
	fun provideRepository(@ApplicationContext context : Context) = Repository2(context)
}


enum class RepositoryState {
	INIT,
	LOCKED,
	LOADING,
	SUCCESS,
	ERROR
}

class RealmNotInitializedException : Exception("Realm not initialized")
class ParentChapterNotFoundException : Exception("Parent chapter not found")
class ChapterNotFoundException : Exception("Chapter not found")
class BucketNotFoundException : Exception("Bucket not found")
class NoteNotFoundException : Exception("Note not found")
class SameBookException : Exception("Same book")

enum class CallbackStatus {
	SUCCESS, ERROR, UNINITIALIZED
}

class Repository2 @Inject constructor(@ApplicationContext val context : Context) {

	val repositoryState : MutableStateFlow<RepositoryState> = MutableStateFlow(RepositoryState.INIT)

	private var realmConfiguration : RealmConfiguration? = null
	private var realm : Realm? = null

	val firebaseStorageApi = FirebaseStorageApi()

	val isAuthenticated : MutableStateFlow<Boolean?> = MutableStateFlow(null)

	init {
		CoroutineScope(Dispatchers.IO).launch {
			isAuthenticated.collect {
				when (it) {
					null -> repositoryState.value = RepositoryState.LOADING
					false -> repositoryState.value = RepositoryState.LOCKED
					true -> {
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

						Log.i("npr71", "key: ${key.joinToString("") { java.lang.String.format("%02x", it) }}")

						try {
							realmConfiguration = RealmConfiguration.Builder(
								setOf(
									BaseObject::class,
									ChapterObject::class,
									NoteObject::class,
									AttachmentObject::class,
									BucketObject::class,
									BucketItemObject::class,
									TagObject::class
								)
							)
//			    .encryptionKey(getNewKey(context))
								.encryptionKey(key)
								.initialData {
									ChapterObject().also { chapterObject ->
										chapterObject.title = "Diary"
										chapterObject.description = "Default diary. Every notes will be saved in this notebook by default"
										chapterObject.color = getRandomColor().toArgb()

										copyToRealm(chapterObject)

										BaseObject().also { baseObject ->
											baseObject.defaultChapterId = chapterObject.id

											copyToRealm(baseObject)
										}
									}
								}
								.build()

							realm = Realm.open(realmConfiguration !!)

							repositoryState.tryEmit(RepositoryState.SUCCESS)
						} catch (e : Exception) {
							e.printStackTrace()
							repositoryState.tryEmit(RepositoryState.ERROR)
						}
					}
				}
			}
		}
	}

	fun putDefaultChapterId(id : RealmUUID, callback : (CallbackStatus) -> Unit) {
		CoroutineScope(Dispatchers.IO).launch {
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

	fun getDefaultChapterId() : Flow<RealmUUID?> {
		return if (realm == null) throw RealmNotInitializedException()
		else realm !!.query(BaseObject::class).first().asFlow().map { it.obj?.defaultChapterId }
	}

	fun getBaseObject() : BaseObject? {
		return if (realm == null) throw RealmNotInitializedException()
		else realm !!.query(BaseObject::class).first().find()
	}

	fun putChapter(parentChapterId : RealmUUID?, chapterObject : ChapterObject, callback : (Boolean, Exception?) -> Unit) {
		CoroutineScope(Dispatchers.IO).launch {
			if (realm == null) {
				callback(false, RealmNotInitializedException())
				return@launch
			}
			try {
				realm !!.write {
					val storedChapterObject = getChapterFromId(chapterObject.id)
					if (storedChapterObject == null) {
						if (parentChapterId == null) {
							copyToRealm(chapterObject)
							callback(true, null)
						} else {
							val parentChapter = getChapterFromId(parentChapterId)
							parentChapter?.let {
								findLatest(it)?.chapterList?.add(chapterObject)
								callback(true, null)
							}
						}
					} else {
						findLatest(storedChapterObject)?.let { latestChapterObject ->
							if (parentChapterId != latestChapterObject.parentChapterId) {
								val oldParentChapter = latestChapterObject.parentChapterId?.let { getChapterFromId(it) }
								val newParentChapter = parentChapterId?.let { getChapterFromId(it) }

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

							latestChapterObject.parentChapterId = chapterObject.parentChapterId

							callback(true, null)
						}
					}
				}
			} catch (e : Exception) {
				callback(false, e)
			}
		}
	}

	fun getChapterFromIdAsFlow(id : RealmUUID?) : Flow<ChapterObject?> {
		return if (realm == null) throw RealmNotInitializedException()
		else realm !!.query(ChapterObject::class, "id == $0 ", id).first().asFlow().map { it.obj }
	}

	fun getAllChapter() : RealmResults<ChapterObject> {
		return if (realm == null) throw RealmNotInitializedException()
		else realm !!.query(ChapterObject::class).find()
	}

	fun getChapterFromId(id : RealmUUID?) : ChapterObject? {
		return if (realm == null) throw RealmNotInitializedException()
		else realm !!.query(ChapterObject::class, "id == $0 ", id).first().find()
	}

	fun getChapterWithParentIdAsFlow(parentChapterId : RealmUUID?) : Flow<List<ChapterObject>> {
		return if (realm == null) throw RealmNotInitializedException()
		else realm !!.query(ChapterObject::class, "parentChapterId == $0 ", parentChapterId).asFlow().map { it.list }
	}

	fun getChapterWithParentId(parentChapterId : RealmUUID?) : Pair<ChapterObject?, List<ChapterObject>> {
		return if (realm == null) throw RealmNotInitializedException()
		else Pair(getChapterFromId(parentChapterId), realm !!.query(ChapterObject::class, "parentChapterId == $0 ", parentChapterId).find().toList())
	}

	fun getParentChapterList(id : RealmUUID?, includeEdge : Boolean = false, callback : (List<ChapterObjectLite>?, Exception?) -> Unit) {
		CoroutineScope(Dispatchers.IO).launch {
			try {
				val chapterObject = getChapterFromId(id)
				val chapterObjectList = mutableListOf<ChapterObjectLite>()
				if (includeEdge) chapterObject?.toLite()?.let { chapterObjectList.add(it) }
				var parentChapterObject = chapterObject?.parentChapterId?.let { it1 -> getChapterFromId(it1) }
				while (parentChapterObject != null) {
					chapterObjectList.add(parentChapterObject.toLite())
					parentChapterObject = parentChapterObject.parentChapterId?.let { it1 -> getChapterFromId(it1) }
				}
				callback(chapterObjectList, null)
			} catch (e : Exception) {
				callback(null, e)
			}
		}
	}

	fun putNote(noteObject : NoteObject, callback : (Boolean, Exception?) -> Unit) {
		CoroutineScope(Dispatchers.IO).launch {
			if (realm == null) {
				callback(false, RealmNotInitializedException())
				return@launch
			}
			try {
				realm !!.write {
					if (noteObject.parentChapterId == null) {
						callback(false, ParentChapterNotFoundException())
					} else {
						val newChapterObject = getChapterFromId(noteObject.parentChapterId !!)
						if (newChapterObject == null) {
							callback(false, ParentChapterNotFoundException())
							return@write
						} else {
							findLatest(newChapterObject)
								?.noteList
								?.let {
									val storedNoteObject = getNoteFromId(noteObject.id)
									val currentChapterObject = storedNoteObject?.parentChapterId?.let { it1 -> getChapterFromId(it1) }
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
												latestNoteObject.parentChapterId = noteObject.parentChapterId
											}
									}
								}

							callback(true, null)
						}
					}
				}
			} catch (e : Exception) {
				e.printStackTrace()
				callback(false, e)
			}
		}
	}

	fun moveNoteToChapter(noteId : RealmUUID, chapterId : RealmUUID) {
		try {
			if (realm == null) throw RealmNotInitializedException()
			else CoroutineScope(Dispatchers.IO).launch {
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
		else CoroutineScope(Dispatchers.IO).launch {
			try {
				realm !!.write {
					val noteObject = getNoteFromId(id)
					val chapterObject = noteObject?.parentChapterId?.let { getChapterFromId(it) }

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

	fun getNoteFromIdAsFlow(id : RealmUUID) : Flow<NoteObject?> {
		return if (realm == null) throw RealmNotInitializedException()
		else realm !!.query(NoteObject::class, "id == $0 ", id).first().asFlow().map { it.obj }
	}

	fun getAllNoteAsFlow() : Flow<RealmResults<NoteObject>> {
		return if (realm == null) throw RealmNotInitializedException()
		else realm !!.query(NoteObject::class).asFlow().map { it.list }
	}

	fun getAllNoteLiteAsFlow() : Flow<List<NoteObjectLite>> {
		return if (realm == null) throw RealmNotInitializedException()
		else realm !!.query(NoteObject::class).asFlow().map { it.list.map { it.toLite() } }
	}

	fun getAllNote() : RealmResults<NoteObject> {
		return if (realm == null) throw RealmNotInitializedException()
		else realm !!.query(NoteObject::class).find()
	}

	fun putBucket(bucketObject : BucketObject, callback : (Boolean, Exception?) -> Unit) {
		CoroutineScope(Dispatchers.IO).launch {
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
								latestBucketObject.modifiedTimestamp = bucketObject.modifiedTimestamp
								latestBucketObject.title = bucketObject.title
								latestBucketObject.description = bucketObject.description
								latestBucketObject.bucketType = bucketObject.bucketType
								latestBucketObject.isFavourite = bucketObject.isFavourite
								latestBucketObject.isLocked = bucketObject.isLocked
							}
					}
				}
			} catch (e : Exception) {
				e.printStackTrace()
				callback(false, e)
			}
		}
	}

	fun putBucketItem(bucketId : RealmUUID, bucketItemObject : BucketItemObject, callback : (Boolean, Exception?) -> Unit) {
		CoroutineScope(Dispatchers.IO).launch {
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
								latestBucketItemObject.modifiedTimestamp = bucketItemObject.modifiedTimestamp
								latestBucketItemObject.title = bucketItemObject.title
								latestBucketItemObject.state = bucketItemObject.state
								latestBucketItemObject.thumbnail = bucketItemObject.thumbnail
								latestBucketItemObject.isFavourite = bucketItemObject.isFavourite
								latestBucketItemObject.isLocked = bucketItemObject.isLocked
								latestBucketItemObject.data = bucketItemObject.data
							}
							callback(true, null)
						}
					} ?: callback(false, BucketNotFoundException())
				}
			} catch (e : Exception) {
				e.printStackTrace()
				callback(false, e)
			}
		}
	}

	fun getAllBucketAsFlow() : Flow<RealmResults<BucketObject>> {
		return if (realm == null) throw RealmNotInitializedException()
		else realm !!.query(BucketObject::class).asFlow().map { it.list }
	}

	fun getAllBucket() : RealmResults<BucketObject> {
		return if (realm == null) throw RealmNotInitializedException()
		else realm !!.query(BucketObject::class).find()
	}

	fun getBucketAsFlow(id : RealmUUID) : Flow<BucketObject?> {
		return if (realm == null) throw RealmNotInitializedException()
		else realm !!.query(BucketObject::class, "id == $0 ", id).first().asFlow().map { it.obj }
	}

	fun getBucketFromId(id : RealmUUID) : BucketObject? {
		return if (realm == null) throw RealmNotInitializedException()
		else realm !!.query(BucketObject::class, "id == $0 ", id).first().find()
	}

	fun getBucketItemAsFlow(id : RealmUUID) : Flow<BucketItemObject?> {
		return if (realm == null) throw RealmNotInitializedException()
		else realm !!.query(BucketItemObject::class, "id == $0 ", id).first().asFlow().map { it.obj }
	}

	fun getAllBucketItem() : RealmResults<BucketItemObject> {
		return if (realm == null) throw RealmNotInitializedException()
		else realm !!.query(BucketItemObject::class).find()
	}

	fun getBucketItem(id : RealmUUID) : BucketItemObject? {
		return if (realm == null) throw RealmNotInitializedException()
		else realm !!.query(BucketItemObject::class, "id == $0 ", id).first().find()
	}

	fun getAttachmentFromId(id : RealmUUID) : AttachmentObject? {
		return if (realm == null) throw RealmNotInitializedException()
		else realm !!.query(AttachmentObject::class, "id == $0 ", id).first().find()
	}

	fun getAllAttachmentAsFlow() : Flow<RealmResults<AttachmentObject>> {
		return if (realm == null) throw RealmNotInitializedException()
		else realm !!.query(AttachmentObject::class).asFlow().map { it.list }
	}

	fun getAllAttachment() : RealmResults<AttachmentObject> {
		return if (realm == null) throw RealmNotInitializedException()
		else realm !!.query(AttachmentObject::class).find()
	}

	fun putAttachment(noteId : RealmUUID, attachmentObjectList : List<AttachmentObject>, callback : (CallbackStatus, Exception?) -> Unit) {
		CoroutineScope(Dispatchers.IO).launch {
			try {
				runBlocking {
					attachmentObjectList.forEach {
						putAttachment(noteId, it, 10) { status, e ->

						}
					}
					callback(CallbackStatus.SUCCESS, null)
				}
			} catch (e : Exception) {
				callback(CallbackStatus.ERROR, e)
			}
		}
	}

	suspend fun putAttachment(noteId : RealmUUID, attachmentObject : AttachmentObject, retry : Int, callback : (Boolean, Exception?) -> Unit) {
		try {
			attachmentObject.parentNoteId = noteId
			realm?.write {
				val noteObject = getNoteFromId(noteId)
				noteObject?.let {
					findLatest(it)?.attachmentList?.add(attachmentObject)
					callback(true, null)
				}
			}
		} catch (e : Exception) {
			if (retry > 0) {
				delay(500)
				putAttachment(noteId, attachmentObject, retry - 1, callback)
			} else {
				callback(false, e)
			}
		}
	}

	fun deleteAttachment(attachmentObjectList : List<AttachmentObject>, callback : (CallbackStatus, Exception?) -> Unit) {
		CoroutineScope(Dispatchers.IO).launch {
			try {
				runBlocking {
					attachmentObjectList.forEach {
						deleteAttachment(it, 10) { status, e ->

						}
					}
					callback(CallbackStatus.SUCCESS, null)
				}
			} catch (e : Exception) {
				callback(CallbackStatus.ERROR, e)
			}
		}
	}

	suspend fun deleteAttachment(attachmentObject : AttachmentObject, retry : Int, callback : (Boolean, Exception?) -> Unit) {
		try {
			realm?.write {
				findLatest(attachmentObject)?.let { delete(it) }
			}
		} catch (e : Exception) {
			if (retry > 0) {
				delay(500)
				e.printStackTrace()
				deleteAttachment(attachmentObject, retry - 1, callback)
			} else {
				callback(false, e)
			}
		}
	}

	fun getAllAttachmentWithFileAsFlow() : Flow<List<Triple<AttachmentObject, File?, Uri?>>> {
		return if (realm == null) throw RealmNotInitializedException()
		else getAllAttachmentAsFlow()
			.map {
				it.map {
					val file = getAttachmentFile(context, it.id, it.extension)
					val uri = file?.let { it1 -> FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", it1) }
					Triple(it, file, uri)
				}
			}
	}

	fun getAllAttachmentWithFile() : List<Triple<AttachmentObject, File?, Uri?>> {
		return if (realm == null) throw RealmNotInitializedException()
		else getAllAttachment().map {
			val file = getAttachmentFile(context, it.id, it.extension)
			val uri = file?.let { it1 -> FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", it1) }
			Triple(it, file, uri)
		}
	}

	fun getAttachmentFromNote(noteId : RealmUUID) : Flow<List<Triple<AttachmentObject, File?, Uri?>>?> {
		return getNoteFromIdAsFlow(noteId).map {
			it?.attachmentList?.map {
				val file = it.id.let { it1 -> getAttachmentFile(context, it1, it.extension) }
				val uri = file?.let { it1 -> FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", it1) }
				Triple(it, file, uri)
			}
		}
	}

	fun getAttachmentFile(context : Context, id : RealmUUID, extension : String?) : File? {
		return try {
			val attachmentDirPath = "${context.filesDir.path}/data/attachment"
			val filePath = "$attachmentDirPath/$id${if (extension != null) ".$extension" else ""}"
			File(filePath)
		} catch (e : Exception) {
//  		TODO Show error message
			null
		}
	}

	fun bufferAttachment(uriList : List<Uri>) : MutableMap<RealmUUID, Triple<AttachmentObject, File?, Uri>> {
		val attachmentList : MutableMap<RealmUUID, Triple<AttachmentObject, File?, Uri>> = mutableMapOf()
		uriList.forEach { uri ->
			var extension : String? = null
			val name = context.getFileName(uri)
			try {
				extension =
					if (uri.scheme.equals(ContentResolver.SCHEME_CONTENT))
						MimeTypeMap.getSingleton().getExtensionFromMimeType(context.applicationContext.contentResolver.getType(uri))
					else
						MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(uri.path?.let { File(it) }).toString())
			} catch (e : Exception) {
//		    	TODO Show error message
				e.printStackTrace()
			} finally {
				AttachmentObject().apply {
					this.name = name ?: this.id.toString()
					this.extension = extension
					this.mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)

					val uriAndFile = createTempAttachmentFileToExpose(context, this.id.toString(), this.extension)
					val inputStream = context.contentResolver.openInputStream(uri)
					val outputStream = context.contentResolver.openOutputStream(uriAndFile.first)

					if (inputStream != null && outputStream != null) copyInputStreamToOutputStream(inputStream, outputStream)

					inputStream?.close()
					outputStream?.close()

					Triple(uriAndFile.first, uriAndFile.second, this)

					attachmentList[this.id] = Triple(this, uriAndFile.second, uriAndFile.first)
				}
			}
		}

		return attachmentList
	}

	fun getAttachmentFile(id : RealmUUID, extension : String?) : File? {
		return try {
			val attachmentDirPath = "${context.filesDir.path}/data/attachment"
			val filePath = "$attachmentDirPath/$id${if (extension != null) ".$extension" else ""}"
			File(filePath)
		} catch (e : Exception) {
//  		TODO Show error message
			e.printStackTrace()
			null
		}
	}

	fun putTag(tagObject : TagObject) {
		if (realm == null) throw RealmNotInitializedException()
		else {
			CoroutineScope(Dispatchers.IO).launch {
				realm?.write {
					val storedTagObject = getTagFromId(tagObject.id)
					if (storedTagObject != null) {
						findLatest(storedTagObject)?.let {
							it.tag = tagObject.tag
							it.color = tagObject.color
						}
					} else {
						copyToRealm(tagObject)
					}
				}
			}
		}
	}

	fun deleteTag(id : RealmUUID?) {
		if (realm == null) throw RealmNotInitializedException()
		else {
			CoroutineScope(Dispatchers.IO).launch {
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

	fun connectTag(noteId : RealmUUID, tagIdList : List<RealmUUID>, callback : (Boolean, Exception?) -> Unit) {
		if (realm == null) throw RealmNotInitializedException()
		else {
			CoroutineScope(Dispatchers.IO).launch {
				try {
					realm?.write {
						getAllTag().forEach { findLatest(it)?.RealmUUIDList?.remove(noteId) }
						tagIdList.forEach {
							val tagObject = getTagFromId(it)
							if (tagObject != null) {
								findLatest(tagObject)?.let {
									if (it.RealmUUIDList.contains(noteId)) it.RealmUUIDList.remove(noteId)
									else {
										it.RealmUUIDList.add(noteId)
									}
								}
							}
						}
						callback(true, null)
					}
				} catch (e : Exception) {
					e.printStackTrace()
					callback(false, e)
				}
			}
		}
	}

	fun connectTag(noteId : RealmUUID, tagId : RealmUUID) {
		if (realm == null) throw RealmNotInitializedException()
		else {
			CoroutineScope(Dispatchers.IO).launch {
				realm?.write {
					val tagObject = getTagFromId(tagId)
					if (tagObject != null) {
						findLatest(tagObject)?.let {
							if (it.RealmUUIDList.contains(noteId)) it.RealmUUIDList.remove(noteId)
							else it.RealmUUIDList.add(noteId)
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

	fun getAllTagAsFlow() : Flow<RealmResults<TagObject>> {
		return if (realm == null) throw RealmNotInitializedException()
		else realm !!.query(TagObject::class).asFlow().map { it.list }
	}

	fun getAllTag() : RealmResults<TagObject> {
		return if (realm == null) throw RealmNotInitializedException()
		else realm !!.query(TagObject::class).find()
	}

	fun isKeyPresentInBucketItem(key : String?) : Boolean {
		return if (realm == null) throw RealmNotInitializedException()
		else realm !!.query(BucketItemObject::class, "key == $0 ", key).count().find() > 0
	}

	fun delete(RealmUUIDList : List<RealmUUID>) {
		if (realm == null) throw RealmNotInitializedException()
		else {
			CoroutineScope(Dispatchers.IO).launch {
				RealmUUIDList.forEach {
					delete(it)
				}
			}
		}
	}

	private suspend fun delete(RealmUUID : RealmUUID) {
		realm?.write {
			val noteObject = getNoteFromId(RealmUUID)
			val chapterObject = noteObject?.parentChapterId?.let { getChapterFromId(it) }

			if (chapterObject != null) {
				findLatest(chapterObject)
					?.noteList
					?.removeIf { it.id == RealmUUID }
			}
			noteObject?.let { findLatest(it)?.let { this.delete(it) } }

			val bucketObject = getBucketFromId(RealmUUID)
			bucketObject?.bucketItemList?.map { it.id }?.let { this@Repository2.delete(it) }
			bucketObject?.let { findLatest(it)?.let { this.delete(it) } }

			val bucketItemObject = getBucketItem(RealmUUID)
			bucketItemObject?.let { findLatest(it)?.let { this.delete(it) } }
		}
	}

	fun clearRealm(callback : (Boolean, Exception?) -> Unit) {
		if (realm == null) throw RealmNotInitializedException()
		else {
			CoroutineScope(Dispatchers.IO).launch {
				try {
					realm !!.write {
						realmConfiguration !!.schema.forEach {
							this.query(it).find().let { this.delete(it) }
						}
					}
					callback(true, null)
				} catch (e : Exception) {
					callback(false, e)
				}
			}
		}
	}
}
