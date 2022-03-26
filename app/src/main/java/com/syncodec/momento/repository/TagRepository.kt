package com.syncodec.momento.repository

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.syncodec.momento.Momento
import com.syncodec.momento.database.UserDatabase
import com.syncodec.momento.database.tag.TagDbEntry
import com.syncodec.momento.database.tag.TagDbTableDao
import com.syncodec.momento.database.tag.TagKeyDbEntry
import com.syncodec.momento.database.tag.TagKeyDbTableDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Singleton

@Singleton
class TagRepository(val momento: Momento) {

	private val objectMapper: ObjectMapper = ObjectMapper().registerModule(KotlinModule())

	private val tagDbTableDao: TagDbTableDao = UserDatabase.getInstance(momento).tagDbTableDao
	private val tagKeyDbTableDao: TagKeyDbTableDao = UserDatabase.getInstance(momento).tagKeyDbTableDao

	val tags: Flow<List<TagDbEntry>> = tagDbTableDao.getAllAsFlow()

	fun getTagsForEntryAsFlow(key: String) = tagKeyDbTableDao.getTagsForEntryAsFlow(key = key)
	fun getEntriesForTagAsFlow(tag: String) = tagKeyDbTableDao.getEntriesForTagAsFlow(tag = tag)

	fun putTag(tag: String) = tagDbTableDao.insert(TagDbEntry(tag = tag))

	suspend fun connectTag(key: String, tagList: List<String>) {
		CoroutineScope(Dispatchers.IO).launch {
			tagList.forEach {
				TagKeyDbEntry(
					tag =  it,
					key = key
				).apply { tagKeyDbTableDao.insert(this) }
			}
		}
	}

	companion object {
		private var INSTANCE: TagRepository? = null

		fun getInstance(momento: Momento): TagRepository {
			synchronized(lock = this) {
				var instance = INSTANCE
				if (instance == null) {
					instance = TagRepository(momento = momento)
					INSTANCE = instance
				}
				return instance
			}
		}
	}
}
