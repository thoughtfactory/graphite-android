package com.syncodec.graphite.repository

import com.syncodec.graphite.Graphite
import com.syncodec.graphite.database.UserDatabase
import com.syncodec.graphite.database.chapter.ChapterDbEntry
import com.syncodec.graphite.database.chapter.ChapterTableDao
import com.syncodec.graphite.database.note.NoteDbEntry
import com.syncodec.graphite.database.note.NoteTableDao
import com.syncodec.graphite.database.notebook.NotebookDbEntry
import com.syncodec.graphite.database.notebook.NotebookTableDao
import com.syncodec.graphite.miscellaneous.generatePrimaryKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONObject

class NoteRepository(val graphite: Graphite) {
	private val attachmentRepository = AttachmentRepository.getInstance(graphite = graphite)

	private var noteTableDao: NoteTableDao = UserDatabase.getInstance(graphite).noteTableDao
	private var notebookTableDao: NotebookTableDao =
		UserDatabase.getInstance(graphite).notebookTableDao
	private var chapterTableDao: ChapterTableDao =
		UserDatabase.getInstance(graphite).chapterTableDao

	//	val noteDbEntryListFlow: Flow<List<NoteDbEntry>> = noteTableDao.getAllAsFlow()
//	val noteTimelineListFlow = noteTableDao.getAllForTimelineAsFlow()
	val notebookListFlow: Flow<List<NotebookDbEntry>> = notebookTableDao.getAllAsFlow()

	suspend fun insert(noteDbEntry: NoteDbEntry) =
		withContext(Dispatchers.IO) { noteTableDao.insert(noteDbEntry) }

	suspend fun insert(chapterDbEntry: ChapterDbEntry) =
		withContext(Dispatchers.IO) { chapterTableDao.insert(chapterDbEntry) }

	suspend fun insert(notebookDbEntry: NotebookDbEntry) =
		withContext(Dispatchers.IO) { notebookTableDao.insert(notebookDbEntry) }

	suspend fun getNote(key: String): Pair<NoteDbEntry?, JSONObject?> =
		Pair(noteTableDao.get(key = key), graphite.getNote(key = key))

	suspend fun getChapter(key: String): ChapterDbEntry? =
		withContext(Dispatchers.IO) { chapterTableDao.get(key = key) }

	fun getNotebookAsFlow(key: String) = notebookTableDao.getAsFlow(key = key)

	fun openNotebookChapterAsFlow(
		notebookKey: String,
		chapterPath: List<String>,
		showArchived: Boolean,
		showLocked: Boolean
	): Flow<List<String>> =
		noteTableDao.getAllKeyFromNotebookAsFlow(
			notebookKey = notebookKey,
			chapterPath = chapterPath,
			showArchived = showArchived,
			showLocked = showLocked
		)

	suspend fun openNotebook(notebookKey: String): List<String> =
		noteTableDao.getAllKeyFromNotebook(notebookKey = notebookKey)

	suspend fun deleteNote(keyList: List<String>) {
		noteTableDao.delete(keyList = keyList)
		attachmentRepository.deleteWithNote(keyList)
		graphite.deleteNote(keyList = keyList)
	}

	fun deleteChapter(keyList: List<String>) = chapterTableDao.delete(keyList = keyList)

	suspend fun deleteNotebook(keyList: List<String>) {
		withContext(Dispatchers.IO) {
			val noteKeyList = noteTableDao.getAllKeyFromNotebook(keyList)
			noteTableDao.deleteWithNotebook(keyList = keyList)
			notebookTableDao.delete(keyList = keyList)
			graphite.deleteNote(keyList = noteKeyList)
		}
	}

	fun getAllKey(): Flow<List<NoteDbEntry>> = noteTableDao.getAllAsFlow()

	fun getAllAsFlow(): Flow<List<NoteDbEntry>> = noteTableDao.getAllAsFlow()

	fun getNoteAsFlow(notebookKey: String): Flow<List<NoteDbEntry>> =
		noteTableDao.getFromNotebookAsFlow(notebookKey = notebookKey)

	fun getChapterAsFlow(notebookKey: String): Flow<List<ChapterDbEntry>> =
		chapterTableDao.getFromNotebookAsFlow(notebookKey = notebookKey)

	suspend fun putNote(noteDbEntry: NoteDbEntry, noteContent: JSONObject?) =
		withContext(Dispatchers.IO) {
			insert(noteDbEntry)
			notebookTableDao.get(noteDbEntry.notebookKey).apply {
				this?.notebookSize =
					noteTableDao.countNotebookSize(notebookKey = noteDbEntry.notebookKey)
				this?.let { insert(it) }
			}
			graphite.putNote(key = noteDbEntry.key, noteContent = noteContent)
		}

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

		fun getInstance(graphite: Graphite): NoteRepository {
			synchronized(lock = this) {
				var instance = INSTANCE
				if (instance == null) {
					instance = NoteRepository(graphite = graphite)
					INSTANCE = instance
				}
				return instance
			}
		}
	}
}
