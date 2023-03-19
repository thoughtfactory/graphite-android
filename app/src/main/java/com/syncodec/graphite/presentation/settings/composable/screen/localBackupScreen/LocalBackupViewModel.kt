package com.syncodec.graphite.presentation.settings.composable.screen.localBackupScreen

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syncodec.graphite.di.repository.koinRepository.KoinRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import java.io.File
import java.io.InputStream


@KoinViewModel
class LocalBackupViewModel(private val repository : KoinRepository) : ViewModel() {
	fun generateSnapshot(callback : (File) -> Unit) {
		viewModelScope.launch(Dispatchers.IO) {
			repository.snapshot.generate(callback)
		}
	}

	fun restore(inputStream : InputStream, callback : (Boolean) -> Unit) {
		viewModelScope.launch(Dispatchers.IO) {
			repository.snapshot.restore(inputStream = inputStream, callback = callback)
		}
	}
}
