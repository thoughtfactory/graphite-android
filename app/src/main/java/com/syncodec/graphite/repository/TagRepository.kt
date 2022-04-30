package com.syncodec.graphite.repository

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.syncodec.graphite.Graphite
import com.syncodec.graphite.database.UserDatabase
import com.syncodec.graphite.database.tag.TagDbEntry
import com.syncodec.graphite.database.tag.TagDbTableDao
import com.syncodec.graphite.database.tag.TagKeyDbEntry
import com.syncodec.graphite.database.tag.TagKeyDbTableDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class TagRepository(val graphite: Graphite) {

	private val objectMapper: ObjectMapper = ObjectMapper().registerModule(KotlinModule())

	private val tagDbTableDao: TagDbTableDao = UserDatabase.getInstance(graphite).tagDbTableDao
	private val tagKeyDbTableDao: TagKeyDbTableDao = UserDatabase.getInstance(graphite).tagKeyDbTableDao

	val tagList: Flow<List<TagDbEntry>> = tagDbTableDao.getAllAsFlow()
	val tagKeyList: Flow<List<TagKeyDbEntry>> = tagKeyDbTableDao.getAllAsFlow()

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

		fun getInstance(graphite: Graphite): TagRepository {
			synchronized(lock = this) {
				var instance = INSTANCE
				if (instance == null) {
					instance = TagRepository(graphite = graphite)
					INSTANCE = instance
				}
				return instance
			}
		}
	}
}
