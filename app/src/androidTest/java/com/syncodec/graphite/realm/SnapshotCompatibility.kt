package com.syncodec.graphite.realm

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.syncodec.graphite.di.model.NoteObject
import com.syncodec.graphite.di.repository.RepositoryState
import com.syncodec.graphite.di.repository.koinRepository.KoinRepository
import com.syncodec.graphite.utils.alice.Alice
import com.syncodec.graphite.utils.alice.putSecretData
import io.realm.kotlin.Realm
import org.apache.commons.compress.archivers.sevenz.SevenZFile
import org.apache.commons.compress.utils.SeekableInMemoryByteChannel
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.TestMethodOrder
import org.junit.runner.RunWith
import java.io.File
import java.io.IOException
import java.nio.channels.Channels
import java.nio.channels.FileChannel
import java.nio.channels.ReadableByteChannel
import java.nio.channels.SeekableByteChannel
import java.nio.file.Paths


@RunWith(AndroidJUnit4::class)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class SnapshotCompatibility {

	private lateinit var context : Context
	private lateinit var appContext : Context
	private lateinit var koinRepository : KoinRepository

	@Before
	fun setUp() {
		context = InstrumentationRegistry.getInstrumentation().context
		appContext = InstrumentationRegistry.getInstrumentation().targetContext
		val realmKey = ByteArray(Realm.ENCRYPTION_KEY_LENGTH)
		realmKey.fill(0)
		appContext.putSecretData("realmKey", realmKey)

		koinRepository = KoinRepository()
		assert(koinRepository.repositoryState.value == RepositoryState.Init)
		koinRepository.initRepository(context = appContext)
	}

	@Test
	@Order(1)
	fun useAppContext() {
		Assert.assertEquals("com.syncodec.graphite", appContext.packageName)
	}

	@Test
	@Order(2)
	fun test_Repository() {
		assert(koinRepository.repositoryState.value != RepositoryState.Error)
	}

	@Test
	@Order(3)
	fun test_DefaultData() {
		assert(koinRepository.getDefaultChapterId() != null)
		println(koinRepository.getDefaultChapterId())
	}

	@Test
	@Order(4)
	fun test_InsertNewNote() {
		NoteObject.getRandomInstance().apply {
			koinRepository.putNote(this)
			koinRepository.getNoteFromId(id = this.id)?.let {
				Assert.assertEquals(it, this)
			}
		}
	}

	@Test
	@Order(5)
	fun test() {
		val assetManager = context.assets
		try {
			val inputStream = assetManager.open("graphite_snapshot_1678313459020.7z")

			koinRepository.snapshot.restore(inputStream) {
				println("Restore: $it")
				koinRepository.getAllNote().size.let {
					println("Note count: $it")
				}
			}


		} catch (e : IOException) {
			e.printStackTrace()
			assert(false)
		} catch (e : Exception) {
			e.printStackTrace()
			assert(false)
		}
	}
}
