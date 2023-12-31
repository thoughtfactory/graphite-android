package com.syncodec.graphite.di.repository.group

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import com.syncodec.graphite.utils.SortOrder
import io.realm.kotlin.types.RealmUUID
import kotlin.reflect.KProperty1


data class RealmObjectGroup<T>(
	val title: String,
	val objectList: List<T>,
) {
	inline fun <R : Comparable<R>> sortedBy(sortOrder: SortOrder?, crossinline selector: (T) -> R?): RealmObjectGroup<T> {
		return RealmObjectGroup(
			title = title,
			objectList = when (sortOrder) {
				SortOrder.Ascending -> objectList.sortedWith(compareBy(selector))
				SortOrder.Descending -> objectList.sortedWith(compareByDescending(selector))
				else -> objectList
			}
		)
	}

	override fun hashCode(): Int {

		var result = title.hashCode()
		result = 31 * result + objectList.hashCode()
		return result
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false

		other as RealmObjectGroup<*>

		if (title != other.title) return false
		if (objectList != other.objectList) return false

		return true
	}
}

data class RealmObjectGroupList<T>(
	val groupList: List<RealmObjectGroup<T>> = listOf(),
	val totalSize: Int = 0,
) {
	fun flatten(): List<T> = groupList.flatMap { it.objectList }

	fun forEachObject(action: (T) -> Unit) = groupList.forEach{it.objectList.forEach{action(it)}}

	fun filterObject(predicate: (T) -> Boolean): List<T> = groupList.flatMap { it.objectList.filter(predicate) }

	fun isEmpty(): Boolean = groupList.all { it.objectList.isEmpty() }

	inline fun <R : Comparable<R>> sortedBy(sortOrder: SortOrder?, crossinline selector: (T) -> R?): RealmObjectGroupList<T> {
		return RealmObjectGroupList(
			groupList = when (sortOrder) {
				SortOrder.Ascending -> groupList.sortedBy { it.title }
				SortOrder.Descending -> groupList.sortedByDescending { it.title }
				else -> groupList
			}.map { it.sortedBy(sortOrder = sortOrder, selector = selector) },
			totalSize = totalSize
		)
	}

	override fun hashCode(): Int {
		var result = groupList.hashCode()
		result = 31 * result + totalSize
		return result
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false

		other as RealmObjectGroupList<*>

		if (groupList != other.groupList) return false
		if (totalSize != other.totalSize) return false

		return true
	}
}

@Composable
fun <T> isAll(
	isSelecting: Boolean,
	selectedIdList: Set<RealmUUID>,
	realmObjectGroupList: RealmObjectGroupList<T>?,
	idGetter: KProperty1<T, RealmUUID>,
	propGetter: KProperty1<T, Boolean>
): State<Boolean> {
	return remember(isSelecting, selectedIdList, realmObjectGroupList?.groupList) {
		derivedStateOf {
			when {
				!isSelecting -> false
				selectedIdList.isEmpty() -> false
				else -> realmObjectGroupList?.flatten()?.filter { idGetter(it) in selectedIdList }?.all { propGetter(it) } ?: false
			}
		}
	}
}

@Composable
fun <T> isAll(
	isSelecting: Boolean,
	selectedIdList: Set<RealmUUID>,
	objectList: List<T>?,
	idGetter: KProperty1<T, RealmUUID>,
	propGetter: KProperty1<T, Boolean>
): State<Boolean> {
	return remember(isSelecting, selectedIdList, objectList) {
		derivedStateOf {
			when {
				!isSelecting -> false
				selectedIdList.isEmpty() -> false
				else -> objectList?.filter { idGetter(it) in selectedIdList }?.all { propGetter(it) } ?: false
			}
		}
	}
}
