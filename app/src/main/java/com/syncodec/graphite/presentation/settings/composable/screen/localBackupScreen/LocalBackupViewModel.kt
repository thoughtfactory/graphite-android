package com.syncodec.graphite.presentation.settings.composable.screen.localBackupScreen

import android.content.Context
import androidx.lifecycle.ViewModel
import com.syncodec.graphite.di.repository.koinRepository.KoinRepository
import org.koin.android.annotation.KoinViewModel


@KoinViewModel
class LocalBackupViewModel(private val repository : KoinRepository) : ViewModel() {

	fun getRealmSnapshot(name : String, path : String) {
		repository.getRealmSnapshot(name, path)
	}

	fun restoreRealmSnapshot(context : Context, name : String, path : String) {
		repository.restoreRealmSnapshot(context, name, path) { _, _ ->}
	}
}
