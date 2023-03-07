package com.syncodec.graphite

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.syncodec.graphite.utils.alice.Alice
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import java.io.File

@RunWith(AndroidJUnit4::class)
class TipTapEditorTest {
	@Test
	fun generateEncryptedTipTap() {

		val passcode = "d5Y3f8*hN8%c%Q3%Jb9vU^8R4MV@z^9*"

		val appContext = InstrumentationRegistry.getInstrumentation().targetContext
		appContext.assets.open("orbit/tmp/index.html").let {
			val html = String(it.readAllBytes())
			Alice.encrypt(html, passcode).let {
				if (it != null) File(appContext.filesDir, "orbital").writeText(it)
			}

			File(appContext.filesDir, "orbital").readText().let {
				Alice.decrypt(it, passcode).let { assertEquals(html, it) }
			}
		}

		assertEquals("com.syncodec.graphite", appContext.packageName)
	}
}
