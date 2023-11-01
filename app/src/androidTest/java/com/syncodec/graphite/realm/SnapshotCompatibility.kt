package com.syncodec.graphite.realm

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.syncodec.graphite.di.model.local.NoteObject
import com.syncodec.graphite.di.repository.Repository
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.TestMethodOrder
import org.junit.runner.RunWith
import java.io.IOException


@RunWith(AndroidJUnit4::class)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class SnapshotCompatibility {

	private lateinit var context : Context
	private lateinit var appContext : Context
	private lateinit var repository : Repository

	@Before
	fun setUp() {
//		context = InstrumentationRegistry.getInstrumentation().context
//		appContext = InstrumentationRegistry.getInstrumentation().targetContext
//		val realmKey = ByteArray(Realm.ENCRYPTION_KEY_LENGTH)
//		realmKey.fill(0)
//		appContext.putSecretData("realmKey", realmKey)
//
//		val realmConfiguration = RealmConfiguration
//			.Builder(
//				setOf(
//					BaseObject::class,
//					ChapterObject::class,
//					NoteObject::class,
//					BucketObject::class,
//					BucketItemObject::class,
//					TagObject::class,
//					DeletedObject::class,
//					DeletedAttachment::class,
//				)
//			)
//			.encryptionKey(key)
//			.initialData {
//				ChapterObject().also { chapterObject ->
//					chapterObject.title = "Diary"
//					chapterObject.description = "Default diary. Every notes will be saved in this notebook by default"
//
//					copyToRealm(chapterObject)
//
//					BaseObject().also { baseObject ->
//						baseObject.defaultChapterId = chapterObject.id
//
//						copyToRealm(baseObject)
//					}
//				}
//			}
//			.schemaVersion(Repository.SCHEMA_VERSION)
//			.migration(RealmMigrator())
//			.build()
//
//		repository = Repository(realm = Realm.open(realmConfiguration))
////		assert(repository.repositoryState.value == Repository.Companion.RepositoryState.Init)
////		repository.initRepository(context = appContext)
	}

	@Test
	@Order(1)
	fun useAppContext() {
		Assert.assertEquals("com.syncodec.graphite", appContext.packageName)
	}

	@Test
	@Order(2)
	fun test_Repository() {
//		assert(repository.repositoryState.value != Repository.Companion.RepositoryState.Error)
	}

	@Test
	@Order(3)
	fun test_DefaultData() {
		assert(repository.getDefaultChapterId() != null)
		println(repository.getDefaultChapterId())
	}

	@Test
	@Order(4)
	fun test_InsertNewNote() {
		NoteObject.getRandomInstance().apply {
			repository.putNote(this)
			repository.getNoteFromId(id = this.id)?.let {
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

//			repository.snapshot.restore(inputStream) {
//				println("Restore: $it")
//				repository.getAllNote().size.let {
//					println("Note count: $it")
//				}
//			}


		} catch (e : IOException) {
			e.printStackTrace()
			assert(false)
		} catch (e : Exception) {
			e.printStackTrace()
			assert(false)
		}
	}
}
