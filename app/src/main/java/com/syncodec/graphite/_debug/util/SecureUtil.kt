package com.syncodec.graphite._debug.util

import com.syncodec.graphite.alice.Alice
import java.io.File


class Main {
	companion object {
		@JvmStatic
		fun main(args: Array<String>) {
			val password = "m7X*fN@Rh#WNcs2Q69NyYQrQkHb@U^%*c59R7K4o2#0d92##HBojTyZ4a^5@qB&0"
			val file = File("/home/pushpull/.cache/Google/AndroidStudio2021.2/device-explorer/Pixel_4_API_Sv2 [emulator-5554]/data/data/com.syncodec.graphite/data/note/f493c93c-3d81-45f5-8339-233171301be0")
			file.readText().apply {
				Alice.decrypt(this, password).apply {
					println(this)
				}
			}

		}
	}
}
