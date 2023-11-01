package com.syncodec.graphite.importTest

import com.syncodec.graphite.di.model.dataExchanger.Exportable
import com.syncodec.graphite.di.repository.LockableRepo
import com.syncodec.graphite.presentation.settings.composable.viewModel.ImportDataViewModel
import io.mockk.mockk
import kotlinx.serialization.json.Json
import org.apache.commons.compress.archivers.zip.ZipFile
import org.apache.commons.compress.utils.SeekableInMemoryByteChannel
import org.junit.Test
import java.io.File


class ImportDataViewModelTest {
	val json = Json { ignoreUnknownKeys = true }

	val lockableRepo = mockk<LockableRepo>()
	val importDataViewModel = ImportDataViewModel(lockableRepo)

	val importTestPath = "./src/test/java/com/syncodec/graphite/importTest/"

	//	c44ea047-8d5a-4d15-9c9d-b85419a4a126.json
	private val uuidRegexString = "[0-9a-z]{8}\\b-[0-9a-z]{4}\\b-[0-9a-z]{4}\\b-[0-9a-z]{4}\\b-[0-9a-z]{12}"
	private val chapterFileRegex = """graphite_export_[0-9]+/chapter/$uuidRegexString.json""".toRegex()
	private val noteFileRegex = """graphite_export_[0-9]+/note/$uuidRegexString.json""".toRegex()
	private val bucketFileRegex = """graphite_export_[0-9]+/bucket/$uuidRegexString.json""".toRegex()
	private val bucketItemFileRegex = """graphite_export_[0-9]+/bucketItem/$uuidRegexString.json""".toRegex()
	private val tagFileRegex = """graphite_export_[0-9]+/tag/$uuidRegexString.json""".toRegex()
	private val attachmentFileRegexBase = """graphite_export_[0-9]+/attachment/"""
	private val attachmentFileRegex = """$attachmentFileRegexBase$uuidRegexString.*""".toRegex()

//	private val googleKeepNoteFileRegex = """.*.json""".toRegex()

	@Test
	fun importFromGraphite_test() {
		val file = File(importTestPath)
		val graphiteExportFile = File(file, "graphite_export.zip")

		val inMemoryByteChannel = SeekableInMemoryByteChannel(graphiteExportFile.readBytes())
		val zipFile = ZipFile(inMemoryByteChannel)
//
		val zipArchiveEntryList = zipFile.entries.toList()

		zipArchiveEntryList.filter { it.name.matches(noteFileRegex) }.forEach { zipArchiveEntry ->
			json.decodeFromString<Exportable.ExportNoteObject>(zipFile.getInputStream(zipArchiveEntry).readBytes().decodeToString()).let { exportNoteObject ->
				zipArchiveEntryList.filter{it.name.matches("$attachmentFileRegexBase${exportNoteObject.id}/.+".toRegex())}.forEach { zipArchiveEntry1 ->
					println(zipArchiveEntry1.name.split("$attachmentFileRegexBase${exportNoteObject.id}/".toRegex())[1])
				}
				println(exportNoteObject.id)
			}
		}

//		zipArchiveEntryList.filter { it.name.matches(attachmentFileRegex) }.forEach { zipArchiveEntry ->
//			json.decodeFromString<Exportable.ExportChapterObject>(zipFile.getInputStream(zipArchiveEntry).readBytes().decodeToString()).let {
//				println(it.id)
//			}
//		}

//		zipArchiveEntryList.filter { it.name.matches(noteDirRegex) }.forEach {
//			println(it.name)
//		}


//		val archivedJourneyList = zipArchiveEntryList.filter { it.name.endsWith(".json") }

	}

	@Test
	fun importFromGoogleKeep_test() {
		val file = File(importTestPath)
		val googleKeepExportFile = File(file, "GKeep.zip")

		val inMemoryByteChannel = SeekableInMemoryByteChannel(googleKeepExportFile.readBytes())
		val zipFile = ZipFile(inMemoryByteChannel)

		val zipArchiveEntryList = zipFile.entries.toList()

//		zipArchiveEntryList.filter { it.name.matches(googleKeepNoteFileRegex) }.forEach {
		zipArchiveEntryList.forEach {
			println(it.name)
		}
	}
}