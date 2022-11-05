package com.syncodec.graphite.di.repository

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
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
import com.syncodec.graphite.di.model.TagObject
import com.syncodec.graphite.di.network.FirebaseStorageApi
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
import io.realm.kotlin.types.ObjectId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.security.InvalidAlgorithmParameterException
import java.security.InvalidKeyException
import java.security.KeyStore
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
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
	INIT, LOADING, SUCCESS, ERROR
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

	private var realm : Realm? = null

	val firebaseStorageApi = FirebaseStorageApi()

	val passcode = ByteArray(64)

	init {
		for (i in 0 until 64) {
			passcode[i] = i.toByte()
		}
		val a = passcode.joinToString("") {
			java.lang.String.format("%02x", it)
		}
		Log.i("npr71", "passcode: $a")

		try {
			realm = Realm
				.open(
					RealmConfiguration.Builder(
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
//					    .encryptionKey(getNewKey(context))
						.encryptionKey(passcode)
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
				)

			repositoryState.tryEmit(RepositoryState.SUCCESS)
		} catch (e : Exception) {
			e.printStackTrace()
			repositoryState.tryEmit(RepositoryState.ERROR)
		}
	}

	fun putDefaultChapterId(id : ObjectId, callback : (CallbackStatus) -> Unit) {
		CoroutineScope(Dispatchers.IO).launch {
			if (realm == null) {
				callback(CallbackStatus.UNINITIALIZED)
			} else {
				realm?.write {
					val baseObject = this.query(BaseObject::class).first().find()
					baseObject?.let { findLatest(it)?.defaultChapterId = id }
					callback(CallbackStatus.SUCCESS)
				}
			}
		}
	}

	fun getDefaultChapterId() : Flow<ObjectId?> {
		return if (realm == null) throw RealmNotInitializedException()
		else realm !!.query(BaseObject::class).first().asFlow().map { it.obj?.defaultChapterId }
	}

	fun putChapter(parentChapterId : ObjectId?, chapterObject : ChapterObject, callback : (Boolean, Exception?) -> Unit) {
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

	fun getChapterFromIdAsFlow(id : ObjectId) : Flow<ChapterObject?> {
		return if (realm == null) throw RealmNotInitializedException()
		else realm !!.query(ChapterObject::class, "id == $0 ", id).first().asFlow().map { it.obj }
	}

	fun getChapterFromId(id : ObjectId?) : ChapterObject? {
		return if (realm == null) throw RealmNotInitializedException()
		else realm !!.query(ChapterObject::class, "id == $0 ", id).first().find()
	}

	fun getChapterWithParentIdAsFlow(parentChapterId : ObjectId?) : Flow<List<ChapterObject>> {
		return if (realm == null) throw RealmNotInitializedException()
		else realm !!.query(ChapterObject::class, "parentChapterId == $0 ", parentChapterId).asFlow().map { it.list }
	}

	fun getChapterWithParentId(parentChapterId : ObjectId?) : List<ChapterObject> {
		return if (realm == null) throw RealmNotInitializedException()
		else realm !!.query(ChapterObject::class, "parentChapterId == $0 ", parentChapterId).find().toList()
	}

	fun getParentChapterList(id : ObjectId?, includeEdge : Boolean = false, callback : (List<ChapterObjectLite>?, Exception?) -> Unit) {
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

	fun moveNoteToChapter(noteId : ObjectId, chapterId : ObjectId) {
		try {
			if (realm == null) throw RealmNotInitializedException()
			else CoroutineScope(Dispatchers.IO).launch {
				realm !!.write {
					val storedNoteObject = getNoteFromId(noteId)
					val moveToStoredChapterObject = getChapterFromId(chapterId)
//  				val moveFromStoredChapterObject = getChapterFromId(storedNoteObject?.parentChapterId)
					Log.i("npr71", "storedNoteObject = ${storedNoteObject?.id}")
					Log.i("npr71", "moveToStoredChapterObject = ${moveToStoredChapterObject?.id}")
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

	fun deleteNote(id : ObjectId, callback : (Boolean, Exception?) -> Unit) {
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

	fun getNoteFromId(id : ObjectId) : NoteObject? {
		return if (realm == null) throw RealmNotInitializedException()
		else realm !!.query(NoteObject::class, "id == $0 ", id).first().find()
	}

	fun getNoteFromIdAsFlow(id : ObjectId) : Flow<NoteObject?> {
		return if (realm == null) throw RealmNotInitializedException()
		else realm !!.query(NoteObject::class, "id == $0 ", id).first().asFlow().map { it.obj }
	}

	fun getAllNoteAsFlow() : Flow<RealmResults<NoteObject>> {
		return if (realm == null) throw RealmNotInitializedException()
		else realm !!.query(NoteObject::class).asFlow().map { it.list }
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

	fun putBucketItem(bucketId : ObjectId, bucketItemObject : BucketItemObject, callback : (Boolean, Exception?) -> Unit) {
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

	fun getBucketAsFlow(id : ObjectId) : Flow<BucketObject?> {
		return if (realm == null) throw RealmNotInitializedException()
		else realm !!.query(BucketObject::class, "id == $0 ", id).first().asFlow().map { it.obj }
	}

	fun getBucketFromId(id : ObjectId) : BucketObject? {
		return if (realm == null) throw RealmNotInitializedException()
		else realm !!.query(BucketObject::class, "id == $0 ", id).first().find()
	}

	fun getBucketItemAsFlow(id : ObjectId) : Flow<BucketItemObject?> {
		return if (realm == null) throw RealmNotInitializedException()
		else realm !!.query(BucketItemObject::class, "id == $0 ", id).first().asFlow().map { it.obj }
	}

	fun getBucketItem(id : ObjectId) : BucketItemObject? {
		return if (realm == null) throw RealmNotInitializedException()
		else realm !!.query(BucketItemObject::class, "id == $0 ", id).first().find()
	}

	fun getAttachmentFromId(id : ObjectId) : AttachmentObject? {
		return if (realm == null) throw RealmNotInitializedException()
		else realm !!.query(AttachmentObject::class, "id == $0 ", id).first().find()
	}

	fun getAllAttachmentAsFlow() : Flow<RealmResults<AttachmentObject>> {
		return if (realm == null) throw RealmNotInitializedException()
		else realm !!.query(AttachmentObject::class).asFlow().map { it.list }
	}

	fun putAttachment(noteId : ObjectId, attachmentObjectList : List<AttachmentObject>, callback : (CallbackStatus, Exception?) -> Unit) {
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

	suspend fun putAttachment(noteId : ObjectId, attachmentObject : AttachmentObject, retry : Int, callback : (Boolean, Exception?) -> Unit) {
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

	fun getAttachmentFromNote(noteId : ObjectId) : Flow<List<Triple<AttachmentObject?, File?, Uri?>>?> {
		return getNoteFromIdAsFlow(noteId).map {
			it?.attachmentList?.map {
				val attachmentObject = getAttachmentFromId(it.id)
				val file = attachmentObject?.id?.let { it1 -> getAttachmentFile(context, it1, attachmentObject?.extension) }
				val uri = file?.let { it1 -> FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", it1) }
				Triple(attachmentObject, file, uri)
			}
		}
	}

	fun getAttachmentFile(context : Context, id : ObjectId, extension : String?) : File? {
		return try {
			val attachmentDirPath = "${context.filesDir.path}/data/attachment"
			val filePath = "$attachmentDirPath/$id${if (extension != null) ".$extension" else ""}"
			File(filePath)
		} catch (e : Exception) {
//  		TODO Show error message
			null
		}
	}

	fun bufferAttachment(uriList : List<Uri>) : MutableMap<ObjectId, Triple<AttachmentObject, File?, Uri>> {
		val attachmentList : MutableMap<ObjectId, Triple<AttachmentObject, File?, Uri>> = mutableMapOf()
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

	fun getAttachmentFile(id : ObjectId, extension : String?) : File? {
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

	fun delete(objectIdList : List<ObjectId>) {
		if (realm == null) throw RealmNotInitializedException()
		else {
			CoroutineScope(Dispatchers.IO).launch {
				objectIdList.forEach {
					delete(it)
				}
			}
		}
	}

	private suspend fun delete(objectId : ObjectId) {
		realm?.write {
			val noteObject = getNoteFromId(objectId)
			val chapterObject = noteObject?.parentChapterId?.let { getChapterFromId(it) }

			if (chapterObject != null) {
				findLatest(chapterObject)
					?.noteList
					?.removeIf { it.id == objectId }
			}
			noteObject?.let { findLatest(it)?.let { this.delete(it) } }

			val bucketObject = getBucketFromId(objectId)
			bucketObject?.bucketItemList?.map { it.id }?.let { delete(it) }
			bucketObject?.let { findLatest(it)?.let { this.delete(it) } }

			val bucketItemObject = getBucketItem(objectId)
			bucketItemObject?.let { findLatest(it)?.let { this.delete(it) } }
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

	fun deleteTag(id : ObjectId?) {
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

	fun getTagFromId(id : ObjectId?) : TagObject? {
		return if (realm == null) throw RealmNotInitializedException()
		else realm!!.query(TagObject::class, "id == $0", id).first().find()
	}

	fun getTagFromIdAsFlow(id : ObjectId) : Flow<TagObject?> {
		return if (realm == null) throw RealmNotInitializedException()
		else realm!!.query(TagObject::class, "id == $0", id).first().asFlow().map { it.obj }
	}

	fun connectTag(noteId : ObjectId, tagIdList:List<ObjectId>, callback : (Boolean, Exception?) -> Unit) {
		if (realm == null) throw RealmNotInitializedException()
		else {
			CoroutineScope(Dispatchers.IO).launch {
				try {
					realm?.write {
						getAllTag().forEach { findLatest(it)?.objectIdList?.remove(noteId) }
						tagIdList.forEach {
							val tagObject = getTagFromId(it)
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
					e.printStackTrace()
					callback(false, e)
				}
			}
		}
	}

	fun connectTag(noteId : ObjectId, tagId : ObjectId) {
		if (realm == null) throw RealmNotInitializedException()
		else {
			CoroutineScope(Dispatchers.IO).launch {
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

	fun getTagFromName(tag: String) : TagObject? {
		return if (realm == null) throw RealmNotInitializedException()
		else realm!!.query(TagObject::class, "tag == $0", tag).first().find()
	}

	fun getTagFromNameAsFlow(tag: String) : Flow<TagObject?> {
		return if (realm == null) throw RealmNotInitializedException()
		else realm!!.query(TagObject::class, "tag == $0", tag).first().asFlow().map { it.obj }
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

	fun getNewKey(context : Context) : ByteArray {
		// open a connection to the android keystore
		val keyStore : KeyStore
		try {
			keyStore = KeyStore.getInstance("AndroidKeyStore")
			keyStore.load(null)
		} catch (e : Exception) {
			Log.v("EXAMPLE", "Failed to open the keystore.")
			throw RuntimeException(e)
		}
		// create a securely generated random asymmetric RSA key
		val realmKey = ByteArray(Realm.ENCRYPTION_KEY_LENGTH)
		SecureRandom().nextBytes(realmKey)
		// create a cipher that uses AES encryption -- we'll use this to encrypt our key
		val cipher : Cipher = try {
			Cipher.getInstance(
				KeyProperties.KEY_ALGORITHM_AES
						+ "/" + KeyProperties.BLOCK_MODE_CBC
						+ "/" + KeyProperties.ENCRYPTION_PADDING_PKCS7
			)
		} catch (e : Exception) {
			Log.e("EXAMPLE", "Failed to create a cipher.")
			throw RuntimeException(e)
		}
		// generate secret key
		val keyGenerator : KeyGenerator = try {
			KeyGenerator.getInstance(
				KeyProperties.KEY_ALGORITHM_AES,
				"AndroidKeyStore"
			)
		} catch (e : NoSuchAlgorithmException) {
			Log.e("EXAMPLE", "Failed to access the key generator.")
			throw RuntimeException(e)
		}
		val keySpec = KeyGenParameterSpec.Builder(
			"realm_key",
			KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
		)
			.setBlockModes(KeyProperties.BLOCK_MODE_CBC)
			.setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
			.setUserAuthenticationRequired(false)
//			.setUserAuthenticationValidityDurationSeconds(300)
			.build()
		try {
			keyGenerator.init(keySpec)
		} catch (e : InvalidAlgorithmParameterException) {
			Log.e("EXAMPLE", "Failed to generate a secret key.")
			throw RuntimeException(e)
		}
		keyGenerator.generateKey()
		// access the generated key in the android keystore, then
		// use the cipher to create an encrypted version of the key
		val initializationVector : ByteArray
		val encryptedKeyForRealm : ByteArray
		try {
			val secretKey = keyStore.getKey("realm_key", null) as SecretKey
			cipher.init(Cipher.ENCRYPT_MODE, secretKey)
			encryptedKeyForRealm = cipher.doFinal(realmKey)
			initializationVector = cipher.iv
		} catch (e : Exception) {
			Log.e("EXAMPLE", "Failed encrypting the key with the secret key.")
			throw RuntimeException(e)
		}
		// keep the encrypted key in shared preferences
		// to persist it across application runs
		val initializationVectorAndEncryptedKey = ByteArray(
			Integer.BYTES +
					initializationVector.size +
					encryptedKeyForRealm.size
		)
		val buffer = ByteBuffer.wrap(initializationVectorAndEncryptedKey)
		buffer.order(ByteOrder.BIG_ENDIAN)
		buffer.putInt(initializationVector.size)
		buffer.put(initializationVector)
		buffer.put(encryptedKeyForRealm)
		context.getSharedPreferences("realm_key", Context.MODE_PRIVATE).edit()
			.putString("iv_and_encrypted_key", Base64.encodeToString(initializationVectorAndEncryptedKey, Base64.NO_WRAP))
			.apply()
		return realmKey // pass to a realm configuration via encryptionKey()
	}

	// Access the encrypted key in the keystore, decrypt it with the secret,
// and use it to open and read from the realm again
	fun getExistingKey(context : Context) : ByteArray {
		// open a connection to the android keystore
		val keyStore : KeyStore
		try {
			keyStore = KeyStore.getInstance("AndroidKeyStore")
			keyStore.load(null)
		} catch (e : Exception) {
			Log.e("EXAMPLE", "Failed to open the keystore.")
			throw RuntimeException(e)
		}
		// access the encrypted key that's stored in shared preferences
		val initializationVectorAndEncryptedKey = Base64.decode(
			context
				?.getSharedPreferences("realm_key", Context.MODE_PRIVATE)
				?.getString("iv_and_encrypted_key", null), Base64.DEFAULT
		)
		val buffer = ByteBuffer.wrap(initializationVectorAndEncryptedKey)
		buffer.order(ByteOrder.BIG_ENDIAN)
		// extract the length of the initialization vector from the buffer
		val initializationVectorLength = buffer.int
		// extract the initialization vector based on that length
		val initializationVector = ByteArray(initializationVectorLength)
		buffer[initializationVector]
		// extract the encrypted key
		val encryptedKey = ByteArray(
			initializationVectorAndEncryptedKey.size
					- Integer.BYTES
					- initializationVectorLength
		)
		buffer[encryptedKey]
		// create a cipher that uses AES encryption to decrypt our key
		val cipher : Cipher
		cipher = try {
			Cipher.getInstance(
				KeyProperties.KEY_ALGORITHM_AES
						+ "/" + KeyProperties.BLOCK_MODE_CBC
						+ "/" + KeyProperties.ENCRYPTION_PADDING_PKCS7
			)
		} catch (e : Exception) {
			Log.e("EXAMPLE", "Failed to create cipher.")
			throw RuntimeException(e)
		}
		// decrypt the encrypted key with the secret key stored in the keystore
		val decryptedKey : ByteArray = try {
			val secretKey = keyStore.getKey("realm_key", null) as SecretKey
			val initializationVectorSpec = IvParameterSpec(initializationVector)
			cipher.init(Cipher.DECRYPT_MODE, secretKey, initializationVectorSpec)
			cipher.doFinal(encryptedKey)
		} catch (e : InvalidKeyException) {
			Log.e("EXAMPLE", "Failed to decrypt. Invalid key.")
			throw RuntimeException(e)
		} catch (e : Exception) {
			Log.e(
				"EXAMPLE",
				"Failed to decrypt the encrypted realm key with the secret key."
			)
			throw RuntimeException(e)
		}
		return decryptedKey // pass to a realm configuration via encryptionKey()
	}

}
