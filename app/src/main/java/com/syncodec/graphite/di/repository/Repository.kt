package com.syncodec.graphite.di.repository

import android.content.Context
import com.syncodec.graphite.BuildConfig
import com.syncodec.graphite.di.model.local.BaseObject
import com.syncodec.graphite.di.model.local.BucketItemObject
import com.syncodec.graphite.di.model.local.BucketObject
import com.syncodec.graphite.di.model.local.BucketObjectLite
import com.syncodec.graphite.di.model.local.ChapterObject
import com.syncodec.graphite.di.model.local.ChapterObjectLite
import com.syncodec.graphite.di.model.local.DeletedAttachment
import com.syncodec.graphite.di.model.local.DeletedObject
import com.syncodec.graphite.di.model.local.NoteObject
import com.syncodec.graphite.di.model.local.NoteObjectLite
import com.syncodec.graphite.di.model.local.TagObject
import com.syncodec.graphite.di.repository.group.RealmObjectGroup
import com.syncodec.graphite.di.repository.group.RealmObjectGroupList
import com.syncodec.graphite.di.snapshot.SnapshotInator
import com.syncodec.graphite.service.syncInator.SyncInatorService
import com.syncodec.graphite.utils.SortBy
import com.syncodec.graphite.utils.SortOn
import com.syncodec.graphite.utils.dataStore.DataStoreInstance
import com.syncodec.graphite.utils.filterNotNull
import com.syncodec.graphite.utils.timeStampToPrettyDay
import io.realm.kotlin.MutableRealm
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import io.realm.kotlin.ext.toRealmList
import io.realm.kotlin.notifications.ResultsChange
import io.realm.kotlin.notifications.SingleQueryChange
import io.realm.kotlin.query.RealmResults
import io.realm.kotlin.types.BaseRealmObject
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.RealmUUID
import io.realm.kotlin.types.TypedRealmObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import java.io.File
import java.time.Instant
import kotlin.reflect.KProperty1


class Repository(val realm: Realm, private val context: Context, dataStoreInstance: DataStoreInstance) {

	val attachmentRepository = AttachmentRepository(context = context)
	val snapshotInator = SnapshotInator(context = context, repository = this)

	private val sortByFlow: Flow<SortBy?> = dataStoreInstance.getSortBy
	private val sortOnFlow: Flow<SortOn?> = dataStoreInstance.getSortOn

	private val _isUnlocked: MutableStateFlow<Boolean> = MutableStateFlow(false)
	val isUnlocked: StateFlow<Boolean> = _isUnlocked

	fun lockRepo() = this._isUnlocked.tryEmit(false)

	fun unlockRepo() = this._isUnlocked.tryEmit(true)

