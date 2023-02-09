package com.syncodec.graphite.presentation.settings.composable.screen.importDataScreen.dialog


//@KoinViewModel
//class ImportDataViewModel(private val repository : KoinRepository) : ViewModel() {
//
//	private val objectMapper = jsonMapper {
//		addModule(
//			kotlinModule().addDeserializer(
//				RealmUUID::class.java,
//				RealmUUIDDeserializer()
//			)
//		)
//	}.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
//
//	fun importData(restoreFolder : File, sevenZFile : SevenZFile, progress : (Int, Int) -> Unit, onComplete: () -> Unit) {
//		sevenZFile.entries.forEach {
//			val entry = it
//			val entryName = entry.name
//			val entryFile = File(restoreFolder, entryName)
//			entryFile.parentFile?.mkdirs()
//			entryFile.delete()
//			entryFile.createNewFile()
//			val entryInputStream = sevenZFile.getInputStream(entry)
//			entryInputStream.copyTo(entryFile.outputStream())
//			entryInputStream.close()
//		}
//
//		sevenZFile.close()
//
//		restoreFolder.listFiles()?.forEach {
//			Log.i("npr71", "file : ${it.name}")
//		}
//	}
//
//	fun importData(context : Context, name : String, path : String, callback : (Boolean, Exception?) -> Unit) {
//		repository.readRealmSnapshot(context, name, path, callback)
//	}
//}
