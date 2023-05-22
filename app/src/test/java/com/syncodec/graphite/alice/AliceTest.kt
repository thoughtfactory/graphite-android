package com.syncodec.graphite.alice

import com.syncodec.graphite.utils.alice.Alice
import org.junit.Test

class AliceTest {

	@Test
	fun decrypt() {
		val encryptedValue = "QDL0fiV7PAPMbRA/SZEXTA5DLtsAXWQWNvi+lPsgezs]W8Z91BwlcImwThoRijPtcA]Vs0IUVUTRG3Mwx5CqrzV98gd0Y7/DN9uK7kT3D8HhCvhx6QiPYj/26aUcVh8hXCiMp/7rkK5ccJdC1uAHhz04EkN8q4sTZOi3zEn1w3iAQY"
		val password = "lt3(3x4R7M^107!&4E74Z%*o8cp2i7y@"
		Alice.decrypt(encryptedValue, password)?.let {
			println(it)
		}
	}
}
