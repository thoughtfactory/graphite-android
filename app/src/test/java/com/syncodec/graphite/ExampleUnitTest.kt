package com.syncodec.graphite

import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
	@Test
	fun addition_isCorrect() {
		assertEquals(4, 2 + 2)
	}

	data class TestData(val realmUUID : RealmUUID?)

	@Test
	fun mutableStateFlow_null_test() {
		val flow = MutableStateFlow<RealmUUID?>(null)
		runBlocking {
			this.launch {
				flow.collect {
					println(it)
				}
			}
			for (i in 0 .. 10) {
				flow.tryEmit(RealmUUID.random())
				delay(100)
				flow.tryEmit(null as RealmUUID?)
				delay(100)
			}
		}
	}
}
