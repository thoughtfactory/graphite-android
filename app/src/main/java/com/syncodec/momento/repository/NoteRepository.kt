package com.syncodec.momento.repository

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import com.syncodec.momento.Momento
import com.syncodec.momento.database.UserDatabase
import com.syncodec.momento.database.chapter.ChapterDbEntry
import com.syncodec.momento.database.chapter.ChapterTableDao
import com.syncodec.momento.database.note.NoteDbEntry
import com.syncodec.momento.database.note.NoteTableDao
import com.syncodec.momento.database.notebook.NotebookDbEntry
import com.syncodec.momento.database.notebook.NotebookTableDao
import com.syncodec.momento.miscellaneous.generatePrimaryKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NoteRepository(val momento: Momento) {
	private val attachmentRepository = AttachmentRepository.getInstance(momento = momento)

	private var noteTableDao: NoteTableDao = UserDatabase.getInstance(momento).noteTableDao
	private var notebookTableDao: NotebookTableDao =
		UserDatabase.getInstance(momento).notebookTableDao
	private var chapterTableDao: ChapterTableDao = UserDatabase.getInstance(momento).chapterTableDao

	//	val noteDbEntryListFlow: Flow<List<NoteDbEntry>> = noteTableDao.getAllAsFlow()
//	val noteTimelineListFlow = noteTableDao.getAllForTimelineAsFlow()
	val notebookListFlow: Flow<List<NotebookDbEntry>> = notebookTableDao.getAllAsFlow()
	val notebookMap: SnapshotStateMap<String, Pair<NotebookDbEntry, Int>> = mutableStateMapOf()

	init {
		CoroutineScope(Dispatchers.IO).launch {
			notebookTableDao.getAllAsFlow().collect {
				it.forEach { notebookDbEntry ->
					CoroutineScope(Dispatchers.IO).launch {
						noteTableDao.countNotebookSize(notebookDbEntry.key).collectLatest {
							notebookMap[notebookDbEntry.key] = Pair(notebookDbEntry, it)
						}
					}
				}
			}
		}
	}

	suspend fun insert(noteDbEntry: NoteDbEntry) =
		withContext(Dispatchers.IO) { noteTableDao.insert(noteDbEntry) }

	suspend fun insert(chapterDbEntry: ChapterDbEntry) =
		withContext(Dispatchers.IO) { chapterTableDao.insert(chapterDbEntry) }

	suspend fun insert(notebookDbEntry: NotebookDbEntry) =
		withContext(Dispatchers.IO) { notebookTableDao.insert(notebookDbEntry) }

	suspend fun getNote(key: String): NoteDbEntry? =
		withContext(Dispatchers.IO) { noteTableDao.get(key = key) }

	suspend fun getChapter(key: String): ChapterDbEntry? =
		withContext(Dispatchers.IO) { chapterTableDao.get(key = key) }

	fun getNotebookAsFlow(key: String) = notebookTableDao.getAsFlow(key = key)

	fun openNotebookAsFlow(notebookKey: String): Flow<List<String>> =
		noteTableDao.getAllKeyFromNotebookAsFlow(notebookKey = notebookKey)

	suspend fun openNotebook(notebookKey: String): List<String> =
		noteTableDao.getAllKeyFromNotebook(notebookKey = notebookKey)

	fun deleteNote(keyList: List<String>) {
		noteTableDao.delete(keyList = keyList)
	}

	fun deleteChapter(keyList: List<String>) = chapterTableDao.delete(keyList = keyList)

	suspend fun deleteNotebook(key: String) =
		withContext(Dispatchers.IO) { notebookTableDao.delete(key) }

	suspend fun getAllKey(): List<String> = TODO()

	fun getNoteAsFlow(notebookKey: String): Flow<List<NoteDbEntry>> =
		noteTableDao.getFromNotebookAsFlow(notebookKey = notebookKey)

	fun getChapterAsFlow(notebookKey: String): Flow<List<ChapterDbEntry>> =
		chapterTableDao.getFromNotebookAsFlow(notebookKey = notebookKey)

	suspend fun putNote(noteDbEntry: NoteDbEntry) =
		withContext(Dispatchers.IO) { insert(noteDbEntry) }

	suspend fun putNotebook(notebook: NotebookDbEntry): String {
		val primaryKey = generatePrimaryKey()
		withContext(Dispatchers.IO) { insert(notebook) }

		return primaryKey
	}

	suspend fun putChapter(
		title: String,
		description: String?,
		notebookKey: String,
		currentRoute: MutableList<String>
	) {
		withContext(Dispatchers.IO) {
			val primaryKey = generatePrimaryKey()
			val currentTimestamp = System.currentTimeMillis()

			ChapterDbEntry(
				key = primaryKey,
				notebookKey = notebookKey,
				chapterPath = currentRoute
			).apply {
				this.createdTimestamp = currentTimestamp
				this.modifiedTimestamp = currentTimestamp
				this.title = title
				this.description = description
			}.apply { insert(this) }
		}
	}

	companion object {
		private var INSTANCE: NoteRepository? = null

		fun getInstance(momento: Momento): NoteRepository {
			synchronized(lock = this) {
				var instance = INSTANCE
				if (instance == null) {
					instance = NoteRepository(momento = momento)
					INSTANCE = instance
				}
				return instance
			}
		}
	}
}
