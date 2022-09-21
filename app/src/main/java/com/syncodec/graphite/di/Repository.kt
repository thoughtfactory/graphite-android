package com.syncodec.graphite.di

import android.content.Context
import com.syncodec.graphite.di.model.*
import com.syncodec.graphite.di.network.FirebaseStorageApi
import com.syncodec.graphite.di.network.GoogleBooksApi
import com.syncodec.graphite.di.network.OpenLibraryApi
import com.syncodec.graphite.di.network.TMDbApi
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.types.ObjectId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.File


object Repository {

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

	val firebaseStorageApi = FirebaseStorageApi()
	val openLibraryApi = OpenLibraryApi()
	val googleBooksApi = GoogleBooksApi()
	val tmDbApi = TMDbApi()

	fun putBase(baseObject: BaseObject) {
		CoroutineScope(Dispatchers.IO).launch {
			realm.write {
				copyToRealm(baseObject)
			}
		}
	}

	fun getBase() = realm.query(BaseObject::class).find().firstOrNull()

	fun getBaseAsFlow() = realm.query(BaseObject::class).first().asFlow()

	fun putQuote(quoteObject: QuoteObject) {
//		store.boxFor<QuoteObject>().put(quoteObject)
	}

	fun getQuoteFromNetwork(date: String, onSuccess: (QuoteObject) -> Unit) {
		CoroutineScope(Dispatchers.IO).launch {
			firebaseStorageApi.getQuote(date = date, onSuccess = onSuccess)
		}
	}

	fun getQuoteByDate(date: String, onSuccess: (QuoteObject) -> Unit) {
//		val quoteObject = store.boxFor<QuoteObject>().query(QuoteObject_.date.equal(date)).build().findFirst()
//		if (quoteObject == null) {
//			getQuoteFromNetwork(date = date, onSuccess = onSuccess)
//		} else {
//			onSuccess(quoteObject)
//		}
	}

	fun getQuoteBgFromNetwork(date: String, onSuccess: () -> Unit) {
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

	fun getNotebookAsFlow(id: ObjectId) = realm.query(ChapterObject::class, "id == $0 ", id).first().asFlow().map { it.obj }

	fun putChapter(parentChapterId: ObjectId?, chapterObject: ChapterObject) {
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
		id: ObjectId,
		title: String,
		description: String?,
		color: Int,
		isFavourite: Boolean,
		isLocked: Boolean,
	) {
		CoroutineScope(Dispatchers.IO).launch {
			realm.write {
				val chapter = realm.query(ChapterObject::class, "id == $0", id).first().find()
				if (chapter != null) {
					findLatest(chapter)?.apply {
						this.title = title
						this.description = description
						this.color = color
						this.isFavourite = isFavourite
						this.isLocked = isLocked
					}
				}
			}
		}
	}

	fun getChapter(id: ObjectId) = realm.query(ChapterObject::class, "id == $0 ", id).first().find()

	fun getChapterAsFlow(id: ObjectId) = realm.query(ChapterObject::class, "id == $0 ", id).first().asFlow().map { it.obj }

	fun getAllChapterAsFlow(): Flow<List<ChapterObject>> = realm.query(ChapterObject::class).asFlow().map { it.list }

//	NOTE - Note component

	fun putNote(chapterId: ObjectId, noteObject: NoteObject, onSuccess: () -> Unit) {
		CoroutineScope(Dispatchers.IO).launch {
			val storedNoteObject = getNote(id = noteObject.id)

			realm.write {
				if (storedNoteObject != null) {
					findLatest(storedNoteObject)?.let {
						delete(it)
					}
				}
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

	fun getNoteAsFlow(id: String) = realm.query(ChapterObject::class, "id == $0 ", id).first().asFlow().map { it.obj }

	fun getNote(id: ObjectId) = realm.query(NoteObject::class, "id == $0 ", id).first().find()

//	ERROR Delete operations are not updated
//	@OptIn(ExperimentalCoroutinesApi::class)
//	fun getNoteFromChapterAsFlow(chapterId: String) = store.boxFor<ChapterObject>().query(ChapterObject_.uId.equal(chapterUId)).build().flow().map { it.firstOrNull() }

	fun getNoteListFromChapterAsFlow(chapterId: String) = realm.query(ChapterObject::class, "Id == $0 ", chapterId).first().asFlow().map { it.obj?.noteList }

	fun getDefaultNotebookId() = getBaseAsFlow().map { it.obj?.defaultChapterId }

	fun deleteNote(id: ObjectId) {
		CoroutineScope(Dispatchers.IO).launch {
			realm.write {
				getNote(id)?.let {
					findLatest(it)?.let { it1 -> delete(it1) }
				}
			}
		}
	}

	fun getChapterTitle(id: ObjectId) = realm.query(ChapterObject::class, "id == $0 ", id).first().find()?.title

//	NOTE - Bucket component

	fun putBucket(bucketObject: BucketObject) {
		CoroutineScope(Dispatchers.IO).launch {
			realm.write {
				copyToRealm(bucketObject)
			}
		}
	}

	fun updateBucket(
		id: ObjectId,
		title: String,
		description: String?,
		isFavourite: Boolean,
		isLocked: Boolean,
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

	fun getBucketAsFlow(id: ObjectId) = realm.query(BucketObject::class, "id == $0 ", id).first().asFlow().map { it.obj }

	fun getBucket(id: ObjectId) = realm.query(BucketObject::class, "id == $0 ", id).first().find()

	fun putBucketItem(bucketId: String, bucketItemObject: BucketItemObject) {
		CoroutineScope(Dispatchers.IO).launch {
			realm.write {
				copyToRealm(bucketItemObject)
			}
		}
	}

	fun getBucketItemAsFlow(id: String) = realm.query(BucketItemObject::class, "id == $0 ", id).first().asFlow().map { it.obj }

	fun Context.getAttachmentFile(id: ObjectId, extension: String?): File? {
		return try {
			val attachmentDirPath = "${filesDir.path}/data/attachment"
			val filePath = "$attachmentDirPath/$id${if (extension != null) ".$extension" else ""}"
			File(filePath)
		} catch (e: Exception) {
//  		TODO Show error message
			e.printStackTrace()
			null
		}
	}

	fun putTag(tagObject: TagObject) {
		CoroutineScope(Dispatchers.IO).launch {
			realm.write {
				copyToRealm(tagObject)
			}
		}
	}

	fun getAllTagAsFlow(): Flow<List<TagObject>> = realm.query(TagObject::class).asFlow().map { it.list }


	fun <T> updateTagConnection(tagObject: TagObject, realmObject: T) {
		CoroutineScope(Dispatchers.IO).launch {

			try {
				realm.write {
					val _tagObject = this.query(TagObject::class, "id == $0", tagObject.id).find().first()

					val objectId = if (realmObject is NoteObject) {
						realmObject.id
					} else if (realmObject is ChapterObject) {
						(realmObject as ChapterObject).id
					} else {
						null
					}

					if (objectId != null) {
						if (_tagObject.objectIdList.contains(objectId)) {
							this.findLatest(_tagObject)?.objectIdList?.remove(objectId)
						} else {
							this.findLatest(_tagObject)?.objectIdList?.add(objectId)
						}
					}
				}
			} catch (e: Exception) {
				e.printStackTrace()
			}
		}
	}
}
