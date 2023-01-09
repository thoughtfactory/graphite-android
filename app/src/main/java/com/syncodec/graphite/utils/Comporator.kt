package com.syncodec.graphite.utils

import io.realm.kotlin.types.RealmUUID
import kotlin.reflect.KMutableProperty1


class  RealmUUIDReorderComparator<T>(private val itemList : List<RealmUUID>, private val kMutableProperty1 : KMutableProperty1<T, RealmUUID>) : Comparator<T> {
	override fun compare(o1 : T, o2 : T) : Int {
		val index1 = itemList.indexOf(kMutableProperty1.get(o1))
		val index2 = itemList.indexOf(kMutableProperty1.get(o2))
		if (index1 == - 1 || index2 == - 1) return 0
		return index1.compareTo(index2)
	}
}
