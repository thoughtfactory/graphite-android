package com.syncodec.graphite.di

import android.app.Application
import android.content.Context
import android.util.Log
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.kedia.ogparser.OpenGraphResult
import com.syncodec.graphite.di.model.AttachmentObject
import com.syncodec.graphite.di.model.BaseObject
import com.syncodec.graphite.di.model.BucketItemObject
import com.syncodec.graphite.di.model.BucketItemState
import com.syncodec.graphite.di.model.BucketObject
import com.syncodec.graphite.di.model.ChapterObject
import com.syncodec.graphite.di.model.NoteObject
import com.syncodec.graphite.di.model.QuoteObject
import com.syncodec.graphite.di.model.TagObject
import com.syncodec.graphite.di.network.FirebaseStorageApi
import com.syncodec.graphite.di.network.Network
import com.syncodec.graphite.di.network.OpenLibraryApi
import com.syncodec.graphite.di.network.TMDbApi
import com.syncodec.graphite.utils.encodeBase64
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.types.ObjectId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Singleton

object Repository {

	var application : Application? = null

	val objectMapper = jsonMapper { addModule(kotlinModule()) }.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

	val realm = Realm.open(
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
		).build()
	)

	val network = Network()
	val firebaseStorageApi = FirebaseStorageApi()
	val openLibraryApi = OpenLibraryApi()
	val tmDbApi = TMDbApi()

	fun putBase(baseObject : BaseObject) {
		CoroutineScope(Dispatchers.IO).launch {
			realm.write {
				copyToRealm(baseObject)
			}
		}
	}

	fun getBase() = realm.query(BaseObject::class).find().firstOrNull()

	fun getBaseAsFlow() = realm.query(BaseObject::class).first().asFlow()

	fun putQuote(quoteObject : QuoteObject) {
//		store.boxFor<QuoteObject>().put(quoteObject)
	}

	fun getQuoteFromNetwork(date : String, onSuccess : (QuoteObject) -> Unit) {
		CoroutineScope(Dispatchers.IO).launch {
			firebaseStorageApi.getQuote(date = date, onSuccess = onSuccess)
		}
	}

	fun getQuoteByDate(date : String, onSuccess : (QuoteObject) -> Unit) {
//		val quoteObject = store.boxFor<QuoteObject>().query(QuoteObject_.date.equal(date)).build().findFirst()
//		if (quoteObject == null) {
//			getQuoteFromNetwork(date = date, onSuccess = onSuccess)
//		} else {
//			onSuccess(quoteObject)
//		}
	}

	fun getQuoteBgFromNetwork(date : String, onSuccess : () -> Unit) {
		CoroutineScope(Dispatchers.IO).launch {
			firebaseStorageApi.getQuoteBg(date = date) { bg ->
				getQuoteByDate(date = date) { quote ->
//					quote.bg = bg
					putQuote(quote)
					onSuccess()
				}
			}
		}
	}

	fun getQuoteKeyList() {
//		store.boxFor<QuoteObject>().query().build().find().map { it.date }
	}

//	NOTE - Chapter component

	fun getAllNotebookAsFlow() = realm.query(ChapterObject::class).asFlow().map { it.list }

	fun getNotebookAsFlow(id : ObjectId) = realm.query(ChapterObject::class, "id == $0 ", id).first().asFlow().map { it.obj }

	fun putChapter(parentChapterId : ObjectId?, chapterObject : ChapterObject) {
		CoroutineScope(Dispatchers.IO).launch {
			realm.write {
				if (parentChapterId == null) {
					copyToRealm(chapterObject)
				} else {
					val parentChapter = realm.query(ChapterObject::class, "id == $0", parentChapterId).first().find()
					if (parentChapter != null) {
						findLatest(parentChapter)?.chapterList?.add(chapterObject)
					}
				}
			}
		}
	}

	fun updateChapter(
		id : ObjectId,
		title : String,
		description : String?,
		color : Int?,
		thumbnail : String?,
		isFavourite : Boolean,
		isLocked : Boolean,
	) {
		CoroutineScope(Dispatchers.IO).launch {
			realm.write {
				val chapter = realm.query(ChapterObject::class, "id == $0", id).first().find()
				if (chapter != null) {
					findLatest(chapter)?.apply {
						this.title = title
						this.description = description
						this.color = color
						this.thumbnail = thumbnail
						this.isFavourite = isFavourite
						this.isLocked = isLocked
					}
				}
			}
		}
	}

	fun getChapter(id : ObjectId) = realm.query(ChapterObject::class, "id == $0 ", id).first().find()

	fun getChapterAsFlow(id : ObjectId) = realm.query(ChapterObject::class, "id == $0 ", id).first().asFlow().map { it.obj }

	fun getAllChapterAsFlow() : Flow<List<ChapterObject>> = realm.query(ChapterObject::class).asFlow().map { it.list }

