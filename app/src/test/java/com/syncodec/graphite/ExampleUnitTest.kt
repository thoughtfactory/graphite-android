package com.syncodec.graphite

import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {

	data class WrappedRealmUUId(val realmUUID : RealmUUID?)

	@Test
	fun realmUUId_null_flow_test() {
		val flow = MutableStateFlow<RealmUUID?>(null)
		runBlocking {
			this.launch {
				flow.collect { println(it) }
			}
			for (i in 0 .. 10) {
				flow.tryEmit(RealmUUID.random())
				delay(100)
				flow.tryEmit(null as RealmUUID?)
				delay(100)
			}
		}
	}

	@Test
	fun wrapped_realmUUId_null_flow_test() {
		val flow = MutableStateFlow<WrappedRealmUUId?>(null)
		runBlocking {
			this.launch {
				flow.collect { println(it) }
			}
			for (i in 0 .. 10) {
				flow.tryEmit(WrappedRealmUUId(RealmUUID.random()))
				delay(100)
				flow.tryEmit(null as WrappedRealmUUId?)
				delay(100)
			}
		}
	}

	@Test
	fun nullFlowTest() {
		var a : String? = "a"
		var b : String? = "b"
		var c : String? = null

		a?.let {
			b?.let {
				c?.let {
					println("a: $a, b: $b, c: $c")
				}
				println()
			} ?: run {
				println("b is null")
			}
		} ?: run {
			println("a is null")
		}
	}
}
