package com.syncodec.graphite

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.syncodec.graphite.utils.alice.Alice

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import java.io.File

@RunWith(AndroidJUnit4::class)
class TipTapEditorTest {
	@Test
	fun useAppContext() {

		val passcode = "2%xY@Z5kYGu*iX!#N3m%03fC%4!070#D"

		val appContext = InstrumentationRegistry.getInstrumentation().targetContext
		appContext.assets.open("orbit/index.html").let {
			val html = String(it.readAllBytes())
			Alice.encrypt(html, passcode).let {
				if (it != null) File(appContext.filesDir, "orbital").writeText(it)
			}

			File(appContext.filesDir, "orbital").readText().let {
				Alice.decrypt(it, passcode).let {
					assertEquals(html, it)
				}
			}
		}

		assertEquals("com.syncodec.graphite", appContext.packageName)
	}
}
