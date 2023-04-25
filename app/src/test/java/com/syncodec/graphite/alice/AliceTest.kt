package com.syncodec.graphite.alice

import com.syncodec.graphite.utils.alice.Alice
import org.junit.Test

class AliceTest {

	@Test
	fun decrypt() {
		val encryptedValue = "hqfm0ZTBPUNPvmLvLYZDskiMrm+1hXsSVwyf9pGSDSo]lZX16dOR14KI0wvyM6eYnQ]VRF8Qim5OXYZpE0xsj2kV9q8zYJmGd6YfbzF2wtUIdaaV52Qo+CS3DoLZY8/ygWj"
		val password = "lt3(3x4R7M^107!&4E74Z%*o8cp2i7y@"
		Alice.decrypt(encryptedValue, password)?.let {
			println(it)
		}
	}
}
