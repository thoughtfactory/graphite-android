package com.syncodec.momento.miscellaneous

import java.util.ArrayList
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1

class CollectionUtils {
	companion object {
		inline fun <reified T, Y> MutableList<T>.listOfField(property: KMutableProperty1<T, Y?>): MutableList<Y> {
			val yy = ArrayList<Y>()
			this.forEach { t: T -> yy.add(property.get(t) as Y) }
			return yy
		}

		inline fun <reified T, Y> MutableList<T>.listOfField(property: KProperty1<T, Y?>): MutableList<Y> {
			val yy = ArrayList<Y>()
			this.forEach { t: T -> yy.add(property.get(t) as Y) }
			return yy
		}

		@JvmName("listOfFieldT")
		inline fun <reified T, Y> List<T>.listOfField(property: KMutableProperty1<T, Y?>): List<Y> {
			val yy = ArrayList<Y>()
			this.forEach { t: T -> yy.add(property.get(t) as Y) }
			return yy
		}

		@JvmName("listOfFieldT")
		inline fun <reified T, Y> List<T>.listOfField(property: KProperty1<T, Y?>): List<Y> {
			val yy = ArrayList<Y>()
			this.forEach { t: T -> yy.add(property.get(t) as Y) }
			return yy
		}
	}
}
