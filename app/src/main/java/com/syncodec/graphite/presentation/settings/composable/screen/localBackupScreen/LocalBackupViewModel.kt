package com.syncodec.graphite.presentation.settings.composable.screen.localBackupScreen

import android.content.Context
import androidx.lifecycle.ViewModel
import com.syncodec.graphite.di.repository.AttachmentRepository
import com.syncodec.graphite.di.repository.koinRepository.KoinRepository
import org.koin.android.annotation.KoinViewModel
import java.io.File


@KoinViewModel
class LocalBackupViewModel(private val repository : KoinRepository, private val attachmentRepository : AttachmentRepository) : ViewModel() {

	fun getRealmSnapshot(name : String, path : String) {
		repository.getRealmSnapshot(name, path)
	}

	fun restoreRealmSnapshot(context : Context, name : String, path : String, callback : (Boolean, Exception?) -> Unit) {
		repository.restoreRealmSnapshot(context, name, path, callback)
	}

	fun restoreAttachment(file : File) {
		attachmentRepository.importAttachmentFromGraphite(file)
	}

	fun clearAttachment() {
		attachmentRepository.deleteAll()
	}
}
