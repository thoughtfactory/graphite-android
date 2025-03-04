package com.syncodec.graphite.network

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.syncodec.graphite.di.network.openLibrary.OpenLibraryApi2
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class OpenLibraryApiTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun searchForBookTest() =         runTest {


        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val openLibraryApi2 = OpenLibraryApi2(context = appContext)

        openLibraryApi2.searchForBook(query = "All the bright places") {
            println(it)
        }
        delay(5000)
    }

}
