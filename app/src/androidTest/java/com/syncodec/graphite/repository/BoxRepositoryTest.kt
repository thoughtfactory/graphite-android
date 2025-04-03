package com.syncodec.graphite.repository

import androidx.test.platform.app.InstrumentationRegistry
import com.syncodec.graphite.di.modelObjectBox.BucketBoxDecrypted
import com.syncodec.graphite.di.modelObjectBox.BucketItemBoxDecrypted
import com.syncodec.graphite.di.secureRepository.BoxRepository
import com.syncodec.graphite.utils.alice2.Alice2
import kotlinx.coroutines.test.runTest
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.jupiter.api.Order
import org.junit.runners.MethodSorters
import kotlin.time.Duration.Companion.minutes
import kotlin.time.measureTime
import kotlin.uuid.ExperimentalUuidApi


@OptIn(ExperimentalUuidApi::class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class BoxRepositoryTest {

    val dataCount = 10000

    @Test
    @Order(1)
    fun t1_putEncryptedTimeTest_test1() {
        runTest(timeout = 2.minutes) {
            val putTimeTest_1000 = measureTime {
                repeat(dataCount) {
                    val bucketBox = BucketBoxDecrypted()
                    boxRepository.putBucketBoxBlocking(bucketBox)
                }
            }
            val bucketBoxSize = boxRepository.countBucketBox()

            println("size : putEncryptedTimeTest : $bucketBoxSize")
            println("time : putEncryptedTimeTest : $putTimeTest_1000")
        }
    }

    @Test
    fun t2_putStandardTimeTest_test2() {
        runTest(timeout = 2.minutes) {

            val putTimeTest_1000 = measureTime {
                val bucketBoxEncrypted = BucketBoxDecrypted()
                boxRepository.putBucketBox(bucketBoxEncrypted)
                repeat(dataCount) {
                    val bucketItemBox = BucketItemBoxDecrypted.randomTodo
                    boxRepository.putBucketItemBoxBlocking(bucketItemBox = bucketItemBox)
                }
            }

            val bucketItemBoxSize = boxRepository.countBucketItemBox()
            println("size : putStandardTimeTest : $bucketItemBoxSize")
            println("time : putStandardTimeTest : $putTimeTest_1000")
        }
    }

    @Test
    fun t3_getEncryptedTimeTest_test3() {
        runTest(timeout = 2.minutes) {
            val getTimeTest_1000 = measureTime {
                boxRepository.getAllBucketBox().map { it.enc }.let { println("size : ${it.size}") }
                boxRepository.getAllBucketBox().map { it.enc }.let { println("size : ${it.size}") }
                boxRepository.getAllBucketBox().map { it.enc }.let { println("size : ${it.size}") }
                boxRepository.getAllBucketBox().map { it.enc }.let { println("size : ${it.size}") }
                boxRepository.getAllBucketBox().map { it.enc }.let { println("size : ${it.size}") }
            }

            println("getEncryptedTimeTest : time : $getTimeTest_1000")
        }
    }

    @Test
    fun t4_getDecryptedTimeTest_test4() {
        runTest(timeout = 2.minutes) {
            val getTimeTest_1000 = measureTime {
                boxRepository.getAllBucketBox().map { it.decryptBlocking(alice2) }.let { println("size : ${it.size}") }
                boxRepository.getAllBucketBox().map { it.decryptBlocking(alice2) }.let { println("size : ${it.size}") }
                boxRepository.getAllBucketBox().map { it.decryptBlocking(alice2) }.let { println("size : ${it.size}") }
                boxRepository.getAllBucketBox().map { it.decryptBlocking(alice2) }.let { println("size : ${it.size}") }
                boxRepository.getAllBucketBox().map { it.decryptBlocking(alice2) }.let { println("size : ${it.size}") }
            }

            println("getDecryptedTimeTest : time : $getTimeTest_1000")
        }
    }

    companion object {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val alice2 = Alice2(context)
        val boxRepository = BoxRepository(context, alice2,).apply {
            println("New")
        }
    }
}

