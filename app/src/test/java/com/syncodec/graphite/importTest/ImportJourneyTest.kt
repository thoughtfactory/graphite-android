package com.syncodec.graphite.importTest

import com.syncodec.graphite.di.model.dataExchanger.Importable
import kotlinx.serialization.json.Json
import org.junit.Test
import java.io.File


class ImportJourneyTest  {

	private val journeyDirPath = "./src/test/java/com/syncodec/graphite/importTest/journey/"
	private val json = Json { ignoreUnknownKeys = true }

	@Test
	fun deserializeJourneyDataTest() {
		val journeyDir = File(journeyDirPath)

		journeyDir.listFiles()?.first { it.extension == "json" }?.let {
			json.decodeFromString<Importable.JourneyNote>(it.readText()).let {
				println(it)
			}
		}

	}

}