//	NOTE - Note component

	fun putNote(chapterId : ObjectId, noteObject : NoteObject, onSuccess : () -> Unit) {
		CoroutineScope(Dispatchers.IO).launch {
			val storedNoteObject = getNote(id = noteObject.id)

			realm.write {
				if (storedNoteObject != null) {
					findLatest(storedNoteObject)?.let {
						noteObject.parentChapterId = chapterId

						deleteNote(it.id) {
							getChapter(chapterId)?.let { chapterObject ->
								noteObject.parentChapterId = chapterId
								findLatest(chapterObject)
									?.noteList
									?.add(noteObject)

								onSuccess()
							}
						}
					}
				} else {
					getChapter(chapterId)?.let { chapterObject ->
						noteObject.parentChapterId = chapterId
						findLatest(chapterObject)
							?.noteList
							?.add(noteObject)

						onSuccess()
					}
				}
			}
		}
	}

	fun getNoteAsFlow(id : String) = realm.query(ChapterObject::class, "id == $0 ", id).first().asFlow().map { it.obj }

	fun getNote(id : ObjectId) = realm.query(NoteObject::class, "id == $0 ", id).first().find()

	fun getAllNoteAsFlow() : Flow<List<NoteObject>> = realm.query(NoteObject::class).asFlow().map { it.list }

//	ERROR Delete operations are not updated
//	@OptIn(ExperimentalCoroutinesApi::class)
//	fun getNoteFromChapterAsFlow(chapterId: String) = store.boxFor<ChapterObject>().query(ChapterObject_.uId.equal(chapterUId)).build().flow().map { it.firstOrNull() }

	fun getNoteListFromChapterAsFlow(chapterId : String) =
		realm.query(ChapterObject::class, "Id == $0 ", chapterId).first().asFlow().map { it.obj?.noteList }

	fun getDefaultNotebookId() = getBaseAsFlow().map { it.obj?.defaultChapterId }

	fun deleteNote(id : ObjectId, onSuccess : () -> Unit) {
		CoroutineScope(Dispatchers.IO).launch {
			realm.write {
				getNote(id)?.let {
					it.attachmentList.forEach { attachmentObject ->
						deleteAttachment(attachmentObject.id)
					}
					findLatest(it)?.let { it1 -> delete(it1) }
				}
				onSuccess()
			}
		}
	}

	fun getChapterTitle(id : ObjectId) = realm.query(ChapterObject::class, "id == $0 ", id).first().find()?.title

	fun getAllAttachmentAsFlow() = realm.query(AttachmentObject::class).asFlow().map { it.list }
	fun getAttachment(id : ObjectId) = realm.query(AttachmentObject::class, "id == $0 ", id).first().find()

	fun deleteAttachment(id : ObjectId) {
		CoroutineScope(Dispatchers.IO).launch {
			realm.write {
				getAttachment(id)?.let {
					application?.getAttachmentFile(it.id, it.extension)?.let {
						if (it.exists()) {
							it.delete()
						}
					}
					findLatest(it)?.let { it1 -> delete(it1) }
				}
			}
		}
	}

