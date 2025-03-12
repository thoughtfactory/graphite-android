package com.syncodec.graphite.alice2

import androidx.test.platform.app.InstrumentationRegistry
import com.syncodec.graphite.utils.alice.Alice
import com.syncodec.graphite.utils.alice2.Alice2
import org.junit.Test


class AliceTest {

    val alice2 = Alice2(InstrumentationRegistry.getInstrumentation().targetContext)

    @Test
    fun decrypt() {

        val str1 = "string_1"

        val str1enc = alice2.encrypt(str1)
        println(str1enc?.decodeToString())
        str1enc ?: return

        val str1dec = alice2.decrypt(str1enc)
        println(str1dec?.toString())

    }
}

