package com.syncodec.graphite

import com.syncodec.graphite.di.model.local.BucketObject
import org.json.JSONObject
import org.junit.Test
import java.io.File


class ImportUnitTest {
	private val testPath = "./src/test/java/com/syncodec/graphite/test"

	@Test
	fun validateInputFile() {
		val importDir = File(testPath, "import")
		val bucketDir = File(importDir, "bucket")
		val bucketItemDir = File(bucketDir, "bucketItem")
		val chapterDir = File(importDir, "chapter")
		val noteDir = File(importDir, "note")

		if (bucketDir.isDirectory) {
			bucketDir.listFiles()?.forEach {
				val bucketString = it.readText()
				val jsonObject = JSONObject(bucketString)
				BucketObject(jsonObject).let {
					println(it.id)
				}
			}
		}
	}
}