//	NOTE - Bucket component

	fun putBucket(bucketObject : BucketObject) {
		CoroutineScope(Dispatchers.IO).launch {
			realm.write {
				copyToRealm(bucketObject)
			}
		}
	}

	fun updateBucket(
		id : ObjectId,
		title : String,
		description : String?,
		isFavourite : Boolean,
		isLocked : Boolean,
	) {
		CoroutineScope(Dispatchers.IO).launch {

			realm.write {
				val bucket = getBucket(id = id)
				if (bucket != null) {
					findLatest(bucket)?.apply {
						this.modifiedTimestamp = System.currentTimeMillis()

						this.title = title
						this.description = description
						this.isFavourite = isFavourite
						this.isLocked = isLocked
					}
				}
			}
		}
	}

	fun getAllBucketAsFlow() = realm.query(BucketObject::class).asFlow().map { it.list }

	fun getBucketAsFlow(id : ObjectId) = realm.query(BucketObject::class, "id == $0 ", id).first().asFlow().map { it.obj }

	fun getBucket(id : ObjectId) = realm.query(BucketObject::class, "id == $0 ", id).first().find()

	fun putBucketItem(bucketId : ObjectId, bucketItemObject : BucketItemObject, onSuccess : (() -> Unit)? = null) {
		CoroutineScope(Dispatchers.IO).launch {
			val storedNoteObject = getBucketItem(id = bucketItemObject.id)

			realm.write {
				if (storedNoteObject != null) {
					findLatest(storedNoteObject)?.let {
						delete(it)
					}
				}
				getBucket(bucketId)?.let { bucketObject ->
					findLatest(bucketObject)
						?.bucketItemList
						?.add(bucketItemObject)

					onSuccess?.invoke()
				}
			}
		}
	}

	fun updateBucketItem(
		id : ObjectId,
		state: BucketItemState,
		isFavourite : Boolean,
		isLocked : Boolean,
	) {
		CoroutineScope(Dispatchers.IO).launch {
			realm.write {
				val bucketItem = getBucketItem(id = id)
				Log.i("npr71", "id: $id")
				Log.i("npr71", "updateBucketItem: $bucketItem")
				if (bucketItem != null) {
					findLatest(bucketItem)?.apply {
						this.modifiedTimestamp = System.currentTimeMillis()

						this.state = state.name
						this.isFavourite = isFavourite
						this.isLocked = isLocked
					}
				}
			}
		}
	}

	fun putBucketItemLink(bucketId : ObjectId, bucketItemObject : BucketItemObject, onSuccess : (() -> Unit)? = null) {
		CoroutineScope(Dispatchers.IO).launch {

			if (bucketItemObject.data == null) {
				putBucketItem(bucketId, bucketItemObject, onSuccess)
			} else {
				val openGraphResult = objectMapper.readValue<OpenGraphResult>(bucketItemObject.data !!)
				network.retrieveImage(openGraphResult.image) {
					bucketItemObject.thumbnail = it?.encodeBase64()
					putBucketItem(bucketId, bucketItemObject, onSuccess)
				}
			}
		}
	}

	fun getBucketItemAsFlow(id : ObjectId) = realm.query(BucketItemObject::class, "id == $0 ", id).first().asFlow().map { it.obj }

	fun getBucketItem(id : ObjectId) = realm.query(BucketItemObject::class, "id == $0 ", id).first().find()

	fun Context.getAttachmentFile(id : ObjectId, extension : String?) : File? {
		return try {
			val attachmentDirPath = "${filesDir.path}/data/attachment"
			val filePath = "$attachmentDirPath/$id${if (extension != null) ".$extension" else ""}"
			File(filePath)
		} catch (e : Exception) {
//  		TODO Show error message
			e.printStackTrace()
			null
		}
	}

	fun putTag(tagObject : TagObject) {
		CoroutineScope(Dispatchers.IO).launch {
			realm.write {
				copyToRealm(tagObject)
			}
		}
	}

	fun getTag(id : ObjectId) = realm.query(TagObject::class, "id == $0 ", id).first().find()

	fun getAllTagAsFlow() : Flow<List<TagObject>> = realm.query(TagObject::class).asFlow().map { it.list }


	fun updateTagConnection(tagObjectId : ObjectId, objectId : ObjectId?) {
		CoroutineScope(Dispatchers.IO).launch {

			try {
				realm.write {
					val _tagObject = this.query(TagObject::class, "id == $0", tagObjectId).find().first()

					if (objectId != null) {
						if (_tagObject.objectIdList.contains(objectId)) {
							this.findLatest(_tagObject)?.objectIdList?.remove(objectId)
						} else {
							this.findLatest(_tagObject)?.objectIdList?.add(objectId)
						}
					}
				}
			} catch (e : Exception) {
				e.printStackTrace()
			}
		}
	}
}
