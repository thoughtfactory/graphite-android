package com.syncodec.graphite.di.repository

import android.content.Context
import android.util.Log
import com.syncodec.graphite.BuildConfig
import com.syncodec.graphite.di.model.local.BaseObject
import com.syncodec.graphite.di.model.local.ChapterObject
import com.syncodec.graphite.utils.alice.AliceRequest2
import com.syncodec.graphite.utils.alice.getSecretData2
import com.syncodec.graphite.utils.alice.putSecretData
import com.syncodec.graphite.utils.dataStore.DataStoreInstance
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.security.SecureRandom


class LockableRepo(val context: Context, private val dataStoreInstance: DataStoreInstance) {

	val repositoryStatusFlow: MutableStateFlow<Repository.Companion.RepositoryStatus> = MutableStateFlow(Repository.Companion.RepositoryStatus.Init)
	private val _isUnlocked: MutableStateFlow<Boolean> = MutableStateFlow(false)
	val isUnlocked: StateFlow<Boolean> = _isUnlocked

	init {
		CoroutineScope(Dispatchers.Default).launch {
			repositoryStatusFlow.collectLatest { repositoryStatus ->
				if (repositoryStatus is Repository.Companion.RepositoryStatus.Success) {
					repositoryStatus.repository.isUnlocked.collectLatest {
						this@LockableRepo._isUnlocked.tryEmit(it)
					}
				}
			}
		}
	}

	fun decryptRepository(retryNum: Int = 1): Repository? {
		try {
			if (retryNum < 0) return null
			else when (val realmKeyAliceRequest = context.getSecretData2(key = "realmKey")) {
				is AliceRequest2.Success -> {
					val key = realmKeyAliceRequest.data
					if (BuildConfig.DEBUG) {
						key.joinToString(separator = "") { eachByte -> "%02x".format(eachByte) }.let {
							Log.d("npr71", "Realm Key: $it")
						}
					}

					val realmConfiguration = RealmConfiguration.Builder(Repository.REALM_BUILDER_SCHEMA)
						.encryptionKey(key)
						.initialData {
							ChapterObject().also { chapterObject ->
								chapterObject.title = "Diary"
								chapterObject.description = "Default diary. Every notes will be saved in this notebook by default"

								copyToRealm(chapterObject)

								BaseObject().also { baseObject ->
									baseObject.defaultChapterId = chapterObject.id

									copyToRealm(baseObject)
								}
							}
						}
						.schemaVersion(Repository.SCHEMA_VERSION)
						.migration(RealmMigrator())
						.build()

					val realm = Realm.open(realmConfiguration)
					val repo = Repository(realm = realm, context = context, dataStoreInstance = dataStoreInstance)
					repositoryStatusFlow.tryEmit(Repository.Companion.RepositoryStatus.Success(repo))
					return repo
				}
				is AliceRequest2.KeystoreUninitialized -> return runBlocking { delay(1000); return@runBlocking decryptRepository(retryNum = retryNum - 1) }
				is AliceRequest2.KeyNotFound -> {
					val realmKey = ByteArray(Realm.ENCRYPTION_KEY_LENGTH)
					SecureRandom.getInstanceStrong().nextBytes(realmKey)
					context.putSecretData("realmKey", realmKey)
					return decryptRepository(retryNum = retryNum - 1)
				}

				is AliceRequest2.UnknownError -> {
					repositoryStatusFlow.tryEmit(Repository.Companion.RepositoryStatus.Error)
					return null
				}
			}
		} catch (e: Exception) {
			if (BuildConfig.DEBUG) e.printStackTrace()
			repositoryStatusFlow.tryEmit(Repository.Companion.RepositoryStatus.Error)
			return null
		}
	}

	fun silentlyDecryptRepository(retryNum: Int = 1): Repository? {
		repositoryStatusFlow.value.let {
			if ( it is Repository.Companion.RepositoryStatus.Success) return it.repository
		}
		try {
			if (retryNum < 0) return null
			else when (val realmKeyAliceRequest = context.getSecretData2(key = "realmKey")) {
				is AliceRequest2.Success -> {
					val key = realmKeyAliceRequest.data
					if (BuildConfig.DEBUG) {
						key.joinToString(separator = "") { eachByte -> "%02x".format(eachByte) }.let {
							Log.d("npr71", "Realm Key: $it")
						}
					}

					val realmConfiguration = RealmConfiguration.Builder(Repository.REALM_BUILDER_SCHEMA)
						.encryptionKey(key)
						.initialData {
							ChapterObject().also { chapterObject ->
								chapterObject.title = "Diary"
								chapterObject.description = "Default diary. Every notes will be saved in this notebook by default"

								copyToRealm(chapterObject)

								BaseObject().also { baseObject ->
									baseObject.defaultChapterId = chapterObject.id

									copyToRealm(baseObject)
								}
							}
						}
						.schemaVersion(Repository.SCHEMA_VERSION)
						.migration(RealmMigrator())
						.build()

					val realm = Realm.open(realmConfiguration)
					return Repository(realm = realm, context = context, dataStoreInstance = dataStoreInstance)
				}
				is AliceRequest2.KeystoreUninitialized -> return runBlocking { delay(1000); return@runBlocking decryptRepository(retryNum = retryNum - 1) }
				is AliceRequest2.KeyNotFound -> {
					val realmKey = ByteArray(Realm.ENCRYPTION_KEY_LENGTH)
					SecureRandom.getInstanceStrong().nextBytes(realmKey)
					context.putSecretData("realmKey", realmKey)
					return decryptRepository(retryNum = retryNum - 1)
				}

				is AliceRequest2.UnknownError -> return null
			}
		} catch (e: Exception) {
			if (BuildConfig.DEBUG) e.printStackTrace()
			return null
		}
	}

	fun lockRepo() {
		repositoryStatusFlow.value.let {
			if (it is Repository.Companion.RepositoryStatus.Success) it.repository.lockRepo()
		}
	}

	fun unlockRepo() {
		repositoryStatusFlow.value.let {
			if (it is Repository.Companion.RepositoryStatus.Success) it.repository.unlockRepo()
		}
	}
}