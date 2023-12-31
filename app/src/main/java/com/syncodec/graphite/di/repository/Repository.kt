package com.syncodec.graphite.di.repository

import android.content.Context
import com.syncodec.graphite.BuildConfig
import com.syncodec.graphite.di.model.local.AttachmentIdentity
import com.syncodec.graphite.di.model.local.BaseObject
import com.syncodec.graphite.di.model.local.BucketItemObject
import com.syncodec.graphite.di.model.local.BucketObject
import com.syncodec.graphite.di.model.local.BucketObjectLite
import com.syncodec.graphite.di.model.local.ChapterObject
import com.syncodec.graphite.di.model.local.ChapterObjectLite
import com.syncodec.graphite.di.model.local.NoteObject
import com.syncodec.graphite.di.model.local.NoteObjectLite
import com.syncodec.graphite.di.model.local.ObjectIdentity
import com.syncodec.graphite.di.model.local.TagObject
import com.syncodec.graphite.di.repository.group.RealmObjectGroup
import com.syncodec.graphite.di.repository.group.RealmObjectGroupList
import com.syncodec.graphite.di.snapshot.SnapshotInator
import com.syncodec.graphite.utils.SortOn
import com.syncodec.graphite.utils.SortOrder
import com.syncodec.graphite.utils.dataStore.DataStoreInstance
import com.syncodec.graphite.utils.filterNotNull
import com.syncodec.graphite.utils.timeStampToPrettyDay
import io.realm.kotlin.MutableRealm
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import io.realm.kotlin.ext.toRealmList
import io.realm.kotlin.notifications.ResultsChange
import io.realm.kotlin.notifications.SingleQueryChange
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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.launch
import java.io.File
import java.time.Instant
import kotlin.reflect.KProperty1


class Repository(val realm: Realm, context: Context, dataStoreInstance: DataStoreInstance) {

	val attachmentRepository = AttachmentRepository(context = context)
	val snapshotInator = SnapshotInator(context = context, repository = this)

	private val sortOrderFlow: Flow<SortOrder?> = dataStoreInstance.getSortOrder
	private val sortOnFlow: Flow<SortOn?> = dataStoreInstance.getSortOn

	private val _isUnlocked: MutableStateFlow<Boolean> = MutableStateFlow(false)
	val isUnlocked: StateFlow<Boolean> = _isUnlocked

	fun lockRepo() = this._isUnlocked.tryEmit(false)

	fun unlockRepo() = this._isUnlocked.tryEmit(true)