	suspend fun putDefaultChapterId(id: RealmUUID) {
		realm.write {
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
	 */
	fun getDefaultChapterIdAsFlow(): Flow<RealmUUID?> = realm.query(BaseObject::class).first().asFlow().map { it.obj?.defaultChapterId }

	/**
	 * Get the default chapterId stored in the [BaseObject]. This should not be null.
	 * @author pushpull
	 * @since 2.2.0
	 */
	fun getDefaultChapterId(): RealmUUID? = realm.query(BaseObject::class).first().find()?.defaultChapterId

	/**
	 * Get BaseObject from realm
	 * @author pushpull
	 * @since 2.2.0
	 * @return BaseObject or null. Mostly not null because baseObject is created when realm is initialized.
	 */
	fun getBaseObject(): BaseObject? = realm.query(BaseObject::class).first().find()

	/**
	 * Get BaseObject from realm as flow
	 * @author pushpull
	 * @since 2.2.0
	 * @return Flow of BaseObject. Mostly not null because baseObject is created when realm is initialized.
	 */
	fun getBaseObjectAsFlow(): Flow<BaseObject?> = realm.query(BaseObject::class).first().asFlow().map { it.obj }

	fun putChapter(chapterObject: ChapterObject, modifyTimestampAuto: Boolean = true) {
		realm.writeBlocking {
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
		realm.writeBlocking {
			val storedBaseObject = getBaseObject()
			storedBaseObject?.let {
				findLatest(it)?.let { latestBaseObject ->
					latestBaseObject.notebookIdOrderList = idOrderList.toRealmList()
				}
			}
		}
	}

	/**
	 * Get all chapters as a flow of list
	 * @author pushpull
	 * @since 2.0.0
	 */
	fun getAllChapterAsFlow(): Flow<List<ChapterObject>> = realm.query(ChapterObject::class).asFlow().map { it.list.map { it } }

	/**
	 * Get chapter from its id
	 * @author pushpull
	 * @since 2.0.0
	 * @param id [RealmUUID] of the chapter. If null, no exceptions will be thrown but result will also be null.
	 * @return ChapterObject with the given id. If no chapter with the given id exists, null will be returned.
	 */
	fun getChapterFromId(id: RealmUUID?): ChapterObject? = realm.query(ChapterObject::class, "id == $0 ", id).first().find()

	inline fun <reified T : TypedRealmObject> getAllObjectOfType(includeLocked: Boolean): List<T> = if (includeLocked) realm.query<T>().find().map { it } else realm.query<T>("isLocked == $0", false).find().map { it }

	inline fun <reified T : TypedRealmObject> getObjectFromId(id: RealmUUID?): T? = realm.query<T>("id == $0 ", id).first().find()

	inline fun <reified T : TypedRealmObject> getObjectFromId(idList: List<RealmUUID>, includeLocked: Boolean): List<T> = if (includeLocked) realm.query<T>("id IN $0 ", idList).find() else realm.query<T>("id IN $0 AND isLocked == $1", idList, false).find()

	inline fun <reified T : TypedRealmObject> getObjectFromIdAsFlow(id: RealmUUID?): Flow<T?> = realm.query<T>("id == $0 ", id).first().asFlow().extractObject()

	suspend inline fun <reified T : TypedRealmObject> setObjectFromId(id: RealmUUID?, crossinline write: T.() -> Unit) = realm.write {
		query(T::class, "id == $0 ", id).first().find()?.write()
	}


	inline fun <reified T : TypedRealmObject> setObjectFromIdSuspended(id: RealmUUID?, crossinline insert: MutableRealm.() -> Unit = {}, crossinline update: T.() -> Unit) = CoroutineScope(Dispatchers.Default).launch {
		realm.write {
			query(T::class, "id == $0 ", id).first().find()?.apply {
				update()
			} ?: insert()
		}
	}

	inline fun <reified T : TypedRealmObject> setMultiObjectFromIdSuspended(idList: Set<RealmUUID>, crossinline write: T.() -> Unit) = CoroutineScope(Dispatchers.Default).launch {
		idList.forEach { id ->
			realm.write { query(T::class, "id == $0 ", id).first().find()?.write() }
		}
	}

	inline fun <reified T : TypedRealmObject> getDeletedObjectOfType() = getBaseObject()?.deletedObjectMap?.filter { it.value?.objectType == T::class.simpleName }?.mapKeys {
		try {
			RealmUUID.from(it.key)
		} catch (e: Exception) {
			if (BuildConfig.DEBUG) e.printStackTrace()
			null
		}
	}?.filterNotNull() ?: mapOf()

	/**
	 * Get list of [ChapterObject] with [parentId] as flow. If [parentId] is null, Notebooks are flowed.
	 * @author pushpull
	 * @since 2.0.0
	 */
	fun getChapterWithParentIdAsFlow(parentId: RealmUUID?): Flow<List<ChapterObject>> = realm.query(ChapterObject::class, "parentId = $0", parentId)
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

	fun getNotebookAsFlow(): Flow<List<ChapterObject>> = realm.query(ChapterObject::class, "parentId = $0", null)
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

	private fun getNotebookOrderAsFlow(): Flow<List<RealmUUID>> = getBaseObjectAsFlow().map { it?.notebookIdOrderList ?: listOf() }

	/**
	 * @author pushpull
	 * @since 2.0.0
	 * @param parentChapterId Id of the parent chapter. Null if the chapter is a notebook.
	 * @return Pair of the chapter and the child chapters list.
	 */
	fun getChapterWithParentId(parentChapterId: RealmUUID?): RealmResults<ChapterObject> = realm.query(ChapterObject::class, "parentId == $0 ", parentChapterId).find()

	/**
	 * Get the path of the chapter within the tree.
	 * @author pushpull
	 * @since 2.0.0
	 * @param id RealmUUID of the current chapter. Null if the chapter is a notebook.
	 * @param includeEdge If true, the current chapter will be included in the path.
	 */
	fun getChapterPath(id: RealmUUID?, includeEdge: Boolean = false): List<ChapterObjectLite> = try {
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

	/**
	 * Saves a note in the database or updates if already present. No need to pass the parent chapter id as it will read from [noteObject].
	 *
	 * @author pushpull
	 * @since 2.2.0
	 */
	fun putNote(noteObject: NoteObject, modifyTimestampAuto: Boolean = true) = realm.writeBlocking {
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
				latestNoteObject.isFavourite = noteObject.isFavourite
				latestNoteObject.isLocked = noteObject.isLocked
				latestNoteObject.parentId = noteObject.parentId
			} ?: copyToRealm(noteObject)
		} ?: copyToRealm(noteObject)
	}

	fun putNoteInDefaultSuspended(noteObject: NoteObject, modifyTimestampAuto: Boolean = true) = CoroutineScope(Dispatchers.Default).launch {
		noteObject.parentId = getDefaultChapterId()
		putNote(noteObject, modifyTimestampAuto)
	}

	fun getNoteFromId(id: RealmUUID): NoteObject? = realm.query(NoteObject::class, "id == $0 ", id).first().find()

	fun getAllNoteAsFlow(): Flow<List<NoteObject>> = realm.query(NoteObject::class)
		.asFlow()
		.extractList()
		.filterLocked(isLockedGetter = NoteObject::isLocked)

	fun getAllNoteLiteAsFlow2(): Flow<List<NoteObjectLite>> = realm.query(NoteObject::class)
		.asFlow()
		.extractList()
		.toLite { toLite() }
		.filterLocked(isLockedGetter = NoteObjectLite::isLocked)

	@OptIn(ExperimentalCoroutinesApi::class)
	fun getDefaultNoteLiteMapAsFlow2(parentId: RealmUUID?): Flow<RealmObjectGroupList<NoteObjectLite>> = realm.query(NoteObject::class, "parentId == $0", parentId)
		.asFlow()
		.extractList()
		.toLite { toLite() }
		.filterLocked(isLockedGetter = NoteObjectLite::isLocked)
		.applyGroupOn(
			titleGetter = NoteObjectLite::title,
			timestampGetter = NoteObjectLite::userTimestamp,
			modifiedTimestampGetter = NoteObjectLite::modifiedTimestamp,
			customGetter = { it.userTimestamp.toString() }
		)
		.toGroupList()

	@OptIn(ExperimentalCoroutinesApi::class)
	private fun <T : BaseRealmObject> Flow<ResultsChange<T>>.extractList(): Flow<List<T>> = this.mapLatest { it.list.toList() }

	fun <T : BaseRealmObject> Flow<SingleQueryChange<T>>.extractObject(): Flow<T?> = this.map { it.obj }

	private fun <T : BaseRealmObject, R> Flow<List<T>>.toLite(converter: T.() -> R): Flow<List<R>> = this.map { it.map(converter) }

	/**
	 * Filters locked objects from list of objects. Uses [isUnlocked] internally
	 * @author pushpull
	 * @since 3.0.0
	 * @param isLockedGetter Getter of locked property
	 */
	private fun <T> Flow<List<T>>.filterLocked(isLockedGetter: KProperty1<T, Boolean>): Flow<List<T>> = this.combine(isUnlocked) { objectList, isUnlocked1 -> if (isUnlocked1) objectList else objectList.filter { !isLockedGetter.get(it) } }

	private fun <T> Flow<List<T>>.applyGroupOn(
		titleGetter: KProperty1<T, String?>,
		timestampGetter: KProperty1<T, Long>,
		modifiedTimestampGetter: KProperty1<T, Long>,
		customGetter: (T) -> String,
	): Flow<List<RealmObjectGroup<T>>> = this.combine(sortOnFlow) { objectList1, sortOn1 ->
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

	private fun <T> Flow<List<RealmObjectGroup<T>>>.toGroupList(): Flow<RealmObjectGroupList<T>> = this.map {
		RealmObjectGroupList(
			groupList = it,
			totalSize = it.fold(0) { acc, realmObjectGroup -> acc + realmObjectGroup.objectList.size }
		)
	}

	private fun <T> sortOnKeySelector(
		t: T,
		sortOn: SortOn?,
		titleGetter: KProperty1<T, String?>,
		timestampGetter: KProperty1<T, Long>,
		modifiedTimestampGetter: KProperty1<T, Long>,
		customGetter: (T) -> String,
	): String = when (sortOn) {
		SortOn.Title -> titleGetter(t)?.firstOrNull()?.lowercase() ?: "."
		SortOn.Timestamp -> timestampGetter(t).timeStampToPrettyDay()
		SortOn.Modified -> modifiedTimestampGetter(t).timeStampToPrettyDay()
		SortOn.Custom -> customGetter(t)
		else -> titleGetter(t)?.firstOrNull()?.lowercase() ?: "."
	}

	private fun <T> Flow<List<T>>.applySortOnBy(
		idGetter: KProperty1<T, RealmUUID>,
		titleGetter: KProperty1<T, String?>,
		timestampGetter: KProperty1<T, Long>,
		modifiedTimestampGetter: KProperty1<T, Long>,
		customOrderFlow: Flow<List<RealmUUID>>? = null
	): Flow<List<T>> = if (customOrderFlow == null) {
		combine(this, sortOnFlow, sortByFlow) { realmObjectList1, sortOn1, sortBy1 ->
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
		combine(this, sortOnFlow, customOrderFlow, sortByFlow) { realmObjectList1, sortOn1, customOrder1, sortBy1 ->
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

	/**
	 * Get all notes with [parentId] as flow.
	 */
	fun getNoteWithParentIdAsFlow(parentId: RealmUUID?): Flow<List<NoteObjectLite>> = realm.query(NoteObject::class, "parentId = $0", parentId)
		.asFlow()
		.extractList()
		.toLite { toLite() }
		.filterLocked(isLockedGetter = NoteObjectLite::isLocked)
		.applySortOnBy(
			idGetter = NoteObjectLite::id,
			titleGetter = NoteObjectLite::title,
			timestampGetter = NoteObjectLite::createdTimestamp,
			modifiedTimestampGetter = NoteObjectLite::modifiedTimestamp,
			customOrderFlow = getNotebookOrderAsFlow(),
		)

	fun getNoteWithParentId(parentId: RealmUUID): RealmResults<NoteObject> = realm.query(NoteObject::class, "parentId = $0", parentId).find()

	fun putBucket(bucketObject: BucketObject, modifyTimestampAuto: Boolean = true) = realm.writeBlocking {
		val storedBucketObject = getObjectFromId<BucketObject>(id = bucketObject.id)
		storedBucketObject?.let {
			findLatest(it)?.let { latestBucketItemObject ->
				latestBucketItemObject.createdTimestamp = bucketObject.createdTimestamp
				latestBucketItemObject.modifiedTimestamp = if (modifyTimestampAuto) Instant.now().toEpochMilli() else bucketObject.modifiedTimestamp
				latestBucketItemObject.title = bucketObject.title
				latestBucketItemObject.description = bucketObject.description
				latestBucketItemObject.bucketType = bucketObject.bucketType
				latestBucketItemObject.isFavourite = bucketObject.isFavourite
				latestBucketItemObject.isLocked = bucketObject.isLocked
			} ?: copyToRealm(bucketObject)
		} ?: copyToRealm(bucketObject)
	}

	fun putBucketSuspended(bucketObject: BucketObject, modifyTimestampAuto: Boolean = true) = CoroutineScope(Dispatchers.Default).launch { putBucket(bucketObject, modifyTimestampAuto) }

	fun reorderBucketList(idOrderList: List<RealmUUID>) = realm.writeBlocking {
		val storedBaseObject = getBaseObject()
		storedBaseObject?.let {
			findLatest(it)?.let { latestBaseObject ->
				latestBaseObject.bucketIdOrderList = idOrderList.toRealmList()
			}
		}
	}

	fun putBucketItem(bucketItemObject: BucketItemObject, modifyTimestampAuto: Boolean = true) = realm.writeBlocking {
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
			} ?: copyToRealm(bucketItemObject)
		} ?: copyToRealm(bucketItemObject)
	}

	fun putBucketItemSuspended(bucketItemObject: BucketItemObject, modifyTimestampAuto: Boolean = true) = CoroutineScope(Dispatchers.Default).launch { putBucketItem(bucketItemObject, modifyTimestampAuto) }

	fun reorderBucketItemList(parentId: RealmUUID, idOrderList: List<RealmUUID>) = setObjectFromIdSuspended<BucketObject>(id = parentId) {
		this.bucketItemOrderList = idOrderList.toRealmList()
	}

	fun getAllBucketAsFlow(): Flow<List<BucketObject>> = realm.query(BucketObject::class).asFlow().map { it.list }.combine(isUnlocked) { bucketList, isAuthenticated ->
		if (isAuthenticated) bucketList else bucketList.filter { !it.isLocked }
	}

	fun getAllBucketLiteAsFlow(): Flow<List<BucketObjectLite>> = realm.query(BucketObject::class)
		.asFlow()
		.extractList()
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

	private fun Flow<List<BucketObjectLite>>.mergeBucketSize(): Flow<List<BucketObjectLite>> = this.combine(getAllBucketSizeAsFlow()) { bucketList1, bucketSizeMap1 ->
		bucketList1.map { it.copy(bucketItemCount = bucketSizeMap1[it.id] ?: 0) }
	}

	/**
	 * Order of bucket list as stored in [BaseObject.bucketIdOrderList]
	 * @author pushpull
	 * @since 3.0.0
	 * @return Flow of list of [BucketObject.id] of [BucketObject]
	 */
	private fun getBucketOrderAsFlow(): Flow<List<RealmUUID>> = getBaseObjectAsFlow().map { it?.bucketIdOrderList ?: listOf() }

	private fun getBucketItemOrderAsFlow(parentId: RealmUUID): Flow<List<RealmUUID>> = getObjectFromIdAsFlow<BucketObject>(id = parentId).map { it?.bucketItemOrderList ?: listOf() }

	fun getAllBucketSizeAsFlow(): Flow<Map<RealmUUID?, Int>> = realm.query(BucketItemObject::class).asFlow().combine(isUnlocked) { bucketItemList1, isAuthenticated1 ->
		if (isAuthenticated1) bucketItemList1.list else bucketItemList1.list.filter { !it.isLocked }
	}.map { it.groupBy { it.parentId }.mapValues { it.value.size } }

	/**
	 * Returns flow of bucket item with provided parent id as flow.
	 * @author pushpull
	 * @since 2.2.0
	 * @param parentId RealmUUID of the parent bucket.
	 * @return Flow of RealmResults of BucketItemObject with provided parent id.
	 */
	fun getBucketItemListFromParentIdAsFlow(parentId: RealmUUID): Flow<List<BucketItemObject>> = realm
		.query(BucketItemObject::class, "parentId == $0 ", parentId)
		.asFlow()
		.extractList()
		.filterLocked(isLockedGetter = BucketItemObject::isLocked)
		.applySortOnBy(
			idGetter = BucketItemObject::id,
			titleGetter = BucketItemObject::title,
			timestampGetter = BucketItemObject::createdTimestamp,
			modifiedTimestampGetter = BucketItemObject::modifiedTimestamp,
			customOrderFlow = getBucketItemOrderAsFlow(parentId = parentId),
		)


	fun getBucketItemWithParentId(parentId: RealmUUID?): List<BucketItemObject> = realm.query(BucketItemObject::class, "parentId == $0 ", parentId).find().map { it }

	fun getBucketItemFromId(id: RealmUUID): BucketItemObject? = realm.query(BucketItemObject::class, "id == $0 ", id).first().find()

	fun putTag(tagObject: TagObject, modifyTimestampAuto: Boolean = true) = realm.writeBlocking {
		val storedTagObject = getTagFromId(tagObject.id)
		storedTagObject?.let {
			findLatest(it)?.let { latestTagObject ->
				latestTagObject.modifiedTimestamp = if (modifyTimestampAuto) Instant.now().toEpochMilli() else latestTagObject.modifiedTimestamp
				latestTagObject.tag = tagObject.tag
				latestTagObject.color = tagObject.color
			} ?: copyToRealm(tagObject)
		} ?: copyToRealm(tagObject)
	}

	fun getTagFromId(id: RealmUUID?): TagObject? = realm.query(TagObject::class, "id == $0", id).first().find()

	fun updateTagConnections(objectId: RealmUUID, tagListToAdd: List<RealmUUID>, tagListToRemove: List<RealmUUID>) = CoroutineScope(Dispatchers.Default).launch {
		realm.write {
			tagListToRemove.forEach {
				getTagFromId(it)?.let { tagObject -> findLatest(tagObject)?.objectIdList?.remove(objectId) }
			}
			tagListToAdd.forEach {
				getTagFromId(it)?.let { tagObject -> findLatest(tagObject)?.objectIdList?.add(objectId) }
			}
		}
	}

	fun deleteFromTags(objectId: RealmUUID) = CoroutineScope(Dispatchers.Default).launch {
		getAllTag().forEach { tagObject ->
			if (objectId in tagObject.objectIdList) setObjectFromId<TagObject>(id = tagObject.id) {
				this.objectIdList.remove(objectId)
			}
		}
	}

	/**
	 * Get all tags as a flow list and observe changes
	 * @author pushpull
	 * @since 2.0.0
	 */
	fun getAllTagAsFlow(): Flow<List<TagObject>> = realm.query(TagObject::class).asFlow().map { it.list }

	/**
	 * Get all tags as a list
	 * @author pushpull
	 * @since 2.0.0
	 */
	fun getAllTag(): List<TagObject> = getAllObjectOfType<TagObject>(includeLocked = true)

	fun deleteAttachment(attachmentList: Set<File>, keepHistory: Boolean = true) {
		if (keepHistory) attachmentList
			.map {
				DeletedAttachment()
					.apply {
						it.parentFile?.name?.let { it1 -> RealmUUID.Companion.from(it1) }?.let { this.parentId = it }
						this.fileName = it.name
					}
			}.let { deletedFileList ->
				getBaseObject()?.let { realm.writeBlocking { findLatest(it)?.deletedAttachmentSet?.addAll(deletedFileList) } }
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
	 * [NoteObject] will delete all associated attachments
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
	fun delete(id: RealmUUID, keepHistory: Boolean) {
		deleteFromTags(objectId = id)
		getObjectFromId<NoteObject>(id = id)?.let {
			deleteAttachment(attachmentRepository.getAttachmentFromNote(it.id), keepHistory)
			attachmentRepository.delete(it.id)
			realm.writeBlocking {
				if (keepHistory) updateDeleteHistory(id, NoteObject::class.simpleName)
				findLatest(it)?.let { delete(it) }
			}
		}
		getObjectFromId<ChapterObject>(id = id)?.let {
			delete(getChapterWithParentId(id).map { it.id }, keepHistory)
			delete(getNoteWithParentId(id).map { it.id }, keepHistory)
			realm.writeBlocking {
				if (keepHistory) updateDeleteHistory(id, ChapterObject::class.simpleName)
				findLatest(it)?.let { delete(it) }
			}
		}
		getObjectFromId<BucketItemObject>(id = id)?.let {
			realm.writeBlocking {
				if (keepHistory) updateDeleteHistory(id, BucketItemObject::class.simpleName)
				findLatest(it)?.let { delete(it) }
			}
		}
		getObjectFromId<BucketObject>(id = id)?.let {
			delete(getBucketItemWithParentId(id).map { it.id }, keepHistory)
			realm.writeBlocking {
				if (keepHistory) updateDeleteHistory(id, BucketObject::class.simpleName)
				findLatest(it)?.let { delete(it) }
			}
		}
		getObjectFromId<TagObject>(id = id)?.let {
			realm.writeBlocking {
				if (keepHistory) updateDeleteHistory(id, TagObject::class.simpleName)
				findLatest(it)?.let { delete(it) }
			}
		}
	}

	private fun MutableRealm.updateDeleteHistory(id: RealmUUID, objectType: String?) {
		query(BaseObject::class).first().find()?.deletedObjectMap?.put(
			key = id.toString(),
			value = DeletedObject().apply {
				deletedTimestamp = Instant.now().toEpochMilli()
				this.objectType = objectType
			}
		)
	}

	fun delete(idList: Collection<RealmUUID>, keepHistory: Boolean = true) = idList.forEach { delete(it, keepHistory) }

	fun deleteSuspended(id: RealmUUID, keepHistory: Boolean = true, callback: suspend () -> Unit = {}) = CoroutineScope(Dispatchers.Default).launch {
		delete(id, keepHistory)
		callback()
	}

	fun deleteSuspended(idList: Collection<RealmUUID>, keepHistory: Boolean = true, callback: suspend () -> Unit = {}) = CoroutineScope(Dispatchers.Default).launch {
		delete(idList, keepHistory)
		callback()
	}

	/**
	 * Clears everything from realm and reinitialize realm with default values. See [initializeRealmSuspended].
	 * @author pushpull
	 * @since 3.0.0
	 * @return true if successful, false if not
	 */
	suspend fun resetRealm(): Boolean {
		return try {
			attachmentRepository.deleteAll()
			realm.write {
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

	companion object {

		const val SCHEMA_VERSION = 5L

		val REALM_BUILDER_SCHEMA = setOf(
			BaseObject::class,
			ChapterObject::class,
			NoteObject::class,
			BucketObject::class,
			BucketItemObject::class,
			TagObject::class,
			DeletedObject::class,
			DeletedAttachment::class,
		)

		sealed class RepositoryStatus {
			data object Init : RepositoryStatus()
			data object Loading : RepositoryStatus()
			data object Locked : RepositoryStatus()
			data class Success(val repository: Repository) : RepositoryStatus()
			data object Error : RepositoryStatus()
		}

		sealed class RealmSnapshotCopyStatus {
			data object Success : RealmSnapshotCopyStatus()
			data object Error : RealmSnapshotCopyStatus()
			data class InProgress(val processed: Int, val total: Int) : RealmSnapshotCopyStatus()
		}
	}
}
