package com.syncodec.graphite

import com.syncodec.graphite.utils.alice.Alice
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File

class TipTapUnitTest {
	@Test
	fun addition_isCorrect() {
		assertEquals(4, 2 + 2)
	}

	@Test
	fun generateEncryptedTipTap() {

		val passcode = "d5Y3f8*hN8%c%Q3%Jb9vU^8R4MV@z^9*"

		File("./src/main/assets/orbit/index.html").let {
			val html = it.readText()
			Alice.encrypt(html, passcode)?.let { File("./src/main/assets/orbit/orbital").writeText(it) }

			File("./src/main/assets/orbit/orbital").readText().let { Alice.decrypt(it, passcode).let { assertEquals(html, it) } }
		}
	}
}
