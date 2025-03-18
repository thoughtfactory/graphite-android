package com.syncodec.graphite.repository

import androidx.test.platform.app.InstrumentationRegistry
import com.syncodec.graphite.di.modelObjectBox.BucketBox
import com.syncodec.graphite.di.modelObjectBox.BucketItemBox
import com.syncodec.graphite.di.secureRepository.BoxRepository
import com.syncodec.graphite.utils.alice2.Alice2
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.time.Duration.Companion.minutes
import kotlin.time.measureTime


class BoxRepositoryTest {

    val dataCount = 10000

    @Test
    fun putEncryptedTimeTest() {
        runTest(timeout = 2.minutes) {
            val putTimeTest_1000 = measureTime {
                repeat(dataCount) {
                    val bucketBox = BucketBox()
                    boxRepository.putBucketBoxBlocking(bucketBox)
                }
            }
            val bucketBoxSize = boxRepository.countBucketBox()

            println("size : putEncryptedTimeTest : $bucketBoxSize")
            println("time : putEncryptedTimeTest : $putTimeTest_1000")
        }
    }

//    @Test
//    fun putStandardTimeTest() {
//        runTest(timeout = 2.minutes) {
//
//            val putTimeTest_1000 = measureTime {
//                val bucketBox = BucketBox()
//                boxRepository.putBucketBox(bucketBox)
//                repeat(dataCount) {
//                    val bucketItemBox = BucketItemBox.randomTodo
//                    boxRepository.putBucketItemBoxBlocking(bucketItemBox = bucketItemBox, parent = bucketBox)
//                }
//            }
//
//            val bucketItemBoxSize = boxRepository.countBucketItemBox()
//            println("size : putStandardTimeTest : $bucketItemBoxSize")
//            println("time : putStandardTimeTest : $putTimeTest_1000")
//        }
//    }

    @Test
    fun getEncryptedTimeTest() {
        runTest(timeout = 2.minutes) {
            val getTimeTest_1000 = measureTime {
                boxRepository.getAllBucketBox().let { println("size : ${it.size}") }
                boxRepository.getAllBucketBox().let { println("size : ${it.size}") }
                boxRepository.getAllBucketBox().let { println("size : ${it.size}") }
                boxRepository.getAllBucketBox().let { println("size : ${it.size}") }
                boxRepository.getAllBucketBox().let { println("size : ${it.size}") }
            }

            println("getEncryptedTimeTest : time : $getTimeTest_1000")
        }
    }

//    @Test
//    fun getStandardTimeTest() {
//        runTest(timeout = 2.minutes) {
//            val getTimeTest_1000 = measureTime {
//                boxRepository.getAllBucketItemBox()
//                boxRepository.getAllBucketItemBox()
//                boxRepository.getAllBucketItemBox()
//                boxRepository.getAllBucketItemBox()
//                boxRepository.getAllBucketItemBox()
//            }
//
//            println("getStandardTimeTest : time : $getTimeTest_1000")
//        }
//    }

    companion object {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val alice2 = Alice2(context)
        val boxRepository = BoxRepository(context, alice2).apply {
            println("New")
        }
    }
}