	suspend fun putDefaultChapterId(id: RealmUUID) {
		realm.write {
			val baseObject = this.query(BaseObject::class).first().find()
			baseObject?.let { findLatest(it)?.defaultChapterId = id }
				?: BaseObject().also { baseObject1 ->
					baseObject1.defaultChapterId = id
					copyToRealm(baseObject1)
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

	/**
	 * Get all chapters as a flow of list
	 * @author pushpull
	 * @since 2.0.0
	 */
	fun getAllChapterAsFlow(): Flow<List<ChapterObject>> = realm.query(ChapterObject::class).asFlow().map { it.list.map { it } }

	inline fun <reified T : TypedRealmObject> getAllObjectOfType(includeLocked: Boolean): List<T> =
		if (includeLocked) realm.query<T>().find().map { it }
		else realm.query<T>("isLocked == $0", false).find().map { it }

	inline fun <reified T : TypedRealmObject> getAllObjectOfTypeAsFlow(includeLocked: Boolean): Flow<List<T>> =
		if (includeLocked) realm.query<T>().asFlow().extractList()
		else realm.query<T>("isLocked == $0", false).asFlow().extractList()

	inline fun <reified T : TypedRealmObject> getObjectFromId(id: RealmUUID?): T? = realm.query<T>("id == $0 ", id).first().find()

	inline fun <reified T : TypedRealmObject> getObjectFromId(idList: List<RealmUUID>, includeLocked: Boolean): List<T> =
		if (includeLocked) realm.query<T>("id IN $0 ", idList).find().map { it }
		else realm.query<T>("id IN $0 AND isLocked == $1", idList, false).find().map { it }

	inline fun <reified T : TypedRealmObject> getObjectFromIdAsFlow(id: RealmUUID?): Flow<T?> = realm.query<T>("id == $0 ", id).first().asFlow().extractObject()

	inline fun <reified T : TypedRealmObject> getObjectWithParentId(parentId: RealmUUID?, includeLocked: Boolean) =
		if (includeLocked) realm.query<T>("parentId == $0 ", parentId).find().map { it }
		else realm.query<T>("parentId == $0 AND isLocked == $1", parentId, false).find().map { it }

	inline fun <reified T : TypedRealmObject> getObjectWithParentIdAsFlow(parentId: RealmUUID?) = realm.query<T>("parentId == $0 ", parentId).asFlow().extractList()

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

	inline fun <reified T : TypedRealmObject> getDeletedObjectOfType() = getBaseObject()
		?.deletedObjectSet
		?.filter { it.objectType == T::class.simpleName }
		?.associate { it.id to it.toObjectMetadata() }
		?.filterNotNull() ?: mapOf()


	@OptIn(ExperimentalCoroutinesApi::class)
	fun <T : BaseRealmObject> Flow<ResultsChange<T>>.extractList(): Flow<List<T>> = this.mapLatest { it.list.toList() }

	fun <T : BaseRealmObject> Flow<SingleQueryChange<T>>.extractObject(): Flow<T?> = this.map { it.obj }

	private fun <T : BaseRealmObject, R> Flow<List<T>>.toLite(converter: T.() -> R): Flow<List<R>> = this.map { it.map(converter) }

	/**
	 * Filters locked objects from list of objects. Uses [isUnlocked] internally
	 * @author pushpull
	 * @since 3.0.0
	 * @param isLockedGetter Getter of locked property
	 */
	private fun <T> Flow<List<T>>.filterLocked(isLockedGetter: KProperty1<T, Boolean>): Flow<List<T>> = this.combine(isUnlocked) { objectList, isUnlocked1 -> if (isUnlocked1) objectList else objectList.filter { !isLockedGetter.get(it) } }

	/**
	 * Groups the list of objects according to [SortOn]
	 * @param titleGetter Getter of title property
	 * @param timestampGetter Getter of timestamp property
	 * @param modifiedTimestampGetter Getter of modifiedTimestamp property
	 * @return Flow of list of [RealmObjectGroup] of [T]
	 * @see [applyGroupSortOnBy] for group list
	 * @author pushpull
	 * @since 3.0.0
	 */
	private fun <T> Flow<List<T>>.applyGroupOn(
		titleGetter: KProperty1<T, String?>,
		timestampGetter: KProperty1<T, Long>,
		modifiedTimestampGetter: KProperty1<T, Long>,
	): Flow<List<RealmObjectGroup<T>>> = this.combine(sortOnFlow) { objectList1, sortOn1 ->
		objectList1.groupBy {
			when (sortOn1) {
				SortOn.Title -> titleGetter(it)?.firstOrNull()?.lowercase() ?: "."
				SortOn.Timestamp -> timestampGetter(it).timeStampToPrettyDay()
				SortOn.Modified -> modifiedTimestampGetter(it).timeStampToPrettyDay()
				else -> timestampGetter(it).timeStampToPrettyDay()
			}
		}.map { RealmObjectGroup(title = it.key, objectList = it.value) }
	}

	/**
	 * Performs sort on the list of objects. Uses [sortOnFlow] and [sortOrderFlow] internally.
	 * @param idGetter Getter of id property
	 * @param titleGetter Getter of title property
	 * @param timestampGetter Getter of timestamp property
	 * @param modifiedTimestampGetter Getter of modifiedTimestamp property
	 * @param customOrderFlow Flow of custom order list. This is used when [SortOn.Custom] is selected. This is used to sort the list in the order of user preference, typically used for notebook and bucket item list.
	 * @return Flow of sorted list of objects
	 * @see [applyGroupSortOnBy] for group list
	 * @author pushpull
	 * @since 3.0.0
	 */
	private fun <T> Flow<List<T>>.applySortOnBy(
		idGetter: KProperty1<T, RealmUUID>,
		titleGetter: KProperty1<T, String?>,
		timestampGetter: KProperty1<T, Long>,
		modifiedTimestampGetter: KProperty1<T, Long>,
		customOrderFlow: Flow<List<RealmUUID>> = MutableStateFlow(listOf())
	): Flow<List<T>> = combine(this, sortOnFlow, sortOrderFlow, customOrderFlow) { realmObjectList1, sortOn1, sortOrder1, customOrder1 ->
		when (sortOn1) {
			SortOn.Title -> if (sortOrder1 == SortOrder.Ascending) realmObjectList1.sortedBy { titleGetter(it)?.firstOrNull()?.lowercase() ?: "." }
			else realmObjectList1.sortedByDescending { titleGetter(it)?.firstOrNull()?.lowercase() ?: "." }

			SortOn.Timestamp -> if (sortOrder1 == SortOrder.Ascending) realmObjectList1.sortedBy { timestampGetter(it) }
			else realmObjectList1.sortedByDescending { timestampGetter(it) }

			SortOn.Modified -> if (sortOrder1 == SortOrder.Ascending) realmObjectList1.sortedBy { modifiedTimestampGetter(it) }
			else realmObjectList1.sortedByDescending { modifiedTimestampGetter(it) }

			SortOn.Custom -> realmObjectList1.sortedBy { customOrder1.indexOf(idGetter(it)) }
			else -> if (sortOrder1 == SortOrder.Ascending) realmObjectList1.sortedBy { titleGetter(it)?.firstOrNull()?.lowercase() ?: "." }
			else realmObjectList1.sortedByDescending { titleGetter(it)?.firstOrNull()?.lowercase() ?: "." }
		}
	}

	/**
	 * Performs sort on the [RealmObjectGroupList]. Sorts [RealmObjectGroupList.groupList] first and [RealmObjectGroup.objectList] next.
	 * @param idGetter Getter of id property
	 * @param titleGetter Getter of title property
	 * @param timestampGetter Getter of timestamp property
	 * @param modifiedTimestampGetter Getter of modifiedTimestamp property
	 * @param customOrderFlow Flow of custom order list. This is used when [SortOn.Custom] is selected. This is used to sort the list in the order of user preference, typically used for notebook and bucket item list.
	 * @return Flow of sorted list of objects
	 * @see [applySortOnBy] for list
	 * @see [applyGroupOn] for group list
	 * @author pushpull
	 * @since 3.0.0
	 */
	private fun <T> Flow<RealmObjectGroupList<T>>.applyGroupSortOnBy(
		idGetter: KProperty1<T, RealmUUID>,
		titleGetter: KProperty1<T, String?>,
		timestampGetter: KProperty1<T, Long>,
		modifiedTimestampGetter: KProperty1<T, Long>,
		customOrderFlow: Flow<List<RealmUUID>> = MutableStateFlow(listOf())
	): Flow<RealmObjectGroupList<T>> = combine(this, sortOnFlow, sortOrderFlow, customOrderFlow) { realmObjectList1, sortOn1, sortOrder1, customOrder1 ->
		when (sortOn1) {
			SortOn.Title -> realmObjectList1.sortedBy(sortOrder = sortOrder1) { titleGetter(it)?.firstOrNull()?.lowercase() ?: "." }
			SortOn.Timestamp -> realmObjectList1.sortedBy(sortOrder = sortOrder1) { timestampGetter(it) }
			SortOn.Modified -> realmObjectList1.sortedBy(sortOrder = sortOrder1) { modifiedTimestampGetter(it) }
			SortOn.Custom -> realmObjectList1.sortedBy(sortOrder = sortOrder1) { customOrder1.indexOf(idGetter(it)) }
			else -> realmObjectList1.sortedBy(sortOrder = sortOrder1) { timestampGetter(it) }
		}
	}

	private fun <T> Flow<List<RealmObjectGroup<T>>>.toGroupList(): Flow<RealmObjectGroupList<T>> = this.map {
		RealmObjectGroupList(
			groupList = it,
			totalSize = it.fold(0) { acc, realmObjectGroup -> acc + realmObjectGroup.objectList.size }
		)
	}

	fun putChapter(chapterObject: ChapterObject, modifyTimestampAuto: Boolean = true) {
		realm.writeBlocking {
			val storedChapterObject = getObjectFromId<ChapterObject>(id = chapterObject.id)
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

	fun putChapterSuspended(chapterObject: ChapterObject, modifyTimestampAuto: Boolean = true) = CoroutineScope(Dispatchers.Default).launch { putChapter(chapterObject = chapterObject, modifyTimestampAuto = modifyTimestampAuto) }

	/**
	 * Saves a note in the database or updates if already present. No need to pass the parent chapter id as it will read from [noteObject].
	 *
	 * @author pushpull
	 * @since 2.2.0
	 */
	fun putNote(noteObject: NoteObject, modifyTimestampAuto: Boolean = true) = realm.writeBlocking {
		val storedNoteObject = getObjectFromId<NoteObject>(id = noteObject.id)
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

	fun putBucketItem(bucketItemObject: BucketItemObject, modifyTimestampAuto: Boolean = true) = realm.writeBlocking {
		val storedBucketItemObject = getObjectFromId<BucketItemObject>(id = bucketItemObject.id)
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
				latestBucketItemObject.bucketItemDataJson = bucketItemObject.bucketItemDataJson
			} ?: copyToRealm(bucketItemObject)
		} ?: copyToRealm(bucketItemObject)
	}

	fun putBucketItemSuspended(bucketItemObject: BucketItemObject, modifyTimestampAuto: Boolean = true) =
		CoroutineScope(Dispatchers.Default).launch { putBucketItem(bucketItemObject = bucketItemObject, modifyTimestampAuto = modifyTimestampAuto) }

	fun putTag(tagObject: TagObject, modifyTimestampAuto: Boolean = true) = realm.writeBlocking {
		val storedTagObject = getObjectFromId<TagObject>(id = tagObject.id)
		storedTagObject?.let {
			findLatest(it)?.let { latestTagObject ->
				latestTagObject.modifiedTimestamp = if (modifyTimestampAuto) Instant.now().toEpochMilli() else latestTagObject.modifiedTimestamp
				latestTagObject.tag = tagObject.tag
				latestTagObject.color = tagObject.color
			} ?: copyToRealm(tagObject)
		} ?: copyToRealm(tagObject)
	}

	fun getAllNoteAsFlow(): Flow<List<NoteObject>> = realm.query(NoteObject::class)
		.asFlow()
		.extractList()
		.filterLocked(isLockedGetter = NoteObject::isLocked)

	fun getAllNoteLiteAsFlow2(): Flow<List<NoteObjectLite>> = realm.query(NoteObject::class)
		.asFlow()
		.extractList()
		.toLite(NoteObject::toLite)
		.filterLocked(isLockedGetter = NoteObjectLite::isLocked)

	@OptIn(ExperimentalCoroutinesApi::class)
	fun getDefaultNoteLiteMapAsFlow3(): Flow<RealmObjectGroupList<NoteObjectLite>> = getDefaultChapterIdAsFlow().transformLatest { defaultChapterId1 ->
		getNoteLiteWithParentIdAsFlow(parentId = defaultChapterId1, sort = true).collectLatest { emit(it) }
	}

	fun getNotebookAsFlow(): Flow<List<ChapterObject>> = getObjectWithParentIdAsFlow<ChapterObject>(parentId = null)
		.filterLocked(isLockedGetter = ChapterObject::isLocked)
		.applySortOnBy(
			idGetter = ChapterObject::id,
			titleGetter = ChapterObject::title,
			timestampGetter = ChapterObject::createdTimestamp,
			modifiedTimestampGetter = ChapterObject::modifiedTimestamp,
			customOrderFlow = getNotebookOrderAsFlow(),
		)

	/**
	 * Order of notebook list as stored in [BaseObject.notebookIdOrderList]. This is used to sort the notebook list in the order of user preference.
	 * @return Flow of list of [ChapterObject.id] of [ChapterObject]
	 * @author pushpull
	 * @since 3.0.0
	 */
	private fun getNotebookOrderAsFlow(): Flow<List<RealmUUID>> = getBaseObjectAsFlow().map { it?.notebookIdOrderList ?: listOf() }

	/**
	 * Order of bucket list as stored in [BaseObject.bucketIdOrderList]
	 * @return Flow of list of [BucketObject.id] of [BucketObject]
	 * @author pushpull
	 * @since 3.0.0
	 */
	private fun getBucketOrderAsFlow(): Flow<List<RealmUUID>> = getBaseObjectAsFlow().map { it?.bucketIdOrderList ?: listOf() }

	/**
	 * Order of bucket item list as stored in [BucketObject.bucketItemOrderList]. This is used to sort the bucket item list in the order of user preference.
	 * @param parentId [RealmUUID] of parent bucket.
	 * @return Flow of list of [BucketItemObject.id] of [BucketItemObject]
	 * @author pushpull
	 * @since 3.0.0
	 */
	private fun getBucketItemOrderAsFlow(parentId: RealmUUID): Flow<List<RealmUUID>> = getObjectFromIdAsFlow<BucketObject>(id = parentId).map { it?.bucketItemOrderList ?: listOf() }

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
	 * Get the path of the chapter within the tree.
	 * @param id RealmUUID of the current chapter. Null if the chapter is a notebook.
	 * @param includeEdge If true, the current chapter will be included in the path.
	 * @author pushpull
	 * @since 2.0.0
	 */
	fun getChapterPath(id: RealmUUID?, includeEdge: Boolean = false): List<ChapterObjectLite> = try {
		val chapterObject = getObjectFromId<ChapterObject>(id = id)
		val chapterObjectList = mutableListOf<ChapterObjectLite>()
		if (includeEdge) chapterObject?.toLite()?.let { chapterObjectList.add(it) }
		var parentChapterObject = chapterObject?.parentId?.let { it1 -> getObjectFromId<ChapterObject>(it1) }
		while (parentChapterObject != null) {
			chapterObjectList.add(parentChapterObject.toLite())
			parentChapterObject = parentChapterObject.parentId?.let { it1 -> getObjectFromId<ChapterObject>(it1) }
		}
		chapterObjectList
	} catch (e: Exception) {
		listOf()
	}

	/**
	 * Get all notes with [parentId] as flow.
	 * @param parentId [RealmUUID] of parent chapter. If null, all notes will be returned.
	 * @param sort If true, notes will be sorted according to [sortOrderFlow] and [sortOnFlow] else notes will be returned in the order they are stored in the database.
	 * @return Flow of list of [NoteObjectLite]
	 * @author pushpull
	 * @since 2.0.0
	 */
	fun getNoteLiteWithParentIdAsFlow(parentId: RealmUUID?, sort: Boolean) = getObjectWithParentIdAsFlow<NoteObject>(parentId = parentId)
		.toLite(NoteObject::toLite)
		.filterLocked(isLockedGetter = NoteObjectLite::isLocked)
		.applyGroupOn(
			titleGetter = NoteObjectLite::title,
			timestampGetter = NoteObjectLite::userTimestamp,
			modifiedTimestampGetter = NoteObjectLite::modifiedTimestamp,
		)
		.toGroupList()
		.let {
			if (sort) it.applyGroupSortOnBy(
				idGetter = NoteObjectLite::id,
				titleGetter = NoteObjectLite::title,
				timestampGetter = NoteObjectLite::userTimestamp,
				modifiedTimestampGetter = NoteObjectLite::modifiedTimestamp,
			) else it
		}

	fun getBucketItemWithParentIdAsFlow(parentId: RealmUUID) = getObjectWithParentIdAsFlow<BucketItemObject>(parentId = parentId)
		.filterLocked(isLockedGetter = BucketItemObject::isLocked)
		.applySortOnBy(
			idGetter = BucketItemObject::id,
			titleGetter = BucketItemObject::title,
			timestampGetter = BucketItemObject::createdTimestamp,
			modifiedTimestampGetter = BucketItemObject::modifiedTimestamp,
			customOrderFlow = getBucketItemOrderAsFlow(parentId = parentId),
		)

	fun getAllBucketLiteAsFlow(): Flow<List<BucketObjectLite>> = realm.query(BucketObject::class)
		.asFlow()
		.extractList()
		.toLite(BucketObject::toLite)
		.filterLocked(isLockedGetter = BucketObjectLite::isLocked)
		.mergeBucketSize()
		.applySortOnBy(
			idGetter = BucketObjectLite::id,
			titleGetter = BucketObjectLite::title,
			timestampGetter = BucketObjectLite::createdTimestamp,
			modifiedTimestampGetter = BucketObjectLite::modifiedTimestamp,
			customOrderFlow = getBucketOrderAsFlow(),
		)

	fun reorderBucketList(idOrderList: List<RealmUUID>) = realm.writeBlocking {
		val storedBaseObject = getBaseObject()
		storedBaseObject?.let {
			findLatest(it)?.let { latestBaseObject ->
				latestBaseObject.bucketIdOrderList = idOrderList.toRealmList()
			}
		}
	}

	fun reorderBucketItemList(parentId: RealmUUID, idOrderList: List<RealmUUID>) = setObjectFromIdSuspended<BucketObject>(id = parentId) {
		this.bucketItemOrderList = idOrderList.toRealmList()
	}

	fun getAllBucketAsFlow(): Flow<List<BucketObject>> = realm.query(BucketObject::class).asFlow().map { it.list }.combine(isUnlocked) { bucketList, isAuthenticated ->
		if (isAuthenticated) bucketList else bucketList.filter { !it.isLocked }
	}

	private fun Flow<List<BucketObjectLite>>.mergeBucketSize(): Flow<List<BucketObjectLite>> = this.combine(getAllBucketSizeAsFlow()) { bucketList1, bucketSizeMap1 ->
		bucketList1.map { it.copy(bucketItemCount = bucketSizeMap1[it.id] ?: 0) }
	}

	fun getAllBucketSizeAsFlow(): Flow<Map<RealmUUID?, Int>> = realm.query(BucketItemObject::class).asFlow().combine(isUnlocked) { bucketItemList1, isAuthenticated1 ->
		if (isAuthenticated1) bucketItemList1.list else bucketItemList1.list.filter { !it.isLocked }
	}.map { it.groupBy { it.parentId }.mapValues { it.value.size } }

	fun updateTagConnections(objectId: RealmUUID, tagListToAdd: List<RealmUUID>, tagListToRemove: List<RealmUUID>) = CoroutineScope(Dispatchers.Default).launch {
		realm.write {
			tagListToRemove.forEach {
				getObjectFromId<TagObject>(id = it)?.let { tagObject -> findLatest(tagObject)?.objectIdList?.remove(objectId) }
			}
			tagListToAdd.forEach {
				getObjectFromId<TagObject>(id = it)?.let { tagObject -> findLatest(tagObject)?.objectIdList?.add(objectId) }
			}
		}
	}

	fun deleteFromTags(objectId: RealmUUID) = CoroutineScope(Dispatchers.Default).launch {
		getAllObjectOfType<TagObject>(includeLocked = true).forEach { tagObject ->
			if (objectId in tagObject.objectIdList) setObjectFromId<TagObject>(id = tagObject.id) {
				this.objectIdList.remove(objectId)
			}
		}
	}

	/**
	 * Deletes attachments and adds them to [BaseObject.deletedAttachmentSet] if [keepHistory] is true
	 */
	fun deleteAttachment(attachmentList: Collection<File>, keepHistory: Boolean = true) {
		if (keepHistory) {
			val deletedFileList = attachmentList
				.mapNotNull {
					val parentId = it.parentFile?.name?.let { it1 -> RealmUUID.from(it1) } ?: return@mapNotNull null
					AttachmentIdentity(parentId = parentId, fileName = it.name)
				}
			getBaseObject()?.let { realm.writeBlocking { findLatest(it)?.deletedAttachmentSet?.addAll(deletedFileList) } }
		}
		attachmentRepository.delete(attachmentList)
	}

	fun deleteAttachment(attachmentIdentity: AttachmentIdentity, keepHistory: Boolean) {
		if (keepHistory) {
			getBaseObject()?.let { realm.writeBlocking { findLatest(it)?.deletedAttachmentSet?.add(attachmentIdentity) } }
		}
		attachmentRepository.delete(attachmentIdentity)
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
			delete(getObjectWithParentId<ChapterObject>(parentId = id, includeLocked = true).map { it.id }, keepHistory)
			delete(getObjectWithParentId<NoteObject>(parentId = id, includeLocked = true).map { it.id }, keepHistory)
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
			delete(getObjectWithParentId<BucketItemObject>(parentId = id, includeLocked = true).map { it.id }, keepHistory)
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

	private fun MutableRealm.updateDeleteHistory(id: RealmUUID, objectType: String?) = query(BaseObject::class)
		.first()
		.find()
		?.deletedObjectSet
		?.add(ObjectIdentity(id = id, deletedTimestamp = Instant.now().toEpochMilli(), objectType = objectType))

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
	 * Clears everything from realm and reinitialize realm with default values.
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
			ObjectIdentity::class,
			AttachmentIdentity::class,
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
